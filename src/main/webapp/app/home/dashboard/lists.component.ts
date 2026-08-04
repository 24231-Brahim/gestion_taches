import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { RouterLink } from '@angular/router';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-dashboard-lists',
  standalone: true,
  imports: [TranslateModule, RouterLink],
  template: `
    <div class="lists-grid" [class.lists-grid-single]="!showRecentProjects()">
      @if (showRecentProjects()) {
        <div class="list-card">
          <div class="list-header">
            <h3 class="list-title" jhiTranslate="dashboard.lists.recentProjects">RECENT PROJECTS</h3>
            @if (seeAllProjectsRoute()) {
              <a [routerLink]="seeAllProjectsRoute()" class="see-all-link" jhiTranslate="dashboard.lists.seeAll">Voir tout</a>
            }
          </div>
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
      }

      <div class="list-card">
        <div class="list-header">
          <h3 class="list-title" jhiTranslate="dashboard.lists.recentTasks">RECENT TASKS</h3>
          @if (seeAllTasksRoute()) {
            <a [routerLink]="seeAllTasksRoute()" class="see-all-link" jhiTranslate="dashboard.lists.seeAll">Voir tout</a>
          }
        </div>
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
      .lists-grid-single {
        grid-template-columns: 1fr;
      }
      .list-card {
        background: var(--color-surface-container);
        border: 1px solid var(--color-outline-variant);
        box-shadow: var(--shadow-sm);
        padding: var(--stack-md);
        border-radius: var(--radius-lg);
      }
      .list-title {
        font-family: var(--font-inter);
        font-weight: 600;
        font-size: var(--headline-md);
        letter-spacing: 0;
        color: var(--color-on-surface);
        margin-bottom: var(--stack-md);
        padding-bottom: var(--stack-sm);
        border-bottom: 1px solid var(--color-outline-variant);
      }
      .list-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: var(--stack-sm);
      }
      .list-header .list-title {
        margin-bottom: var(--stack-md);
      }
      .see-all-link {
        font-family: var(--font-inter);
        font-size: var(--text-xs);
        font-weight: 600;
        color: var(--color-primary);
        text-decoration: none;
        white-space: nowrap;
      }
      .see-all-link:hover {
        color: var(--color-secondary);
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
        font-family: var(--font-inter);
        font-size: var(--text-sm);
        text-transform: none;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .item-key {
        font-family: var(--font-inter);
        font-size: var(--text-xs);
        text-transform: none;
        flex-shrink: 0;
        color: var(--color-primary);
        font-weight: 700;
      }
      .item-meta {
        font-family: var(--font-inter);
        font-size: var(--text-xs);
        text-transform: none;
        flex-shrink: 0;
        font-weight: 700;
      }
      .text-muted {
        font-family: var(--font-inter);
        font-size: var(--text-sm);
        color: var(--color-muted);
      }
    `,
  ],
})
export class DashboardListsComponent {
  readonly recentProjects = input<any[]>([]);
  readonly recentTasks = input<any[]>([]);
  readonly showRecentProjects = input(true);
  readonly seeAllProjectsRoute = input<string | null>('/project');
  readonly seeAllTasksRoute = input<string | null>('/my-tasks');

  statusColor(status: string): string {
    const map: Record<string, string> = {
      DONE: 'var(--color-success)',
      IN_PROGRESS: 'var(--color-info)',
      READY_FOR_TEST: 'var(--color-status-in-review)',
      NEEDS_INFO: 'var(--color-danger)',
      NEW: 'var(--color-muted)',
    };
    return map[status] ?? 'var(--color-on-surface)';
  }
}
