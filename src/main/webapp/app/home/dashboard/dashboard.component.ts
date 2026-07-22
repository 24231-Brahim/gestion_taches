/* eslint-disable @typescript-eslint/no-unsafe-return */
import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { HttpParams, httpResource } from '@angular/common/http';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { KpiCardComponent } from './kpi-card.component';
import { DashboardChartsComponent } from './charts.component';
import { DashboardListsComponent } from './lists.component';
import { DashboardTimelineComponent } from './timeline.component';
import { DashboardQuickActionsComponent } from './quick-actions.component';

export interface DashboardKpis {
  totalProjects: number;
  activeProjects: number;
  totalTasks: number;
  completedTasks: number;
  overdueTasks: number;
  teamMembers: number;
  projectProgress: Array<{ projectId: number; projectName: string; totalTasks: number; doneTasks: number }>;
  taskDistribution: Array<{ status: string; count: number }>;
}

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-dashboard',
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
        <jhi-kpi-card label="{{ 'dashboard.kpi.totalProjects' | translate }}" [value]="totalProjects()" icon="folder" />
        <jhi-kpi-card label="{{ 'dashboard.kpi.activeProjects' | translate }}" [value]="activeProjects()" icon="rocket" />
        <jhi-kpi-card label="{{ 'dashboard.kpi.totalTasks' | translate }}" [value]="totalTasks()" icon="tasks" />
        <jhi-kpi-card label="{{ 'dashboard.kpi.completedTasks' | translate }}" [value]="completedTasks()" icon="check-circle" />
        <jhi-kpi-card label="{{ 'dashboard.kpi.overdueTasks' | translate }}" [value]="overdueTasks()" icon="exclamation-circle" />
        <jhi-kpi-card label="{{ 'dashboard.kpi.teamMembers' | translate }}" [value]="teamMembers()" icon="users" />
      </div>
      <jhi-dashboard-quick-actions />
      <jhi-dashboard-charts [taskDistribution]="taskDistribution()" [projectProgress]="projectProgress()" />
      <jhi-dashboard-lists [recentProjects]="recentProjects()" [recentTasks]="recentTasks()" />
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
export class DashboardComponent {
  readonly totalProjects = computed(() => this.kpisResource.value()?.totalProjects ?? 0);
  readonly activeProjects = computed(() => this.kpisResource.value()?.activeProjects ?? 0);
  readonly totalTasks = computed(() => this.kpisResource.value()?.totalTasks ?? 0);
  readonly completedTasks = computed(() => this.kpisResource.value()?.completedTasks ?? 0);
  readonly overdueTasks = computed(() => this.kpisResource.value()?.overdueTasks ?? 0);
  readonly teamMembers = computed(() => this.kpisResource.value()?.teamMembers ?? 0);
  readonly taskDistribution = computed(() => this.kpisResource.value()?.taskDistribution ?? []);
  readonly projectProgress = computed(() => this.kpisResource.value()?.projectProgress ?? []);
  readonly recentProjects = computed<any[]>(() => this.projectsResource.value() ?? []);
  readonly recentTasks = computed<any[]>(() => this.tasksResource.value() ?? []);
  readonly loading = computed(() => this.kpisResource.isLoading() || this.projectsResource.isLoading() || this.tasksResource.isLoading());
  readonly error = computed(() => this.kpisResource.error() ?? this.projectsResource.error() ?? this.tasksResource.error());

  private readonly applicationConfigService = inject(ApplicationConfigService);
  private readonly kpisResource = httpResource<DashboardKpis>(() => ({
    url: this.applicationConfigService.getEndpointFor('api/dashboard/kpis'),
  }));
  private readonly projectsResource = httpResource<any[]>(() => ({
    url: this.applicationConfigService.getEndpointFor('api/projects'),
    params: new HttpParams().set('page', '0').set('size', '5').set('sort', 'createdAt,desc'),
  }));
  private readonly tasksResource = httpResource<any[]>(() => ({
    url: this.applicationConfigService.getEndpointFor('api/tasks'),
    params: new HttpParams().set('page', '0').set('size', '10').set('sort', 'updatedAt,desc'),
  }));
}
