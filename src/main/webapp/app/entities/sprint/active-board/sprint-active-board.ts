import { ChangeDetectionStrategy, Component, HostListener, computed, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';
import { TaskStatus } from 'app/entities/enumerations/task-status.model';
import { TranslateDirective } from 'app/shared/language';
import { FormatMediumDatePipe } from 'app/shared/date';
import { PRIORITY_COLORS, PRIORITY_ICONS, STATUS_BADGES } from 'app/entities/task/task-helper';
import { ITask } from 'app/entities/task/task.model';
import { ISprint } from '../sprint.model';

interface KanbanColumn {
  status: string;
  tasks: ITask[];
}

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-sprint-active-board',
  standalone: true,
  templateUrl: './sprint-active-board.html',
  styles: [
    `
      .sprint-board {
        display: flex;
        flex-direction: column;
        gap: 16px;
      }
      .sprint-board-header {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: 16px;
        background: var(--color-surface-container, #1b2025);
        border: 1px solid var(--color-outline-variant, #2a3038);
        border-radius: var(--radius-lg);
        padding: 16px;
        box-shadow: var(--shadow-sm);
      }
      .sprint-info {
        display: flex;
        flex-direction: column;
        gap: 4px;
      }
      .sprint-name {
        font-family: var(--font-inter);
        font-size: 1.1rem;
        margin: 0;
        color: var(--color-text, #dfe3ea);
      }
      .sprint-goal {
        margin: 0;
        font-size: 0.85rem;
        color: var(--color-text-muted, #6a8fac);
        font-style: italic;
      }
      .sprint-dates {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 0.8rem;
        color: var(--color-text-muted, #6a8fac);
      }
      .sprint-status-badge {
        font-family: var(--font-inter);
        font-size: 0.65rem;
        text-transform: none;
        padding: 2px 8px;
        border: 1px solid var(--color-outline-variant);
        border-radius: 9999px;
        background: var(--color-surface, #0f1419);
        color: var(--color-text, #dfe3ea);
        font-weight: 600;
      }
      .sprint-actions {
        display: flex;
        gap: 8px;
        flex-shrink: 0;
      }
      .kanban-board {
        display: flex;
        gap: 12px;
        overflow-x: auto;
        padding: 12px 0;
        min-height: 60vh;
      }
      .kanban-column {
        flex: 1;
        min-width: 200px;
        max-width: 300px;
        background: var(--color-surface-container-low, #1b2025);
        border: 1px solid var(--color-outline-variant, #2a3038);
        border-radius: var(--radius-lg);
        transition: border-color 0.2s;
      }
      .kanban-column-drag-over {
        border-color: var(--color-primary, #97cbff);
      }
      .kanban-column-header {
        padding: 12px;
        border-radius: var(--radius-lg) var(--radius-lg) 0 0;
        display: flex;
        align-items: center;
        justify-content: space-between;
      }
      .kanban-column-title {
        font-family: var(--font-display);
        font-size: 0.75rem;
        text-transform: none;
        letter-spacing: 0;
        font-weight: 700;
      }
      .kanban-column-count {
        background: rgb(0 0 0 / 8%);
        color: inherit;
        border-radius: var(--radius-pill, 9999px);
        padding: 1px 8px;
        font-size: 0.75rem;
        font-family: var(--font-inter);
      }
      .kanban-column-body {
        padding: 8px;
        display: flex;
        flex-direction: column;
        gap: 8px;
        min-height: 100px;
        border-radius: 0 0 var(--radius-lg) var(--radius-lg);
      }
      .kanban-card {
        background: var(--color-surface, #1b2025);
        border: 1px solid var(--color-outline-variant, #2a3038);
        border-radius: var(--radius-lg);
        padding: 10px;
        cursor: grab;
        transition:
          background-color var(--transition-fast),
          box-shadow var(--transition-fast);
        box-shadow: var(--shadow-sm);
      }
      .kanban-card:hover {
        transform: translateY(-2px);
        box-shadow: var(--shadow-md);
      }
      .kanban-card-dragging {
        opacity: 0.5;
      }
      .kanban-card-not-draggable {
        cursor: default;
        opacity: 0.75;
      }
      .kanban-card-not-draggable:hover {
        transform: none;
        box-shadow: var(--shadow-sm);
      }
      .kanban-card-top {
        display: flex;
        align-items: center;
        gap: 6px;
        margin-bottom: 6px;
        font-size: 0.8rem;
      }
      .kanban-card-id {
        color: var(--color-text-muted, #6a8fac);
        font-size: 0.7rem;
        margin-left: auto;
        font-family: var(--font-mono);
      }
      .kanban-card-sp {
        background: var(--color-primary-container, #25a7fd);
        color: #000;
        font-size: 0.65rem;
        font-weight: 600;
        padding: 1px 6px;
        font-family: var(--font-mono);
        border-radius: var(--radius-sm);
        margin-left: 4px;
      }
      .kanban-card-title {
        font-size: 0.85rem;
        color: var(--color-text, #dfe3ea);
        line-height: 1.3;
        margin-bottom: 8px;
        word-break: break-word;
      }
      .kanban-card-footer {
        display: flex;
        align-items: center;
        gap: 6px;
      }
      .assignee-initials {
        width: 22px;
        height: 22px;
        background: var(--color-primary-container, #25a7fd);
        color: var(--color-on-primary-container, #fff);
        font-size: 0.65rem;
        font-weight: 600;
        display: flex;
        align-items: center;
        justify-content: center;
        font-family: var(--font-mono);
        border-radius: 50%;
      }
      .kanban-empty {
        padding: 20px 8px;
        text-align: center;
        color: var(--color-muted, #6a8fac);
        font-family: var(--font-inter);
        font-size: 0.8rem;
      }
      .filter-bar {
        display: flex;
        align-items: flex-end;
        gap: 16px;
        padding: 12px 16px;
        background: var(--color-surface-container, #1b2025);
        border: 1px solid var(--color-outline-variant, #2a3038);
        border-radius: var(--radius-lg);
        box-shadow: var(--shadow-sm);
        flex-wrap: wrap;
      }
      .filter-group {
        display: flex;
        flex-direction: column;
        gap: 4px;
      }
      .filter-label {
        font-family: var(--font-inter);
        font-size: 0.7rem;
        text-transform: none;
        color: var(--color-text-muted, #6a8fac);
      }
      .filter-select {
        background: var(--color-surface, #0f1419);
        border: 1px solid var(--color-outline-variant, #2a3038);
        border-radius: var(--radius-sm);
        color: var(--color-text, #dfe3ea);
        font-family: var(--font-inter);
        font-size: 0.8rem;
        padding: 6px 10px;
        min-width: 150px;
      }
      .filter-reset-btn {
        background: transparent;
        border: 1px solid var(--color-outline-variant, #2a3038);
        border-radius: var(--radius-sm);
        color: var(--color-text-muted, #6a8fac);
        font-family: var(--font-inter);
        font-size: 0.75rem;
        padding: 6px 12px;
        cursor: pointer;
        display: flex;
        align-items: center;
        gap: 4px;
        transition:
          background-color var(--transition-fast),
          box-shadow var(--transition-fast);
      }
      .filter-reset-btn:hover {
        color: var(--color-text, #dfe3ea);
        border-color: var(--color-primary, #97cbff);
      }
    `,
  ],
  imports: [FormsModule, FontAwesomeModule, TranslateDirective, TranslateModule, FormatMediumDatePipe],
})
export class SprintActiveBoard {
  readonly sprint = input<ISprint | null>(null);
  readonly tasks = input<ITask[]>([]);
  readonly canManage = input(false);

