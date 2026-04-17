import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ResourceListComponent } from '../components/resource-list/resource-list.component';
import { ResourceViewComponent } from '../components/resource-view/resource-view.component';
import { MedicalHistoryService } from '../../../services/medical-history.service';
import { ListingItem } from '../../../models/listing.model';
import { AppUser } from '../../../models/app-user.model';
import { MedicalHistory, MedicalHistoryRequest } from '../../../models/medical-history.model';
import { UserDirectoryService } from '../../../services/user-directory.service';

@Component({
  selector: 'app-medical-history',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ResourceListComponent, ResourceViewComponent],
  templateUrl: './medical-history.component.html'
})
export class MedicalHistoryComponent implements OnInit {
  loading = false;
  error = '';
  formError = '';
  formSuccess = '';
  editingPatientId: number | null = null;

  records: ListingItem[] = [];
  selected: MedicalHistory | null = null;
  readonly role = this.getRole();
  patients: AppUser[] = [];
  providers: AppUser[] = [];

  readonly historyForm = this.fb.group({
    patientId: ['', Validators.required],
    providerIds: [[] as string[]],
    diagnosis: [''],
    diagnosisDate: [''],
    progressionStage: ['MILD'],
    geneticRisk: [''],
    familyHistory: [''],
    environmentalFactors: [''],
    comorbidities: [''],
    medicationAllergies: [''],
    environmentalAllergies: [''],
    foodAllergies: ['']
  });

  constructor(
    private readonly medicalHistoryService: MedicalHistoryService,
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

    this.medicalHistoryService.getHistoryListByRole(this.role).subscribe({
      next: (histories) => {
        this.deferUi(() => {
          this.records = histories.map((h) => ({
            id: h.id,
            title: h.patientName || `Patient #${h.patientId}`,
            subtitle: h.diagnosis || 'No diagnosis',
            status: h.progressionStage || 'N/A',
            updatedAt: h.updatedAt || h.createdAt,
            raw: h
          }));
          this.selected = histories[0] || null;
          this.loading = false;
        });
      },
      error: () => {
        this.deferUi(() => {
          this.error = 'Unable to load medical history records from gateway.';
          this.loading = false;
        });
      }
    });
  }

  onSelect(item: ListingItem): void {
    const row = item.raw as MedicalHistory;
    this.selected = row;
    this.editingPatientId = row.patientId;
    this.historyForm.patchValue({
      patientId: row.patientId?.toString() || '',
      providerIds: [],
      diagnosis: row.diagnosis || '',
      diagnosisDate: row.diagnosisDate || '',
      progressionStage: (row.progressionStage || 'MILD') as 'MILD' | 'MODERATE' | 'SEVERE',
      geneticRisk: row.geneticRisk || '',
      familyHistory: row.familyHistory || '',
      comorbidities: row.comorbidities || '',
      medicationAllergies: row.medicationAllergies || '',
      environmentalFactors: '',
      environmentalAllergies: '',
      foodAllergies: ''
    });
  }

  submitCreate(): void {
    this.deferUi(() => {
      this.formError = '';
      this.formSuccess = '';
    });
    if (this.historyForm.invalid) {
      this.deferUi(() => {
        this.formError = 'Patient ID is required to create medical history.';
      });
      return;
    }

    const patientId = Number(this.historyForm.controls.patientId.value);
    if (!Number.isInteger(patientId) || patientId <= 0) {
      this.deferUi(() => {
        this.formError = 'Invalid patient ID. Please select a valid patient from the list.';
      });
      return;
    }

    this.medicalHistoryService.createHistory(this.buildPayload()).subscribe((result) => {
      if (!result) {
        this.deferUi(() => {
          this.formError = 'Medical history creation failed. Provider role required.';
        });
        return;
      }
      this.deferUi(() => {
        this.formSuccess = 'Medical history created successfully.';
        this.resetForm();
        this.load();
      });
    });
  }

  submitUpdate(): void {
    this.deferUi(() => {
      this.formError = '';
      this.formSuccess = '';
    });
    if (!this.editingPatientId) {
      this.deferUi(() => {
        this.formError = 'Select a medical history record to update.';
      });
      return;
    }
    if (this.historyForm.invalid) {
      this.deferUi(() => {
        this.formError = 'Patient ID is required to update medical history.';
      });
      return;
    }

    const patientId = Number(this.historyForm.controls.patientId.value);
    if (!Number.isInteger(patientId) || patientId <= 0) {
      this.deferUi(() => {
        this.formError = 'Invalid patient ID. Please select a valid patient from the list.';
      });
      return;
    }

    this.medicalHistoryService.updateHistory(this.editingPatientId, this.buildPayload()).subscribe((result) => {
      if (!result) {
        this.deferUi(() => {
          this.formError = 'Medical history update failed. Provider role required.';
        });
        return;
      }
      this.deferUi(() => {
        this.formSuccess = 'Medical history updated successfully.';
        this.load();
      });
    });
  }

  deleteSelected(): void {
    this.deferUi(() => {
      this.formError = '';
      this.formSuccess = '';
    });

    if (!this.editingPatientId) {
      this.deferUi(() => {
        this.formError = 'Select a medical history record to delete.';
      });
      return;
    }

    this.medicalHistoryService.deleteHistory(this.editingPatientId).subscribe((ok) => {
      if (!ok) {
        this.deferUi(() => {
          this.formError = 'Medical history delete failed. Provider role required.';
        });
        return;
      }

      this.deferUi(() => {
        this.formSuccess = 'Medical history deleted successfully.';
        this.selected = null;
        this.resetForm();
        this.load();
      });
    });
  }

  resetForm(): void {
    this.editingPatientId = null;
    this.historyForm.reset({
      patientId: '',
      providerIds: [],
      diagnosis: '',
      diagnosisDate: '',
      progressionStage: 'MILD',
      geneticRisk: '',
      familyHistory: '',
      environmentalFactors: '',
      comorbidities: '',
      medicationAllergies: '',
      environmentalAllergies: '',
      foodAllergies: ''
    });
  }

  private buildPayload(): MedicalHistoryRequest {
    const form = this.historyForm.getRawValue();
    return {
      patientId: Number(form.patientId),
      providerIds: (form.providerIds || []).map((id) => Number(id)).filter((id) => !Number.isNaN(id)),
      diagnosis: (form.diagnosis || '').trim() || undefined,
      diagnosisDate: form.diagnosisDate || undefined,
      progressionStage: (form.progressionStage || 'MILD') as 'MILD' | 'MODERATE' | 'SEVERE',
      geneticRisk: (form.geneticRisk || '').trim() || undefined,
      familyHistory: (form.familyHistory || '').trim() || undefined,
      environmentalFactors: (form.environmentalFactors || '').trim() || undefined,
      comorbidities: (form.comorbidities || '').trim() || undefined,
      medicationAllergies: (form.medicationAllergies || '').trim() || undefined,
      environmentalAllergies: (form.environmentalAllergies || '').trim() || undefined,
      foodAllergies: (form.foodAllergies || '').trim() || undefined
    };
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
}
