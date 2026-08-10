import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import dayjs from 'dayjs/esm';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';
import { AlertService } from 'app/core/util/alert.service';
import { FormatMediumDatePipe, FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { TaskService } from 'app/entities/task/service/task.service';
import { ITask } from 'app/entities/task/task.model';
import { TaskKanbanBoard } from 'app/entities/task/kanban/task-kanban-board';
import { EpicService } from '../service/epic.service';
import { IEpic } from '../epic.model';

type Tab = 'tasks' | 'kanban' | 'timeline';

interface EpicStats {
  totalTasks: number;
  doneTasks: number;
  inProgressTasks: number;
  todoTasks: number;
  totalStoryPoints: number;
  doneStoryPoints: number;
  progress: number;
}

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-epic-detail',
  templateUrl: './epic-detail.html',
  styles: [
    `
      .epic-detail-container {
        padding: 24px;
        max-width: 1400px;
        margin: 0 auto;
      }
      .epic-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 20px;
        flex-wrap: wrap;
        gap: 12px;
      }
      .epic-header-info {
        display: flex;
        align-items: center;
        gap: 16px;
      }
      .epic-title {
        font-family: var(--font-inter);
        font-size: 1.4rem;
        color: var(--color-text);
        margin: 0;
      }
      .epic-meta {
        color: var(--color-text-muted);
        font-size: 0.85rem;
        font-family: var(--font-inter);
      }
      .epic-description {
        color: var(--color-text);
        font-size: 0.9rem;
        margin: 4px 0;
      }
      .status-badge {
        display: inline-block;
        padding: 2px 10px;
        border: 1px solid var(--color-outline-variant);
        border-radius: 9999px;
        font-size: 0.75rem;
        font-family: var(--font-inter);
        text-transform: none;
        background: var(--color-surface-container);
        font-weight: 600;
      }
      .tab-bar {
        display: flex;
        gap: 0;
        border-bottom: 1px solid var(--color-outline-variant);
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
      .progress-wrapper {
        margin-bottom: 20px;
        padding: 12px 16px;
        border: 1px solid var(--color-outline-variant);
        border-radius: var(--radius-lg);
        background: var(--color-surface-container);
        box-shadow: var(--shadow-sm);
      }
      .progress-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 8px;
      }
      .progress-label {
        font-family: var(--font-inter);
        font-size: 0.8rem;
        color: var(--color-text-muted);
        text-transform: none;
      }
      .progress-value {
        font-family: var(--font-inter);
        font-size: 0.85rem;
        color: var(--color-primary);
        font-weight: 600;
      }
      .progress-bar {
        width: 100%;
        height: 6px;
        background: var(--color-surface-container-high);
        border-radius: 9999px;
        overflow: hidden;
      }
      .progress-fill {
        height: 100%;
        background: var(--color-status-done);
        border-radius: 9999px;
        transition: width 0.3s ease;
      }
      .stats-grid {
        display: flex;
        gap: 12px;
        flex-wrap: wrap;
        margin-bottom: 20px;
      }
      .stat-card {
        flex: 1;
        min-width: 140px;
        border: 1px solid var(--color-outline-variant);
        border-radius: var(--radius-lg);
        padding: 12px 16px;
        background: var(--color-surface-container);
        box-shadow: var(--shadow-sm);
      }
      .stat-label {
        font-family: var(--font-inter);
        font-size: 0.7rem;
        text-transform: none;
        color: var(--color-text-muted);
        display: block;
        margin-bottom: 4px;
      }
      .stat-value {
        font-family: var(--font-inter);
        font-size: 1.3rem;
        font-weight: 600;
      }
      .stat-value.done {
        color: var(--color-status-done);
      }
      .stat-value.in-progress {
        color: var(--color-status-in-progress);
      }
      .stat-value.todo {
        color: var(--color-status-todo);
      }
      .stat-value.points {
        color: var(--color-primary);
      }
      .task-table {
        width: 100%;
        border-collapse: collapse;
        font-size: 0.85rem;
      }
      .task-table th {
        font-family: var(--font-inter);
        font-size: 0.7rem;
        text-transform: none;
        color: var(--color-text-muted);
        text-align: left;
        padding: 8px 12px;
        border-bottom: 1px solid var(--color-outline-variant);
        font-weight: 600;
      }
      .task-table td {
        padding: 8px 12px;
        border-bottom: 1px solid var(--color-outline-variant);
        color: var(--color-text);
      }
      .task-table tr:hover {
        background: rgba(255, 255, 255, 0.03);
      }
      .task-table tbody tr {
        cursor: pointer;
      }
      .assignee-initials {
        width: 22px;
        height: 22px;
        border-radius: 50%;
        background: var(--color-primary-container);
        color: var(--color-on-primary-container);
        font-size: 0.65rem;
        font-weight: 600;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        font-family: var(--font-mono);
      }
      .sp-badge {
        background: var(--color-primary-container);
        color: var(--color-on-primary-container);
        font-size: 0.65rem;
        font-weight: 600;
        padding: 1px 6px;
        font-family: var(--font-mono);
        border-radius: var(--radius-sm);
      }
      .timeline-container {
        position: relative;
        padding: 20px 0;
      }
      .timeline-bar {
        position: relative;
        height: 6px;
        background: var(--color-surface-container-high);
        border-radius: 9999px;
        margin: 40px 0;
      }
      .timeline-fill {
        height: 100%;
        background: var(--color-primary);
        border-radius: 9999px;
        transition: width 0.4s ease;
      }
      .timeline-marker {
        position: absolute;
        top: -16px;
        transform: translateX(-50%);
        font-family: var(--font-inter);
        font-size: 0.65rem;
        color: var(--color-text-muted);
      }
      .sprint-list {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
      }
      .sprint-chip {
        padding: 4px 12px;
        border: 1px solid var(--color-outline-variant);
        border-radius: var(--radius-sm);
        font-size: 0.75rem;
        font-family: var(--font-inter);
        background: var(--color-surface-container);
        color: var(--color-text);
        text-decoration: none;
      }
      .sprint-chip:hover {
        border-color: var(--color-primary);
        color: var(--color-primary);
      }
      @media (max-width: 768px) {
        .epic-header {
          flex-direction: column;
          align-items: flex-start;
        }
        .tab-item {
          padding: 8px 12px;
          font-size: 0.75rem;
        }
        .stats-grid {
          flex-direction: column;
        }
      }
    `,
  ],
  imports: [
    RouterLink,
    FontAwesomeModule,
    TranslateDirective,
    TranslateModule,
    FormatMediumDatePipe,
    FormatMediumDatetimePipe,
    TaskKanbanBoard,
  ],
})
export class EpicDetail {
  readonly epic = input<IEpic | null>(null);

