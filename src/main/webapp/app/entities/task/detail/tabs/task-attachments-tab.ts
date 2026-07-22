import { ChangeDetectionStrategy, Component, effect, inject, input, signal } from '@angular/core';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { IAttachment } from 'app/entities/attachment/attachment.model';
import { AttachmentService } from 'app/entities/attachment/service/attachment.service';

@Component({
  selector: 'jhi-task-attachments-tab',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './task-attachments-tab.html',
  imports: [FontAwesomeModule, TranslateModule, TranslateDirective, FormatMediumDatetimePipe],
})
export class TaskAttachmentsTab {
  readonly taskId = input.required<number>();

  readonly attachments = signal<IAttachment[]>([]);
  readonly isLoading = signal(false);

  private readonly attachmentService = inject(AttachmentService);

  constructor() {
    effect(() => {
      const id = this.taskId();
      if (id) {
        this.loadAttachments(id);
      }
    });
  }

  private loadAttachments(taskId: number): void {
    this.isLoading.set(true);
    this.attachmentService.query({ 'taskId.equals': taskId, sort: 'uploadedAt,desc' }).subscribe({
      next: res => {
        this.attachments.set(res.body ?? []);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }
}
