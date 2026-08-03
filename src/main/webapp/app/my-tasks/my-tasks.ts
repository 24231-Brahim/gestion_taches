import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { TranslateDirective } from 'app/shared/language';
import { AccountService } from 'app/core/auth/account.service';
import { TaskKanbanBoard } from 'app/entities/task/kanban/task-kanban-board';
import { TaskDetailPanel } from 'app/entities/task/detail/task-detail-panel';
import { TaskService } from 'app/entities/task/service/task.service';
import { ITask } from 'app/entities/task/task.model';
import { ProjectService } from 'app/entities/project/service/project.service';
import { ProjectRole } from 'app/entities/enumerations/project-role.model';

const MANAGED_ROLES: ProjectRole[] = [ProjectRole.OWNER, ProjectRole.MANAGER];

@Component({
  selector: 'jhi-my-tasks',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './my-tasks.html',
  styles: [
    `
      .my-tasks-page {
        padding: var(--stack-lg, 24px);
      }
      .my-tasks-title {
        font-family: var(--font-display);
        margin-bottom: var(--stack-md, 16px);
      }
      .my-tasks-empty {
        display: flex;
        flex-direction: column;
        align-items: center;
        text-align: center;
        gap: 8px;
        color: var(--color-muted);
        padding: var(--stack-lg, 48px) 0;
      }
      .my-tasks-empty-icon {
        font-size: 2rem;
        color: var(--color-primary);
        opacity: 0.6;
      }
    `,
  ],
  imports: [TranslateModule, TranslateDirective, FontAwesomeModule, TaskKanbanBoard, TaskDetailPanel],
})
export default class MyTasks {
  readonly tasks = signal<ITask[]>([]);
  readonly isLoading = signal(false);
  readonly selectedTask = signal<ITask | null>(null);
  readonly drawerVisible = signal(false);

  readonly hasTasks = computed(() => this.tasks().length > 0);

  private readonly accountService = inject(AccountService);
  private readonly taskService = inject(TaskService);
  private readonly projectService = inject(ProjectService);

  constructor() {
    this.loadTasks();
  }

  private loadTasks(): void {
    const account = this.accountService.account();
    if (!account) {
      return;
    }

    this.isLoading.set(true);
    const authorities = account.authorities ?? [];
    const isAdmin = authorities.includes('ROLE_ADMIN');
    const isProjectManager = authorities.includes('ROLE_PROJET_MANAGER');

    if (isAdmin) {
      this.taskService.query({ size: 500, sort: 'updatedAt,desc' }).subscribe({
        next: res => {
          this.tasks.set(res.body ?? []);
          this.isLoading.set(false);
        },
        error: () => this.isLoading.set(false),
      });
      return;
    }

    if (isProjectManager) {
      this.projectService.getMyRoles().subscribe({
        next: roles => {
          const managedProjectIds = roles.filter(r => r.role && MANAGED_ROLES.includes(r.role)).map(r => r.projectId);
          if (managedProjectIds.length === 0) {
            this.tasks.set([]);
            this.isLoading.set(false);
            return;
          }
          forkJoin(
            managedProjectIds.map(projectId =>
              this.taskService.query({ 'projectId.equals': projectId, size: 500, sort: 'updatedAt,desc' }),
            ),
          ).subscribe({
            next: responses => {
              this.tasks.set(responses.flatMap(res => res.body ?? []));
              this.isLoading.set(false);
            },
            error: () => this.isLoading.set(false),
          });
        },
        error: () => this.isLoading.set(false),
      });
      return;
    }

    const currentUserId = account.id;
    if (currentUserId == null) {
      this.tasks.set([]);
      this.isLoading.set(false);
      return;
    }
    this.taskService.query({ 'assigneeId.equals': currentUserId, size: 500, sort: 'updatedAt,desc' }).subscribe({
      next: res => {
        this.tasks.set(res.body ?? []);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  onSelectTask(task: ITask): void {
    this.selectedTask.set(task);
    this.drawerVisible.set(true);
  }

  onCloseDrawer(): void {
    this.drawerVisible.set(false);
    this.selectedTask.set(null);
  }

  onKanbanStatusChange(event: { taskId: number; status: string }): void {
    this.tasks.update(list => list.map(t => (t.id === event.taskId ? { ...t, status: event.status as ITask['status'] } : t)));
  }
}
