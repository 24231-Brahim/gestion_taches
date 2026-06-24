import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { IProject } from 'app/entities/project/project.model';
import { ProjectService } from 'app/entities/project/service/project.service';
import { SprintService } from '../service/sprint.service';
import { ISprint } from '../sprint.model';

import { SprintFormService } from './sprint-form.service';
import { SprintUpdate } from './sprint-update';

describe('Sprint Management Update Component', () => {
  let comp: SprintUpdate;
  let fixture: ComponentFixture<SprintUpdate>;
  let activatedRoute: ActivatedRoute;
  let sprintFormService: SprintFormService;
  let sprintService: SprintService;
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

    fixture = TestBed.createComponent(SprintUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    sprintFormService = TestBed.inject(SprintFormService);
    sprintService = TestBed.inject(SprintService);
    projectService = TestBed.inject(ProjectService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Project query and add missing value', () => {
      const sprint: ISprint = { id: 28936 };
      const project: IProject = { id: 10300 };
      sprint.project = project;

      const projectCollection: IProject[] = [{ id: 10300 }];
      vitest.spyOn(projectService, 'query').mockReturnValue(of(new HttpResponse({ body: projectCollection })));
      const additionalProjects = [project];
      const expectedCollection: IProject[] = [...additionalProjects, ...projectCollection];
      vitest.spyOn(projectService, 'addProjectToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ sprint });
      comp.ngOnInit();

      expect(projectService.query).toHaveBeenCalled();
      expect(projectService.addProjectToCollectionIfMissing).toHaveBeenCalledWith(
        projectCollection,
        ...additionalProjects.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.projectsSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const sprint: ISprint = { id: 28936 };
      const project: IProject = { id: 10300 };
      sprint.project = project;

      activatedRoute.data = of({ sprint });
      comp.ngOnInit();

      expect(comp.projectsSharedCollection()).toContainEqual(project);
      expect(comp.sprint).toEqual(sprint);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<ISprint>();
      const sprint = { id: 19154 };
      vitest.spyOn(sprintFormService, 'getSprint').mockReturnValue(sprint);
      vitest.spyOn(sprintService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ sprint });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(sprint);
      saveSubject.complete();

      // THEN
      expect(sprintFormService.getSprint).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(sprintService.update).toHaveBeenCalledWith(expect.objectContaining(sprint));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<ISprint>();
      const sprint = { id: 19154 };
      vitest.spyOn(sprintFormService, 'getSprint').mockReturnValue({ id: null });
      vitest.spyOn(sprintService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ sprint: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(sprint);
      saveSubject.complete();

      // THEN
      expect(sprintFormService.getSprint).toHaveBeenCalled();
      expect(sprintService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<ISprint>();
      const sprint = { id: 19154 };
      vitest.spyOn(sprintService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ sprint });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(sprintService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
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
