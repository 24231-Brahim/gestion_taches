import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';

import dayjs from 'dayjs/esm';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { TranslateDirective } from 'app/shared/language';
import { IProject, IProjectCardStats } from '../project.model';

const MAX_VISIBLE_MEMBER_AVATARS = 4;
// References design-system.scss tokens rather than hardcoded hex values.
const AVATAR_PALETTE = [
  'var(--color-primary)',
  'var(--color-secondary)',
  'var(--color-tertiary)',
  'var(--color-priority-medium)',
  'var(--color-danger)',
  'var(--color-tag-teal-fg)',
];

function colorForLogin(login: string | null | undefined): string {
  if (!login) {
    return AVATAR_PALETTE[0];
  }
  let hash = 0;
  for (let i = 0; i < login.length; i++) {
    hash = login.charCodeAt(i) + ((hash << 5) - hash);
  }
  return AVATAR_PALETTE[Math.abs(hash) % AVATAR_PALETTE.length];
}

function initialsForLogin(login: string | null | undefined): string {
  if (!login) {
    return '?';
  }
  return login.slice(0, 2).toUpperCase();
}

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-project-card',
  imports: [RouterLink, FontAwesomeModule, TranslateDirective, TranslateModule],
  templateUrl: './project-card.html',
  styleUrl: './project-card.scss',
})
export class ProjectCard {
  readonly project = input.required<IProject>();
  readonly stats = input<IProjectCardStats | undefined>(undefined);
  readonly canManage = input(false);
  readonly canDelete = input(false);

  readonly deleteProject = output<IProject>();

  readonly ownerInitials = computed(() => initialsForLogin(this.project().ownerLogin));
  readonly ownerColor = computed(() => colorForLogin(this.project().ownerLogin));

  readonly visibleMembers = computed(() => (this.project().projectMembers ?? []).slice(0, MAX_VISIBLE_MEMBER_AVATARS));
  readonly hiddenMemberCount = computed(() => Math.max((this.project().projectMembers?.length ?? 0) - MAX_VISIBLE_MEMBER_AVATARS, 0));

  readonly progressPercent = computed(() => {
    const s = this.stats();
    if (!s || s.totalTasks === 0) {
      return 0;
    }
    return Math.round((s.doneTasks / s.totalTasks) * 100);
  });

  readonly createdAgo = computed(() => {
    const createdAt = this.project().createdAt;
    return createdAt ? dayjs(createdAt).fromNow() : '';
  });

  memberInitials(login: string | null | undefined): string {
    return initialsForLogin(login);
  }

  memberColor(login: string | null | undefined): string {
    return colorForLogin(login);
  }

  onDelete(event: Event): void {
    event.stopPropagation();
    event.preventDefault();
    this.deleteProject.emit(this.project());
  }
}
