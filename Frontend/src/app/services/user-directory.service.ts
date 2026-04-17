import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, map, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { AppUser } from '../models/app-user.model';

@Injectable({ providedIn: 'root' })
export class UserDirectoryService {
  private readonly gatewayUrl = environment.gatewayUrl || 'http://localhost:8083';

  constructor(private readonly http: HttpClient) {}

  getUsersByRole(role: 'PATIENT' | 'PROVIDER' | 'CAREGIVER' | 'ADMIN'): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(`${this.gatewayUrl}/api/users/role/${role}`, { headers: this.authHeaders() }).pipe(
      map((users) => users || []),
      catchError(() => of([]))
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
