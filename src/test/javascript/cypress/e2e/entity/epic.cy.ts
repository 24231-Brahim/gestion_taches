import {
  entityConfirmDeleteButtonSelector,
  entityCreateButtonSelector,
  entityCreateCancelButtonSelector,
  entityCreateSaveButtonSelector,
  entityDeleteButtonSelector,
  entityDetailsBackButtonSelector,
  entityDetailsButtonSelector,
  entityEditButtonSelector,
  entityTableSelector,
} from '../../support/entity';

describe('Epic e2e test', () => {
  const epicPageUrl = '/epic';
  const epicPageUrlPattern = new RegExp('/epic(\\?.*)?$');
  let username: string;
  let password: string;
  const epicSample = {
    title: 'avant que de peur de chef de cuisine',
    status: 'CANCELLED',
    priority: 'LOWEST',
    createdAt: '2026-06-23T22:20:41.395Z',
  };

  let epic;
  let project;

  before(() => {
    cy.credentials().then(credentials => {
      ({ username, password } = credentials);
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/projects',
      body: { name: 'antagoniste', description: 'du côté de après', key: 'au point q', createdAt: '2026-06-24T00:56:52.392Z' },
    }).then(({ body }) => {
      project = body;
    });
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/epics+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/epics').as('postEntityRequest');
    cy.intercept('DELETE', '/api/epics/*').as('deleteEntityRequest');
  });

  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/projects', {
      statusCode: 200,
      body: [project],
    });
  });

  afterEach(() => {
    if (epic) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/epics/${epic.id}`,
      }).then(() => {
        epic = undefined;
      });
    }
  });

  afterEach(() => {
    if (project) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/projects/${project.id}`,
      }).then(() => {
        project = undefined;
      });
    }
  });

  it('Epics menu should load Epics page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('epic');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Epic').should('exist');
    cy.url().should('match', epicPageUrlPattern);
  });

  describe('Epic page', () => {
    it('should have translated page title', () => {
      cy.visit(epicPageUrl);
      cy.getEntityHeading('Epic').should('not.contain', 'gestionTachesApp.epic.home.title');
    });

    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(epicPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Epic page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/epic/new$'));
        cy.getEntityCreateUpdateHeading('Epic');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', epicPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/epics',
          body: {
            ...epicSample,
            project,
          },
        }).then(({ body }) => {
          epic = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/epics+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/epics?page=0&size=20>; rel="last",<http://localhost/api/epics?page=0&size=20>; rel="first"',
              },
              body: [epic],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(epicPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Epic page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('epic');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', epicPageUrlPattern);
      });

      it('edit button click should load edit Epic page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Epic');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', epicPageUrlPattern);
      });

      it('edit button click should load edit Epic page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Epic');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', epicPageUrlPattern);
      });

      it('last delete button click should delete instance of Epic', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('epic').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', epicPageUrlPattern);

        epic = undefined;
      });
    });
  });

  describe('new Epic page', () => {
    beforeEach(() => {
      cy.visit(epicPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Epic');
    });

    it('should create an instance of Epic', () => {
      cy.get(`[data-cy="title"]`).type('après que sur');
      cy.get(`[data-cy="title"]`).should('have.value', 'après que sur');

      cy.get(`[data-cy="description"]`).type('en dehors de plic tant que');
      cy.get(`[data-cy="description"]`).should('have.value', 'en dehors de plic tant que');

      cy.get(`[data-cy="status"]`).select('IN_PROGRESS');

      cy.get(`[data-cy="priority"]`).select('HIGHEST');

      cy.get(`[data-cy="createdAt"]`).type('2026-06-24T04:20');
      cy.get(`[data-cy="createdAt"]`).blur();
      cy.get(`[data-cy="createdAt"]`).should('have.value', '2026-06-24T04:20');

      cy.get(`[data-cy="updatedAt"]`).type('2026-06-23T13:24');
      cy.get(`[data-cy="updatedAt"]`).blur();
      cy.get(`[data-cy="updatedAt"]`).should('have.value', '2026-06-23T13:24');

      cy.get(`[data-cy="project"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        epic = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', epicPageUrlPattern);
    });
  });
});
