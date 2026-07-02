import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, HostListener, inject, input, output } from '@angular/core';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { IssueStatus } from 'app/entities/enumerations/issue-status.model';
import { AlertService } from 'app/core/util/alert.service';
import { TranslateDirective } from 'app/shared/language';
import { ISSUE_TYPE_COLORS, ISSUE_TYPE_ICONS, PRIORITY_COLORS, PRIORITY_ICONS, STATUS_BADGES } from '../issue-helper';
import { IIssue } from '../issue.model';
import { IssueService } from '../service/issue.service';

interface KanbanColumn {
  status: string;
  issues: IIssue[];
}

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-issue-kanban-board',
  templateUrl: './issue-kanban-board.html',
  styles: [
    `
      .kanban-board {
        display: flex;
        gap: 12px;
        overflow-x: auto;
        padding: 12px 0;
        min-height: 60vh;
      }
      .kanban-column {
        flex: 1;
        min-width: 220px;
        max-width: 300px;
        background: var(--color-surface-container, #1b2025);
        border-radius: 6px;
        border: 3px solid var(--color-outline-variant, #2a3038);
        transition: border-color 0.2s;
      }
      .kanban-column-drag-over {
        border-color: var(--color-primary, #97cbff);
      }
      .kanban-column-header {
        padding: 12px;
        border-top: 3px solid;
        border-radius: 3px 3px 0 0;
        display: flex;
        align-items: center;
        justify-content: space-between;
      }
      .kanban-column-title {
        font-family: 'Audiowide', monospace;
        font-size: 0.75rem;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        color: var(--color-text, #dfe3ea);
      }
      .kanban-column-count {
        background: var(--color-surface-container-high, #262d36);
        color: var(--color-text-muted, #6a8fac);
        border-radius: 10px;
        padding: 1px 8px;
        font-size: 0.75rem;
      }
      .kanban-column-body {
        padding: 8px;
        display: flex;
        flex-direction: column;
        gap: 8px;
        min-height: 100px;
      }
      .kanban-card {
        background: var(--color-surface, #0f1419);
        border: 2px solid var(--color-outline-variant, #2a3038);
        border-radius: 6px;
        padding: 10px;
        cursor: grab;
        transition: all 0.15s;
        box-shadow: 3px 3px 0 var(--color-outline-variant, #2a3038);
      }
      .kanban-card:hover {
        transform: translate(-1px, -1px);
        box-shadow: 4px 4px 0 var(--color-primary, #97cbff);
      }
      .kanban-card-dragging {
        opacity: 0.5;
      }
      .kanban-card-top {
        display: flex;
        align-items: center;
        gap: 6px;
        margin-bottom: 6px;
        font-size: 0.8rem;
      }
      .kanban-card-id {
        color: var(--color-text-muted, #6a8fac);
        font-size: 0.7rem;
        margin-left: auto;
      }
      .kanban-card-title {
        font-size: 0.85rem;
        color: var(--color-text, #dfe3ea);
        line-height: 1.3;
        margin-bottom: 8px;
        word-break: break-word;
      }
      .kanban-card-footer {
        display: flex;
        align-items: center;
        gap: 6px;
      }
      .assignee-initials {
        width: 22px;
        height: 22px;
        border-radius: 50%;
        background: var(--color-primary-container, #25a7fd);
        color: #000;
        font-size: 0.65rem;
        font-weight: 700;
        display: flex;
        align-items: center;
        justify-content: center;
        font-family: 'JetBrains Mono', monospace;
      }
    `,
  ],
  imports: [FontAwesomeModule, TranslateDirective, TranslateModule],
})
export class IssueKanbanBoard {
  readonly issues = input<IIssue[]>([]);
  readonly selectIssue = output<IIssue>();

  readonly typeColors = ISSUE_TYPE_COLORS;
  readonly typeIcons = ISSUE_TYPE_ICONS;
  readonly priorityColors = PRIORITY_COLORS;
  readonly priorityIcons = PRIORITY_ICONS;
  readonly statusBadges = STATUS_BADGES;

  dragIssueId: number | null = null;
  dragOverStatus: string | null = null;

  readonly columns: KanbanColumn[] = Object.keys(IssueStatus).map(status => ({
    status,
    issues: [],
  }));

  protected readonly issueService = inject(IssueService);
  protected readonly alertService = inject(AlertService);
  protected readonly translateService = inject(TranslateService);

  getColumns(): KanbanColumn[] {
    return this.columns.map(col => ({
      ...col,
      issues: this.issues().filter(i => i.status === col.status),
    }));
  }

  onDragStart(issue: IIssue): void {
    this.dragIssueId = issue.id;
  }

  onDragOver(event: DragEvent, status: string): void {
    event.preventDefault();
    this.dragOverStatus = status;
  }

  onDragLeave(): void {
    this.dragOverStatus = null;
  }

  onDrop(event: DragEvent, targetStatus: string): void {
    event.preventDefault();
    this.dragOverStatus = null;
    if (this.dragIssueId === null) {
      return;
    }
    const issue = this.issues().find(i => i.id === this.dragIssueId);
    if (!issue || issue.status === targetStatus) {
      this.dragIssueId = null;
      return;
    }
    this.issueService.partialUpdate({ id: issue.id, status: targetStatus as keyof typeof IssueStatus }).subscribe({
      next: () => {
        issue.status = targetStatus as keyof typeof IssueStatus;
        this.dragIssueId = null;
      },
      error: (err: HttpErrorResponse) => {
        this.dragIssueId = null;
        const message = err.error?.detail ?? err.message ?? this.translateService.instant('error.general');
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  @HostListener('document:dragend')
  onDragEnd(): void {
    this.dragIssueId = null;
    this.dragOverStatus = null;
  }

  getInitials(login: string | undefined): string {
    if (!login) {
      return '?';
    }
    return login.charAt(0).toUpperCase();
  }

  onIssueClick(issue: IIssue): void {
    this.selectIssue.emit(issue);
  }
}
