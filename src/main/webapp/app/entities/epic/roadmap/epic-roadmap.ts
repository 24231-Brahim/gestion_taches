import { HttpHeaders } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, computed, effect, inject, signal, untracked } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Data, ParamMap, Router, RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { KeyValuePipe } from '@angular/common';
import { Subscription, combineLatest, tap } from 'rxjs';

import { DEFAULT_SORT_DATA, SORT } from 'app/config/navigation.constants';
import { ITEMS_PER_PAGE, PAGE_HEADER, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { IProject } from 'app/entities/project/project.model';
import { ProjectService } from 'app/entities/project/service/project.service';
import { Alert } from 'app/shared/alert/alert';
import { AlertError } from 'app/shared/alert/alert-error';
import { Filter, FilterOptions, IFilterOption, IFilterOptions } from 'app/shared/filter';
import FilterComponent from 'app/shared/filter/filter';
import { TranslateDirective } from 'app/shared/language';
import { SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { TaskService } from 'app/entities/task/service/task.service';
import { ITask } from 'app/entities/task/task.model';
import { IEpic } from '../epic.model';
import { EpicService } from '../service/epic.service';

interface EpicWithProgress extends IEpic {
  progress: number;
  totalIssues: number;
  doneIssues: number;
}

const STATUS_COLORS: Record<string, string> = {
  TODO: 'var(--color-status-todo, #2196f3)',
  IN_PROGRESS: 'var(--color-status-in-progress, #ff9800)',
  DONE: 'var(--color-status-done, #4caf50)',
  CANCELLED: 'var(--color-status-cancelled, #f44336)',
};

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-epic-roadmap',
  templateUrl: './epic-roadmap.html',
  styles: [
    `
      .roadmap-page {
        padding-bottom: 24px;
      }
      .roadmap-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 16px;
        gap: 12px;
        flex-wrap: wrap;
      }
      .roadmap-toolbar {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 20px;
        flex-wrap: wrap;
      }
      .roadmap-container {
        background: var(--color-surface-container, #1b2025);
        border: 3px solid var(--color-outline-variant, #2a3038);
        border-radius: 6px;
        overflow: hidden;
      }
      .roadmap-timeline-header {
        padding: 12px 20px;
        border-bottom: 2px solid var(--color-outline-variant, #2a3038);
      }
      .roadmap-month-labels {
        display: flex;
        justify-content: space-between;
        font-family: 'JetBrains Mono', monospace;
        font-size: 0.7rem;
        color: var(--color-text-muted, #6a8fac);
        text-transform: uppercase;
      }
      .roadmap-list {
        display: flex;
        flex-direction: column;
      }
      .roadmap-epic-row {
        display: flex;
        align-items: center;
        gap: 16px;
        padding: 14px 20px;
        border-bottom: 1px solid var(--color-outline-variant, #2a3038);
        transition: background 0.15s;
      }
      .roadmap-epic-row:last-child {
        border-bottom: none;
      }
      .roadmap-epic-row:hover {
        background: rgba(255, 255, 255, 0.03);
      }
      .roadmap-epic-info {
        width: 240px;
        flex-shrink: 0;
      }
      .roadmap-epic-title {
        font-family: 'Audiowide', monospace;
        font-size: 0.85rem;
        color: var(--color-text, #dfe3ea);
        text-decoration: none;
        display: block;
        margin-bottom: 4px;
        word-break: break-word;
      }
      .roadmap-epic-title:hover {
        color: var(--color-primary, #97cbff);
      }
      .roadmap-epic-meta {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 0.75rem;
        color: var(--color-text-muted, #6a8fac);
        flex-wrap: wrap;
      }
      .status-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        display: inline-block;
      }
      .roadmap-epic-dates {
        font-family: 'JetBrains Mono', monospace;
        font-size: 0.65rem;
      }
      .roadmap-epic-bar-container {
        flex: 1;
        display: flex;
        align-items: center;
        gap: 12px;
        min-width: 0;
      }
      .roadmap-epic-bar-track {
        flex: 1;
        height: 20px;
        background: var(--color-surface, #0f1419);
        border-radius: 6px;
        overflow: hidden;
        border: 2px solid var(--color-outline-variant, #2a3038);
      }
      .roadmap-epic-bar {
        height: 100%;
        border-radius: 6px;
        transition: width 0.4s ease;
        display: flex;
        align-items: center;
        justify-content: flex-end;
        padding-right: 6px;
        min-width: 0;
      }
      .roadmap-epic-bar-label {
        font-size: 0.65rem;
        font-weight: 700;
        color: #000;
        font-family: 'JetBrains Mono', monospace;
      }
      .roadmap-pct {
        width: 40px;
        text-align: right;
        font-size: 0.75rem;
        color: var(--color-text-muted, #6a8fac);
        font-family: 'JetBrains Mono', monospace;
      }
      .roadmap-legend {
        display: flex;
        gap: 16px;
        margin-top: 16px;
        flex-wrap: wrap;
      }
      .roadmap-legend-item {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 0.8rem;
        color: var(--color-text-muted, #6a8fac);
      }
      .legend-dot {
        width: 10px;
        height: 10px;
        border-radius: 50%;
        display: inline-block;
      }
      @media (max-width: 768px) {
        .roadmap-epic-row {
          flex-direction: column;
          align-items: flex-start;
          gap: 8px;
        }
        .roadmap-epic-info {
          width: 100%;
        }
        .roadmap-epic-bar-container {
          width: 100%;
        }
      }
    `,
  ],
  standalone: true,
  imports: [
    RouterLink,
    FormsModule,
    FontAwesomeModule,
    AlertError,
    Alert,
    TranslateDirective,
    TranslateModule,
    KeyValuePipe,
    FilterComponent,
  ],
})
export class EpicRoadmap implements OnInit {
  subscription: Subscription | null = null;
  readonly epics = signal<IEpic[]>([]);
  readonly allTasks = signal<ITask[]>([]);
  readonly filterStatus = signal<string>('');
  readonly filterProject = signal<string>('');
  readonly filterPriority = signal<string>('');
  readonly sortBy = signal<string>('');

