package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.config.WebConfig;
import com.dreamteam.breakloop.repository.InMemoryActivityLogRepository;
import com.dreamteam.breakloop.service.ActivityLogService;
import com.dreamteam.breakloop.service.RequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActivityLogController.class)
@Import({InMemoryActivityLogRepository.class, ActivityLogService.class, RequestValidator.class,
        NewApiExceptionHandler.class, WebConfig.class})
@TestPropertySource(properties = "breakloop.auth.enabled=false")
class ActivityLogControllerTest {

    private static final String LOG = """
            {"id": "ignored", "activityId": "a1", "category": "MOVE",
             "source": "BROWSED", "startedAt": 1000, "xp": 20,
             "timeOfDay": "EVENING", "availableMin": 15,
             "latitude": 4.71234, "longitude": -74.06789}""";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private InMemoryActivityLogRepository repository;

    @BeforeEach
    void clearStore() {
        repository.stored.clear();
    }

    @Test
    void putUsesRouteIdAndRoundsCoordinates() throws Exception {
        mvc.perform(put("/api/users/u1/activity-logs/l1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOG))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("l1"))
                .andExpect(jsonPath("$.latitude").value(4.71))
                .andExpect(jsonPath("$.completedAt").isEmpty());

        assertEquals("l1", repository.stored.get("l1").id());
    }

    @Test
    void missingRequiredFieldIs400() throws Exception {
        mvc.perform(put("/api/users/u1/activity-logs/l1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOG.replace("\"activityId\": \"a1\",", "")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUnknownIs404() throws Exception {
        mvc.perform(get("/api/users/u1/activity-logs/nope"))
                .andExpect(status().isNotFound());
    }

    @Test
    void batchWritesAll() throws Exception {
        mvc.perform(post("/api/users/u1/activity-logs/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[" + LOG.replace("ignored", "l1") + "]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.written").value(1));
        assertEquals(1, repository.stored.size());
    }
}
