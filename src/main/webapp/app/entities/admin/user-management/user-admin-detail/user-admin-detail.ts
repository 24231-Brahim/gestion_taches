import { DatePipe, NgClass, UpperCasePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { TranslateDirective } from 'app/shared/language';
import { UserManagementService, IUserAdminDetail } from '../service/user-management.service';

@Component({
  selector: 'jhi-user-admin-detail',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './user-admin-detail.html',
  imports: [RouterLink, FontAwesomeModule, TranslateDirective, TranslateModule, DatePipe, NgClass, UpperCasePipe],
})
export class UserAdminDetail implements OnInit {
  readonly userDetail = signal<IUserAdminDetail | null>(null);
  readonly isLoading = signal(true);
  readonly error = signal<string | null>(null);

  private readonly route = inject(ActivatedRoute);
  private readonly userManagementService = inject(UserManagementService);

  ngOnInit(): void {
    const login = this.route.snapshot.paramMap.get('login');
    if (login) {
      this.loadDetail(login);
    }
  }

  previousState(): void {
    globalThis.history.back();
  }

  private loadDetail(login: string): void {
    this.isLoading.set(true);
    this.error.set(null);
    this.userManagementService.findDetail(login).subscribe({
      next: detail => {
        this.userDetail.set(detail);
        this.isLoading.set(false);
      },
      error: () => {
        this.error.set('userManagement.detail.loadError');
        this.isLoading.set(false);
      },
    });
  }
}
