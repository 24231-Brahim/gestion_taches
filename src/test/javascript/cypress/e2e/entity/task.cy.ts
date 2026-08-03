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

describe('Task e2e test', () => {
  const taskPageUrl = '/task';
  const taskPageUrlPattern = new RegExp('/task(\\?.*)?$');
  let username: string;
  let password: string;
  const taskSample = {
    title: 'multiple vérifier',
    status: 'CANCELLED',
    priority: 'HIGHEST',
    createdAt: '2026-06-24T06:19:11.694Z',
  };

  let task;
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
    cy.intercept('GET', '/api/tasks+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/tasks').as('postEntityRequest');
    cy.intercept('DELETE', '/api/tasks/*').as('deleteEntityRequest');
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
    if (task) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/tasks/${task.id}`,
      }).then(() => {
        task = undefined;
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

  it('Tasks menu should load Tasks page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('task');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Task').should('exist');
    cy.url().should('match', taskPageUrlPattern);
  });

  describe('Task page', () => {
    it('should have translated page title', () => {
      cy.visit(taskPageUrl);
      cy.getEntityHeading('Task').should('not.contain', 'gestionTachesApp.task.home.title');
    });

    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(taskPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Task page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/task/new$'));
        cy.getEntityCreateUpdateHeading('Task');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', taskPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/tasks',
          body: {
            ...taskSample,
            project,
          },
        }).then(({ body }) => {
          task = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/tasks+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/tasks?page=0&size=20>; rel="last",<http://localhost/api/tasks?page=0&size=20>; rel="first"',
              },
              body: [task],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(taskPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Task page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('task');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', taskPageUrlPattern);
      });

      it('edit button click should load edit Task page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Task');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', taskPageUrlPattern);
      });

      it('edit button click should load edit Task page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Task');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', taskPageUrlPattern);
      });

      it('last delete button click should delete instance of Task', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('task').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', taskPageUrlPattern);

        task = undefined;
      });
    });
  });

  describe('new Task page', () => {
    beforeEach(() => {
      cy.visit(taskPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Task');
    });

    it('should create an instance of Task', () => {
      cy.get(`[data-cy="title"]`).type('triste miaou brave');
      cy.get(`[data-cy="title"]`).should('have.value', 'triste miaou brave');

      cy.get(`[data-cy="description"]`).type('équipe');
      cy.get(`[data-cy="description"]`).should('have.value', 'équipe');

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
        task = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', taskPageUrlPattern);
    });
  });
});
