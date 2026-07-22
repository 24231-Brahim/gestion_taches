/* eslint-disable @typescript-eslint/no-unsafe-return */
import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

interface TimelineItem {
  id: number;
  title: string;
  status: string;
  date: string;
  color: string;
}

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-dashboard-timeline',
  standalone: true,
  imports: [TranslateModule],
  template: `
    <div class="timeline-card">
      <h3 class="timeline-title" jhiTranslate="dashboard.timeline.title">RECENT ACTIVITY</h3>
      <div class="timeline-body">
        @if (activities().length > 0) {
          <div class="timeline-items">
            @for (a of activities(); track a.id) {
              <div class="timeline-item">
                <div class="timeline-marker" [style.background]="a.color"></div>
                <div class="timeline-content">
                  <span class="tl-title">{{ a.title }}</span>
                  <span class="tl-meta">{{ a.status }} — {{ a.date }}</span>
                </div>
              </div>
            }
          </div>
        } @else {
          <p class="text-muted" jhiTranslate="dashboard.noData">No data</p>
        }
      </div>
    </div>
  `,
  styles: [
    `
      .timeline-card {
        background: var(--color-surface-container);
        border: 1px solid var(--color-outline-variant);
        box-shadow: var(--shadow-sm);
        padding: var(--stack-md);
        border-radius: var(--radius-lg);
      }
      .timeline-title {
        font-family: var(--font-inter);
        font-weight: 600;
        font-size: var(--headline-md);
        letter-spacing: 0;
        color: var(--color-on-surface);
        margin-bottom: var(--stack-md);
        padding-bottom: var(--stack-sm);
        border-bottom: 1px solid var(--color-outline-variant);
      }
      .timeline-body {
        min-height: 100px;
      }
      .timeline-items {
        display: flex;
        flex-direction: column;
      }
      .timeline-item {
        display: flex;
        gap: var(--stack-sm);
        padding: var(--stack-sm) 0;
        border-left: 1px solid var(--color-outline-variant);
        padding-left: var(--stack-md);
        position: relative;
      }
      .timeline-marker {
        position: absolute;
        left: -7px;
        top: var(--stack-sm);
        width: 12px;
        height: 12px;
        border: none;
        border-radius: 9999px;
      }
      .timeline-content {
        display: flex;
        flex-direction: column;
        gap: 2px;
      }
      .tl-title {
        font-family: var(--font-inter);
        font-size: var(--text-sm);
        text-transform: none;
        color: var(--color-on-surface);
      }
      .tl-meta {
        font-family: var(--font-inter);
        font-size: var(--text-xs);
        color: var(--color-muted);
      }
      .text-muted {
        font-family: var(--font-inter);
        font-size: var(--text-sm);
        color: var(--color-muted);
      }
    `,
  ],
})
export class DashboardTimelineComponent {
  readonly tasks = input.required<any[]>();

  readonly activities = computed<TimelineItem[]>(() => {
    const colors = ['#22c55e', '#25a7fd', '#f59e0b', '#a855f7', '#52d6fd'];
    const sorted = [...this.tasks()]
      .sort((a, b) => {
        const da = a.updatedAt ?? a.createdAt ?? '';
        const db = b.updatedAt ?? b.createdAt ?? '';
        return db.localeCompare(da);
      })
      .slice(0, 10);
    return sorted.map((task, i) => ({
      id: task.id,
      title: task.title ?? 'Untitled',
      status: task.status ?? 'N/A',
      date: task.updatedAt ?? task.createdAt ?? '—',
      color: colors[i % colors.length],
    }));
  });
}
