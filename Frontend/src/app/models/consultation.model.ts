export interface Consultation {
  id: number;
  title: string;
  description?: string;
  startTime: string;
  endTime?: string;
  type: string;
  status: string;
  meetingLink?: string;
  providerId: number;
  providerName?: string;
  patientId: number;
  patientName?: string;
  caregiverId?: number;
  createdAt?: string;
}

export interface ConsultationRequest {
  title: string;
  description?: string;
  startTime: string;
  endTime?: string;
  type: 'PRESENTIAL' | 'ONLINE';
  patientId: number;
  caregiverId?: number;
  providerId?: number;
}
