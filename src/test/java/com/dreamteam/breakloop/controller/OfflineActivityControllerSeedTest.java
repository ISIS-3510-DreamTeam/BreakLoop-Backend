package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.service.OfflineActivityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OfflineActivityController.class)
@TestPropertySource(properties = "breakloop.admin.key=secret")
class OfflineActivityControllerSeedTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OfflineActivityService service;

    @Test
    void seedWithoutKeyIs403() throws Exception {
        mvc.perform(post("/api/offline-activities/seed"))
                .andExpect(status().isForbidden());
    }

    @Test
    void seedWithWrongKeyIs403() throws Exception {
        mvc.perform(post("/api/offline-activities/seed")
                        .header("X-Admin-Key", "nope"))
                .andExpect(status().isForbidden());
    }

    @Test
    void seedWithRightKeyLoadsCatalog() throws Exception {
        when(service.seedCatalog()).thenReturn(18);

        mvc.perform(post("/api/offline-activities/seed")
                        .header("X-Admin-Key", "secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seeded").value(18));
        verify(service).seedCatalog();
    }
}
