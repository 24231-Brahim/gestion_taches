import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Observable, finalize } from 'rxjs';

import { AlertService } from 'app/core/util/alert.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { IProject } from '../project.model';
import { ProjectService } from '../service/project.service';

import { ProjectFormGroup, ProjectFormService } from './project-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-project-update',
  templateUrl: './project-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class ProjectUpdate implements OnInit {
  readonly isSaving = signal(false);
  project: IProject | null = null;

  protected projectService = inject(ProjectService);
  protected projectFormService = inject(ProjectFormService);
  protected activatedRoute = inject(ActivatedRoute);
  protected alertService = inject(AlertService);
  protected translateService = inject(TranslateService);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: ProjectFormGroup = this.projectFormService.createProjectFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ project }) => {
      this.project = project;
      if (project) {
        this.updateForm(project);
      }
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const project = this.projectFormService.getProject(this.editForm);
    if (project.id === null) {
      this.subscribeToSaveResponse(this.projectService.create(project));
    } else {
      this.subscribeToSaveResponse(this.projectService.update(project));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IProject | null>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: (err: HttpErrorResponse) => this.onSaveError(err),
    });
  }

  protected onSaveSuccess(): void {
    this.projectService.refresh();
    this.previousState();
  }

  protected onSaveError(err: HttpErrorResponse): void {
    const message = err.error?.detail ?? err.message ?? this.translateService.instant('error.general');
    this.alertService.addAlert({ type: 'danger', message });
  }

  protected onSaveFinalize(): void {
    this.isSaving.set(false);
  }

  protected updateForm(project: IProject): void {
    this.project = project;
    this.projectFormService.resetForm(this.editForm, project);
  }
}
