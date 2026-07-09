import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class HeaderActionsService {
  searchVisible = signal(false);
  searchQuery = signal('');

  private onSearchInputFn: ((query: string) => void) | null = null;
  private onFilterClickFn: (() => void) | null = null;

  show(onSearchInput: (query: string) => void, onFilterClick: () => void) {
    this.searchVisible.set(true);
    this.onSearchInputFn = onSearchInput;
    this.onFilterClickFn = onFilterClick;
  }

  hide() {
    this.searchVisible.set(false);
    this.searchQuery.set('');
    this.onSearchInputFn = null;
    this.onFilterClickFn = null;
  }

  updateQuery(query: string) {
    this.searchQuery.set(query);
    this.onSearchInputFn?.(query);
  }

  triggerFilter() {
    this.onFilterClickFn?.();
  }
}
