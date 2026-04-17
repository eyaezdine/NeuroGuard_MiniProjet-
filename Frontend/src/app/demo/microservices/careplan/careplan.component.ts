import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CarePlanService } from '../../../services/careplan.service';
import { ListingItem } from '../../../models/listing.model';
import { AppUser } from '../../../models/app-user.model';
import { CarePlan, CarePlanRequest } from '../../../models/careplan.model';
import { UserDirectoryService } from '../../../services/user-directory.service';
import { ResourceListComponent } from '../components/resource-list/resource-list.component';
import { ResourceViewComponent } from '../components/resource-view/resource-view.component';

@Component({
  selector: 'app-careplan',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ResourceListComponent, ResourceViewComponent],
  templateUrl: './careplan.component.html'
})
export class CareplanComponent implements OnInit {
  loading = false;
  error = '';
  formError = '';
  formSuccess = '';
  editingId: number | null = null;

  records: ListingItem[] = [];
  selected: CarePlan | null = null;
  patients: AppUser[] = [];
  providers: AppUser[] = [];

  readonly carePlanForm = this.fb.group({
    patientId: ['', Validators.required],
    providerId: [''],
    priority: ['MEDIUM'],
    nutritionPlan: [''],
    sleepPlan: [''],
    activityPlan: [''],
    medicationPlan: ['']
  });

  constructor(
    private readonly carePlanService: CarePlanService,
    private readonly userDirectoryService: UserDirectoryService,
    private readonly fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.deferUi(() => {
      this.loadDirectoryData();
      this.load();
    });
  }

  displayUser(user: AppUser): string {
    const name = `${user.firstName || ''} ${user.lastName || ''}`.trim();
    return name || user.email || `User #${user.id}`;
  }

  load(): void {
    this.deferUi(() => {
      this.loading = true;
      this.error = '';
    });

    this.carePlanService.getCarePlans().subscribe({
      next: (plans) => {
        this.deferUi(() => {
          this.records = plans.map((p) => ({
            id: p.id,
            title: p.patientName || `Patient #${p.patientId}`,
            subtitle: p.providerName || `Provider #${p.providerId}`,
            status: p.priority || p.nutritionStatus || 'N/A',
            updatedAt: p.updatedAt || p.createdAt,
            raw: p
          }));
          this.selected = plans[0] || null;
          this.loading = false;
        });
      },
      error: () => {
        this.deferUi(() => {
          this.error = 'Unable to load care plans from gateway.';
          this.loading = false;
        });
      }
    });
  }

  onSelect(item: ListingItem): void {
    const row = item.raw as CarePlan;
    this.selected = row;
    this.editingId = row.id;
    this.carePlanForm.patchValue({
      patientId: row.patientId?.toString() || '',
      providerId: row.providerId?.toString() || '',
      priority: row.priority || 'MEDIUM',
      nutritionPlan: row.nutritionPlan || '',
      sleepPlan: row.sleepPlan || '',
      activityPlan: row.activityPlan || '',
      medicationPlan: row.medicationPlan || ''
    });
  }

  submitCreate(): void {
    this.deferUi(() => {
      this.formError = '';
      this.formSuccess = '';
    });
    if (this.carePlanForm.invalid) {
      this.deferUi(() => {
        this.formError = 'Patient ID is required to create a care plan.';
      });
      return;
    }

    const patientId = Number(this.carePlanForm.controls.patientId.value);
    if (!Number.isInteger(patientId) || patientId <= 0) {
      this.deferUi(() => {
        this.formError = 'Invalid patient ID. Please select a valid patient from the list.';
      });
      return;
    }

    this.carePlanService.createCarePlan(this.buildPayload()).subscribe({
      next: (result) => {
        if (!result) {
          this.deferUi(() => {
            this.formError = 'Care plan creation failed. Check role permissions.';
          });
          return;
        }
        this.deferUi(() => {
          this.formSuccess = 'Care plan created successfully.';
          this.resetForm();
          this.load();
        });
      },
      error: (err: HttpErrorResponse) => {
        this.deferUi(() => {
          this.formError = this.extractBackendMessage(err, 'Care plan creation failed.');
        });
      }
    });
  }

