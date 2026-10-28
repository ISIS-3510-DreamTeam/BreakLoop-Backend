package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.config.WebConfig;
import com.dreamteam.breakloop.repository.ActivityLogRepository;
import com.dreamteam.breakloop.service.ActivityLogService;
import com.dreamteam.breakloop.service.RequestValidator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Auth enabled (the default): the interceptor is wired on the new routes. */
@WebMvcTest(ActivityLogController.class)
@Import({ActivityLogService.class, RequestValidator.class,
        NewApiExceptionHandler.class, WebConfig.class})
class NewEndpointsAuthWiringTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ActivityLogRepository repository;

    @MockitoBean
    private FirebaseAuth firebaseAuth;

    @Test
    void withoutTokenIs401() throws Exception {
        mvc.perform(get("/api/users/u1/activity-logs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenOfAnotherUserIs403() throws Exception {
        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn("someone-else");
        when(firebaseAuth.verifyIdToken("t")).thenReturn(token);

        mvc.perform(get("/api/users/u1/activity-logs")
                        .header("Authorization", "Bearer t"))
                .andExpect(status().isForbidden());
    }

    @Test
    void tokenOfRouteUserIsAllowed() throws Exception {
        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn("u1");
        when(firebaseAuth.verifyIdToken("t")).thenReturn(token);
        when(repository.findAll("u1", null, null)).thenReturn(List.of());

        mvc.perform(get("/api/users/u1/activity-logs")
                        .header("Authorization", "Bearer t"))
                .andExpect(status().isOk());
    }
}
