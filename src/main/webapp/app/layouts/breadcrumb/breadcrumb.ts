import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, ActivatedRouteSnapshot, NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter } from 'rxjs';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { ProjectService } from 'app/entities/project/service/project.service';

interface BreadcrumbItem {
  label: string;
  route?: string;
  /** True when `label` is literal display text (e.g. a project name) rather than an i18n key. */
  raw?: boolean;
}

@Component({
  selector: 'jhi-breadcrumb',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './breadcrumb.html',
  styleUrl: './breadcrumb.scss',
  imports: [RouterLink, FontAwesomeModule, TranslateModule],
})
export default class Breadcrumb implements OnInit {
  readonly items = signal<BreadcrumbItem[]>([]);
  readonly projectName = signal<string | null>(null);
  readonly projectKey = signal<string | null>(null);

  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly projectService = inject(ProjectService);
  private readonly cdr = inject(ChangeDetectorRef);

  ngOnInit(): void {
    this.router.events.pipe(filter(event => event instanceof NavigationEnd)).subscribe(() => {
      this.buildBreadcrumb();
    });
    this.buildBreadcrumb();
  }

  private buildBreadcrumb(): void {
    const items: BreadcrumbItem[] = [];
    let route = this.activatedRoute;

    while (route.firstChild) {
      route = route.firstChild;
    }

    const snapshot = route.snapshot;
    const routeConfig = snapshot.routeConfig;

    if (!routeConfig) {
      this.items.set([]);
      return;
    }

    // Angular's default paramsInheritanceStrategy ('emptyOnly') means the root ActivatedRoute's
    // snapshot doesn't carry params from non-empty-path descendants — walk up from the deepest
    // matched route to find the ':key' param wherever it was actually matched.
    let key: string | null = null;
    let paramRoute: ActivatedRouteSnapshot | null = snapshot;
    while (paramRoute && key === null) {
      key = paramRoute.paramMap.get('key');
      paramRoute = paramRoute.parent;
    }

    if (key) {
      items.push({ label: 'global.menu.entities.project', route: '/project' });
      items.push({ label: this.projectName() ?? key.toUpperCase(), route: `/project/${key}/view`, raw: true });

      const fullUrl = snapshot.url.map(segment => segment.path).join('/');
      const segments = fullUrl.split('/');

      if (segments[0] === 'sprint' || segments[0] === 'epic' || segments[0] === 'task') {
        const entityLabel =
          segments[0] === 'task'
            ? 'global.menu.entities.task'
            : segments[0] === 'sprint'
              ? 'global.menu.entities.sprint'
              : 'global.menu.entities.epic';
        const entityRoute = `/project/${key}/${segments[0]}`;

        if (segments.length === 1 || (segments.length === 2 && segments[1] !== 'new')) {
          items.push({ label: entityLabel });
        } else if (segments[1] === 'new') {
          items.push({ label: entityLabel, route: entityRoute });
          items.push({ label: 'entity.action.create' });
        } else if (segments.length >= 2) {
          items.push({ label: entityLabel, route: entityRoute });
          if (segments[2] === 'view') {
            items.push({ label: 'entity.action.view' });
          } else if (segments[2] === 'edit') {
            items.push({ label: 'entity.action.view', route: `${entityRoute}/${segments[1]}/view` });
            items.push({ label: 'entity.action.edit' });
          }
        }
      }
    } else {
      items.push({ label: 'global.menu.home', route: '/' });

      const fullUrl = snapshot.url.map(segment => segment.path).join('/');
      if (fullUrl === 'project' || fullUrl.startsWith('project/')) {
        items.push({ label: 'global.menu.entities.project', route: '/project' });
        if (fullUrl === 'project/new') {
          items.push({ label: 'entity.action.create' });
        } else if (fullUrl.includes('/view')) {
          const name = this.projectName();
          items.push(name ? { label: name, raw: true } : { label: 'entity.action.view' });
        } else if (fullUrl.includes('/edit')) {
          const name = this.projectName();
          items.push(name ? { label: name, raw: true } : { label: 'entity.action.view' });
          items.push({ label: 'entity.action.edit' });
        }
      }
    }

    if (items.length > 0) {
      const lastItem = items[items.length - 1];
      delete lastItem.route;
    }

    this.items.set(items);
    this.cdr.markForCheck();

    if (key && !this.projectName()) {
      this.projectService.findByKey(key).subscribe({
        next: project => {
          this.projectName.set(project.name ?? key.toUpperCase());
          this.buildBreadcrumb();
        },
        error: () => {
          this.projectName.set(key.toUpperCase());
          this.buildBreadcrumb();
        },
      });
    }
  }
}
