import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WellbeingService, WellbeingRecord, ActivityRecord } from '../../../services/wellbeing.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-wellbeing',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './wellbeing.component.html',
  styleUrls: ['./wellbeing.component.scss']
})
export class WellbeingComponent implements OnInit {
  userId = '';

  wellbeingRows: WellbeingRecord[] = [];
  latestWellbeing: WellbeingRecord | null = null;
  todayWellbeing: WellbeingRecord | null = null;
  avgSleepHours: number | null = null;

  /** Formulaire quotidien (une entrée / jour côté API). */
  wellbeingForm: {
    mood: string;
    sleepHours: number;
    stressLevel: number;
    memoryDifficulty: number;
    appetite: string;
    notes: string;
  } = {
    mood: 'HAPPY',
    sleepHours: 7,
    stressLevel: 2,
    memoryDifficulty: 2,
    appetite: 'NORMAL',
    notes: ''
  };

  editingTodayId: number | null = null;

  activityRows: ActivityRecord[] = [];
  activityForm: {
    activityType: string;
    durationMinutes: number;
    intensity: string;
    assistedBy: string;
    notes: string;
  } = {
    activityType: 'WALKING',
    durationMinutes: 30,
    intensity: 'MODERATE',
    assistedBy: '',
    notes: ''
  };

  moodCodes = [
    { code: 'HAPPY', label: 'Heureux', emoji: '😊' },
    { code: 'NEUTRAL', label: 'Neutre', emoji: '😐' },
    { code: 'SAD', label: 'Triste', emoji: '😢' },
    { code: 'IRRITATED', label: 'Irrité', emoji: '😠' },
    { code: 'ANXIOUS', label: 'Anxieux', emoji: '😰' },
    { code: 'TIRED', label: 'Fatigué', emoji: '😴' }
  ];

  appetiteOptions = ['GOOD', 'NORMAL', 'LOW'];
  activityTypes = ['WALKING', 'EXERCISE', 'PHYSIOTHERAPY', 'GARDENING', 'OTHER'];
  intensityOptions = ['LOW', 'MODERATE', 'HIGH'];

  constructor(
    private wellbeingService: WellbeingService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const user = this.authService.getUser();
    if (user) {
      this.userId = String(user.id ?? user._id ?? user.userId ?? '');
      this.reload();
    }
  }

  private todayISO(): string {
    const d = new Date();
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }

  private normalizeDate(raw: string | undefined): string {
    if (!raw) return '';
    return raw.length >= 10 ? raw.slice(0, 10) : raw;
  }

  reload(): void {
    if (!this.userId) return;

    this.wellbeingService.listWellbeingByUser(this.userId).subscribe({
      next: (rows) => {
        this.wellbeingRows = [...rows].sort((a, b) =>
          this.normalizeDate(b.date).localeCompare(this.normalizeDate(a.date))
        );
        this.latestWellbeing = this.wellbeingRows[0] ?? null;

        const sleeps = this.wellbeingRows
          .map((r) => r.sleepHours)
          .filter((h): h is number => h != null && !Number.isNaN(h));
        this.avgSleepHours =
          sleeps.length > 0 ? Math.round((sleeps.reduce((a, b) => a + b, 0) / sleeps.length) * 10) / 10 : null;

        const t = this.todayISO();
        this.todayWellbeing =
          this.wellbeingRows.find((r) => this.normalizeDate(r.date) === t) ?? null;

        if (this.todayWellbeing?.id != null) {
          this.editingTodayId = this.todayWellbeing.id;
          this.patchFormFromRecord(this.todayWellbeing);
        } else {
          this.editingTodayId = null;
          this.resetFormDefaults();
        }
      },
      error: (err) => console.error('Wellbeing list failed', err)
    });

    this.wellbeingService.listActivitiesByUser(this.userId).subscribe({
      next: (rows) => {
        this.activityRows = [...rows].sort((a, b) =>
          this.normalizeDate(b.date).localeCompare(this.normalizeDate(a.date))
        );
      },
      error: (err) => console.error('Activities list failed', err)
    });
  }

  private patchFormFromRecord(r: WellbeingRecord): void {
    this.wellbeingForm = {
      mood: r.mood ?? 'HAPPY',
      sleepHours: r.sleepHours ?? 7,
      stressLevel: r.stressLevel ?? 2,
      memoryDifficulty: r.memoryDifficulty ?? 2,
      appetite: r.appetite ?? 'NORMAL',
      notes: r.notes ?? ''
    };
  }

  private resetFormDefaults(): void {
    this.wellbeingForm = {
      mood: 'HAPPY',
      sleepHours: 7,
      stressLevel: 2,
      memoryDifficulty: 2,
      appetite: 'NORMAL',
      notes: ''
    };
  }

  setMoodCode(code: string): void {
    this.wellbeingForm.mood = code;
  }

  saveDailyWellbeing(): void {
    if (!this.userId) return;

    const payload: WellbeingRecord = {
      userId: this.userId,
      date: this.todayISO(),
      mood: this.wellbeingForm.mood,
      sleepHours: this.wellbeingForm.sleepHours,
      stressLevel: this.wellbeingForm.stressLevel,
      memoryDifficulty: this.wellbeingForm.memoryDifficulty,
      appetite: this.wellbeingForm.appetite,
      notes: this.wellbeingForm.notes || undefined
    };

    if (this.editingTodayId != null) {
      this.wellbeingService.updateWellbeing(this.editingTodayId, payload).subscribe({
        next: () => this.reload(),
        error: (e) => alert(this.formatError(e, 'Mise à jour impossible.'))
      });
      return;
    }

    this.wellbeingService.createWellbeing(payload).subscribe({
      next: () => this.reload(),
      error: (e) => {
        const msg = this.formatError(e, 'Enregistrement impossible.');
        alert(msg);
        this.reload();
      }
    });
  }

  deleteWellbeingRow(id: number | undefined): void {
    if (id == null) return;
    if (!confirm('Supprimer cette entrée bien-être ?')) return;
    this.wellbeingService.deleteWellbeing(id).subscribe({
      next: () => this.reload(),
      error: (e) => alert(this.formatError(e, 'Suppression impossible.'))
    });
  }

  saveActivity(): void {
    if (!this.userId) return;
    const body: ActivityRecord = {
      userId: this.userId,
      date: this.todayISO(),
      activityType: this.activityForm.activityType,
      durationMinutes: this.activityForm.durationMinutes,
      intensity: this.activityForm.intensity,
      assistedBy: this.activityForm.assistedBy || undefined,
      notes: this.activityForm.notes || undefined
    };
    this.wellbeingService.createActivity(body).subscribe({
      next: () => {
        this.activityForm = {
          ...this.activityForm,
          notes: '',
          assistedBy: ''
        };
        this.reload();
      },
      error: (e) => alert(this.formatError(e, 'Activité non enregistrée.'))
    });
  }

  deleteActivityRow(id: number | undefined): void {
    if (id == null) return;
    if (!confirm('Supprimer cette activité ?')) return;
    this.wellbeingService.deleteActivity(id).subscribe({
      next: () => this.reload(),
      error: (e) => alert(this.formatError(e, 'Suppression impossible.'))
    });
  }

  private formatError(err: unknown, fallback: string): string {
    const e = err as { error?: unknown; message?: string };
    if (typeof e?.error === 'string') return e.error;
    if (e?.error && typeof (e.error as { message?: string }).message === 'string') {
      return (e.error as { message: string }).message;
    }
    return e?.message ?? fallback;
  }

  moodEmoji(code: string | undefined): string {
    return this.moodCodes.find((m) => m.code === code)?.emoji ?? '•';
  }
}
