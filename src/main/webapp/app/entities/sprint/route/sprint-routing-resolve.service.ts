import { inject } from '@angular/core';
import { ActivatedRouteSnapshot } from '@angular/router';

import { EMPTY, Observable, catchError, of } from 'rxjs';

import { SprintService } from '../service/sprint.service';
import { ISprint } from '../sprint.model';

const sprintResolve = (route: ActivatedRouteSnapshot): Observable<null | ISprint> => {
  const { id } = route.params;
  if (id) {
    const service = inject(SprintService);
    return service.find(id).pipe(catchError(() => EMPTY));
  }

  return of(null);
};

export default sprintResolve;