  readonly activeTab = signal<Tab>('tasks');
  readonly tasks = signal<ITask[]>([]);

  readonly currentEpic = signal<IEpic | null>(null);
  readonly currentProjectKey = signal<string | null>(null);

  protected readonly taskService = inject(TaskService);
  protected readonly alertService = inject(AlertService);
  protected readonly translateService = inject(TranslateService);
  protected readonly activatedRoute = inject(ActivatedRoute);
  protected readonly accountService = inject(AccountService);
  protected readonly epicService = inject(EpicService);
  protected readonly router = inject(Router);

  private epicSyncEffect = effect(() => {
    this.currentEpic.set(this.epic());
  });

  // ADMIN/PROJET_MANAGER bypass only — this page doesn't have the project's member list loaded,
  // so a non-admin OWNER/MANAGER won't see this link either (safe default: never shown to anyone
  // unauthorized, matches the backend's EpicService which requires OWNER/MANAGER project role).
  readonly canManageEpics = computed(
    () =>
      this.accountService.account()?.authorities?.includes('ROLE_ADMIN') ||
      this.accountService.account()?.authorities?.includes('ROLE_PROJET_MANAGER') ||
      false,
  );

  readonly epicStats = computed<EpicStats>(() => {
    const t = this.tasks();
    const total = t.length;
    const done = t.filter(i => i.status === 'DONE').length;
    const inProgress = t.filter(i => i.status === 'IN_PROGRESS').length;
    const todo = t.filter(i => i.status === 'NEW').length;
    const totalSp = t.reduce((s, i) => s + (i.storyPoints ?? 0), 0);
    const doneSp = t.filter(i => i.status === 'DONE').reduce((s, i) => s + (i.storyPoints ?? 0), 0);
    return {
      totalTasks: total,
      doneTasks: done,
      inProgressTasks: inProgress,
      todoTasks: todo,
      totalStoryPoints: totalSp,
      doneStoryPoints: doneSp,
      progress: total > 0 ? Math.round((done / total) * 100) : 0,
    };
  });

