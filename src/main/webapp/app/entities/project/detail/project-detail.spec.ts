import { beforeEach, describe, expect, it } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';

import { registerAllIcons } from 'src/test/javascript/mocks/fa-icon-library';

import { ProjectDetail } from './project-detail';

describe('Project Management Detail Component', () => {
  let comp: ProjectDetail;
  let fixture: ComponentFixture<ProjectDetail>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./project-detail').then(m => m.ProjectDetail),
              resolve: { project: () => of({ id: 10300 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    });
    const library = TestBed.inject(FaIconLibrary);
    registerAllIcons(library);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectDetail);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load project on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', ProjectDetail);

      // THEN
      expect(instance.project()).toEqual(expect.objectContaining({ id: 10300 }));
    });
  });
});
