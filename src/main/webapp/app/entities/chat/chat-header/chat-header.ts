import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { ConversationType } from 'app/entities/enumerations/conversation-type.model';

import { IChatMember, IConversation } from '../chat.model';

@Component({
  selector: 'jhi-chat-header',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './chat-header.html',
  styleUrl: './chat-header.scss',
  imports: [FontAwesomeModule, TranslateModule],
})
export class ChatHeader {
  readonly conversation = input<IConversation | null>(null);
  readonly members = input<IChatMember[]>([]);
  readonly currentUserLogin = input<string | null>(null);
  readonly showBack = input(false);

  readonly back = output();

  readonly title = computed(() => {
    const conversation = this.conversation();
    if (!conversation) {
      return '';
    }
    if (conversation.type === ConversationType.GENERAL) {
      return conversation.name ?? 'Général';
    }
    const other = (conversation.participants ?? []).find(p => p.userLogin !== this.currentUserLogin());
    return other?.userLogin ?? conversation.name ?? '';
  });

  readonly isDirect = computed(() => this.conversation()?.type === ConversationType.DIRECT);
}
