package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.FriendProfile;
import com.dreamteam.breakloop.model.FriendProgress;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteBatch;
import com.google.common.util.concurrent.MoreExecutors;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Same paths and fields the Flutter app writes straight to Firestore:
 * {@code users/{uid}}, {@code friendCodes/{CODE}},
 * {@code users/{uid}/friends/{friendUid}} and
 * {@code users/{uid}/progress/current}.
 */
@Repository
public class FirebaseFriendRepository implements FriendRepository {

    private final Firestore firestore;

    public FirebaseFriendRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public Optional<FriendProfile> findProfile(String uid) {
        try {
            return toProfile(uid, user(uid).get().get());

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving friend profile", e);
        }
    }

    @Override
    public Optional<FriendProfile> createProfile(
            String uid, String email, String displayName,
            Supplier<String> codes, int maxAttempts) {
        try {
            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                String code = codes.get();

                Optional<FriendProfile> result = firestore.runTransaction(tx -> {
                    DocumentReference userRef = user(uid);
                    DocumentReference codeRef =
                            firestore.collection("friendCodes").document(code);

                    DocumentSnapshot userDoc = tx.get(userRef).get();
                    Optional<FriendProfile> existing = toProfile(uid, userDoc);

                    if (existing.isPresent()) {
                        return existing;
                    }

                    if (tx.get(codeRef).get().exists()) {
                        return Optional.<FriendProfile>empty();
                    }

                    // The document may hold other fields; keep what is there
                    String storedEmail = userDoc.getString("email");
                    String storedName = userDoc.getString("displayName");
                    String finalEmail = email.isEmpty() && storedEmail != null
                            ? storedEmail : email;
                    String finalName = storedName != null
                            ? storedName : displayName;

                    Map<String, Object> fields = new HashMap<>();
                    fields.put("email", finalEmail);
                    fields.put("displayName", finalName);
                    fields.put("friendCode", code);

                    if (userDoc.get("createdAt") == null) {
                        fields.put("createdAt", FieldValue.serverTimestamp());
                    }

                    tx.set(userRef, fields, SetOptions.merge());
                    tx.set(codeRef, Map.of("uid", uid));

                    return Optional.of(new FriendProfile(
                            uid, finalEmail, finalName, code));
                }).get();

                if (result.isPresent()) {
                    return result;
                }
            }

            return Optional.empty();

        } catch (Exception e) {
            throw new RuntimeException("Error creating friend profile", e);
        }
    }

    @Override
    public Optional<String> findUidByCode(String code) {
        try {
            DocumentSnapshot document = firestore
                    .collection("friendCodes").document(code).get().get();

            return Optional.ofNullable(
                    document.exists() ? document.getString("uid") : null);

        } catch (Exception e) {
            throw new RuntimeException("Error looking up friend code", e);
        }
    }

    @Override
    public void addFriendship(String uid, String friendUid) {
        try {
            WriteBatch batch = firestore.batch();

            batch.set(friend(uid, friendUid),
                    Map.of("since", FieldValue.serverTimestamp()));
            batch.set(friend(friendUid, uid),
                    Map.of("since", FieldValue.serverTimestamp()));

            batch.commit().get();

        } catch (Exception e) {
            throw new RuntimeException("Error adding friend", e);
        }
    }

    @Override
    public List<String> findFriendUids(String uid) {
        try {
            return user(uid).collection("friends").get().get()
                    .getDocuments().stream()
                    .map(DocumentSnapshot::getId)
                    .toList();

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving friends", e);
        }
    }

    @Override
    public CompletableFuture<Optional<String>> findDisplayName(String uid) {
        return toFuture(user(uid).get(), document -> Optional.ofNullable(
                document.exists() ? document.getString("displayName") : null));
    }

    @Override
    public CompletableFuture<FriendProgress> findProgress(String uid) {
        return toFuture(
                user(uid).collection("progress").document("current").get(),
                document -> document.exists()
                        ? new FriendProgress(
                                toInt(document.get("streak")),
                                toInt(document.get("petLevel")))
                        : FriendProgress.EMPTY);
    }

    private static Optional<FriendProfile> toProfile(
            String uid, DocumentSnapshot document) {

        String code = document.exists() ? document.getString("friendCode") : null;

        if (code == null) {
            return Optional.empty();
        }

        return Optional.of(new FriendProfile(
                uid,
                orEmpty(document.getString("email")),
                orEmpty(document.getString("displayName")),
                code));
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private static Integer toInt(Object value) {
        return value instanceof Number n ? n.intValue() : null;
    }

    private static <T> CompletableFuture<T> toFuture(
            ApiFuture<DocumentSnapshot> source,
            Function<DocumentSnapshot, T> mapper) {

        CompletableFuture<T> future = new CompletableFuture<>();

        ApiFutures.addCallback(source, new ApiFutureCallback<>() {
            @Override
            public void onSuccess(DocumentSnapshot result) {
                try {
                    future.complete(mapper.apply(result));
                } catch (RuntimeException e) {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                future.completeExceptionally(t);
            }
        }, MoreExecutors.directExecutor());

        return future;
    }

    private DocumentReference user(String uid) {
        return firestore.collection("users").document(uid);
    }

    private DocumentReference friend(String uid, String friendUid) {
        return user(uid).collection("friends").document(friendUid);
    }
}
