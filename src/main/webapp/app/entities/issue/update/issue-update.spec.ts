import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { IEpic } from 'app/entities/epic/epic.model';
import { EpicService } from 'app/entities/epic/service/epic.service';
import { IProject } from 'app/entities/project/project.model';
import { ProjectService } from 'app/entities/project/service/project.service';
import { SprintService } from 'app/entities/sprint/service/sprint.service';
import { ISprint } from 'app/entities/sprint/sprint.model';
import { IIssue } from '../issue.model';
import { IssueService } from '../service/issue.service';

import { IssueFormService } from './issue-form.service';
import { IssueUpdate } from './issue-update';

describe('Issue Management Update Component', () => {
  let comp: IssueUpdate;
  let fixture: ComponentFixture<IssueUpdate>;
  let activatedRoute: ActivatedRoute;
  let issueFormService: IssueFormService;
  let issueService: IssueService;
  let sprintService: SprintService;
  let epicService: EpicService;
  let projectService: ProjectService;

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

    fixture = TestBed.createComponent(IssueUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    issueFormService = TestBed.inject(IssueFormService);
    issueService = TestBed.inject(IssueService);
    sprintService = TestBed.inject(SprintService);
    epicService = TestBed.inject(EpicService);
    projectService = TestBed.inject(ProjectService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Sprint query and add missing value', () => {
      const issue: IIssue = { id: 29374 };
      const sprint: ISprint = { id: 19154 };
      issue.sprint = sprint;

      const sprintCollection: ISprint[] = [{ id: 19154 }];
      vitest.spyOn(sprintService, 'query').mockReturnValue(of(new HttpResponse({ body: sprintCollection })));
      const additionalSprints = [sprint];
      const expectedCollection: ISprint[] = [...additionalSprints, ...sprintCollection];
      vitest.spyOn(sprintService, 'addSprintToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ issue });
      comp.ngOnInit();

      expect(sprintService.query).toHaveBeenCalled();
      expect(sprintService.addSprintToCollectionIfMissing).toHaveBeenCalledWith(
        sprintCollection,
        ...additionalSprints.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.sprintsSharedCollection()).toEqual(expectedCollection);
    });

    it('should call Epic query and add missing value', () => {
      const issue: IIssue = { id: 29374 };
      const epic: IEpic = { id: 5106 };
      issue.epic = epic;

      const epicCollection: IEpic[] = [{ id: 5106 }];
      vitest.spyOn(epicService, 'query').mockReturnValue(of(new HttpResponse({ body: epicCollection })));
      const additionalEpics = [epic];
      const expectedCollection: IEpic[] = [...additionalEpics, ...epicCollection];
      vitest.spyOn(epicService, 'addEpicToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ issue });
      comp.ngOnInit();

      expect(epicService.query).toHaveBeenCalled();
      expect(epicService.addEpicToCollectionIfMissing).toHaveBeenCalledWith(
        epicCollection,
        ...additionalEpics.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.epicsSharedCollection()).toEqual(expectedCollection);
    });

    it('should call Project query and add missing value', () => {
      const issue: IIssue = { id: 29374 };
      const project: IProject = { id: 10300 };
      issue.project = project;

      const projectCollection: IProject[] = [{ id: 10300 }];
      vitest.spyOn(projectService, 'query').mockReturnValue(of(new HttpResponse({ body: projectCollection })));
      const additionalProjects = [project];
      const expectedCollection: IProject[] = [...additionalProjects, ...projectCollection];
      vitest.spyOn(projectService, 'addProjectToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ issue });
      comp.ngOnInit();

      expect(projectService.query).toHaveBeenCalled();
      expect(projectService.addProjectToCollectionIfMissing).toHaveBeenCalledWith(
        projectCollection,
        ...additionalProjects.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.projectsSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const issue: IIssue = { id: 29374 };
      const sprint: ISprint = { id: 19154 };
      issue.sprint = sprint;
      const epic: IEpic = { id: 5106 };
      issue.epic = epic;
      const project: IProject = { id: 10300 };
      issue.project = project;

      activatedRoute.data = of({ issue });
      comp.ngOnInit();

      expect(comp.sprintsSharedCollection()).toContainEqual(sprint);
      expect(comp.epicsSharedCollection()).toContainEqual(epic);
      expect(comp.projectsSharedCollection()).toContainEqual(project);
      expect(comp.issue).toEqual(issue);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IIssue>();
      const issue = { id: 6256 };
      vitest.spyOn(issueFormService, 'getIssue').mockReturnValue(issue);
      vitest.spyOn(issueService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ issue });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(issue);
      saveSubject.complete();

      // THEN
      expect(issueFormService.getIssue).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(issueService.update).toHaveBeenCalledWith(expect.objectContaining(issue));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IIssue>();
      const issue = { id: 6256 };
      vitest.spyOn(issueFormService, 'getIssue').mockReturnValue({ id: null });
      vitest.spyOn(issueService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ issue: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(issue);
      saveSubject.complete();

      // THEN
      expect(issueFormService.getIssue).toHaveBeenCalled();
      expect(issueService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IIssue>();
      const issue = { id: 6256 };
      vitest.spyOn(issueService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ issue });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(issueService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareSprint', () => {
      it('should forward to sprintService', () => {
        const entity = { id: 19154 };
        const entity2 = { id: 28936 };
        vitest.spyOn(sprintService, 'compareSprint');
        comp.compareSprint(entity, entity2);
        expect(sprintService.compareSprint).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareEpic', () => {
      it('should forward to epicService', () => {
        const entity = { id: 5106 };
        const entity2 = { id: 11594 };
        vitest.spyOn(epicService, 'compareEpic');
        comp.compareEpic(entity, entity2);
        expect(epicService.compareEpic).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareProject', () => {
      it('should forward to projectService', () => {
        const entity = { id: 10300 };
        const entity2 = { id: 3319 };
        vitest.spyOn(projectService, 'compareProject');
        comp.compareProject(entity, entity2);
        expect(projectService.compareProject).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
