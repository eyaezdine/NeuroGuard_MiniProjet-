import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, map, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { CarePlan, CarePlanMessage, CarePlanMessageRequest, CarePlanRequest } from '../models/careplan.model';

@Injectable({ providedIn: 'root' })
export class CarePlanService {
  private readonly gatewayUrl = environment.gatewayUrl || 'http://localhost:8083';

  constructor(private readonly http: HttpClient) {}

  getCarePlans(): Observable<CarePlan[]> {
    return this.http.get<CarePlan[]>(`${this.gatewayUrl}/api/care-plans/list`, { headers: this.authHeaders() }).pipe(
      map((items) => items || []),
      catchError(() => of([]))
    );
  }

  getCarePlanById(id: number): Observable<CarePlan | null> {
    return this.http.get<CarePlan>(`${this.gatewayUrl}/api/care-plans/${id}`, { headers: this.authHeaders() }).pipe(
      map((item) => item || null),
      catchError(() => of(null))
    );
  }

  createCarePlan(payload: CarePlanRequest): Observable<CarePlan | null> {
    return this.http
      .post<CarePlan>(`${this.gatewayUrl}/api/care-plans`, payload, { headers: this.authHeaders() })
      .pipe(map((item) => item || null));
  }

  updateCarePlan(id: number, payload: CarePlanRequest): Observable<CarePlan | null> {
    return this.http
      .put<CarePlan>(`${this.gatewayUrl}/api/care-plans/${id}`, payload, { headers: this.authHeaders() })
      .pipe(map((item) => item || null));
  }

  deleteCarePlan(id: number): Observable<boolean> {
    return this.http.delete<void>(`${this.gatewayUrl}/api/care-plans/${id}`, { headers: this.authHeaders() }).pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }

  getMessages(carePlanId: number): Observable<CarePlanMessage[]> {
    return this.http
      .get<CarePlanMessage[]>(`${this.gatewayUrl}/api/care-plans/${carePlanId}/messages`, { headers: this.authHeaders() })
      .pipe(
        map((items) => items || []),
        catchError(() => of([]))
      );
  }

  sendMessage(carePlanId: number, payload: CarePlanMessageRequest): Observable<CarePlanMessage | null> {
    return this.http
      .post<CarePlanMessage>(`${this.gatewayUrl}/api/care-plans/${carePlanId}/messages`, payload, { headers: this.authHeaders() })
      .pipe(
        map((item) => item || null),
        catchError(() => of(null))
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
