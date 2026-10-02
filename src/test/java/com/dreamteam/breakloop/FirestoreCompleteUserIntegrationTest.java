package com.dreamteam.breakloop;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.google.cloud.firestore.FieldValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FirestoreCompleteUserIntegrationTest {

    @Autowired
    private Firestore firestore;

    @Test
    void crearUsuarioDePruebaConTodosLosDatos() throws Exception {

        // =========================================================
        // 1. CREAR USUARIO NUEVO EN FIREBASE AUTH
        // =========================================================

        String email = "backend_test_" + System.currentTimeMillis()
                + "@breakloop.test";

        String password = "Test1234!";
        Date now = new Date();
        UserRecord.CreateRequest userRequest = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password)
                .setEmailVerified(false);

        UserRecord userRecord = FirebaseAuth.getInstance()
                .createUser(userRequest);

        String uid = userRecord.getUid();

        assertNotNull(uid);

        System.out.println("======================================");
        System.out.println("USUARIO DE PRUEBA CREADO");
        System.out.println("UID: " + uid);
        System.out.println("EMAIL: " + email);
        System.out.println("======================================");

        // =========================================================
        // 2. DOCUMENTO PRINCIPAL DEL USUARIO
        // users/{uid}
        // =========================================================

        DocumentReference userReference = firestore.collection("users")
                .document(uid);

        Map<String, Object> userData = new HashMap<>();

        userData.put("email", email);
        userData.put("createdAt", FieldValue.serverTimestamp());
        userData.put("updatedAt", FieldValue.serverTimestamp());
        userReference.set(userData).get();

        // =========================================================
        // 3. PROGRESS
        // users/{uid}/progress/current
        // =========================================================

        Map<String, Object> progressData = new HashMap<>();

        progressData.put("xp", 250);
        progressData.put("streak", 8);
        progressData.put("petLevel", 4);
        progressData.put("petHealth", 90);

        userReference
                .collection("progress")
                .document("current")
                .set(progressData)
                .get();

        // =========================================================
        // 4. USAGE
        // users/{uid}/usage/2026-10-02
        // =========================================================

        Map<String, Object> usageData = new HashMap<>();

        usageData.put("screenTimeMs", 7200000L);
        usageData.put("pickups", 42);
        usageData.put("unlocks", 5);
        usageData.put("isPartial", false);
        usageData.put("updatedAt", FieldValue.serverTimestamp());

        userReference
                .collection("usage")
                .document("2026-10-02")
                .set(usageData)
                .get();

        // =========================================================
        // 5. FOCUS SESSION
        // users/{uid}/focusSessions/session001
        // =========================================================

        Map<String, Object> focusSessionData = new HashMap<>();

        focusSessionData.put("startTime", now);
        focusSessionData.put("duration", 1500);
        focusSessionData.put("type", "POMODORO");
        focusSessionData.put("status", "COMPLETED");
        focusSessionData.put("xpEarned", 20);

        userReference
                .collection("focusSessions")
                .document("session001")
                .set(focusSessionData)
                .get();

        // =========================================================
        // 6. GOAL
        // users/{uid}/goals/goal001
        // =========================================================

        Map<String, Object> goalData = new HashMap<>();

        goalData.put("targetMinutes", 100);
        goalData.put("startDate", "2026-10-01");
        goalData.put("endDate", "2026-10-07");
        goalData.put("achieved", false);

        userReference
                .collection("goals")
                .document("goal001")
                .set(goalData)
                .get();

        // =========================================================
        // 7. OFFLINE ACTIVITY
        // offlineActivities/activity001
        // =========================================================

        Map<String, Object> activityData = new HashMap<>();

        activityData.put("title", "Take a walk");
        activityData.put(
                "description",
                "Go for a short walk without your phone.");
        activityData.put("category", "PHYSICAL");
        activityData.put("durationMinutes", 20);
        activityData.put("difficulty", "EASY");
        activityData.put("active", true);

        firestore
                .collection("offlineActivities")
                .document("activity001")
                .set(activityData)
                .get();

        // =========================================================
        // 8. VERIFICAR USUARIO
        // =========================================================

        DocumentSnapshot userSnapshot = userReference.get().get();

        assertTrue(userSnapshot.exists());
        assertEquals(email, userSnapshot.getString("email"));

        // =========================================================
        // 9. VERIFICAR PROGRESS
        // =========================================================

        DocumentSnapshot progressSnapshot = userReference
                .collection("progress")
                .document("current")
                .get()
                .get();

        assertTrue(progressSnapshot.exists());

        assertEquals(
                250L,
                progressSnapshot.getLong("xp"));

        assertEquals(
                8L,
                progressSnapshot.getLong("streak"));

        assertEquals(
                4L,
                progressSnapshot.getLong("petLevel"));

        assertEquals(
                90L,
                progressSnapshot.getLong("petHealth"));

        // =========================================================
        // 10. VERIFICAR USAGE
        // =========================================================

        DocumentSnapshot usageSnapshot = userReference
                .collection("usage")
                .document("2026-10-02")
                .get()
                .get();

        assertTrue(usageSnapshot.exists());

        assertEquals(
                7200000L,
                usageSnapshot.getLong("screenTimeMs"));

        assertEquals(
                42L,
                usageSnapshot.getLong("pickups"));

        assertEquals(
                5L,
                usageSnapshot.getLong("unlocks"));

        assertFalse(
                usageSnapshot.getBoolean("isPartial"));

        // =========================================================
        // 11. VERIFICAR FOCUS SESSION
        // =========================================================

        DocumentSnapshot focusSnapshot = userReference
                .collection("focusSessions")
                .document("session001")
                .get()
                .get();

        assertTrue(focusSnapshot.exists());

        assertEquals(
                1500L,
                focusSnapshot.getLong("duration"));

        assertEquals(
                "POMODORO",
                focusSnapshot.getString("type"));

        assertEquals(
                "COMPLETED",
                focusSnapshot.getString("status"));

        assertEquals(
                20L,
                focusSnapshot.getLong("xpEarned"));

        // =========================================================
        // 12. VERIFICAR GOAL
        // =========================================================

        DocumentSnapshot goalSnapshot = userReference
                .collection("goals")
                .document("goal001")
                .get()
                .get();

        assertTrue(goalSnapshot.exists());

        assertEquals(
                100L,
                goalSnapshot.getLong("targetMinutes"));

        assertEquals(
                "2026-10-01",
                goalSnapshot.getString("startDate"));

        assertEquals(
                "2026-10-07",
                goalSnapshot.getString("endDate"));

        assertFalse(
                goalSnapshot.getBoolean("achieved"));

        // =========================================================
        // 13. VERIFICAR OFFLINE ACTIVITY
        // =========================================================

        DocumentSnapshot activitySnapshot = firestore
                .collection("offlineActivities")
                .document("activity001")
                .get()
                .get();

        assertTrue(activitySnapshot.exists());

        assertEquals(
                "Take a walk",
                activitySnapshot.getString("title"));

        assertEquals(
                "PHYSICAL",
                activitySnapshot.getString("category"));

        assertEquals(
                20L,
                activitySnapshot.getLong("durationMinutes"));

        assertEquals(
                "EASY",
                activitySnapshot.getString("difficulty"));

        assertTrue(
                activitySnapshot.getBoolean("active"));

        // =========================================================
        // RESULTADO
        // =========================================================

        System.out.println("======================================");
        System.out.println("TEST COMPLETADO CORRECTAMENTE");
        System.out.println("UID: " + uid);
        System.out.println("Email: " + email);
        System.out.println("Datos guardados en Firestore.");
        System.out.println("======================================");
    }
}