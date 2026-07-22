import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Observable, finalize } from 'rxjs';

import { AlertService } from 'app/core/util/alert.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { ITEM_SAVED_EVENT } from 'app/config/navigation.constants';
import { IProject } from '../project.model';
import { ProjectService } from '../service/project.service';
import { ProjectFormGroup, ProjectFormService } from './project-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-project-form-modal',
  templateUrl: './project-form-modal.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class ProjectFormModal {
  project?: IProject;

  readonly isSaving = signal(false);

  protected activeModal = inject(NgbActiveModal);
  protected projectService = inject(ProjectService);
  protected projectFormService = inject(ProjectFormService);
  protected alertService = inject(AlertService);
  protected translateService = inject(TranslateService);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: ProjectFormGroup = this.projectFormService.createProjectFormGroup();

  ngOnInit(): void {
    if (this.project) {
      this.projectFormService.resetForm(this.editForm, this.project);
    }
  }

  cancel(): void {
    this.activeModal.dismiss();
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
    result.pipe(finalize(() => this.isSaving.set(false))).subscribe({
      next: () => {
        this.projectService.refresh();
        this.activeModal.close(ITEM_SAVED_EVENT);
      },
      error: (err: HttpErrorResponse) => {
        const message = err.error?.detail ?? err.message ?? this.translateService.instant('error.general');
        this.alertService.addAlert({ type: 'danger', message });
      },
    });
  }
}
