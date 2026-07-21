import { HttpHeaders } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, effect, inject, signal, untracked } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Data, ParamMap, Router, RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { NgbPagination } from '@ng-bootstrap/ng-bootstrap/pagination';
import { TranslateModule } from '@ngx-translate/core';
import { Subscription, combineLatest, switchMap, tap } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { CsvDownloadService } from 'app/shared/csv/csv-download.service';
import { DEFAULT_SORT_DATA, ITEM_DELETED_EVENT, SORT } from 'app/config/navigation.constants';
import { ITEMS_PER_PAGE, PAGE_HEADER, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { AccountService } from 'app/core/auth/account.service';
import { Alert } from 'app/shared/alert/alert';
import { AlertError } from 'app/shared/alert/alert-error';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { Filter, FilterOptions, IFilterOption, IFilterOptions } from 'app/shared/filter';
import { TranslateDirective } from 'app/shared/language';
import { ItemCount } from 'app/shared/pagination';
import { SortByDirective, SortDirective, SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { ProjectRole } from 'app/entities/enumerations/project-role.model';
import { TaskStatus } from 'app/entities/enumerations/task-status.model';
import { IProject } from 'app/entities/project/project.model';
import { ProjectService } from 'app/entities/project/service/project.service';
import { TaskDeleteDialog } from '../delete/task-delete-dialog';
import { TaskDetailPanel } from '../detail/task-detail-panel';
import { TaskKanbanBoard } from '../kanban/task-kanban-board';
import { ISSUE_TYPE_COLORS, ISSUE_TYPE_ICONS, PRIORITY_COLORS, PRIORITY_ICONS, STATUS_BADGES, ViewMode } from '../task-helper';
import { ITask } from '../task.model';
import { TaskService } from '../service/task.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-task',
  templateUrl: './task.html',
  styles: [
    `
      .task-page-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 16px;
        gap: 12px;
        flex-wrap: wrap;
      }
      .task-toolbar {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 16px;
        flex-wrap: wrap;
      }
      .search-box {
        position: relative;
        flex: 1;
        min-width: 200px;
      }
      .search-box .search-icon {
        position: absolute;
        left: 10px;
        top: 50%;
        transform: translateY(-50%);
        color: var(--color-text-muted, #6a8fac);
        font-size: 0.85rem;
      }
      .search-box input {
        padding-left: 30px;
      }
      .view-mode-tabs {
        display: flex;
        gap: 0;
        border: 1px solid var(--color-outline-variant, #2a3038);
        border-radius: var(--radius-md);
        overflow: hidden;
      }
      .view-mode-tabs .btn {
        border: none;
        border-radius: 0;
        padding: 6px 14px;
        background: transparent;
        color: var(--color-text-muted, #6a8fac);
        font-size: 0.85rem;
        font-family: var(--font-inter);
      }
      .view-mode-tabs .btn-active {
        background: var(--color-primary, #97cbff);
        color: #000;
      }
      .status-badge {
        display: inline-block;
        padding: 2px 10px;
        border-radius: 9999px;
        font-size: 0.75rem;
        color: #fff;
        font-weight: 600;
        font-family: var(--font-inter);
      }
      .task-row {
        cursor: pointer;
        transition: background-color var(--transition-fast);
      }
      .task-row:hover {
        background: var(--color-surface-container, #1b2025);
      }
      .task-title-cell {
        font-weight: 500;
        max-width: 300px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .assignee-initials-sm {
        width: 24px;
        height: 24px;
        border-radius: 50%;
        background: var(--color-primary-container, #25a7fd);
        color: #000;
        font-size: 0.65rem;
        font-weight: 600;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        font-family: var(--font-mono);
      }
    `,
  ],
  imports: [
    RouterLink,
    FormsModule,
    FontAwesomeModule,
    AlertError,
    Alert,
    SortDirective,
    SortByDirective,
    TranslateDirective,
    TranslateModule,
    FormatMediumDatetimePipe,
    Filter,
    NgbPagination,
    ItemCount,
    TaskKanbanBoard,
    TaskDetailPanel,
  ],
})
export class Task implements OnInit {
  subscription: Subscription | null = null;
  readonly tasks = signal<ITask[]>([]);
  sortState = sortStateSignal({});
  filters: IFilterOptions = new FilterOptions();
  readonly itemsPerPage = signal(ITEMS_PER_PAGE);
  readonly totalItems = signal(0);
  readonly page = signal(1);
  readonly searchQuery = signal('');

  readonly currentProjectKey = signal<string | null>(null);
  readonly currentProject = signal<IProject | null>(null);

  readonly viewMode = signal<ViewMode>('list');
  readonly selectedTask = signal<ITask | null>(null);
  readonly drawerVisible = signal(false);

  private readonly csvDownloadService = inject(CsvDownloadService);

  filteredTasks = computed(() => {
    const q = this.searchQuery().toLowerCase();
    if (!q) {
      return this.tasks();
    }
    return this.tasks().filter(i => i.title?.toLowerCase().includes(q));
  });