  readonly selectTask = output<ITask>();
  readonly statusChange = output<{ taskId: number; status: string }>();
  readonly startSprint = output<void>();
  readonly completeSprint = output<void>();
  readonly reopenSprint = output<void>();

  readonly priorityIcons = PRIORITY_ICONS;
  readonly priorityColors = PRIORITY_COLORS;

  readonly filterAssignee = signal<string>('');
  readonly filterPriority = signal<string>('');

  readonly priorityValues = Object.keys(PRIORITY_ICONS);

  readonly uniqueAssignees = computed(() => {
    const logins = new Set<string>();
    for (const task of this.tasks()) {
      if (task.assignee?.login) {
        logins.add(task.assignee.login);
      }
    }
    return Array.from(logins).sort();
  });

  readonly filteredTasks = computed(() => {
    let result = this.tasks();
    const assignee = this.filterAssignee();
    const priority = this.filterPriority();
    if (assignee) {
      result = result.filter(t => t.assignee?.login === assignee);
    }
    if (priority) {
      result = result.filter(t => t.priority === priority);
    }
    return result;
  });

  readonly hasActiveFilters = computed(() => {
    return !!(this.filterAssignee() || this.filterPriority());
  });

  resetFilters(): void {
    this.filterAssignee.set('');
    this.filterPriority.set('');
  }

