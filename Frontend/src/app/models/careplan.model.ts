export interface CarePlan {
  id: number;
  patientId: number;
  patientName?: string;
  providerId: number;
  providerName?: string;
  priority?: string;
  nutritionStatus?: string;
  sleepStatus?: string;
  activityStatus?: string;
  medicationStatus?: string;
  nutritionPlan?: string;
  sleepPlan?: string;
  activityPlan?: string;
  medicationPlan?: string;
  updatedAt?: string;
  createdAt?: string;
}

export interface CarePlanRequest {
  patientId: number;
  providerId?: number;
  priority?: 'LOW' | 'MEDIUM' | 'HIGH';
  nutritionPlan?: string;
  sleepPlan?: string;
  activityPlan?: string;
  medicationPlan?: string;
  nutritionDeadline?: string;
  sleepDeadline?: string;
  activityDeadline?: string;
  medicationDeadline?: string;
}

export interface CarePlanMessage {
  id: number;
  senderId: number;
  senderName?: string;
  content: string;
  createdAt: string;
}

export interface CarePlanMessageRequest {
  content: string;
}
