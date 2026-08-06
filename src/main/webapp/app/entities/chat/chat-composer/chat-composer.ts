import { ChangeDetectionStrategy, Component, ElementRef, input, output, signal, viewChild } from '@angular/core';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { TranslateDirective } from 'app/shared/language';

@Component({
  selector: 'jhi-chat-composer',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './chat-composer.html',
  styleUrl: './chat-composer.scss',
  imports: [FontAwesomeModule, TranslateModule, TranslateDirective],
})
export class ChatComposer {
  readonly disabled = input(false);
  readonly sending = input(false);

  readonly send = output<string>();

  readonly content = signal('');

  private readonly textarea = viewChild.required<ElementRef<HTMLTextAreaElement>>('input');

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.submit();
    }
  }

  autoResize(): void {
    const el = this.textarea().nativeElement;
    el.style.height = 'auto';
    el.style.height = `${Math.min(el.scrollHeight, 160)}px`;
  }

  submit(): void {
    const content = this.content().trim();
    if (!content || this.disabled() || this.sending()) {
      return;
    }
    this.send.emit(content);
    this.content.set('');
    this.textarea().nativeElement.style.height = 'auto';
  }
}