  filteredEpics = computed(() => {
    let list = this.epics();
    const statusFilter = this.filterStatus();
    const projectFilter = this.filterProject();
    const priorityFilter = this.filterPriority();
    const sortField = this.sortBy();
    if (statusFilter) {
      list = list.filter(e => e.status === statusFilter);
    }
    if (projectFilter) {
      list = list.filter(e => e.project?.name === projectFilter || e.project?.id?.toString() === projectFilter);
    }
    if (priorityFilter) {
      list = list.filter(e => e.priority === priorityFilter);
    }
    if (sortField) {
      const [field, dir] = sortField.split(':') as [string, string];
      list = [...list].sort((a, b) => {
        const aVal = String((a as any)[field] ?? '');
        const bVal = String((b as any)[field] ?? '');
        const cmp = aVal.localeCompare(bVal);
        return dir === 'desc' ? -cmp : cmp;
      });
    }
    return list;
  });

  readonly epicsWithProgress = computed<EpicWithProgress[]>(() => {
    const tasks = this.allTasks();
    return this.filteredEpics().map(e => {
      const epicTasks = tasks.filter(t => t.epic?.id === e.id);
      const total = epicTasks.length;
      const done = epicTasks.filter(t => t.status === 'DONE').length;
      return {
        ...e,
        progress: total > 0 ? Math.round((done / total) * 100) : 0,
        totalIssues: total,
        doneIssues: done,
      };
    });
  });

  readonly hasDateRange = computed(() => this.epics().some(e => e.startDate && e.endDate));

  readonly sortState = sortStateSignal({});
  readonly filters: IFilterOptions = new FilterOptions();
  readonly itemsPerPage = signal(ITEMS_PER_PAGE);
  readonly totalItems = signal(0);
  readonly page = signal(1);

  protected readonly uniqueProjects = computed(() => {
    const names = new Set<string>();
    for (const e of this.epics()) {
      if (e.project?.name) {
        names.add(e.project.name);
      }
    }
    return Array.from(names).sort();
  });

  readonly currentProjectKey = signal<string | null>(null);
  readonly currentProject = signal<IProject | null>(null);

  readonly router = inject(Router);
  readonly epicService = inject(EpicService);
  readonly taskService = inject(TaskService);
  protected readonly projectService = inject(ProjectService);
  readonly isLoading = this.epicService.epicsResource.isLoading;
  readonly statusColors = STATUS_COLORS;
  protected readonly activatedRoute = inject(ActivatedRoute);
  protected readonly sortService = inject(SortService);
  protected readonly filterOptions = toSignal(this.filters.filterChanges);

