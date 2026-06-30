import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';
import { TranslateDirective } from 'app/shared/language';
import { DashboardComponent } from 'app/home/dashboard/dashboard.component';

@Component({
  selector: 'jhi-home',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './home.html',
  styleUrl: './home.scss',
  imports: [TranslateDirective, TranslateModule, RouterLink, DashboardComponent],
})
export default class Home {
  public readonly account = inject(AccountService).account;
  readonly isManagerOrAdmin = computed(
    () => this.account()?.authorities.some(a => a === 'ROLE_ADMIN' || a === 'ROLE_PROJET_MANAGER') ?? false,
  );

  private readonly router = inject(Router);

  login(): void {
    this.router.navigate(['/login']);
  }
}
