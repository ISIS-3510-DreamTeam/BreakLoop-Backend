package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.config.WebConfig;
import com.dreamteam.breakloop.model.FriendProgress;
import com.dreamteam.breakloop.model.UserAchievement;
import com.dreamteam.breakloop.model.UserProfile;
import com.dreamteam.breakloop.repository.InMemoryFriendRepository;
import com.dreamteam.breakloop.repository.UserProfileRepository;
import com.dreamteam.breakloop.service.PublicProfileService;
import com.dreamteam.breakloop.service.UserAchievementService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** u1 is the profile owner, u2 a friend, u3 a stranger. */
@WebMvcTest(PublicProfileController.class)
@Import({InMemoryFriendRepository.class, PublicProfileService.class,
        NewApiExceptionHandler.class, WebConfig.class})
class PublicProfileControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private InMemoryFriendRepository friends;

    @MockitoBean
    private UserProfileRepository profiles;

    @MockitoBean
    private UserAchievementService achievements;

    @MockitoBean
    private FirebaseAuth firebaseAuth;

    @BeforeEach
    void setUp() throws Exception {
        friends.friends.clear();
        friends.progress.clear();
        friends.addFriendship("u1", "u2");

        when(profiles.find("u1"))
                .thenReturn(Optional.of(new UserProfile("ana", null)));

        for (String uid : new String[] {"u1", "u2", "u3"}) {
            FirebaseToken token = mock(FirebaseToken.class);
            when(token.getUid()).thenReturn(uid);
            when(firebaseAuth.verifyIdToken(uid)).thenReturn(token);
        }
    }

    @Test
    void publicProfileIsVisibleToOwnerAndFriendsOnly() throws Exception {
        friends.progress.put("u1", new FriendProgress(4, 2));

        for (String caller : new String[] {"u1", "u2"}) {
            getAs(caller, "/api/users/u1/public-profile")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.uid").value("u1"))
                    .andExpect(jsonPath("$.displayName").value("ana"))
                    .andExpect(jsonPath("$.photoUrl").isEmpty())
                    .andExpect(jsonPath("$.streak").value(4))
                    .andExpect(jsonPath("$.petLevel").value(2));
        }

        getAs("u3", "/api/users/u1/public-profile")
                .andExpect(status().isForbidden());
        getAs("u1", "/api/users/ghost/public-profile")
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/users/u1/public-profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void badgesAreTheUnlockedAchievementIds() throws Exception {
        when(achievements.getUnlocked("u1"))
                .thenReturn(List.of(new UserAchievement("streak_7", 1L)));

        getAs("u2", "/api/users/u1/badges")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0]").value("streak_7"));
        getAs("u3", "/api/users/u1/badges")
                .andExpect(status().isForbidden());
        getAs("u1", "/api/users/ghost/badges")
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/users/u1/badges"))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions getAs(String caller, String url) throws Exception {
        return mvc.perform(get(url).header("Authorization", "Bearer " + caller));
    }
}
