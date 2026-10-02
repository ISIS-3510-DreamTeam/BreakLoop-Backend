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
