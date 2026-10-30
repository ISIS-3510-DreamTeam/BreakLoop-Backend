package com.dreamteam.breakloop.service;

import com.dreamteam.breakloop.exception.RequestValidationException;
import com.dreamteam.breakloop.model.AddFriendResult;
import com.dreamteam.breakloop.model.FriendProfile;
import com.dreamteam.breakloop.model.FriendProgress;
import com.dreamteam.breakloop.model.FriendSummary;
import com.dreamteam.breakloop.repository.FriendRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@Service
public class FriendService {

    static final int MAX_CODE_ATTEMPTS = 10;

    private final FriendRepository repository;
    private final FriendCodeGenerator codes;

    public FriendService(FriendRepository repository, FriendCodeGenerator codes) {
        this.repository = repository;
        this.codes = codes;
    }

    /**
     * Existing profile (one that has a friendCode) or a new one.
     *
     * @param bodyEmail  email sent by the client, may be null or blank
     * @param tokenEmail email in the Firebase token, may be null
     */
    public FriendProfile getOrCreateProfile(
            String uid, String bodyEmail, String tokenEmail) {

        return repository.findProfile(uid).orElseGet(() -> {
            String email = firstNonBlank(bodyEmail, tokenEmail);
            String displayName = email.contains("@")
                    ? email.substring(0, email.indexOf('@'))
                    : email;

            return repository
                    .createProfile(uid, email, displayName,
                            codes::generate, MAX_CODE_ATTEMPTS)
                    .orElseThrow(() -> new IllegalStateException(
                            "Could not reserve a friend code"));
        });
    }

    public AddFriendResult addFriend(String uid, String rawCode) {
        String code = rawCode == null ? "" : rawCode.trim().toUpperCase(Locale.ROOT);

        if (code.isEmpty()) {
            throw new RequestValidationException("code must not be empty");
        }

        String friendUid = repository.findUidByCode(code).orElse(null);

        if (friendUid == null) {
            return AddFriendResult.NOT_FOUND;
        }

        if (friendUid.equals(uid)) {
            return AddFriendResult.OWN_CODE;
        }

        repository.addFriendship(uid, friendUid);

        return AddFriendResult.ADDED;
    }

    public List<FriendSummary> getFriends(String uid) {
        // Start every read first, then wait: nothing is fetched one by one
        List<CompletableFuture<FriendSummary>> pending =
                repository.findFriendUids(uid).stream()
                        .map(this::summarize)
                        .toList();

        return pending.stream()
                .map(CompletableFuture::join)
                .sorted(Comparator.comparing(FriendSummary::displayName,
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private CompletableFuture<FriendSummary> summarize(String friendUid) {
        CompletableFuture<java.util.Optional<String>> name =
                repository.findDisplayName(friendUid);
        CompletableFuture<FriendProgress> progress =
                repository.findProgress(friendUid);

        return name.thenCombine(progress, (displayName, p) -> new FriendSummary(
                friendUid,
                displayName.orElse(""),
                p.streak() != null ? p.streak() : 0,
                p.petLevel() != null ? p.petLevel() : 1));
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }

        return second != null ? second.trim() : "";
    }
}
