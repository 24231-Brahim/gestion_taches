import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { TranslateDirective } from 'app/shared/language';
import { ITask } from 'app/entities/task/task.model';
import { ISprint } from '../sprint.model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-sprint-backlog-planning',
  templateUrl: './sprint-backlog-planning.html',
  imports: [FontAwesomeModule, TranslateDirective, TranslateModule],
})
export class SprintBacklogPlanning {
  readonly sprint = input<ISprint | null>(null);
  readonly allTasks = input<ITask[]>([]);

  readonly assignToSprint = output<{ taskId: number; sprintId: number }>();
  readonly removeFromSprint = output<number>();

  readonly backlogTasks = computed(() => this.allTasks().filter(task => !task.sprint?.id));

  readonly sprintTasks = computed(() => this.allTasks().filter(task => task.sprint?.id === this.sprint()?.id));

  protected draggedTask: ITask | null = null;

  sprintId(): number {
    return this.sprint()?.id ?? 0;
  }

  onDragStart(task: ITask): void {
    this.draggedTask = task;
  }

  onDropBacklog(event: DragEvent): void {
    event.preventDefault();
    if (this.draggedTask) {
      this.removeFromSprint.emit(this.draggedTask.id);
      this.draggedTask = null;
    }
  }

  onDropSprint(event: DragEvent): void {
    event.preventDefault();
    if (this.draggedTask && this.sprint()?.id) {
      this.assignToSprint.emit({ taskId: this.draggedTask.id, sprintId: this.sprint()!.id });
      this.draggedTask = null;
    }
  }
}
