import { HttpHeaders } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, computed, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Data, ParamMap, Router, RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { NgbPagination } from '@ng-bootstrap/ng-bootstrap/pagination';
import { TranslateModule } from '@ngx-translate/core';
import { Subscription, combineLatest, tap } from 'rxjs';

import { AccountService } from 'app/core/auth/account.service';
import { CsvDownloadService } from 'app/shared/csv/csv-download.service';
import { DEFAULT_SORT_DATA, ITEM_DELETED_EVENT, SORT } from 'app/config/navigation.constants';
import { ITEMS_PER_PAGE, PAGE_HEADER, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { Alert } from 'app/shared/alert/alert';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { ItemCount } from 'app/shared/pagination';
import { SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { ProjectRole } from 'app/entities/enumerations/project-role.model';
import { ProjectDeleteDialog } from '../delete/project-delete-dialog';
import { IProject, IProjectCardStats } from '../project.model';
import { ProjectService } from '../service/project.service';
import { ProjectCard } from './project-card';

type ProjectScope = 'mine' | 'all';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-project',
  templateUrl: './project.html',
  styleUrl: './project.scss',
  imports: [
    RouterLink,
    FormsModule,
    FontAwesomeModule,
    AlertError,
    Alert,
    TranslateDirective,
    TranslateModule,
    NgbPagination,
    ItemCount,
    ProjectCard,
  ],
})
export class Project implements OnInit {
  subscription: Subscription | null = null;
  readonly projects = signal<IProject[]>([]);

  sortState = sortStateSignal({});

  readonly itemsPerPage = signal(ITEMS_PER_PAGE);
  readonly totalItems = signal(0);
  readonly page = signal(1);
  readonly error = signal<string | null>(null);
  readonly scope = signal<ProjectScope>('all');
  readonly cardStats = signal<Map<number, IProjectCardStats>>(new Map());
  readonly userProjectRoles = signal<Map<number, ProjectRole>>(new Map());

  private readonly csvDownloadService = inject(CsvDownloadService);
  readonly router = inject(Router);
  readonly projectService = inject(ProjectService);
  readonly isLoading = this.projectService.projectsResource.isLoading;
  readonly activatedRoute = inject(ActivatedRoute);
  readonly sortService = inject(SortService);
  private readonly accountService = inject(AccountService);
  private readonly modalService = inject(NgbModal);

  readonly isAdmin = computed(() => {
    const account = this.accountService.account();
    return !!account?.authorities?.includes('ROLE_ADMIN');
  });

  readonly isProjectManager = computed(() => {
    const account = this.accountService.account();
    return !!account?.authorities?.includes('ROLE_PROJET_MANAGER');
  });

  readonly canToggleScope = computed(() => this.isAdmin() || this.isProjectManager());

  constructor() {
    effect(() => {
      const headers = this.projectService.projectsResource.headers();
      if (headers) {
        this.fillComponentAttributesFromResponseHeader(headers);
      }
    });
    effect(() => {
      this.projects.set(this.fillComponentAttributesFromResponseBody([...this.projectService.projects()]));
      this.loadCardStats();
    });
    effect(() => {
      const err = this.projectService.projectsResource.error();
      this.error.set(err ? 'error.projects.loadFailed' : null);
    });
  }

  trackId = (item: IProject): number => this.projectService.getProjectIdentifier(item);

  exportCsv(): void {
    this.csvDownloadService.download('api/export/csv/projects', 'projects.csv');
  }

  ngOnInit(): void {
    this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
      .pipe(
        tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
        tap(() => this.load()),
      )
      .subscribe();
    this.loadUserProjectRoles();
  }

  load(): void {
    this.queryBackend();
    this.projectService.refresh();
  }

  setScope(scope: ProjectScope): void {
    if (this.scope() === scope) {
      return;
    }
    this.scope.set(scope);
    this.load();
  }

  canManage(project: IProject): boolean {
    if (this.isAdmin() || this.isProjectManager()) {
      return true;
    }
    const role = this.userProjectRoles().get(project.id);
    return role === ProjectRole.OWNER || role === ProjectRole.MANAGER;
  }

  // Deletion is stricter than edit: OWNER only (matches
  // ProjectService.delete()'s requireProjectRole(id, ProjectRole.OWNER)).
  canDelete(project: IProject): boolean {
    if (this.isAdmin() || this.isProjectManager()) {
      return true;
    }
    return this.userProjectRoles().get(project.id) === ProjectRole.OWNER;
  }

  statsFor(project: IProject): IProjectCardStats | undefined {
    return this.cardStats().get(project.id);
  }

  onDeleteProject(project: IProject): void {
    const modalRef = this.modalService.open(ProjectDeleteDialog, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.project = project;
    modalRef.closed
      .pipe(
        tap(reason => {
          if (reason === ITEM_DELETED_EVENT) {
            this.projectService.refresh();
            this.load();
          }
        }),
      )
      .subscribe();
  }

  navigateToPage(page: number): void {
    this.handleNavigation(page, this.sortState());
  }

  protected loadUserProjectRoles(): void {
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

  protected loadCardStats(): void {
    if (this.projects().length === 0) {
      return;
    }
    this.projectService.getProjectCardStats().subscribe(stats => {
      const map = new Map<number, IProjectCardStats>();
      for (const s of stats) {
        map.set(s.projectId, s);
      }
      this.cardStats.set(map);
    });
  }

  protected fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    const page = params.get(PAGE_HEADER);
    this.page.set(+(page ?? 1));
    this.sortState.set(this.sortService.parseSortParam(params.get(SORT) ?? data[DEFAULT_SORT_DATA]));
  }

  protected fillComponentAttributesFromResponseBody(data: IProject[]): IProject[] {
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
      sort: this.sortService.buildSortParam(this.sortState()),
      scope: this.scope(),
    };
    this.projectService.projectsParams.set(queryObject);
  }

  protected handleNavigation(page: number, sortState: SortState): void {
    const queryParamsObj = {
      page,
      size: this.itemsPerPage(),
      sort: this.sortService.buildSortParam(sortState),
    };

    this.router.navigate(['./'], {
      relativeTo: this.activatedRoute,
      queryParams: queryParamsObj,
    });
  }
}
