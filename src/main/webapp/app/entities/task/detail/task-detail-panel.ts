import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, effect, inject, input, output, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';
import { AlertService } from 'app/core/util/alert.service';
import { TaskStatus } from 'app/entities/enumerations/task-status.model';
import { Priority } from 'app/entities/enumerations/priority.model';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { PRIORITY_COLORS, PRIORITY_ICONS, STATUS_BADGES } from '../task-helper';
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
        inset: 0;
        width: 100vw;
        height: 100vh;
        background: var(--color-surface);
        z-index: 1050;
        display: flex;
        flex-direction: column;
        box-shadow: var(--shadow-lg);
      }
      .task-drawer-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 16px 32px;
        border-bottom: 1px solid var(--color-outline-variant);
      }
      .task-drawer-body {
        flex: 1;
        overflow-y: auto;
        display: flex;
        gap: 40px;
        padding: 32px;
        max-width: 1800px;
        width: 100%;
        margin: 0 auto;
      }
      .task-drawer-main {
        flex: 1;
        min-width: 0;
      }
      .task-drawer-sidebar {
        width: 300px;
        flex-shrink: 0;
      }
      .task-drawer-title {
        font-family: var(--font-inter);
        font-size: 1.1rem;
        color: var(--color-text);
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
        color: var(--color-text-muted);
        margin-bottom: 4px;
        font-family: var(--font-inter);
        font-weight: 600;
      }
      .task-description {
        color: var(--color-text);
        font-size: 0.9rem;
        line-height: 1.5;
        white-space: pre-wrap;
      }
      .task-comment-box {
        background: var(--color-surface-container);
        border: 1px solid var(--color-outline-variant);
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
        color: var(--color-text-muted);
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
      .task-drawer-actions {
        margin-top: 24px;
        padding-top: 16px;
        border-top: 1px solid var(--color-outline-variant);
      }
      .assignee-avatar {
        width: 28px;
        height: 28px;
        border-radius: 50%;
        background: var(--color-primary-container);
        color: var(--color-on-primary-container);
        font-size: 0.75rem;
        font-weight: 600;
        display: flex;
        align-items: center;
        justify-content: center;
        font-family: var(--font-mono);
      }
      @media (max-width: 768px) {
        .task-drawer-header {
          padding: 16px 20px;
        }
        .task-drawer-body {
          flex-direction: column;
          padding: 20px;
          gap: 20px;
        }
        .task-drawer-sidebar {
          width: 100%;
        }
      }
      .tab-bar {
        display: flex;
        gap: 0;
        border-bottom: 1px solid var(--color-outline-variant);
        margin-bottom: 16px;
      }
      .tab-item {
        padding: 8px 16px;
        cursor: pointer;
        font-family: var(--font-inter);
        font-size: 0.8rem;
        text-transform: none;
        letter-spacing: 0;
        border: none;
        background: transparent;
        color: var(--color-text-muted);
        border-bottom: 2px solid transparent;
        margin-bottom: -1px;
        transition:
          color var(--transition-fast),
          border-color var(--transition-fast);
      }
      .tab-item:hover {
        color: var(--color-text);
      }
      .tab-item.active {
        color: var(--color-primary);
        border-bottom-color: var(--color-primary);
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

  readonly taskStatusValues = Object.keys(TaskStatus);
  readonly priorityValues = Object.keys(Priority);
  readonly priorityColors = PRIORITY_COLORS;
  readonly priorityIcons = PRIORITY_ICONS;
  readonly statusBadges = STATUS_BADGES;

  readonly isSaving = signal(false);
  readonly assignableUsers = signal<IUser[]>([]);
  readonly activeTab = signal<'details' | 'comments' | 'attachments' | 'history'>('details');
  private readonly lastTaskId = signal<number | null>(null);

  protected readonly taskService = inject(TaskService);
  protected readonly alertService = inject(AlertService);
  protected readonly translateService = inject(TranslateService);
  protected readonly accountService = inject(AccountService);

  // Reassignment is management-only, even for the assignee's own task (matches
  // TaskResource.assignTask's OWNER/MANAGER requirement — ADMIN/PROJET_MANAGER bypass shown here
  // since this shared drawer has no per-project member list loaded).
  readonly canReassign = computed(
    () =>
      this.accountService.account()?.authorities?.includes('ROLE_ADMIN') ||
      this.accountService.account()?.authorities?.includes('ROLE_PROJET_MANAGER') ||
      false,
  );

  // Bare USER accounts are read-only: they never see the edit button in the drawer.
  readonly canEdit = computed(
    () =>
      this.accountService.account()?.authorities.some(a => a === 'ROLE_ADMIN' || a === 'ROLE_PROJET_MANAGER' || a === 'ROLE_DEVELOPER') ??
      false,
  );

  // A plain project member viewing a colleague's task (allowed, read-only, "for context") must not
  // be able to edit its status — only the assignee themself, or ADMIN/PROJET_MANAGER/OWNER/MANAGER.
  canEditStatus(task: ITask): boolean {
    return this.canReassign() || this.accountService.account()?.login === task.assignee?.login;
  }

  constructor() {
    effect(() => {
      if (this.visible() && this.assignableUsers().length === 0) {
        this.taskService.getAssignableUsers().subscribe({
          next: users => this.assignableUsers.set(users),
        });
      }
    });
    effect(() => {
      const id = this.task()?.id ?? null;
      if (id !== untracked(this.lastTaskId)) {
        this.lastTaskId.set(id);
        this.activeTab.set('details');
      }
    });
  }

  setTab(tab: 'details' | 'comments' | 'attachments' | 'history'): void {
    this.activeTab.set(tab);
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
