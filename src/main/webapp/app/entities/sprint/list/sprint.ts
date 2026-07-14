import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import dayjs from 'dayjs/esm';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';
import { AlertService } from 'app/core/util/alert.service';
import { TaskService } from 'app/entities/task/service/task.service';
import { ITask } from 'app/entities/task/task.model';
import { ProjectRole } from 'app/entities/enumerations/project-role.model';
import { FormatMediumDatePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { SprintActiveBoard } from '../active-board/sprint-active-board';
import { SprintBacklogPlanning } from '../backlog-planning/sprint-backlog-planning';
import { SprintBurndownChart } from '../burndown/sprint-burndown-chart';
import { SprintService, VelocityReport } from '../service/sprint.service';
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
      .velocity-modal-overlay {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.7);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 1050;
      }
      .velocity-modal {
        background: var(--color-surface-container, #1b2025);
        border: 3px solid var(--color-outline-variant, #2a3038);
        padding: 24px;
        max-width: 420px;
        width: 90%;
        box-shadow: 6px 6px 0 var(--color-outline-variant, #2a3038);
      }
      .velocity-modal h3 {
        font-family: 'Audiowide', monospace;
        color: var(--color-primary, #97cbff);
        margin-bottom: 16px;
      }
      .velocity-stat {
        display: flex;
        justify-content: space-between;
        padding: 8px 0;
        border-bottom: 1px solid var(--color-outline-variant, #2a3038);
        font-family: 'JetBrains Mono', monospace;
        font-size: 0.9rem;
        color: var(--color-text, #dfe3ea);
      }
      .velocity-stat-value {
        font-weight: bold;
        color: var(--color-primary, #97cbff);
      }
      .tooltip-wrapper {
        position: relative;
      }
      .tooltip-wrapper[title] {
        cursor: not-allowed;
      }
      .btn-disabled {
        opacity: 0.5;
        pointer-events: none;
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
  readonly showVelocityModal = signal(false);
  readonly velocityReport = signal<VelocityReport | null>(null);

  readonly sprints = signal<ISprint[]>([]);
  readonly tasks = signal<ITask[]>([]);
  readonly projectTasks = signal<ITask[]>([]);

  readonly selectedSprint = computed(() => {
    const id = this.selectedSprintId();
    if (!id) {
      return null;
    }
    return this.sprints().find(s => s.id === id) ?? null;
  });

  readonly userProjectRole = computed<ProjectRole | null>(() => {
    const account = this.accountService.account();
    if (!account) {
      return null;
    }
    if (account.authorities.includes('ROLE_ADMIN')) {
      return ProjectRole.OWNER;
    }
    return null;
  });

  readonly canManageSprints = computed(() => {
    const role = this.userProjectRole();
    return role === ProjectRole.OWNER || role === ProjectRole.MANAGER;
  });

  readonly hasActiveSprint = computed(() => this.sprints().some(s => s.status === 'ACTIVE'));

  readonly canStartSprint = computed(() => {
    const sp = this.selectedSprint();
    if (!sp || sp.status !== 'PLANNED') {
      return false;
    }
    return !this.hasActiveSprint();
  });

  readonly canCloseSprint = computed(() => this.selectedSprint()?.status === 'ACTIVE');

  readonly daysLeft = computed(() => {
    const sp = this.selectedSprint();
    if (!sp?.endDate) {
      return 0;
    }
    const now = dayjs();
    const end = dayjs(sp.endDate);
    return Math.max(0, end.diff(now, 'day'));
  });

  protected readonly sprintService = inject(SprintService);
  protected readonly taskService = inject(TaskService);
  protected readonly alertService = inject(AlertService);
  protected readonly translateService = inject(TranslateService);
  protected readonly accountService = inject(AccountService);

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

  private sprintSelectionEffect = effect(() => {
    const id = this.selectedSprintId();
    const sp = this.sprints().find(s => s.id === id);
    if (sp?.project?.id) {
      this.taskService.tasksParams.set({
        'projectId.equals': sp.project.id,
        size: 500,
      });
    }
  });

  private issuesEffect = effect(() => {
    const raw = this.taskService.tasks();
    if (raw && raw.length > 0) {
      this.tasks.set(raw.filter(i => i.sprint?.id === this.selectedSprintId()));
      this.projectTasks.set(raw);
    } else if (raw?.length === 0 && this.taskService.tasksResource.hasValue()) {
      this.tasks.set([]);
      this.projectTasks.set([]);
    }
  });

  ngOnInit(): void {
    this.sprintService.sprintsParams.set({
      size: 100,
      sort: 'startDate,desc',
    });
  }

  onSprintSelect(): void {
    const sp = this.selectedSprint();
    if (sp?.project?.id) {
      this.taskService.tasksParams.set({
        'projectId.equals': sp.project.id,
        size: 500,
      });
    }
  }

  private refreshIssues(): void {
    const sp = this.selectedSprint();
    if (sp?.project?.id) {
      this.taskService.tasksParams.set({
        'projectId.equals': sp.project.id,
        size: 500,
        t: Date.now(),
      });
    }
  }

  onStatusChange(event: { taskId: number; status: string }): void {
    this.isSaving.set(true);
    this.taskService.partialUpdate({ id: event.taskId, status: event.status as any }).subscribe({
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

  onAssignToSprint(event: { taskId: number; sprintId: number }): void {
    this.isSaving.set(true);
    this.taskService.partialUpdate({ id: event.taskId, sprint: { id: event.sprintId } }).subscribe({
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

  onRemoveFromSprint(taskId: number): void {
    this.isSaving.set(true);
    this.taskService.partialUpdate({ id: taskId, sprint: null }).subscribe({
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
    this.sprintService.startSprint(sp.id).subscribe({
      next: updated => {
        this.isSaving.set(false);
        Object.assign(sp, updated);
        this.refreshIssues();
        this.sprintService.sprintsParams.update(p => ({ ...p, t: Date.now() }));
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
    this.sprintService.closeSprint(sp.id).subscribe({
      next: report => {
        this.isSaving.set(false);
        Object.assign(sp, { status: 'COMPLETED' });
        this.velocityReport.set(report);
        this.showVelocityModal.set(true);
        this.refreshIssues();
        this.sprintService.sprintsParams.update(p => ({ ...p, t: Date.now() }));
      },
      error: (err: HttpErrorResponse) => {
        this.isSaving.set(false);
        const message = err.error?.detail ?? err.message ?? this.translateService.instant('error.general');
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  closeVelocityModal(): void {
    this.showVelocityModal.set(false);
    this.velocityReport.set(null);
  }

  onSelectTask(_issue: ITask): void {
    // no-op for now
  }

  setTab(tab: Tab): void {
    this.activeTab.set(tab);
  }
}
