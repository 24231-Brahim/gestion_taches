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
      <jhi-dashboard-charts [issues]="issues()" [projects]="projects()" />
      <jhi-dashboard-lists [recentProjects]="recentProjects()" [recentTasks]="recentTasks()" />
      <div class="bottom-grid">
        <jhi-dashboard-timeline [issues]="issues()" />
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
        font-family: var(--font-mono);
        font-size: var(--text-sm);
        border: 3px solid var(--color-outline);
      }
      .loading-banner {
        background: var(--color-surface-container-high);
        padding: var(--stack-md);
        font-family: var(--font-mono);
        font-size: var(--text-sm);
        border: 3px solid var(--color-primary);
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
  readonly projects = computed<any[]>(() => this.projectsResource.value() ?? []);
  readonly issues = computed<any[]>(() => this.issuesResource.value() ?? []);
  readonly stats = computed<any>(() => this.statsResource.value() ?? {});
  readonly totalProjects = computed(() => this.stats().totalProjects ?? 0);
  readonly activeProjects = computed(() => this.stats().activeProjects ?? 0);
  readonly totalTasks = computed(() => this.stats().totalTasks ?? 0);
  readonly completedTasks = computed(() => this.stats().completedTasks ?? 0);
  readonly overdueTasks = computed(() => this.stats().overdueTasks ?? 0);
  readonly teamMembers = computed(() => this.memberCountResource.value() ?? 0);
  readonly loading = computed(
    () =>
      this.projectsResource.isLoading() ||
      this.issuesResource.isLoading() ||
      this.statsResource.isLoading() ||
      this.doneCountResource.isLoading() ||
      this.memberCountResource.isLoading(),
  );
  readonly error = computed(
    () =>
      this.projectsResource.error() ??
      this.issuesResource.error() ??
      this.statsResource.error() ??
      this.doneCountResource.error() ??
      this.memberCountResource.error(),
  );
  readonly recentProjects = computed(() =>
    [...this.projects()].sort((a, b) => (b.createdAt ?? '').localeCompare(a.createdAt ?? '')).slice(0, 5),
  );
  readonly recentTasks = computed(() => {
    const sorted = [...this.issues()].sort((a, b) => {
      const da = b.updatedAt ?? b.createdAt ?? '';
      const db = a.updatedAt ?? a.createdAt ?? '';
      return da.localeCompare(db);
    });
    return sorted.slice(0, 8);
  });

  private readonly applicationConfigService = inject(ApplicationConfigService);
  private readonly projectsResource = httpResource<any[]>(() => ({
    url: this.applicationConfigService.getEndpointFor('api/projects'),
    params: new HttpParams().set('page', '0').set('size', '500'),
  }));
  private readonly issuesResource = httpResource<any[]>(() => ({
    url: this.applicationConfigService.getEndpointFor('api/issues'),
    params: new HttpParams().set('page', '0').set('size', '500'),
  }));
  private readonly statsResource = httpResource<any>(() => ({
    url: this.applicationConfigService.getEndpointFor('api/developer-dashboard/statistics'),
  }));
  private readonly doneCountResource = httpResource<number>(() => ({
    url: this.applicationConfigService.getEndpointFor('api/issues/count'),
    params: new HttpParams().set('status.equals', 'DONE'),
  }));
  private readonly memberCountResource = httpResource<number>(() => ({
    url: this.applicationConfigService.getEndpointFor('api/projects/members/count'),
  }));
}
