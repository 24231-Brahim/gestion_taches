import { HttpHeaders } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, computed, effect, inject, signal, untracked } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Data, ParamMap, Router, RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { NgbPagination } from '@ng-bootstrap/ng-bootstrap/pagination';
import { TranslateModule } from '@ngx-translate/core';
import { Subscription, combineLatest, tap } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
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
import { ProjectService } from 'app/entities/project/service/project.service';
import { IssueDeleteDialog } from '../delete/issue-delete-dialog';
import { IssueDetailPanel } from '../detail/issue-detail-panel';
import { IssueKanbanBoard } from '../kanban/issue-kanban-board';
import { ISSUE_TYPE_COLORS, ISSUE_TYPE_ICONS, PRIORITY_COLORS, PRIORITY_ICONS, STATUS_BADGES, ViewMode } from '../issue-helper';
import { IIssue } from '../issue.model';
import { IssueService } from '../service/issue.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-issue',
  templateUrl: './issue.html',
  styles: [
    `
      .issue-page-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 16px;
        gap: 12px;
        flex-wrap: wrap;
      }
      .issue-toolbar {
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
        border: 2px solid var(--color-outline-variant, #2a3038);
        border-radius: 6px;
        overflow: hidden;
      }
      .view-mode-tabs .btn {
        border: none;
        border-radius: 0;
        padding: 6px 14px;
        background: transparent;
        color: var(--color-text-muted, #6a8fac);
        font-size: 0.85rem;
      }
      .view-mode-tabs .btn-active {
        background: var(--color-primary, #97cbff);
        color: #000;
      }
      .status-badge {
        display: inline-block;
        padding: 2px 10px;
        border-radius: 10px;
        font-size: 0.75rem;
        color: #fff;
        font-weight: 600;
      }
      .issue-row {
        cursor: pointer;
        transition: background 0.15s;
      }
      .issue-row:hover {
        background: var(--color-surface-container, #1b2025);
      }
      .issue-title-cell {
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
        font-weight: 700;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        font-family: 'JetBrains Mono', monospace;
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
    IssueKanbanBoard,
    IssueDetailPanel,
  ],
})
export class Issue implements OnInit {
  subscription: Subscription | null = null;
  readonly issues = signal<IIssue[]>([]);
  sortState = sortStateSignal({});
  filters: IFilterOptions = new FilterOptions();
  readonly itemsPerPage = signal(ITEMS_PER_PAGE);
  readonly totalItems = signal(0);
  readonly page = signal(1);
  readonly searchQuery = signal('');

  readonly viewMode = signal<ViewMode>('list');
  readonly selectedIssue = signal<IIssue | null>(null);
  readonly drawerVisible = signal(false);

  filteredIssues = computed(() => {
    const q = this.searchQuery().toLowerCase();
    if (!q) {
      return this.issues();
    }
    return this.issues().filter(i => i.title?.toLowerCase().includes(q));
  });

  readonly csvExportUrl: string;
  readonly router = inject(Router);
  readonly issueService = inject(IssueService);
  readonly isLoading = this.issueService.issuesResource.isLoading;
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

  constructor() {
    const appConfig = inject(ApplicationConfigService);
    this.csvExportUrl = appConfig.getEndpointFor('api/export/csv/issues');
    effect(() => {
      const headers = this.issueService.issuesResource.headers();
      if (headers) {
        this.fillComponentAttributesFromResponseHeader(headers);
      }
    });
    effect(() => {
      this.issues.set(this.fillComponentAttributesFromResponseBody([...this.issueService.issues()]));
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

  canEditIssue(issue: IIssue): boolean {
    const role = this.userProjectRoles().get(issue.project?.id ?? -1);
    if (!role) return false;
    return role === ProjectRole.OWNER || role === ProjectRole.MANAGER || issue.createdBy?.login === this.currentUserLogin();
  }

  canDeleteIssue(issue: IIssue): boolean {
    return this.canEditIssue(issue);
  }

  trackId = (item: IIssue): number => this.issueService.getIssueIdentifier(item);

  ngOnInit(): void {
    this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
      .pipe(
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

  delete(issue: IIssue): void {
    const modalRef = this.modalService.open(IssueDeleteDialog, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.issue = issue;
    modalRef.closed.pipe(tap(reason => (reason === ITEM_DELETED_EVENT ? this.load() : undefined))).subscribe();
  }

  load(): void {
    this.queryBackend();
  }

  onSelectIssue(issue: IIssue): void {
    this.selectedIssue.set(issue);
    this.drawerVisible.set(true);
  }

  onCloseDrawer(): void {
    this.drawerVisible.set(false);
    this.selectedIssue.set(null);
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

  protected fillComponentAttributesFromResponseBody(data: IIssue[]): IIssue[] {
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
    this.issueService.issuesParams.set(queryObject);
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
