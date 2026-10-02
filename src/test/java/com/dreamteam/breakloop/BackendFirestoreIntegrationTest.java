package com.dreamteam.breakloop;

import com.dreamteam.breakloop.model.FocusSession;
import com.dreamteam.breakloop.model.Goal;
import com.dreamteam.breakloop.model.OfflineActivity;
import com.dreamteam.breakloop.model.Progress;
import com.dreamteam.breakloop.model.Usage;
import com.dreamteam.breakloop.model.User;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BackendFirestoreIntegrationTest {

    @Autowired
    private Firestore firestore;

    @Test
    void shouldCreateAndReadCompleteUserData() throws Exception {

        // --------------------------------------------------
        // 1. Create Firebase Authentication user
        // --------------------------------------------------

        String email = "backend_test_" + System.currentTimeMillis() + "@breakloop.test";

        String password = "Test1234!";

        UserRecord firebaseUser = FirebaseAuth.getInstance()
                .createUser(
                        new com.google.firebase.auth.UserRecord.CreateRequest()
                                .setEmail(email)
                                .setPassword(password));

        String uid = firebaseUser.getUid();

        assertNotNull(uid);

        System.out.println("Created Firebase user: " + uid);

        // --------------------------------------------------
        // 2. Create users/{uid}
        // --------------------------------------------------

        Map<String, Object> userData = new HashMap<>();

        userData.put("email", email);
        userData.put(
                "createdAt",
                com.google.cloud.firestore.FieldValue.serverTimestamp());
        userData.put(
                "updatedAt",
                com.google.cloud.firestore.FieldValue.serverTimestamp());

        firestore
                .collection("users")
                .document(uid)
                .set(userData)
                .get();

        // --------------------------------------------------
        // 3. Create progress/current
        // --------------------------------------------------

        Map<String, Object> progressData = new HashMap<>();

        progressData.put("xp", 0);
        progressData.put("streak", 0);
        progressData.put("petLevel", 1);
        progressData.put("petHealth", 100);

        firestore
                .collection("users")
                .document(uid)
                .collection("progress")
                .document("current")
                .set(progressData)
                .get();

        // --------------------------------------------------
        // 4. Create usage
        // --------------------------------------------------

        String date = "2026-10-02";

        Map<String, Object> usageData = new HashMap<>();

        usageData.put("screenTimeMs", 3600000L);
        usageData.put("pickups", 25);
        usageData.put("unlocks", 10);
        usageData.put("isPartial", false);
        usageData.put(
                "updatedAt",
                com.google.cloud.firestore.FieldValue.serverTimestamp());

        firestore
                .collection("users")
                .document(uid)
                .collection("usage")
                .document(date)
                .set(usageData)
                .get();

        // --------------------------------------------------
        // 5. Create focus session
        // --------------------------------------------------

        Map<String, Object> focusData = new HashMap<>();

        focusData.put(
                "startTime",
                com.google.cloud.firestore.FieldValue.serverTimestamp());
        focusData.put("duration", 1500);
        focusData.put("type", "POMODORO");
        focusData.put("status", "COMPLETED");
        focusData.put("xpEarned", 20);

        firestore
                .collection("users")
                .document(uid)
                .collection("focusSessions")
                .document("session001")
                .set(focusData)
                .get();

        // --------------------------------------------------
        // 6. Create goal
        // --------------------------------------------------

        Map<String, Object> goalData = new HashMap<>();

        goalData.put("targetMinutes", 100);
        goalData.put("startDate", "2026-10-01");
        goalData.put("endDate", "2026-10-07");
        goalData.put("achieved", false);

        firestore
                .collection("users")
                .document(uid)
                .collection("goals")
                .document("goal001")
                .set(goalData)
                .get();

        // --------------------------------------------------
        // 7. Create offline activity
        // --------------------------------------------------

        Map<String, Object> activityData = new HashMap<>();

        activityData.put("title", "Read a book");
        activityData.put(
                "description",
                "Read a book for 20 minutes");
        activityData.put("category", "LEISURE");
        activityData.put("durationMinutes", 20);
        activityData.put("difficulty", "EASY");
        activityData.put("active", true);

        firestore
                .collection("offlineActivities")
                .document("integration_test_activity")
                .set(activityData)
                .get();

        // ==================================================
        // VERIFICATION
        // ==================================================

        // --------------------------------------------------
        // Verify user
        // --------------------------------------------------

        var userDocument = firestore
                .collection("users")
                .document(uid)
                .get()
                .get();

        assertTrue(userDocument.exists());
        assertEquals(email, userDocument.getString("email"));

        // --------------------------------------------------
        // Verify progress
        // --------------------------------------------------

        var progressDocument = firestore
                .collection("users")
                .document(uid)
                .collection("progress")
                .document("current")
                .get()
                .get();

        assertTrue(progressDocument.exists());
        assertEquals(0L, progressDocument.getLong("xp"));
        assertEquals(0L, progressDocument.getLong("streak"));
        assertEquals(1L, progressDocument.getLong("petLevel"));
        assertEquals(100L, progressDocument.getLong("petHealth"));

        // --------------------------------------------------
        // Verify usage
        // --------------------------------------------------

        var usageDocument = firestore
                .collection("users")
                .document(uid)
                .collection("usage")
                .document(date)
                .get()
                .get();

        assertTrue(usageDocument.exists());
        assertEquals(
                3600000L,
                usageDocument.getLong("screenTimeMs"));
        assertEquals(
                25L,
                usageDocument.getLong("pickups"));
        assertEquals(
                10L,
                usageDocument.getLong("unlocks"));

        // --------------------------------------------------
        // Verify focus session
        // --------------------------------------------------

        var focusDocument = firestore
                .collection("users")
                .document(uid)
                .collection("focusSessions")
                .document("session001")
                .get()
                .get();

        assertTrue(focusDocument.exists());
        assertEquals(
                1500L,
                focusDocument.getLong("duration"));
        assertEquals(
                "POMODORO",
                focusDocument.getString("type"));
        assertEquals(
                "COMPLETED",
                focusDocument.getString("status"));
        assertEquals(
                20L,
                focusDocument.getLong("xpEarned"));

        // --------------------------------------------------
        // Verify goal
        // --------------------------------------------------

        var goalDocument = firestore
                .collection("users")
                .document(uid)
                .collection("goals")
                .document("goal001")
                .get()
                .get();

        assertTrue(goalDocument.exists());
        assertEquals(
                100L,
                goalDocument.getLong("targetMinutes"));
        assertEquals(
                "2026-10-01",
                goalDocument.getString("startDate"));
        assertEquals(
                "2026-10-07",
                goalDocument.getString("endDate"));
        assertFalse(goalDocument.getBoolean("achieved"));

        // --------------------------------------------------
        // Verify offline activity
        // --------------------------------------------------

        var activityDocument = firestore
                .collection("offlineActivities")
                .document("integration_test_activity")
                .get()
                .get();

        assertTrue(activityDocument.exists());
        assertEquals(
                "Read a book",
                activityDocument.getString("title"));
        assertEquals(
                "LEISURE",
                activityDocument.getString("category"));
        assertEquals(
                20L,
                activityDocument.getLong("durationMinutes"));
        assertTrue(activityDocument.getBoolean("active"));

        // --------------------------------------------------
        // Final output
        // --------------------------------------------------

        System.out.println();
        System.out.println("========================================");
        System.out.println("BACKEND FIRESTORE TEST PASSED");
        System.out.println("========================================");
        System.out.println("Firebase UID: " + uid);
        System.out.println("Email: " + email);
        System.out.println("User: OK");
        System.out.println("Progress: OK");
        System.out.println("Usage: OK");
        System.out.println("Focus Session: OK");
        System.out.println("Goal: OK");
        System.out.println("Offline Activity: OK");
        System.out.println("========================================");
    }
}