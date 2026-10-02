package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.Usage;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class FirebaseUsageRepository implements UsageRepository {

    private final Firestore firestore;

    public FirebaseUsageRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public List<Usage> findAll(String uid) {
        try {
            var documents = firestore
                    .collection("users")
                    .document(uid)
                    .collection("usage")
                    .get()
                    .get()
                    .getDocuments();

            List<Usage> result = new ArrayList<>();

            for (var document : documents) {
                Usage usage = document.toObject(Usage.class);
                usage.setDate(document.getId());
                result.add(usage);
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving usage", e);
        }
    }

    @Override
    public Optional<Usage> findByDate(String uid, String date) {
        try {
            var document = firestore
                    .collection("users")
                    .document(uid)
                    .collection("usage")
                    .document(date)
                    .get()
                    .get();

            if (!document.exists()) {
                return Optional.empty();
            }

            Usage usage = document.toObject(Usage.class);
            usage.setDate(date);

            return Optional.of(usage);

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving usage", e);
        }
    }

    @Override
    public void save(String uid, Usage usage) {
        try {
            firestore
                    .collection("users")
                    .document(uid)
                    .collection("usage")
                    .document(usage.getDate())
                    .set(usage)
                    .get();

        } catch (Exception e) {
            throw new RuntimeException("Error saving usage", e);
        }
    }
}