import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { IIssue } from 'app/entities/issue/issue.model';
import { IssueService } from 'app/entities/issue/service/issue.service';
import { IComment } from '../comment.model';
import { CommentService } from '../service/comment.service';

import { CommentFormService } from './comment-form.service';
import { CommentUpdate } from './comment-update';

describe('Comment Management Update Component', () => {
  let comp: CommentUpdate;
  let fixture: ComponentFixture<CommentUpdate>;
  let activatedRoute: ActivatedRoute;
  let commentFormService: CommentFormService;
  let commentService: CommentService;
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

    fixture = TestBed.createComponent(CommentUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    commentFormService = TestBed.inject(CommentFormService);
    commentService = TestBed.inject(CommentService);
    issueService = TestBed.inject(IssueService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Issue query and add missing value', () => {
      const comment: IComment = { id: 24616 };
      const issue: IIssue = { id: 6256 };
      comment.issue = issue;

      const issueCollection: IIssue[] = [{ id: 6256 }];
      vitest.spyOn(issueService, 'query').mockReturnValue(of(new HttpResponse({ body: issueCollection })));
      const additionalIssues = [issue];
      const expectedCollection: IIssue[] = [...additionalIssues, ...issueCollection];
      vitest.spyOn(issueService, 'addIssueToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ comment });
      comp.ngOnInit();

      expect(issueService.query).toHaveBeenCalled();
      expect(issueService.addIssueToCollectionIfMissing).toHaveBeenCalledWith(
        issueCollection,
        ...additionalIssues.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.issuesSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const comment: IComment = { id: 24616 };
      const issue: IIssue = { id: 6256 };
      comment.issue = issue;

      activatedRoute.data = of({ comment });
      comp.ngOnInit();

      expect(comp.issuesSharedCollection()).toContainEqual(issue);
      expect(comp.comment).toEqual(comment);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IComment>();
      const comment = { id: 25492 };
      vitest.spyOn(commentFormService, 'getComment').mockReturnValue(comment);
      vitest.spyOn(commentService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ comment });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(comment);
      saveSubject.complete();

      // THEN
      expect(commentFormService.getComment).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(commentService.update).toHaveBeenCalledWith(expect.objectContaining(comment));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IComment>();
      const comment = { id: 25492 };
      vitest.spyOn(commentFormService, 'getComment').mockReturnValue({ id: null });
      vitest.spyOn(commentService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ comment: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(comment);
      saveSubject.complete();

      // THEN
      expect(commentFormService.getComment).toHaveBeenCalled();
      expect(commentService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IComment>();
      const comment = { id: 25492 };
      vitest.spyOn(commentService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ comment });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(commentService.update).toHaveBeenCalled();
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
