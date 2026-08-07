import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { TaskStatus } from 'app/entities/enumerations/task-status.model';
import { Priority } from 'app/entities/enumerations/priority.model';
import { IEpic } from 'app/entities/epic/epic.model';
import { SprintService } from 'app/entities/sprint/service/sprint.service';
import { ISprint } from 'app/entities/sprint/sprint.model';
import { AlertService } from 'app/core/util/alert.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';

import { ITask } from '../task.model';
import { TaskService } from '../service/task.service';

import { TaskFormGroup, TaskFormService } from './task-form.service';
import { EpicService } from 'app/entities/epic/service/epic.service';
import { IProject, IProjectMember } from 'app/entities/project/project.model';
import { ProjectService } from 'app/entities/project/service/project.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-task-update',
  templateUrl: './task-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class TaskUpdate implements OnInit {
  readonly isSaving = signal(false);
  readonly isProjectContext = signal(false);
  task: ITask | null = null;
  taskStatusValues = Object.keys(TaskStatus);
  priorityValues = Object.keys(Priority);

  sprintsSharedCollection = signal<ISprint[]>([]);
  epicsSharedCollection = signal<IEpic[]>([]);
  projectsSharedCollection = signal<IProject[]>([]);
  projectMembers = signal<IProjectMember[]>([]);

  protected taskService = inject(TaskService);
  protected taskFormService = inject(TaskFormService);
  protected sprintService = inject(SprintService);
  protected epicService = inject(EpicService);
  protected projectService = inject(ProjectService);
  protected activatedRoute = inject(ActivatedRoute);
  protected alertService = inject(AlertService);
  protected translateService = inject(TranslateService);
  protected destroyRef = inject(DestroyRef);
  protected router = inject(Router);

  private hasContextQueryParams = false;
  private contextSprintId: number | null = null;
  private contextEpicId: number | null = null;

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: TaskFormGroup = this.taskFormService.createTaskFormGroup();

  compareSprint = (o1: ISprint | null, o2: ISprint | null): boolean => this.sprintService.compareSprint(o1, o2);

  compareEpic = (o1: IEpic | null, o2: IEpic | null): boolean => this.epicService.compareEpic(o1, o2);

  compareProject = (o1: IProject | null, o2: IProject | null): boolean => this.projectService.compareProject(o1, o2);
  compareProjectMember = (o1: { id: number; login: string } | null, o2: { id: number; login: string } | null): boolean =>
    o1 !== null && o2 !== null ? o1.id === o2.id : o1 === o2;

  loadProjectMembers(projectId: number): void {
    this.projectService.getMembers(projectId).subscribe({
      next: members => this.projectMembers.set(members),
    });
  }

  loadProjectScopedOptions(projectId: number): void {
    this.sprintService
      .query({ 'projectId.equals': projectId })
      .pipe(map((res: HttpResponse<ISprint[]>) => res.body ?? []))
      .pipe(map((sprints: ISprint[]) => this.sprintService.addSprintToCollectionIfMissing<ISprint>(sprints, this.task?.sprint)))
      .subscribe((sprints: ISprint[]) => this.sprintsSharedCollection.set(sprints));

    this.epicService
      .query({ 'projectId.equals': projectId })
      .pipe(map((res: HttpResponse<IEpic[]>) => res.body ?? []))
      .pipe(map((epics: IEpic[]) => this.epicService.addEpicToCollectionIfMissing<IEpic>(epics, this.task?.epic)))
      .subscribe((epics: IEpic[]) => this.epicsSharedCollection.set(epics));
  }

  ngOnInit(): void {
    const query = this.activatedRoute.snapshot.queryParamMap;
    const sprintId = query.get('sprintId');
    const epicId = query.get('epicId');
    this.hasContextQueryParams = sprintId !== null || epicId !== null;
    this.contextSprintId = sprintId ? Number(sprintId) : null;
    this.contextEpicId = epicId ? Number(epicId) : null;

    this.activatedRoute.data.subscribe(({ task }) => {
      this.task = task;
      if (task) {
        this.updateForm(task);
        const projectId = task.project?.id;
        if (projectId) {
          this.loadProjectScopedOptions(projectId);
          this.loadProjectMembers(projectId);
          return;
        }
      }

      if (!this.hasContextQueryParams) {
        this.loadRelationshipsOptions();
      }
    });

    // When arriving with sprintId/epicId query params (from Sprint/Epic detail pages), pre-select
    // the Sprint/Epic (and the project) after the project context has been resolved, so that the
    // project valueChanges handler (which clears sprint/epic) does not wipe the pre-selection.
    if (this.hasContextQueryParams) {
      this.applyQueryParamSelections();
    } else {
      // Pre-select project from parent route :key param (e.g. when coming from project-detail)
      this.activatedRoute.parent?.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(params => {
        const projectKey = params.get('key');
        if (projectKey && !this.task) {
          this.projectService.findByKey(projectKey).subscribe(project => {
            if (project) {
              this.editForm.patchValue({ project });
              this.loadProjectMembers(project.id!);
              this.loadProjectScopedOptions(project.id!);
              this.isProjectContext.set(true);
            }
          });
        }
      });
    }

    // Watch for project changes to load members and filter sprints/epics
    this.editForm
      .get('project')
      ?.valueChanges.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(project => {
        const projectVal = project as IProject | null;
        if (projectVal?.id) {
          this.loadProjectMembers(projectVal.id);
          this.loadProjectScopedOptions(projectVal.id);
          this.editForm.patchValue({ assignee: null, sprint: null, epic: null }, { emitEvent: false });
        } else {
          this.loadRelationshipsOptions();
          this.projectMembers.set([]);
          this.editForm.patchValue({ assignee: null, sprint: null, epic: null }, { emitEvent: false });
        }
      });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const project = this.editForm.get('project')?.value as IProject | null;
    if (!project?.id) {
      this.alertService.addAlert({ type: 'danger', translationKey: 'gestionTachesApp.task.error.noProject' });
      this.isSaving.set(false);
      return;
    }
    const task = this.taskFormService.getTask(this.editForm);
    if (task.id === null) {
      this.subscribeToSaveResponse(this.taskService.createForProject(project.id, task));
    } else {
      this.subscribeToSaveResponse(this.taskService.update(task));
    }
  }

  protected subscribeToSaveResponse(result: Observable<ITask | null>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: (err: HttpErrorResponse) => this.onSaveError(err),
    });
  }

  protected onSaveSuccess(): void {
    this.taskService.refresh();
    if (this.hasContextQueryParams) {
      const sprint = this.editForm.get('sprint')?.value as ISprint | null;
      const epic = this.editForm.get('epic')?.value as IEpic | null;
      const project = this.editForm.get('project')?.value as IProject | null;
      const projectKey = project?.key;
      if (sprint?.id && projectKey) {
        this.router.navigate(['/project', projectKey, 'sprint', sprint.id, 'view']);
        return;
      }
      if (epic?.id && projectKey) {
        this.router.navigate(['/project', projectKey, 'epic', epic.id, 'view']);
        return;
      }
    }
    this.previousState();
  }

  protected onSaveError(err: HttpErrorResponse): void {
    const message = err.error?.detail ?? err.message ?? this.translateService.instant('error.general');
    this.alertService.addAlert({ type: 'danger', message });
  }

  protected onSaveFinalize(): void {
    this.isSaving.set(false);
  }

  protected updateForm(task: ITask): void {
    this.task = task;
    this.taskFormService.resetForm(this.editForm, task);

    this.sprintsSharedCollection.update(sprints => this.sprintService.addSprintToCollectionIfMissing<ISprint>(sprints, task.sprint));
    this.epicsSharedCollection.update(epics => this.epicService.addEpicToCollectionIfMissing<IEpic>(epics, task.epic));
    this.projectsSharedCollection.update(projects => this.projectService.addProjectToCollectionIfMissing<IProject>(projects, task.project));
  }

  protected loadRelationshipsOptions(): void {
    this.sprintService
      .query()
      .pipe(map((res: HttpResponse<ISprint[]>) => res.body ?? []))
      .pipe(map((sprints: ISprint[]) => this.sprintService.addSprintToCollectionIfMissing<ISprint>(sprints, this.task?.sprint)))
      .subscribe((sprints: ISprint[]) => this.sprintsSharedCollection.set(sprints));

    this.epicService
      .query()
      .pipe(map((res: HttpResponse<IEpic[]>) => res.body ?? []))
      .pipe(map((epics: IEpic[]) => this.epicService.addEpicToCollectionIfMissing<IEpic>(epics, this.task?.epic)))
      .subscribe((epics: IEpic[]) => this.epicsSharedCollection.set(epics));

    this.projectService
      .query()
      .pipe(map((res: HttpResponse<IProject[]>) => res.body ?? []))
      .pipe(map((projects: IProject[]) => this.projectService.addProjectToCollectionIfMissing<IProject>(projects, this.task?.project)))
      .subscribe((projects: IProject[]) => {
        this.projectsSharedCollection.set(projects);
        // Load project members if project is known
        const projectId = this.task?.project?.id ?? projects.find(p => this.editForm.get('project')?.value?.id === p.id)?.id;
        if (projectId) {
          this.loadProjectMembers(projectId);
        }
      });
  }

  /**
   * Applies the sprintId/projectId/epicId query params in creation mode: the project is patched
   * first (its valueChanges handler clears sprint/epic), then the linked Sprint/Epic are loaded and
   * pre-selected. The fields stay fully editable afterwards.
   */
  private applyQueryParamSelections(): void {
    if (this.task) {
      return;
    }
    const parentKey = this.activatedRoute.parent?.snapshot.paramMap.get('key') ?? null;
    const projectId = this.activatedRoute.snapshot.queryParamMap.get('projectId');

    const resolveProject = (project: IProject | null): void => {
      if (this.task) {
        return;
      }
      if (project) {
        this.editForm.patchValue({ project });
        this.projectsSharedCollection.update(projects => this.projectService.addProjectToCollectionIfMissing(projects, project));
        this.loadProjectMembers(project.id);
        this.loadProjectScopedOptions(project.id);
        this.isProjectContext.set(true);
      }
      this.preSelectSprint();
      this.preSelectEpic();
    };

    if (parentKey) {
      this.projectService.findByKey(parentKey).subscribe(project => resolveProject(project));
    } else if (projectId) {
      this.projectService.find(Number(projectId)).subscribe(project => resolveProject(project));
    } else {
      resolveProject(null);
    }
  }

  private preSelectSprint(): void {
    if (this.task || this.contextSprintId === null) {
      return;
    }
    this.sprintService.find(this.contextSprintId).subscribe(sprint => {
      this.sprintsSharedCollection.update(sprints => this.sprintService.addSprintToCollectionIfMissing(sprints, sprint));
      this.editForm.patchValue({ sprint });
    });
  }

  private preSelectEpic(): void {
    if (this.task || this.contextEpicId === null) {
      return;
    }
    this.epicService.find(this.contextEpicId).subscribe(epic => {
      this.epicsSharedCollection.update(epics => this.epicService.addEpicToCollectionIfMissing(epics, epic));
      this.editForm.patchValue({ epic });
    });
  }
}
