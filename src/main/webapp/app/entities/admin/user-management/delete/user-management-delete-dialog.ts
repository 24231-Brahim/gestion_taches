import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { TranslateModule } from '@ngx-translate/core';

import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { UserManagementService } from '../service/user-management.service';
import { IUserManagement } from '../user-management.model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './user-management-delete-dialog.html',
  imports: [TranslateDirective, TranslateModule, FormsModule, FontAwesomeModule, AlertError],
})
export class UserManagementDeleteDialog {
  userManagement?: IUserManagement;
  readonly errorMessage = signal<string | null>(null);

  protected readonly userManagementService = inject(UserManagementService);
  protected readonly activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(login: string): void {
    this.errorMessage.set(null);
    this.userManagementService.delete(login).subscribe({
      next: () => {
        this.activeModal.close(ITEM_DELETED_EVENT);
      },
      error: err => {
        const detail = err.error?.detail;
        if (detail) {
          this.errorMessage.set(detail);
        } else {
          this.errorMessage.set('An error occurred while deleting the user.');
        }
      },
    });
  }
}
