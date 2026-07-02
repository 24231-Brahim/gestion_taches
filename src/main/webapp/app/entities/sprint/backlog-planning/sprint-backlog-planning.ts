import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { TranslateDirective } from 'app/shared/language';
import { IIssue } from 'app/entities/issue/issue.model';
import { ISprint } from '../sprint.model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-sprint-backlog-planning',
  templateUrl: './sprint-backlog-planning.html',
  imports: [FontAwesomeModule, TranslateDirective, TranslateModule],
})
export class SprintBacklogPlanning {
  readonly sprint = input<ISprint | null>(null);
  readonly allIssues = input<IIssue[]>([]);

  readonly assignToSprint = output<{ issueId: number; sprintId: number }>();
  readonly removeFromSprint = output<number>();

  readonly backlogIssues = computed(() => this.allIssues().filter(issue => !issue.sprint?.id));

  readonly sprintIssues = computed(() => this.allIssues().filter(issue => issue.sprint?.id === this.sprint()?.id));

  protected draggedIssue: IIssue | null = null;

  sprintId(): number {
    return this.sprint()?.id ?? 0;
  }

  onDragStart(issue: IIssue): void {
    this.draggedIssue = issue;
  }

  onDropBacklog(event: DragEvent): void {
    event.preventDefault();
    if (this.draggedIssue) {
      this.removeFromSprint.emit(this.draggedIssue.id);
      this.draggedIssue = null;
    }
  }

  onDropSprint(event: DragEvent): void {
    event.preventDefault();
    if (this.draggedIssue && this.sprint()?.id) {
      this.assignToSprint.emit({ issueId: this.draggedIssue.id, sprintId: this.sprint()!.id });
      this.draggedIssue = null;
    }
  }
}