  readonly uniqueSprints = computed(() => {
    const seen = new Map<number, { id: number; name: string }>();
    for (const t of this.tasks()) {
      if (t.sprint?.id && !seen.has(t.sprint.id)) {
        seen.set(t.sprint.id, { id: t.sprint.id, name: t.sprint.name ?? '' });
      }
    }
    return Array.from(seen.values());
  });

  readonly uniqueAssignees = computed(() => {
    const logins = new Set<string>();
    for (const t of this.tasks()) {
      if (t.assignee?.login) {
        logins.add(t.assignee.login);
      }
    }
    return Array.from(logins).sort();
  });

  private epicEffect = effect(() => {
    const ep = this.epic();
    if (ep?.id) {
      // projectId is required alongside epicId: TaskResource.requireCriteriaProjectAccess()
      // rejects any non-admin/PM request with a 403 if it lacks a projectId filter, which was
      // silently emptying this page's task list for real project members (only ADMIN/PROJET_MANAGER
      // bypass that check and never saw the bug).
      const queryObject: Record<string, string | number> = {
        'epicId.equals': ep.id,
        size: 500,
      };
      if (ep.project?.id) {
        queryObject['projectId.equals'] = ep.project.id;
      }
      this.taskService.tasksParams.set(queryObject);
    }
  });

  private projectKeyEffect = effect(() => {
    let route: ActivatedRoute | null = this.activatedRoute;
    while (route) {
      const key = route.snapshot.paramMap.get('key');
      if (key) {
        this.currentProjectKey.set(key);
        return;
      }
      route = route.parent;
    }
  });

  private tasksEffect = effect(() => {
    const raw = this.taskService.tasks();
    if (raw) {
      this.tasks.set(raw.filter(i => i.epic?.id === this.epic()?.id));
    }
  });

  getStatusColor(status: string | null | undefined): string {
    const colors: Record<string, string> = {
      NEW: 'var(--color-status-backlog)',
      IN_PROGRESS: 'var(--color-status-in-progress)',
      READY_FOR_TEST: 'var(--color-status-in-review)',
      DONE: 'var(--color-status-done)',
      NEEDS_INFO: 'var(--color-status-cancelled)',
    };
    return colors[status ?? ''] ?? 'var(--color-outline-variant)';
  }

  getPriorityColor(priority: string | null | undefined): string {
    const colors: Record<string, string> = {
      LOWEST: 'var(--color-priority-lowest)',
      LOW: 'var(--color-priority-low)',
      MEDIUM: 'var(--color-priority-medium)',
      HIGH: 'var(--color-priority-high)',
      HIGHEST: 'var(--color-priority-highest)',
    };
    return colors[priority ?? ''] ?? 'var(--color-outline-variant)';
  }

  getEpicStatusColor(status: string | null | undefined): string {
    const colors: Record<string, string> = {
      TODO: 'var(--color-status-todo)',
      IN_PROGRESS: 'var(--color-status-in-progress)',
      DONE: 'var(--color-status-done)',
      CANCELLED: 'var(--color-status-cancelled)',
    };
    return colors[status ?? ''] ?? 'var(--color-outline-variant)';
  }

  getInitials(login: string | undefined): string {
    if (!login) {
      return '?';
    }
    return login.charAt(0).toUpperCase();
  }

  setTab(tab: Tab): void {
    this.activeTab.set(tab);
  }

  openCreateTaskModal(): void {
    const ep = this.currentEpic();
    if (!ep) {
      return;
    }
    this.router.navigate(['/project', this.currentProjectKey(), 'task', 'new'], {
      queryParams: {
        epicId: ep.id,
        projectId: ep.project?.id,
      },
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  onSelectTask(task: ITask): void {
    const key = this.currentProjectKey();
    if (key) {
      this.router.navigate(['/project', key, 'task', task.id, 'view']);
    }
  }

  onStatusChange(event: { taskId: number; status: string }): void {
    this.taskService.partialUpdate({ id: event.taskId, status: event.status as any }).subscribe({
      next: () => this.refreshEpic(),
      error: (err: HttpErrorResponse) => {
        const message = err.error?.detail ?? err.message ?? this.translateService.instant('error.general');
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  private refreshEpic(): void {
    const ep = this.currentEpic();
    if (ep?.id) {
      this.epicService.find(ep.id).subscribe({
        next: updated => this.currentEpic.set(updated),
      });
    }
  }
}
