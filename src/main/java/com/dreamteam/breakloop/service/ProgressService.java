package com.dreamteam.breakloop.service;

import com.dreamteam.breakloop.model.Progress;
import com.dreamteam.breakloop.model.ProgressUpdate;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProgressService {

    private final Firestore firestore;

    public ProgressService(Firestore firestore) {
        this.firestore = firestore;
    }

    /** A user without a progress document has streak 0. */
    public Progress get(String uid) {
        try {
            DocumentSnapshot document = ref(uid).get().get();

            Progress progress = new Progress();
            progress.setStreak(readInt(document, "streak"));
            progress.setLongestStreak(readInt(document, "longestStreak"));
            return progress;

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving progress", e);
        }
    }

    /** Writes only the fields that were sent. A missing field keeps its stored value. */
    public void save(String uid, ProgressUpdate update) {
        Map<String, Object> fields = new HashMap<>();
        if (update.getStreak() != null) fields.put("streak", update.getStreak());
        if (update.getLongestStreak() != null) fields.put("longestStreak", update.getLongestStreak());

        if (fields.isEmpty()) {
            return;
        }

        try {
            ref(uid).set(fields, SetOptions.merge()).get();
        } catch (Exception e) {
            throw new RuntimeException("Error saving progress", e);
        }
    }

    private DocumentReference ref(String uid) {
        return firestore
                .collection("users")
                .document(uid)
                .collection("progress")
                .document("current");
    }

    private static int readInt(DocumentSnapshot document, String field) {
        Long value = document.getLong(field);
        return value != null ? value.intValue() : 0;
    }
}