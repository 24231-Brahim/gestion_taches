import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

interface TaskStatusCount {
  label: string;
  value: number;
  color: string;
}

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-dashboard-charts',
  standalone: true,
  imports: [TranslateModule],
  template: `
    <div class="charts-grid">
      <div class="chart-card">
        <h3 class="chart-title" jhiTranslate="dashboard.charts.projectProgress">PROJECT PROGRESS</h3>
        <div class="chart-body">
          @if (projectProgress().length > 0) {
            <div class="progress-list">
              @for (p of projectProgress(); track p.name) {
                <div class="progress-item">
                  <div class="progress-label">
                    <span class="progress-name">{{ p.name }}</span>
                    <span class="progress-pct">{{ p.percent }}%</span>
                  </div>
                  <div class="progress-bar-brutal">
                    <div class="progress-fill" [style.width.%]="p.percent" [style.background]="p.color"></div>
                  </div>
                </div>
              }
            </div>
          } @else {
            <p class="text-muted" jhiTranslate="dashboard.noData">No data</p>
          }
        </div>
      </div>

      <div class="chart-card">
        <h3 class="chart-title" jhiTranslate="dashboard.charts.taskDistribution">TASK DISTRIBUTION</h3>
        <div class="chart-body">
          @if (taskDistribution().length > 0) {
            <svg [attr.viewBox]="'0 0 200 200'" class="donut-svg">
              @for (slice of donutSlices(); track slice.label) {
                <circle
                  cx="100"
                  cy="100"
                  r="80"
                  fill="none"
                  [attr.stroke]="slice.color"
                  [attr.stroke-width]="20"
                  [attr.stroke-dasharray]="slice.dashArray"
                  [attr.stroke-dashoffset]="slice.dashOffset"
                  transform="rotate(-90 100 100)"
                />
              }
              <text x="100" y="95" text-anchor="middle" class="donut-center-value" fill="var(--color-on-surface)">
                {{ totalTasks() }}
              </text>
              <text x="100" y="115" text-anchor="middle" class="donut-center-label" fill="var(--color-muted)">TOTAL</text>
            </svg>
            <div class="donut-legend">
              @for (slice of taskDistribution(); track slice.label) {
                <div class="legend-item">
                  <span class="legend-dot" [style.background]="slice.color"></span>
                  <span class="legend-label">{{ slice.label }}</span>
                  <span class="legend-value">{{ slice.value }}</span>
                </div>
              }
            </div>
          } @else {
            <p class="text-muted" jhiTranslate="dashboard.noData">No data</p>
          }
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      .charts-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: var(--stack-lg);
      }
      .chart-card {
        background: var(--color-surface-container);
        border: 3px solid var(--color-primary);
        box-shadow: var(--shadow-brutal);
        padding: var(--stack-md);
      }
      .chart-title {
        font-family: var(--font-display);
        font-size: var(--headline-md);
        letter-spacing: 0.04em;
        color: var(--color-primary);
        margin-bottom: var(--stack-md);
        padding-bottom: var(--stack-sm);
        border-bottom: 3px solid var(--color-primary);
      }
      .chart-body {
        min-height: 200px;
      }
      .progress-list {
        display: flex;
        flex-direction: column;
        gap: var(--stack-md);
      }
      .progress-item {
        display: flex;
        flex-direction: column;
        gap: 4px;
      }
      .progress-label {
        display: flex;
        justify-content: space-between;
        font-family: var(--font-mono);
        font-size: var(--text-sm);
        color: var(--color-on-surface);
      }
      .progress-name {
        text-transform: uppercase;
      }
      .progress-pct {
        font-weight: 700;
      }
      .progress-bar-brutal {
        height: 8px;
        background: var(--color-surface-container-high);
        border: 2px solid var(--color-outline);
      }
      .progress-fill {
        height: 100%;
        transition: width var(--transition-fast);
      }
      .donut-svg {
        width: 200px;
        height: 200px;
        display: block;
        margin: 0 auto;
      }
      .donut-center-value {
        font-family: var(--font-mono);
        font-size: 28px;
        font-weight: 700;
      }
      .donut-center-label {
        font-family: var(--font-mono);
        font-size: 10px;
        text-transform: uppercase;
        letter-spacing: 0.1em;
      }
      .donut-legend {
        display: flex;
        flex-wrap: wrap;
        gap: var(--stack-sm);
        margin-top: var(--stack-md);
        justify-content: center;
      }
      .legend-item {
        display: flex;
        align-items: center;
        gap: 6px;
        font-family: var(--font-mono);
        font-size: var(--text-xs);
        color: var(--color-on-surface);
        text-transform: uppercase;
      }
      .legend-dot {
        width: 10px;
        height: 10px;
        border: 2px solid var(--color-outline);
      }
      .legend-value {
        font-weight: 700;
        margin-left: auto;
      }
    `,
  ],
})
export class DashboardChartsComponent {
  readonly tasks = input.required<any[]>();
  readonly projects = input.required<any[]>();

  readonly totalTasks = computed(() => this.tasks().length);

  readonly taskDistribution = computed<TaskStatusCount[]>(() => {
    const statuses = ['BACKLOG', 'TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE', 'CANCELLED'];
    const colors = ['#6a8fac', '#f59e0b', '#25a7fd', '#a855f7', '#22c55e', '#ef4444'];
    const map = new Map<string, number>();
    for (const s of statuses) map.set(s, 0);
    for (const task of this.tasks()) {
      const st = task.status ?? 'BACKLOG';
      map.set(st, (map.get(st) ?? 0) + 1);
    }
    return Array.from(map.entries())
      .filter(([_, v]) => v > 0)
      .map(([label, value], i) => ({
        label,
        value,
        color: colors[statuses.indexOf(label)] ?? '#6a8fac',
      }));
  });

  readonly donutSlices = computed(() => {
    const total = this.totalTasks();
    if (total === 0) return [];
    const slices = this.taskDistribution();
    const circumference = 2 * Math.PI * 80;
    let offset = 0;
    return slices.map(s => {
      const length = (s.value / total) * circumference;
      const slice = {
        color: s.color,
        dashArray: `${length} ${circumference - length}`,
        dashOffset: -offset,
        label: s.label,
      };
      offset += length;
      return slice;
    });
  });

  readonly projectProgress = computed(() => {
    const projectTasks = new Map<number, any[]>();
    for (const task of this.tasks()) {
      const pid = task.project?.id;
      if (pid) {
        if (!projectTasks.has(pid)) projectTasks.set(pid, []);
        projectTasks.get(pid)!.push(task);
      }
    }
    const projectMap = new Map(this.projects().map(p => [p.id, p]));
    const colors = ['#22c55e', '#25a7fd', '#f59e0b', '#a855f7', '#52d6fd', '#ef4444', '#ec4899', '#8b5cf6', '#0ea5e9', '#84cc16'];
    return Array.from(projectTasks.entries())
      .map(([pid, iss], i) => {
        const total = iss.length;
        const done = iss.filter(x => x.status === 'DONE').length;
        const project = projectMap.get(pid);
        return {
          name: project?.name ?? `Project #${pid}`,
          percent: total > 0 ? Math.round((done / total) * 100) : 0,
          color: colors[i % colors.length],
        };
      })
      .sort((a, b) => a.percent - b.percent)
      .slice(0, 10);
  });
}
