import {
  entityConfirmDeleteButtonSelector,
  entityCreateButtonSelector,
  entityCreateCancelButtonSelector,
  entityCreateSaveButtonSelector,
  entityDeleteButtonSelector,
  entityEditButtonSelector,
  entityTableSelector,
} from '../../support/entity';

describe('Task e2e test', () => {
  let username: string;
  let password: string;

  let project;
  let task;

  before(() => {
    cy.credentials().then(credentials => {
      ({ username, password } = credentials);
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  const createProject = () =>
    cy
      .authenticatedRequest({
        method: 'POST',
        url: '/api/projects',
        body: { name: `E2E Task ${Date.now()}`, description: 'e2e', key: `E2ETK${Date.now()}` },
      })
      .then(({ body }) => {
        project = body;
      });

  const createTask = () =>
    cy
      .authenticatedRequest({
        method: 'POST',
        url: '/api/tasks',
        body: { title: 'e2e task', status: 'NEW', priority: 'MEDIUM', project },
      })
      .then(({ body }) => {
        task = body;
      });

  const taskListUrl = () => `/project/${project.key}/task`;
  const taskListUrlPattern = key => new RegExp(`/project/${key}/task(\\?.*)?$`);

  afterEach(() => {
    if (task) {
      cy.authenticatedRequest({ method: 'DELETE', url: `/api/tasks/${task.id}` }).then(() => {
        task = undefined;
      });
    }
  });

  afterEach(() => {
    if (project) {
      cy.authenticatedRequest({ method: 'DELETE', url: `/api/projects/${project.id}` }).then(() => {
        project = undefined;
      });
    }
  });

  describe('project task list', () => {
    beforeEach(() => {
      createProject();
      cy.intercept('GET', '/api/tasks+(?*|)').as('entitiesRequest');
    });

    it('should display the tasks of a project', () => {
      cy.visit(taskListUrl());
      cy.wait('@entitiesRequest');
      cy.get('[data-cy="TaskHeading"]').should('exist');
      cy.url().should('match', taskListUrlPattern(project.key));
    });

    it('should load the create Task page', () => {
      cy.visit(taskListUrl());
      cy.wait('@entitiesRequest');
      cy.get(entityCreateButtonSelector).click();
      cy.url().should('match', new RegExp(`/project/${project.key}/task/new$`));
      cy.get('[data-cy="TaskCreateUpdateHeading"]').should('exist');
      cy.get(entityCreateSaveButtonSelector).should('exist');
      cy.get(entityCreateCancelButtonSelector).click();
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', taskListUrlPattern(project.key));
    });
  });

  describe('with existing task', () => {
    beforeEach(() => {
      createProject().then(() => createTask());
      cy.intercept('GET', '/api/tasks+(?*|)').as('entitiesRequest');
      cy.intercept('DELETE', '/api/tasks/*').as('deleteEntityRequest');
      cy.visit(taskListUrl());
      cy.wait('@entitiesRequest');
    });

    it('row click should open the task detail drawer', () => {
      cy.get(entityTableSelector).first().click();
      cy.get('.task-drawer-overlay').should('exist');
    });

    it('edit button click should load edit Task page and save', () => {
      cy.get(entityEditButtonSelector).first().click();
      cy.url().should('match', new RegExp(`/project/${project.key}/task/${task.id}/edit$`));
      cy.get('[data-cy="TaskCreateUpdateHeading"]').should('exist');
      cy.get(entityCreateSaveButtonSelector).click();
      cy.url().should('match', taskListUrlPattern(project.key));
    });

    it('last delete button click should delete instance of Task', () => {
      cy.get(entityDeleteButtonSelector).last().click();
      cy.get('[data-cy="taskDeleteDialogHeading"]').should('exist');
      cy.get(entityConfirmDeleteButtonSelector).click();
      cy.wait('@deleteEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(204);
      });
      cy.url().should('match', taskListUrlPattern(project.key));
      task = undefined;
    });
  });
});
