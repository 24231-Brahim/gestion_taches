import { HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { IIssue } from 'app/entities/issue/issue.model';
import { IssueService } from 'app/entities/issue/service/issue.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { IAttachment } from '../attachment.model';
import { AttachmentService } from '../service/attachment.service';

import { AttachmentFormGroup, AttachmentFormService } from './attachment-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-attachment-update',
  templateUrl: './attachment-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class AttachmentUpdate implements OnInit {
  readonly isSaving = signal(false);
  attachment: IAttachment | null = null;

  issuesSharedCollection = signal<IIssue[]>([]);

  protected attachmentService = inject(AttachmentService);
  protected attachmentFormService = inject(AttachmentFormService);
  protected issueService = inject(IssueService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: AttachmentFormGroup = this.attachmentFormService.createAttachmentFormGroup();

  compareIssue = (o1: IIssue | null, o2: IIssue | null): boolean => this.issueService.compareIssue(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ attachment }) => {
      this.attachment = attachment;
      if (attachment) {
        this.updateForm(attachment);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const attachment = this.attachmentFormService.getAttachment(this.editForm);
    if (attachment.id === null) {
      this.subscribeToSaveResponse(this.attachmentService.create(attachment));
    } else {
      this.subscribeToSaveResponse(this.attachmentService.update(attachment));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IAttachment | null>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving.set(false);
  }

  protected updateForm(attachment: IAttachment): void {
    this.attachment = attachment;
    this.attachmentFormService.resetForm(this.editForm, attachment);

    this.issuesSharedCollection.update(issues => this.issueService.addIssueToCollectionIfMissing<IIssue>(issues, attachment.issue));
  }

  protected loadRelationshipsOptions(): void {
    this.issueService
      .query()
      .pipe(map((res: HttpResponse<IIssue[]>) => res.body ?? []))
      .pipe(map((issues: IIssue[]) => this.issueService.addIssueToCollectionIfMissing<IIssue>(issues, this.attachment?.issue)))
      .subscribe((issues: IIssue[]) => this.issuesSharedCollection.set(issues));
  }
}
