import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, effect, inject, input, signal } from '@angular/core';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { AlertService } from 'app/core/util/alert.service';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { IAttachment } from 'app/entities/attachment/attachment.model';
import { AttachmentService } from 'app/entities/attachment/service/attachment.service';

@Component({
  selector: 'jhi-task-attachments-tab',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './task-attachments-tab.html',
  styles: [
    `
      .attachment-drop-zone {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 16px;
        border: 2px dashed var(--color-outline-variant, #2a3038);
        border-radius: var(--radius-lg);
        background: var(--color-surface-container, #1b2025);
        color: var(--color-text-muted, #6a8fac);
        font-family: var(--font-inter);
        font-size: 0.8rem;
        cursor: pointer;
        transition:
          border-color 0.2s,
          background 0.2s;
        margin-bottom: 12px;
      }
      .attachment-drop-zone:hover,
      .attachment-drop-zone.drag-over {
        border-color: var(--color-primary, #97cbff);
        background: var(--color-surface-container-high, #262a30);
      }
      .attachments-list {
        display: flex;
        flex-direction: column;
        gap: 8px;
      }
      .attachment-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 10px;
        background: var(--color-surface-container, #1b2025);
        border: 1px solid var(--color-outline-variant, #2a3038);
        border-radius: var(--radius-lg);
      }
      .attachment-name {
        font-family: var(--font-inter);
        font-size: 0.85rem;
        color: var(--color-primary, #97cbff);
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
        white-space: nowrap;
      }
    `,
  ],
  imports: [FontAwesomeModule, TranslateModule, TranslateDirective, FormatMediumDatetimePipe],
})
export class TaskAttachmentsTab {
  readonly taskId = input.required<number>();

  readonly attachments = signal<IAttachment[]>([]);
  readonly isLoading = signal(false);
  readonly isDragOver = signal(false);

  private readonly http = inject(HttpClient);
  private readonly attachmentService = inject(AttachmentService);
  private readonly alertService = inject(AlertService);
  private readonly appConfig = inject(ApplicationConfigService);

  constructor() {
    effect(() => {
      const id = this.taskId();
      if (id) {
        this.loadAttachments(id);
      }
    });
  }

  loadAttachments(taskId: number): void {
    this.isLoading.set(true);
    this.http.get<IAttachment[]>(this.appConfig.getEndpointFor(`api/attachments/by-task/${taskId}`)).subscribe({
      next: attachments => {
        this.attachments.set(attachments);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.alertService.addAlert({ type: 'danger', translationKey: 'error.general' });
      },
    });
  }

  uploadAttachment(file: File): void {
    const formData = new FormData();
    formData.append('file', file);
    this.http.post(this.appConfig.getEndpointFor(`api/attachments/upload?taskId=${this.taskId()}`), formData).subscribe({
      next: () => this.loadAttachments(this.taskId()),
      error: (err: HttpErrorResponse) => {
        const message = err.error?.detail ?? err.message ?? 'error.general';
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  deleteAttachment(id: number): void {
    this.attachmentService.delete(id).subscribe({
      next: () => this.loadAttachments(this.taskId()),
      error: (err: HttpErrorResponse) => {
        const message = err.error?.detail ?? err.message ?? 'error.general';
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  downloadAttachment(attachment: IAttachment): void {
    this.http.get(this.appConfig.getEndpointFor(`api/attachments/download/${attachment.id}`), { responseType: 'blob' }).subscribe({
      next: blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = attachment.fileName ?? 'download';
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => this.alertService.addAlert({ type: 'danger', translationKey: 'error.general' }),
    });
  }

  onFileDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver.set(false);
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.uploadAttachment(files[0]);
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver.set(true);
  }

  onDragLeave(): void {
    this.isDragOver.set(false);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = input.files;
    if (files && files.length > 0) {
      this.uploadAttachment(files[0]);
      input.value = '';
    }
  }
}
