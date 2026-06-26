import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';
import { LoginService } from 'app/login/login.service';
import { TranslateDirective } from 'app/shared/language';

@Component({
  selector: 'jhi-bottom-nav',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './bottom-nav.html',
  styleUrl: './bottom-nav.scss',
  imports: [RouterLink, RouterLinkActive, FontAwesomeModule, TranslateDirective, TranslateModule],
})
export default class BottomNav {
  readonly account = inject(AccountService).account;
  private readonly loginService = inject(LoginService);
  private readonly router = inject(Router);

  logout(): void {
    this.loginService.logout();
    this.router.navigate(['']);
  }
}
