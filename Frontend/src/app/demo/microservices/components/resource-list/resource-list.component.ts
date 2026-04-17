import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ListingItem, ListQueryState } from '../../../../models/listing.model';

@Component({
  selector: 'app-resource-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './resource-list.component.html'
})
export class ResourceListComponent implements OnChanges {
  @Input() title = 'Resource List';
  @Input() items: ListingItem[] = [];
  @Input() statusOptions: string[] = [];

  @Output() itemSelected = new EventEmitter<ListingItem>();

  query: ListQueryState = {
    search: '',
    status: 'ALL',
    sortBy: 'updatedAt',
    direction: 'desc'
  };

  filtered: ListingItem[] = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['items']) {
      this.apply();
    }
  }

  apply(): void {
    const normalized = this.query.search.trim().toLowerCase();
    const statusFilter = this.query.status.toUpperCase();

    this.filtered = [...this.items]
      .filter((item) => {
        const passesSearch =
          !normalized ||
          item.title.toLowerCase().includes(normalized) ||
          item.subtitle.toLowerCase().includes(normalized);
        const passesStatus = statusFilter === 'ALL' || (item.status || '').toUpperCase() === statusFilter;
        return passesSearch && passesStatus;
      })
      .sort((a, b) => {
        if (this.query.sortBy === 'title') {
          return this.compare(a.title, b.title);
        }
        if (this.query.sortBy === 'status') {
          return this.compare(a.status || '', b.status || '');
        }
        return this.compareDate(a.updatedAt, b.updatedAt);
      });

    if (this.query.direction === 'desc') {
      this.filtered.reverse();
    }
  }

  select(item: ListingItem): void {
    this.itemSelected.emit(item);
  }

  private compare(left: string, right: string): number {
    return left.localeCompare(right);
  }

  private compareDate(left?: string, right?: string): number {
    const l = left ? new Date(left).getTime() : 0;
    const r = right ? new Date(right).getTime() : 0;
    return l - r;
  }
}
