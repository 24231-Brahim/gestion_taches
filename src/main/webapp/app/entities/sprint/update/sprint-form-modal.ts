import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ReactiveFormsModule } from '@angular/forms';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbActiveModal, NgbInputDatepicker } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { SprintStatus } from 'app/entities/enumerations/sprint-status.model';
import { IProject } from 'app/entities/project/project.model';
import { ProjectService } from 'app/entities/project/service/project.service';
import { AlertService } from 'app/core/util/alert.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { ITEM_SAVED_EVENT } from 'app/config/navigation.constants';
import { ISprint } from '../sprint.model';
import { SprintService } from '../service/sprint.service';
import { SprintFormGroup, SprintFormService } from './sprint-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-sprint-form-modal',
  templateUrl: './sprint-form-modal.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule, NgbInputDatepicker],
})
export class SprintFormModal implements OnInit {
  sprint?: ISprint;
  projectKey?: string;

  readonly isSaving = signal(false);
  readonly isProjectContext = signal(false);
  sprintStatusValues = Object.keys(SprintStatus);

  projectsSharedCollection = signal<IProject[]>([]);

  protected activeModal = inject(NgbActiveModal);
  protected sprintService = inject(SprintService);
  protected sprintFormService = inject(SprintFormService);
  protected projectService = inject(ProjectService);
  protected alertService = inject(AlertService);
  protected translateService = inject(TranslateService);
  protected destroyRef = inject(DestroyRef);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: SprintFormGroup = this.sprintFormService.createSprintFormGroup();

  compareProject = (o1: IProject | null, o2: IProject | null): boolean => this.projectService.compareProject(o1, o2);

  ngOnInit(): void {
    if (this.sprint) {
      this.sprintFormService.resetForm(this.editForm, this.sprint);
      this.projectsSharedCollection.update(projects =>
        this.projectService.addProjectToCollectionIfMissing<IProject>(projects, this.sprint!.project),
      );
    }

    this.loadRelationshipsOptions();

    if (this.projectKey && !this.sprint) {
      this.projectService.findByKey(this.projectKey).subscribe(project => {
        if (project) {
          this.editForm.patchValue({ project });
          this.isProjectContext.set(true);
        }
      });
    }
  }

  cancel(): void {
    this.activeModal.dismiss();
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
    result.pipe(finalize(() => this.isSaving.set(false))).subscribe({
      next: () => {
        this.sprintService.refresh();
        this.activeModal.close(ITEM_SAVED_EVENT);
      },
      error: (err: HttpErrorResponse) => {
        const message = err.error?.detail ?? err.message ?? this.translateService.instant('error.general');
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }

  protected loadRelationshipsOptions(): void {
    this.projectService
      .query()
      .pipe(map((res: HttpResponse<IProject[]>) => res.body ?? []))
      .pipe(map((projects: IProject[]) => this.projectService.addProjectToCollectionIfMissing<IProject>(projects, this.sprint?.project)))
      .subscribe((projects: IProject[]) => this.projectsSharedCollection.set(projects));
  }
}
