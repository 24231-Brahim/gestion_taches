import { ChangeDetectionStrategy, Component, OnDestroy, effect, inject, signal } from '@angular/core';
import { DatePipe, NgOptimizedImage } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbDropdown, NgbDropdownMenu, NgbDropdownToggle } from '@ng-bootstrap/ng-bootstrap/dropdown';
import { TranslateModule } from '@ngx-translate/core';
import { environment } from 'environments/environment';

import { AccountService } from 'app/core/auth/account.service';

import { INotification, NotificationService } from 'app/core/util/notification.service';
import { EntityEventService } from 'app/core/util/entity-event.service';
import { SearchService } from 'app/layouts/search/search.service';

import { TranslateDirective } from 'app/shared/language';

@Component({
  selector: 'jhi-navbar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
  imports: [
    RouterLink,
    RouterLinkActive,
    FontAwesomeModule,
    NgbDropdown,
    NgbDropdownMenu,
    NgbDropdownToggle,
    TranslateDirective,
    TranslateModule,
    DatePipe,
    NgOptimizedImage,
  ],
})
export default class Navbar implements OnDestroy {
  readonly isNavbarCollapsed = signal(true);
  readonly version: string;
  readonly account = inject(AccountService).account;
  readonly notificationService = inject(NotificationService);
  readonly entityEventService = inject(EntityEventService);
  private readonly searchService = inject(SearchService);

  constructor() {
    const { VERSION } = environment;
    if (VERSION) {
      this.version = VERSION.toLowerCase().startsWith('v') ? VERSION : `v${VERSION}`;
    } else {
      this.version = '';
    }
    effect(() => {
      if (this.account() !== null) {
        this.notificationService.startPolling();
        this.entityEventService.connect();
      } else {
        this.notificationService.stopPolling();
        this.entityEventService.disconnect();
      }
    });
  }

  ngOnDestroy(): void {
    this.notificationService.stopPolling();
  }

  collapseNavbar(): void {
    this.isNavbarCollapsed.set(true);
  }

  openSearch(): void {
    this.searchService.open();
  }

  markNotificationRead(notification: INotification): void {
    if (!notification.isRead) {
      this.notificationService.markAsRead(notification.id).subscribe(() => {
        this.notificationService.refresh();
      });
    }
  }

  toggleMobileSidebar(): void {
    document.body.classList.toggle('sidebar-mobile-open');
  }
}
