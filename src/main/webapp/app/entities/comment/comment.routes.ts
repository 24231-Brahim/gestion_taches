import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import CommentResolve from './route/comment-routing-resolve.service';

const commentRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/comment').then(m => m.Comment),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/comment-detail').then(m => m.CommentDetail),
    resolve: {
      comment: CommentResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/comment-update').then(m => m.CommentUpdate),
    resolve: {
      comment: CommentResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/comment-update').then(m => m.CommentUpdate),
    resolve: {
      comment: CommentResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default commentRoute;
