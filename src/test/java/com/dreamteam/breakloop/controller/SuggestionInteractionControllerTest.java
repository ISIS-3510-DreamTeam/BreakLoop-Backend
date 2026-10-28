package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.config.WebConfig;
import com.dreamteam.breakloop.model.SuggestionInteraction;
import com.dreamteam.breakloop.repository.SuggestionInteractionRepository;
import com.dreamteam.breakloop.service.RequestValidator;
import com.dreamteam.breakloop.service.SuggestionInteractionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SuggestionInteractionController.class)
@Import({SuggestionInteractionService.class, RequestValidator.class,
        NewApiExceptionHandler.class, WebConfig.class})
@TestPropertySource(properties = "breakloop.auth.enabled=false")
class SuggestionInteractionControllerTest {

    private static final String PENDING = """
            {"openedAt": 1000, "timeOfDay": "MORNING", "isWeekend": true,
             "latitude": 4.71234, "longitude": -74.06789,
             "shownSuggestions": [{"activityId": "a1", "category": "MOVE",
               "availableMin": 15, "score": 0.9,
               "reasons": ["GOOD_WEATHER"], "shownAt": 1000}],
             "outcome": "PENDING"}""";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SuggestionInteractionRepository repository;

    @Test
    void putStoresRoundedDocumentWithRouteId() throws Exception {
        mvc.perform(put("/api/users/u1/suggestion-interactions/i1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PENDING))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("i1"))
                .andExpect(jsonPath("$.latitude").value(4.71))
                .andExpect(jsonPath("$.longitude").value(-74.07))
                .andExpect(jsonPath("$.isWeekend").value(true))
                .andExpect(jsonPath("$.weekend").doesNotExist());

        verify(repository).save(eq("u1"), any(SuggestionInteraction.class));
    }

    @Test
    void acceptedWithoutActivityIs400() throws Exception {
        mvc.perform(put("/api/users/u1/suggestion-interactions/i1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PENDING.replace("PENDING", "ACCEPTED")
                                .replace("\"outcome\"", "\"closedAt\": 2000, \"outcome\"")))
                .andExpect(status().isBadRequest());

        verify(repository, never()).save(any(), any());
    }

    @Test
    void unknownEnumValueIs400() throws Exception {
        mvc.perform(put("/api/users/u1/suggestion-interactions/i1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PENDING.replace("MORNING", "BREAKFAST")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reopeningClosedInteractionIs409() throws Exception {
        String closed = PENDING.replace("\"PENDING\"", "\"NO_ACTIVITY\"")
                .replace("\"outcome\"", "\"closedAt\": 2000, \"outcome\"");
        mvc.perform(put("/api/users/u1/suggestion-interactions/i1")
                .contentType(MediaType.APPLICATION_JSON).content(closed));

        // Second request sees the closed document stored by the first
        when(repository.findById("u1", "i1")).thenReturn(Optional.of(
                new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(closed.replace("\"openedAt\"", "\"id\": \"i1\", \"openedAt\""),
                                SuggestionInteraction.class)));

        mvc.perform(put("/api/users/u1/suggestion-interactions/i1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PENDING))
                .andExpect(status().isConflict());
    }

    @Test
    void getUnknownIs404() throws Exception {
        when(repository.findById("u1", "nope")).thenReturn(Optional.empty());

        mvc.perform(get("/api/users/u1/suggestion-interactions/nope"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listReturnsInteractions() throws Exception {
        when(repository.findAll("u1", null, null)).thenReturn(List.of());

        mvc.perform(get("/api/users/u1/suggestion-interactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void batchReportsWrittenCount() throws Exception {
        when(repository.findByIds(eq("u1"), anyCollection()))
                .thenReturn(Map.of());

        mvc.perform(post("/api/users/u1/suggestion-interactions/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[" + PENDING.replace("{\"openedAt\"",
                                "{\"id\": \"i1\", \"openedAt\"") + "]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.written").value(1))
                .andExpect(jsonPath("$.skipped").isEmpty());
    }

    @Test
    void batchItemWithoutIdIs400() throws Exception {
        mvc.perform(post("/api/users/u1/suggestion-interactions/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[" + PENDING + "]"))
                .andExpect(status().isBadRequest());
    }
}
