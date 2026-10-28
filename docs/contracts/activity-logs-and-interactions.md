# Contract: activity catalog, suggestion interactions and activity logs

For the Kotlin and Flutter apps. Collections and fields are also read directly by `BreakLoop-Analytics`.

## General rules

- **Units:** every `*At` field is epoch **milliseconds**. `availableMin` and `durationMin` are **minutes**.
- **Idempotent writes:** the client generates a UUID and uses it as the document id. Repeating the same request never creates duplicates. The id in the URL wins over the `id` in the body.
- **Auth (user endpoints):** `Authorization: Bearer <Firebase ID token>`. `401` missing/invalid token, `403` token uid ≠ `{uid}` in the path. Catalog endpoints are open.
- **Coordinates:** the server always rounds `latitude`/`longitude` to 2 decimals (~1 km), whatever the app sends. Send `null` if there is no location.
- **Errors:** `{ "error": "Bad Request", "message": "..." }` with `400` (validation, unknown enum value, malformed JSON), `404`, `409`.
- **Nulls:** fields marked nullable may be sent as `null` or omitted.

## Enums

| Enum | Values |
|---|---|
| `category` | `MOVE` `CREATE` `RELAX` `SOCIALIZE` |
| `timeOfDay` | `MORNING` (05–11) `AFTERNOON` (12–17) `EVENING` (18–21) `NIGHT` (all other hours, 22–04) |
| `weather` | `CLEAR` `CLOUDY` `RAIN` `SNOW` |
| `placeLabel` | `HOME` `UNIVERSITY` `OTHER` |
| `outcome` | `PENDING` `ACCEPTED` `OTHER_ACTIVITY` `NO_ACTIVITY` |
| `declineReason` | `NOT_NOW` `NOT_INTERESTED` `NOT_ENOUGH_TIME` `WRONG_PLACE` `WEATHER` `PREFERRED_OTHER` `UNKNOWN` |
| `reasons[]` | `MATCHES_INTEREST` `GOOD_WEATHER` `RIGHT_TIME_OF_DAY` `FITS_AVAILABLE_TIME` |
| `source` (log) | `RECOMMENDED` `BROWSED` |

`timeOfDay` uses the hour of the **device's local time**, with the same ranges in Kotlin and Flutter.

## Catalog

`GET /api/offline-activities[?category=MOVE]`

```json
{
  "id": "walk-park", "title": "Walk in the park", "category": "MOVE",
  "durationMin": 15, "xp": 20, "prompt": "Take a short walk.", "outdoor": true,
  "weather": ["CLEAR", "CLOUDY"], "timeOfDay": ["MORNING", "AFTERNOON"],
  "active": true, "updatedAt": 1760000000000
}
```

(Legacy fields `description`, `durationMinutes`, `difficulty` may also appear.)

`GET /api/offline-activities/version` → `{ "version": "9f2c4a1b7d3e8a60", "updatedAt": 1760000000000 }`.
Download the catalog only when `version` differs from the one you stored.

## Suggestion interactions

One document per **visit** to the Offline screen: `users/{uid}/suggestionInteractions/{interactionId}`.

`PUT /api/users/{uid}/suggestion-interactions/{id}` — on open (PENDING):

```json
{
  "openedAt": 1760000000000,
  "closedAt": null,
  "timeOfDay": "MORNING",
  "isWeekend": false,
  "weather": "CLEAR",
  "placeLabel": "HOME",
  "latitude": 4.7110,
  "longitude": -74.0721,
  "shownSuggestions": [
    { "activityId": "walk-park", "category": "MOVE", "availableMin": 15, "score": 0.82,
      "reasons": ["GOOD_WEATHER", "FITS_AVAILABLE_TIME"], "shownAt": 1760000000000 }
  ],
  "outcome": "PENDING",
  "acceptedActivityId": null,
  "chosenActivityId": null,
  "declineReason": null
}
```

Closed as accepted (same id, second PUT):

