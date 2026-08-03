import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';
import { FormatMediumDatePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';
import { ProjectRole } from 'app/entities/enumerations/project-role.model';
import { IProject } from 'app/entities/project/project.model';
import { ProjectService } from 'app/entities/project/service/project.service';
import { TaskService } from 'app/entities/task/service/task.service';
import { SprintService } from '../service/sprint.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-sprint-table',
  templateUrl: './sprint-table.html',
  styles: [
    `
      .sprint-table-page {
        padding: 24px;
        max-width: 1400px;
        margin: 0 auto;
      }
      .page-title {
        font-family: var(--font-inter);
        font-size: 1.4rem;
        color: var(--color-text, #dfe3ea);
        margin-bottom: 20px;
      }
    `,
  ],
  imports: [RouterLink, FontAwesomeModule, TranslateDirective, TranslateModule, FormatMediumDatePipe],
})
export class SprintTable implements OnInit {
  readonly currentProjectKey = signal<string | null>(null);
  readonly currentProject = signal<IProject | null>(null);

  readonly sprints = computed(() => this.sprintService.sprints());
  readonly tasks = computed(() => this.taskService.tasks());

  readonly sprintTaskCounts = computed(() => {
    const counts = new Map<number, number>();
    for (const t of this.tasks()) {
      if (t.sprint?.id) {
        counts.set(t.sprint.id, (counts.get(t.sprint.id) ?? 0) + 1);
      }
    }
    return counts;
  });

  readonly userProjectRole = computed<ProjectRole | null>(() => {
    const account = this.accountService.account();
    if (!account) return null;
    if (account.authorities.includes('ROLE_ADMIN')) return ProjectRole.OWNER;
    return null;
  });

  readonly canManageSprints = computed(() => {
    const role = this.userProjectRole();
    return role === ProjectRole.OWNER || role === ProjectRole.MANAGER;
  });

  protected readonly activatedRoute = inject(ActivatedRoute);
  protected readonly sprintService = inject(SprintService);
  protected readonly taskService = inject(TaskService);
  protected readonly projectService = inject(ProjectService);
  protected readonly accountService = inject(AccountService);

  ngOnInit(): void {
    const parentParamMap = this.activatedRoute.parent?.paramMap ?? this.activatedRoute.paramMap;
    parentParamMap.subscribe(params => {
      const key = params.get('key');
      if (key) {
        this.currentProjectKey.set(key);
        this.projectService.findByKey(key).subscribe(project => {
          if (project) {
            this.currentProject.set(project);
            this.sprintService.sprintsParams.set({
              size: 100,
              sort: 'startDate,desc',
              'projectId.equals': project.id,
            });
            this.sprintService.refresh();
            this.taskService.tasksParams.set({
              size: 100,
              'projectId.equals': project.id,
              'sprintId.specified': true,
            });
          }
        });
      }
    });
  }

  getTaskCount(sprintId: number): number {
    return this.sprintTaskCounts().get(sprintId) ?? 0;
  }
}
