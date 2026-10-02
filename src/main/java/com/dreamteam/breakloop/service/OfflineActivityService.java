package com.dreamteam.breakloop.service;

import com.dreamteam.breakloop.model.OfflineActivity;
import com.dreamteam.breakloop.repository.OfflineActivityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OfflineActivityService {

    private final OfflineActivityRepository repository;

    public OfflineActivityService(
            OfflineActivityRepository repository
    ) {
        this.repository = repository;
    }

    public List<OfflineActivity> getActivities() {
        return repository.findAll();
    }

    public OfflineActivity getActivity(String id) {

        return repository
                .findById(id)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Offline activity not found"
                        )
                );
    }

    public String createActivity(
            OfflineActivity activity
    ) {

        validateActivity(activity);

        return repository.save(activity);
    }

    public void updateActivity(
            String id,
            OfflineActivity activity
    ) {

        validateActivity(activity);

        repository.update(id, activity);
    }

    public void deleteActivity(String id) {
        repository.delete(id);
    }

    private void validateActivity(
            OfflineActivity activity
    ) {

        if (activity.getTitle() == null ||
                activity.getTitle().isBlank()) {

            throw new IllegalArgumentException(
                    "Title is required"
            );
        }

        if (activity.getDescription() == null ||
                activity.getDescription().isBlank()) {

            throw new IllegalArgumentException(
                    "Description is required"
            );
        }

        if (activity.getDurationMinutes() <= 0) {

            throw new IllegalArgumentException(
                    "Duration must be greater than zero"
            );
        }
    }
}