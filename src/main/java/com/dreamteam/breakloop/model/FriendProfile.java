package com.dreamteam.breakloop.model;

/** Public identity used to add friends: {@code users/{uid}} fields. */
public record FriendProfile(
        String uid, String email, String displayName, String friendCode) {
}
