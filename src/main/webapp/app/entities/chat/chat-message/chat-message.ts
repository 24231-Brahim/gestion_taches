import { ChangeDetectionStrategy, Component, computed, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { TranslateDirective } from 'app/shared/language';
import { FormatMediumDatetimePipe } from 'app/shared/date';

import { IChatMessage } from '../chat.model';

@Component({
  selector: 'jhi-chat-message',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './chat-message.html',
  styleUrl: './chat-message.scss',
  imports: [FontAwesomeModule, TranslateModule, TranslateDirective, FormsModule, FormatMediumDatetimePipe],
})
export class ChatMessage {
  readonly message = input.required<IChatMessage>();
  readonly mine = input(false);

  readonly edit = output<{ messageId: number; content: string }>();
  readonly delete = output<number>();

  readonly editing = signal(false);
  readonly draft = signal('');

  readonly senderLogin = computed(() => this.message().sender?.login ?? '');

  onEditKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.saveEdit();
    } else if (event.key === 'Escape') {
      event.preventDefault();
      this.cancelEdit();
    }
  }

  startEdit(): void {
    this.draft.set(this.message().content);
    this.editing.set(true);
  }

  cancelEdit(): void {
    this.editing.set(false);
    this.draft.set('');
  }

  saveEdit(): void {
    const content = this.draft().trim();
    if (!content) {
      this.cancelEdit();
      return;
    }
    this.edit.emit({ messageId: this.message().id, content });
    this.editing.set(false);
  }

  confirmDelete(): void {
    if (window.confirm('Supprimer ce message ?')) {
      this.delete.emit(this.message().id);
    }
  }

  initials(login: string): string {
    return (login || '?').slice(0, 2).toUpperCase();
  }
}
