describe('Comment e2e test', () => {
  let username: string;
  let password: string;

  let project;
  let task;
  let comment;

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
        body: { name: `E2E Comment ${Date.now()}`, description: 'e2e', key: `E2ECO${Date.now()}` },
      })
      .then(({ body }) => {
        project = body;
      });

  const createTask = () =>
    cy
      .authenticatedRequest({
        method: 'POST',
        url: '/api/tasks',
        body: { title: 'e2e comment task', status: 'NEW', priority: 'MEDIUM', project },
      })
      .then(({ body }) => {
        task = body;
      });

  const createComment = () =>
    cy
      .authenticatedRequest({
        method: 'POST',
        url: '/api/comments',
        body: { content: 'e2e comment', task: { id: task.id } },
      })
      .then(({ body }) => {
        comment = body;
      });

  const taskDetailUrl = () => `/project/${project.key}/task/${task.id}/view`;
  const openCommentsTab = () => cy.get('.tab-item').eq(1).click();

  afterEach(() => {
    if (comment) {
      cy.authenticatedRequest({ method: 'DELETE', url: `/api/comments/${comment.id}` }).then(() => {
        comment = undefined;
      });
    }
  });

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

  it('should display comments on the task detail comments tab', () => {
    createProject().then(() => createTask().then(() => createComment()));
    cy.visit(taskDetailUrl());
    openCommentsTab();
    cy.get('.comment-item').should('exist');
    cy.get('.comment-body').should('contain', 'e2e comment');
  });

  it('should add a new comment from the comments tab', () => {
    createProject().then(() => createTask());
    cy.visit(taskDetailUrl());
    openCommentsTab();
    cy.get('.comment-form textarea').type('e2e added comment');
    cy.get('.comment-form .btn-primary').click();
    cy.get('.comment-body').should('contain', 'e2e added comment');
  });
});
