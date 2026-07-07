import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { IIssue } from 'app/entities/issue/issue.model';
import { IssueService } from 'app/entities/issue/service/issue.service';
import { IAttachment } from '../attachment.model';
import { AttachmentService } from '../service/attachment.service';

import { AttachmentFormService } from './attachment-form.service';
import { AttachmentUpdate } from './attachment-update';

describe('Attachment Management Update Component', () => {
  let comp: AttachmentUpdate;
  let fixture: ComponentFixture<AttachmentUpdate>;
  let activatedRoute: ActivatedRoute;
  let attachmentFormService: AttachmentFormService;
  let attachmentService: AttachmentService;
  let issueService: IssueService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    });

    fixture = TestBed.createComponent(AttachmentUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    attachmentFormService = TestBed.inject(AttachmentFormService);
    attachmentService = TestBed.inject(AttachmentService);
    issueService = TestBed.inject(IssueService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Issue query and add missing value', () => {
      const attachment: IAttachment = { id: 16801 };
      const issue: IIssue = { id: 6256 };
      attachment.issue = issue;

      const issueCollection: IIssue[] = [{ id: 6256 }];
      vitest.spyOn(issueService, 'query').mockReturnValue(of(new HttpResponse({ body: issueCollection })));
      const additionalIssues = [issue];
      const expectedCollection: IIssue[] = [...additionalIssues, ...issueCollection];
      vitest.spyOn(issueService, 'addIssueToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ attachment });
      comp.ngOnInit();

      expect(issueService.query).toHaveBeenCalled();
      expect(issueService.addIssueToCollectionIfMissing).toHaveBeenCalledWith(
        issueCollection,
        ...additionalIssues.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.issuesSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const attachment: IAttachment = { id: 16801 };
      const issue: IIssue = { id: 6256 };
      attachment.issue = issue;

      activatedRoute.data = of({ attachment });
      comp.ngOnInit();

      expect(comp.issuesSharedCollection()).toContainEqual(issue);
      expect(comp.attachment).toEqual(attachment);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IAttachment>();
      const attachment = { id: 8078 };
      vitest.spyOn(attachmentFormService, 'getAttachment').mockReturnValue(attachment);
      vitest.spyOn(attachmentService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ attachment });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(attachment);
      saveSubject.complete();

      // THEN
      expect(attachmentFormService.getAttachment).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(attachmentService.update).toHaveBeenCalledWith(expect.objectContaining(attachment));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IAttachment>();
      const attachment = { id: 8078 };
      vitest.spyOn(attachmentFormService, 'getAttachment').mockReturnValue({ id: null });
      vitest.spyOn(attachmentService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ attachment: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(attachment);
      saveSubject.complete();

      // THEN
      expect(attachmentFormService.getAttachment).toHaveBeenCalled();
      expect(attachmentService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IAttachment>();
      const attachment = { id: 8078 };
      vitest.spyOn(attachmentService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ attachment });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(attachmentService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareIssue', () => {
      it('should forward to issueService', () => {
        const entity = { id: 6256 };
        const entity2 = { id: 29374 };
        vitest.spyOn(issueService, 'compareIssue');
        comp.compareIssue(entity, entity2);
        expect(issueService.compareIssue).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
