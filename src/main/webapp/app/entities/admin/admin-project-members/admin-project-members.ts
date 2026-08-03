import { ChangeDetectionStrategy, Component, OnInit, computed, effect, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, ParamMap, Router, RouterLink } from '@angular/router';
import { HttpResponse } from '@angular/common/http';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbPagination } from '@ng-bootstrap/ng-bootstrap/pagination';
import { TranslateModule } from '@ngx-translate/core';

import { ITEMS_PER_PAGE, PAGE_HEADER, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { Alert } from 'app/shared/alert/alert';
import { AlertError } from 'app/shared/alert/alert-error';
import { SortByDirective, SortDirective, SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { TranslateDirective } from 'app/shared/language';
import { ItemCount } from 'app/shared/pagination';
import { IProjectMember } from 'app/entities/project/project.model';
import { AdminProjectMemberService } from './admin-project-members.service';

@Component({
  selector: 'jhi-admin-project-members',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './admin-project-members.html',
  imports: [
    RouterLink,
    FormsModule,
    FontAwesomeModule,
    AlertError,
    Alert,
    SortDirective,
    SortByDirective,
    NgbPagination,
    TranslateDirective,
    TranslateModule,
    ItemCount,
    DatePipe,
  ],
})
export class AdminProjectMembers implements OnInit {
  readonly members = signal<IProjectMember[]>([]);
  readonly isLoading = signal(false);
  readonly totalItems = signal(0);
  readonly page = signal(1);
  readonly itemsPerPage = signal(ITEMS_PER_PAGE);
  readonly searchInput = signal('');
  readonly searchQuery = signal('');

  sortState = sortStateSignal({});
  filteredMembers = computed(() => {
    const q = this.searchQuery().toLowerCase();
    if (!q) {
      return this.members();
    }
    return this.members().filter(
      m =>
        (m.userLogin?.toLowerCase().includes(q) ?? false) ||
        (m.projectName?.toLowerCase().includes(q) ?? false) ||
        (m.projectKey?.toLowerCase().includes(q) ?? false) ||
        (m.role?.toLowerCase().includes(q) ?? false),
    );
  });

  private debounceSearch = effect(() => {
    const value = this.searchInput();
    const handle = window.setTimeout(() => this.searchQuery.set(value.trim()), 250);
    return () => window.clearTimeout(handle);
  });

  private readonly memberService = inject(AdminProjectMemberService);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly sortService = inject(SortService);

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
    this.memberService.query(queryObject).subscribe({
      next: (res: HttpResponse<IProjectMember[]>) => {
        this.isLoading.set(false);
        this.totalItems.set(Number(res.headers.get(TOTAL_COUNT_RESPONSE_HEADER)));
        this.members.set(res.body ?? []);
      },
      error: () => this.isLoading.set(false),
    });
  }

  transition(): void {
    this.loadAll();
  }

  navigateToWithComponentValues(event: SortState): void {
    this.handleNavigation(this.page(), event);
  }

  navigateToPage(page: number): void {
    this.handleNavigation(page, this.sortState());
  }

  protected fillComponentAttributeFromRoute(params: ParamMap): void {
    const page = params.get(PAGE_HEADER);
    this.page.set(+(page ?? 1));
    this.sortState.set(this.sortService.parseSortParam(params.get('sort') ?? 'joinedAt,desc'));
  }

  protected handleNavigation(page: number, sortState: SortState): void {
    const queryParamsObj: any = {
      page,
      size: this.itemsPerPage(),
      sort: this.sortService.buildSortParam(sortState),
    };
    this.router.navigate(['./'], { relativeTo: this.activatedRoute, queryParams: queryParamsObj });
  }
}
