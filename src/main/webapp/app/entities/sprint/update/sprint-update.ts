import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbInputDatepicker } from '@ng-bootstrap/ng-bootstrap/datepicker';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { SprintStatus } from 'app/entities/enumerations/sprint-status.model';
import { IProject } from 'app/entities/project/project.model';
import { ProjectService } from 'app/entities/project/service/project.service';
import { AlertService } from 'app/core/util/alert.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { SprintService } from '../service/sprint.service';
import { ISprint } from '../sprint.model';

import { SprintFormGroup, SprintFormService } from './sprint-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-sprint-update',
  templateUrl: './sprint-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule, NgbInputDatepicker],
})
export class SprintUpdate implements OnInit {
  readonly isSaving = signal(false);
  readonly isProjectContext = signal(false);
  sprint: ISprint | null = null;
  sprintStatusValues = Object.keys(SprintStatus);

  projectsSharedCollection = signal<IProject[]>([]);

  protected sprintService = inject(SprintService);
  protected sprintFormService = inject(SprintFormService);
  protected projectService = inject(ProjectService);
  protected activatedRoute = inject(ActivatedRoute);
  protected alertService = inject(AlertService);
  protected translateService = inject(TranslateService);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: SprintFormGroup = this.sprintFormService.createSprintFormGroup();

  compareProject = (o1: IProject | null, o2: IProject | null): boolean => this.projectService.compareProject(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ sprint }) => {
      this.sprint = sprint;
      if (sprint) {
        this.updateForm(sprint);
      }

      this.loadRelationshipsOptions();
    });

    // Pre-select project from parent route :key param (e.g. when coming from project-detail)
    const key = this.activatedRoute.parent?.paramMap.subscribe(params => {
      const projectKey = params.get('key');
      if (projectKey && !this.sprint) {
        this.projectService.findByKey(projectKey).subscribe(project => {
          if (project) {
            this.editForm.patchValue({ project });
            this.isProjectContext.set(true);
          }
        });
      }
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const sprint = this.sprintFormService.getSprint(this.editForm);
    if (sprint.id === null) {
      this.subscribeToSaveResponse(this.sprintService.create(sprint));
    } else {
      this.subscribeToSaveResponse(this.sprintService.update(sprint));
    }
  }

  protected subscribeToSaveResponse(result: Observable<ISprint | null>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: (err: HttpErrorResponse) => this.onSaveError(err),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(err: HttpErrorResponse): void {
    const message = err.error?.detail ?? err.message ?? this.translateService.instant('error.general');
    this.alertService.addAlert({ type: 'danger', message });
  }

  protected onSaveFinalize(): void {
    this.isSaving.set(false);
  }

  protected updateForm(sprint: ISprint): void {
    this.sprint = sprint;
    this.sprintFormService.resetForm(this.editForm, sprint);

    this.projectsSharedCollection.update(projects =>
      this.projectService.addProjectToCollectionIfMissing<IProject>(projects, sprint.project),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.projectService
      .query()
      .pipe(map((res: HttpResponse<IProject[]>) => res.body ?? []))
      .pipe(map((projects: IProject[]) => this.projectService.addProjectToCollectionIfMissing<IProject>(projects, this.sprint?.project)))
      .subscribe((projects: IProject[]) => this.projectsSharedCollection.set(projects));
  }
}