  constructor() {
    effect(() => {
      const headers = this.epicService.epicsResource.headers();
      if (headers) {
        this.fillComponentAttributesFromResponseHeader(headers);
      }
    });
    effect(() => {
      this.epics.set(this.fillComponentAttributesFromResponseBody([...this.epicService.epics()]));
    });
    effect(() => {
      const tasks = this.taskService.tasks();
      if (tasks) {
        this.allTasks.set(tasks);
      }
    });
    effect(() => {
      const epics = this.epics();
      if (epics.length > 0) {
        const projectId = epics[0]?.project?.id;
        if (projectId) {
          this.taskService.tasksParams.set({
            'projectId.equals': projectId,
            size: 500,
          });
        }
      }
    });
    effect(() => {
      const filterOptions = this.filterOptions();
      if (filterOptions) {
        untracked(() => {
          this.handleNavigation(1, this.sortState(), filterOptions);
        });
      }
    });
  }

  trackId = (item: IEpic): number => this.epicService.getEpicIdentifier(item);

  ngOnInit(): void {
    this.resolveProjectContext();
    this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
      .pipe(
        tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
        tap(() => this.load()),
      )
      .subscribe();
  }

  load(): void {
    this.queryBackend();
  }

  getBarStyle(epic: EpicWithProgress): Record<string, string> {
    const pct = epic.progress;
    return {
      width: `${pct}%`,
      background: this.statusColors[epic.status ?? 'TODO'] ?? 'var(--color-primary)',
    };
  }

  getBarRadius(epic: EpicWithProgress): Record<string, string> {
    const pct = epic.progress;
    return {
      borderRadius: pct >= 100 ? '6px' : '6px 0 0 6px',
    };
  }

  getEpicDates(epic: IEpic): string {
    if (epic.startDate && epic.endDate) {
      return `${epic.startDate.format('MMM D')} - ${epic.endDate.format('MMM D')}`;
    }
    if (epic.createdAt) {
      return epic.createdAt.format('MMM D');
    }
    return '';
  }

  protected fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    const page = params.get(PAGE_HEADER);
    this.page.set(+(page ?? 1));
    this.sortState.set(this.sortService.parseSortParam(params.get(SORT) ?? data[DEFAULT_SORT_DATA]));
    this.filters.initializeFromParams(params);
  }

  protected fillComponentAttributesFromResponseBody(data: IEpic[]): IEpic[] {
    return data;
  }

  protected fillComponentAttributesFromResponseHeader(headers: HttpHeaders): void {
    this.totalItems.set(Number(headers.get(TOTAL_COUNT_RESPONSE_HEADER)));
  }

  protected queryBackend(): void {
    const pageToLoad: number = this.page();
    const queryObject: any = {
      page: pageToLoad - 1,
      size: this.itemsPerPage(),
      eagerload: true,
      sort: this.sortService.buildSortParam(this.sortState()),
    };
    for (const filterOption of this.filters.filterOptions) {
      queryObject[filterOption.name] = filterOption.values;
    }
    if (this.currentProject()) {
      queryObject['projectId.equals'] = this.currentProject()!.id;
    }
    this.epicService.epicsParams.set(queryObject);
  }

  private resolveProjectContext(): void {
    let route: ActivatedRoute | null = this.activatedRoute;
    while (route) {
      const key = route.snapshot.paramMap.get('key');
      if (key) {
        this.currentProjectKey.set(key);
        this.projectService.findByKey(key).subscribe(project => this.currentProject.set(project));
        return;
      }
      route = route.parent;
    }
  }

  protected handleNavigation(page: number, sortState: SortState, filterOptions?: IFilterOption[]): void {
    const queryParamsObj: any = {
      page,
      size: this.itemsPerPage(),
      sort: this.sortService.buildSortParam(sortState),
    };
    if (filterOptions) {
      for (const filterOption of filterOptions) {
        queryParamsObj[filterOption.nameAsQueryParam()] = filterOption.values;
      }
    }
    this.router.navigate(['./'], { relativeTo: this.activatedRoute, queryParams: queryParamsObj });
  }
}
