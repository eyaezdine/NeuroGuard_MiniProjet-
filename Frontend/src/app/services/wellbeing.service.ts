import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/** Aligné sur `com.devx.msbienetre.entity.Wellbeing` (ms-bien-etre). */
export interface WellbeingRecord {
  id?: number;
  userId: string;
  date?: string;
  mood?: string;
  sleepHours?: number | null;
  stressLevel?: number | null;
  memoryDifficulty?: number | null;
  appetite?: string;
  notes?: string;
  riskFlag?: boolean;
  createdAt?: string;
}

/** Aligné sur `com.devx.msbienetre.entity.Activity`. */
export interface ActivityRecord {
  id?: number;
  userId: string;
  date?: string;
  activityType?: string;
  durationMinutes?: number | null;
  intensity?: string;
  assistedBy?: string;
  notes?: string;
  createdAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class WellbeingService {
  private readonly gatewayBase = 'http://localhost:8083';
  private readonly wellbeingUrl = `${this.gatewayBase}/api/wellbeing`;
  private readonly activitiesUrl = `${this.gatewayBase}/api/activities`;

  constructor(private http: HttpClient) {}

  // --- Wellbeing (CRUD + par utilisateur) ---

  listWellbeingByUser(userId: string): Observable<WellbeingRecord[]> {
    return this.http.get<WellbeingRecord[]>(`${this.wellbeingUrl}/user/${encodeURIComponent(userId)}`);
  }

  createWellbeing(body: WellbeingRecord): Observable<WellbeingRecord> {
    return this.http.post<WellbeingRecord>(this.wellbeingUrl, body);
  }

  updateWellbeing(id: number, body: WellbeingRecord): Observable<WellbeingRecord> {
    return this.http.put<WellbeingRecord>(`${this.wellbeingUrl}/${id}`, body);
  }

  deleteWellbeing(id: number): Observable<void> {
    return this.http.delete<void>(`${this.wellbeingUrl}/${id}`);
  }

  // --- Activities ---

  listActivitiesByUser(userId: string): Observable<ActivityRecord[]> {
    return this.http.get<ActivityRecord[]>(`${this.activitiesUrl}/user/${encodeURIComponent(userId)}`);
  }

  createActivity(body: ActivityRecord): Observable<ActivityRecord> {
    return this.http.post<ActivityRecord>(this.activitiesUrl, body);
  }

  updateActivity(id: number, body: ActivityRecord): Observable<ActivityRecord> {
    return this.http.put<ActivityRecord>(`${this.activitiesUrl}/${id}`, body);
  }

  deleteActivity(id: number): Observable<void> {
    return this.http.delete<void>(`${this.activitiesUrl}/${id}`);
  }
}
