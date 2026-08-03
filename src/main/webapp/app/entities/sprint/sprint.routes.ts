import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import SprintResolve from './route/sprint-routing-resolve.service';

const sprintRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/sprint').then(m => m.Sprint),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/sprint-detail').then(m => m.SprintDetail),
    resolve: {
      sprint: SprintResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/sprint-update').then(m => m.SprintUpdate),
    resolve: {
      sprint: SprintResolve,
    },
    data: { authorities: ['ROLE_ADMIN', 'ROLE_PROJET_MANAGER'] },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/sprint-update').then(m => m.SprintUpdate),
    resolve: {
      sprint: SprintResolve,
    },
    data: { authorities: ['ROLE_ADMIN', 'ROLE_PROJET_MANAGER'] },
    canActivate: [UserRouteAccessService],
  },
];

export default sprintRoute;
