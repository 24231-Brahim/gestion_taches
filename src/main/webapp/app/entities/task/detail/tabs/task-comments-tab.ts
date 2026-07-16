import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal } from '@angular/core';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { IComment } from 'app/entities/comment/comment.model';
import { CommentService } from 'app/entities/comment/service/comment.service';

@Component({
  selector: 'jhi-task-comments-tab',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './task-comments-tab.html',
  imports: [FontAwesomeModule, TranslateModule, TranslateDirective, FormatMediumDatetimePipe],
})
export class TaskCommentsTab {
  readonly taskId = input.required<number>();

  readonly comments = signal<IComment[]>([]);
  readonly isLoading = signal(false);

  private readonly commentService = inject(CommentService);

  constructor() {
    effect(() => {
      const id = this.taskId();
      if (id) {
        this.loadComments(id);
      }
    });
  }

  private loadComments(taskId: number): void {
    this.isLoading.set(true);
    this.commentService.query({ 'taskId.equals': taskId, sort: 'createdAt,desc' }).subscribe({
      next: res => {
        this.comments.set(res.body ?? []);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }
}
