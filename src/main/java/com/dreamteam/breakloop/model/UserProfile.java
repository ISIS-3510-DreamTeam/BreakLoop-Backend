package com.dreamteam.breakloop.model;

/** Profile fields of {@code users/{uid}}; either one may be null. */
public record UserProfile(String displayName, String photoUrl) {
}
