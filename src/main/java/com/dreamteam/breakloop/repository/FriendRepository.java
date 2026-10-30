package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.FriendProfile;
import com.dreamteam.breakloop.model.FriendProgress;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface FriendRepository {

    /** The profile, only if {@code users/{uid}} already has a friendCode. */
    Optional<FriendProfile> findProfile(String uid);

    /**
     * Creates the profile with a code reserved in {@code friendCodes}. Each
     * attempt is one transaction: if the code is taken, the next one from
     * the supplier is tried. If the user got a code in the meantime, that
     * one is returned.
     *
     * @return the profile, or empty if every attempt hit a taken code
     */
    Optional<FriendProfile> createProfile(
            String uid, String email, String displayName,
            Supplier<String> codes, int maxAttempts);

    Optional<String> findUidByCode(String code);

    /** Writes both directions in one WriteBatch; overwriting is fine. */
    void addFriendship(String uid, String friendUid);

    List<String> findFriendUids(String uid);

    /** Empty if the user document or its displayName is missing. */
    CompletableFuture<Optional<String>> findDisplayName(String uid);

    CompletableFuture<FriendProgress> findProgress(String uid);
}
