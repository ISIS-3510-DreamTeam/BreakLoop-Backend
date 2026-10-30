package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.config.WebConfig;
import com.dreamteam.breakloop.model.FriendProfile;
import com.dreamteam.breakloop.repository.FriendRepository;
import com.dreamteam.breakloop.service.FriendCodeGenerator;
import com.dreamteam.breakloop.service.FriendService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Auth enabled (the default): the interceptor is wired on the friends routes. */
@WebMvcTest(FriendController.class)
@Import({FriendService.class, FriendCodeGenerator.class,
        NewApiExceptionHandler.class, WebConfig.class})
class FriendsAuthWiringTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private FriendRepository repository;

    @MockitoBean
    private FirebaseAuth firebaseAuth;

    @Test
    void withoutTokenIs401OnAllThreeRoutes() throws Exception {
        mvc.perform(post("/api/users/u1/friend-profile"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/users/u1/friends"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/users/u1/friends"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenOfAnotherUserIs403() throws Exception {
        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn("someone-else");
        when(firebaseAuth.verifyIdToken("t")).thenReturn(token);

        mvc.perform(get("/api/users/u1/friends")
                        .header("Authorization", "Bearer t"))
                .andExpect(status().isForbidden());
    }

    @Test
    void tokenEmailIsUsedWhenBodyHasNone() throws Exception {
        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn("u1");
        when(token.getEmail()).thenReturn("tok@mail.com");
        when(firebaseAuth.verifyIdToken("t")).thenReturn(token);
        when(repository.findProfile("u1")).thenReturn(Optional.empty());
        when(repository.createProfile(
                org.mockito.ArgumentMatchers.eq("u1"),
                org.mockito.ArgumentMatchers.eq("tok@mail.com"),
                org.mockito.ArgumentMatchers.eq("tok"),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(Optional.of(
                        new FriendProfile("u1", "tok@mail.com", "tok", "AB3K")));

        mvc.perform(post("/api/users/u1/friend-profile")
                        .header("Authorization", "Bearer t"))
                .andExpect(status().isOk());
    }
}
