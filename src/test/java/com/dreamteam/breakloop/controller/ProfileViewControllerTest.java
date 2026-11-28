package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.config.WebConfig;
import com.dreamteam.breakloop.repository.ProfileViewRepository;
import com.dreamteam.breakloop.service.ProfileViewService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileViewController.class)
@Import({ProfileViewService.class, NewApiExceptionHandler.class,
        WebConfig.class})
class ProfileViewControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProfileViewRepository repository;

    @MockitoBean
    private FirebaseAuth firebaseAuth;

    @BeforeEach
    void setUp() throws Exception {
        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn("u1");
        when(firebaseAuth.verifyIdToken("t")).thenReturn(token);
    }

    @Test
    void storesTheSentTimeForTheTokenUser() throws Exception {
        // An extra field such as the visited uid is ignored
        send("{\"viewedAt\":1760000000000,\"visitedUid\":\"u2\"}")
                .andExpect(status().isNoContent());

        verify(repository).add("u1", 1760000000000L);
    }

    @Test
    void missingOrNonPositiveTimeUsesServerTime() throws Exception {
        long before = System.currentTimeMillis();

        for (String body : new String[] {
                "{}", "{\"viewedAt\":0}", "{\"viewedAt\":-5}"}) {
            send(body).andExpect(status().isNoContent());
        }

        long after = System.currentTimeMillis();
        ArgumentCaptor<Long> stored = ArgumentCaptor.forClass(Long.class);
        verify(repository, times(3)).add(eq("u1"), stored.capture());

        for (long viewedAt : stored.getAllValues()) {
            assertTrue(viewedAt >= before && viewedAt <= after);
        }
    }

    @Test
    void withoutTokenIs401AndNothingIsStored() throws Exception {
        mvc.perform(post("/api/users/me/profile-views")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"viewedAt\":1760000000000}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(repository);
    }

    private ResultActions send(String body) throws Exception {
        return mvc.perform(post("/api/users/me/profile-views")
                .header("Authorization", "Bearer t")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
