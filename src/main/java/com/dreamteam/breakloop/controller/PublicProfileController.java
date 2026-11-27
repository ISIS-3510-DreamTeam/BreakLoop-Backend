package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.auth.FirebaseAuthInterceptor;
import com.dreamteam.breakloop.model.PublicProfile;
import com.dreamteam.breakloop.service.PublicProfileService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class PublicProfileController {

    private final PublicProfileService service;

    public PublicProfileController(PublicProfileService service) {
        this.service = service;
    }

    @GetMapping("/{uid}/public-profile")
    public PublicProfile publicProfile(
            @PathVariable String uid,
            @RequestAttribute(FirebaseAuthInterceptor.TOKEN_UID_ATTRIBUTE)
            String requesterUid
    ) {
        return service.getPublicProfile(requesterUid, uid);
    }
}