  submitUpdate(): void {
    this.deferUi(() => {
      this.formError = '';
      this.formSuccess = '';
    });
    if (!this.editingId) {
      this.deferUi(() => {
        this.formError = 'Select a care plan to update.';
      });
      return;
    }
    if (this.carePlanForm.invalid) {
      this.deferUi(() => {
        this.formError = 'Patient ID is required to update a care plan.';
      });
      return;
    }

    const patientId = Number(this.carePlanForm.controls.patientId.value);
    if (!Number.isInteger(patientId) || patientId <= 0) {
      this.deferUi(() => {
        this.formError = 'Invalid patient ID. Please select a valid patient from the list.';
      });
      return;
    }

    this.carePlanService.updateCarePlan(this.editingId, this.buildPayload()).subscribe({
      next: (result) => {
        if (!result) {
          this.deferUi(() => {
            this.formError = 'Care plan update failed. Only owner/admin can update.';
          });
          return;
        }
        this.deferUi(() => {
          this.formSuccess = 'Care plan updated successfully.';
          this.load();
        });
      },
      error: (err: HttpErrorResponse) => {
        this.deferUi(() => {
          this.formError = this.extractBackendMessage(err, 'Care plan update failed.');
        });
      }
    });
  }

  deleteSelected(): void {
    this.deferUi(() => {
      this.formError = '';
      this.formSuccess = '';
    });

    if (!this.editingId) {
      this.deferUi(() => {
        this.formError = 'Select a care plan to delete.';
      });
      return;
    }

    this.carePlanService.deleteCarePlan(this.editingId).subscribe((ok) => {
      if (!ok) {
        this.deferUi(() => {
          this.formError = 'Care plan delete failed.';
        });
        return;
      }

      this.deferUi(() => {
        this.formSuccess = 'Care plan deleted successfully.';
        this.selected = null;
        this.resetForm();
        this.load();
      });
    });
  }

  resetForm(): void {
    this.editingId = null;
    this.carePlanForm.reset({
      patientId: '',
      providerId: '',
      priority: 'MEDIUM',
      nutritionPlan: '',
      sleepPlan: '',
      activityPlan: '',
      medicationPlan: ''
    });
  }

  private buildPayload(): CarePlanRequest {
    const form = this.carePlanForm.getRawValue();
    return {
      patientId: Number(form.patientId),
      providerId: form.providerId ? Number(form.providerId) : undefined,
      priority: (form.priority || 'MEDIUM') as 'LOW' | 'MEDIUM' | 'HIGH',
      nutritionPlan: (form.nutritionPlan || '').trim() || undefined,
      sleepPlan: (form.sleepPlan || '').trim() || undefined,
      activityPlan: (form.activityPlan || '').trim() || undefined,
      medicationPlan: (form.medicationPlan || '').trim() || undefined
    };
  }

  private deferUi(work: () => void): void {
    setTimeout(work, 0);
  }

  private loadDirectoryData(): void {
    this.userDirectoryService.getUsersByRole('PATIENT').subscribe((users) => {
      this.deferUi(() => {
        this.patients = users;
      });
    });
    this.userDirectoryService.getUsersByRole('PROVIDER').subscribe((users) => {
      this.deferUi(() => {
        this.providers = users;
      });
    });
  }

  private extractBackendMessage(err: HttpErrorResponse, fallback: string): string {
    const candidate = err?.error;
    if (typeof candidate === 'string' && candidate.trim()) {
      return candidate;
    }
    if (candidate && typeof candidate.message === 'string' && candidate.message.trim()) {
      return candidate.message;
    }
    if (candidate && typeof candidate.error === 'string' && candidate.error.trim()) {
      return candidate.error;
    }
    return fallback;
  }
}
