import { Injectable, signal } from '@angular/core';

export type Theme = 'dark' | 'light';

const STORAGE_KEY = 'theme';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  readonly theme = signal<Theme>(this.loadInitialTheme());

  constructor() {
    this.applyTheme(this.theme());
  }

  toggle(): void {
    const newTheme = this.theme() === 'dark' ? 'light' : 'dark';
    this.theme.set(newTheme);
    this.applyTheme(newTheme);
    localStorage.setItem(STORAGE_KEY, newTheme);
  }

  private loadInitialTheme(): Theme {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === 'dark' || stored === 'light') {
      return stored;
    }
    return 'light';
  }

  private applyTheme(theme: Theme): void {
    document.documentElement.setAttribute('data-theme', theme);
  }
}
