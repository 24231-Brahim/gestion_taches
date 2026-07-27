import { ChangeDetectionStrategy, Component, ElementRef, inject, OnDestroy, OnInit, signal, viewChild } from '@angular/core';
import { Router } from '@angular/router';
import { SlicePipe } from '@angular/common';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { SearchService, SearchResult } from './search.service';
import { debounceTime, distinctUntilChanged, Subject, switchMap, takeUntil } from 'rxjs';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-search-dialog',
  standalone: true,
  imports: [FontAwesomeModule, TranslateModule, SlicePipe],
  template: `
    @if (searchService.searchOpen()) {
      <div class="search-overlay" (click)="onOverlayClick($event)">
        <div class="search-dialog">
          <div class="search-input-wrapper">
            <fa-icon icon="search" class="search-icon" />
            <input
              #searchInput
              type="text"
              class="search-input"
              [placeholder]="'global.menu.search.placeholder' | translate"
              [value]="searchService.searchQuery()"
              (input)="onInput($event)"
              (keydown)="onKeydown($event)"
            />
            <kbd class="search-shortcut">ESC</kbd>
          </div>
          @if (searchService.searchLoading()) {
            <div class="search-loading">Searching...</div>
          }
          @if (!searchService.searchLoading() && searchService.searchResults().length > 0) {
            <div class="search-results">
              @for (result of searchService.searchResults(); track $index) {
                <a
                  class="search-result-item"
                  [class.active]="activeIndex() === $index"
                  (click)="selectResult(result)"
                  (mouseenter)="activeIndex.set($index)"
                >
                  <span class="result-type" [class]="'result-type-' + result.type">{{ result.type }}</span>
                  <div class="result-info">
                    <span class="result-title">{{ result.title }}</span>
                    @if (result.description) {
                      <span class="result-description">{{ result.description | slice: 0 : 80 }}</span>
                    }
                  </div>
                  @if (result.projectKey) {
                    <span class="result-project">{{ result.projectKey }}</span>
                  }
                </a>
              }
            </div>
          }
          @if (!searchService.searchLoading() && searchService.searchQuery().length >= 2 && searchService.searchResults().length === 0) {
            <div class="search-empty">No results found</div>
          }
        </div>
      </div>
    }
  `,
  styles: [
    `
      .search-overlay {
        position: fixed;
        inset: 0;
        background: rgba(0, 0, 0, 0.6);
        z-index: 9999;
        display: flex;
        justify-content: center;
        padding-top: 15vh;
      }
      .search-dialog {
        width: 100%;
        max-width: 560px;
        background: var(--color-surface-container-high, #1e2128);
        border: 1px solid var(--color-outline-variant, #2a3038);
        border-radius: var(--radius-lg, 12px);
        overflow: hidden;
        max-height: 60vh;
        display: flex;
        flex-direction: column;
      }
      .search-input-wrapper {
        display: flex;
        align-items: center;
        padding: 12px 16px;
        gap: 10px;
        border-bottom: 1px solid var(--color-outline-variant, #2a3038);
      }
      .search-icon {
        color: var(--color-text-muted, #6a8fac);
        font-size: 1rem;
      }
      .search-input {
        flex: 1;
        background: none;
        border: none;
        outline: none;
        color: var(--color-text, #dfe3ea);
        font-family: var(--font-inter, Inter, sans-serif);
        font-size: 0.95rem;
      }
      .search-input::placeholder {
        color: var(--color-text-muted, #6a8fac);
      }
      .search-shortcut {
        font-family: var(--font-jetbrains, monospace);
        font-size: 0.7rem;
        color: var(--color-text-muted, #6a8fac);
        background: var(--color-surface-container, #171a20);
        border: 1px solid var(--color-outline-variant, #2a3038);
        border-radius: 4px;
        padding: 2px 6px;
      }
      .search-loading,
      .search-empty {
        padding: 24px;
        text-align: center;
        color: var(--color-text-muted, #6a8fac);
        font-family: var(--font-inter, Inter, sans-serif);
        font-size: 0.85rem;
      }
      .search-results {
        overflow-y: auto;
        max-height: 50vh;
        padding: 4px 0;
      }
      .search-result-item {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 10px 16px;
        cursor: pointer;
        text-decoration: none;
        color: inherit;
        transition: background 0.1s;
      }
      .search-result-item:hover,
      .search-result-item.active {
        background: var(--color-surface-container, #171a20);
      }
      .result-type {
        font-family: var(--font-jetbrains, monospace);
        font-size: 0.65rem;
        font-weight: 600;
        text-transform: uppercase;
        padding: 2px 6px;
        border-radius: 4px;
        background: var(--color-outline-variant, #2a3038);
        color: var(--color-text-muted, #6a8fac);
        min-width: 50px;
        text-align: center;
      }
      .result-type-project {
        background: rgba(33, 150, 243, 0.15);
        color: #64b5f6;
      }
      .result-type-task {
        background: rgba(76, 175, 80, 0.15);
        color: #81c784;
      }
      .result-type-sprint {
        background: rgba(255, 152, 0, 0.15);
        color: #ffb74d;
      }
      .result-type-epic {
        background: rgba(156, 39, 176, 0.15);
        color: #ce93d8;
      }
      .result-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 2px;
        min-width: 0;
      }
      .result-title {
        font-family: var(--font-inter, Inter, sans-serif);
        font-size: 0.85rem;
        color: var(--color-text, #dfe3ea);
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      .result-description {
        font-size: 0.75rem;
        color: var(--color-text-muted, #6a8fac);
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      .result-project {
        font-family: var(--font-jetbrains, monospace);
        font-size: 0.7rem;
        color: var(--color-text-muted, #6a8fac);
      }
    `,
  ],
})
export class SearchDialogComponent implements OnInit, OnDestroy {
  readonly searchService = inject(SearchService);
  private readonly router = inject(Router);
  private readonly destroy$ = new Subject<void>();
  private readonly inputSubject = new Subject<string>();

