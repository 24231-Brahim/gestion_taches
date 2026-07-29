import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import ActionHistoryResolve from './route/action-history-routing-resolve.service';

const actionHistoryRoute: Routes = [
  {
    path: ':id/view',
    loadComponent: () => import('./detail/action-history-detail').then(m => m.ActionHistoryDetail),
    resolve: {
      actionHistory: ActionHistoryResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default actionHistoryRoute;