  dragTaskId: number | null = null;
  dragOverStatus: string | null = null;

  readonly columns: KanbanColumn[] = Object.keys(TaskStatus).map(status => ({
    status,
    tasks: [],
  }));

  getColumns(): KanbanColumn[] {
    const filtered = this.filteredTasks();
    return this.columns.map(col => ({
      ...col,
      tasks: filtered.filter(i => i.status === col.status),
    }));
  }

  protected readonly accountService = inject(AccountService);

  // Matches TaskService.checkTaskUpdatePermission(): only ADMIN/PROJET_MANAGER or the task's own
  // assignee may change its status. Other members still see the card (read-only) but can't drag it.
  canDrag(task: ITask): boolean {
    const account = this.accountService.account();
    if (!account) {
      return false;
    }
    if (account.authorities?.includes('ROLE_ADMIN') || account.authorities?.includes('ROLE_PROJET_MANAGER')) {
      return true;
    }
    return account.login === task.assignee?.login;
  }

  onDragStart(task: ITask, event: DragEvent): void {
    if (!this.canDrag(task)) {
      event.preventDefault();
      return;
    }
    this.dragTaskId = task.id;
  }

  onDragOver(event: DragEvent, status: string): void {
    event.preventDefault();
    this.dragOverStatus = status;
  }

  onDragLeave(): void {
    this.dragOverStatus = null;
  }

  onDrop(event: DragEvent, targetStatus: string): void {
    event.preventDefault();
    this.dragOverStatus = null;
    if (this.dragTaskId === null) {
      return;
    }
    const task = this.tasks().find(i => i.id === this.dragTaskId);
    if (!task || task.status === targetStatus || !this.canDrag(task)) {
      this.dragTaskId = null;
      return;
    }
    this.statusChange.emit({ taskId: this.dragTaskId, status: targetStatus });
    this.dragTaskId = null;
  }

  @HostListener('document:dragend')
  onDragEnd(): void {
    this.dragTaskId = null;
    this.dragOverStatus = null;
  }

  getPriorityIcon(priority: string | null | undefined): string {
    return PRIORITY_ICONS[priority ?? 'MEDIUM'] ?? 'flag';
  }

  getPriorityColor(priority: string | null | undefined): string {
    return PRIORITY_COLORS[priority ?? 'MEDIUM'] ?? 'var(--color-outline-variant)';
  }

  getColumnColor(status: string): string {
    return STATUS_BADGES[status]?.color ?? 'var(--color-outline-variant)';
  }

  getColumnBg(status: string): string {
    return STATUS_BADGES[status]?.bg ?? 'var(--color-surface-container)';
  }

  statusBorder(status: string | null | undefined): string {
    const borders: Record<string, string> = {
      PLANNED: 'var(--color-status-backlog, #9e9e9e)',
      ACTIVE: 'var(--color-status-in-progress, #ff9800)',
      COMPLETED: 'var(--color-status-done, #4caf50)',
      CANCELLED: 'var(--color-status-cancelled, #f44336)',
    };
    return borders[status ?? 'PLANNED'] ?? 'var(--color-outline-variant)';
  }

  getInitials(login: string | undefined): string {
    if (!login) {
      return '?';
    }
    return login.charAt(0).toUpperCase();
  }
}
