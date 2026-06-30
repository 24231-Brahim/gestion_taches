import { ChangeDetectionStrategy, Component, effect, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';
import { AlertService } from 'app/core/util/alert.service';
import { Alert } from 'app/shared/alert/alert';
import { AlertError } from 'app/shared/alert/alert-error';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import HasAnyAuthorityDirective from 'app/shared/auth/has-any-authority.directive';
import { IProject, IProjectMember } from '../project.model';
import { ProjectService } from '../service/project.service';
import { UserService } from 'app/entities/user/service/user.service';
import { IUser } from 'app/entities/user/user.model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-project-detail',
  templateUrl: './project-detail.html',
  imports: [
    FontAwesomeModule,
    Alert,
    AlertError,
    TranslateDirective,
    TranslateModule,
    RouterLink,
    FormatMediumDatetimePipe,
    FormsModule,
    HasAnyAuthorityDirective,
  ],
})
export class ProjectDetail {
  readonly project = input<IProject | null>(null);

  readonly members = signal<IProjectMember[]>([]);
  readonly showAddForm = signal(false);
  readonly users = signal<IUser[]>([]);
  readonly selectedUserId = signal<number | null>(null);
  readonly newMemberRole = signal('MEMBER');
  readonly editingMemberId = signal<number | null>(null);
  readonly editingRole = signal('');

  private readonly projectService = inject(ProjectService);
  private readonly accountService = inject(AccountService);
  private readonly alertService = inject(AlertService);
  private readonly userService = inject(UserService);

  constructor() {
    effect(() => {
      const proj = this.project();
      if (proj?.id) {
        this.loadMembers(proj.id);
      }
    });
  }

  loadMembers(projectId: number): void {
    this.projectService.getMembers(projectId).subscribe({
      next: members => this.members.set(members),
      error: () => this.alertService.addAlert({ type: 'danger', translationKey: 'error.loading' }),
    });
  }

  loadUsers(): void {
    this.userService.query({ page: 0, size: 100 }).subscribe({
      next: res => this.users.set(res.body ?? []),
    });
  }

  toggleAddForm(): void {
    this.showAddForm.update(v => !v);
    if (this.showAddForm()) {
      this.loadUsers();
    }
  }

  addMember(projectId: number): void {
    const userId = this.selectedUserId();
    if (!userId) {
      return;
    }
    this.projectService.addMember(projectId, userId).subscribe({
      next: () => {
        this.loadMembers(projectId);
        this.showAddForm.set(false);
        this.selectedUserId.set(null);
        this.newMemberRole.set('MEMBER');
        this.alertService.addAlert({ type: 'success', translationKey: 'gestionTachesApp.project.member.added' });
      },
      error: () => this.alertService.addAlert({ type: 'danger', translationKey: 'gestionTachesApp.project.member.error.add' }),
    });
  }

  removeMember(projectId: number, userId: number): void {
    this.projectService.removeMember(projectId, userId).subscribe({
      next: () => {
        this.loadMembers(projectId);
        this.alertService.addAlert({ type: 'success', translationKey: 'gestionTachesApp.project.member.removed' });
      },
      error: () => this.alertService.addAlert({ type: 'danger', translationKey: 'gestionTachesApp.project.member.error.remove' }),
    });
  }

  startEdit(member: IProjectMember): void {
    this.editingMemberId.set(member.id);
    this.editingRole.set(member.role ?? '');
  }

  cancelEdit(): void {
    this.editingMemberId.set(null);
    this.editingRole.set('');
  }

  saveRole(projectId: number, member: IProjectMember): void {
    this.projectService.updateMemberRole(projectId, member.userId!, this.editingRole()).subscribe({
      next: () => {
        this.loadMembers(projectId);
        this.cancelEdit();
        this.alertService.addAlert({ type: 'success', translationKey: 'gestionTachesApp.project.member.updated' });
      },
      error: () => this.alertService.addAlert({ type: 'danger', translationKey: 'gestionTachesApp.project.member.error.update' }),
    });
  }

  previousState(): void {
    globalThis.history.back();
  }
}
