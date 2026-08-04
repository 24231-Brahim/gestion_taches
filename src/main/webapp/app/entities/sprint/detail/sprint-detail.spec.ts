import { beforeEach, describe, expect, it } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';

import { registerAllIcons } from 'src/test/javascript/mocks/fa-icon-library';

import { SprintDetail } from './sprint-detail';

describe('Sprint Management Detail Component', () => {
  let comp: SprintDetail;
  let fixture: ComponentFixture<SprintDetail>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./sprint-detail').then(m => m.SprintDetail),
              resolve: { sprint: () => of({ id: 19154 }) },
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
    fixture = TestBed.createComponent(SprintDetail);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load sprint on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', SprintDetail);

      // THEN
      expect(instance.sprint()).toEqual(expect.objectContaining({ id: 19154 }));
    });
  });
});
