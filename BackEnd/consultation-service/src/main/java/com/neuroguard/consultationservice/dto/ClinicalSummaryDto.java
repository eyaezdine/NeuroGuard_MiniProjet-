package com.neuroguard.consultationservice.dto;

public class ClinicalSummaryDto {
    private Long patientId;
    private boolean historyFound;
    private String progressionStage;
    private String comorbidities;
    private String medicationAllergies;
    private String environmentalAllergies;
    private String foodAllergies;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public boolean isHistoryFound() {
        return historyFound;
    }

    public void setHistoryFound(boolean historyFound) {
        this.historyFound = historyFound;
    }

    public String getProgressionStage() {
        return progressionStage;
    }

    public void setProgressionStage(String progressionStage) {
        this.progressionStage = progressionStage;
    }

    public String getComorbidities() {
        return comorbidities;
    }

    public void setComorbidities(String comorbidities) {
        this.comorbidities = comorbidities;
    }

    public String getMedicationAllergies() {
        return medicationAllergies;
    }

    public void setMedicationAllergies(String medicationAllergies) {
        this.medicationAllergies = medicationAllergies;
    }

    public String getEnvironmentalAllergies() {
        return environmentalAllergies;
    }

    public void setEnvironmentalAllergies(String environmentalAllergies) {
        this.environmentalAllergies = environmentalAllergies;
    }

    public String getFoodAllergies() {
        return foodAllergies;
    }

    public void setFoodAllergies(String foodAllergies) {
        this.foodAllergies = foodAllergies;
    }
}
