package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.UserProfile;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
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

    @Override
    public void update(String uid, String displayName, String photoUrl) {
        Map<String, Object> fields = new HashMap<>();
        if (displayName != null) fields.put("displayName", displayName);
        if (photoUrl != null) fields.put("photoUrl", photoUrl);

        try {
            // merge() keeps friendCode, email, pet state and the other fields
            firestore.collection("users").document(uid)
                    .set(fields, SetOptions.merge())
                    .get();

        } catch (Exception e) {
            throw new RuntimeException("Error saving user profile", e);
        }
    }
}
