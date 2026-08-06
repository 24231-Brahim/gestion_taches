import { AfterViewInit, ChangeDetectionStrategy, Component, ElementRef, OnInit, inject, signal, viewChild } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';
import { StateStorageService } from 'app/core/auth/state-storage.service';
import { LoginService } from 'app/login/login.service';
import { ThemeService } from 'app/core/util/theme.service';
import { LANGUAGES } from 'app/config/language.constants';
import { TranslateDirective } from 'app/shared/language';
import FindLanguageFromKeyPipe from 'app/shared/language/find-language-from-key.pipe';

@Component({
  selector: 'jhi-login',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TranslateDirective,
    TranslateModule,
    ReactiveFormsModule,
    RouterLink,
    FontAwesomeModule,
    FindLanguageFromKeyPipe,
    NgOptimizedImage,
  ],
  templateUrl: './login.html',
  styleUrl: './login.component.scss',
})
export default class Login implements OnInit, AfterViewInit {
  username = viewChild.required<ElementRef>('username');

  readonly authenticationError = signal(false);
  readonly showLangMenu = signal(false);
  readonly themeService = inject(ThemeService);
  readonly languages = LANGUAGES;

  loginForm = new FormGroup({
    username: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    rememberMe: new FormControl(false, { nonNullable: true, validators: [Validators.required] }),
  });

  private readonly accountService = inject(AccountService);
  private readonly loginService = inject(LoginService);
  private readonly router = inject(Router);
  readonly translateService = inject(TranslateService);
  private readonly stateStorageService = inject(StateStorageService);

  changeLanguage(lang: string): void {
    this.stateStorageService.storeLocale(lang);
    this.translateService.use(lang);
    this.showLangMenu.set(false);
  }

  toggleLangMenu(): void {
    this.showLangMenu.update(v => !v);
  }

  closeLangMenu(): void {
    this.showLangMenu.set(false);
  }

  ngOnInit(): void {
    // if already authenticated then navigate to home page
    this.accountService.identity().subscribe(() => {
      if (this.accountService.isAuthenticated()) {
        this.router.navigate(['/home']);
      }
    });
  }

  ngAfterViewInit(): void {
    this.username().nativeElement.focus();
  }

  login(): void {
    this.loginService.login(this.loginForm.getRawValue()).subscribe({
      next: () => {
        this.authenticationError.set(false);
        if (!this.router.currentNavigation()) {
          // There were no routing during login (eg from navigationToStoredUrl)
          this.router.navigate(['/home']);
        }
      },
      error: () => this.authenticationError.set(true),
    });
  }
}
