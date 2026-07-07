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

describe('ActionHistory e2e test', () => {
  const actionHistoryPageUrl = '/action-history';
  const actionHistoryPageUrlPattern = new RegExp('/action-history(\\?.*)?$');
  let username: string;
  let password: string;
  // const actionHistorySample = {"action":"derrière","createdAt":"2026-06-23T17:01:37.179Z"};

  let actionHistory;
  // let issue;

  before(() => {
    cy.credentials().then(credentials => {
      ({ username, password } = credentials);
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/issues',
      body: {"title":"au cas où autour vivace","description":"paf dévorer","type":"STORY","status":"CANCELLED","priority":"MEDIUM","createdAt":"2026-06-23T17:10:19.529Z","updatedAt":"2026-06-24T04:15:06.809Z"},
    }).then(({ body }) => {
      issue = body;
    });
  });
   */

  beforeEach(() => {
    cy.intercept('GET', '/api/action-histories+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/action-histories').as('postEntityRequest');
    cy.intercept('DELETE', '/api/action-histories/*').as('deleteEntityRequest');
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/issues', {
      statusCode: 200,
      body: [issue],
    });

  });
   */

  afterEach(() => {
    if (actionHistory) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/action-histories/${actionHistory.id}`,
      }).then(() => {
        actionHistory = undefined;
      });
    }
  });

  /* Disabled due to incompatibility
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
   */

  it('ActionHistories menu should load ActionHistories page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('action-history');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('ActionHistory').should('exist');
    cy.url().should('match', actionHistoryPageUrlPattern);
  });

  describe('ActionHistory page', () => {
    it('should have translated page title', () => {
      cy.visit(actionHistoryPageUrl);
      cy.getEntityHeading('ActionHistory').should('not.contain', 'gestionTachesApp.actionHistory.home.title');
    });

    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(actionHistoryPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create ActionHistory page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/action-history/new$'));
        cy.getEntityCreateUpdateHeading('ActionHistory');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', actionHistoryPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      /* Disabled due to incompatibility
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/action-histories',
          body: {
            ...actionHistorySample,
            issue: issue,
          },
        }).then(({ body }) => {
          actionHistory = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/action-histories+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/action-histories?page=0&size=20>; rel="last",<http://localhost/api/action-histories?page=0&size=20>; rel="first"',
              },
              body: [actionHistory],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(actionHistoryPageUrl);

        cy.wait('@entitiesRequestInternal');
      });
       */

      beforeEach(function () {
        cy.visit(actionHistoryPageUrl);

        cy.wait('@entitiesRequest').then(({ response }) => {
          if (response?.body.length === 0) {
            this.skip();
          }
        });
      });

      it('detail button click should load details ActionHistory page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('actionHistory');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', actionHistoryPageUrlPattern);
      });

      it('edit button click should load edit ActionHistory page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('ActionHistory');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', actionHistoryPageUrlPattern);
      });

      it('edit button click should load edit ActionHistory page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('ActionHistory');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', actionHistoryPageUrlPattern);
      });

      // Reason: cannot create a required entity with relationship with required relationships.
      it.skip('last delete button click should delete instance of ActionHistory', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('actionHistory').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', actionHistoryPageUrlPattern);

        actionHistory = undefined;
      });
    });
  });

  describe('new ActionHistory page', () => {
    beforeEach(() => {
      cy.visit(actionHistoryPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('ActionHistory');
    });

    // Reason: cannot create a required entity with relationship with required relationships.
    it.skip('should create an instance of ActionHistory', () => {
      cy.get(`[data-cy="action"]`).type('puisque');
      cy.get(`[data-cy="action"]`).should('have.value', 'puisque');

      cy.get(`[data-cy="fieldChanged"]`).type('ha ha plic gestionnaire');
      cy.get(`[data-cy="fieldChanged"]`).should('have.value', 'ha ha plic gestionnaire');

      cy.get(`[data-cy="oldValue"]`).type('rose pour que aussitôt');
      cy.get(`[data-cy="oldValue"]`).should('have.value', 'rose pour que aussitôt');

      cy.get(`[data-cy="newValue"]`).type('multiple du fait que équipe');
      cy.get(`[data-cy="newValue"]`).should('have.value', 'multiple du fait que équipe');

      cy.get(`[data-cy="createdAt"]`).type('2026-06-24T07:49');
      cy.get(`[data-cy="createdAt"]`).blur();
      cy.get(`[data-cy="createdAt"]`).should('have.value', '2026-06-24T07:49');

      cy.get(`[data-cy="issue"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        actionHistory = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', actionHistoryPageUrlPattern);
    });
  });
});
