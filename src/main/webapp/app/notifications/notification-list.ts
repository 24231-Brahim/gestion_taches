import { HttpHeaders } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbPagination } from '@ng-bootstrap/ng-bootstrap/pagination';
import { TranslateModule } from '@ngx-translate/core';

import { NotificationService, INotification } from 'app/core/util/notification.service';
import { ItemCount } from 'app/shared/pagination';
import { ITEMS_PER_PAGE } from 'app/config/pagination.constants';

@Component({
  selector: 'jhi-notification-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './notification-list.html',
  styleUrl: './notification-list.scss',
  imports: [FontAwesomeModule, TranslateModule, DatePipe, NgbPagination, ItemCount],
})
export default class NotificationListComponent implements OnInit {
  readonly notifications = signal<INotification[]>([]);
  readonly page = signal(0);
  readonly size = signal(ITEMS_PER_PAGE);
  readonly totalItems = signal(0);
  readonly notificationService = inject(NotificationService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    this.loadPage();
  }

  loadPage(page?: number): void {
    const p = page ?? this.page();
    this.notificationService.getNotificationsPaginated(p, this.size()).subscribe(res => {
      this.notifications.set(res.body ?? []);
      this.totalItems.set(Number(res.headers.get('X-Total-Count') ?? 0));
      this.page.set(p);
    });
  }

  markAsRead(notification: INotification): void {
    if (!notification.isRead) {
      this.notificationService.markAsRead(notification.id).subscribe(() => {
        this.loadPage();
      });
    }
    if (notification.issueId) {
      this.router.navigate(['/issue', notification.issueId]);
    }
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe(() => {
      this.loadPage();
    });
  }
}
