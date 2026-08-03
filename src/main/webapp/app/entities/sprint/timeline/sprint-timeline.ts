import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';

import dayjs from 'dayjs/esm';

import { TranslateDirective } from 'app/shared/language';
import { ITask } from 'app/entities/task/task.model';
import { ISprint } from '../sprint.model';

const STATUS_COLORS: Record<string, string> = {
  NEW: 'var(--color-status-backlog, #9e9e9e)',
  TODO: 'var(--color-status-todo, #2196f3)',
  IN_PROGRESS: 'var(--color-status-in-progress, #ff9800)',
  IN_REVIEW: 'var(--color-status-in-review, #9c27b0)',
  DONE: 'var(--color-status-done, #4caf50)',
  CANCELLED: 'var(--color-status-cancelled, #f44336)',
};

interface TimelineLabel {
  label: string;
  offset: number;
}

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-sprint-timeline',
  templateUrl: './sprint-timeline.html',
  styles: [
    `
      .timeline-header,
      .timeline-row {
        display: flex;
        gap: 12px;
        align-items: center;
      }
      .timeline-header {
        padding-bottom: 8px;
        border-bottom: 1px solid var(--color-outline-variant, #2a3038);
      }
      .timeline-task-col {
        width: 260px;
        flex-shrink: 0;
        display: flex;
        align-items: center;
        gap: 8px;
        min-width: 0;
      }
      .timeline-header-label {
        font-family: var(--font-inter);
        font-size: 0.7rem;
        text-transform: none;
        color: var(--color-text-muted, #6a8fac);
        font-weight: 600;
      }
      .timeline-scale {
        position: relative;
        flex: 1;
        min-width: 0;
        height: 100%;
      }
      .timeline-labels {
        position: relative;
        height: 16px;
      }
      .timeline-label {
        position: absolute;
        top: 0;
        inset-inline-start: 0;
        white-space: nowrap;
        font-family: var(--font-inter);
        font-size: 0.65rem;
        color: var(--color-text-muted, #6a8fac);
      }
      .timeline-body {
        display: flex;
        flex-direction: column;
      }
      .timeline-row {
        height: 40px;
        border-bottom: 1px solid var(--color-outline-variant, #2a3038);
      }
      .timeline-row:hover {
        background: var(--color-surface-container, #1b2025);
      }
      .timeline-task-title {
        font-family: var(--font-inter);
        font-size: 0.8rem;
        color: var(--color-text, #dfe3ea);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        flex: 1;
        min-width: 0;
      }
      .status-badge {
        display: inline-block;
        padding: 2px 8px;
        border: 1px solid var(--color-outline-variant, #2a3038);
        border-radius: 9999px;
        font-size: 0.65rem;
        font-family: var(--font-inter);
        color: var(--color-text-muted, #6a8fac);
        background: var(--color-surface-container, #1b2025);
        flex-shrink: 0;
      }
      .timeline-track {
        align-self: stretch;
      }
      .timeline-gridline {
        position: absolute;
        top: 0;
        bottom: 0;
        width: 1px;
        background: var(--color-outline-variant, #2a3038);
        opacity: 0.5;
      }
      .timeline-bar {
        position: absolute;
        top: 12px;
        bottom: 12px;
        inset-inline-start: var(--bar-start, 0);
        inset-inline-end: var(--bar-end, 0);
        border-radius: 4px;
        min-width: 6px;
        opacity: 0.9;
      }
      .timeline-today {
        position: absolute;
        top: 0;
        bottom: 0;
        width: 2px;
        background: var(--color-primary, #97cbff);
        z-index: 2;
      }
      .timeline-empty {
        padding: 32px 16px;
        text-align: center;
        color: var(--color-text-muted, #6a8fac);
        font-family: var(--font-inter);
        font-size: 0.85rem;
      }
      @media (max-width: 768px) {
        .timeline-task-col {
          width: 180px;
        }
      }
    `,
  ],
  imports: [TranslateDirective, TranslateModule],
})
export class SprintTimeline {
  readonly sprint = input<ISprint | null>(null);
  readonly tasks = input<ITask[]>([]);

  readonly rangeStart = computed(() => {
    const sp = this.sprint();
    if (sp?.startDate) {
      return sp.startDate.startOf('day');
    }
    const created = this.tasks()
      .map(t => t.createdAt)
      .filter((d): d is dayjs.Dayjs => Boolean(d));
    if (created.length > 0) {
      return created.reduce((min, d) => (d.isBefore(min) ? d : min)).startOf('day');
    }
    return dayjs().startOf('day');
  });

  readonly rangeEnd = computed(() => {
    const sp = this.sprint();
    if (sp?.endDate) {
      return sp.endDate.endOf('day');
    }
    const created = this.tasks()
      .map(t => t.createdAt)
      .filter((d): d is dayjs.Dayjs => Boolean(d));
    if (created.length > 0) {
      return created.reduce((max, d) => (d.isAfter(max) ? d : max)).endOf('day');
    }
    return dayjs().endOf('day');
  });

  readonly totalDays = computed(() => {
    const days = this.rangeEnd().diff(this.rangeStart(), 'day') + 1;
    return Math.max(1, days);
  });

  readonly useWeeks = computed(() => this.totalDays() > 62);

  readonly labels = computed<TimelineLabel[]>(() => {
    const start = this.rangeStart();
    const labels: TimelineLabel[] = [];
    if (this.useWeeks()) {
      let cursor = start.startOf('week');
      while (cursor.isBefore(this.rangeEnd())) {
        labels.push({ label: cursor.format('MMM D'), offset: Math.max(0, cursor.diff(start, 'day')) });
        cursor = cursor.add(1, 'week');
      }
    } else {
      for (let i = 0; i < this.totalDays(); i++) {
        const d = start.add(i, 'day');
        labels.push({ label: d.format('MMM D'), offset: i });
      }
    }
    return labels;
  });

  readonly todayOffset = computed(() => {
    const now = dayjs().startOf('day');
    const start = this.rangeStart();
    if (now.isBefore(start)) {
      return 0;
    }
    if (now.isAfter(this.rangeEnd())) {
      return this.totalDays() + 1;
    }
    return now.diff(start, 'day') + 1;
  });

  getStatusColor(status: string | null | undefined): string {
    return STATUS_COLORS[status ?? ''] ?? 'var(--color-outline-variant)';
  }

  labelPosition(offset: number): string {
    return `${(offset / this.totalDays()) * 100}%`;
  }

  barStyle(task: ITask): Record<string, string> {
    const start = this.rangeStart();
    const total = Math.max(1, this.rangeEnd().diff(start, 'day'));
    const taskStart = task.createdAt ? task.createdAt.startOf('day') : start;
    const clampedStart = taskStart.isBefore(start) ? start : taskStart;
    const startOffset = Math.min(total, clampedStart.diff(start, 'day'));
    return {
      '--bar-start': `${(startOffset / total) * 100}%`,
      '--bar-end': `${((total - startOffset) / total) * 100}%`,
    };
  }

  todayStyle(): Record<string, string> {
    const offset = Math.min(this.totalDays(), Math.max(0, this.todayOffset() - 1));
    return {
      'inset-inline-start': `${(offset / this.totalDays()) * 100}%`,
    };
  }
}
