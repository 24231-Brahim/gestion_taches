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
        border: 3px solid var(--color-primary);
        box-shadow: var(--shadow-brutal);
        padding: var(--stack-md);
      }
      .timeline-title {
        font-family: var(--font-display);
        font-size: var(--headline-md);
        letter-spacing: 0.04em;
        color: var(--color-primary);
        margin-bottom: var(--stack-md);
        padding-bottom: var(--stack-sm);
        border-bottom: 3px solid var(--color-primary);
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
        border-left: 3px solid var(--color-outline);
        padding-left: var(--stack-md);
        position: relative;
      }
      .timeline-marker {
        position: absolute;
        left: -7px;
        top: var(--stack-sm);
        width: 12px;
        height: 12px;
        border: 2px solid var(--color-outline);
        transform: rotate(45deg);
      }
      .timeline-content {
        display: flex;
        flex-direction: column;
        gap: 2px;
      }
      .tl-title {
        font-family: var(--font-mono);
        font-size: var(--text-sm);
        text-transform: uppercase;
        color: var(--color-on-surface);
      }
      .tl-meta {
        font-family: var(--font-mono);
        font-size: var(--text-xs);
        color: var(--color-muted);
      }
      .text-muted {
        font-family: var(--font-mono);
        font-size: var(--text-sm);
        color: var(--color-muted);
      }
    `,
  ],
})
export class DashboardTimelineComponent {
  readonly issues = input.required<any[]>();

  readonly activities = computed<TimelineItem[]>(() => {
    const colors = ['#22c55e', '#25a7fd', '#f59e0b', '#a855f7', '#52d6fd'];
    const sorted = [...this.issues()]
      .sort((a, b) => {
        const da = a.updatedAt ?? a.createdAt ?? '';
        const db = b.updatedAt ?? b.createdAt ?? '';
        return db.localeCompare(da);
      })
      .slice(0, 10);
    return sorted.map((issue, i) => ({
      id: issue.id,
      title: issue.title ?? 'Untitled',
      status: issue.status ?? 'N/A',
      date: issue.updatedAt ?? issue.createdAt ?? '—',
      color: colors[i % colors.length],
    }));
  });
}
