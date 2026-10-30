package com.dreamteam.breakloop.model;

/** Raw {@code progress/current} values; null when the field is missing. */
public record FriendProgress(Integer streak, Integer petLevel) {

    public static final FriendProgress EMPTY = new FriendProgress(null, null);
}
