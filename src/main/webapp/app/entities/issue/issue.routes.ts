import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import IssueResolve from './route/issue-routing-resolve.service';

const issueRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/issue').then(m => m.Issue),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/issue-detail').then(m => m.IssueDetail),
    resolve: {
      issue: IssueResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/issue-update').then(m => m.IssueUpdate),
    resolve: {
      issue: IssueResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/issue-update').then(m => m.IssueUpdate),
    resolve: {
      issue: IssueResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default issueRoute;
