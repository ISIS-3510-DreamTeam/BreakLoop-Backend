package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.FocusSession;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class FirebaseFocusSessionRepository implements FocusSessionRepository {

    private final Firestore firestore;

    public FirebaseFocusSessionRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public List<FocusSession> findAll(String uid) {
        try {
            var documents = firestore
                    .collection("users")
                    .document(uid)
                    .collection("focusSessions")
                    .get()
                    .get()
                    .getDocuments();

            List<FocusSession> result = new ArrayList<>();

            for (var document : documents) {
                FocusSession session = document.toObject(FocusSession.class);
                session.setId(document.getId());
                result.add(session);
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving focus sessions", e);
        }
    }

    @Override
    public Optional<FocusSession> findById(String uid, String id) {
        try {
            var document = firestore
                    .collection("users")
                    .document(uid)
                    .collection("focusSessions")
                    .document(id)
                    .get()
                    .get();

            if (!document.exists()) {
                return Optional.empty();
            }

            FocusSession session = document.toObject(FocusSession.class);
            session.setId(id);

            return Optional.of(session);

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving focus session", e);
        }
    }

    @Override
    public void save(String uid, FocusSession session) {
        try {
            firestore
                    .collection("users")
                    .document(uid)
                    .collection("focusSessions")
                    .document(session.getId())
                    .set(session)
                    .get();

        } catch (Exception e) {
            throw new RuntimeException("Error saving focus session", e);
        }
    }
}