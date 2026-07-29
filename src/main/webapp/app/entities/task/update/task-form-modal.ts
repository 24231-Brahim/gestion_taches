import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ReactiveFormsModule } from '@angular/forms';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { TaskStatus } from 'app/entities/enumerations/task-status.model';
import { Priority } from 'app/entities/enumerations/priority.model';
import { IEpic } from 'app/entities/epic/epic.model';
import { EpicService } from 'app/entities/epic/service/epic.service';
import { IProject, IProjectMember } from 'app/entities/project/project.model';
import { ProjectService } from 'app/entities/project/service/project.service';
import { ISprint } from 'app/entities/sprint/sprint.model';
import { SprintService } from 'app/entities/sprint/service/sprint.service';
import { AlertService } from 'app/core/util/alert.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { ITEM_SAVED_EVENT } from 'app/config/navigation.constants';
import { ITask } from '../task.model';
import { TaskService } from '../service/task.service';
import { TaskFormGroup, TaskFormService } from './task-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-task-form-modal',
  templateUrl: './task-form-modal.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class TaskFormModal implements OnInit {
  task?: ITask;
  projectKey?: string;

  readonly isSaving = signal(false);
  readonly isProjectContext = signal(false);
  taskStatusValues = Object.keys(TaskStatus);
  priorityValues = Object.keys(Priority);

  sprintsSharedCollection = signal<ISprint[]>([]);
  epicsSharedCollection = signal<IEpic[]>([]);
  projectsSharedCollection = signal<IProject[]>([]);
  projectMembers = signal<IProjectMember[]>([]);

  protected activeModal = inject(NgbActiveModal);
  protected taskService = inject(TaskService);
  protected taskFormService = inject(TaskFormService);
  protected sprintService = inject(SprintService);
  protected epicService = inject(EpicService);
  protected projectService = inject(ProjectService);
  protected alertService = inject(AlertService);
  protected translateService = inject(TranslateService);
  protected destroyRef = inject(DestroyRef);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: TaskFormGroup = this.taskFormService.createTaskFormGroup();

  compareSprint = (o1: ISprint | null, o2: ISprint | null): boolean => this.sprintService.compareSprint(o1, o2);
  compareEpic = (o1: IEpic | null, o2: IEpic | null): boolean => this.epicService.compareEpic(o1, o2);
  compareProject = (o1: IProject | null, o2: IProject | null): boolean => this.projectService.compareProject(o1, o2);
  compareProjectMember = (o1: { id: number; login: string } | null, o2: { id: number; login: string } | null): boolean =>
    o1 !== null && o2 !== null ? o1.id === o2.id : o1 === o2;

  ngOnInit(): void {
    if (this.task) {
      this.taskFormService.resetForm(this.editForm, this.task);
      this.sprintsSharedCollection.update(sprints =>
        this.sprintService.addSprintToCollectionIfMissing<ISprint>(sprints, this.task!.sprint),
      );
      this.epicsSharedCollection.update(epics => this.epicService.addEpicToCollectionIfMissing<IEpic>(epics, this.task!.epic));
      this.projectsSharedCollection.update(projects =>
        this.projectService.addProjectToCollectionIfMissing<IProject>(projects, this.task!.project),
      );
    }

    this.loadRelationshipsOptions();

    if (this.projectKey && !this.task) {
      this.projectService.findByKey(this.projectKey).subscribe(project => {
        if (project) {
          this.editForm.patchValue({ project });
          this.loadProjectMembers(project.id!);
          this.isProjectContext.set(true);
        }
      });
    }

    this.editForm
      .get('project')
      ?.valueChanges.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(project => {
        const projectVal = project as IProject | null;
        if (projectVal?.id) {
          this.loadProjectMembers(projectVal.id);
          this.editForm.patchValue({ assignee: null }, { emitEvent: false });
        }
      });
  }

  cancel(): void {
    this.activeModal.dismiss();
  }

  save(): void {
    this.isSaving.set(true);
    const task = this.taskFormService.getTask(this.editForm);
    if (task.id === null) {
      this.subscribeToSaveResponse(this.taskService.create(task));
    } else {
      this.subscribeToSaveResponse(this.taskService.update(task));
    }
  }

  protected subscribeToSaveResponse(result: Observable<ITask | null>): void {
    result.pipe(finalize(() => this.isSaving.set(false))).subscribe({
      next: () => {
        this.taskService.refresh();
        this.activeModal.close(ITEM_SAVED_EVENT);
      },
      error: (err: HttpErrorResponse) => {
        const message = err.error?.detail ?? err.message ?? this.translateService.instant('error.general');
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  protected loadProjectMembers(projectId: number): void {
    this.projectService.getMembers(projectId).subscribe({
      next: members => this.projectMembers.set(members),
    });
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
        const projectId = this.task?.project?.id ?? projects.find(p => this.editForm.get('project')?.value?.id === p.id)?.id;
        if (projectId) {
          this.loadProjectMembers(projectId);
        }
      });
  }
}
