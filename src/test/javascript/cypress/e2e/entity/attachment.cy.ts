describe('Attachment e2e test', () => {
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
        body: { name: `E2E Attachment ${Date.now()}`, description: 'e2e', key: `E2EAT${Date.now()}` },
      })
      .then(({ body }) => {
        project = body;
      });

  const createTask = () =>
    cy
      .authenticatedRequest({
        method: 'POST',
        url: '/api/tasks',
        body: { title: 'e2e attachment task', status: 'NEW', priority: 'MEDIUM', project },
      })
      .then(({ body }) => {
        task = body;
      });

  const taskDetailUrl = () => `/project/${project.key}/task/${task.id}/view`;
  const openAttachmentsTab = () => cy.get('.tab-item').eq(2).click();

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

  it('should upload a new attachment from the drop zone and display it', () => {
    createProject().then(() => createTask());
    cy.visit(taskDetailUrl());
    openAttachmentsTab();
    cy.get('.attachment-drop-zone input[type="file"]').then($input => {
      const file = new File(['e2e attachment content'], 'e2e.txt', { type: 'text/plain' });
      const dataTransfer = new DataTransfer();
      dataTransfer.items.add(file);
      const input = $input[0] as HTMLInputElement;
      input.files = dataTransfer.files;
      cy.wrap($input).trigger('change', { force: true });
    });
    cy.get('.attachment-item').should('exist');
  });
});
