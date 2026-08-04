import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, ActivatedRouteSnapshot, Router, convertToParamMap } from '@angular/router';

import { of, throwError } from 'rxjs';

import { ProjectService } from '../service/project.service';

import projectResolve from './project-routing-resolve.service';

describe('Project routing resolve service', () => {
  let mockRouter: Router;
  let mockActivatedRouteSnapshot: ActivatedRouteSnapshot;
  let service: ProjectService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({}),
            },
          },
        },
      ],
    });
    mockRouter = TestBed.inject(Router);
    vitest.spyOn(mockRouter, 'navigate');
    mockActivatedRouteSnapshot = TestBed.inject(ActivatedRoute).snapshot;
    service = TestBed.inject(ProjectService);
  });

  describe('resolve', () => {
    it('should return IProject returned by findByKey', async () => {
      // GIVEN
      service.findByKey = vitest.fn(id => of({ id }));
      mockActivatedRouteSnapshot.params = { key: 'foo' };

      // WHEN
      await new Promise<void>(resolve => {
        TestBed.runInInjectionContext(() => {
          projectResolve(mockActivatedRouteSnapshot).subscribe({
            next(result) {
              // THEN
              expect(service.findByKey).toHaveBeenCalledWith('foo');
              expect(result).toEqual({ id: 'foo' });
              resolve();
            },
          });
        });
      });
    });

    it('should return null if key is not provided', async () => {
      // GIVEN
      service.findByKey = vitest.fn();
      mockActivatedRouteSnapshot.params = {};

      // WHEN
      await new Promise<void>(resolve => {
        TestBed.runInInjectionContext(() => {
          projectResolve(mockActivatedRouteSnapshot).subscribe({
            next(result) {
              // THEN
              expect(service.findByKey).not.toHaveBeenCalled();
              expect(result).toEqual(null);
              resolve();
            },
          });
        });
      });
    });

    it('should return null if data not found in server', async () => {
      // GIVEN
      vitest.spyOn(service, 'findByKey').mockReturnValue(throwError(() => new HttpErrorResponse({ status: 404, statusText: 'Not Found' })));
      mockActivatedRouteSnapshot.params = { key: 'foo' };

      // WHEN
      await new Promise<void>(resolve => {
        TestBed.runInInjectionContext(() => {
          projectResolve(mockActivatedRouteSnapshot).subscribe({
            next(result) {
              // THEN
              expect(service.findByKey).toHaveBeenCalledWith('foo');
              expect(result).toEqual(null);
              expect(mockRouter.navigate).not.toHaveBeenCalled();
              resolve();
            },
          });
        });
      });
    });

    it('should return null if server returns an error other than 404', async () => {
      // GIVEN
      vitest
        .spyOn(service, 'findByKey')
        .mockReturnValue(throwError(() => new HttpErrorResponse({ status: 500, statusText: 'Internal Server Error' })));
      mockActivatedRouteSnapshot.params = { key: 'foo' };

      // WHEN
      await new Promise<void>(resolve => {
        TestBed.runInInjectionContext(() => {
          projectResolve(mockActivatedRouteSnapshot).subscribe({
            next(result) {
              // THEN
              expect(service.findByKey).toHaveBeenCalledWith('foo');
              expect(result).toEqual(null);
              expect(mockRouter.navigate).not.toHaveBeenCalled();
              resolve();
            },
          });
        });
      });
    });
  });
});
