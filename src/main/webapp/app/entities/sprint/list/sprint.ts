import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import dayjs from 'dayjs/esm';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { AlertService } from 'app/core/util/alert.service';
import { IssueService } from 'app/entities/issue/service/issue.service';
import { IIssue } from 'app/entities/issue/issue.model';
import { FormatMediumDatePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { SprintActiveBoard } from '../active-board/sprint-active-board';
import { SprintBacklogPlanning } from '../backlog-planning/sprint-backlog-planning';
import { SprintBurndownChart } from '../burndown/sprint-burndown-chart';
import { SprintService } from '../service/sprint.service';
import { ISprint } from '../sprint.model';

type Tab = 'board' | 'planning' | 'burndown';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-sprint',
  templateUrl: './sprint.html',
  styles: [
    `
      .sprint-page {
        padding: 24px;
        max-width: 1400px;
        margin: 0 auto;
      }
      .sprint-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 20px;
        flex-wrap: wrap;
        gap: 12px;
      }
      .sprint-header-left {
        display: flex;
        align-items: center;
        gap: 16px;
      }
      .sprint-selector {
        min-width: 280px;
      }
      .sprint-selector select {
        background: var(--color-surface-container, #1b2025);
        border: 2px solid var(--color-outline-variant, #2a3038);
        color: var(--color-text, #dfe3ea);
        font-family: 'JetBrains Mono', monospace;
        font-size: 0.9rem;
        padding: 8px 12px;
      }
      .sprint-header-actions {
        display: flex;
        gap: 8px;
        align-items: center;
      }
      .sprint-name {
        font-family: 'Audiowide', monospace;
        font-size: 1.4rem;
        color: var(--color-text, #dfe3ea);
        margin: 0;
      }
      .sprint-meta {
        color: var(--color-text-muted, #6a8fac);
        font-size: 0.85rem;
        font-family: 'JetBrains Mono', monospace;
      }
      .sprint-goal {
        color: var(--color-text, #dfe3ea);
        font-size: 0.9rem;
        margin: 4px 0;
      }
      .status-badge {
        display: inline-block;
        padding: 2px 10px;
        border: 2px solid var(--color-outline-variant, #2a3038);
        font-size: 0.75rem;
        font-family: 'JetBrains Mono', monospace;
        text-transform: uppercase;
        background: var(--color-surface-container, #1b2025);
      }
      .tab-bar {
        display: flex;
        gap: 0;
        border-bottom: 3px solid var(--color-outline-variant, #2a3038);
        margin-bottom: 20px;
      }
      .tab-item {
        padding: 10px 24px;
        cursor: pointer;
        font-family: 'JetBrains Mono', monospace;
        font-size: 0.85rem;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        border: none;
        background: transparent;
        color: var(--color-text-muted, #6a8fac);
        border-bottom: 3px solid transparent;
        margin-bottom: -3px;
        transition:
          color 0.15s,
          border-color 0.15s;
      }
      .tab-item:hover {
        color: var(--color-text, #dfe3ea);
      }
      .tab-item.active {
        color: var(--color-primary, #97cbff);
        border-bottom-color: var(--color-primary, #97cbff);
      }
      .no-sprint {
        text-align: center;
        padding: 60px 20px;
        color: var(--color-text-muted, #6a8fac);
        font-family: 'JetBrains Mono', monospace;
      }
      @media (max-width: 768px) {
        .sprint-page {
          padding: 12px;
        }
        .sprint-header {
          flex-direction: column;
          align-items: stretch;
        }
        .sprint-selector {
          min-width: 0;
        }
        .tab-item {
          padding: 8px 12px;
          font-size: 0.75rem;
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
    FormatMediumDatePipe,
    SprintActiveBoard,
    SprintBacklogPlanning,
    SprintBurndownChart,
  ],
})
export class Sprint implements OnInit {
  readonly activeTab = signal<Tab>('board');
  readonly isSaving = signal(false);
  readonly selectedSprintId = signal<number | null>(null);

  protected readonly sprintService = inject(SprintService);
  protected readonly issueService = inject(IssueService);
  protected readonly alertService = inject(AlertService);
  protected readonly translateService = inject(TranslateService);

  readonly sprints = signal<ISprint[]>([]);
  readonly issues = signal<IIssue[]>([]);
  readonly projectIssues = signal<IIssue[]>([]);

  readonly selectedSprint = computed(() => {
    const id = this.selectedSprintId();
    if (!id) {
      return null;
    }
    return this.sprints().find(s => s.id === id) ?? null;
  });

  constructor() {
    this.issueService.issuesParams.set(undefined);
  }

  ngOnInit(): void {
    this.sprintService.sprintsParams.set({
      size: 100,
      sort: 'startDate,desc',
    });
  }

  private sprintsEffect = effect(() => {
    const raw = this.sprintService.sprints();
    if (raw) {
      this.sprints.set(raw);
      if (raw.length > 0 && !this.selectedSprintId()) {
        const active = raw.find(s => s.status === 'ACTIVE');
        this.selectedSprintId.set(active?.id ?? raw[0].id);
      }
    }
  });

  private issuesEffect = effect(() => {
    const raw = this.issueService.issues();
    if (raw && raw.length > 0) {
      this.issues.set(raw.filter(i => i.sprint?.id === this.selectedSprintId()) as IIssue[]);
      this.projectIssues.set(raw as IIssue[]);
    } else if (raw && raw.length === 0 && this.issueService.issuesResource.hasValue()) {
      this.issues.set([]);
      this.projectIssues.set([]);
    }
  });

  onSprintSelect(): void {
    const sp = this.selectedSprint();
    if (sp?.project?.id) {
      this.issueService.issuesParams.set({
        'projectId.equals': sp.project.id,
        size: 500,
      });
    }
  }

  private refreshIssues(): void {
    const sp = this.selectedSprint();
    if (sp?.project?.id) {
      this.issueService.issuesParams.set({
        'projectId.equals': sp.project.id,
        size: 500,
        t: Date.now(),
      });
    }
  }

  onStatusChange(event: { issueId: number; status: string }): void {
    this.isSaving.set(true);
    this.issueService.partialUpdate({ id: event.issueId, status: event.status as any }).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.refreshIssues();
      },
      error: (err: HttpErrorResponse) => {
        this.isSaving.set(false);
        const message = err.error?.detail ?? err.message ?? this.translateService.instant('error.general');
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  onAssignToSprint(event: { issueId: number; sprintId: number }): void {
    this.isSaving.set(true);
    this.issueService.partialUpdate({ id: event.issueId, sprint: { id: event.sprintId } as any }).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.refreshIssues();
      },
      error: (err: HttpErrorResponse) => {
        this.isSaving.set(false);
        const message = err.error?.detail ?? err.message ?? this.translateService.instant('error.general');
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  onRemoveFromSprint(issueId: number): void {
    this.isSaving.set(true);
    this.issueService.partialUpdate({ id: issueId, sprint: null as any }).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.refreshIssues();
      },
      error: (err: HttpErrorResponse) => {
        this.isSaving.set(false);
        const message = err.error?.detail ?? err.message ?? this.translateService.instant('error.general');
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  onStartSprint(): void {
    const sp = this.selectedSprint();
    if (!sp) {
      return;
    }
    this.isSaving.set(true);
    this.sprintService.partialUpdate({ id: sp.id, status: 'ACTIVE' }).subscribe({
      next: () => {
        this.isSaving.set(false);
        Object.assign(sp, { status: 'ACTIVE' });
      },
      error: (err: HttpErrorResponse) => {
        this.isSaving.set(false);
        const message = err.error?.detail ?? err.message ?? this.translateService.instant('error.general');
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  onCompleteSprint(): void {
    const sp = this.selectedSprint();
    if (!sp) {
      return;
    }
    this.isSaving.set(true);
    this.sprintService.partialUpdate({ id: sp.id, status: 'COMPLETED' }).subscribe({
      next: () => {
        this.isSaving.set(false);
        Object.assign(sp, { status: 'COMPLETED' });
      },
      error: (err: HttpErrorResponse) => {
        this.isSaving.set(false);
        const message = err.error?.detail ?? err.message ?? this.translateService.instant('error.general');
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  onReopenSprint(): void {
    const sp = this.selectedSprint();
    if (!sp) {
      return;
    }
    this.isSaving.set(true);
    this.sprintService.partialUpdate({ id: sp.id, status: 'ACTIVE' }).subscribe({
      next: () => {
        this.isSaving.set(false);
        Object.assign(sp, { status: 'ACTIVE' });
      },
      error: (err: HttpErrorResponse) => {
        this.isSaving.set(false);
        const message = err.error?.detail ?? err.message ?? this.translateService.instant('error.general');
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  onSelectIssue(_issue: IIssue): void {
    // no-op for now
  }

  setTab(tab: Tab): void {
    this.activeTab.set(tab);
  }

  get canManage(): boolean {
    return true;
  }

  readonly daysLeft = computed(() => {
    const sp = this.selectedSprint();
    if (!sp?.endDate) {
      return 0;
    }
    const now = dayjs();
    const end = dayjs(sp.endDate);
    return Math.max(0, end.diff(now, 'day'));
  });
}
