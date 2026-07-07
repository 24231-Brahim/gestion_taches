import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import AttachmentResolve from './route/attachment-routing-resolve.service';

const attachmentRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/attachment').then(m => m.Attachment),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/attachment-detail').then(m => m.AttachmentDetail),
    resolve: {
      attachment: AttachmentResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/attachment-update').then(m => m.AttachmentUpdate),
    resolve: {
      attachment: AttachmentResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/attachment-update').then(m => m.AttachmentUpdate),
    resolve: {
      attachment: AttachmentResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default attachmentRoute;
