import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { ConversationType } from 'app/entities/enumerations/conversation-type.model';
import { TranslateDirective } from 'app/shared/language';

import { IConversation } from '../chat.model';

@Component({
  selector: 'jhi-conversation-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './conversation-list.html',
  styleUrl: './conversation-list.scss',
  imports: [FontAwesomeModule, TranslateModule, TranslateDirective],
})
export class ConversationList {
  readonly conversations = input<IConversation[]>([]);
  readonly selectedId = input<number | null>(null);
  readonly currentUserLogin = input<string | null>(null);

  readonly selectConversation = output<IConversation>();

  readonly conversationsByType = computed(() => {
    const all = this.conversations();
    return {
      general: all.filter(c => c.type === ConversationType.GENERAL),
      direct: all.filter(c => c.type === ConversationType.DIRECT),
    };
  });

  displayName(conversation: IConversation): string {
    if (conversation.type === ConversationType.GENERAL) {
      return conversation.name ?? 'Général';
    }
    const other = (conversation.participants ?? []).find(p => p.userLogin !== this.currentUserLogin());
    return other?.userLogin ?? conversation.name ?? '';
  }

  initial(conversation: IConversation): string {
    return (this.displayName(conversation) || '?').slice(0, 1);
  }

  trackConversation = (_index: number, conversation: IConversation): number => conversation.id;
}
