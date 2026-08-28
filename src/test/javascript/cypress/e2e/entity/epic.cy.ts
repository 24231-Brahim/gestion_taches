import {
  entityConfirmDeleteButtonSelector,
  entityCreateButtonSelector,
  entityCreateCancelButtonSelector,
  entityCreateSaveButtonSelector,
  entityDeleteButtonSelector,
  entityDetailsButtonSelector,
  entityDetailsBackButtonSelector,
  entityEditButtonSelector,
} from '../../support/entity';

describe('Epic e2e test', () => {
  let username: string;
  let password: string;

  let project;
  let epic;

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
        body: { name: `E2E Epic ${Date.now()}`, description: 'e2e', key: `E2EEC${Date.now()}` },
      })
      .then(({ body }) => {
        project = body;
      });

  const createEpic = () =>
    cy
      .authenticatedRequest({
        method: 'POST',
        url: '/api/epics',
        body: { title: 'e2e epic', status: 'TODO', priority: 'MEDIUM', project },
      })
      .then(({ body }) => {
        epic = body;
      });

  const epicTableUrl = () => `/project/${project.key}/epic/table`;
  const epicUrlPattern = key => new RegExp(`/project/${key}/epic(.*)$`);

  afterEach(() => {
    if (epic) {
      cy.authenticatedRequest({ method: 'DELETE', url: `/api/epics/${epic.id}` }).then(() => {
        epic = undefined;
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

  describe('project epic table', () => {
    beforeEach(() => {
      createProject();
      cy.intercept('GET', '/api/epics+(?*|)').as('entitiesRequest');
    });

    it('should display the epics of a project', () => {
      cy.visit(epicTableUrl());
      cy.wait('@entitiesRequest');
      cy.get('[data-cy="EpicHeading"]').should('exist');
      cy.url().should('match', epicUrlPattern(project.key));
    });

    it('should load the create Epic page', () => {
      cy.visit(epicTableUrl());
      cy.wait('@entitiesRequest');
      cy.get(entityCreateButtonSelector).click();
      cy.url().should('match', new RegExp(`/project/${project.key}/epic/new$`));
      cy.get('[data-cy="EpicCreateUpdateHeading"]').should('exist');
      cy.get(entityCreateSaveButtonSelector).should('exist');
      cy.get(entityCreateCancelButtonSelector).click();
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', epicUrlPattern(project.key));
    });
  });

  describe('with existing epic', () => {
    beforeEach(() => {
      createProject().then(() => createEpic());
      cy.intercept('GET', '/api/epics+(?*|)').as('entitiesRequest');
      cy.intercept('DELETE', '/api/epics/*').as('deleteEntityRequest');
      cy.visit(epicTableUrl());
      cy.wait('@entitiesRequest');
    });

    it('detail button click should load details Epic page', () => {
      cy.get(entityDetailsButtonSelector).first().click();
      cy.url().should('match', new RegExp(`/project/${project.key}/epic/${epic.id}/view$`));
      cy.get(entityDetailsBackButtonSelector).click();
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', epicUrlPattern(project.key));
    });

    it('edit button click should load edit Epic page and save', () => {
      cy.get(entityEditButtonSelector).first().click();
      cy.url().should('match', new RegExp(`/project/${project.key}/epic/${epic.id}/edit$`));
      cy.get('[data-cy="EpicCreateUpdateHeading"]').should('exist');
      cy.get(entityCreateSaveButtonSelector).click();
      cy.url().should('match', epicUrlPattern(project.key));
    });

    it('last delete button click should delete instance of Epic', () => {
      cy.get(entityDeleteButtonSelector).last().click();
      cy.get('[data-cy="epicDeleteDialogHeading"]').should('exist');
      cy.get(entityConfirmDeleteButtonSelector).click();
      cy.wait('@deleteEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(204);
      });
      cy.url().should('match', epicUrlPattern(project.key));
      epic = undefined;
    });
  });
});
