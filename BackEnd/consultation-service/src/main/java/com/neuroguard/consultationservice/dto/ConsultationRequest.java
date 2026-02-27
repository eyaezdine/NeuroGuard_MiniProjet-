package com.neuroguard.consultationservice.dto;

import com.neuroguard.consultationservice.entity.ConsultationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ConsultationRequest {
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @NotNull
    private ConsultationType type;
    @NotNull
    private String patientId;          // ← anciennement Long
    private String caregiverId;        // ← anciennement Long

    // Getters et setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public ConsultationType getType() { return type; }
    public void setType(ConsultationType type) { this.type = type; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getCaregiverId() { return caregiverId; }
    public void setCaregiverId(String caregiverId) { this.caregiverId = caregiverId; }
}