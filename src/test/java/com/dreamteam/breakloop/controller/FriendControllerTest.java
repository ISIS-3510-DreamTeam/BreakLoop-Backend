package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.config.WebConfig;
import com.dreamteam.breakloop.model.FriendProfile;
import com.dreamteam.breakloop.model.FriendProgress;
import com.dreamteam.breakloop.repository.InMemoryFriendRepository;
import com.dreamteam.breakloop.service.FriendCodeGenerator;
import com.dreamteam.breakloop.service.FriendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FriendController.class)
@Import({InMemoryFriendRepository.class, FriendService.class,
        FriendCodeGenerator.class, NewApiExceptionHandler.class,
        WebConfig.class})
@TestPropertySource(properties = "breakloop.auth.enabled=false")
class FriendControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private InMemoryFriendRepository repository;

    @BeforeEach
    void clearStore() {
        repository.profiles.clear();
        repository.codeOwners.clear();
        repository.friends.clear();
        repository.progress.clear();
    }

    @Test
    void profileIsCreatedOnceAndReturnedAgain() throws Exception {
        String first = mvc.perform(post("/api/users/u1/friend-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@mail.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("u1"))
                .andExpect(jsonPath("$.email").value("ana@mail.com"))
                .andExpect(jsonPath("$.displayName").value("ana"))
                .andExpect(jsonPath("$.friendCode").isString())
                .andReturn().getResponse().getContentAsString();

        String code = first.replaceAll(".*\"friendCode\":\"(\\w+)\".*", "$1");

        mvc.perform(post("/api/users/u1/friend-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@mail.com\"}"))
                .andExpect(jsonPath("$.friendCode").value(code));
    }

    @Test
    void profileWithoutBodyIsAccepted() throws Exception {
        mvc.perform(post("/api/users/u1/friend-profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(""));
    }

    @Test
    void addFriendReportsEachResult() throws Exception {
        repository.codeOwners.put("AB3K", "u2");
        repository.codeOwners.put("MINE", "u1");

        add("ab3k").andExpect(jsonPath("$.result").value("ADDED"));
        add("NOPE").andExpect(jsonPath("$.result").value("NOT_FOUND"));
        add("MINE").andExpect(jsonPath("$.result").value("OWN_CODE"));
    }

    @Test
    void emptyCodeIs400() throws Exception {
        add("  ").andExpect(status().isBadRequest());

        mvc.perform(post("/api/users/u1/friends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listReturnsFriendsWithProgress() throws Exception {
        repository.profiles.put("u2", new FriendProfile("u2", "", "zoe", "C"));
        repository.profiles.put("u3", new FriendProfile("u3", "", "ana", "D"));
        repository.friends.put("u1", List.of("u2", "u3"));
        repository.progress.put("u2", new FriendProgress(4, 2));

        mvc.perform(get("/api/users/u1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].uid").value("u3"))
                .andExpect(jsonPath("$[0].displayName").value("ana"))
                .andExpect(jsonPath("$[0].streak").value(0))
                .andExpect(jsonPath("$[0].petLevel").value(1))
                .andExpect(jsonPath("$[1].streak").value(4))
                .andExpect(jsonPath("$[1].petLevel").value(2));
    }

    private org.springframework.test.web.servlet.ResultActions add(String code)
            throws Exception {
        return mvc.perform(post("/api/users/u1/friends")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + code + "\"}"));
    }
}
