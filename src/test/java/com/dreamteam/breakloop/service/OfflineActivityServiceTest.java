package com.dreamteam.breakloop.service;

import com.dreamteam.breakloop.model.OfflineActivity;
import com.dreamteam.breakloop.repository.OfflineActivityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OfflineActivityServiceTest {

    private final OfflineActivityRepository repository =
            mock(OfflineActivityRepository.class);

    private final OfflineActivityService service = new OfflineActivityService(
            repository,
            new ObjectMapper(),
            new ClassPathResource("catalog/test-activities.json"));

    @Test
    void seedMarksEverythingActiveAndUpsertsById() {
        assertEquals(2, service.seedCatalog());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OfflineActivity>> saved =
                ArgumentCaptor.forClass(List.class);
        verify(repository).upsertAll(saved.capture());

        assertEquals(List.of("walk-park", "doodle"),
                saved.getValue().stream().map(OfflineActivity::getId).toList());
        assertTrue(saved.getValue().stream().allMatch(OfflineActivity::isActive));
        assertEquals("Walk in the park", saved.getValue().get(0).getTitle());
        assertEquals(15, saved.getValue().get(0).getDurationMin());
    }

    @Test
    void categoryFilterIsCaseInsensitive() {
        service.getActivities("move");

        verify(repository).findByCategory("MOVE");
    }

    @Test
    void versionIsStableForSameCatalogAndChangesWithIt() {
        OfflineActivity a = activity("a", 10L);
        when(repository.findAll()).thenReturn(List.of(a));
        Map<String, Object> first = service.getCatalogVersion();
        Map<String, Object> again = service.getCatalogVersion();

        assertEquals(first, again);
        assertEquals(10L, first.get("updatedAt"));

        OfflineActivity edited = activity("a", 20L);
        edited.setXp(99);
        when(repository.findAll()).thenReturn(List.of(edited));

        assertNotEquals(first.get("version"),
                service.getCatalogVersion().get("version"));
    }

    private static OfflineActivity activity(String id, long updatedAt) {
        OfflineActivity a = new OfflineActivity();
        a.setId(id);
        a.setUpdatedAt(updatedAt);
        return a;
    }
}
