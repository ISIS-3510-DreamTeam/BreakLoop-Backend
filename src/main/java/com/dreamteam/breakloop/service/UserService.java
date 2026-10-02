package com.dreamteam.breakloop.service;

import com.dreamteam.breakloop.model.Progress;
import com.dreamteam.breakloop.model.User;
import com.dreamteam.breakloop.repository.UserRepository;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final Firestore firestore;

    public UserService(UserRepository userRepository, Firestore firestore) {
        this.userRepository = userRepository;
        this.firestore = firestore;
    }

    public User getUser(String uid) {
        return userRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public Progress getProgress(String uid) {
        try {
            var document = firestore
                    .collection("users")
                    .document(uid)
                    .collection("progress")
                    .document("current")
                    .get()
                    .get();

            if (!document.exists()) {
                throw new RuntimeException("Progress not found");
            }

            return document.toObject(Progress.class);

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving progress", e);
        }
    }

    public void saveProgress(String uid, Progress progress) {
        try {
            firestore
                    .collection("users")
                    .document(uid)
                    .collection("progress")
                    .document("current")
                    .set(progress)
                    .get();

        } catch (Exception e) {
            throw new RuntimeException("Error saving progress", e);
        }
    }
}