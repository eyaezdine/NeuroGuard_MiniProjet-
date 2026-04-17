import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-resource-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './resource-view.component.html'
})
export class ResourceViewComponent {
  @Input() title = 'Detail View';
  @Input() data: unknown = null;

  asEntries(): Array<{ key: string; value: unknown }> {
    if (!this.data || typeof this.data !== 'object') {
      return [];
    }
    return Object.entries(this.data as Record<string, unknown>).map(([key, value]) => ({ key, value }));
  }
}
