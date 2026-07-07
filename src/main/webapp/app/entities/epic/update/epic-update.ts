import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { EpicStatus } from 'app/entities/enumerations/epic-status.model';
import { Priority } from 'app/entities/enumerations/priority.model';
import { IProject } from 'app/entities/project/project.model';
import { ProjectService } from 'app/entities/project/service/project.service';
import { AlertService } from 'app/core/util/alert.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { IEpic } from '../epic.model';
import { EpicService } from '../service/epic.service';

import { EpicFormGroup, EpicFormService } from './epic-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-epic-update',
  templateUrl: './epic-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class EpicUpdate implements OnInit {
  readonly isSaving = signal(false);
  epic: IEpic | null = null;
  epicStatusValues = Object.keys(EpicStatus);
  priorityValues = Object.keys(Priority);

  projectsSharedCollection = signal<IProject[]>([]);

  protected epicService = inject(EpicService);
  protected epicFormService = inject(EpicFormService);
  protected projectService = inject(ProjectService);
  protected activatedRoute = inject(ActivatedRoute);
  protected alertService = inject(AlertService);
  protected translateService = inject(TranslateService);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: EpicFormGroup = this.epicFormService.createEpicFormGroup();

  compareProject = (o1: IProject | null, o2: IProject | null): boolean => this.projectService.compareProject(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ epic }) => {
      this.epic = epic;
      if (epic) {
        this.updateForm(epic);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const epic = this.epicFormService.getEpic(this.editForm);
    if (epic.id === null) {
      this.subscribeToSaveResponse(this.epicService.create(epic));
    } else {
      this.subscribeToSaveResponse(this.epicService.update(epic));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IEpic | null>): void {
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

  protected updateForm(epic: IEpic): void {
    this.epic = epic;
    this.epicFormService.resetForm(this.editForm, epic);

    this.projectsSharedCollection.update(projects => this.projectService.addProjectToCollectionIfMissing<IProject>(projects, epic.project));
  }

  protected loadRelationshipsOptions(): void {
    this.projectService
      .query()
      .pipe(map((res: HttpResponse<IProject[]>) => res.body ?? []))
      .pipe(map((projects: IProject[]) => this.projectService.addProjectToCollectionIfMissing<IProject>(projects, this.epic?.project)))
      .subscribe((projects: IProject[]) => this.projectsSharedCollection.set(projects));
  }
}
