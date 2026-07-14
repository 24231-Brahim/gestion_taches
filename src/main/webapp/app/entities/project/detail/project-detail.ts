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
import { ITask, NewTask } from 'app/entities/task/task.model';
import { TaskService } from 'app/entities/task/service/task.service';
import { TaskType } from 'app/entities/enumerations/task-type.model';
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
  readonly tasks = signal<ITask[]>([]);

  // Task creation form
  readonly showTaskForm = signal(false);
  readonly newTaskTitle = signal('');
  readonly newTaskDescription = signal('');
  readonly newTaskType = signal<keyof typeof TaskType>('TASK');
  readonly newTaskPriority = signal<keyof typeof Priority>('MEDIUM');
  readonly newTaskSprintId = signal<number | null>(null);
  readonly newTaskAssigneeId = signal<number | null>(null);
  readonly isCreatingTask = signal(false);

  readonly taskTypeValues = Object.keys(TaskType);
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

  readonly canCreateTasks = computed(() => {
    return this.userProjectRole() !== null;
  });

  readonly canExportCsv = computed(() => {
    const role = this.userProjectRole();
    return role === ProjectRole.OWNER || role === ProjectRole.MANAGER;
  });

  readonly csvExportUrl = computed(() => {
    const proj = this.project();
    if (!proj?.id) return '';
    return this.applicationConfigService.getEndpointFor(`api/export/csv/projects/${proj.id}/tasks`);
  });

  private readonly projectService = inject(ProjectService);
  private readonly accountService = inject(AccountService);
  private readonly alertService = inject(AlertService);
  private readonly applicationConfigService = inject(ApplicationConfigService);
  private readonly userService = inject(UserService);
  private readonly sprintService = inject(SprintService);
  private readonly taskService = inject(TaskService);

  constructor() {
    effect(() => {
      const proj = this.project();
      if (proj?.id) {
        this.loadMembers(proj.id);
        this.loadSprints(proj.id);
        this.loadTasks(proj.id);
      }
    });
  }

  loadSprints(projectId: number): void {
    this.sprintService.query({ 'projectId.equals': projectId }).subscribe({
      next: res => this.sprints.set(res.body ?? []),
    });
  }

  loadTasks(projectId: number): void {
    this.taskService.query({ 'projectId.equals': projectId, size: 100 }).subscribe({
      next: res => this.tasks.set(res.body ?? []),
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

  toggleTaskForm(): void {
    this.showTaskForm.update(v => !v);
    if (!this.showTaskForm()) {
      this.resetTaskForm();
    }
  }

  resetTaskForm(): void {
    this.newTaskTitle.set('');
    this.newTaskDescription.set('');
    this.newTaskType.set('TASK');
    this.newTaskPriority.set('MEDIUM');
    this.newTaskSprintId.set(null);
    this.newTaskAssigneeId.set(null);
  }

  createTask(projectId: number): void {
    const title = this.newTaskTitle();
    if (!title) {
      return;
    }
    this.isCreatingTask.set(true);

    const newTask: NewTask = {
      id: null,
      title,
      description: this.newTaskDescription() || null,
      type: this.newTaskType(),
      status: 'NEW',
      priority: this.newTaskPriority(),
      createdAt: dayjs(),
      updatedAt: dayjs(),
      project: { id: projectId, name: this.project()?.name ?? null, key: this.project()?.key ?? null },
      sprint: this.newTaskSprintId() ? { id: this.newTaskSprintId()!, name: '' } : null,
      assignee: this.newTaskAssigneeId()
        ? { id: this.newTaskAssigneeId()!, login: this.members().find(m => m.userId === this.newTaskAssigneeId())?.userLogin ?? '' }
        : null,
      createdBy: null,
    };

    this.taskService.createForProject(projectId, newTask).subscribe({
      next: () => {
        this.loadTasks(projectId);
        this.showTaskForm.set(false);
        this.resetTaskForm();
        this.isCreatingTask.set(false);
        this.alertService.addAlert({ type: 'success', translationKey: 'gestionTachesApp.task.created' });
      },
      error: () => {
        this.isCreatingTask.set(false);
        this.alertService.addAlert({ type: 'danger', translationKey: 'error.general' });
      },
    });
  }

  previousState(): void {
    globalThis.history.back();
  }
}