  readonly activeIndex = signal(0);
  readonly searchInput = viewChild<ElementRef<HTMLInputElement>>('searchInput');

  ngOnInit(): void {
    this.inputSubject.pipe(debounceTime(300), distinctUntilChanged(), takeUntil(this.destroy$)).subscribe(query => {
      if (query.length < 2) {
        this.searchService.searchResults.set([]);
        this.searchService.searchLoading.set(false);
        return;
      }
      this.searchService.searchLoading.set(true);
      this.searchService.search(query).subscribe({
        next: results => {
          this.searchService.searchResults.set(results);
          this.searchService.searchLoading.set(false);
          this.activeIndex.set(0);
        },
        error: () => {
          this.searchService.searchLoading.set(false);
        },
      });
    });

    document.addEventListener('keydown', this.handleGlobalKeydown);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    document.removeEventListener('keydown', this.handleGlobalKeydown);
  }

  private handleGlobalKeydown = (e: KeyboardEvent): void => {
    if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
      e.preventDefault();
      if (this.searchService.searchOpen()) {
        this.searchService.close();
      } else {
        this.searchService.open();
        setTimeout(() => this.searchInput()?.nativeElement?.focus(), 50);
      }
    }
  };

  onInput(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.searchService.searchQuery.set(value);
    this.searchService.searchLoading.set(true);
    this.inputSubject.next(value);
  }

  onKeydown(event: KeyboardEvent): void {
    const results = this.searchService.searchResults();
    if (event.key === 'Escape') {
      this.searchService.close();
    } else if (event.key === 'ArrowDown') {
      event.preventDefault();
      this.activeIndex.update(i => Math.min(i + 1, results.length - 1));
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      this.activeIndex.update(i => Math.max(i - 1, 0));
    } else if (event.key === 'Enter') {
      event.preventDefault();
      if (results.length > 0) {
        this.selectResult(results[this.activeIndex()]);
      }
    }
  }

  onOverlayClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('search-overlay')) {
      this.searchService.close();
    }
  }

  selectResult(result: SearchResult): void {
    if (result.link) {
      this.router.navigateByUrl(result.link);
    }
    this.searchService.close();
  }
}
