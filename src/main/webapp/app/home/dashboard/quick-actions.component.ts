import { ChangeDetectionStrategy, Component } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { RouterLink } from '@angular/router';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-dashboard-quick-actions',
  standalone: true,
  imports: [TranslateModule, RouterLink],
  template: `
    <div class="quick-actions-card">
      <h3 class="qa-title" jhiTranslate="dashboard.quickActions.title">QUICK ACTIONS</h3>
      <div class="qa-grid">
        <a [routerLink]="['/project/new']" class="qa-btn">
          <span class="qa-icon">+</span>
          <span jhiTranslate="dashboard.quickActions.newProject">New Project</span>
        </a>
        <a [routerLink]="['/project']" class="qa-btn">
          <span class="qa-icon">◉</span>
          <span jhiTranslate="dashboard.quickActions.viewProjects">View Projects</span>
        </a>
      </div>
    </div>
  `,
  styles: [
    `
      .quick-actions-card {
        background: var(--color-surface-container);
        border: 3px solid var(--color-primary);
        box-shadow: var(--shadow-brutal);
        padding: var(--stack-md);
      }
      .qa-title {
        font-family: var(--font-display);
        font-size: var(--headline-md);
        letter-spacing: 0.04em;
        color: var(--color-primary);
        margin-bottom: var(--stack-md);
        padding-bottom: var(--stack-sm);
        border-bottom: 3px solid var(--color-primary);
      }
      .qa-grid {
        display: flex;
        flex-direction: row;
        gap: var(--stack-md);
        flex-wrap: wrap;
      }
      .qa-btn {
        display: flex;
        flex: 1 1 160px;
        flex-direction: column;
        align-items: center;
        gap: var(--stack-sm);
        padding: var(--stack-md);
        background: var(--color-surface);
        border: 3px solid var(--color-primary);
        box-shadow: var(--shadow-brutal);
        text-decoration: none;
        color: var(--color-on-surface);
        font-family: var(--font-mono);
        font-size: var(--text-sm);
        text-transform: uppercase;
        letter-spacing: 0.04em;
        transition: none;
      }
      .qa-btn:hover {
        background: var(--color-surface-container-high);
        transform: translate(-2px, -2px);
        box-shadow: var(--shadow-brutal-hover);
      }
      .qa-icon {
        font-size: var(--text-2xl);
        font-weight: 700;
        color: var(--color-primary);
        line-height: 1;
      }
    `,
  ],
})
export class DashboardQuickActionsComponent {}
