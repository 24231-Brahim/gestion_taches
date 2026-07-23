import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';
import { AlertService } from 'app/core/util/alert.service';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { IGroupMessage, NewGroupMessage } from '../group-message.model';
import { GroupMessageService } from '../service/group-message.service';
import { IProjectMember } from 'app/entities/project/project.model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-group-message-list',
  templateUrl: './group-message-list.html',
  styles: [
    `
      .discussion-container {
        display: flex;
        flex-direction: column;
        gap: 16px;
      }
      .discussion-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
      }
      .discussion-title {
        font-family: var(--font-inter);
        font-size: 1rem;
        font-weight: 600;
        color: var(--color-text, #dfe3ea);
        margin: 0;
      }
      .members-sidebar {
        background: var(--color-surface-container, #1b2025);
        border: 1px solid var(--color-outline-variant, #2a3038);
        border-radius: var(--radius-lg);
        padding: 12px;
      }
      .members-title {
        font-family: var(--font-inter);
        font-size: 0.75rem;
        font-weight: 600;
        text-transform: none;
        color: var(--color-text-muted, #6a8fac);
        margin: 0 0 8px;
      }
      .member-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 6px 8px;
        border-radius: var(--radius-md);
        font-family: var(--font-inter);
        font-size: 0.85rem;
        color: var(--color-text, #dfe3ea);
      }
      .member-item:hover {
        background: var(--color-surface-container-high, #262a30);
      }
      .member-avatar {
        width: 28px;
        height: 28px;
        border-radius: 50%;
        background: var(--color-primary-container, #25a7fd);
        color: var(--color-on-primary-container);
        font-size: 0.75rem;
        font-weight: 600;
        display: flex;
        align-items: center;
        justify-content: center;
        font-family: var(--font-mono);
        flex-shrink: 0;
      }
      .member-role {
        font-size: 0.7rem;
        color: var(--color-text-muted, #6a8fac);
        margin-left: auto;
      }
      .message-list {
        display: flex;
        flex-direction: column;
        gap: 12px;
        flex: 1;
        min-width: 0;
      }
      .message-item {
        background: var(--color-surface-container, #1b2025);
        border: 1px solid var(--color-outline-variant, #2a3038);
        border-radius: var(--radius-lg);
        padding: 12px 14px;
      }
      .message-item.private {
        border-left: 3px solid var(--color-primary, #97cbff);
      }
      .message-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 6px;
      }
      .message-author {
        font-family: var(--font-inter);
        font-size: 0.8rem;
        font-weight: 600;
        color: var(--color-primary, #97cbff);
      }
      .message-recipient {
        font-family: var(--font-inter);
        font-size: 0.7rem;
        color: var(--color-text-muted, #6a8fac);
        background: var(--color-surface-container-high, #262a30);
        padding: 1px 6px;
        border-radius: var(--radius-sm);
      }
      .message-date {
        font-family: var(--font-inter);
        font-size: 0.7rem;
        color: var(--color-text-muted, #6a8fac);
        margin-left: auto;
      }
      .message-content {
        font-family: var(--font-inter);
        font-size: 0.85rem;
        color: var(--color-text, #dfe3ea);
        white-space: pre-wrap;
        line-height: 1.5;
      }
      .compose-box {
        background: var(--color-surface-container, #1b2025);
        border: 1px solid var(--color-outline-variant, #2a3038);
        border-radius: var(--radius-lg);
        padding: 14px;
        display: flex;
        flex-direction: column;
        gap: 10px;
      }
      .compose-row {
        display: flex;
        gap: 10px;
        align-items: flex-end;
      }
      .compose-row textarea {
        flex: 1;
      }
      .compose-row select {
        width: 200px;
        flex-shrink: 0;
      }
      .empty-state {
        text-align: center;
        padding: 32px 16px;
        color: var(--color-text-muted, #6a8fac);
        font-family: var(--font-inter);
        font-size: 0.85rem;
      }
    `,
  ],
  imports: [FormsModule, FontAwesomeModule, TranslateDirective, TranslateModule, FormatMediumDatetimePipe],
})
export class GroupMessageListComponent {
  readonly projectId = input.required<number>();
  readonly members = input.required<IProjectMember[]>();

  readonly messages = signal<IGroupMessage[]>([]);
  readonly newMessageText = signal('');
  readonly selectedRecipientId = signal<number | null>(null);
  readonly isLoading = signal(false);

  protected readonly groupMessageService = inject(GroupMessageService);
  protected readonly accountService = inject(AccountService);
  protected readonly alertService = inject(AlertService);

  readonly currentLogin = computed(() => this.accountService.account()?.login);

  constructor() {
    effect(() => {
      const id = this.projectId();
      if (id) {
        this.loadMessages(id);
      }
    });
  }

  loadMessages(projectId: number): void {
    this.isLoading.set(true);
    this.groupMessageService.find(projectId).subscribe({
      next: messages => {
        this.messages.set(messages);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.alertService.addAlert({ type: 'danger', translationKey: 'error.general' });
      },
    });
  }

  sendMessage(): void {
    const content = this.newMessageText().trim();
    if (!content) {
      return;
    }
    const recipientId = this.selectedRecipientId();
    const newMsg: NewGroupMessage = {
      id: null,
      content,
      recipient: recipientId ? { id: recipientId, login: '' } : null,
    };
    this.groupMessageService.create(this.projectId(), newMsg).subscribe({
      next: () => {
        this.newMessageText.set('');
        this.selectedRecipientId.set(null);
        this.loadMessages(this.projectId());
      },
      error: (err: HttpErrorResponse) => {
        const message = err.error?.detail ?? err.message ?? 'error.general';
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  isOwnMessage(msg: IGroupMessage): boolean {
    return msg.sender?.login === this.currentLogin();
  }

  getInitials(login: string | undefined): string {
    if (!login) {
      return '?';
    }
    return login.charAt(0).toUpperCase();
  }

  trackById(_index: number, msg: IGroupMessage): number {
    return msg.id;
  }
}
