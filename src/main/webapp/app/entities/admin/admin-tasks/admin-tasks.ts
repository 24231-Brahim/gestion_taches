import { ChangeDetectionStrategy, Component, OnInit, computed, effect, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, ParamMap, Router, RouterLink } from '@angular/router';
import { HttpResponse } from '@angular/common/http';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbPagination } from '@ng-bootstrap/ng-bootstrap/pagination';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { ITEMS_PER_PAGE, PAGE_HEADER, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { Alert } from 'app/shared/alert/alert';
import { AlertError } from 'app/shared/alert/alert-error';
import { Filter, FilterOptions, IFilterOption, IFilterOptions } from 'app/shared/filter';
import { SortByDirective, SortDirective, SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { TranslateDirective } from 'app/shared/language';
import { ItemCount } from 'app/shared/pagination';
import { TaskService } from 'app/entities/task/service/task.service';
import { ITask } from 'app/entities/task/task.model';
import { STATUS_BADGES, PRIORITY_COLORS, StatusBadge } from 'app/entities/task/task-helper';

@Component({
  selector: 'jhi-admin-tasks',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './admin-tasks.html',
  imports: [
    RouterLink,
    FormsModule,
    FontAwesomeModule,
    AlertError,
    Alert,
    SortDirective,
    SortByDirective,
    Filter,
    NgbPagination,
    TranslateDirective,
    TranslateModule,
    ItemCount,
    DatePipe,
  ],
})
export class AdminTasks implements OnInit {
  readonly tasks = signal<ITask[]>([]);
  readonly isLoading = signal(false);
  readonly totalItems = signal(0);
  readonly page = signal(1);
  readonly itemsPerPage = signal(ITEMS_PER_PAGE);
  readonly searchInput = signal('');
  readonly searchQuery = signal('');

  private debounceSearch = effect(() => {
    const value = this.searchInput();
    const handle = window.setTimeout(() => this.searchQuery.set(value.trim()), 250);
    return () => window.clearTimeout(handle);
  });

  readonly statusBadges = STATUS_BADGES;
  readonly priorityColors = PRIORITY_COLORS;
  sortState = sortStateSignal({});
  filters: IFilterOptions = new FilterOptions();
  filteredTasks = computed(() => {
    const q = this.searchQuery().toLowerCase();
    if (!q) {
      return this.tasks();
    }
    return this.tasks().filter(i => i.title?.toLowerCase().includes(q));
  });

  private readonly taskService = inject(TaskService);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly sortService = inject(SortService);
  private readonly translateService = inject(TranslateService);

  ngOnInit(): void {
    this.activatedRoute.queryParamMap.subscribe((params: ParamMap) => {
      this.fillComponentAttributeFromRoute(params);
      this.loadAll();
    });
  }

  loadAll(): void {
    this.isLoading.set(true);
    const queryObject: any = {
      page: this.page() - 1,
      size: this.itemsPerPage(),
      sort: this.sortService.buildSortParam(this.sortState()),
    };
    for (const filterOption of this.filters.filterOptions) {
      queryObject[filterOption.name] = filterOption.values;
    }
    this.taskService.query(queryObject).subscribe({
      next: (res: HttpResponse<ITask[]>) => {
        this.isLoading.set(false);
        this.totalItems.set(Number(res.headers.get(TOTAL_COUNT_RESPONSE_HEADER)));
        this.tasks.set(res.body ?? []);
      },
      error: () => this.isLoading.set(false),
    });
  }

  transition(): void {
    this.loadAll();
  }

  navigateToWithComponentValues(event: SortState): void {
    this.handleNavigation(this.page(), event, this.filters.filterOptions);
  }

  navigateToPage(page: number): void {
    this.handleNavigation(page, this.sortState(), this.filters.filterOptions);
  }

  getStatusColor(status: string | null | undefined): string {
    if (!status) return 'var(--color-muted)';
    const badge: StatusBadge | undefined = (STATUS_BADGES as Record<string, StatusBadge | undefined>)[status];
    return badge ? badge.color : 'var(--color-muted)';
  }

  getPriorityColor(priority: string | null | undefined): string {
    return PRIORITY_COLORS[priority ?? ''] ?? 'var(--color-muted)';
  }

  getStatusLabel(status: string | null | undefined): string {
    if (!status) return '';
    return this.translateEnumKey('gestionTachesApp.TaskStatus', status);
  }

  getPriorityLabel(priority: string | null | undefined): string {
    if (!priority) return '';
    return this.translateEnumKey('gestionTachesApp.Priority', priority);
  }

  private translateEnumKey(prefix: string, value: string): string {
    const key = `${prefix}.${value}`;
    const translated = this.translateService.instant(key);
    return translated === key ? value : translated;
  }

  protected fillComponentAttributeFromRoute(params: ParamMap): void {
    const page = params.get(PAGE_HEADER);
    this.page.set(+(page ?? 1));
    this.sortState.set(this.sortService.parseSortParam(params.get('sort') ?? 'createdAt,desc'));
    this.filters.initializeFromParams(params);
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
