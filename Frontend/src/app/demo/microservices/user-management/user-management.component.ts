import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-management.component.html'
})
export class UserManagementComponent implements OnInit {
  readonly gatewayUrl = 'http://localhost:8083';
  users: any[] = [];
  loading = false;
  error = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.fetchUsers();
  }

  fetchUsers(): void {
    this.loading = true;
    this.error = '';
    
    // The backend returns { success: true, data: [users...] }
    this.http.get<any>(`${this.gatewayUrl}/api/users`).subscribe({
      next: (response) => {
        if (response.success) {
          this.users = response.data;
        } else {
          this.error = 'Failed to fetch users.';
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Could not connect to User Service via Gateway.';
        this.loading = false;
        console.error('Fetch error:', err);
      }
    });
  }
}
