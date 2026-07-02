import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';
import { AlertService } from 'app/core/util/alert.service';
import { IComment } from 'app/entities/comment/comment.model';
import { CommentService } from 'app/entities/comment/service/comment.service';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-issue-comment-list',
  templateUrl: './issue-comment-list.html',
  styles: [
    `
      .issue-comments {
        display: flex;
        flex-direction: column;
        gap: 12px;
      }
      .issue-section-title {
        font-family: 'JetBrains Mono', monospace;
        font-size: 0.8rem;
        text-transform: uppercase;
        letter-spacing: 0.08em;
        color: var(--color-text-muted, #6a8fac);
        margin: 0 0 4px;
      }
      .comment-item {
        background: var(--color-surface-container, #1b2025);
        border: 2px solid var(--color-outline-variant, #2a3038);
        padding: 10px 12px;
      }
      .comment-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 6px;
      }
      .comment-author {
        font-family: 'JetBrains Mono', monospace;
        font-size: 0.8rem;
        font-weight: 700;
        color: var(--color-primary, #97cbff);
      }
      .comment-date {
        font-family: 'JetBrains Mono', monospace;
        font-size: 0.7rem;
        color: var(--color-text-muted, #6a8fac);
      }
      .comment-delete {
        margin-left: auto;
      }
      .comment-content {
        font-family: 'JetBrains Mono', monospace;
        font-size: 0.85rem;
        color: var(--color-text, #dfe3ea);
        white-space: pre-wrap;
        line-height: 1.5;
      }
      .comment-form {
        margin-top: 8px;
        display: flex;
        flex-direction: column;
      }
      .text-muted {
        color: var(--color-text-muted, #6a8fac);
        font-family: 'JetBrains Mono', monospace;
        font-size: 0.8rem;
      }
    `,
  ],
  imports: [FormsModule, FontAwesomeModule, TranslateDirective, TranslateModule, FormatMediumDatetimePipe],
})
export class IssueCommentList implements OnInit {
  readonly issueId = input.required<number>();

  readonly comments = signal<IComment[]>([]);
  readonly newCommentText = signal('');

  protected readonly http = inject(HttpClient);
  protected readonly commentService = inject(CommentService);
  protected readonly alertService = inject(AlertService);
  protected readonly accountService = inject(AccountService);

  ngOnInit(): void {
    this.loadComments();
  }

  loadComments(): void {
    this.http.get<IComment[]>(`/api/comments/by-issue/${this.issueId()}`).subscribe({
      next: comments => this.comments.set(comments),
      error: () => this.alertService.addAlert({ type: 'danger', translationKey: 'error.general' }),
    });
  }

  addComment(): void {
    const content = this.newCommentText().trim();
    if (!content) {
      return;
    }
    this.commentService.create({ id: null, content, issue: { id: this.issueId() } }).subscribe({
      next: () => {
        this.newCommentText.set('');
        this.loadComments();
      },
      error: (err: HttpErrorResponse) => {
        const message = err.error?.detail ?? err.message ?? 'error.general';
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  deleteComment(id: number): void {
    this.commentService.delete(id).subscribe({
      next: () => this.loadComments(),
      error: (err: HttpErrorResponse) => {
        const message = err.error?.detail ?? err.message ?? 'error.general';
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  isAuthor(comment: IComment): boolean {
    return this.accountService.account()?.login === comment.author?.login;
  }

  canModify(comment: IComment): boolean {
    return this.accountService.hasAnyAuthority(['ROLE_ADMIN', 'ROLE_PROJET_MANAGER', 'ROLE_DEVELOPER']) || this.isAuthor(comment);
  }
}
