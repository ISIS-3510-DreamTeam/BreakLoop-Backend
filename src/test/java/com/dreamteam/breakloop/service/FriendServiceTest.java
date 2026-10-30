package com.dreamteam.breakloop.service;

import com.dreamteam.breakloop.exception.RequestValidationException;
import com.dreamteam.breakloop.model.AddFriendResult;
import com.dreamteam.breakloop.model.FriendProfile;
import com.dreamteam.breakloop.model.FriendProgress;
import com.dreamteam.breakloop.model.FriendSummary;
import com.dreamteam.breakloop.repository.InMemoryFriendRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FriendServiceTest {

    private final InMemoryFriendRepository repository =
            new InMemoryFriendRepository();
    private final Deque<String> nextCodes = new ArrayDeque<>();

    private final FriendService service = new FriendService(
            repository,
            new FriendCodeGenerator() {
                @Override
                public String generate() {
                    return nextCodes.isEmpty() ? "ZZZZ" : nextCodes.poll();
                }
            });

    @Test
    void createsProfileWithDisplayNameFromEmail() {
        nextCodes.add("AB3K");

        FriendProfile profile =
                service.getOrCreateProfile("u1", "ana@mail.com", null);

        assertEquals(new FriendProfile("u1", "ana@mail.com", "ana", "AB3K"),
                profile);
        assertEquals("u1", repository.codeOwners.get("AB3K"));
    }

    @Test
    void usesTokenEmailWhenBodyEmailIsBlank() {
        nextCodes.add("AB3K");

        FriendProfile profile =
                service.getOrCreateProfile("u1", "  ", "tok@mail.com");

        assertEquals("tok@mail.com", profile.email());
        assertEquals("tok", profile.displayName());
    }

    @Test
    void existingProfileIsNotRecreated() {
        nextCodes.add("AB3K");
        nextCodes.add("CD4E");

        FriendProfile first = service.getOrCreateProfile("u1", "a@b.c", null);
        FriendProfile second = service.getOrCreateProfile("u1", "a@b.c", null);

        assertEquals(first, second);
        assertEquals(1, repository.profileWrites);
    }

    @Test
    void retriesWhenCodeIsTaken() {
        repository.codeOwners.put("AAAA", "other");
        nextCodes.add("AAAA");
        nextCodes.add("BBBB");

        assertEquals("BBBB",
                service.getOrCreateProfile("u1", "a@b.c", null).friendCode());
    }

    @Test
    void failsAfterTenTakenCodes() {
        repository.codeOwners.put("ZZZZ", "other");

        assertThrows(IllegalStateException.class,
                () -> service.getOrCreateProfile("u1", "a@b.c", null));
    }

    @Test
    void addsFriendInBothDirectionsNormalizingCode() {
        repository.codeOwners.put("AB3K", "u2");

        assertEquals(AddFriendResult.ADDED, service.addFriend("u1", " ab3k "));
        assertEquals(List.of("u2"), repository.friends.get("u1"));
        assertEquals(List.of("u1"), repository.friends.get("u2"));
    }

    @Test
    void unknownCodeIsNotFound() {
        assertEquals(AddFriendResult.NOT_FOUND, service.addFriend("u1", "NOPE"));
        assertEquals(0, repository.friends.size());
    }

    @Test
    void ownCodeIsRejected() {
        repository.codeOwners.put("AB3K", "u1");

        assertEquals(AddFriendResult.OWN_CODE, service.addFriend("u1", "AB3K"));
        assertEquals(0, repository.friends.size());
    }

    @Test
    void emptyCodeIsValidationError() {
        assertThrows(RequestValidationException.class,
                () -> service.addFriend("u1", "   "));
        assertThrows(RequestValidationException.class,
                () -> service.addFriend("u1", null));
    }

    @Test
    void friendWithoutProgressOrProfileGetsDefaults() {
        repository.friends.put("u1", List.of("ghost"));

        assertEquals(List.of(new FriendSummary("ghost", "", 0, 1)),
                service.getFriends("u1"));
    }

    @Test
    void partialProgressFillsOnlyMissingFields() {
        repository.friends.put("u1", List.of("u2"));
        repository.progress.put("u2", new FriendProgress(5, null));

        assertEquals(List.of(new FriendSummary("u2", "", 5, 1)),
                service.getFriends("u1"));
    }

    @Test
    void friendsAreSortedByDisplayName() {
        repository.profiles.put("u2", new FriendProfile("u2", "", "zoe", "C"));
        repository.profiles.put("u3", new FriendProfile("u3", "", "Ana", "D"));
        repository.profiles.put("u4", new FriendProfile("u4", "", "bob", "E"));
        repository.friends.put("u1", List.of("u2", "u3", "u4"));
        repository.progress.put("u3", new FriendProgress(7, 3));

        List<FriendSummary> result = service.getFriends("u1");

        assertEquals(List.of("Ana", "bob", "zoe"),
                result.stream().map(FriendSummary::displayName).toList());
        assertEquals(7, result.get(0).streak());
        assertEquals(3, result.get(0).petLevel());
    }
}
