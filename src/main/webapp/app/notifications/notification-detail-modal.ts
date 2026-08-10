import { Component, inject } from '@angular/core';
import { DatePipe } from '@angular/common';

import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { INotification } from 'app/core/util/notification.service';
import { NotificationService } from 'app/core/util/notification.service';

@Component({
  selector: 'jhi-notification-detail-modal',
  templateUrl: './notification-detail-modal.html',
  imports: [FontAwesomeModule, TranslateModule, DatePipe],
})
export class NotificationDetailModal {
  notification!: INotification;

  readonly activeModal = inject(NgbActiveModal);
  private readonly notificationService = inject(NotificationService);

  markAsRead(): void {
    if (!this.notification.isRead) {
      this.notificationService.markAsRead(this.notification.id).subscribe(() => {
        this.notification.isRead = true;
      });
    }
  }

  close(): void {
    this.activeModal.dismiss();
  }
}
