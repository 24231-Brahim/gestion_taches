import { ChangeDetectionStrategy, Component, OnInit, inject, input, signal } from '@angular/core';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { ITaskHistory } from 'app/entities/task-history/task-history.model';
import { TaskHistoryService } from 'app/entities/task-history/service/task-history.service';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-task-activity-feed',
  templateUrl: './task-activity-feed.html',
  styles: [
    `
      .task-activity {
        display: flex;
        flex-direction: column;
        gap: 12px;
      }
      .task-section-title {
        font-family: var(--font-inter);
        font-size: 0.8rem;
        text-transform: none;
        color: var(--color-text-muted);
        margin: 0 0 4px;
        font-weight: 600;
      }
      .activity-feed {
        display: flex;
        flex-direction: column;
        gap: 0;
      }
      .activity-item {
        display: flex;
        gap: 12px;
        padding: 10px 0;
        border-bottom: 1px solid var(--color-outline-variant);
      }
      .activity-item:last-child {
        border-bottom: none;
      }
      .activity-dot {
        width: 10px;
        height: 10px;
        margin-top: 5px;
        flex-shrink: 0;
        background: var(--color-primary);
        border: 2px solid var(--color-primary-container);
        border-radius: 50%;
      }
      .activity-content {
        font-family: var(--font-inter);
        font-size: 0.8rem;
        color: var(--color-text);
        line-height: 1.5;
      }
      .activity-action {
        font-weight: 600;
        color: var(--color-primary);
      }
      .activity-detail {
        color: var(--color-text-muted);
      }
      .activity-date {
        font-size: 0.7rem;
        color: var(--color-text-muted);
        margin-top: 2px;
      }
      .text-muted {
        color: var(--color-text-muted);
        font-family: var(--font-inter);
        font-size: 0.8rem;
      }
    `,
  ],
  imports: [FontAwesomeModule, TranslateDirective, TranslateModule, FormatMediumDatetimePipe],
})
export class TaskActivityFeed implements OnInit {
  readonly taskId = input.required<number>();

  readonly histories = signal<ITaskHistory[]>([]);

  protected readonly taskHistoryService = inject(TaskHistoryService);

  ngOnInit(): void {
    this.loadHistories();
  }

  loadHistories(): void {
    this.taskHistoryService.findByTask(this.taskId()).subscribe({
      next: histories => this.histories.set(histories),
    });
  }
}
