import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import AttachmentResolve from './route/attachment-routing-resolve.service';

const attachmentRoute: Routes = [
  {
    path: ':id/view',
    loadComponent: () => import('./detail/attachment-detail').then(m => m.AttachmentDetail),
    resolve: {
      attachment: AttachmentResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default attachmentRoute;
