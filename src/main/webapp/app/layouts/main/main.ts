import { ChangeDetectionStrategy, Component, DOCUMENT, OnInit, Renderer2, RendererFactory2, inject, signal } from '@angular/core';
import { Event, NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs/operators';

import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import dayjs from 'dayjs/esm';

import { AppPageTitleStrategy } from 'app/app-page-title-strategy';
import { AccountService } from 'app/core/auth/account.service';
import Breadcrumb from '../breadcrumb/breadcrumb';
import BottomNav from '../bottom-nav/bottom-nav';
import PageRibbon from '../profiles/page-ribbon';
import { SearchDialogComponent } from '../search/search-dialog.component';
import Sidebar from '../sidebar/sidebar';

@Component({
  selector: 'jhi-main',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './main.html',
  providers: [AppPageTitleStrategy],
  imports: [RouterOutlet, BottomNav, PageRibbon, Sidebar, Breadcrumb, SearchDialogComponent],
})
export default class Main implements OnInit {
  readonly account = inject(AccountService).account;
  readonly appReady = signal(false);

  private readonly renderer: Renderer2;
  private readonly htmlElement: HTMLElement;
  private readonly router = inject(Router);
  private readonly appPageTitleStrategy = inject(AppPageTitleStrategy);
  private readonly accountService = inject(AccountService);
  private readonly document = inject(DOCUMENT);
  private readonly translateService = inject(TranslateService);
  private readonly rootRenderer = inject(RendererFactory2);

  constructor() {
    this.htmlElement = this.document.documentElement;
    this.renderer = this.rootRenderer.createRenderer(this.htmlElement, null);
  }

  readonly isErrorPage = signal(false);

  private readonly errorRoutes = ['/404', '/error', '/accessdenied'];

  private checkIfErrorPage(): void {
    this.isErrorPage.set(this.errorRoutes.includes(this.router.url));
  }

  ngOnInit(): void {
    this.router.events.pipe(filter((event: Event): event is NavigationEnd => event instanceof NavigationEnd)).subscribe(() => {
      this.checkIfErrorPage();
    });
    this.checkIfErrorPage();

    this.accountService.identity().subscribe({
      next: () => this.appReady.set(true),
      error: () => this.appReady.set(true),
    });

    this.translateService.onLangChange.subscribe((langChangeEvent: LangChangeEvent) => {
      this.appPageTitleStrategy.updateTitle(this.router.routerState.snapshot);
      dayjs.locale(langChangeEvent.lang);
      this.renderer.setAttribute(this.htmlElement, 'lang', langChangeEvent.lang);
      const isRtl = langChangeEvent.lang === 'ar';
      this.renderer.setAttribute(this.htmlElement, 'dir', isRtl ? 'rtl' : 'ltr');
    });
  }
}