  protected readonly appConfig = inject(ApplicationConfigService);
  readonly router = inject(Router);
  readonly taskService = inject(TaskService);
  readonly isLoading = this.taskService.tasksResource.isLoading;
  readonly typeColors = ISSUE_TYPE_COLORS;
  readonly typeIcons = ISSUE_TYPE_ICONS;
  readonly priorityColors = PRIORITY_COLORS;
  readonly priorityIcons = PRIORITY_ICONS;
  readonly statusBadges = STATUS_BADGES;
  readonly userProjectRoles = signal<Map<number, ProjectRole>>(new Map());
  readonly currentUserLogin = computed(() => this.accountService.account()?.login ?? null);
  protected readonly activatedRoute = inject(ActivatedRoute);
  protected readonly sortService = inject(SortService);
  protected readonly filterOptions = toSignal(this.filters.filterChanges);
  protected modalService = inject(NgbModal);
  protected readonly accountService = inject(AccountService);
  protected readonly projectService = inject(ProjectService);

  protected readonly destroyRef = inject(DestroyRef);

  constructor() {
    effect(() => {
      const headers = this.taskService.tasksResource.headers();
      if (headers) {
        this.fillComponentAttributesFromResponseHeader(headers);
      }
    });
    effect(() => {
      this.tasks.set(this.fillComponentAttributesFromResponseBody([...this.taskService.tasks()]));
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

  canEditTask(task: ITask): boolean {
    const role = this.userProjectRoles().get(task.project?.id ?? -1);
    if (!role) return false;
    return role === ProjectRole.OWNER || role === ProjectRole.MANAGER || task.createdBy?.login === this.currentUserLogin();
  }

  canDeleteTask(task: ITask): boolean {
    return this.canEditTask(task);
  }

  exportCsv(): void {
    const project = this.currentProject();
    if (project) {
      this.csvDownloadService.download(
        `api/export/csv/projects/${encodeURIComponent(project.id)}/tasks`,
        `project-${project.key}-tasks.csv`,
      );
    } else {
      this.csvDownloadService.download('api/export/csv/tasks', 'tasks.csv');
    }
  }

  trackId = (item: ITask): number => this.taskService.getTaskIdentifier(item);

  ngOnInit(): void {
    const parentParamMap = this.activatedRoute.parent?.paramMap ?? this.activatedRoute.paramMap;
    parentParamMap
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        tap(params => {
          const key = params.get('key');
          if (key) {
            this.currentProjectKey.set(key);
            this.projectService.findByKey(key).subscribe(project => this.currentProject.set(project));
          }
        }),
        switchMap(() => combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])),
        tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
        tap(() => this.load()),
      )
      .subscribe();
    this.loadUserProjectRoles();
  }

  loadUserProjectRoles(): void {
    this.projectService.getMyRoles().subscribe(members => {
      const map = new Map<number, ProjectRole>();
      for (const m of members) {
        if (m.projectId != null && m.role != null) {
          map.set(m.projectId, m.role);
        }
      }
      this.userProjectRoles.set(map);
    });
  }

  delete(task: ITask): void {
    const modalRef = this.modalService.open(TaskDeleteDialog, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.task = task;
    modalRef.closed
      .pipe(
        tap(reason => {
          if (reason === ITEM_DELETED_EVENT) {
            this.taskService.refresh();
            this.load();
          }
        }),
      )
      .subscribe();
  }

  load(): void {
    this.queryBackend();
    this.taskService.refresh();
  }

  onSelectTask(task: ITask): void {
    this.selectedTask.set(task);
    this.drawerVisible.set(true);
  }

  onCloseDrawer(): void {
    this.drawerVisible.set(false);
    this.selectedTask.set(null);
  }

  onTaskChanged(updated: ITask): void {
    this.tasks.update(list => list.map(t => (t.id === updated.id ? updated : t)));
    this.selectedTask.set(updated);
  }

  onKanbanStatusChange(event: { taskId: number; status: string }): void {
    this.tasks.update(list => list.map(t => (t.id === event.taskId ? { ...t, status: event.status as keyof typeof TaskStatus } : t)));
  }

  setViewMode(mode: ViewMode): void {
    this.viewMode.set(mode);
  }

  navigateToWithComponentValues(event: SortState): void {
    this.handleNavigation(this.page(), event, this.filters.filterOptions);
  }

  navigateToPage(page: number): void {
    this.handleNavigation(page, this.sortState(), this.filters.filterOptions);
  }

  protected fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    const page = params.get(PAGE_HEADER);
    this.page.set(+(page ?? 1));
    this.sortState.set(this.sortService.parseSortParam(params.get(SORT) ?? data[DEFAULT_SORT_DATA]));
    this.filters.initializeFromParams(params);
  }

  protected fillComponentAttributesFromResponseBody(data: ITask[]): ITask[] {
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
    const project = this.currentProject();
    if (project) {
      queryObject['projectId.equals'] = project.id;
    }
    for (const filterOption of this.filters.filterOptions) {
      queryObject[filterOption.name] = filterOption.values;
    }
    this.taskService.tasksParams.set(queryObject);
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
