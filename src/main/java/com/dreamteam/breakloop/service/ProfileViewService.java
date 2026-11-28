package com.dreamteam.breakloop.service;

import com.dreamteam.breakloop.repository.ProfileViewRepository;
import org.springframework.stereotype.Service;

@Service
public class ProfileViewService {

    private final ProfileViewRepository repository;

    public ProfileViewService(ProfileViewRepository repository) {
        this.repository = repository;
    }

    /** A missing or non-positive viewedAt is replaced by the server time. */
    public void record(String uid, Long viewedAt) {
        repository.add(uid, viewedAt != null && viewedAt > 0
                ? viewedAt
                : System.currentTimeMillis());
    }
}
