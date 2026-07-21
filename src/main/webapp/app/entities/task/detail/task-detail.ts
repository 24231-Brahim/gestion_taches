import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { Alert } from 'app/shared/alert/alert';
import { AlertError } from 'app/shared/alert/alert-error';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { ITask } from '../task.model';
import { TaskCommentsTab } from './tabs/task-comments-tab';
import { TaskAttachmentsTab } from './tabs/task-attachments-tab';
import { TaskHistoryTab } from './tabs/task-history-tab';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-task-detail',
  templateUrl: './task-detail.html',
  styles: [
    `
      .tab-bar {
        display: flex;
        gap: 0;
        border-bottom: 1px solid var(--color-outline-variant, #2a3038);
        margin-bottom: 20px;
      }
      .tab-item {
        padding: 10px 24px;
        cursor: pointer;
        font-family: var(--font-inter);
        font-size: 0.85rem;
        text-transform: none;
        letter-spacing: 0;
        border: none;
        background: transparent;
        color: var(--color-text-muted, #6a8fac);
        border-bottom: 2px solid transparent;
        margin-bottom: -1px;
        transition:
          color var(--transition-fast),
          border-color var(--transition-fast);
      }
      .tab-item:hover {
        color: var(--color-text, #dfe3ea);
      }
      .tab-item.active {
        color: var(--color-primary, #97cbff);
        border-bottom-color: var(--color-primary, #97cbff);
      }
      @media (max-width: 768px) {
        .tab-item {
          padding: 8px 12px;
          font-size: 0.75rem;
        }
      }
    `,
  ],
  imports: [
    RouterLink,
    FontAwesomeModule,
    Alert,
    AlertError,
    TranslateDirective,
    TranslateModule,
    FormatMediumDatetimePipe,
    TaskCommentsTab,
    TaskAttachmentsTab,
    TaskHistoryTab,
  ],
})
export class TaskDetail {
  readonly task = input<ITask | null>(null);
  readonly activeTab = signal('details');

  readonly currentProjectKey = computed(() => this.task()?.project?.key ?? null);

  setTab(tab: string): void {
    this.activeTab.set(tab);
  }

  previousState(): void {
    globalThis.history.back();
  }
}
