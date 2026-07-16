import { HttpHeaders } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, effect, inject, signal, untracked } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Data, ParamMap, Router, RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { NgbPagination } from '@ng-bootstrap/ng-bootstrap/pagination';
import { TranslateModule } from '@ngx-translate/core';
import { Subscription, combineLatest, filter, tap } from 'rxjs';

import { DEFAULT_SORT_DATA, ITEM_DELETED_EVENT, SORT } from 'app/config/navigation.constants';
import { ITEMS_PER_PAGE, PAGE_HEADER, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { IProject } from 'app/entities/project/project.model';
import { ProjectService } from 'app/entities/project/service/project.service';
import { Alert } from 'app/shared/alert/alert';
import { AlertError } from 'app/shared/alert/alert-error';
import { FormatMediumDatePipe, FormatMediumDatetimePipe } from 'app/shared/date';
import { Filter, FilterOptions, IFilterOption, IFilterOptions } from 'app/shared/filter';
import { TranslateDirective } from 'app/shared/language';
import { ItemCount } from 'app/shared/pagination';
import { SortByDirective, SortDirective, SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { EpicDeleteDialog } from '../delete/epic-delete-dialog';
import { IEpic } from '../epic.model';
import { EpicService } from '../service/epic.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-epic',
  templateUrl: './epic.html',
  styles: [
    `
      .status-badge {
        display: inline-block;
        padding: 2px 10px;
        border: 2px solid var(--color-outline-variant, #2a3038);
        font-size: 0.75rem;
        font-family: 'JetBrains Mono', monospace;
        text-transform: uppercase;
        background: var(--color-surface-container, #1b2025);
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
    FormatMediumDatePipe,
    Filter,
    NgbPagination,
    ItemCount,
  ],
})
export class Epic implements OnInit {
  subscription: Subscription | null = null;
  readonly epics = signal<IEpic[]>([]);

  sortState = sortStateSignal({});
  filters: IFilterOptions = new FilterOptions();

  readonly itemsPerPage = signal(ITEMS_PER_PAGE);
  readonly totalItems = signal(0);
  readonly page = signal(1);

  readonly currentProjectKey = signal<string | null>(null);
  readonly currentProject = signal<IProject | null>(null);

  readonly router = inject(Router);
  protected readonly epicService = inject(EpicService);
  protected readonly projectService = inject(ProjectService);
  // eslint-disable-next-line @typescript-eslint/member-ordering
  readonly isLoading = this.epicService.epicsResource.isLoading;
  protected readonly activatedRoute = inject(ActivatedRoute);
  protected readonly sortService = inject(SortService);
  protected readonly filterOptions = toSignal(this.filters.filterChanges);
  protected modalService = inject(NgbModal);

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
      const filterOptions = this.filterOptions();
      if (filterOptions) {
        untracked(() => {
          // Only watch for filter changes. Other signals should be ignored.
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

  delete(epic: IEpic): void {
    const modalRef = this.modalService.open(EpicDeleteDialog, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.epic = epic;
    // unsubscribe not needed because closed completes on modal close
    modalRef.closed
      .pipe(
        filter(reason => reason === ITEM_DELETED_EVENT),
        tap(() => this.load()),
      )
      .subscribe();
  }

  load(): void {
    this.queryBackend();
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

  getStatusColor(status: string | null | undefined): string {
    const colors: Record<string, string> = {
      TODO: 'var(--color-status-todo, #2196f3)',
      IN_PROGRESS: 'var(--color-status-in-progress, #ff9800)',
      DONE: 'var(--color-status-done, #4caf50)',
      CANCELLED: 'var(--color-status-cancelled, #f44336)',
    };
    return colors[status ?? ''] ?? 'var(--color-outline-variant)';
  }

  getPriorityColor(priority: string | null | undefined): string {
    const colors: Record<string, string> = {
      LOWEST: 'var(--color-priority-lowest, #9e9e9e)',
      LOW: 'var(--color-priority-low, #607d8b)',
      MEDIUM: 'var(--color-priority-medium, #2196f3)',
      HIGH: 'var(--color-priority-high, #ff9800)',
      HIGHEST: 'var(--color-priority-highest, #f44336)',
    };
    return colors[priority ?? ''] ?? 'var(--color-outline-variant)';
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

    this.router.navigate(['./'], {
      relativeTo: this.activatedRoute,
      queryParams: queryParamsObj,
    });
  }
}
