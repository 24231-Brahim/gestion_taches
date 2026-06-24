import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { IIssue } from 'app/entities/issue/issue.model';
import { IssueService } from 'app/entities/issue/service/issue.service';
import { IActionHistory } from '../action-history.model';
import { ActionHistoryService } from '../service/action-history.service';

import { ActionHistoryFormService } from './action-history-form.service';
import { ActionHistoryUpdate } from './action-history-update';

describe('ActionHistory Management Update Component', () => {
  let comp: ActionHistoryUpdate;
  let fixture: ComponentFixture<ActionHistoryUpdate>;
  let activatedRoute: ActivatedRoute;
  let actionHistoryFormService: ActionHistoryFormService;
  let actionHistoryService: ActionHistoryService;
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

    fixture = TestBed.createComponent(ActionHistoryUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    actionHistoryFormService = TestBed.inject(ActionHistoryFormService);
    actionHistoryService = TestBed.inject(ActionHistoryService);
    issueService = TestBed.inject(IssueService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Issue query and add missing value', () => {
      const actionHistory: IActionHistory = { id: 6945 };
      const issue: IIssue = { id: 6256 };
      actionHistory.issue = issue;

      const issueCollection: IIssue[] = [{ id: 6256 }];
      vitest.spyOn(issueService, 'query').mockReturnValue(of(new HttpResponse({ body: issueCollection })));
      const additionalIssues = [issue];
      const expectedCollection: IIssue[] = [...additionalIssues, ...issueCollection];
      vitest.spyOn(issueService, 'addIssueToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ actionHistory });
      comp.ngOnInit();

      expect(issueService.query).toHaveBeenCalled();
      expect(issueService.addIssueToCollectionIfMissing).toHaveBeenCalledWith(
        issueCollection,
        ...additionalIssues.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.issuesSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const actionHistory: IActionHistory = { id: 6945 };
      const issue: IIssue = { id: 6256 };
      actionHistory.issue = issue;

      activatedRoute.data = of({ actionHistory });
      comp.ngOnInit();

      expect(comp.issuesSharedCollection()).toContainEqual(issue);
      expect(comp.actionHistory).toEqual(actionHistory);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IActionHistory>();
      const actionHistory = { id: 2047 };
      vitest.spyOn(actionHistoryFormService, 'getActionHistory').mockReturnValue(actionHistory);
      vitest.spyOn(actionHistoryService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ actionHistory });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(actionHistory);
      saveSubject.complete();

      // THEN
      expect(actionHistoryFormService.getActionHistory).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(actionHistoryService.update).toHaveBeenCalledWith(expect.objectContaining(actionHistory));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IActionHistory>();
      const actionHistory = { id: 2047 };
      vitest.spyOn(actionHistoryFormService, 'getActionHistory').mockReturnValue({ id: null });
      vitest.spyOn(actionHistoryService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ actionHistory: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(actionHistory);
      saveSubject.complete();

      // THEN
      expect(actionHistoryFormService.getActionHistory).toHaveBeenCalled();
      expect(actionHistoryService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IActionHistory>();
      const actionHistory = { id: 2047 };
      vitest.spyOn(actionHistoryService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ actionHistory });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(actionHistoryService.update).toHaveBeenCalled();
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
