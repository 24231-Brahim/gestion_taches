import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { TranslateDirective } from 'app/shared/language';

import { IChatMember } from '../chat.model';

@Component({
  selector: 'jhi-member-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './member-list.html',
  styleUrl: './member-list.scss',
  imports: [FontAwesomeModule, TranslateModule, TranslateDirective],
})
export class MemberList {
  readonly members = input<IChatMember[]>([]);
  readonly currentUserLogin = input<string | null>(null);

  readonly openDirect = output<number>();

  initials(login: string | null | undefined): string {
    if (!login) {
      return '?';
    }
    return login.slice(0, 2).toUpperCase();
  }

  isSelf(member: IChatMember): boolean {
    return member.userLogin === this.currentUserLogin();
  }

  trackMember = (_index: number, member: IChatMember): number => member.userId ?? -1;
}
