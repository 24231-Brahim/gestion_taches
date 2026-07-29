import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, HostListener, inject, input, output } from '@angular/core';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { TaskStatus } from 'app/entities/enumerations/task-status.model';
import { AlertService } from 'app/core/util/alert.service';
import { TranslateDirective } from 'app/shared/language';
import { ISSUE_TYPE_COLORS, ISSUE_TYPE_ICONS, PRIORITY_COLORS, PRIORITY_ICONS, STATUS_BADGES } from '../task-helper';
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
        background: var(--color-surface-container, #1b2025);
        border-radius: var(--radius-lg);
        border: 1px solid var(--color-outline-variant, #2a3038);
        transition: border-color 0.2s;
      }
      .kanban-column-drag-over {
        border-color: var(--color-primary, #97cbff);
      }
      .kanban-column-header {
        padding: 12px;
        border-top: 2px solid;
        border-radius: var(--radius-lg) var(--radius-lg) 0 0;
        display: flex;
        align-items: center;
        justify-content: space-between;
      }
      .kanban-column-title {
        font-family: var(--font-inter);
        font-size: 0.75rem;
        text-transform: uppercase;
        letter-spacing: 0;
        color: var(--color-text, #dfe3ea);
        font-weight: 600;
      }
      .kanban-column-count {
        background: var(--color-surface-container-high, #262d36);
        color: var(--color-text-muted, #6a8fac);
        border-radius: 9999px;
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
        background: var(--color-surface-container, #1b2025);
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
        border-radius: 50%;
        background: var(--color-primary-container, #25a7fd);
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
  imports: [FontAwesomeModule, TranslateDirective, TranslateModule],
})
export class TaskKanbanBoard {
  readonly tasks = input<ITask[]>([]);
  readonly selectTask = output<ITask>();
  readonly taskStatusChanged = output<{ taskId: number; status: string }>();

  readonly typeColors = ISSUE_TYPE_COLORS;
  readonly typeIcons = ISSUE_TYPE_ICONS;
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

  getColumns(): KanbanColumn[] {
    return this.columns.map(col => ({
      ...col,
      tasks: this.tasks().filter(i => i.status === col.status),
    }));
  }

  onDragStart(task: ITask): void {
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
    if (!task || task.status === targetStatus) {
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
