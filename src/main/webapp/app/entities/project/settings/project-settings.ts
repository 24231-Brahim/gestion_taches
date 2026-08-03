import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, Signal, computed, effect, inject, input, signal } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';
import { AlertService } from 'app/core/util/alert.service';
import { Alert } from 'app/shared/alert/alert';
import { AlertError } from 'app/shared/alert/alert-error';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/service/user.service';
import { ProjectRole } from 'app/entities/enumerations/project-role.model';
import { IProject, IProjectMember } from '../project.model';
import { ProjectService } from '../service/project.service';
import { ProjectFormGroup, ProjectFormService } from '../update/project-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-project-settings',
  templateUrl: './project-settings.html',
  imports: [
    FormsModule,
    ReactiveFormsModule,
    RouterLink,
    FontAwesomeModule,
    Alert,
    AlertError,
    TranslateDirective,
    TranslateModule,
    FormatMediumDatetimePipe,
  ],
})
export class ProjectSettings {
  readonly project = input<IProject | null>(null);
  readonly isSaving = signal(false);

  readonly members = signal<IProjectMember[]>([]);
  readonly showAddForm = signal(false);
  readonly users = signal<IUser[]>([]);
  readonly selectedUserId = signal<number | null>(null);

  readonly userProjectRole: Signal<ProjectRole | null> = computed(() => {
    const account = this.accountService.account();
    if (!account) {
      return null;
    }
    if (account.authorities.includes('ROLE_ADMIN')) {
      return ProjectRole.OWNER;
    }
    const member = this.members().find(m => m.userLogin === account.login);
    return member?.role ?? null;
  });

  readonly canManage = computed(() => {
    const role = this.userProjectRole();
    return role === ProjectRole.OWNER || role === ProjectRole.MANAGER;
  });

  readonly isForbidden = computed(() => !this.canManage());

  protected projectService = inject(ProjectService);
  protected projectFormService = inject(ProjectFormService);
  protected accountService = inject(AccountService);
  protected alertService = inject(AlertService);
  protected translateService = inject(TranslateService);
  protected userService = inject(UserService);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: ProjectFormGroup = this.projectFormService.createProjectFormGroup();

  constructor() {
    effect(() => {
      const proj = this.project();
      if (proj?.id) {
        this.projectFormService.resetForm(this.editForm, proj);
        this.editForm.controls.key.disable();
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

  save(): void {
    this.isSaving.set(true);
    const project = this.projectFormService.getProject(this.editForm);
    if (project.id === null) {
      this.isSaving.set(false);
      return;
    }
    this.projectService.update(project).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.projectService.refresh();
        this.alertService.addAlert({ type: 'success', translationKey: 'gestionTachesApp.project.updated' });
      },
      error: (err: HttpErrorResponse) => {
        this.isSaving.set(false);
        const message = err.error?.detail ?? err.message ?? this.translateService.instant('error.general');
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }
}
