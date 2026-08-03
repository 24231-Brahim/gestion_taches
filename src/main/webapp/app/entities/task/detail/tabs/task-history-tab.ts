import { ChangeDetectionStrategy, Component, effect, inject, input, signal } from '@angular/core';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { ITaskHistory } from 'app/entities/task-history/task-history.model';
import { TaskHistoryService } from 'app/entities/task-history/service/task-history.service';

@Component({
  selector: 'jhi-task-history-tab',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './task-history-tab.html',
  imports: [FontAwesomeModule, TranslateModule, TranslateDirective, FormatMediumDatetimePipe],
})
export class TaskHistoryTab {
  readonly taskId = input.required<number>();

  readonly taskHistories = signal<ITaskHistory[]>([]);
  readonly isLoading = signal(false);

  private readonly taskHistoryService = inject(TaskHistoryService);

  constructor() {
    effect(() => {
      const id = this.taskId();
      if (id) {
        this.loadHistory(id);
      }
    });
  }

  private loadHistory(taskId: number): void {
    this.isLoading.set(true);
    this.taskHistoryService.findByTask(taskId).subscribe({
      next: res => {
        this.taskHistories.set(res.body ?? []);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }
}
