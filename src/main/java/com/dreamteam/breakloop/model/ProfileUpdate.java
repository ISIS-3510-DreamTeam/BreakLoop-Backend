package com.dreamteam.breakloop.model;

/**
 * Body of PUT /api/users/me/profile. Both fields are optional:
 * only the ones that arrive are written.
 */
public record ProfileUpdate(String displayName, String photoUrl) {
}
