import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, HttpClientModule],
  templateUrl: './user-management.component.html'
})
export class UserManagementComponent implements OnInit {
  readonly gatewayUrl = 'http://localhost:8083';
  users: any[] = [];
  loading = false;
  error = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loading = true;
    this.http.get<any[]>(`${this.gatewayUrl}/api/users`).subscribe({
      next: (data) => { this.users = data; this.loading = false; },
      error: (err) => { this.error = 'Could not connect to User Service.'; this.loading = false; }
    });
  }
}
