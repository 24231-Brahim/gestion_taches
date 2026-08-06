import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { TranslateDirective } from 'app/shared/language';
import { AccountService } from 'app/core/auth/account.service';
import { PRIORITY_COLORS, PRIORITY_ICONS, STATUS_BADGES } from 'app/entities/task/task-helper';
import { TaskKanbanBoard } from 'app/entities/task/kanban/task-kanban-board';
import { TaskDetailPanel } from 'app/entities/task/detail/task-detail-panel';
import { TaskService } from 'app/entities/task/service/task.service';
import { ITask } from 'app/entities/task/task.model';

type ViewMode = 'list' | 'kanban';
const VIEW_MODE_KEY = 'myTasksViewMode';

@Component({
  selector: 'jhi-my-tasks',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './my-tasks.html',
  styles: [
    `
      .my-tasks-page {
        padding: var(--stack-lg, 24px);
      }
      .my-tasks-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 12px;
        flex-wrap: wrap;
        margin-bottom: var(--stack-md, 16px);
      }
      .my-tasks-title {
        font-family: var(--font-display);
        margin: 0;
      }
      .view-mode-tabs {
        display: flex;
        gap: 0;
        border: 1px solid var(--color-outline-variant);
        border-radius: var(--radius-md);
        overflow: hidden;
      }
      .view-mode-tabs .btn {
        border: none;
        border-radius: 0;
        padding: 6px 14px;
        background: transparent;
        color: var(--color-text-muted);
        font-size: 0.85rem;
        font-family: var(--font-inter);
      }
      .view-mode-tabs .btn-active {
        background: var(--color-primary);
        color: var(--color-on-primary-container);
      }
      .my-tasks-empty {
        display: flex;
        flex-direction: column;
        align-items: center;
        text-align: center;
        gap: 8px;
        color: var(--color-muted);
        padding: var(--stack-lg, 48px) 0;
      }
      .my-tasks-empty-icon {
        font-size: 2rem;
        color: var(--color-primary);
        opacity: 0.6;
      }
      .task-row {
        cursor: pointer;
        transition: background-color var(--transition-fast);
      }
      .task-row:hover {
        background: var(--color-surface-container);
      }
      .task-title-cell {
        font-weight: 500;
        max-width: 300px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .assignee-initials-sm {
        width: 24px;
        height: 24px;
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
      .status-badge {
        display: inline-block;
        padding: 2px 10px;
        border-radius: var(--radius-pill, 9999px);
        font-size: 0.75rem;
        font-weight: 600;
        font-family: var(--font-display);
      }
    `,
  ],
  imports: [TranslateModule, TranslateDirective, FontAwesomeModule, TaskKanbanBoard, TaskDetailPanel],
})
export default class MyTasks {
  readonly tasks = signal<ITask[]>([]);
  readonly isLoading = signal(false);
  readonly selectedTask = signal<ITask | null>(null);
  readonly drawerVisible = signal(false);

  readonly viewMode = signal<ViewMode>(this.loadViewMode());
  readonly priorityColors = PRIORITY_COLORS;
  readonly priorityIcons = PRIORITY_ICONS;
  readonly statusBadges = STATUS_BADGES;

  readonly hasTasks = computed(() => this.tasks().length > 0);

  private readonly accountService = inject(AccountService);
  private readonly taskService = inject(TaskService);

  constructor() {
    this.loadTasks();
  }

  onSelectTask(task: ITask): void {
    this.selectedTask.set(task);
    this.drawerVisible.set(true);
  }

  trackId = (item: ITask): number => this.taskService.getTaskIdentifier(item);

  setViewMode(mode: ViewMode): void {
    this.viewMode.set(mode);
    localStorage.setItem(VIEW_MODE_KEY, mode);
  }

  onCloseDrawer(): void {
    this.drawerVisible.set(false);
    this.selectedTask.set(null);
  }

  onKanbanStatusChange(event: { taskId: number; status: string }): void {
    this.tasks.update(list => list.map(t => (t.id === event.taskId ? { ...t, status: event.status as ITask['status'] } : t)));
  }

  private loadViewMode(): ViewMode {
    const stored = localStorage.getItem(VIEW_MODE_KEY);
    return stored === 'kanban' ? 'kanban' : 'list';
  }

  private loadTasks(): void {
    const account = this.accountService.account();
    if (!account) {
      this.tasks.set([]);
      this.isLoading.set(false);
      return;
    }

    this.isLoading.set(true);

    const currentUserId = account.id;
    if (currentUserId == null) {
      this.tasks.set([]);
      this.isLoading.set(false);
      return;
    }

    this.taskService.query({ 'assigneeId.equals': currentUserId, size: 500, sort: 'updatedAt,desc' }).subscribe({
      next: res => {
        this.tasks.set(res.body ?? []);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.tasks.set([]);
      },
    });
  }
}
