package com.dreamteam.breakloop.model;

/** What a user and their friends may see. Never add usage or flagged-app data. */
public record PublicProfile(
        String uid, String displayName, String photoUrl,
        int streak, int petLevel) {
}
