package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(String uid);

    void save(User user);

    void update(String uid, User user);
}