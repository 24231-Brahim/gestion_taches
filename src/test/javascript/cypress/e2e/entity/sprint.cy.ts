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

describe('Sprint e2e test', () => {
  const sprintPageUrl = '/sprint';
  const sprintPageUrlPattern = new RegExp('/sprint(\\?.*)?$');
  let username: string;
  let password: string;
  const sprintSample = { name: 'rectorat biathlète candide', status: 'CANCELLED' };

  let sprint;
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
      body: { name: 'cot cot', description: 'athlète commissionnaire', key: 'psitt tand', createdAt: '2026-06-23T14:35:00.728Z' },
    }).then(({ body }) => {
      project = body;
    });
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/sprints+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/sprints').as('postEntityRequest');
    cy.intercept('DELETE', '/api/sprints/*').as('deleteEntityRequest');
  });

  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/projects', {
      statusCode: 200,
      body: [project],
    });
  });

  afterEach(() => {
    if (sprint) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/sprints/${sprint.id}`,
      }).then(() => {
        sprint = undefined;
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

  it('Sprints menu should load Sprints page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('sprint');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Sprint').should('exist');
    cy.url().should('match', sprintPageUrlPattern);
  });

  describe('Sprint page', () => {
    it('should have translated page title', () => {
      cy.visit(sprintPageUrl);
      cy.getEntityHeading('Sprint').should('not.contain', 'gestionTachesApp.sprint.home.title');
    });

    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(sprintPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Sprint page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/sprint/new$'));
        cy.getEntityCreateUpdateHeading('Sprint');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', sprintPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/sprints',
          body: {
            ...sprintSample,
            project,
          },
        }).then(({ body }) => {
          sprint = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/sprints+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/sprints?page=0&size=20>; rel="last",<http://localhost/api/sprints?page=0&size=20>; rel="first"',
              },
              body: [sprint],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(sprintPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Sprint page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('sprint');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', sprintPageUrlPattern);
      });

      it('edit button click should load edit Sprint page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Sprint');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', sprintPageUrlPattern);
      });

      it('edit button click should load edit Sprint page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Sprint');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', sprintPageUrlPattern);
      });

      it('last delete button click should delete instance of Sprint', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('sprint').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', sprintPageUrlPattern);

        sprint = undefined;
      });
    });
  });

  describe('new Sprint page', () => {
    beforeEach(() => {
      cy.visit(sprintPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Sprint');
    });

    it('should create an instance of Sprint', () => {
      cy.get(`[data-cy="name"]`).type('gens');
      cy.get(`[data-cy="name"]`).should('have.value', 'gens');

      cy.get(`[data-cy="goal"]`).type('hors de à travers');
      cy.get(`[data-cy="goal"]`).should('have.value', 'hors de à travers');

      cy.get(`[data-cy="startDate"]`).type('2026-06-23');
      cy.get(`[data-cy="startDate"]`).blur();
      cy.get(`[data-cy="startDate"]`).should('have.value', '2026-06-23');

      cy.get(`[data-cy="endDate"]`).type('2026-06-23');
      cy.get(`[data-cy="endDate"]`).blur();
      cy.get(`[data-cy="endDate"]`).should('have.value', '2026-06-23');

      cy.get(`[data-cy="status"]`).select('CANCELLED');

      cy.get(`[data-cy="project"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        sprint = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', sprintPageUrlPattern);
    });
  });
});
