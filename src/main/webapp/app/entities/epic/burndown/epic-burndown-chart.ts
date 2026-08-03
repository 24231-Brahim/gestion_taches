import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal } from '@angular/core';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { TranslateDirective } from 'app/shared/language';
import { IEpic } from '../epic.model';
import { EpicBurndownData, EpicService } from '../service/epic.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-epic-burndown-chart',
  templateUrl: './epic-burndown-chart.html',
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
export class EpicBurndownChart {
  readonly epic = input<IEpic | null>(null);
  readonly totalStoryPoints = input<number>(0);
  readonly doneStoryPoints = input<number>(0);

  private readonly epicService = inject(EpicService);
  readonly burndownData = signal<EpicBurndownData | null>(null);

  private burndownEffect = effect(() => {
    const ep = this.epic();
    if (ep?.id) {
      this.epicService.getBurndown(ep.id).subscribe({
        next: data => this.burndownData.set(data),
        error: () => this.burndownData.set(null),
      });
    }
  });

  readonly idealPoints = computed(() => {
    const data = this.burndownData();
    if (!data || data.dates.length === 0) {
      return [];
    }
    const total = this.totalStoryPoints();
    return data.dates.map((_, i) => ({
      x: 40 + (i / Math.max(1, data.dates.length - 1)) * 250,
      y: 40 + (data.ideal[i] / Math.max(1, total)) * 120,
    }));
  });

  readonly actualPoints = computed(() => {
    const data = this.burndownData();
    if (!data || data.dates.length === 0) {
      return [];
    }
    const total = this.totalStoryPoints();
    return data.dates.map((_, i) => ({
      x: 40 + (i / Math.max(1, data.dates.length - 1)) * 250,
      y: 40 + (data.actual[i] / Math.max(1, total)) * 120,
    }));
  });

  readonly idealLine = computed(() => {
    return this.idealPoints()
      .map(p => `${p.x},${p.y}`)
      .join(' ');
  });

  readonly actualLine = computed(() => {
    return this.actualPoints()
      .map(p => `${p.x},${p.y}`)
      .join(' ');
  });

  readonly dayLabels = computed(() => {
    const data = this.burndownData();
    if (!data) {
      return [];
    }
    const step = Math.max(1, Math.floor(data.dates.length / 5));
    return data.dates
      .filter((_, i) => i % step === 0 || i === data.dates.length - 1)
      .map((d, _i) => {
        const idx = data.dates.indexOf(d);
        return {
          x: 40 + (idx / Math.max(1, data.dates.length - 1)) * 250,
          label: d.substring(5),
        };
      });
  });
}
