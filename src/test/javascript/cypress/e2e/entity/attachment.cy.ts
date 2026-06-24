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

describe('Attachment e2e test', () => {
  const attachmentPageUrl = '/attachment';
  const attachmentPageUrlPattern = new RegExp('/attachment(\\?.*)?$');
  let username: string;
  let password: string;
  // const attachmentSample = {"fileName":"là affable afin que","filePath":"un peu","uploadedAt":"2026-06-24T03:02:11.236Z"};

  let attachment;
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
      body: {"title":"avant dessous","description":"athlète gens","type":"STORY","status":"IN_PROGRESS","priority":"LOWEST","createdAt":"2026-06-23T19:20:42.930Z","updatedAt":"2026-06-23T23:49:00.287Z"},
    }).then(({ body }) => {
      issue = body;
    });
  });
   */

  beforeEach(() => {
    cy.intercept('GET', '/api/attachments+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/attachments').as('postEntityRequest');
    cy.intercept('DELETE', '/api/attachments/*').as('deleteEntityRequest');
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
    if (attachment) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/attachments/${attachment.id}`,
      }).then(() => {
        attachment = undefined;
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

  it('Attachments menu should load Attachments page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('attachment');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Attachment').should('exist');
    cy.url().should('match', attachmentPageUrlPattern);
  });

  describe('Attachment page', () => {
    it('should have translated page title', () => {
      cy.visit(attachmentPageUrl);
      cy.getEntityHeading('Attachment').should('not.contain', 'gestionTachesApp.attachment.home.title');
    });

    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(attachmentPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Attachment page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/attachment/new$'));
        cy.getEntityCreateUpdateHeading('Attachment');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', attachmentPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      /* Disabled due to incompatibility
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/attachments',
          body: {
            ...attachmentSample,
            issue: issue,
          },
        }).then(({ body }) => {
          attachment = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/attachments+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/attachments?page=0&size=20>; rel="last",<http://localhost/api/attachments?page=0&size=20>; rel="first"',
              },
              body: [attachment],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(attachmentPageUrl);

        cy.wait('@entitiesRequestInternal');
      });
       */

      beforeEach(function () {
        cy.visit(attachmentPageUrl);

        cy.wait('@entitiesRequest').then(({ response }) => {
          if (response?.body.length === 0) {
            this.skip();
          }
        });
      });

      it('detail button click should load details Attachment page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('attachment');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', attachmentPageUrlPattern);
      });

      it('edit button click should load edit Attachment page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Attachment');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', attachmentPageUrlPattern);
      });

      it('edit button click should load edit Attachment page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Attachment');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', attachmentPageUrlPattern);
      });

      // Reason: cannot create a required entity with relationship with required relationships.
      it.skip('last delete button click should delete instance of Attachment', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('attachment').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', attachmentPageUrlPattern);

        attachment = undefined;
      });
    });
  });

  describe('new Attachment page', () => {
    beforeEach(() => {
      cy.visit(attachmentPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Attachment');
    });

    // Reason: cannot create a required entity with relationship with required relationships.
    it.skip('should create an instance of Attachment', () => {
      cy.get(`[data-cy="fileName"]`).type('à cause de aussi désagréable');
      cy.get(`[data-cy="fileName"]`).should('have.value', 'à cause de aussi désagréable');

      cy.get(`[data-cy="filePath"]`).type('équipe de recherche');
      cy.get(`[data-cy="filePath"]`).should('have.value', 'équipe de recherche');

      cy.get(`[data-cy="uploadedAt"]`).type('2026-06-24T03:34');
      cy.get(`[data-cy="uploadedAt"]`).blur();
      cy.get(`[data-cy="uploadedAt"]`).should('have.value', '2026-06-24T03:34');

      cy.get(`[data-cy="issue"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        attachment = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', attachmentPageUrlPattern);
    });
  });
});
