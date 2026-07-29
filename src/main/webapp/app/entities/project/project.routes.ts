import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import ProjectResolve from './route/project-routing-resolve.service';

const projectRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/project').then(m => m.Project),
    data: {
      defaultSort: `key,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/project-update').then(m => m.ProjectUpdate),
    resolve: {
      project: ProjectResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':key/view',
    loadComponent: () => import('./detail/project-detail').then(m => m.ProjectDetail),
    resolve: {
      project: ProjectResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':key/edit',
    loadComponent: () => import('./update/project-update').then(m => m.ProjectUpdate),
    resolve: {
      project: ProjectResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':key/sprint',
    resolve: {
      project: ProjectResolve,
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('../sprint/sprint.routes'),
  },
  {
    path: ':key/epic',
    resolve: {
      project: ProjectResolve,
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('../epic/epic.routes'),
  },
  {
    path: ':key/task',
    resolve: {
      project: ProjectResolve,
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('../task/task.routes'),
  },
];

export default projectRoute;
