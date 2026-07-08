import { ChangeDetectionStrategy, Component, computed, effect, inject, input, Signal, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import dayjs from 'dayjs/esm';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';
import { AlertService } from 'app/core/util/alert.service';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { Alert } from 'app/shared/alert/alert';
import { AlertError } from 'app/shared/alert/alert-error';
import { FormatMediumDatePipe, FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { IProject, IProjectMember } from '../project.model';
import { ProjectService } from '../service/project.service';
import { UserService } from 'app/entities/user/service/user.service';
import { IUser } from 'app/entities/user/user.model';
import { ISprint } from 'app/entities/sprint/sprint.model';
import { SprintService } from 'app/entities/sprint/service/sprint.service';
import { IIssue, NewIssue } from 'app/entities/issue/issue.model';
import { IssueService } from 'app/entities/issue/service/issue.service';
import { IssueType } from 'app/entities/enumerations/issue-type.model';
import { Priority } from 'app/entities/enumerations/priority.model';
import { ProjectRole } from 'app/entities/enumerations/project-role.model';

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
    FormatMediumDatePipe,
    FormsModule,
  ],
})
export class ProjectDetail {
  readonly project = input<IProject | null>(null);

  readonly members = signal<IProjectMember[]>([]);
  readonly showAddForm = signal(false);
  readonly users = signal<IUser[]>([]);
  readonly selectedUserId = signal<number | null>(null);
  readonly newMemberRole = signal<ProjectRole>(ProjectRole.MEMBER);
  readonly editingMemberId = signal<number | null>(null);
  readonly editingRole = signal<ProjectRole | ''>('');

  readonly sprints = signal<ISprint[]>([]);
  readonly issues = signal<IIssue[]>([]);

  // Issue creation form
  readonly showIssueForm = signal(false);
  readonly newIssueTitle = signal('');
  readonly newIssueDescription = signal('');
  readonly newIssueType = signal<keyof typeof IssueType>('TASK');
  readonly newIssuePriority = signal<keyof typeof Priority>('MEDIUM');
  readonly newIssueSprintId = signal<number | null>(null);
  readonly newIssueAssigneeId = signal<number | null>(null);
  readonly isCreatingIssue = signal(false);

  readonly issueTypeValues = Object.keys(IssueType);
  readonly priorityValues = Object.keys(Priority);

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

  readonly canManageMembers = computed(() => {
    const role = this.userProjectRole();
    return role === ProjectRole.OWNER || role === ProjectRole.MANAGER;
  });

  readonly canManageSprints = computed(() => {
    const role = this.userProjectRole();
    return role === ProjectRole.OWNER || role === ProjectRole.MANAGER;
  });

  readonly canCreateIssues = computed(() => {
    return this.userProjectRole() !== null;
  });

  readonly canExportCsv = computed(() => {
    const role = this.userProjectRole();
    return role === ProjectRole.OWNER || role === ProjectRole.MANAGER;
  });

  readonly csvExportUrl = computed(() => {
    const proj = this.project();
    if (!proj?.id) return '';
    return this.applicationConfigService.getEndpointFor(`api/export/csv/projects/${proj.id}/issues`);
  });

  private readonly projectService = inject(ProjectService);
  private readonly accountService = inject(AccountService);
  private readonly alertService = inject(AlertService);
  private readonly applicationConfigService = inject(ApplicationConfigService);
  private readonly userService = inject(UserService);
  private readonly sprintService = inject(SprintService);
  private readonly issueService = inject(IssueService);

  constructor() {
    effect(() => {
      const proj = this.project();
      if (proj?.id) {
        this.loadMembers(proj.id);
        this.loadSprints(proj.id);
        this.loadIssues(proj.id);
      }
    });
  }

  loadSprints(projectId: number): void {
    this.sprintService.query({ 'projectId.equals': projectId }).subscribe({
      next: res => this.sprints.set(res.body ?? []),
    });
  }

  loadIssues(projectId: number): void {
    this.issueService.query({ 'projectId.equals': projectId, size: 100 }).subscribe({
      next: res => this.issues.set(res.body ?? []),
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
        this.newMemberRole.set(ProjectRole.MEMBER);
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

  toggleIssueForm(): void {
    this.showIssueForm.update(v => !v);
    if (!this.showIssueForm()) {
      this.resetIssueForm();
    }
  }

  resetIssueForm(): void {
    this.newIssueTitle.set('');
    this.newIssueDescription.set('');
    this.newIssueType.set('TASK');
    this.newIssuePriority.set('MEDIUM');
    this.newIssueSprintId.set(null);
    this.newIssueAssigneeId.set(null);
  }

  createIssue(projectId: number): void {
    const title = this.newIssueTitle();
    if (!title) {
      return;
    }
    this.isCreatingIssue.set(true);

    const newIssue: NewIssue = {
      id: null,
      title,
      description: this.newIssueDescription() || null,
      type: this.newIssueType(),
      status: 'BACKLOG',
      priority: this.newIssuePriority(),
      createdAt: dayjs(),
      updatedAt: dayjs(),
      project: { id: projectId, name: this.project()?.name ?? null, key: this.project()?.key ?? null },
      sprint: this.newIssueSprintId() ? { id: this.newIssueSprintId()!, name: '' } : null,
      assignee: this.newIssueAssigneeId()
        ? { id: this.newIssueAssigneeId()!, login: this.members().find(m => m.userId === this.newIssueAssigneeId())?.userLogin ?? '' }
        : null,
      createdBy: null,
    };

    this.issueService.createForProject(projectId, newIssue).subscribe({
      next: () => {
        this.loadIssues(projectId);
        this.showIssueForm.set(false);
        this.resetIssueForm();
        this.isCreatingIssue.set(false);
        this.alertService.addAlert({ type: 'success', translationKey: 'gestionTachesApp.issue.created' });
      },
      error: () => {
        this.isCreatingIssue.set(false);
        this.alertService.addAlert({ type: 'danger', translationKey: 'error.general' });
      },
    });
  }

  previousState(): void {
    globalThis.history.back();
  }
}
