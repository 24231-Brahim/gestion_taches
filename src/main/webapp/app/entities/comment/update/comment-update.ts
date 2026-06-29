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
import { IComment } from '../comment.model';
import { CommentService } from '../service/comment.service';

import { CommentFormGroup, CommentFormService } from './comment-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-comment-update',
  templateUrl: './comment-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class CommentUpdate implements OnInit {
  readonly isSaving = signal(false);
  comment: IComment | null = null;

  issuesSharedCollection = signal<IIssue[]>([]);

  protected commentService = inject(CommentService);
  protected commentFormService = inject(CommentFormService);
  protected issueService = inject(IssueService);
  protected activatedRoute = inject(ActivatedRoute);
  protected alertService = inject(AlertService);
  protected translateService = inject(TranslateService);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: CommentFormGroup = this.commentFormService.createCommentFormGroup();

  compareIssue = (o1: IIssue | null, o2: IIssue | null): boolean => this.issueService.compareIssue(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ comment }) => {
      this.comment = comment;
      if (comment) {
        this.updateForm(comment);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const comment = this.commentFormService.getComment(this.editForm);
    if (comment.id === null) {
      this.subscribeToSaveResponse(this.commentService.create(comment));
    } else {
      this.subscribeToSaveResponse(this.commentService.update(comment));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IComment | null>): void {
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

  protected updateForm(comment: IComment): void {
    this.comment = comment;
    this.commentFormService.resetForm(this.editForm, comment);

    this.issuesSharedCollection.update(issues => this.issueService.addIssueToCollectionIfMissing<IIssue>(issues, comment.issue));
  }

  protected loadRelationshipsOptions(): void {
    this.issueService
      .query()
      .pipe(map((res: HttpResponse<IIssue[]>) => res.body ?? []))
      .pipe(map((issues: IIssue[]) => this.issueService.addIssueToCollectionIfMissing<IIssue>(issues, this.comment?.issue)))
      .subscribe((issues: IIssue[]) => this.issuesSharedCollection.set(issues));
  }
}
