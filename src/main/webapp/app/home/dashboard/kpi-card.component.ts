import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

export interface KpiData {
  label: string;
  value: number | string;
  icon: string;
  trend?: number;
  unit?: string;
}

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-kpi-card',
  standalone: true,
  imports: [FontAwesomeModule, TranslateModule],
  template: `
    <div class="kpi-card">
      <div class="kpi-header">
        <span class="kpi-label">{{ label() }}</span>
        <fa-icon [icon]="icon()" class="kpi-icon" />
      </div>
      <div class="kpi-value">{{ value() }}</div>
      @if (trend(); as t) {
        <div class="kpi-trend" [class.positive]="t >= 0" [class.negative]="t < 0">
          <fa-icon [icon]="t >= 0 ? 'arrow-up' : 'arrow-down'" />
          <span>{{ t }}%</span>
        </div>
      }
      @if (unit(); as u) {
        <div class="kpi-unit">{{ u }}</div>
      }
    </div>
  `,
  styles: [
    `
      .kpi-card {
        background: var(--color-surface-container);
        border: 3px solid var(--color-primary);
        box-shadow: var(--shadow-brutal);
        padding: var(--stack-md);
        display: flex;
        flex-direction: column;
        gap: var(--stack-sm);
      }
      .kpi-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
      .kpi-label {
        font-family: var(--font-mono);
        font-size: var(--text-xs);
        text-transform: uppercase;
        letter-spacing: 0.05em;
        color: var(--color-muted);
      }
      .kpi-icon {
        color: var(--color-primary);
        font-size: var(--text-lg);
      }
      .kpi-value {
        font-family: var(--font-mono);
        font-size: var(--text-4xl);
        font-weight: 700;
        color: var(--color-on-surface);
        line-height: 1;
      }
      .kpi-trend {
        font-family: var(--font-mono);
        font-size: var(--text-sm);
        display: flex;
        align-items: center;
        gap: 4px;
      }
      .kpi-trend.positive {
        color: var(--color-success);
      }
      .kpi-trend.negative {
        color: var(--color-danger);
      }
      .kpi-unit {
        font-family: var(--font-mono);
        font-size: var(--text-xs);
        color: var(--color-muted);
      }
    `,
  ],
})
export class KpiCardComponent {
  readonly label = input.required<string>();
  readonly value = input.required<number | string>();
  readonly icon = input.required<string>();
  readonly trend = input<number>();
  readonly unit = input<string>();
}
