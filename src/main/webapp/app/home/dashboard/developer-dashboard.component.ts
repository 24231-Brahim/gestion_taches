/* eslint-disable @typescript-eslint/no-unsafe-return */
import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { HttpParams, httpResource } from '@angular/common/http';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { AccountService } from 'app/core/auth/account.service';
import { KpiCardComponent } from './kpi-card.component';
import { DashboardChartsComponent } from './charts.component';
import { DashboardListsComponent } from './lists.component';
import { DashboardTimelineComponent } from './timeline.component';
import { DashboardQuickActionsComponent } from './quick-actions.component';

export interface DeveloperDashboardStatistics {
  assignedTasksTotal: number;
  inProgressTasks: number;
  doneTasks: number;
  overdueTasks: number;
  memberProjectsCount: number;
  taskDistribution: { status: string; count: number }[];
}

interface MemberProject {
  id: number;
  key: string;
  name: string;
}

interface ProjectProgressStats {
  projectId: number;
  totalTasks: number;
  doneTasks: number;
}

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-developer-dashboard',
  standalone: true,
  imports: [
    TranslateModule,
    KpiCardComponent,
    DashboardChartsComponent,
    DashboardListsComponent,
    DashboardTimelineComponent,
    DashboardQuickActionsComponent,
  ],
  template: `
    <div class="dashboard">
      @if (error()) {
        <div class="error-banner">
          <span jhiTranslate="dashboard.error">Failed to load dashboard data</span>
        </div>
      }
      @if (loading()) {
        <div class="loading-banner">
          <span jhiTranslate="dashboard.loading">Loading dashboard data...</span>
        </div>
      }
      <div class="kpi-grid">
        @if (!hideTaskStats()) {
          <jhi-kpi-card label="{{ 'dashboard.kpi.myAssignedTasks' | translate }}" [value]="assignedTasksTotal()" icon="tasks" />
          <jhi-kpi-card label="{{ 'dashboard.kpi.overdueTasks' | translate }}" [value]="overdueTasks()" icon="exclamation-circle" />
        }
        <jhi-kpi-card label="{{ 'dashboard.kpi.inProgressTasks' | translate }}" [value]="inProgressTasks()" icon="sync" />
        <jhi-kpi-card label="{{ 'dashboard.kpi.completedTasks' | translate }}" [value]="doneTasks()" icon="check-circle" />
        <jhi-kpi-card label="{{ 'dashboard.kpi.myProjects' | translate }}" [value]="memberProjectsCount()" icon="folder" />
      </div>
      <jhi-dashboard-quick-actions [variant]="variant()" [firstProjectKey]="firstProjectKey()" />
      <jhi-dashboard-charts [taskDistribution]="taskDistribution()" [projectProgress]="projectProgress()" />
      <jhi-dashboard-lists [recentProjects]="recentProjects()" [recentTasks]="recentTasks()" [showRecentProjects]="showRecentProjects()" />
      <div class="bottom-grid">
        <jhi-dashboard-timeline [tasks]="recentTasks()" />
      </div>
    </div>
  `,
  styles: [
    `
      .dashboard {
        display: flex;
        flex-direction: column;
        gap: var(--stack-lg);
        padding: var(--stack-lg) 0;
      }
      .kpi-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
        gap: var(--stack-md);
      }
      .bottom-grid {
        display: grid;
        grid-template-columns: 1fr;
        gap: var(--stack-lg);
      }
      .error-banner {
        background: var(--color-danger);
        color: white;
        padding: var(--stack-md);
        font-family: var(--font-inter);
        font-size: var(--text-sm);
        border: 1px solid var(--color-outline);
        border-radius: var(--radius-md);
      }
      .loading-banner {
        background: var(--color-surface-container-high);
        padding: var(--stack-md);
        font-family: var(--font-inter);
        font-size: var(--text-sm);
        border: 1px solid var(--color-primary);
        border-radius: var(--radius-md);
        animation: pulse 1.5s ease-in-out infinite;
      }
      @keyframes pulse {
        0%,
        100% {
          opacity: 1;
        }
        50% {
          opacity: 0.5;
        }
      }
    `,
  ],
})
export class DeveloperDashboardComponent {
  readonly showRecentProjects = input(false);
  readonly hideTaskStats = input(false);

  readonly variant = computed<'developer' | 'user'>(() => (this.hideTaskStats() ? 'user' : 'developer'));

  readonly assignedTasksTotal = computed(() => this.statisticsResource.value()?.assignedTasksTotal ?? 0);
  readonly inProgressTasks = computed(() => this.statisticsResource.value()?.inProgressTasks ?? 0);
  readonly doneTasks = computed(() => this.statisticsResource.value()?.doneTasks ?? 0);
  readonly overdueTasks = computed(() => this.statisticsResource.value()?.overdueTasks ?? 0);
  readonly memberProjectsCount = computed(() => this.statisticsResource.value()?.memberProjectsCount ?? 0);
  readonly taskDistribution = computed(() => this.statisticsResource.value()?.taskDistribution ?? []);

  readonly firstProjectKey = computed(() => this.myProjectsResource.value()?.[0]?.key ?? null);

  readonly recentProjects = computed<MemberProject[]>(() => this.myProjectsResource.value() ?? []);

  readonly projectProgress = computed(() => {
    const projects = this.myProjectsResource.value() ?? [];
    const stats = this.projectStatsResource.value() ?? [];
    const statsById = new Map(stats.map(s => [s.projectId, s]));
    return projects.map(p => {
      const s = statsById.get(p.id);
      return { projectId: p.id, projectName: p.name, totalTasks: s?.totalTasks ?? 0, doneTasks: s?.doneTasks ?? 0 };
    });
  });

  readonly recentTasks = computed<any[]>(() => this.recentTasksResource.value() ?? []);

  readonly loading = computed(
    () =>
      this.statisticsResource.isLoading() ||
      this.myProjectsResource.isLoading() ||
      this.projectStatsResource.isLoading() ||
      this.recentTasksResource.isLoading(),
  );

  readonly error = computed(
    () =>
      this.statisticsResource.error() ??
      this.myProjectsResource.error() ??
      this.projectStatsResource.error() ??
      this.recentTasksResource.error(),
  );

  private readonly applicationConfigService = inject(ApplicationConfigService);
  private readonly accountService = inject(AccountService);
  private readonly translateService = inject(TranslateService);

  private readonly currentUserId = computed(() => this.accountService.account()?.id ?? null);

  private readonly statisticsResource = httpResource<DeveloperDashboardStatistics>(() => ({
    url: this.applicationConfigService.getEndpointFor('api/developer-dashboard/statistics'),
  }));

  // Projects the developer is a member of (scope=mine also applies to plain DEVELOPER accounts
  // on the backend regardless of the query param, since they never get global project access).
  private readonly myProjectsResource = httpResource<MemberProject[]>(() => ({
    url: this.applicationConfigService.getEndpointFor('api/projects'),
    params: new HttpParams().set('scope', 'mine').set('page', '0').set('size', '50').set('sort', 'name,asc'),
  }));

  private readonly projectStatsResource = httpResource<ProjectProgressStats[]>(() => ({
    url: this.applicationConfigService.getEndpointFor('api/projects/progress'),
  }));

  private readonly recentTasksResource = httpResource<any[]>(() => {
    const userId = this.currentUserId();
    if (userId == null) {
      return undefined;
    }
    return {
      url: this.applicationConfigService.getEndpointFor('api/tasks'),
      params: new HttpParams().set('assigneeId.equals', userId).set('page', '0').set('size', '5').set('sort', 'updatedAt,desc'),
    };
  });
}
