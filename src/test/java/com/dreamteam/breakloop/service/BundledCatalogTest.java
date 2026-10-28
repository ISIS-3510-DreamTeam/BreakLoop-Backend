package com.dreamteam.breakloop.service;

import com.dreamteam.breakloop.repository.OfflineActivityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class BundledCatalogTest {

    @Test
    void bundledCatalogHas18ValidActivities() {
        OfflineActivityService service = new OfflineActivityService(
                mock(OfflineActivityRepository.class),
                new ObjectMapper(),
                new ClassPathResource("catalog/activities.json"));

        assertEquals(18, service.seedCatalog());
    }
}
