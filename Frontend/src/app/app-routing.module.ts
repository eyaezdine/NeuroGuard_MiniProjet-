// angular import
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

// Project import
import { AdminLayout } from './theme/layouts/admin-layout/admin-layout.component';
import { GuestLayoutComponent } from './theme/layouts/guest-layout/guest-layout.component';

const routes: Routes = [
  {
    path: '',
    component: AdminLayout,
    children: [
      {
        path: '',
        redirectTo: '/dashboard/default',
        pathMatch: 'full'
      },
      {
        path: 'dashboard/default',
        loadComponent: () => import('./demo/dashboard/default/default.component').then((c) => c.DefaultComponent)
      },
      // ── Microservices ──────────────────────────────────────────────────
      {
        path: 'ms/users',
        loadComponent: () =>
          import('./demo/microservices/user-management/user-management.component').then((c) => c.UserManagementComponent),
        title: 'User Management'
      },
      {
        path: 'ms/medical-history',
        loadComponent: () =>
          import('./demo/microservices/medical-history/medical-history.component').then((c) => c.MedicalHistoryComponent),
        title: 'Medical History'
      },
      {
        path: 'ms/consultation',
        loadComponent: () =>
          import('./demo/microservices/consultation/consultation.component').then((c) => c.ConsultationComponent),
        title: 'Consultation'
      },
      {
        path: 'ms/forum',
        loadComponent: () =>
          import('./demo/microservices/forum/forum.component').then((c) => c.ForumComponent),
        title: 'Forum'
      },
      {
        path: 'ms/wellbeing',
        loadComponent: () =>
          import('./demo/microservices/wellbeing/wellbeing.component').then((c) => c.WellbeingComponent),
        title: 'Well-being'
      },
      {
        path: 'ms/delivery',
        loadComponent: () =>
          import('./demo/microservices/delivery/delivery.component').then((c) => c.DeliveryComponent),
        title: 'Delivery'
      }
    ]
  },
  {
    path: '',
    component: GuestLayoutComponent,
    children: [
      {
        path: 'login',
        loadComponent: () => import('./demo/pages/authentication/auth-login/auth-login.component').then((c) => c.AuthLoginComponent)
      },
      {
        path: 'register',
        loadComponent: () =>
          import('./demo/pages/authentication/auth-register/auth-register.component').then((c) => c.AuthRegisterComponent)
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
