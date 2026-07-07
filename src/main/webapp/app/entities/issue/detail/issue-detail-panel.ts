import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, effect, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { AlertService } from 'app/core/util/alert.service';
import { IssueStatus } from 'app/entities/enumerations/issue-status.model';
import { IssueType } from 'app/entities/enumerations/issue-type.model';
import { Priority } from 'app/entities/enumerations/priority.model';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { ISSUE_TYPE_COLORS, ISSUE_TYPE_ICONS, PRIORITY_COLORS, PRIORITY_ICONS, STATUS_BADGES } from '../issue-helper';
import { IIssue } from '../issue.model';
import { IssueService } from '../service/issue.service';
import { IssueCommentList } from '../comments/issue-comment-list';
import { IssueAttachmentList } from '../attachments/issue-attachment-list';
import { IssueActivityFeed } from '../activity/issue-activity-feed';
import { IUser } from 'app/entities/user/user.model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-issue-detail-panel',
  templateUrl: './issue-detail-panel.html',
  styles: [
    `
      .issue-drawer-overlay {
        position: fixed;
        inset: 0;
        background: rgba(0, 0, 0, 0.5);
        z-index: 1040;
      }
      .issue-drawer {
        position: fixed;
        top: 0;
        right: 0;
        width: min(680px, 100vw);
        height: 100vh;
        background: var(--color-surface, #0f1419);
        border-left: 3px solid var(--color-outline-variant, #2a3038);
        z-index: 1050;
        display: flex;
        flex-direction: column;
        box-shadow: -4px 0 0 var(--color-primary, #97cbff);
      }
      .issue-drawer-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 16px 20px;
        border-bottom: 2px solid var(--color-outline-variant, #2a3038);
      }
      .issue-drawer-body {
        flex: 1;
        overflow-y: auto;
        display: flex;
        gap: 20px;
        padding: 20px;
      }
      .issue-drawer-main {
        flex: 1;
        min-width: 0;
      }
      .issue-drawer-sidebar {
        width: 220px;
        flex-shrink: 0;
      }
      .issue-drawer-title {
        font-family: 'Audiowide', monospace;
        font-size: 1.1rem;
        color: var(--color-text, #dfe3ea);
        margin-bottom: 20px;
        word-break: break-word;
      }
      .issue-section {
        margin-bottom: 20px;
      }
      .issue-section-label {
        display: block;
        font-size: 0.7rem;
        text-transform: uppercase;
        letter-spacing: 0.08em;
        color: var(--color-text-muted, #6a8fac);
        margin-bottom: 4px;
        font-family: 'JetBrains Mono', monospace;
      }
      .issue-description {
        color: var(--color-text, #dfe3ea);
        font-size: 0.9rem;
        line-height: 1.5;
        white-space: pre-wrap;
      }
      .issue-comment-box {
        background: var(--color-surface-container, #1b2025);
        border: 2px solid var(--color-outline-variant, #2a3038);
        border-radius: 6px;
        padding: 12px;
        min-height: 40px;
      }
      .issue-field-group {
        margin-bottom: 16px;
      }
      .issue-field-label {
        display: block;
        font-size: 0.65rem;
        text-transform: uppercase;
        letter-spacing: 0.08em;
        color: var(--color-text-muted, #6a8fac);
        margin-bottom: 4px;
        font-family: 'JetBrains Mono', monospace;
      }
      .issue-assignee {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 0.9rem;
      }
      .issue-type-badge {
        display: none;
      }
      .issue-drawer-actions {
        margin-top: 24px;
        padding-top: 16px;
        border-top: 2px solid var(--color-outline-variant, #2a3038);
      }
      .assignee-avatar {
        width: 28px;
        height: 28px;
        border-radius: 50%;
        background: var(--color-primary-container, #25a7fd);
        color: #000;
        font-size: 0.75rem;
        font-weight: 700;
        display: flex;
        align-items: center;
        justify-content: center;
        font-family: 'JetBrains Mono', monospace;
      }
      @media (max-width: 768px) {
        .issue-drawer {
          width: 100vw;
        }
        .issue-drawer-body {
          flex-direction: column;
        }
        .issue-drawer-sidebar {
          width: 100%;
        }
      }
    `,
  ],
  imports: [
    RouterLink,
    FormsModule,
    FontAwesomeModule,
    TranslateDirective,
    TranslateModule,
    FormatMediumDatetimePipe,
    IssueCommentList,
    IssueAttachmentList,
    IssueActivityFeed,
  ],
})
export class IssueDetailPanel {
  readonly issue = input.required<IIssue | null>();
  readonly visible = input(false);
  readonly close = input.required<() => void>();

  readonly issueTypeLabels = IssueType;
  readonly issueStatusValues = Object.keys(IssueStatus);
  readonly issueTypeValues = Object.keys(IssueType);
  readonly priorityValues = Object.keys(Priority);
  readonly typeColors = ISSUE_TYPE_COLORS;
  readonly typeIcons = ISSUE_TYPE_ICONS;
  readonly priorityColors = PRIORITY_COLORS;
  readonly priorityIcons = PRIORITY_ICONS;
  readonly statusBadges = STATUS_BADGES;

  readonly isSaving = signal(false);
  readonly assignableUsers = signal<IUser[]>([]);

  protected readonly issueService = inject(IssueService);
  protected readonly alertService = inject(AlertService);
  protected readonly translateService = inject(TranslateService);

  constructor() {
    effect(() => {
      if (this.visible() && this.assignableUsers().length === 0) {
        this.issueService.getAssignableUsers().subscribe({
          next: users => this.assignableUsers.set(users),
        });
      }
    });
  }

  onStatusChange(issue: IIssue, newStatus: string): void {
    if (issue.status === newStatus) {
      return;
    }
    this.isSaving.set(true);
    this.issueService.partialUpdate({ id: issue.id, status: newStatus as keyof typeof IssueStatus }).subscribe({
      next: updated => {
        Object.assign(issue, updated);
        this.isSaving.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isSaving.set(false);
        const message = err.error?.detail ?? err.message ?? this.translateService.instant('error.general');
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  onAssignChange(issue: IIssue, userId: string): void {
    if (!userId) {
      return;
    }
    this.isSaving.set(true);
    this.issueService.assign(issue.id, Number(userId)).subscribe({
      next: updated => {
        Object.assign(issue, updated);
        this.isSaving.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isSaving.set(false);
        const message = err.error?.detail ?? err.message ?? this.translateService.instant('error.general');
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  getInitials(login: string | undefined): string {
    if (!login) {
      return '?';
    }
    return login.charAt(0).toUpperCase();
  }
}
