package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.service.OfflineActivityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OfflineActivityController.class)
class OfflineActivityControllerNoAdminKeyTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OfflineActivityService service;

    @Test
    void emptyAdminKeyDisablesSeedEvenWithEmptyHeader() throws Exception {
        mvc.perform(post("/api/offline-activities/seed")
                        .header("X-Admin-Key", ""))
                .andExpect(status().isForbidden());

        verify(service, never()).seedCatalog();
    }
}
