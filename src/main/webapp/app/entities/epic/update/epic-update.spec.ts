import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { IProject } from 'app/entities/project/project.model';
import { ProjectService } from 'app/entities/project/service/project.service';
import { IEpic } from '../epic.model';
import { EpicService } from '../service/epic.service';

import { EpicFormService } from './epic-form.service';
import { EpicUpdate } from './epic-update';

describe('Epic Management Update Component', () => {
  let comp: EpicUpdate;
  let fixture: ComponentFixture<EpicUpdate>;
  let activatedRoute: ActivatedRoute;
  let epicFormService: EpicFormService;
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

    fixture = TestBed.createComponent(EpicUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    epicFormService = TestBed.inject(EpicFormService);
    epicService = TestBed.inject(EpicService);
    projectService = TestBed.inject(ProjectService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Project query and add missing value', () => {
      const epic: IEpic = { id: 11594 };
      const project: IProject = { id: 10300 };
      epic.project = project;

      const projectCollection: IProject[] = [{ id: 10300 }];
      vitest.spyOn(projectService, 'query').mockReturnValue(of(new HttpResponse({ body: projectCollection })));
      const additionalProjects = [project];
      const expectedCollection: IProject[] = [...additionalProjects, ...projectCollection];
      vitest.spyOn(projectService, 'addProjectToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ epic });
      comp.ngOnInit();

      expect(projectService.query).toHaveBeenCalled();
      expect(projectService.addProjectToCollectionIfMissing).toHaveBeenCalledWith(
        projectCollection,
        ...additionalProjects.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.projectsSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const epic: IEpic = { id: 11594 };
      const project: IProject = { id: 10300 };
      epic.project = project;

      activatedRoute.data = of({ epic });
      comp.ngOnInit();

      expect(comp.projectsSharedCollection()).toContainEqual(project);
      expect(comp.epic).toEqual(epic);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IEpic>();
      const epic = { id: 5106 };
      vitest.spyOn(epicFormService, 'getEpic').mockReturnValue(epic);
      vitest.spyOn(epicService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ epic });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(epic);
      saveSubject.complete();

      // THEN
      expect(epicFormService.getEpic).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(epicService.update).toHaveBeenCalledWith(expect.objectContaining(epic));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IEpic>();
      const epic = { id: 5106 };
      vitest.spyOn(epicFormService, 'getEpic').mockReturnValue({ id: null });
      vitest.spyOn(epicService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ epic: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(epic);
      saveSubject.complete();

      // THEN
      expect(epicFormService.getEpic).toHaveBeenCalled();
      expect(epicService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IEpic>();
      const epic = { id: 5106 };
      vitest.spyOn(epicService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ epic });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(epicService.update).toHaveBeenCalled();
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
