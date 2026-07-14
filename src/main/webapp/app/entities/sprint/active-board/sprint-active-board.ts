import { ChangeDetectionStrategy, Component, HostListener, input, output } from '@angular/core';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { TaskStatus } from 'app/entities/enumerations/task-status.model';
import { SprintStatus } from 'app/entities/enumerations/sprint-status.model';
import { TranslateDirective } from 'app/shared/language';
import { FormatMediumDatePipe } from 'app/shared/date';
import { ISSUE_TYPE_COLORS, ISSUE_TYPE_ICONS, PRIORITY_COLORS, PRIORITY_ICONS, STATUS_BADGES } from 'app/entities/task/task-helper';
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
        border: 3px solid var(--color-outline-variant, #2a3038);
        padding: 16px;
        box-shadow: 4px 4px 0 var(--color-outline-variant, #2a3038);
      }
      .sprint-info {
        display: flex;
        flex-direction: column;
        gap: 4px;
      }
      .sprint-name {
        font-family: 'Audiowide', monospace;
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
        font-family: 'JetBrains Mono', monospace;
        font-size: 0.65rem;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        padding: 2px 8px;
        border: 2px solid;
        background: var(--color-surface, #0f1419);
        color: var(--color-text, #dfe3ea);
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
        background: var(--color-surface-container, #1b2025);
        border: 3px solid var(--color-outline-variant, #2a3038);
        transition: border-color 0.2s;
      }
      .kanban-column-drag-over {
        border-color: var(--color-primary, #97cbff);
      }
      .kanban-column-header {
        padding: 12px;
        border-top: 3px solid;
        display: flex;
        align-items: center;
        justify-content: space-between;
      }
      .kanban-column-title {
        font-family: 'Audiowide', monospace;
        font-size: 0.75rem;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        color: var(--color-text, #dfe3ea);
      }
      .kanban-column-count {
        background: var(--color-surface-container-high, #262d36);
        color: var(--color-text-muted, #6a8fac);
        padding: 1px 8px;
        font-size: 0.75rem;
      }
      .kanban-column-body {
        padding: 8px;
        display: flex;
        flex-direction: column;
        gap: 8px;
        min-height: 100px;
      }
      .kanban-card {
        background: var(--color-surface, #0f1419);
        border: 2px solid var(--color-outline-variant, #2a3038);
        padding: 10px;
        cursor: grab;
        transition: all 0.15s;
        box-shadow: 3px 3px 0 var(--color-outline-variant, #2a3038);
      }
      .kanban-card:hover {
        transform: translate(-1px, -1px);
        box-shadow: 4px 4px 0 var(--color-primary, #97cbff);
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
        color: #000;
        font-size: 0.65rem;
        font-weight: 700;
        display: flex;
        align-items: center;
        justify-content: center;
        font-family: 'JetBrains Mono', monospace;
      }
      .kanban-empty {
        padding: 20px 8px;
        text-align: center;
        color: var(--color-text-muted, #6a8fac);
        font-size: 0.8rem;
      }
    `,
  ],
  imports: [FontAwesomeModule, TranslateDirective, TranslateModule, FormatMediumDatePipe],
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

  readonly typeIcons = ISSUE_TYPE_ICONS;
  readonly typeColors = ISSUE_TYPE_COLORS;
  readonly priorityIcons = PRIORITY_ICONS;
  readonly priorityColors = PRIORITY_COLORS;

  dragTaskId: number | null = null;
  dragOverStatus: string | null = null;

  readonly columns: KanbanColumn[] = Object.keys(TaskStatus).map(status => ({
    status,
    tasks: [],
  }));

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
    this.statusChange.emit({ taskId: this.dragTaskId, status: targetStatus });
    this.dragTaskId = null;
  }

  @HostListener('document:dragend')
  onDragEnd(): void {
    this.dragTaskId = null;
    this.dragOverStatus = null;
  }

  getTypeIcon(type: string | null | undefined): string {
    return ISSUE_TYPE_ICONS[type ?? 'STORY'] ?? 'tag';
  }

  getTypeColor(type: string | null | undefined): string {
    return ISSUE_TYPE_COLORS[type ?? 'STORY'] ?? 'var(--color-outline-variant)';
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
