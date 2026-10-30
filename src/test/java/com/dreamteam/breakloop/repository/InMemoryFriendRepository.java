package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.FriendProfile;
import com.dreamteam.breakloop.model.FriendProgress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/** Test double with the same rules as the Firestore one. */
public class InMemoryFriendRepository implements FriendRepository {

    public final Map<String, FriendProfile> profiles = new HashMap<>();
    public final Map<String, String> codeOwners = new HashMap<>();
    public final Map<String, List<String>> friends = new HashMap<>();
    public final Map<String, FriendProgress> progress = new HashMap<>();
    public int profileWrites;

    @Override
    public Optional<FriendProfile> findProfile(String uid) {
        return Optional.ofNullable(profiles.get(uid));
    }

    @Override
    public Optional<FriendProfile> createProfile(
            String uid, String email, String displayName,
            Supplier<String> codes, int maxAttempts) {

        for (int i = 0; i < maxAttempts; i++) {
            String code = codes.get();

            if (!codeOwners.containsKey(code)) {
                FriendProfile profile =
                        new FriendProfile(uid, email, displayName, code);
                profiles.put(uid, profile);
                codeOwners.put(code, uid);
                profileWrites++;
                return Optional.of(profile);
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<String> findUidByCode(String code) {
        return Optional.ofNullable(codeOwners.get(code));
    }

    @Override
    public void addFriendship(String uid, String friendUid) {
        friends.computeIfAbsent(uid, k -> new ArrayList<>()).add(friendUid);
        friends.computeIfAbsent(friendUid, k -> new ArrayList<>()).add(uid);
    }

    @Override
    public List<String> findFriendUids(String uid) {
        return friends.getOrDefault(uid, List.of());
    }

    @Override
    public CompletableFuture<Optional<String>> findDisplayName(String uid) {
        FriendProfile profile = profiles.get(uid);
        return CompletableFuture.completedFuture(
                Optional.ofNullable(profile).map(FriendProfile::displayName));
    }

    @Override
    public CompletableFuture<FriendProgress> findProgress(String uid) {
        return CompletableFuture.completedFuture(
                progress.getOrDefault(uid, FriendProgress.EMPTY));
    }
}
