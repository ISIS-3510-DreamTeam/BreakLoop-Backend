package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.auth.FirebaseAuthInterceptor;
import com.dreamteam.breakloop.model.ProfileViewRequest;
import com.dreamteam.breakloop.service.ProfileViewService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Counts visits to friends' profiles (BQ19). */
@RestController
@RequestMapping("/api/users/me/profile-views")
public class ProfileViewController {

    private final ProfileViewService service;

    public ProfileViewController(ProfileViewService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void record(
            @RequestBody(required = false) ProfileViewRequest body,
            @RequestAttribute(FirebaseAuthInterceptor.TOKEN_UID_ATTRIBUTE)
            String requesterUid
    ) {
        service.record(requesterUid, body == null ? null : body.viewedAt());
    }
}
