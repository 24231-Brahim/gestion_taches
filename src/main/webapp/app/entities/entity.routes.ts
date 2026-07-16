import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'gestionTachesApp.adminAuthority.home.title' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'user-management',
    data: { pageTitle: 'userManagement.home.title' },
    loadChildren: () => import('./admin/user-management/user-management.routes'),
  },
  {
    path: 'project',
    data: { pageTitle: 'gestionTachesApp.project.home.title' },
    loadChildren: () => import('./project/project.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
