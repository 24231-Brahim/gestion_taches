import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';

import { registerAllIcons } from 'src/test/javascript/mocks/fa-icon-library';

import { EpicDetail } from './epic-detail';

describe('Epic Management Detail Component', () => {
  let comp: EpicDetail;
  let fixture: ComponentFixture<EpicDetail>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./epic-detail').then(m => m.EpicDetail),
              resolve: { epic: () => of({ id: 5106 }) },
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
    fixture = TestBed.createComponent(EpicDetail);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load epic on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', EpicDetail);

      // THEN
      expect(instance.epic()).toEqual(expect.objectContaining({ id: 5106 }));
    });
  });

  it('should navigate to the task detail page when a task is selected', () => {
    const router = TestBed.inject(Router);
    const navigateSpy = vitest.spyOn(router, 'navigate').mockResolvedValue(true);

    comp.currentProjectKey.set('KEY');
    comp.onSelectTask({ id: 123, title: 'Task 123' });

    expect(navigateSpy).toHaveBeenCalledWith(['/project', 'KEY', 'task', 123, 'view']);
  });
});
