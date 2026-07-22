import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { TranslateDirective } from 'app/shared/language';
import { ISprint } from '../sprint.model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-sprint-burndown-chart',
  templateUrl: './sprint-burndown-chart.html',
  imports: [FontAwesomeModule, TranslateDirective, TranslateModule],
  styles: [
    `
      .burndown-container {
        display: flex;
        flex-direction: column;
        border-radius: var(--radius-lg);
      }

      .burndown-stats {
        display: flex;
        flex-direction: row;
        gap: 16px;
      }

      .stat-card {
        border: 1px solid var(--color-outline-variant);
        border-radius: var(--radius-lg);
        padding: 16px;
        background: var(--color-surface-container);
        box-shadow: var(--shadow-sm);
      }

      .stat-value {
        font-size: 1.5rem;
        font-family: var(--font-inter);
        font-weight: 600;
      }

      .stat-value.done {
        color: var(--color-status-done);
      }

      .stat-value.remaining {
        color: var(--color-status-todo);
      }

      .stat-value.velocity {
        color: var(--color-primary);
      }

      .burndown-svg {
        width: 100%;
        height: auto;
        max-height: 250px;
      }
    `,
  ],
})
export class SprintBurndownChart {
  readonly sprint = input<ISprint | null>(null);
  readonly totalTasks = input<number>(0);
  readonly doneTasks = input<number>(0);
  readonly daysLeft = input<number>(0);

  readonly actualPoints = computed(() => {
    const total = this.totalTasks();
    const done = this.doneTasks();
    const xStart = 40;
    const xEnd = 290;
    const yTop = 40;
    const yBottom = 160;

    if (total === 0) {
      return [
        { x: xStart, y: yTop },
        { x: xEnd, y: yTop },
      ];
    }

    const remaining = total - done;
    const yCurrent = yTop + (remaining / total) * (yBottom - yTop);

    return [
      { x: xStart, y: yBottom },
      { x: xEnd, y: yCurrent },
    ];
  });

  readonly actualLine = computed(() => {
    return this.actualPoints()
      .map(p => `${p.x},${p.y}`)
      .join(' ');
  });
}
