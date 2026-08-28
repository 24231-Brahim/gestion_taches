import { entityConfirmDeleteButtonSelector, entityCreateSaveButtonSelector } from '../../support/entity';

describe('Sprint e2e test', () => {
  let username: string;
  let password: string;

  let project;
  let sprint;

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
        body: { name: `E2E Sprint ${Date.now()}`, description: 'e2e', key: `E2ESP${Date.now()}` },
      })
      .then(({ body }) => {
        project = body;
      });

  const createSprint = () =>
    cy
      .authenticatedRequest({
        method: 'POST',
        url: '/api/sprints',
        body: { name: 'e2e sprint', status: 'PLANNED', project },
      })
      .then(({ body }) => {
        sprint = body;
      });

  const sprintListUrl = () => `/project/${project.key}/sprint`;
  const sprintUrlPattern = key => new RegExp(`/project/${key}/sprint(.*)$`);

  afterEach(() => {
    if (sprint) {
      cy.authenticatedRequest({ method: 'DELETE', url: `/api/sprints/${sprint.id}` }).then(() => {
        sprint = undefined;
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

  describe('project sprint page', () => {
    beforeEach(() => {
      createProject();
      cy.intercept('GET', '/api/sprints+(?*|)').as('entitiesRequest');
    });

    it('should display the sprint page of a project', () => {
      cy.visit(sprintListUrl());
      cy.wait('@entitiesRequest');
      cy.get('.sprint-page').should('exist');
      cy.url().should('match', sprintUrlPattern(project.key));
    });

    it('should create a new Sprint from the new page', () => {
      cy.visit(`/project/${project.key}/sprint/new`);
      cy.get('[data-cy="SprintCreateUpdateHeading"]').should('exist');
      cy.get('[data-cy="name"]').type('e2e sprint new');
      cy.get('[data-cy="status"]').select('PLANNED');
      cy.get(entityCreateSaveButtonSelector).click();
      cy.url().should('match', sprintUrlPattern(project.key));
    });
  });

  describe('with existing sprint', () => {
    beforeEach(() => {
      createProject().then(() => createSprint());
      cy.intercept('GET', '/api/sprints+(?*|)').as('entitiesRequest');
      cy.intercept('DELETE', '/api/sprints/*').as('deleteEntityRequest');
      cy.visit(sprintListUrl());
      cy.wait('@entitiesRequest');
    });

    it('should display the selected sprint details', () => {
      cy.get('.sprint-page').should('exist');
      cy.get('.sprint-name').should('contain', 'e2e sprint');
    });

    it('should delete the sprint from the board header', () => {
      cy.get('.btn-outline-danger').click();
      cy.get('[data-cy="sprintDeleteDialogHeading"]').should('exist');
      cy.get(entityConfirmDeleteButtonSelector).click();
      cy.wait('@deleteEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(204);
      });
      sprint = undefined;
    });
  });
});
