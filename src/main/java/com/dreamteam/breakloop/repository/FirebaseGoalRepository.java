package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.Goal;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class FirebaseGoalRepository implements GoalRepository {

    private final Firestore firestore;

    public FirebaseGoalRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public List<Goal> findAll(String uid) {
        try {
            var documents = firestore
                    .collection("users")
                    .document(uid)
                    .collection("goals")
                    .get()
                    .get()
                    .getDocuments();

            List<Goal> result = new ArrayList<>();

            for (var document : documents) {
                Goal goal = document.toObject(Goal.class);
                goal.setId(document.getId());
                result.add(goal);
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving goals", e);
        }
    }

    @Override
    public Optional<Goal> findById(String uid, String id) {
        try {
            var document = firestore
                    .collection("users")
                    .document(uid)
                    .collection("goals")
                    .document(id)
                    .get()
                    .get();

            if (!document.exists()) {
                return Optional.empty();
            }

            Goal goal = document.toObject(Goal.class);
            goal.setId(id);

            return Optional.of(goal);

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving goal", e);
        }
    }

    @Override
    public void save(String uid, Goal goal) {
        try {
            firestore
                    .collection("users")
                    .document(uid)
                    .collection("goals")
                    .document(goal.getId())
                    .set(goal)
                    .get();

        } catch (Exception e) {
            throw new RuntimeException("Error saving goal", e);
        }
    }
}