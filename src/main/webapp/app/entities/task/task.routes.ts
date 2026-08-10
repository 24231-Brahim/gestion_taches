import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import TaskResolve from './route/task-routing-resolve.service';

const taskRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/task').then(m => m.Task),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/task-detail').then(m => m.TaskDetail),
    resolve: {
      task: TaskResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/task-update').then(m => m.TaskUpdate),
    resolve: {
      task: TaskResolve,
    },
    data: { authorities: ['ROLE_ADMIN', 'ROLE_PROJET_MANAGER', 'ROLE_DEVELOPER', 'ROLE_USER'] },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/task-update').then(m => m.TaskUpdate),
    resolve: {
      task: TaskResolve,
    },
    // Any authenticated project member may edit a task (status/description/priority/title etc.).
    // Reassignment (changing the assignee) stays OWNER/MANAGER-only and is rejected server-side.
    data: { authorities: ['ROLE_ADMIN', 'ROLE_PROJET_MANAGER', 'ROLE_DEVELOPER', 'ROLE_USER'] },
    canActivate: [UserRouteAccessService],
  },
];

export default taskRoute;
