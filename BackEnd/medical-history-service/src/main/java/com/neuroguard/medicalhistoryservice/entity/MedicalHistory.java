package com.neuroguard.medicalhistoryservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class MedicalHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String patientId;          // Long → String

    private String diagnosis;
    private LocalDate diagnosisDate;
    @Enumerated(EnumType.STRING)
    private ProgressionStage progressionStage;

    @Column(length = 1000)
    private String geneticRisk;
    @Column(length = 1000)
    private String familyHistory;
    @Column(length = 1000)
    private String environmentalFactors;
    @Column(length = 1000)
    private String comorbidities;
    @Column(length = 1000)
    private String medicationAllergies;
    @Column(length = 1000)
    private String environmentalAllergies;
    @Column(length = 1000)
    private String foodAllergies;

    @ElementCollection
    @CollectionTable(name = "medical_history_surgeries", joinColumns = @JoinColumn(name = "medical_history_id"))
    private List<Surgery> surgeries = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "medical_history_providers", joinColumns = @JoinColumn(name = "medical_history_id"))
    @Column(name = "provider_id")
    private List<String> providerIds = new ArrayList<>();   // List<Long> → List<String>

    @ElementCollection
    @CollectionTable(name = "medical_history_caregivers", joinColumns = @JoinColumn(name = "medical_history_id"))
    @Column(name = "caregiver_id")
    private List<String> caregiverIds = new ArrayList<>();  // List<Long> → List<String>

    @OneToMany(mappedBy = "medicalHistoryId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MedicalRecordFile> files = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}