package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.UserProfile;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class FirebaseUserProfileRepository implements UserProfileRepository {

    private final Firestore firestore;

    public FirebaseUserProfileRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public Optional<UserProfile> find(String uid) {
        try {
            DocumentSnapshot document = firestore
                    .collection("users").document(uid).get().get();

            if (!document.exists()) {
                return Optional.empty();
            }

            return Optional.of(new UserProfile(
                    document.getString("displayName"),
                    document.getString("photoUrl")));

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving user profile", e);
        }
    }
}
