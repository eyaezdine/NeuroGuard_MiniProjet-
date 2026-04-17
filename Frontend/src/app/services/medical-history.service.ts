import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, forkJoin, map, of, switchMap } from 'rxjs';
import { environment } from '../../environments/environment';
import { PageResponse } from '../models/api-response.model';
import { MedicalHistory, MedicalHistoryRequest } from '../models/medical-history.model';

@Injectable({ providedIn: 'root' })
export class MedicalHistoryService {
  private readonly gatewayUrl = environment.gatewayUrl || 'http://localhost:8083';

  constructor(private readonly http: HttpClient) {}

  getHistoryListByRole(role: string): Observable<MedicalHistory[]> {
    const safeRole = (role || '').toUpperCase();

    if (safeRole === 'PROVIDER' || safeRole === 'ADMIN') {
      return this.http
        .get<PageResponse<MedicalHistory>>(`${this.gatewayUrl}/api/provider/medical-history`, {
          headers: this.authHeaders()
        })
        .pipe(
          map((page) => page.content || []),
          catchError(() => of([]))
        );
    }

    if (safeRole === 'CAREGIVER') {
      return this.http
        .get<Array<{ id: number }>>(`${this.gatewayUrl}/api/caregiver/medical-history/patients`, {
          headers: this.authHeaders()
        })
        .pipe(
          switchMap((resp) => {
            const patients = resp || [];
            if (!patients.length) {
              return of([]);
            }
            return forkJoin(
              patients.map((p) =>
                this.http
                  .get<MedicalHistory>(`${this.gatewayUrl}/api/caregiver/medical-history/${p.id}`, {
                    headers: this.authHeaders()
                  })
                  .pipe(catchError(() => of(null)))
              )
            ).pipe(map((records) => records.filter((r): r is MedicalHistory => r !== null)));
          }),
          catchError(() => of([]))
        );
    }

    return this.http.get<MedicalHistory>(`${this.gatewayUrl}/api/patient/medical-history/me`, { headers: this.authHeaders() }).pipe(
      map((item) => (item ? [item] : [])),
      catchError(() => of([]))
    );
  }

  getMyHistory(): Observable<MedicalHistory | null> {
    return this.http.get<MedicalHistory>(`${this.gatewayUrl}/api/patient/medical-history/me`, { headers: this.authHeaders() }).pipe(
      map((item) => item || null),
      catchError(() => of(null))
    );
  }

  createHistory(payload: MedicalHistoryRequest): Observable<MedicalHistory | null> {
    return this.http
      .post<MedicalHistory>(`${this.gatewayUrl}/api/provider/medical-history`, payload, { headers: this.authHeaders() })
      .pipe(
        map((item) => item || null),
        catchError(() => of(null))
      );
  }

  updateHistory(patientId: number, payload: MedicalHistoryRequest): Observable<MedicalHistory | null> {
    return this.http
      .put<MedicalHistory>(`${this.gatewayUrl}/api/provider/medical-history/${patientId}`, payload, { headers: this.authHeaders() })
      .pipe(
        map((item) => item || null),
        catchError(() => of(null))
      );
  }

  deleteHistory(patientId: number): Observable<boolean> {
    return this.http
      .delete<void>(`${this.gatewayUrl}/api/provider/medical-history/${patientId}`, { headers: this.authHeaders() })
      .pipe(
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
