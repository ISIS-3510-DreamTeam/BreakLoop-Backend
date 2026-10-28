package com.dreamteam.breakloop.service;

import com.dreamteam.breakloop.exception.RequestValidationException;
import com.dreamteam.breakloop.model.ActivityCategory;
import com.dreamteam.breakloop.model.ActivityLog;
import com.dreamteam.breakloop.model.LogSource;
import com.dreamteam.breakloop.model.TimeOfDay;
import com.dreamteam.breakloop.repository.InMemoryActivityLogRepository;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActivityLogServiceTest {

    private final InMemoryActivityLogRepository repository =
            new InMemoryActivityLogRepository();

    private final ActivityLogService service = new ActivityLogService(
            repository,
            new RequestValidator(Validation
                    .buildDefaultValidatorFactory().getValidator()));

    private static ActivityLog log(String id, Long completedAt) {
        return new ActivityLog(
                id, "a1", ActivityCategory.RELAX, LogSource.RECOMMENDED, null,
                1_000L, completedAt, 20, TimeOfDay.EVENING, null, 15, null,
                4.71234, -74.06789);
    }

    @Test
    void routeIdWinsAndCoordinatesAreRounded() {
        ActivityLog result = service.normalize("route", log("body", null));

        assertEquals("route", result.id());
        assertEquals(4.71, result.latitude());
        assertEquals(-74.07, result.longitude());
    }

    @Test
    void completedBeforeStartedIsRejected() {
        assertThrows(RequestValidationException.class,
                () -> service.normalize("x", log("x", 500L)));
    }

    @Test
    void missingRequiredFieldsAreRejected() {
        ActivityLog empty = new ActivityLog(null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);

        assertThrows(RequestValidationException.class,
                () -> service.normalize("x", empty));
    }

    @Test
    void lateStartedWriteDoesNotEraseCompletion() {
        service.upsert("u", "x", log("x", 5_000L));

        ActivityLog result = service.upsert("u", "x", log("x", null));

        assertEquals(5_000L, result.completedAt());
        assertEquals(5_000L, repository.stored.get("x").completedAt());
    }

    @Test
    void sameRequestTwiceLeavesOneDocument() {
        service.upsert("u", "x", log("x", null));
        service.upsert("u", "x", log("x", null));

        assertEquals(1, repository.stored.size());
    }
}
