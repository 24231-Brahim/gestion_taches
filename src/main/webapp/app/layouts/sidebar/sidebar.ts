import { ChangeDetectionStrategy, Component, HostListener, OnDestroy, inject, signal } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter } from 'rxjs';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbDropdown, NgbDropdownMenu, NgbDropdownToggle } from '@ng-bootstrap/ng-bootstrap/dropdown';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';
import { StateStorageService } from 'app/core/auth/state-storage.service';
import { ProfileService } from 'app/layouts/profiles/profile.service';
import { LoginService } from 'app/login/login.service';
import { ThemeService } from 'app/core/util/theme.service';
import { LANGUAGES } from 'app/config/language.constants';
import { TranslateDirective } from 'app/shared/language';
import FindLanguageFromKeyPipe from 'app/shared/language/find-language-from-key.pipe';
import ActiveMenuDirective from 'app/layouts/navbar/active-menu.directive';

@Component({
  selector: 'jhi-sidebar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
  host: {
    '[class.collapsed]': 'collapsed()',
  },
  imports: [
    RouterLink,
    RouterLinkActive,
    FontAwesomeModule,
    TranslateDirective,
    TranslateModule,
    NgbDropdown,
    NgbDropdownMenu,
    NgbDropdownToggle,
    FindLanguageFromKeyPipe,
    ActiveMenuDirective,
  ],
})
export default class Sidebar implements OnDestroy {
  readonly account = inject(AccountService).account;
  readonly collapsed = signal(false);
  readonly isMobile = signal(window.innerWidth < 768);
  readonly mobileOpen = signal(false);
  readonly inProduction = signal(true);
  readonly openAPIEnabled = signal(false);
  readonly themeService = inject(ThemeService);
  readonly languages = LANGUAGES;
  readonly settingsOpen = signal(false);

  readonly currentProjectKey = signal<string | null>(null);

  private readonly profileService = inject(ProfileService);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly loginService = inject(LoginService);
  private readonly translateService = inject(TranslateService);
  private readonly stateStorageService = inject(StateStorageService);
  private readonly bodyObserver = new MutationObserver(() => {
    this.mobileOpen.set(document.body.classList.contains('sidebar-mobile-open'));
  });

  constructor() {
    this.profileService.getProfileInfo().subscribe(profileInfo => {
      this.inProduction.set(profileInfo.inProduction ?? true);
      this.openAPIEnabled.set(profileInfo.openAPIEnabled ?? false);
    });
    this.bodyObserver.observe(document.body, { attributes: true, attributeFilter: ['class'] });

    this.router.events.pipe(filter(event => event instanceof NavigationEnd)).subscribe(() => {
      this.detectProjectContext();
    });
    this.detectProjectContext();
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

  toggleSettings(): void {
    this.settingsOpen.update(v => !v);
  }

  changeLanguage(languageKey: string): void {
    this.stateStorageService.storeLocale(languageKey);
    this.translateService.use(languageKey);
  }

  logout(): void {
    this.loginService.logout();
    this.router.navigate(['']);
  }

  private detectProjectContext(): void {
    let route = this.activatedRoute;
    while (route.firstChild) {
      route = route.firstChild;
    }
    const key = route.snapshot.params['key'] ?? this.findKeyInAncestors(this.activatedRoute);
    this.currentProjectKey.set(key ?? null);
  }

  private findKeyInAncestors(route: ActivatedRoute): string | null {
    if (route.snapshot.params['key']) {
      return route.snapshot.params['key'] as string;
    }
    if (route.parent) {
      return this.findKeyInAncestors(route.parent);
    }
    return null;
  }
}
