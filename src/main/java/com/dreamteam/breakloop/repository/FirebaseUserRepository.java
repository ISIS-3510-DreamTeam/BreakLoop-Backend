package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.User;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class FirebaseUserRepository implements UserRepository {

    private final Firestore firestore;

    public FirebaseUserRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public Optional<User> findById(String uid) {
        try {
            DocumentSnapshot document = firestore
                    .collection("users")
                    .document(uid)
                    .get()
                    .get();

            if (!document.exists()) {
                return Optional.empty();
            }

            User user = document.toObject(User.class);
            user.setUid(uid);

            return Optional.of(user);

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error retrieving user", e);
        }
    }

    @Override
    public void save(User user) {
        try {
            firestore
                    .collection("users")
                    .document(user.getUid())
                    .set(user)
                    .get();

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error saving user", e);
        }
    }

    @Override
    public void update(String uid, User user) {
        try {
            firestore
                    .collection("users")
                    .document(uid)
                    .set(user)
                    .get();

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error updating user", e);
        }
    }
}