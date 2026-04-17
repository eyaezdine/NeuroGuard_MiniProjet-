package com.neuroguard.medicalhistoryservice.dto;

import com.neuroguard.medicalhistoryservice.entity.ProgressionStage;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClinicalSummaryResponse {
    private Long patientId;
    private boolean historyFound;
    private ProgressionStage progressionStage;
    private String comorbidities;
    private String medicationAllergies;
    private String environmentalAllergies;
    private String foodAllergies;

    public boolean hasClinicalRiskContext() {
        return notBlank(comorbidities)
                || notBlank(medicationAllergies)
                || notBlank(environmentalAllergies)
                || notBlank(foodAllergies)
                || progressionStage == ProgressionStage.SEVERE;
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
