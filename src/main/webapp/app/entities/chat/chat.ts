import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { interval } from 'rxjs';
import { AccountService } from 'app/core/auth/account.service';
import { TranslateDirective } from 'app/shared/language';

import { ConversationType } from 'app/entities/enumerations/conversation-type.model';
import { IProject } from 'app/entities/project/project.model';
import { ProjectService } from 'app/entities/project/service/project.service';

import { IChatMember, IChatMessage, IConversation } from './chat.model';
import { ChatService } from './service/chat.service';
import { ConversationList } from './conversation-list/conversation-list';
import { MemberList } from './member-list/member-list';
import { ChatHeader } from './chat-header/chat-header';
import { ChatMessageList } from './chat-message-list/chat-message-list';
import { ChatComposer } from './chat-composer/chat-composer';

const MESSAGE_PAGE_SIZE = 30;
const MESSAGE_POLL_INTERVAL = 5000;
const PRESENCE_POLL_INTERVAL = 30000;

@Component({
  selector: 'jhi-chat',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './chat.html',
  styleUrl: './chat.scss',
  imports: [
    FontAwesomeModule,
    TranslateModule,
    TranslateDirective,
    RouterLink,
    ConversationList,
    MemberList,
    ChatHeader,
    ChatMessageList,
    ChatComposer,
  ],
})
export class Chat implements OnInit {
  readonly projectId = signal<number | null>(null);
  readonly projectKey = signal<string | null>(null);
  readonly project = signal<IProject | null>(null);

  readonly conversations = signal<IConversation[]>([]);
  readonly selectedConversation = signal<IConversation | null>(null);
  readonly messages = signal<IChatMessage[]>([]);
  readonly members = signal<IChatMember[]>([]);

  readonly loadingConversations = signal(false);
  readonly loadingMessages = signal(false);
  readonly loadingOlder = signal(false);
  readonly sending = signal(false);
  readonly hasMore = signal(false);
  readonly atBottom = signal(true);
  readonly loadError = signal<string | null>(null);
  readonly isMobile = signal(window.innerWidth < 992);
  readonly showConversationList = signal(true);

  readonly currentUserLogin = computed(() => this.accountService.account()?.login ?? null);

  readonly messageList = viewChild(ChatMessageList);

  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly projectService = inject(ProjectService);
  private readonly chatService = inject(ChatService);
  private readonly accountService = inject(AccountService);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    let route: ActivatedRoute | null = this.activatedRoute;
    let key: string | null = null;
    while (route) {
      key = route.snapshot.paramMap.get('key');
      if (key) {
        break;
      }
      route = route.parent;
    }
    if (!key) {
      this.loadError.set('chat.error.load');
      return;
    }
    this.projectService.findByKey(key).subscribe({
      next: project => {
        this.project.set(project);
        this.projectKey.set(project.key ?? null);
        this.projectId.set(project.id);
        this.loadConversations();
        this.loadMembers();
      },
      error: () => this.loadError.set('chat.error.load'),
    });