```json
{ "...": "same fields as above, plus", "closedAt": 1760000090000, "outcome": "ACCEPTED", "acceptedActivityId": "walk-park" }
```

Closed with another activity: `"outcome": "OTHER_ACTIVITY", "chosenActivityId": "doodle"`.
Closed without starting anything: `"outcome": "NO_ACTIVITY", "declineReason": "NOT_NOW"`.

The response is `200` with the stored document (id, rounded coordinates, defaults applied).

Required: `openedAt`, `timeOfDay`, `isWeekend`, `outcome`. Rules (`400` unless noted):

- `outcome != PENDING` requires `closedAt` (and `closedAt >= openedAt`).
- `ACCEPTED` requires `acceptedActivityId`, and it must be one of the `shownSuggestions[].activityId`.
- `OTHER_ACTIVITY` requires `chosenActivityId`.
- `declineReason` defaults: `OTHER_ACTIVITY` → `PREFERRED_OTHER`, `NO_ACTIVITY` → `UNKNOWN`. It is ignored (stored as `null`) for `PENDING` and `ACCEPTED`.
- A closed interaction cannot go back to `PENDING` → **`409`**. Treat 409 on a stale PENDING as "already done": drop that queued item, do not retry.

Batch: `POST .../batch` with a JSON array; every item needs its own `id`. All items are validated first (any invalid → `400`, nothing written). Response `{ "written": 2, "skipped": ["<id>"] }`: `skipped` are stale PENDING items that would have reopened a closed interaction.

Read: `GET .../suggestion-interactions[?since=<openedAt ms>&limit=<n>]` (newest first), `GET .../suggestion-interactions/{id}` (`404` if missing).

## Activity logs

`users/{uid}/activityLogs/{logId}`. `PUT /api/users/{uid}/activity-logs/{logId}`:

```json
{
  "id": "5b0f…",
  "activityId": "walk-park",
  "category": "MOVE",
  "source": "RECOMMENDED",
  "interactionId": "7c1e…",
  "startedAt": 1760000100000,
  "completedAt": null,
  "xp": 20,
  "timeOfDay": "MORNING",
  "weather": "CLEAR",
  "availableMin": 15,
  "placeLabel": "HOME",
  "latitude": 4.71,
  "longitude": -74.07
}
```

Required: `activityId`, `category`, `source`, `startedAt`, `xp`, `timeOfDay`, `availableMin`. Nullable: `interactionId` (null for `BROWSED` outside a visit), `completedAt`, `weather`, `placeLabel`, coordinates. `completedAt >= startedAt`.
Send the log when the activity starts, and PUT again with `completedAt` when it finishes. If a late "started" retry arrives after "completed", the server keeps the existing `completedAt`.

Batch (`POST .../batch`, array with an `id` each → `{ "written": n, "skipped": [] }`) and reads (`GET` list with `?since=<startedAt ms>&limit=<n>`, `GET /{logId}`) work like interactions.

## Daily usage

`users/{uid}/usage/{date}` has a new `flaggedTimeMs` (long, default `0`): total ms in apps the user marked as distracting. Total only, never per app.

## App flow

1. Enter Offline → generate the interaction UUID and `PUT` with `PENDING` (include context: `timeOfDay`, `isWeekend`, weather, place, location).
2. Each time the suggested card changes (user taps 5/15/30 min), append it to `shownSuggestions` and `PUT` again.
3. User starts the suggested activity → `ACCEPTED` (+ `closedAt`). Starts a different one → `OTHER_ACTIVITY` with `chosenActivityId`. Leaves without starting anything → `NO_ACTIVITY` (optional reason chips → `declineReason`). In all three, also create the activity log (`interactionId` = this visit).
4. If the app was force-closed, on the next start close the pending interaction as `NO_ACTIVITY` (`closedAt` = last known time).
5. All `PUT`s go through the local queue and are safe to retry. On `409` for a PENDING write, discard it.
6. Compute `timeOfDay` with the ranges above, identically in Kotlin and Flutter.
