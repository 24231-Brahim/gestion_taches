import { HttpClient } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, input, signal } from '@angular/core';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';
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
        font-family: 'JetBrains Mono', monospace;
        font-size: 0.8rem;
        text-transform: uppercase;
        letter-spacing: 0.08em;
        color: var(--color-text-muted, #6a8fac);
        margin: 0 0 4px;
      }
      .attachment-drop-zone {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 16px;
        border: 2px dashed var(--color-outline-variant, #2a3038);
        background: var(--color-surface-container, #1b2025);
        color: var(--color-text-muted, #6a8fac);
        font-family: 'JetBrains Mono', monospace;
        font-size: 0.8rem;
        cursor: pointer;
        transition:
          border-color 0.15s,
          background 0.15s;
      }
      .attachment-drop-zone:hover,
      .attachment-drop-zone.drag-over {
        border-color: var(--color-primary, #97cbff);
        background: var(--color-surface-container-high, #262a30);
      }
      .attachment-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 10px;
        background: var(--color-surface-container, #1b2025);
        border: 2px solid var(--color-outline-variant, #2a3038);
      }
      .attachment-name {
        font-family: 'JetBrains Mono', monospace;
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
        font-family: 'JetBrains Mono', monospace;
        font-size: 0.7rem;
        color: var(--color-text-muted, #6a8fac);
        white-space: nowrap;
      }
      .text-muted {
        color: var(--color-text-muted, #6a8fac);
        font-family: 'JetBrains Mono', monospace;
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

  ngOnInit(): void {
    this.loadAttachments();
  }

  loadAttachments(): void {
    this.http.get<IAttachment[]>(this.appConfig.getEndpointFor(`api/attachments/by-task/${this.taskId()}`)).subscribe({
      next: attachments => this.attachments.set(attachments),
    });
  }

  uploadAttachment(file: File): void {
    const formData = new FormData();
    formData.append('file', file);
    this.http.post(this.appConfig.getEndpointFor(`api/attachments/upload?taskId=${this.taskId()}`), formData).subscribe({
      next: () => this.loadAttachments(),
    });
  }

  deleteAttachment(id: number): void {
    this.attachmentService.delete(id).subscribe({
      next: () => this.loadAttachments(),
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

  protected readonly isAdminOrManagerOrDev = () =>
    this.accountService.hasAnyAuthority(['ROLE_ADMIN', 'ROLE_PROJET_MANAGER', 'ROLE_DEVELOPER']);
}
