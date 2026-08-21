import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, HostListener, inject, input, output } from '@angular/core';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { TaskStatus } from 'app/entities/enumerations/task-status.model';
import { AccountService } from 'app/core/auth/account.service';
import { AlertService } from 'app/core/util/alert.service';
import { PRIORITY_COLORS, PRIORITY_ICONS, STATUS_BADGES } from '../task-helper';
import { ITask } from '../task.model';
import { TaskService } from '../service/task.service';

interface KanbanColumn {
  status: string;
  tasks: ITask[];
}

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-task-kanban-board',
  templateUrl: './task-kanban-board.html',
  styles: [
    `
      .kanban-board {
        display: flex;
        gap: 12px;
        overflow-x: auto;
        padding: 12px 0;
        min-height: 60vh;
      }
      .kanban-column {
        flex: 1;
        min-width: 220px;
        max-width: 300px;
        background: var(--color-surface-container-low);
        border-radius: var(--radius-lg);
        border: 1px solid var(--color-outline-variant);
        transition: border-color 0.2s;
      }
      .kanban-column-drag-over {
        border-color: var(--color-primary);
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
        background: var(--color-surface);
        border: 1px solid var(--color-outline-variant);
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
        color: var(--color-text-muted);
        font-size: 0.7rem;
        margin-left: auto;
        font-family: var(--font-mono);
      }
      .kanban-card-title {
        font-size: 0.85rem;
        color: var(--color-text);
        line-height: 1.3;
        margin-bottom: 8px;
        word-break: break-word;
      }
      .kanban-card-project-tag {
        display: inline-block;
        font-family: var(--font-display);
        font-size: 0.65rem;
        font-weight: 600;
        padding: 2px 8px;
        border-radius: var(--radius-pill, 9999px);
        background: var(--color-tag-blue-bg);
        color: var(--color-tag-blue-fg);
        margin-bottom: 8px;
      }
      .kanban-card-footer {
        display: flex;
        align-items: center;
        gap: 6px;
      }
      .assignee-initials {
        width: 22px;
        height: 22px;
        border-radius: 50%;
        background: var(--color-primary-container);
        color: var(--color-on-primary-container);
        font-size: 0.65rem;
        font-weight: 600;
        display: flex;
        align-items: center;
        justify-content: center;
        font-family: var(--font-mono);
      }
    `,
  ],
  imports: [FontAwesomeModule, TranslateModule],
})
export class TaskKanbanBoard {
  readonly tasks = input<ITask[]>([]);
  readonly showProjectTag = input<boolean>(false);
  readonly selectTask = output<ITask>();
  readonly taskStatusChanged = output<{ taskId: number; status: string }>();

  readonly priorityColors = PRIORITY_COLORS;
  readonly priorityIcons = PRIORITY_ICONS;
  readonly statusBadges = STATUS_BADGES;

  dragTaskId: number | null = null;
  dragOverStatus: string | null = null;

  readonly columns: KanbanColumn[] = Object.keys(TaskStatus).map(status => ({
    status,
    tasks: [],
  }));

  protected readonly taskService = inject(TaskService);
  protected readonly alertService = inject(AlertService);
  protected readonly translateService = inject(TranslateService);
  protected readonly accountService = inject(AccountService);

  getColumns(): KanbanColumn[] {
    return this.columns.map(col => ({
      ...col,
      tasks: this.tasks().filter(i => i.status === col.status),
    }));
  }

  // A card may only be dragged (status changed) by ADMIN/PROJET_MANAGER or the task's own
  // assignee — matches TaskService.checkTaskUpdatePermission(). Other members still see the card
  // (read-only, for context) but it renders non-draggable.
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
    this.taskService.partialUpdate({ id: task.id, status: targetStatus as keyof typeof TaskStatus }).subscribe({
      next: () => {
        this.taskStatusChanged.emit({ taskId: task.id!, status: targetStatus });
        this.dragTaskId = null;
      },
      error: (err: HttpErrorResponse) => {
        this.dragTaskId = null;
        const message = err.error?.detail ?? err.message ?? this.translateService.instant('error.general');
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  @HostListener('document:dragend')
  onDragEnd(): void {
    this.dragTaskId = null;
    this.dragOverStatus = null;
  }

  getInitials(login: string | undefined): string {
    if (!login) {
      return '?';
    }
    return login.charAt(0).toUpperCase();
  }

  onTaskClick(task: ITask): void {
    this.selectTask.emit(task);
  }
}
