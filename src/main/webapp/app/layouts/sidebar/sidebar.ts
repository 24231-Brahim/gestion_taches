import { ChangeDetectionStrategy, Component, HostListener, OnDestroy, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';
import { TranslateDirective } from 'app/shared/language';

@Component({
  selector: 'jhi-sidebar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
  host: {
    '[class.collapsed]': 'collapsed()',
  },
  imports: [RouterLink, RouterLinkActive, FontAwesomeModule, TranslateDirective, TranslateModule],
})
export default class Sidebar implements OnDestroy {
  readonly account = inject(AccountService).account;
  readonly collapsed = signal(false);
  readonly isMobile = signal(window.innerWidth < 768);
  readonly mobileOpen = signal(false);

  private readonly bodyObserver = new MutationObserver(() => {
    this.mobileOpen.set(document.body.classList.contains('sidebar-mobile-open'));
  });

  constructor() {
    this.bodyObserver.observe(document.body, { attributes: true, attributeFilter: ['class'] });
  }

  ngOnDestroy(): void {
    this.bodyObserver.disconnect();
  }

  @HostListener('window:resize')
  onResize(): void {
    this.isMobile.set(window.innerWidth < 768);
    if (window.innerWidth >= 768) {
      this.mobileOpen.set(false);
      document.body.classList.remove('sidebar-mobile-open');
    }
  }

  toggleCollapse(): void {
    if (this.isMobile()) {
      this.mobileOpen.update(v => !v);
    } else {
      this.collapsed.update(v => !v);
    }
  }

  closeMobile(): void {
    this.mobileOpen.set(false);
    document.body.classList.remove('sidebar-mobile-open');
  }
}
