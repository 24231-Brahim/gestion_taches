import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import dayjs from 'dayjs/esm';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';
import { AlertService } from 'app/core/util/alert.service';
import { FormatMediumDatePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { ProjectRole } from 'app/entities/enumerations/project-role.model';
import { TaskService } from 'app/entities/task/service/task.service';
import { ITask } from 'app/entities/task/task.model';
import { SprintActiveBoard } from '../active-board/sprint-active-board';
import { SprintBacklogPlanning } from '../backlog-planning/sprint-backlog-planning';
import { SprintBurndownChart } from '../burndown/sprint-burndown-chart';
import { SprintService, VelocityReport } from '../service/sprint.service';
import { ISprint } from '../sprint.model';

type Tab = 'board' | 'planning' | 'burndown';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-sprint-detail',
  templateUrl: './sprint-detail.html',
  styles: [
    `
      .sprint-detail-container {
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
      .sprint-header-info {
        display: flex;
        align-items: center;
        gap: 16px;
      }
      .sprint-header-actions {
        display: flex;
        gap: 8px;
      }
      .sprint-name {
        font-family: var(--font-inter);
        font-size: 1.4rem;
        color: var(--color-text, #dfe3ea);
        margin: 0;
      }
      .sprint-meta {
        color: var(--color-text-muted, #6a8fac);
        font-size: 0.85rem;
        font-family: var(--font-inter);
      }
      .sprint-goal {
        color: var(--color-text, #dfe3ea);
        font-size: 0.9rem;
        margin: 4px 0;
      }
      .status-badge {
        display: inline-block;
        padding: 2px 10px;
        border: 1px solid var(--color-outline-variant, #2a3038);
        border-radius: 9999px;
        font-size: 0.75rem;
        font-family: var(--font-inter);
        text-transform: none;
        background: var(--color-surface-container, #1b2025);
        font-weight: 600;
      }
      .tab-bar {
        display: flex;
        gap: 0;
        border-bottom: 1px solid var(--color-outline-variant, #2a3038);
        margin-bottom: 20px;
      }
      .tab-item {
        padding: 10px 24px;
        cursor: pointer;
        font-family: var(--font-inter);
        font-size: 0.85rem;
        text-transform: none;
        letter-spacing: 0;
        border: none;
        background: transparent;
        color: var(--color-text-muted, #6a8fac);
        border-bottom: 2px solid transparent;
        margin-bottom: -1px;
        transition:
          color var(--transition-fast),
          border-color var(--transition-fast);
      }
      .tab-item:hover {
        color: var(--color-text, #dfe3ea);
      }
      .tab-item.active {
        color: var(--color-primary, #97cbff);
        border-bottom-color: var(--color-primary, #97cbff);
      }
      .velocity-modal-overlay {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: color-mix(in srgb, var(--color-bg) 70%, transparent);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 1050;
      }
      .velocity-modal {
        background: var(--color-surface-container, #1b2025);
        border: 1px solid var(--color-outline-variant, #2a3038);
        border-radius: var(--radius-lg);
        padding: 24px;
        max-width: 420px;
        width: 90%;
        box-shadow: var(--shadow-lg);
      }
      .velocity-modal h3 {
        font-family: var(--font-inter);
        color: var(--color-primary, #97cbff);
        margin-bottom: 16px;
        font-weight: 600;
      }
      .velocity-stat {
        display: flex;
        justify-content: space-between;
        padding: 8px 0;
        border-bottom: 1px solid var(--color-outline-variant, #2a3038);
        font-family: var(--font-inter);
        font-size: 0.9rem;
        color: var(--color-text, #dfe3ea);
      }
      .velocity-stat-value {
        font-weight: 600;
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
      .sprint-progress-wrapper {
        margin-bottom: 20px;
        padding: 12px 16px;
        border: 1px solid var(--color-outline-variant, #2a3038);
        border-radius: var(--radius-lg);
        background: var(--color-surface-container, #1b2025);
        box-shadow: var(--shadow-sm);
      }
      .sprint-progress-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 8px;
      }
      .sprint-progress-label {
        font-family: var(--font-inter);
        font-size: 0.8rem;
        color: var(--color-text-muted, #6a8fac);
        text-transform: none;
      }
      .sprint-progress-value {
        font-family: var(--font-inter);
        font-size: 0.85rem;
        color: var(--color-primary, #97cbff);
        font-weight: 600;
      }
      .sprint-progress-bar {
        width: 100%;
        height: 6px;
        background: var(--color-surface-container-high, #262d36);
        border-radius: 9999px;
        overflow: hidden;
      }
      .sprint-progress-fill {
        height: 100%;
        background: var(--color-status-done, #4caf50);
        border-radius: 9999px;
        transition: width 0.3s ease;
      }
      @media (max-width: 768px) {
        .sprint-header {
          flex-direction: column;
          align-items: flex-start;
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
    FontAwesomeModule,
    TranslateDirective,
    TranslateModule,
    FormatMediumDatePipe,
    SprintActiveBoard,
    SprintBacklogPlanning,
    SprintBurndownChart,
  ],
})
export class SprintDetail {
  readonly sprint = input<ISprint | null>(null);

  readonly projectKey = computed(() => this._currentSprint()?.project?.key ?? null);

  readonly activeTab = signal<Tab>('board');
  readonly isSaving = signal(false);
  readonly showVelocityModal = signal(false);
  readonly velocityReport = signal<VelocityReport | null>(null);

  readonly tasks = signal<ITask[]>([]);
  readonly projectTasks = signal<ITask[]>([]);

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

  readonly hasActiveSprint = computed(() => {
    const sp = this._currentSprint();
    if (!sp?.project?.id) {
      return false;
    }
    return this.sprintService.sprints().some(s => s.status === 'ACTIVE' && s.id !== sp.id);
  });

  readonly canStartSprint = computed(() => {
    const sp = this._currentSprint();
    if (!sp || sp.status !== 'PLANNED') {
      return false;
    }
    return true;
  });

  readonly canCloseSprint = computed(() => this._currentSprint()?.status === 'ACTIVE');

  readonly sprintTasksCount = computed(() => this.tasks().length);
  readonly doneTasksCount = computed(() => this.tasks().filter(i => i.status === 'DONE').length);
  readonly sprintProgress = computed(() => {
    const total = this.sprintTasksCount();
    if (total === 0) {
      return 0;
    }
    return Math.round((this.doneTasksCount() / total) * 100);
  });

  readonly totalStoryPoints = computed(() => 0);

  readonly doneStoryPoints = computed(() => 0);

  readonly daysLeft = computed(() => {
    const sp = this._currentSprint();
    if (!sp?.endDate) {
      return 0;
    }
    const now = dayjs();
    const end = dayjs(sp.endDate);
    return Math.max(0, end.diff(now, 'day'));
  });

  protected readonly taskService = inject(TaskService);
  protected readonly sprintService = inject(SprintService);
  protected readonly alertService = inject(AlertService);
  protected readonly translateService = inject(TranslateService);
  protected readonly accountService = inject(AccountService);

  private readonly _currentSprint = signal<ISprint | null>(null);

  private sprintSyncEffect = effect(() => {
    this._currentSprint.set(this.sprint());
  });

  private issuesEffect = effect(() => {
    const raw = this.taskService.tasks();
    if (raw && raw.length > 0) {
      this.tasks.set(raw.filter(i => i.sprint?.id === this._currentSprint()?.id));
      this.projectTasks.set(raw);
    } else if (raw?.length === 0 && this.taskService.tasksResource.hasValue()) {
      this.tasks.set([]);
      this.projectTasks.set([]);
    }
  });

  private sprintEffect = effect(() => {
    const sp = this._currentSprint();
    if (sp?.project?.id) {
      this.taskService.tasksParams.set({
        'projectId.equals': sp.project.id,
        size: 500,
      });
    }
  });

  onSelectTask(_issue: ITask): void {
    // no-op for now
  }

  private refreshIssues(): void {
    const sp = this._currentSprint();
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
    const sp = this._currentSprint();
    if (!sp) {
      return;
    }
    this.isSaving.set(true);
    this.sprintService.startSprint(sp.id).subscribe({
      next: updated => {
        this.isSaving.set(false);
        this._currentSprint.set(updated);
        this.refreshIssues();
      },
      error: (err: HttpErrorResponse) => {
        this.isSaving.set(false);
        const message = err.error?.detail ?? err.message ?? this.translateService.instant('error.general');
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  onCompleteSprint(): void {
    const sp = this._currentSprint();
    if (!sp) {
      return;
    }
    this.isSaving.set(true);
    this.sprintService.closeSprint(sp.id).subscribe({
      next: report => {
        this.isSaving.set(false);
        this._currentSprint.set({ ...sp, status: 'COMPLETED' });
        this.velocityReport.set(report);
        this.showVelocityModal.set(true);
        this.refreshIssues();
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

  setTab(tab: Tab): void {
    this.activeTab.set(tab);
  }
}
