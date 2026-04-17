export interface ListingItem {
  id: number;
  title: string;
  subtitle: string;
  status?: string;
  updatedAt?: string;
  raw: unknown;
}

export type SortDirection = 'asc' | 'desc';

export interface ListQueryState {
  search: string;
  status: string;
  sortBy: 'title' | 'updatedAt' | 'status';
  direction: SortDirection;
}
