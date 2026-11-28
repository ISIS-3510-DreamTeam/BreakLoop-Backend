package com.dreamteam.breakloop.repository;

import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class FirebaseProfileViewRepository implements ProfileViewRepository {

    private final Firestore firestore;

    public FirebaseProfileViewRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public void add(String uid, long viewedAt) {
        try {
            firestore.collection("users").document(uid)
                    .collection("profileViews")
                    .add(Map.of("viewedAt", viewedAt))
                    .get();

        } catch (Exception e) {
            throw new RuntimeException("Error saving profile view", e);
        }
    }
}
