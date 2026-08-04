import { ChangeDetectionStrategy, Component, computed, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { TranslateDirective } from 'app/shared/language';
import { ITask } from 'app/entities/task/task.model';
import { ISprint } from '../sprint.model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-sprint-backlog-planning',
  templateUrl: './sprint-backlog-planning.html',
  styles: [
    `
      .backlog-search {
        margin-bottom: 8px;
      }
      .backlog-search-input {
        width: 100%;
        background: var(--color-surface);
        border: 1px solid var(--color-outline-variant);
        border-radius: var(--radius-sm);
        color: var(--color-text);
        font-family: var(--font-inter);
        font-size: 0.8rem;
        padding: 8px 10px;
        box-sizing: border-box;
      }
      .backlog-search-input::placeholder {
        color: var(--color-text-muted);
      }
      .backlog-search-input:focus {
        outline: none;
        border-color: var(--color-primary);
      }
      .planning-card-meta {
        display: flex;
        align-items: center;
        gap: 6px;
        margin-top: 4px;
      }
      .planning-card-sp {
        background: var(--color-primary-container);
        color: var(--color-on-primary-container);
        font-size: 0.65rem;
        font-weight: 600;
        padding: 1px 6px;
        font-family: var(--font-mono);
        border-radius: var(--radius-sm);
      }
    `,
  ],
  imports: [FormsModule, FontAwesomeModule, TranslateDirective, TranslateModule],
})
export class SprintBacklogPlanning {
  readonly sprint = input<ISprint | null>(null);
  readonly allTasks = input<ITask[]>([]);
  readonly sprintTasksInput = input<ITask[] | null>(null);
  readonly canManage = input(false);
  readonly sprintStatus = input<string>('PLANNED');

  readonly assignToSprint = output<{ taskId: number; sprintId: number }>();
  readonly removeFromSprint = output<number>();

  readonly searchQuery = signal('');

  readonly backlogTasks = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    const all = this.allTasks().filter(task => !task.sprint?.id);
    if (!query) {
      return all;
    }
    return all.filter(task => {
      const titleMatch = task.title?.toLowerCase().includes(query) ?? false;
      const descMatch = task.description?.toLowerCase().includes(query) ?? false;
      return titleMatch || descMatch;
    });
  });

  readonly sprintTasks = computed(() => {
    const explicit = this.sprintTasksInput();
    if (explicit) {
      return explicit;
    }
    return this.allTasks().filter(task => task.sprint?.id === this.sprint()?.id);
  });

  readonly isDragDisabled = computed(() => this.sprintStatus() === 'ACTIVE');

  protected draggedTask: ITask | null = null;

  sprintId(): number {
    return this.sprint()?.id ?? 0;
  }

  onDragStart(task: ITask): void {
    if (this.isDragDisabled()) {
      return;
    }
    this.draggedTask = task;
  }

  onDropBacklog(event: DragEvent): void {
    event.preventDefault();
    if (this.draggedTask && !this.isDragDisabled()) {
      this.removeFromSprint.emit(this.draggedTask.id);
      this.draggedTask = null;
    }
  }

  onDropSprint(event: DragEvent): void {
    event.preventDefault();
    if (this.draggedTask && this.sprint()?.id && !this.isDragDisabled()) {
      this.assignToSprint.emit({ taskId: this.draggedTask.id, sprintId: this.sprint()!.id });
      this.draggedTask = null;
    }
  }
}
