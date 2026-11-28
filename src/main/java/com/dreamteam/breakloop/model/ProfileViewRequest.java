package com.dreamteam.breakloop.model;

/**
 * Body of POST /api/users/me/profile-views. It deliberately has no field for
 * the visited user.
 */
public record ProfileViewRequest(Long viewedAt) {
}
