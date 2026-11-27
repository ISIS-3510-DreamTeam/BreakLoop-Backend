package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.UserProfile;

import java.util.Optional;

public interface UserProfileRepository {

    /** Empty if {@code users/{uid}} does not exist. */
    Optional<UserProfile> find(String uid);
}
