package com.dreamteam.breakloop.repository;

public interface ProfileViewRepository {

    /** Adds {@code users/{uid}/profileViews/{autoId}} with only viewedAt. */
    void add(String uid, long viewedAt);
}
