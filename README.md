# BreakLoop Backend - How to Use

## Requirements

- Java 17 or higher
- Maven
- Firebase project with Firestore enabled
- Firebase Admin SDK service account credentials

## Firebase Configuration

The backend uses Firebase Admin SDK to connect to Firestore. Before running the backend, configure the `GOOGLE_APPLICATION_CREDENTIALS` environment variable with the path to your Firebase service account JSON file.

In PowerShell:

```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\firebase-service-account.json"
```

Verify that the file exists:

```powershell
Test-Path $env:GOOGLE_APPLICATION_CREDENTIALS
```

It should return `True`.

> **Important:** Never commit the Firebase service account JSON file to the repository.

## Run the Backend

From the project root:

```powershell
mvn spring-boot:run
```

The backend will start on:

```text
http://localhost:8080
```

## Run Tests

To run all tests:

```powershell
mvn test
```

To run the Firebase integration test:

```powershell
mvn test -Dtest=FirestoreCompleteUserIntegrationTest
```

This test creates a Firebase Authentication test user and populates its corresponding Firestore data.

## Offline Activities API

The backend exposes the following REST endpoints:

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/offline-activities` | Get active offline activities |
| GET | `/api/offline-activities/{id}` | Get an activity by ID |
| POST | `/api/offline-activities` | Create an activity |
| PUT | `/api/offline-activities/{id}` | Update an activity |
| DELETE | `/api/offline-activities/{id}` | Deactivate an activity |

Example:

```text
GET http://localhost:8080/api/offline-activities
```

## Catalog, suggestion interactions and activity logs (BQ12)

Contract for the apps: [`docs/contracts/activity-logs-and-interactions.md`](docs/contracts/activity-logs-and-interactions.md).
Collection and field names are also read directly by `BreakLoop-Analytics`; do not rename them without telling that team.

### New endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/offline-activities?category=MOVE` | none | Active activities, optional category filter |
| GET | `/api/offline-activities/version` | none | `{ version, updatedAt }` of the active catalog |
| POST | `/api/offline-activities/seed` | `X-Admin-Key` | Upsert the bundled catalog (`catalog/activities.json`) by id |
| PUT | `/api/offline-activities/{id}` | none | Upsert an activity with that id (POST create still works) |
| PUT | `/api/users/{uid}/suggestion-interactions/{id}` | Bearer | Upsert one visit to the Offline screen |
| POST | `/api/users/{uid}/suggestion-interactions/batch` | Bearer | Upsert a list (WriteBatch, 500 per commit) |
| GET | `/api/users/{uid}/suggestion-interactions` | Bearer | List, newest first (`?since=<ms>&limit=<n>`) |
| GET | `/api/users/{uid}/suggestion-interactions/{id}` | Bearer | One interaction (404 if missing) |
| PUT | `/api/users/{uid}/activity-logs/{logId}` | Bearer | Upsert one activity log |
| POST | `/api/users/{uid}/activity-logs/batch` | Bearer | Upsert a list |
| GET | `/api/users/{uid}/activity-logs` | Bearer | List, newest first (`?since=<ms>&limit=<n>`) |
| GET | `/api/users/{uid}/activity-logs/{logId}` | Bearer | One log (404 if missing) |

Existing endpoints are unchanged and stay open. Daily usage (`users/{uid}/usage`) now accepts and returns `flaggedTimeMs` (long, default 0): total time in distracting apps, never per app.

### Authentication (new user endpoints only)

```bash
curl -X PUT http://localhost:8080/api/users/$UID/activity-logs/$(uuidgen) \
  -H "Authorization: Bearer $FIREBASE_ID_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"activityId":"walk-park","category":"MOVE","source":"RECOMMENDED","startedAt":1760000000000,"xp":20,"timeOfDay":"MORNING","availableMin":15}'
```

`401` if the token is missing or invalid, `403` if its uid is not the `{uid}` in the path.

### Idempotency

Every write is an upsert keyed by a client-generated UUID, which is the document id. Sending the same request again never creates a duplicate, so clients can retry freely from their local queue. The id in the path wins over the `id` in the body.

### Examples

```bash
# Open the Offline screen
curl -X PUT http://localhost:8080/api/users/$UID/suggestion-interactions/$IID \
  -H "Authorization: Bearer $FIREBASE_ID_TOKEN" -H "Content-Type: application/json" \
  -d '{"openedAt":1760000000000,"timeOfDay":"MORNING","isWeekend":false,"outcome":"PENDING",
       "shownSuggestions":[{"activityId":"walk-park","category":"MOVE","availableMin":15,"score":0.8,"reasons":["GOOD_WEATHER"],"shownAt":1760000000000}]}'

# Seed the catalog
curl -X POST http://localhost:8080/api/offline-activities/seed -H "X-Admin-Key: $BREAKLOOP_ADMIN_KEY"

# Catalog version
curl http://localhost:8080/api/offline-activities/version
```

### Properties

| Property | Env variable | Default | Purpose |
|---|---|---|---|
| `breakloop.admin.key` | `BREAKLOOP_ADMIN_KEY` | empty | Key for `POST /seed`. Empty disables the endpoint (403) |
| `breakloop.auth.enabled` | `BREAKLOOP_AUTH_ENABLED` | `true` | Require a Firebase ID token on the new user endpoints |

> **Important:** the admin key is set through the environment (or Cloud Run config) and is **never** committed to the repository.

To run only the new tests (no Firebase credentials needed):

```powershell
mvn "-Dtest=CoordinatesTest,SuggestionInteractionServiceTest,ActivityLogServiceTest,OfflineActivityServiceTest,SuggestionInteractionControllerTest,ActivityLogControllerTest,OfflineActivityController*Test,FirebaseAuthInterceptorTest,NewEndpointsAuthWiringTest" test
```
