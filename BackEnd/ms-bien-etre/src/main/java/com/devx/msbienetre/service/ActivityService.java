package com.devx.msbienetre.service;

import com.devx.msbienetre.client.UserClient;
import com.devx.msbienetre.dto.UserApiResponse;
import com.devx.msbienetre.entity.Activity;
import com.devx.msbienetre.repository.ActivityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository repository;
    private final UserClient userClient;

    public ActivityService(ActivityRepository repository, UserClient userClient) {
        this.repository = repository;
        this.userClient = userClient;
    }

    public Activity createActivity(Activity activity) {
        // Validate patient existence using Feign Client
        try {
            UserApiResponse resp = userClient.getUserById(activity.getUserId());
            if (resp == null || !resp.isSuccess() || resp.getData() == null) {
                throw new RuntimeException("Patient not found in User Service");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error communicating with User Service: " + e.getMessage());
        }

        return repository.save(activity);
    }

    public List<Activity> getAll() {
        return repository.findAll();
    }

    public Activity getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
    }

    public List<Activity> getByUser(String userId) {
        return repository.findByUserId(userId);
    }

    public Activity updateActivity(Long id, Activity updated) {
        Activity existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        existing.setActivityType(updated.getActivityType());
        existing.setDurationMinutes(updated.getDurationMinutes());
        existing.setIntensity(updated.getIntensity());
        existing.setAssistedBy(updated.getAssistedBy());
        existing.setNotes(updated.getNotes());
        existing.setDate(updated.getDate());

        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
