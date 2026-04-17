import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ListingItem } from '../../../models/listing.model';
import { Consultation, ConsultationRequest } from '../../../models/consultation.model';
import { AppUser } from '../../../models/app-user.model';
import { ConsultationService } from '../../../services/consultation.service';
import { UserDirectoryService } from '../../../services/user-directory.service';
import { ResourceListComponent } from '../components/resource-list/resource-list.component';
import { ResourceViewComponent } from '../components/resource-view/resource-view.component';

@Component({
  selector: 'app-consultation',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ResourceListComponent, ResourceViewComponent],
  templateUrl: './consultation.component.html'
})
export class ConsultationComponent implements OnInit {
  loading = false;
  error = '';
  formError = '';
  formSuccess = '';
  editingId: number | null = null;

  records: ListingItem[] = [];
  selected: Consultation | null = null;
  readonly role = this.getRole();
  patients: AppUser[] = [];
  providers: AppUser[] = [];
  caregivers: AppUser[] = [];

  readonly consultationForm = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(120)]],
    description: [''],
    startTime: ['', Validators.required],
    endTime: [''],
    type: ['ONLINE', Validators.required],
    patientId: ['', Validators.required],
    caregiverId: [''],
    providerId: ['']
  });

  constructor(
    private readonly consultationService: ConsultationService,
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

    this.consultationService.getConsultationsByRole(this.role).subscribe({
      next: (consultations) => {
        this.deferUi(() => {
          this.records = consultations.map((c) => ({
            id: c.id,
            title: c.title,
            subtitle: `${c.patientName || 'Patient'} - ${c.providerName || 'Provider'}`,
            status: c.status,
            updatedAt: c.startTime,
            raw: c
          }));
          this.selected = consultations[0] || null;
          this.loading = false;
        });
      },
      error: () => {
        this.deferUi(() => {
          this.error = 'Unable to load consultations from gateway.';
          this.loading = false;
        });
      }
    });
  }

  onSelect(item: ListingItem): void {
    const row = item.raw as Consultation;
    this.selected = row;
    this.editingId = row.id;
    this.consultationForm.patchValue({
      title: row.title || '',
      description: row.description || '',
      startTime: this.toDateTimeInput(row.startTime),
      endTime: this.toDateTimeInput(row.endTime),
      type: (row.type || 'ONLINE') as 'PRESENTIAL' | 'ONLINE',
      patientId: row.patientId?.toString() || '',
      caregiverId: row.caregiverId?.toString() || '',
      providerId: row.providerId?.toString() || ''
    });
  }

  submitCreate(): void {
    this.deferUi(() => {
      this.formError = '';
      this.formSuccess = '';
    });
    if (this.consultationForm.invalid) {
      this.deferUi(() => {
        this.formError = 'Please fill all required consultation fields.';
      });
      return;
    }

    const patientId = Number(this.consultationForm.controls.patientId.value);
    if (!Number.isInteger(patientId) || patientId <= 0) {
      this.deferUi(() => {
        this.formError = 'Invalid patient ID. Please select a valid patient from the list.';
      });
      return;
    }

    const payload = this.buildPayload();
    this.consultationService.createConsultation(payload).subscribe({
      next: (result) => {
        if (!result) {
          this.deferUi(() => {
            this.formError = 'Consultation creation failed. Check your role and input values.';
          });
          return;
        }
        this.deferUi(() => {
          this.formSuccess = 'Consultation created successfully.';
          this.resetForm();
          this.load();
        });
      },
      error: (err: HttpErrorResponse) => {
        this.deferUi(() => {
          this.formError = this.extractBackendMessage(err, 'Consultation creation failed.');
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
        this.formError = 'Select a consultation to update.';
      });
      return;
    }
    if (this.consultationForm.invalid) {
      this.deferUi(() => {
        this.formError = 'Please fill all required consultation fields.';
      });
      return;
    }

    const patientId = Number(this.consultationForm.controls.patientId.value);
    if (!Number.isInteger(patientId) || patientId <= 0) {
      this.deferUi(() => {
        this.formError = 'Invalid patient ID. Please select a valid patient from the list.';
      });
      return;
    }

    const payload = this.buildPayload();
    this.consultationService.updateConsultation(this.editingId, payload).subscribe({
      next: (result) => {
        if (!result) {
          this.deferUi(() => {
            this.formError = 'Consultation update failed. Only provider owner can update.';
          });
          return;
        }
        this.deferUi(() => {
          this.formSuccess = 'Consultation updated successfully.';
          this.load();
        });
      },
      error: (err: HttpErrorResponse) => {
        this.deferUi(() => {
          this.formError = this.extractBackendMessage(err, 'Consultation update failed.');
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
        this.formError = 'Select a consultation to delete.';
      });
      return;
    }

    this.consultationService.deleteConsultation(this.editingId).subscribe((ok) => {
      if (!ok) {
        this.deferUi(() => {
          this.formError = 'Consultation delete failed. Only provider owner can delete.';
        });
        return;
      }

      this.deferUi(() => {
        this.formSuccess = 'Consultation deleted successfully.';
        this.selected = null;
        this.resetForm();
        this.load();
      });
    });
  }

  resetForm(): void {
    this.editingId = null;
    this.consultationForm.reset({
      title: '',
      description: '',
      startTime: '',
      endTime: '',
      type: 'ONLINE',
      patientId: '',
      caregiverId: '',
      providerId: ''
    });
  }

  private buildPayload(): ConsultationRequest {
    const form = this.consultationForm.getRawValue();
    return {
      title: (form.title || '').trim(),
      description: (form.description || '').trim() || undefined,
      startTime: form.startTime || '',
      endTime: form.endTime || undefined,
      type: (form.type || 'ONLINE') as 'PRESENTIAL' | 'ONLINE',
      patientId: Number(form.patientId),
      caregiverId: form.caregiverId ? Number(form.caregiverId) : undefined,
      providerId: form.providerId ? Number(form.providerId) : undefined
    };
  }

  private toDateTimeInput(value?: string): string {
    if (!value) {
      return '';
    }
    return value.length >= 16 ? value.substring(0, 16) : value;
  }

  private loadDirectoryData(): void {
    this.userDirectoryService.getUsersByRole('PATIENT').subscribe((users) => {
      this.patients = users;
    });
    this.userDirectoryService.getUsersByRole('PROVIDER').subscribe((users) => {
      this.providers = users;
    });
    this.userDirectoryService.getUsersByRole('CAREGIVER').subscribe((users) => {
      this.caregivers = users;
    });
  }

  private deferUi(work: () => void): void {
    setTimeout(work, 0);
  }

  private getRole(): string {
    const userRaw = localStorage.getItem('user');
    if (!userRaw) {
      return 'PATIENT';
    }

    try {
      const user = JSON.parse(userRaw) as { role?: string };
      return (user.role || 'PATIENT').toUpperCase();
    } catch {
      return 'PATIENT';
    }
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
