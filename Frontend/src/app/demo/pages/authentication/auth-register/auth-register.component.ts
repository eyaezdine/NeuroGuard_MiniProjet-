// Angular import
import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

// Project import
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-auth-register',
  standalone: true,
  imports: [RouterModule, FormsModule, CommonModule],
  templateUrl: './auth-register.component.html',
  styleUrl: './auth-register.component.scss'
})
export class AuthRegisterComponent {
  firstName = '';
  lastName = '';
  email = '';
  password = '';
  role = 'PATIENT'; // Default role
  loading = false;
  error = '';
  validationErrors: { field: string; message: string }[] = [];

  roles = ['PATIENT', 'CAREGIVER', 'PROVIDER'];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit() {
    this.loading = true;
    this.error = '';
    this.validationErrors = [];

    const userData = {
      firstName: this.firstName,
      lastName: this.lastName,
      email: this.email,
      password: this.password,
      role: this.role
    };

    this.authService.register(userData).subscribe({
      next: () => {
        this.router.navigate(['/dashboard/default']);
      },
      error: (err) => {
        console.error('Registration failed:', err);
        this.error = err.error?.message || 'Registration failed. Please try again.';
        
        // Extract validation errors if present
        if (err.error?.errors && Array.isArray(err.error.errors)) {
          this.validationErrors = err.error.errors;
        }
        
        this.loading = false;
      }
    });
  }

  // public method
  SignUpOptions = [
    {
      image: 'assets/images/authentication/google.svg',
      name: 'Google'
    },
    {
      image: 'assets/images/authentication/twitter.svg',
      name: 'Twitter'
    },
    {
      image: 'assets/images/authentication/facebook.svg',
      name: 'Facebook'
    }
  ];
}
