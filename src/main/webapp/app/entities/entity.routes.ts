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
  {
    path: 'sprint',
    data: { pageTitle: 'gestionTachesApp.sprint.home.title' },
    loadChildren: () => import('./sprint/sprint.routes'),
  },
  {
    path: 'epic',
    data: { pageTitle: 'gestionTachesApp.epic.home.title' },
    loadChildren: () => import('./epic/epic.routes'),
  },
  {
    path: 'issue',
    data: { pageTitle: 'gestionTachesApp.issue.home.title' },
    loadChildren: () => import('./issue/issue.routes'),
  },
  {
    path: 'comment',
    data: { pageTitle: 'gestionTachesApp.comment.home.title' },
    loadChildren: () => import('./comment/comment.routes'),
  },
  {
    path: 'attachment',
    data: { pageTitle: 'gestionTachesApp.attachment.home.title' },
    loadChildren: () => import('./attachment/attachment.routes'),
  },
  {
    path: 'action-history',
    data: { pageTitle: 'gestionTachesApp.actionHistory.home.title' },
    loadChildren: () => import('./action-history/action-history.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
