import { inject } from '@angular/core';
import { ActivatedRouteSnapshot } from '@angular/router';

import { EMPTY, Observable, catchError, of } from 'rxjs';

import { ITask } from '../task.model';
import { TaskService } from '../service/task.service';

const issueResolve = (route: ActivatedRouteSnapshot): Observable<null | ITask> => {
  const { id } = route.params;
  if (id) {
    const service = inject(TaskService);
    return service.find(id).pipe(catchError(() => EMPTY));
  }

  return of(null);
};

export default issueResolve;
