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
        background: var(--color-surface, #0f1419);
        border: 2px solid var(--color-outline-variant, #2a3038);
        color: var(--color-text, #dfe3ea);
        font-family: 'JetBrains Mono', monospace;
        font-size: 0.8rem;
        padding: 8px 10px;
        box-sizing: border-box;
      }
      .backlog-search-input::placeholder {
        color: var(--color-text-muted, #6a8fac);
      }
      .backlog-search-input:focus {
        outline: none;
        border-color: var(--color-primary, #97cbff);
      }
      .planning-card-meta {
        display: flex;
        align-items: center;
        gap: 6px;
        margin-top: 4px;
      }
      .planning-card-sp {
        background: var(--color-primary-container, #25a7fd);
        color: #000;
        font-size: 0.65rem;
        font-weight: 700;
        padding: 1px 6px;
        font-family: 'JetBrains Mono', monospace;
      }
    `,
  ],
  imports: [FormsModule, FontAwesomeModule, TranslateDirective, TranslateModule],
})
export class SprintBacklogPlanning {
  readonly sprint = input<ISprint | null>(null);
  readonly allTasks = input<ITask[]>([]);
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

  readonly sprintTasks = computed(() => this.allTasks().filter(task => task.sprint?.id === this.sprint()?.id));

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
