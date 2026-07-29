import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import CommentResolve from './route/comment-routing-resolve.service';

const commentRoute: Routes = [
  {
    path: ':id/view',
    loadComponent: () => import('./detail/comment-detail').then(m => m.CommentDetail),
    resolve: {
      comment: CommentResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default commentRoute;
