package com.esprit.microservice.careplanservice.dto;

import lombok.Data;

@Data
public class ClinicalSummaryDto {
    private Long patientId;
    private boolean historyFound;
    private String progressionStage;
    private String comorbidities;
    private String medicationAllergies;
    private String environmentalAllergies;
    private String foodAllergies;
}
