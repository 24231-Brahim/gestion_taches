import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import ActionHistoryResolve from './route/action-history-routing-resolve.service';

const actionHistoryRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/action-history').then(m => m.ActionHistory),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/action-history-detail').then(m => m.ActionHistoryDetail),
    resolve: {
      actionHistory: ActionHistoryResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/action-history-update').then(m => m.ActionHistoryUpdate),
    resolve: {
      actionHistory: ActionHistoryResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/action-history-update').then(m => m.ActionHistoryUpdate),
    resolve: {
      actionHistory: ActionHistoryResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default actionHistoryRoute;
