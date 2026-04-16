package com.devx.msbienetre.service;

import com.devx.msbienetre.client.UserClient;
import com.devx.msbienetre.dto.UserDTO;
import com.devx.msbienetre.entity.Wellbeing;
import com.devx.msbienetre.repository.WellbeingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WellbeingService {

    private final WellbeingRepository repository;
    private final UserClient userClient;

    public WellbeingService(WellbeingRepository repository, UserClient userClient) {
        this.repository = repository;
        this.userClient = userClient;
    }

    public Wellbeing createWellbeing(Wellbeing wellbeing) {

        // Validate user existence using Feign Client
        try {
            UserDTO user = userClient.getUserById(wellbeing.getUserId());
            if (user == null) {
                throw new RuntimeException("User not found in User Service");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error communicating with User Service: " + e.getMessage());
        }

        if (repository.existsByUserIdAndDate(
                wellbeing.getUserId(),
                wellbeing.getDate())) {
            throw new RuntimeException("Wellbeing record already exists for this patient today.");
        }

        boolean risk = false;

        if (wellbeing.getSleepHours() != null && wellbeing.getSleepHours() < 4)
            risk = true;

        if (wellbeing.getStressLevel() != null && wellbeing.getStressLevel() >= 4)
            risk = true;

        if (wellbeing.getMemoryDifficulty() != null && wellbeing.getMemoryDifficulty() >= 4)
            risk = true;

        wellbeing.setRiskFlag(risk);

        return repository.save(wellbeing);
    }

    public List<Wellbeing> getAll() {
        return repository.findAll();
    }

    public Wellbeing getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wellbeing not found"));
    }

    public List<Wellbeing> getByUser(String userId) {
        return repository.findByUserId(userId);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Wellbeing updateWellbeing(Long id, Wellbeing updated) {
        Wellbeing existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wellbeing not found"));

        existing.setMood(updated.getMood());
        existing.setSleepHours(updated.getSleepHours());
        existing.setStressLevel(updated.getStressLevel());
        existing.setMemoryDifficulty(updated.getMemoryDifficulty());
        existing.setAppetite(updated.getAppetite());
        existing.setNotes(updated.getNotes());

        boolean risk = false;
        if (existing.getSleepHours() != null && existing.getSleepHours() < 4)
            risk = true;
        if (existing.getStressLevel() != null && existing.getStressLevel() >= 4)
            risk = true;
        if (existing.getMemoryDifficulty() != null && existing.getMemoryDifficulty() >= 4)
            risk = true;
        existing.setRiskFlag(risk);

        return repository.save(existing);
    }

}