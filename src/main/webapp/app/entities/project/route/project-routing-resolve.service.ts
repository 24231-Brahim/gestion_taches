import { inject } from '@angular/core';
import { ActivatedRouteSnapshot } from '@angular/router';

import { Observable, catchError, of } from 'rxjs';

import { IProject } from '../project.model';
import { ProjectService } from '../service/project.service';

const projectResolve = (route: ActivatedRouteSnapshot): Observable<null | IProject> => {
  const { key } = route.params;
  if (key) {
    const service = inject(ProjectService);
    return service.findByKey(key).pipe(catchError(() => of(null)));
  }

  return of(null);
};

export default projectResolve;
