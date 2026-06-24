import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, catchError, of } from 'rxjs';

import { IEpic } from '../epic.model';
import { EpicService } from '../service/epic.service';

const epicResolve = (route: ActivatedRouteSnapshot): Observable<null | IEpic> => {
  const { id } = route.params;
  if (id) {
    const router = inject(Router);
    const service = inject(EpicService);
    return service.find(id).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 404) {
          router.navigate(['404']);
        } else {
          router.navigate(['error']);
        }
        return EMPTY;
      }),
    );
  }

  return of(null);
};

export default epicResolve;
