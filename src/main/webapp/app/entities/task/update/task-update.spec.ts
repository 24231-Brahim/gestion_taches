import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, ActivatedRouteSnapshot, Router, convertToParamMap, provideRouter } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { IEpic } from 'app/entities/epic/epic.model';
import { EpicService } from 'app/entities/epic/service/epic.service';
import { IProject } from 'app/entities/project/project.model';
import { ProjectService } from 'app/entities/project/service/project.service';
import { SprintService } from 'app/entities/sprint/service/sprint.service';
import { ISprint } from 'app/entities/sprint/sprint.model';
import { ITask } from '../task.model';
import { TaskService } from '../service/task.service';

import { TaskFormService } from './task-form.service';
import { TaskUpdate } from './task-update';

describe('Task Management Update Component', () => {
  let comp: TaskUpdate;
  let fixture: ComponentFixture<TaskUpdate>;
  let activatedRoute: ActivatedRoute;
  let taskFormService: TaskFormService;
  let taskService: TaskService;
  let sprintService: SprintService;
  let epicService: EpicService;
  let projectService: ProjectService;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
            snapshot: {
              queryParamMap: convertToParamMap({}),
            },
          },
        },
      ],
    });

    fixture = TestBed.createComponent(TaskUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    taskFormService = TestBed.inject(TaskFormService);
    taskService = TestBed.inject(TaskService);
    sprintService = TestBed.inject(SprintService);
    epicService = TestBed.inject(EpicService);
    projectService = TestBed.inject(ProjectService);
    router = TestBed.inject(Router);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Sprint query and add missing value', () => {
      const task: ITask = { id: 29374 };
      const sprint: ISprint = { id: 19154 };
      task.sprint = sprint;

      const sprintCollection: ISprint[] = [{ id: 19154 }];
      vitest.spyOn(sprintService, 'query').mockReturnValue(of(new HttpResponse({ body: sprintCollection })));
      const additionalSprints = [sprint];
      const expectedCollection: ISprint[] = [...additionalSprints, ...sprintCollection];
      vitest.spyOn(sprintService, 'addSprintToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ task });
      comp.ngOnInit();

      expect(sprintService.query).toHaveBeenCalled();
      expect(sprintService.addSprintToCollectionIfMissing).toHaveBeenCalledWith(
        sprintCollection,
        ...additionalSprints.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.sprintsSharedCollection()).toEqual(expectedCollection);
    });

    it('should call Epic query and add missing value', () => {
      const task: ITask = { id: 29374 };
      const epic: IEpic = { id: 5106 };
      task.epic = epic;

      const epicCollection: IEpic[] = [{ id: 5106 }];
      vitest.spyOn(epicService, 'query').mockReturnValue(of(new HttpResponse({ body: epicCollection })));
      const additionalEpics = [epic];
      const expectedCollection: IEpic[] = [...additionalEpics, ...epicCollection];
      vitest.spyOn(epicService, 'addEpicToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ task });
      comp.ngOnInit();

      expect(epicService.query).toHaveBeenCalled();
      expect(epicService.addEpicToCollectionIfMissing).toHaveBeenCalledWith(
        epicCollection,
        ...additionalEpics.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.epicsSharedCollection()).toEqual(expectedCollection);
    });

    it('should call Project query and add missing value', () => {
      const task: ITask = { id: 29374 };

      const projectCollection: IProject[] = [{ id: 10300 }];
      vitest.spyOn(projectService, 'query').mockReturnValue(of(new HttpResponse({ body: projectCollection })));
      const expectedCollection: IProject[] = [{ id: 10300 }];
      vitest.spyOn(projectService, 'addProjectToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ task });
      comp.ngOnInit();

      expect(projectService.query).toHaveBeenCalled();
      expect(comp.projectsSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const task: ITask = { id: 29374 };
      const sprint: ISprint = { id: 19154 };
      task.sprint = sprint;
      const epic: IEpic = { id: 5106 };
      task.epic = epic;
      const project: IProject = { id: 10300 };
      task.project = project;

      activatedRoute.data = of({ task });
      comp.ngOnInit();

      expect(comp.sprintsSharedCollection()).toContainEqual(sprint);
      expect(comp.epicsSharedCollection()).toContainEqual(epic);
      expect(comp.projectsSharedCollection()).toContainEqual(project);
      expect(comp.task).toEqual(task);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<ITask>();
      const task = { id: 6256 };
      vitest.spyOn(taskFormService, 'getTask').mockReturnValue(task);
      vitest.spyOn(taskService, 'update').mockReturnValue(saveSubject);

      activatedRoute.data = of({ task });
      comp.ngOnInit();
      comp.editForm.patchValue({ project: { id: 10300 } });

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(task);
      saveSubject.complete();

      // THEN
      expect(taskFormService.getTask).toHaveBeenCalled();

      expect(taskService.update).toHaveBeenCalledWith(expect.objectContaining(task));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<ITask>();
      const task = { id: 6256 };
      vitest.spyOn(taskFormService, 'getTask').mockReturnValue({ id: null });
      vitest.spyOn(taskService, 'createForProject').mockReturnValue(saveSubject);

      activatedRoute.data = of({ task: null });
      comp.ngOnInit();
      comp.editForm.patchValue({ project: { id: 10300 } });

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(task);
      saveSubject.complete();

      // THEN
      expect(taskFormService.getTask).toHaveBeenCalled();
      expect(taskService.createForProject).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<ITask>();
      const task = { id: 6256 };
      vitest.spyOn(taskService, 'update').mockReturnValue(saveSubject);

      activatedRoute.data = of({ task });
      comp.ngOnInit();
      comp.editForm.patchValue({ project: { id: 10300 } });

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(taskService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
    });
  });

  describe('query param pre-selection', () => {
    const project: IProject = { id: 10300, key: 'KEY', name: 'Proj' };
    const sprint: ISprint = { id: 19154, name: 'Sprint 1' };
    const epic: IEpic = { id: 5106, title: 'Epic 1' };

    beforeEach(() => {
      activatedRoute.snapshot = {
        queryParamMap: convertToParamMap({
          sprintId: '19154',
          epicId: '5106',
          projectId: '10300',
        }),
      } as unknown as ActivatedRouteSnapshot;
      activatedRoute.data = of({ task: null });
      vitest.spyOn(projectService, 'find').mockReturnValue(of(project));
      vitest.spyOn(sprintService, 'find').mockReturnValue(of(sprint));
      vitest.spyOn(epicService, 'find').mockReturnValue(of(epic));
      vitest.spyOn(projectService, 'getMembers').mockReturnValue(of([]));
      vitest.spyOn(sprintService, 'query').mockReturnValue(of(new HttpResponse({ body: [] })));
      vitest.spyOn(epicService, 'query').mockReturnValue(of(new HttpResponse({ body: [] })));
    });

    it('should pre-select project, sprint and epic in creation mode', () => {
      comp.ngOnInit();

      expect(comp.editForm.get('project')?.value).toEqual(project);
      expect(comp.editForm.get('sprint')?.value).toEqual(sprint);
      expect(comp.editForm.get('epic')?.value).toEqual(epic);
    });

    it('should keep pre-selected fields editable', () => {
      comp.ngOnInit();

      comp.editForm.patchValue({ sprint: { id: 999, name: 'Autre sprint' }, epic: null });

      expect(comp.editForm.get('sprint')?.value).toEqual({ id: 999, name: 'Autre sprint' });
      expect(comp.editForm.get('epic')?.value).toBeNull();
    });
  });

  describe('redirect after save with query params', () => {
    it('should navigate to the finally selected sprint detail page', () => {
      const saveSubject = new Subject<ITask>();
      const navigateSpy = vitest.spyOn(router, 'navigate').mockResolvedValue(true);
      activatedRoute.snapshot = {
        queryParamMap: convertToParamMap({
          sprintId: '19154',
          projectId: '10300',
        }),
      } as unknown as ActivatedRouteSnapshot;
      activatedRoute.data = of({ task: null });
      vitest.spyOn(projectService, 'find').mockReturnValue(of({ id: 10300, key: 'KEY', name: 'Proj' }));
      vitest.spyOn(sprintService, 'find').mockReturnValue(of({ id: 19154, name: 'Sprint 1' }));
      vitest.spyOn(projectService, 'getMembers').mockReturnValue(of([]));
      vitest.spyOn(sprintService, 'query').mockReturnValue(of(new HttpResponse({ body: [] })));
      vitest.spyOn(epicService, 'query').mockReturnValue(of(new HttpResponse({ body: [] })));
      vitest.spyOn(taskFormService, 'getTask').mockReturnValue({ id: null });
      vitest.spyOn(taskService, 'createForProject').mockReturnValue(saveSubject);

      comp.ngOnInit();
      comp.save();
      saveSubject.next({ id: 6256 });
      saveSubject.complete();

      expect(navigateSpy).toHaveBeenCalledWith(['/project', 'KEY', 'sprint', 19154, 'view']);
    });

    it('should navigate to the finally selected epic detail page when the sprint was cleared', () => {
      const saveSubject = new Subject<ITask>();
      const navigateSpy = vitest.spyOn(router, 'navigate').mockResolvedValue(true);
      activatedRoute.snapshot = {
        queryParamMap: convertToParamMap({
          sprintId: '19154',
          projectId: '10300',
        }),
      } as unknown as ActivatedRouteSnapshot;
      activatedRoute.data = of({ task: null });
      vitest.spyOn(projectService, 'find').mockReturnValue(of({ id: 10300, key: 'KEY', name: 'Proj' }));
      vitest.spyOn(sprintService, 'find').mockReturnValue(of({ id: 19154, name: 'Sprint 1' }));
      vitest.spyOn(projectService, 'getMembers').mockReturnValue(of([]));
      vitest.spyOn(sprintService, 'query').mockReturnValue(of(new HttpResponse({ body: [] })));
      vitest.spyOn(epicService, 'query').mockReturnValue(of(new HttpResponse({ body: [] })));
      vitest.spyOn(taskFormService, 'getTask').mockReturnValue({ id: null });
      vitest.spyOn(taskService, 'createForProject').mockReturnValue(saveSubject);

      comp.ngOnInit();
      comp.editForm.patchValue({ sprint: null, epic: { id: 5106, title: 'Epic choisi' } });
      comp.save();
      saveSubject.next({ id: 6256 });
      saveSubject.complete();

      expect(navigateSpy).toHaveBeenCalledWith(['/project', 'KEY', 'epic', 5106, 'view']);
    });

    it('should keep the classic previousState navigation when no sprint/epic is selected', () => {
      const saveSubject = new Subject<ITask>();
      const navigateSpy = vitest.spyOn(router, 'navigate');
      const backSpy = vitest.spyOn(globalThis.history, 'back');
      activatedRoute.snapshot = {
        queryParamMap: convertToParamMap({
          sprintId: '19154',
          projectId: '10300',
        }),
      } as unknown as ActivatedRouteSnapshot;
      activatedRoute.data = of({ task: null });
      vitest.spyOn(projectService, 'find').mockReturnValue(of({ id: 10300, key: 'KEY', name: 'Proj' }));
      vitest.spyOn(sprintService, 'find').mockReturnValue(of({ id: 19154, name: 'Sprint 1' }));
      vitest.spyOn(projectService, 'getMembers').mockReturnValue(of([]));
      vitest.spyOn(sprintService, 'query').mockReturnValue(of(new HttpResponse({ body: [] })));
      vitest.spyOn(epicService, 'query').mockReturnValue(of(new HttpResponse({ body: [] })));
      vitest.spyOn(taskFormService, 'getTask').mockReturnValue({ id: null });
      vitest.spyOn(taskService, 'createForProject').mockReturnValue(saveSubject);

      comp.ngOnInit();
      comp.editForm.patchValue({ sprint: null, epic: null });
      comp.save();
      saveSubject.next({ id: 6256 });
      saveSubject.complete();

      expect(navigateSpy).not.toHaveBeenCalled();
      expect(backSpy).toHaveBeenCalled();
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
