# Friends contract

Friends are shared with the Flutter app, which writes the same Firestore
documents directly. **This backend reads and writes exactly the same paths and
fields. Do not rename collections or fields**: a Kotlin user and a Flutter user
must be able to be friends with each other.

## Firestore schema

| Path | Fields |
|---|---|
| `users/{uid}` | `email`, `displayName`, `friendCode`, `createdAt` (server timestamp). Written with merge; the document may hold other fields |
| `friendCodes/{CODE}` | `uid` |
| `users/{uid}/friends/{friendUid}` | `since` (server timestamp) |
| `users/{uid}/progress/current` | `streak`, `petLevel` (read only here) |

Rules:

- **Code:** 4 characters from `ABCDEFGHJKLMNPQRSTUVWXYZ23456789`, drawn with `SecureRandom`. It is reserved in a Firestore transaction (user document + `friendCodes/{code}`); if the code exists another one is drawn, up to 10 attempts, then `500`.
- **Default `displayName`:** the part of the email before `@`.
- **Existing profile:** `users/{uid}` has a `friendCode`. It is never recreated.
- **Friendship:** written in both directions in one `WriteBatch`. Repeating it overwrites `since` (idempotent).
- **Defaults when reading a friend:** `displayName = ""`, `streak = 0`, `petLevel = 1` (document or field missing).
- **Incoming code:** `trim` + uppercase.

## Authentication

All routes require `Authorization: Bearer <Firebase ID token>` whose uid equals
`{uid}` in the path: `401` if missing or invalid, `403` on a different uid.

## Endpoints

### `POST /api/users/{uid}/friend-profile`

Returns the profile, creating it with a new code if there is none. Calling it
twice returns the same code. The body may be empty, and `email` may be blank:
the email of the token is used then.

```json
{ "email": "ana@mail.com" }
```

`200`:

```json
{ "uid": "u1", "email": "ana@mail.com", "displayName": "ana", "friendCode": "AB3K" }
```

### `POST /api/users/{uid}/friends`

```json
{ "code": "AB3K" }
```

`200`: `{ "result": "ADDED" }`

| `result` | Meaning |
|---|---|
| `ADDED` | Friendship written in both directions |
| `NOT_FOUND` | No `friendCodes/{CODE}` document |
| `OWN_CODE` | The code belongs to `{uid}` |

`400` if `code` is missing or blank.

### `GET /api/users/{uid}/friends`

Friends ordered by `displayName` (case-insensitive). Friends, and each friend's
profile and progress, are read in parallel.

```json
[ { "uid": "u2", "displayName": "bob", "streak": 4, "petLevel": 2 } ]
```
