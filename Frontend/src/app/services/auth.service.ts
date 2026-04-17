import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly gatewayUrl = 'http://localhost:8083';

  constructor(private http: HttpClient, private router: Router) {}

  login(credentials: { email: string; password: string }): Observable<any> {
    return this.http.post<any>(`${this.gatewayUrl}/api/auth/login`, credentials).pipe(
      tap(response => {
        if (response.success && response.data.user) {
          localStorage.setItem('user', JSON.stringify(response.data.user));
        }
      })
    );
  }

  register(userData: any): Observable<any> {
    return this.http.post<any>(`${this.gatewayUrl}/api/auth/register`, userData).pipe(
      tap(response => {
        if (response.success && response.data.user) {
          localStorage.setItem('user', JSON.stringify(response.data.user));
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem('user');
    this.router.navigate(['/login']);
  }

  getUser(): any {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }

  isAuthenticated(): boolean {
    return !!this.getUser();
  }
}
