package com.dreamteam.breakloop.service;

import com.dreamteam.breakloop.exception.NotFoundException;
import com.dreamteam.breakloop.model.UserAchievement;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.SetOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserAchievementService {

    private final Firestore firestore;
    private final AchievementService achievements;

    public UserAchievementService(Firestore firestore, AchievementService achievements) {
        this.firestore = firestore;
        this.achievements = achievements;
    }

    /** All unlocks of a user, from users/{uid}/achievements. */
    public List<UserAchievement> getUnlocked(String uid) {
        try {
            List<UserAchievement> result = new ArrayList<>();

            for (QueryDocumentSnapshot document : achievementsRef(uid).get().get().getDocuments()) {
                Long unlockedAt = document.getLong("unlockedAt");
                result.add(new UserAchievement(
                        document.getId(),
                        unlockedAt != null ? unlockedAt : 0L));
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving achievements", e);
        }
    }

    /**
     * Idempotent unlock. The first write creates the document. A repeated write
     * keeps the earliest date, so the same badge sent from two devices ends up
     * with one document.
     */
    public UserAchievement unlock(String uid, String achievementId, long unlockedAt) {
        if (!isKnown(achievementId)) {
            throw new NotFoundException("Unknown achievement: " + achievementId);
        }

        DocumentReference reference = achievementsRef(uid).document(achievementId);

        try {
            long stored = firestore.runTransaction(transaction -> {
                DocumentSnapshot snapshot = transaction.get(reference).get();

                if (!snapshot.exists()) {
                    transaction.set(reference, Map.of("unlockedAt", unlockedAt));
                    return unlockedAt;
                }

                Long existing = snapshot.getLong("unlockedAt");
                long existingAt = existing != null ? existing : unlockedAt;

                if (unlockedAt < existingAt) {
                    transaction.set(reference, Map.of("unlockedAt", unlockedAt), SetOptions.merge());
                    return unlockedAt;
                }

                return existingAt;
            }).get();

            return new UserAchievement(achievementId, stored);

        } catch (Exception e) {
            throw new RuntimeException("Error saving achievement", e);
        }
    }

    /** Developer tools only: removes every unlock of a user. */
    public void deleteAll(String uid) {
        try {
            List<QueryDocumentSnapshot> documents = achievementsRef(uid).get().get().getDocuments();
            if (documents.isEmpty()) return;

            var batch = firestore.batch();
            for (QueryDocumentSnapshot document : documents) {
                batch.delete(document.getReference());
            }
            batch.commit().get();

        } catch (Exception e) {
            throw new RuntimeException("Error deleting achievements", e);
        }
    }

    private boolean isKnown(String achievementId) {
        return achievements.getCatalog().stream()
                .anyMatch(a -> a.getId().equals(achievementId));
    }

    private CollectionReference achievementsRef(String uid) {
        return firestore
                .collection("users")
                .document(uid)
                .collection("achievements");
    }
}