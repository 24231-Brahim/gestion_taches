import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, input, signal } from '@angular/core';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import dayjs from 'dayjs/esm';

import { AccountService } from 'app/core/auth/account.service';
import { AlertService } from 'app/core/util/alert.service';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { IAttachment } from 'app/entities/attachment/attachment.model';
import { AttachmentService } from 'app/entities/attachment/service/attachment.service';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-task-attachment-list',
  templateUrl: './task-attachment-list.html',
  styles: [
    `
      .task-attachments {
        display: flex;
        flex-direction: column;
        gap: 12px;
      }
      .task-section-title {
        font-family: var(--font-inter);
        font-size: 0.8rem;
        text-transform: none;
        color: var(--color-text-muted);
        margin: 0 0 4px;
        font-weight: 600;
      }
      .attachment-drop-zone {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 16px;
        border: 2px dashed var(--color-outline-variant);
        border-radius: var(--radius-lg);
        background: var(--color-surface-container);
        color: var(--color-text-muted);
        font-family: var(--font-inter);
        font-size: 0.8rem;
        cursor: pointer;
        transition:
          border-color var(--transition-fast),
          background var(--transition-fast);
      }
      .attachment-drop-zone:hover,
      .attachment-drop-zone.drag-over {
        border-color: var(--color-primary);
        background: var(--color-surface-container-high);
      }
      .attachment-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 10px;
        background: var(--color-surface-container);
        border: 1px solid var(--color-outline-variant);
        border-radius: var(--radius-lg);
      }
      .attachment-name {
        font-family: var(--font-inter);
        font-size: 0.85rem;
        color: var(--color-primary);
        text-decoration: none;
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .attachment-name:hover {
        text-decoration: underline;
      }
      .attachment-date {
        font-family: var(--font-inter);
        font-size: 0.7rem;
        color: var(--color-text-muted);
        white-space: nowrap;
      }
      .text-muted {
        color: var(--color-text-muted);
        font-family: var(--font-inter);
        font-size: 0.8rem;
      }
    `,
  ],
  imports: [FontAwesomeModule, TranslateDirective, TranslateModule, FormatMediumDatetimePipe],
})
export class TaskAttachmentList implements OnInit {
  readonly taskId = input.required<number>();

  readonly attachments = signal<IAttachment[]>([]);

  protected readonly http = inject(HttpClient);
  protected readonly attachmentService = inject(AttachmentService);
  protected readonly accountService = inject(AccountService);
  protected readonly appConfig = inject(ApplicationConfigService);
  protected readonly alertService = inject(AlertService);

  ngOnInit(): void {
    this.loadAttachments();
  }

  loadAttachments(): void {
    this.http
      .get<
        (Omit<IAttachment, 'uploadedAt'> & { uploadedAt?: string | null })[]
      >(this.appConfig.getEndpointFor(`api/attachments/by-task/${this.taskId()}`))
      .subscribe({
        // Same fix as comments: the REST payload's uploadedAt is a plain ISO string, but
        // formatMediumDatetime requires a real dayjs instance and throws otherwise.
        next: attachments =>
          this.attachments.set(attachments.map(a => ({ ...a, uploadedAt: a.uploadedAt ? dayjs(a.uploadedAt) : undefined }))),
        error: () => this.alertService.addAlert({ type: 'danger', translationKey: 'error.general' }),
      });
  }

  uploadAttachment(file: File): void {
    const formData = new FormData();
    formData.append('file', file);
    this.http.post(this.appConfig.getEndpointFor(`api/attachments/upload?taskId=${this.taskId()}`), formData).subscribe({
      next: () => this.loadAttachments(),
      error: (err: HttpErrorResponse) => {
        const message = err.error?.detail ?? err.message ?? 'error.general';
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  deleteAttachment(id: number): void {
    this.attachmentService.delete(id).subscribe({
      next: () => this.loadAttachments(),
      error: (err: HttpErrorResponse) => {
        const message = err.error?.detail ?? err.message ?? 'error.general';
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  downloadUrl(attachment: IAttachment): string {
    return `/api/attachments/download/${attachment.id}`;
  }

  onFileDrop(event: DragEvent): void {
    event.preventDefault();
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.uploadAttachment(files[0]);
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = input.files;
    if (files && files.length > 0) {
      this.uploadAttachment(files[0]);
      input.value = '';
    }
  }

  isUploader(attachment: IAttachment): boolean {
    return this.accountService.account()?.login === attachment.uploadedBy?.login;
  }

  canModify(attachment: IAttachment): boolean {
    // Moderation bypass is ADMIN/PROJET_MANAGER only — a DEVELOPER may only delete their own
    // uploads, never anyone else's (matches the backend's checkCanModifyAttachment).
    return this.accountService.hasAnyAuthority(['ROLE_ADMIN', 'ROLE_PROJET_MANAGER']) || this.isUploader(attachment);
  }
}
