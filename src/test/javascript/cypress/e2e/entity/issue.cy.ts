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

describe('Issue e2e test', () => {
  const issuePageUrl = '/issue';
  const issuePageUrlPattern = new RegExp('/issue(\\?.*)?$');
  let username: string;
  let password: string;
  const issueSample = {
    title: 'multiple vérifier',
    type: 'IMPROVEMENT',
    status: 'CANCELLED',
    priority: 'HIGHEST',
    createdAt: '2026-06-24T06:19:11.694Z',
  };

  let issue;
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
      body: { name: 'partout', description: 'splendide touriste à la faveur de', key: 'presque br', createdAt: '2026-06-24T05:47:22.089Z' },
    }).then(({ body }) => {
      project = body;
    });
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/issues+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/issues').as('postEntityRequest');
    cy.intercept('DELETE', '/api/issues/*').as('deleteEntityRequest');
  });

  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/comments', {
      statusCode: 200,
      body: [],
    });

    cy.intercept('GET', '/api/attachments', {
      statusCode: 200,
      body: [],
    });

    cy.intercept('GET', '/api/action-histories', {
      statusCode: 200,
      body: [],
    });

    cy.intercept('GET', '/api/sprints', {
      statusCode: 200,
      body: [],
    });

    cy.intercept('GET', '/api/epics', {
      statusCode: 200,
      body: [],
    });

    cy.intercept('GET', '/api/projects', {
      statusCode: 200,
      body: [project],
    });
  });

  afterEach(() => {
    if (issue) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/issues/${issue.id}`,
      }).then(() => {
        issue = undefined;
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

  it('Issues menu should load Issues page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('issue');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Issue').should('exist');
    cy.url().should('match', issuePageUrlPattern);
  });

  describe('Issue page', () => {
    it('should have translated page title', () => {
      cy.visit(issuePageUrl);
      cy.getEntityHeading('Issue').should('not.contain', 'gestionTachesApp.issue.home.title');
    });

    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(issuePageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Issue page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/issue/new$'));
        cy.getEntityCreateUpdateHeading('Issue');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', issuePageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/issues',
          body: {
            ...issueSample,
            project,
          },
        }).then(({ body }) => {
          issue = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/issues+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/issues?page=0&size=20>; rel="last",<http://localhost/api/issues?page=0&size=20>; rel="first"',
              },
              body: [issue],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(issuePageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Issue page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('issue');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', issuePageUrlPattern);
      });

      it('edit button click should load edit Issue page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Issue');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', issuePageUrlPattern);
      });

      it('edit button click should load edit Issue page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Issue');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', issuePageUrlPattern);
      });

      it('last delete button click should delete instance of Issue', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('issue').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', issuePageUrlPattern);

        issue = undefined;
      });
    });
  });

  describe('new Issue page', () => {
    beforeEach(() => {
      cy.visit(issuePageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Issue');
    });

    it('should create an instance of Issue', () => {
      cy.get(`[data-cy="title"]`).type('triste miaou brave');
      cy.get(`[data-cy="title"]`).should('have.value', 'triste miaou brave');

      cy.get(`[data-cy="description"]`).type('équipe');
      cy.get(`[data-cy="description"]`).should('have.value', 'équipe');

      cy.get(`[data-cy="type"]`).select('SUBTASK');

      cy.get(`[data-cy="status"]`).select('CANCELLED');

      cy.get(`[data-cy="priority"]`).select('LOW');

      cy.get(`[data-cy="createdAt"]`).type('2026-06-23T23:05');
      cy.get(`[data-cy="createdAt"]`).blur();
      cy.get(`[data-cy="createdAt"]`).should('have.value', '2026-06-23T23:05');

      cy.get(`[data-cy="updatedAt"]`).type('2026-06-23T17:31');
      cy.get(`[data-cy="updatedAt"]`).blur();
      cy.get(`[data-cy="updatedAt"]`).should('have.value', '2026-06-23T17:31');

      cy.get(`[data-cy="project"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        issue = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', issuePageUrlPattern);
    });
  });
});
