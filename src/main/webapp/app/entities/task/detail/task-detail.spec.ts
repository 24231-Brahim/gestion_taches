import { beforeEach, describe, expect, it } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';

import { registerAllIcons } from 'src/test/javascript/mocks/fa-icon-library';

import { TaskDetail } from './task-detail';

describe('Task Management Detail Component', () => {
  let comp: TaskDetail;
  let fixture: ComponentFixture<TaskDetail>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./task-detail').then(m => m.TaskDetail),
              resolve: { task: () => of({ id: 6256 }) },
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
    fixture = TestBed.createComponent(TaskDetail);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load task on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', TaskDetail);

      // THEN
      expect(instance.task()).toEqual(expect.objectContaining({ id: 6256 }));
    });
  });
});
