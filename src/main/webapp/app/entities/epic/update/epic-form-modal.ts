import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { EpicStatus } from 'app/entities/enumerations/epic-status.model';
import { Priority } from 'app/entities/enumerations/priority.model';
import { IProject } from 'app/entities/project/project.model';
import { ProjectService } from 'app/entities/project/service/project.service';
import { AlertService } from 'app/core/util/alert.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { ITEM_SAVED_EVENT } from 'app/config/navigation.constants';
import { IEpic } from '../epic.model';
import { EpicService } from '../service/epic.service';
import { EpicFormGroup, EpicFormService } from './epic-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-epic-form-modal',
  templateUrl: './epic-form-modal.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class EpicFormModal implements OnInit {
  epic?: IEpic;
  projectKey?: string;

  readonly isSaving = signal(false);
  readonly isProjectContext = signal(false);
  epicStatusValues = Object.keys(EpicStatus);
  priorityValues = Object.keys(Priority);

  projectsSharedCollection = signal<IProject[]>([]);

  protected activeModal = inject(NgbActiveModal);
  protected epicService = inject(EpicService);
  protected epicFormService = inject(EpicFormService);
  protected projectService = inject(ProjectService);
  protected alertService = inject(AlertService);
  protected translateService = inject(TranslateService);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: EpicFormGroup = this.epicFormService.createEpicFormGroup();

  compareProject = (o1: IProject | null, o2: IProject | null): boolean => this.projectService.compareProject(o1, o2);

  ngOnInit(): void {
    if (this.epic) {
      this.epicFormService.resetForm(this.editForm, this.epic);
      this.projectsSharedCollection.update(projects =>
        this.projectService.addProjectToCollectionIfMissing<IProject>(projects, this.epic!.project),
      );
    }

    this.loadRelationshipsOptions();

    if (this.projectKey && !this.epic) {
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
    const epic = this.epicFormService.getEpic(this.editForm);
    if (epic.id === null) {
      this.subscribeToSaveResponse(this.epicService.create(epic));
    } else {
      this.subscribeToSaveResponse(this.epicService.update(epic));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IEpic | null>): void {
    result.pipe(finalize(() => this.isSaving.set(false))).subscribe({
      next: () => {
        this.epicService.refresh();
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
      .pipe(map((projects: IProject[]) => this.projectService.addProjectToCollectionIfMissing<IProject>(projects, this.epic?.project)))
      .subscribe((projects: IProject[]) => this.projectsSharedCollection.set(projects));
  }
}
