import { ChangeDetectionStrategy, Component, effect, inject, input, signal } from '@angular/core';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { IActionHistory } from 'app/entities/action-history/action-history.model';
import { ActionHistoryService } from 'app/entities/action-history/service/action-history.service';

@Component({
  selector: 'jhi-task-history-tab',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './task-history-tab.html',
  imports: [FontAwesomeModule, TranslateModule, TranslateDirective, FormatMediumDatetimePipe],
})
export class TaskHistoryTab {
  readonly taskId = input.required<number>();

  readonly actionHistories = signal<IActionHistory[]>([]);
  readonly isLoading = signal(false);

  private readonly actionHistoryService = inject(ActionHistoryService);

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
    this.actionHistoryService.query({ 'taskId.equals': taskId, sort: 'createdAt,desc' }).subscribe({
      next: res => {
        this.actionHistories.set(res.body ?? []);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }
}
