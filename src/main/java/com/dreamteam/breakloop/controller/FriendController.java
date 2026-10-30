package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.auth.FirebaseAuthInterceptor;
import com.dreamteam.breakloop.model.AddFriendRequest;
import com.dreamteam.breakloop.model.AddFriendResponse;
import com.dreamteam.breakloop.model.FriendProfile;
import com.dreamteam.breakloop.model.FriendProfileRequest;
import com.dreamteam.breakloop.model.FriendSummary;
import com.dreamteam.breakloop.service.FriendService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{uid}")
public class FriendController {

    private final FriendService service;

    public FriendController(FriendService service) {
        this.service = service;
    }

    @PostMapping("/friend-profile")
    public ResponseEntity<FriendProfile> friendProfile(
            @PathVariable String uid,
            @RequestBody(required = false) FriendProfileRequest body,
            HttpServletRequest request
    ) {
        String tokenEmail = (String) request.getAttribute(
                FirebaseAuthInterceptor.TOKEN_EMAIL_ATTRIBUTE);

        return ResponseEntity.ok(service.getOrCreateProfile(
                uid, body == null ? null : body.email(), tokenEmail));
    }

    @PostMapping("/friends")
    public ResponseEntity<AddFriendResponse> addFriend(
            @PathVariable String uid,
            @RequestBody(required = false) AddFriendRequest body
    ) {
        return ResponseEntity.ok(new AddFriendResponse(
                service.addFriend(uid, body == null ? null : body.code())));
    }

    @GetMapping("/friends")
    public ResponseEntity<List<FriendSummary>> friends(
            @PathVariable String uid
    ) {
        return ResponseEntity.ok(service.getFriends(uid));
    }
}
