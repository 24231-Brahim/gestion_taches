import { ChangeDetectionStrategy, Component, OnDestroy, effect, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbDropdown, NgbDropdownMenu, NgbDropdownToggle } from '@ng-bootstrap/ng-bootstrap/dropdown';
import { TranslateModule } from '@ngx-translate/core';
import { environment } from 'environments/environment';

import { AccountService } from 'app/core/auth/account.service';

import { NotificationService } from 'app/core/util/notification.service';

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
  ],
})
export default class Navbar implements OnDestroy {
  readonly isNavbarCollapsed = signal(true);
  readonly version: string;
  readonly account = inject(AccountService).account;
  readonly notificationService = inject(NotificationService);

  private readonly router = inject(Router);

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
      } else {
        this.notificationService.stopPolling();
      }
    });
  }

  ngOnDestroy(): void {
    this.notificationService.stopPolling();
  }

  collapseNavbar(): void {
    this.isNavbarCollapsed.set(true);
  }

  markNotificationRead(notification: any): void {
    if (!notification.isRead) {
      this.notificationService.markAsRead(notification.id).subscribe(() => {
        this.notificationService.refresh();
      });
    }
    if (notification.taskId && notification.projectKey) {
      this.router.navigate(['/project', notification.projectKey, 'task', notification.taskId]);
    }
  }

  toggleMobileSidebar(): void {
    document.body.classList.toggle('sidebar-mobile-open');
  }
}
