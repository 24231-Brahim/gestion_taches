import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter } from 'rxjs';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { IProject } from 'app/entities/project/project.model';
import { ProjectService } from 'app/entities/project/service/project.service';

interface BreadcrumbItem {
  label: string;
  route?: string;
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

    const rootParams = this.activatedRoute.snapshot.params;
    const key = rootParams['key'];

    if (key) {
      items.push({ label: 'Projects', route: '/project' });
      items.push({ label: this.projectName() ?? key.toUpperCase(), route: `/project/${key}/view` });

      const fullUrl = snapshot.url.map(segment => segment.path).join('/');
      const segments = fullUrl.split('/');

      if (segments[0] === 'sprint' || segments[0] === 'epic' || segments[0] === 'task') {
        const entityLabel = segments[0] === 'task' ? 'Tasks' : segments[0] === 'sprint' ? 'Sprints' : 'Epics';
        const entityRoute = `/project/${key}/${segments[0]}`;

        if (segments.length === 1 || (segments.length === 2 && segments[1] !== 'new')) {
          items.push({ label: entityLabel });
        } else if (segments[1] === 'new') {
          items.push({ label: entityLabel, route: entityRoute });
          items.push({ label: 'Create' });
        } else if (segments.length >= 2) {
          items.push({ label: entityLabel, route: entityRoute });
          if (segments[2] === 'view') {
            items.push({ label: 'Details' });
          } else if (segments[2] === 'edit') {
            items.push({ label: 'Details', route: `${entityRoute}/${segments[1]}/view` });
            items.push({ label: 'Edit' });
          }
        }
      }
    } else {
      items.push({ label: 'Home', route: '/' });

      const fullUrl = snapshot.url.map(segment => segment.path).join('/');
      if (fullUrl === 'project' || fullUrl.startsWith('project/')) {
        items.push({ label: 'Projects', route: '/project' });
        if (fullUrl === 'project/new') {
          items.push({ label: 'Create' });
        } else if (fullUrl.includes('/view')) {
          items.push({ label: this.projectName() ?? 'Details' });
        } else if (fullUrl.includes('/edit')) {
          items.push({ label: this.projectName() ?? 'Details' });
          items.push({ label: 'Edit' });
        }
      }
    }

    if (items.length > 0) {
      const lastItem = items[items.length - 1];
      delete lastItem.route;
    }

    this.items.set(items);

    if (key && !this.projectName()) {
      this.projectService.findByKey(key).subscribe({
        next: project => this.projectName.set(project.name ?? key.toUpperCase()),
        error: () => this.projectName.set(key.toUpperCase()),
      });
    }
  }
}
