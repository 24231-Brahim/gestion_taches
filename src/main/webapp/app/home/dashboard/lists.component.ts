import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { RouterLink } from '@angular/router';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-dashboard-lists',
  standalone: true,
  imports: [TranslateModule, RouterLink],
  template: `
    <div class="lists-grid">
      <div class="list-card">
        <h3 class="list-title" jhiTranslate="dashboard.lists.recentProjects">RECENT PROJECTS</h3>
        <div class="list-body">
          @if (recentProjects().length > 0) {
            <div class="list-items">
              @for (p of recentProjects(); track p.id) {
                <a [routerLink]="['/project', p.key, 'view']" class="list-item">
                  <span class="item-name">{{ p.name }}</span>
                  <span class="item-key">{{ p.key }}</span>
                </a>
              }
            </div>
          } @else {
            <p class="text-muted" jhiTranslate="dashboard.noData">No data</p>
          }
        </div>
      </div>

      <div class="list-card">
        <h3 class="list-title" jhiTranslate="dashboard.lists.recentTasks">RECENT TASKS</h3>
        <div class="list-body">
          @if (recentTasks().length > 0) {
            <div class="list-items">
              @for (t of recentTasks(); track t.id) {
                <a [routerLink]="['/project', t.project?.key, 'task', t.id, 'view']" class="list-item">
                  <span class="item-name">{{ t.title }}</span>
                  <span class="item-meta" [style.color]="statusColor(t.status!)">{{ t.status }}</span>
                </a>
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
      .lists-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: var(--stack-lg);
      }
      .list-card {
        background: var(--color-surface-container);
        border: 3px solid var(--color-primary);
        box-shadow: var(--shadow-brutal);
        padding: var(--stack-md);
      }
      .list-title {
        font-family: var(--font-display);
        font-size: var(--headline-md);
        letter-spacing: 0.04em;
        color: var(--color-primary);
        margin-bottom: var(--stack-md);
        padding-bottom: var(--stack-sm);
        border-bottom: 3px solid var(--color-primary);
      }
      .list-body {
        min-height: 100px;
      }
      .list-items {
        display: flex;
        flex-direction: column;
        gap: 2px;
      }
      .list-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: var(--stack-sm);
        text-decoration: none;
        color: var(--color-on-surface);
        border: 2px solid transparent;
        transition: none;
      }
      .list-item:hover {
        background: var(--color-surface-container-high);
        border-color: var(--color-outline);
      }
      .item-name {
        font-family: var(--font-mono);
        font-size: var(--text-sm);
        text-transform: uppercase;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .item-key {
        font-family: var(--font-mono);
        font-size: var(--text-xs);
        text-transform: uppercase;
        flex-shrink: 0;
        color: var(--color-primary);
        font-weight: 700;
      }
      .item-meta {
        font-family: var(--font-mono);
        font-size: var(--text-xs);
        text-transform: uppercase;
        flex-shrink: 0;
        font-weight: 700;
      }
      .text-muted {
        font-family: var(--font-mono);
        font-size: var(--text-sm);
        color: var(--color-muted);
      }
    `,
  ],
})
export class DashboardListsComponent {
  readonly recentProjects = input<any[]>([]);
  readonly recentTasks = input<any[]>([]);

  statusColor(status: string): string {
    const map: Record<string, string> = {
      DONE: 'var(--color-success)',
      IN_PROGRESS: 'var(--color-info)',
      IN_REVIEW: '#a855f7',
      TODO: '#f59e0b',
      NEW: 'var(--color-muted)',
      CANCELLED: 'var(--color-danger)',
    };
    return map[status] ?? 'var(--color-on-surface)';
  }
}
