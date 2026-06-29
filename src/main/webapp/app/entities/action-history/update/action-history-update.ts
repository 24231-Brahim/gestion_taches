import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { IIssue } from 'app/entities/issue/issue.model';
import { IssueService } from 'app/entities/issue/service/issue.service';
import { AlertService } from 'app/core/util/alert.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { IActionHistory } from '../action-history.model';
import { ActionHistoryService } from '../service/action-history.service';

import { ActionHistoryFormGroup, ActionHistoryFormService } from './action-history-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-action-history-update',
  templateUrl: './action-history-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class ActionHistoryUpdate implements OnInit {
  readonly isSaving = signal(false);
  actionHistory: IActionHistory | null = null;

  issuesSharedCollection = signal<IIssue[]>([]);

  protected actionHistoryService = inject(ActionHistoryService);
  protected actionHistoryFormService = inject(ActionHistoryFormService);
  protected issueService = inject(IssueService);
  protected activatedRoute = inject(ActivatedRoute);
  protected alertService = inject(AlertService);
  protected translateService = inject(TranslateService);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: ActionHistoryFormGroup = this.actionHistoryFormService.createActionHistoryFormGroup();

  compareIssue = (o1: IIssue | null, o2: IIssue | null): boolean => this.issueService.compareIssue(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ actionHistory }) => {
      this.actionHistory = actionHistory;
      if (actionHistory) {
        this.updateForm(actionHistory);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const actionHistory = this.actionHistoryFormService.getActionHistory(this.editForm);
    if (actionHistory.id === null) {
      this.subscribeToSaveResponse(this.actionHistoryService.create(actionHistory));
    } else {
      this.subscribeToSaveResponse(this.actionHistoryService.update(actionHistory));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IActionHistory | null>): void {
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

  protected updateForm(actionHistory: IActionHistory): void {
    this.actionHistory = actionHistory;
    this.actionHistoryFormService.resetForm(this.editForm, actionHistory);

    this.issuesSharedCollection.update(issues => this.issueService.addIssueToCollectionIfMissing<IIssue>(issues, actionHistory.issue));
  }

  protected loadRelationshipsOptions(): void {
    this.issueService
      .query()
      .pipe(map((res: HttpResponse<IIssue[]>) => res.body ?? []))
      .pipe(map((issues: IIssue[]) => this.issueService.addIssueToCollectionIfMissing<IIssue>(issues, this.actionHistory?.issue)))
      .subscribe((issues: IIssue[]) => this.issuesSharedCollection.set(issues));
  }
}
