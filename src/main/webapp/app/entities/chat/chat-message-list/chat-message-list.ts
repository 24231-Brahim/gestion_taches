import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  afterNextRender,
  afterRenderEffect,
  effect,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { TranslateDirective } from 'app/shared/language';

import { IChatMessage } from '../chat.model';
import { ChatMessage } from '../chat-message/chat-message';

@Component({
  selector: 'jhi-chat-message-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './chat-message-list.html',
  styleUrl: './chat-message-list.scss',
  imports: [FontAwesomeModule, TranslateModule, TranslateDirective, ChatMessage],
})
export class ChatMessageList {
  readonly messages = input<IChatMessage[]>([]);
  readonly loadingOlder = input(false);
  readonly hasMore = input(false);
  readonly currentUserLogin = input<string | null>(null);

  readonly loadOlder = output();
  readonly atBottomChange = output<boolean>();
  readonly edit = output<{ messageId: number; content: string }>();
  readonly delete = output<number>();

  private readonly scrollContainer = viewChild.required<ElementRef<HTMLElement>>('scrollContainer');

  private atBottom = true;
  private needsScrollReconcile = false;
  private previousScrollHeight = 0;

  constructor() {
    effect(() => {
      this.messages();
      this.needsScrollReconcile = true;
    });

    afterNextRender(() => {
      this.atBottom = true;
      this.scrollToBottom(false);
    });

    afterRenderEffect(() => {
      this.messages();
      if (this.needsScrollReconcile) {
        this.needsScrollReconcile = false;
        this.reconcileScroll();
      }
    });
  }

  trackMessage = (_index: number, message: IChatMessage): number => message.id;

  /** Called by the parent when switching conversations: jump to the newest messages. */
  resetScroll(): void {
    this.atBottom = true;
    this.previousScrollHeight = 0;
    this.needsScrollReconcile = true;
  }

  onScroll(): void {
    const el = this.scrollContainer().nativeElement;
    const distanceToBottom = el.scrollHeight - el.scrollTop - el.clientHeight;
    const isBottom = distanceToBottom < 40;

    if (isBottom !== this.atBottom) {
      this.atBottom = isBottom;
      this.atBottomChange.emit(isBottom);
    }

    if (this.atBottom) {
      this.scrollToBottom(false);
    } else if (el.scrollTop < 12 && this.hasMore() && !this.loadingOlder()) {
      this.previousScrollHeight = el.scrollHeight;
      this.loadOlder.emit();
    }
  }

  private reconcileScroll(): void {
    const el = this.scrollContainer().nativeElement;
    if (this.atBottom) {
      this.scrollToBottom(false);
    } else if (this.previousScrollHeight > 0) {
      el.scrollTop = el.scrollHeight - this.previousScrollHeight;
      this.previousScrollHeight = 0;
    }
  }

  private scrollToBottom(smooth: boolean): void {
    const el = this.scrollContainer().nativeElement;
    el.scrollTo({ top: el.scrollHeight, behavior: smooth ? 'smooth' : 'auto' });
  }
}
