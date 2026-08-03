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
import { INotification } from 'app/core/util/notification.service';
import { AdminNotificationService } from './admin-notifications.service';

@Component({
  selector: 'jhi-admin-notifications',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './admin-notifications.html',
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
export class AdminNotifications implements OnInit {
  readonly notifications = signal<INotification[]>([]);
  readonly isLoading = signal(false);
  readonly totalItems = signal(0);
  readonly page = signal(1);
  readonly itemsPerPage = signal(ITEMS_PER_PAGE);
  readonly searchInput = signal('');
  readonly searchQuery = signal('');

  sortState = sortStateSignal({});
  filteredNotifications = computed(() => {
    const q = this.searchQuery().toLowerCase();
    if (!q) {
      return this.notifications();
    }
    return this.notifications().filter(
      n =>
        n.message.toLowerCase().includes(q) ||
        (n.userLogin?.toLowerCase().includes(q) ?? false) ||
        (n.taskTitle?.toLowerCase().includes(q) ?? false) ||
        (n.projectKey?.toLowerCase().includes(q) ?? false),
    );
  });

  private debounceSearch = effect(() => {
    const value = this.searchInput();
    const handle = window.setTimeout(() => this.searchQuery.set(value.trim()), 250);
    return () => window.clearTimeout(handle);
  });

  private readonly notificationService = inject(AdminNotificationService);
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
    this.notificationService.query(queryObject).subscribe({
      next: (res: HttpResponse<INotification[]>) => {
        this.isLoading.set(false);
        this.totalItems.set(Number(res.headers.get(TOTAL_COUNT_RESPONSE_HEADER)));
        this.notifications.set(res.body ?? []);
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
    this.sortState.set(this.sortService.parseSortParam(params.get('sort') ?? 'createdAt,desc'));
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
