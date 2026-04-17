export interface MedicalHistory {
  id: number;
  patientId: number;
  patientName: string;
  diagnosis: string;
  diagnosisDate?: string;
  progressionStage?: string;
  geneticRisk?: string;
  familyHistory?: string;
  comorbidities?: string;
  medicationAllergies?: string;
  providerNames?: string[];
  caregiverNames?: string[];
  updatedAt?: string;
  createdAt?: string;
}

export interface MedicalHistoryRequest {
  patientId: number;
  diagnosis?: string;
  diagnosisDate?: string;
  progressionStage?: 'MILD' | 'MODERATE' | 'SEVERE';
  geneticRisk?: string;
  familyHistory?: string;
  environmentalFactors?: string;
  comorbidities?: string;
  medicationAllergies?: string;
  environmentalAllergies?: string;
  foodAllergies?: string;
  caregiverIds?: number[];
  providerIds?: number[];
}