    interval(MESSAGE_POLL_INTERVAL)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.refreshConversations());
    interval(PRESENCE_POLL_INTERVAL)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.heartbeat();
        this.loadMembers();
      });

    window.addEventListener('resize', this.onResize);
    this.destroyRef.onDestroy(() => window.removeEventListener('resize', this.onResize));
  }

  loadConversations(): void {
    const projectId = this.projectId();
    if (!projectId) {
      return;
    }
    this.loadingConversations.set(true);
    this.chatService.getConversations(projectId).subscribe({
      next: list => {
        this.conversations.set(list);
        if (!this.selectedConversation()) {
          const general = list.find(c => c.type === ConversationType.GENERAL);
          if (general) {
            this.selectConversation(general);
          } else if (list.length > 0) {
            this.selectConversation(list[0]);
          }
        }
        this.loadingConversations.set(false);
      },
      error: () => {
        this.loadError.set('chat.error.load');
        this.loadingConversations.set(false);
      },
    });
  }

  loadMembers(): void {
    const projectId = this.projectId();
    if (!projectId) {
      return;
    }
    this.chatService.getMembers(projectId).subscribe({
      next: members => this.members.set(members),
      error: () => undefined,
    });
  }

  refreshConversations(): void {
    const projectId = this.projectId();
    if (!projectId || this.loadingConversations() || this.loadError()) {
      return;
    }
    this.chatService.getConversations(projectId).subscribe({
      next: list => {
        this.conversations.set(list);
        const selected = this.selectedConversation();
        if (selected) {
          const fresh = list.find(c => c.id === selected.id);
          if (fresh) {
            this.selectedConversation.set(fresh);
            this.clearUnread(fresh.id);
            this.loadLatestMessages();
          }
        }
      },
      error: () => undefined,
    });
  }

  heartbeat(): void {
    const projectId = this.projectId();
    if (!projectId) {
      return;
    }
    this.chatService.updatePresence(projectId).subscribe({ error: () => undefined });
  }

  selectConversation(conversation: IConversation): void {
    this.selectedConversation.set(conversation);
    this.messages.set([]);
    this.clearUnread(conversation.id);
    if (this.isMobile()) {
      this.showConversationList.set(false);
    }
    const projectId = this.projectId();
    if (projectId) {
      this.chatService.markConversationRead(projectId, conversation.id).subscribe({ error: () => undefined });
    }
    this.messageList()?.resetScroll();
    this.loadMessages();
  }

  openDirect(userId: number): void {
    const projectId = this.projectId();
    if (!projectId) {
      return;
    }
    this.chatService.openDirectConversation(projectId, userId).subscribe({
      next: conversation => {
        this.upsertConversation(conversation);
        this.selectConversation(conversation);
      },
      error: () => undefined,
    });
  }

  loadMessages(): void {
    const projectId = this.projectId();
    const conversationId = this.selectedConversation()?.id;
    if (!projectId || !conversationId) {
      return;
    }
    this.loadingMessages.set(true);
    this.chatService.getMessages(projectId, conversationId, null, MESSAGE_PAGE_SIZE).subscribe({
      next: messages => {
        this.messages.set(messages);
        this.hasMore.set(messages.length === MESSAGE_PAGE_SIZE);
        this.loadingMessages.set(false);
      },
      error: () => this.loadingMessages.set(false),
    });
  }

  loadOlder(): void {
    const projectId = this.projectId();
    const current = this.messages();
    const conversationId = this.selectedConversation()?.id;
    if (!projectId || !conversationId || current.length === 0 || this.loadingOlder()) {
      return;
    }
    this.loadingOlder.set(true);
    this.chatService.getMessages(projectId, conversationId, current[0].id, MESSAGE_PAGE_SIZE).subscribe({
      next: older => {
        this.messages.set([...older, ...current]);
        this.hasMore.set(older.length === MESSAGE_PAGE_SIZE);
        this.loadingOlder.set(false);
      },
      error: () => this.loadingOlder.set(false),
    });
  }

  send(content: string): void {
    const projectId = this.projectId();
    const conversationId = this.selectedConversation()?.id;
    if (!projectId || !conversationId || this.sending()) {
      return;
    }
    this.sending.set(true);
    this.chatService.sendMessage(projectId, conversationId, content).subscribe({
      next: message => {
        this.messages.update(list => [...list, message]);
        this.clearUnread(conversationId);
        this.sending.set(false);
        this.messageList()?.resetScroll();
      },
      error: () => this.sending.set(false),
    });
  }

  onEditMessage(event: { messageId: number; content: string }): void {
    const projectId = this.projectId();
    if (!projectId) {
      return;
    }
    this.chatService.updateMessage(projectId, event.messageId, event.content).subscribe({
      next: updated => {
        this.messages.update(list => list.map(m => (m.id === updated.id ? updated : m)));
      },
      error: () => undefined,
    });
  }

  onDeleteMessage(messageId: number): void {
    const projectId = this.projectId();
    if (!projectId) {
      return;
    }
    this.chatService.deleteMessage(projectId, messageId).subscribe({
      next: () => {
        this.messages.update(list => list.map(m => (m.id === messageId ? { ...m, deleted: true, content: '' } : m)));
      },
      error: () => undefined,
    });
  }

  onAtBottomChange(bottom: boolean): void {
    this.atBottom.set(bottom);
    if (bottom) {
      const conversationId = this.selectedConversation()?.id;
      const projectId = this.projectId();
      if (conversationId && projectId) {
        this.clearUnread(conversationId);
        this.chatService.markConversationRead(projectId, conversationId).subscribe({ error: () => undefined });
      }
    }
  }

  backToList(): void {
    this.showConversationList.set(true);
  }

  clearUnread(conversationId: number): void {
    this.conversations.update(list => list.map(c => (c.id === conversationId ? { ...c, unreadCount: 0 } : c)));
  }

  private loadLatestMessages(): void {
    const projectId = this.projectId();
    const conversationId = this.selectedConversation()?.id;
    if (!projectId || !conversationId || !this.atBottom()) {
      return;
    }
    this.chatService.getMessages(projectId, conversationId, null, MESSAGE_PAGE_SIZE).subscribe({
      next: messages => {
        this.messages.set(messages);
        this.hasMore.set(messages.length === MESSAGE_PAGE_SIZE);
      },
      error: () => undefined,
    });
  }

  private upsertConversation(conversation: IConversation): void {
    this.conversations.update(list => {
      const exists = list.some(c => c.id === conversation.id);
      return exists ? list.map(c => (c.id === conversation.id ? conversation : c)) : [...list, conversation];
    });
  }

  private readonly onResize = (): void => {
    const mobile = window.innerWidth < 992;
    this.isMobile.set(mobile);
    if (!mobile) {
      this.showConversationList.set(true);
    }
  };
}
