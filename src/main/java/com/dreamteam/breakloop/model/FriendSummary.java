package com.dreamteam.breakloop.model;

/** A friend with the streak and pet level shown in the friends list. */
public record FriendSummary(
        String uid, String displayName, int streak, int petLevel) {
}
