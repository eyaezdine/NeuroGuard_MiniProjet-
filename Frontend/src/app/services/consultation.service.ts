import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, map, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { Consultation, ConsultationRequest } from '../models/consultation.model';

@Injectable({ providedIn: 'root' })
export class ConsultationService {
  private readonly gatewayUrl = environment.gatewayUrl || 'http://localhost:8083';

  constructor(private readonly http: HttpClient) {}

  getConsultationsByRole(role: string): Observable<Consultation[]> {
    const safeRole = (role || '').toUpperCase();
    const endpoint =
      safeRole === 'PROVIDER'
        ? '/api/consultations/provider'
        : safeRole === 'CAREGIVER'
          ? '/api/consultations/caregiver'
          : safeRole === 'ADMIN'
            ? '/api/consultations/admin'
            : '/api/consultations/patient';

    return this.http.get<Consultation[]>(`${this.gatewayUrl}${endpoint}`, { headers: this.authHeaders() }).pipe(
      map((items) => items || []),
      catchError(() => of([]))
    );
  }

  createConsultation(payload: ConsultationRequest): Observable<Consultation | null> {
    return this.http
      .post<Consultation>(`${this.gatewayUrl}/api/consultations`, payload, { headers: this.authHeaders() })
      .pipe(map((item) => item || null));
  }

  updateConsultation(id: number, payload: ConsultationRequest): Observable<Consultation | null> {
    return this.http
      .put<Consultation>(`${this.gatewayUrl}/api/consultations/${id}`, payload, { headers: this.authHeaders() })
      .pipe(map((item) => item || null));
  }

  deleteConsultation(id: number): Observable<boolean> {
    return this.http.delete<void>(`${this.gatewayUrl}/api/consultations/${id}`, { headers: this.authHeaders() }).pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }

  private authHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    if (!token) {
      return new HttpHeaders();
    }
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }
}
