import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { AlertService } from 'app/core/util/alert.service';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { IComment } from 'app/entities/comment/comment.model';
import { CommentService } from 'app/entities/comment/service/comment.service';

@Component({
  selector: 'jhi-task-comments-tab',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './task-comments-tab.html',
  styles: [
    `
      .tab-section {
        padding: 4px 0;
      }
      .tab-section-title {
        font-family: var(--font-inter);
        font-size: 1rem;
        font-weight: 600;
        color: var(--color-text, #dfe3ea);
        margin: 0 0 20px;
        display: flex;
        align-items: center;
      }
      .comments-list {
        display: flex;
        flex-direction: column;
        gap: 16px;
        margin-bottom: 24px;
      }
      .comment-item {
        background: var(--color-surface-container, #1b2025);
        border: 1px solid var(--color-outline-variant, #2a3038);
        border-radius: 12px;
        padding: 16px 20px;
        transition: border-color var(--transition-fast);
      }
      .comment-item:hover {
        border-color: var(--color-outline, #404952);
      }
      .comment-header {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 10px;
      }
      .comment-avatar {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        background: var(--color-primary-container, #0099fe);
        color: var(--color-on-primary-container);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 0.75rem;
        font-weight: 700;
        font-family: var(--font-mono);
        flex-shrink: 0;
      }
      .comment-meta {
        display: flex;
        flex-direction: column;
        flex: 1;
        min-width: 0;
      }
      .comment-author {
        font-family: var(--font-inter);
        font-size: 0.85rem;
        font-weight: 600;
        color: var(--color-primary, #97cbff);
        line-height: 1.3;
      }
      .comment-date {
        font-family: var(--font-inter);
        font-size: 0.7rem;
        color: var(--color-text-muted, #6a8fac);
      }
      .comment-actions {
        margin-left: auto;
        flex-shrink: 0;
      }
      .comment-body {
        font-family: var(--font-inter);
        font-size: 0.88rem;
        color: var(--color-text, #dfe3ea);
        white-space: pre-wrap;
        line-height: 1.6;
        padding-left: 44px;
      }
      .comment-form {
        background: var(--color-surface-container, #1b2025);
        border: 1px solid var(--color-outline-variant, #2a3038);
        border-radius: 12px;
        padding: 16px 20px;
      }
      .comment-form textarea {
        resize: vertical;
        min-height: 80px;
      }
      .empty-state {
        padding: 40px 20px;
        text-align: center;
        color: var(--color-text-muted, #6a8fac);
        font-family: var(--font-inter);
        font-size: 0.88rem;
      }
      .empty-state fa-icon {
        font-size: 2rem;
        margin-bottom: 12px;
        opacity: 0.5;
      }
      .loading-state {
        text-align: center;
        padding: 40px 20px;
        color: var(--color-text-muted, #6a8fac);
      }
    `,
  ],
  imports: [FormsModule, FontAwesomeModule, TranslateModule, TranslateDirective, FormatMediumDatetimePipe],
})
export class TaskCommentsTab {
  readonly taskId = input.required<number>();

  readonly comments = signal<IComment[]>([]);
  readonly isLoading = signal(false);
  readonly newCommentText = signal('');

  readonly sortedComments = computed(() => {
    return [...this.comments()].sort((a, b) => {
      const aTime = a.createdAt?.valueOf() ?? 0;
      const bTime = b.createdAt?.valueOf() ?? 0;
      return bTime - aTime;
    });
  });

  private readonly http = inject(HttpClient);
  private readonly appConfig = inject(ApplicationConfigService);
  private readonly commentService = inject(CommentService);
  private readonly alertService = inject(AlertService);
  private readonly accountService = inject(AccountService);

  constructor() {
    effect(() => {
      const id = this.taskId();
      if (id) {
        this.loadComments(id);
      }
    });
  }

  addComment(): void {
    const content = this.newCommentText().trim();
    if (!content) {
      return;
    }
    this.commentService.create({ id: null, content, task: { id: this.taskId() } }).subscribe({
      next: () => {
        this.newCommentText.set('');
        this.loadComments(this.taskId());
      },
      error: (err: HttpErrorResponse) => {
        const message = err.error?.detail ?? err.message ?? 'error.general';
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  deleteComment(id: number): void {
    this.commentService.delete(id).subscribe({
      next: () => this.loadComments(this.taskId()),
      error: (err: HttpErrorResponse) => {
        const message = err.error?.detail ?? err.message ?? 'error.general';
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  getInitials(login: string | null | undefined): string {
    if (!login) return '?';
    return login.substring(0, 2).toUpperCase();
  }

  isAuthor(comment: IComment): boolean {
    return this.accountService.account()?.login === comment.author?.login;
  }

  canModify(comment: IComment): boolean {
    return this.accountService.hasAnyAuthority(['ROLE_ADMIN', 'ROLE_PROJET_MANAGER', 'ROLE_DEVELOPER']) || this.isAuthor(comment);
  }

  private loadComments(taskId: number): void {
    this.isLoading.set(true);
    this.http.get<IComment[]>(this.appConfig.getEndpointFor(`api/comments/by-task/${taskId}`)).subscribe({
      next: comments => {
        this.comments.set(comments);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }
}
