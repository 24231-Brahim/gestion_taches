import { ChangeDetectionStrategy, Component, DOCUMENT, OnInit, Renderer2, RendererFactory2, inject, signal } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';

import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import dayjs from 'dayjs/esm';

import { AppPageTitleStrategy } from 'app/app-page-title-strategy';
import { AccountService } from 'app/core/auth/account.service';
import Breadcrumb from '../breadcrumb/breadcrumb';
import BottomNav from '../bottom-nav/bottom-nav';
import PageRibbon from '../profiles/page-ribbon';
import Sidebar from '../sidebar/sidebar';

@Component({
  selector: 'jhi-main',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './main.html',
  providers: [AppPageTitleStrategy],
  imports: [RouterOutlet, BottomNav, PageRibbon, Sidebar, Breadcrumb],
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

  ngOnInit(): void {
    this.accountService.identity().subscribe({
      next: () => this.appReady.set(true),
      error: () => this.appReady.set(true),
    });

    this.translateService.onLangChange.subscribe((langChangeEvent: LangChangeEvent) => {
      this.appPageTitleStrategy.updateTitle(this.router.routerState.snapshot);
      dayjs.locale(langChangeEvent.lang);
      this.renderer.setAttribute(this.htmlElement, 'lang', langChangeEvent.lang);
    });
  }
}
