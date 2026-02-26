package com.neuroguard.medicalhistoryservice.dto;

import com.neuroguard.medicalhistoryservice.entity.ProgressionStage;
import com.neuroguard.medicalhistoryservice.entity.Surgery;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MedicalHistoryRequest {
    private String patientId;          // required, now String
    private String diagnosis;
    private LocalDate diagnosisDate;
    private ProgressionStage progressionStage;
    private String geneticRisk;
    private String familyHistory;
    private String environmentalFactors;
    private String comorbidities;
    private String medicationAllergies;
    private String environmentalAllergies;
    private String foodAllergies;
    private List<Surgery> surgeries;
    private List<String> providerIds;          // List<Long> → List<String>
    private List<String> caregiverNames;       // caregivers by username (email)
    private List<String> caregiverIds;         // caregivers by ID
}