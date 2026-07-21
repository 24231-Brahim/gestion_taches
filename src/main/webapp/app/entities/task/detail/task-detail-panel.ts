import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, effect, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { AlertService } from 'app/core/util/alert.service';
import { TaskStatus } from 'app/entities/enumerations/task-status.model';
import { TaskType } from 'app/entities/enumerations/task-type.model';
import { Priority } from 'app/entities/enumerations/priority.model';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { ISSUE_TYPE_COLORS, ISSUE_TYPE_ICONS, PRIORITY_COLORS, PRIORITY_ICONS, STATUS_BADGES } from '../task-helper';
import { ITask } from '../task.model';
import { TaskService } from '../service/task.service';
import { TaskCommentList } from '../comments/task-comment-list';
import { TaskAttachmentList } from '../attachments/task-attachment-list';
import { TaskActivityFeed } from '../activity/task-activity-feed';
import { IUser } from 'app/entities/user/user.model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-task-detail-panel',
  templateUrl: './task-detail-panel.html',
  styles: [
    `
      .task-drawer-overlay {
        position: fixed;
        inset: 0;
        background: rgba(0, 0, 0, 0.5);
        z-index: 1040;
      }
      .task-drawer {
        position: fixed;
        top: 0;
        right: 0;
        width: min(680px, 100vw);
        height: 100vh;
        background: var(--color-surface, #0f1419);
        border-left: 1px solid var(--color-outline-variant, #2a3038);
        border-radius: var(--radius-lg) 0 0 var(--radius-lg);
        z-index: 1050;
        display: flex;
        flex-direction: column;
        box-shadow: var(--shadow-lg);
      }
      .task-drawer-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 16px 20px;
        border-bottom: 1px solid var(--color-outline-variant, #2a3038);
      }
      .task-drawer-body {
        flex: 1;
        overflow-y: auto;
        display: flex;
        gap: 20px;
        padding: 20px;
      }
      .task-drawer-main {
        flex: 1;
        min-width: 0;
      }
      .task-drawer-sidebar {
        width: 220px;
        flex-shrink: 0;
      }
      .task-drawer-title {
        font-family: var(--font-inter);
        font-size: 1.1rem;
        color: var(--color-text, #dfe3ea);
        margin-bottom: 20px;
        word-break: break-word;
      }
      .task-section {
        margin-bottom: 20px;
      }
      .task-section-label {
        display: block;
        font-size: 0.7rem;
        text-transform: none;
        color: var(--color-text-muted, #6a8fac);
        margin-bottom: 4px;
        font-family: var(--font-inter);
        font-weight: 600;
      }
      .task-description {
        color: var(--color-text, #dfe3ea);
        font-size: 0.9rem;
        line-height: 1.5;
        white-space: pre-wrap;
      }
      .task-comment-box {
        background: var(--color-surface-container, #1b2025);
        border: 1px solid var(--color-outline-variant, #2a3038);
        border-radius: var(--radius-lg);
        padding: 12px;
        min-height: 40px;
      }
      .task-field-group {
        margin-bottom: 16px;
      }
      .task-field-label {
        display: block;
        font-size: 0.65rem;
        text-transform: none;
        color: var(--color-text-muted, #6a8fac);
        margin-bottom: 4px;
        font-family: var(--font-inter);
        font-weight: 600;
      }
      .task-assignee {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 0.9rem;
      }
      .task-type-badge {
        display: none;
      }
      .task-drawer-actions {
        margin-top: 24px;
        padding-top: 16px;
        border-top: 1px solid var(--color-outline-variant, #2a3038);
      }
      .assignee-avatar {
        width: 28px;
        height: 28px;
        border-radius: 50%;
        background: var(--color-primary-container, #25a7fd);
        color: #000;
        font-size: 0.75rem;
        font-weight: 600;
        display: flex;
        align-items: center;
        justify-content: center;
        font-family: var(--font-mono);
      }
      @media (max-width: 768px) {
        .task-drawer {
          width: 100vw;
        }
        .task-drawer-body {
          flex-direction: column;
        }
        .task-drawer-sidebar {
          width: 100%;
        }
      }
    `,
  ],
  imports: [
    RouterLink,
    FormsModule,
    FontAwesomeModule,
    TranslateDirective,
    TranslateModule,
    FormatMediumDatetimePipe,
    TaskCommentList,
    TaskAttachmentList,
    TaskActivityFeed,
  ],
})
export class TaskDetailPanel {
  readonly task = input.required<ITask | null>();
  readonly visible = input(false);
  readonly close = input.required<() => void>();
  readonly taskChanged = output<ITask>();

  readonly taskTypeLabels = TaskType;
  readonly taskStatusValues = Object.keys(TaskStatus);
  readonly taskTypeValues = Object.keys(TaskType);
  readonly priorityValues = Object.keys(Priority);
  readonly typeColors = ISSUE_TYPE_COLORS;
  readonly typeIcons = ISSUE_TYPE_ICONS;
  readonly priorityColors = PRIORITY_COLORS;
  readonly priorityIcons = PRIORITY_ICONS;
  readonly statusBadges = STATUS_BADGES;

  readonly isSaving = signal(false);
  readonly assignableUsers = signal<IUser[]>([]);

  protected readonly taskService = inject(TaskService);
  protected readonly alertService = inject(AlertService);
  protected readonly translateService = inject(TranslateService);

  constructor() {
    effect(() => {
      if (this.visible() && this.assignableUsers().length === 0) {
        this.taskService.getAssignableUsers().subscribe({
          next: users => this.assignableUsers.set(users),
        });
      }
    });
  }

  onStatusChange(task: ITask, newStatus: string): void {
    if (task.status === newStatus) {
      return;
    }
    this.isSaving.set(true);
    this.taskService.partialUpdate({ id: task.id, status: newStatus as keyof typeof TaskStatus }).subscribe({
      next: updated => {
        this.taskChanged.emit({ ...task, ...updated });
        this.isSaving.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isSaving.set(false);
        const message = err.error?.detail ?? err.message ?? this.translateService.instant('error.general');
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  onAssignChange(task: ITask, userId: string): void {
    if (!userId) {
      return;
    }
    this.isSaving.set(true);
    this.taskService.assign(task.id, Number(userId)).subscribe({
      next: updated => {
        this.taskChanged.emit({ ...task, ...updated });
        this.isSaving.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isSaving.set(false);
        const message = err.error?.detail ?? err.message ?? this.translateService.instant('error.general');
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  getInitials(login: string | undefined): string {
    if (!login) {
      return '?';
    }
    return login.charAt(0).toUpperCase();
  }
}
