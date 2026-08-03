import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'gestionTachesApp.adminAuthority.home.title', authorities: ['ROLE_ADMIN'] },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'user-management',
    data: { pageTitle: 'userManagement.home.title', authorities: ['ROLE_ADMIN'] },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./admin/user-management/user-management.routes'),
  },
  {
    path: 'project',
    data: { pageTitle: 'gestionTachesApp.project.home.title' },
    loadChildren: () => import('./project/project.routes'),
  },
  {
    path: 'my-tasks',
    data: { pageTitle: 'gestionTachesApp.myTasks.title' },
    loadComponent: () => import('../my-tasks/my-tasks'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
