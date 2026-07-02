# TODO

## Sprint / Comment / Attachment / Action-history Refactoring

### Backend
- [x] Ajout champ `author` (User) to Comment entity + DTO + Mapper
- [x] Liquibase changelog for `author_id`
- [x] Repositories: findByIssueId for Comment, Attachment, ActionHistory
- [x] Repository: findByProjectIdAndStatus for Sprint
- [x] Services: findByIssueId for Comment, Attachment, ActionHistory
- [x] SprintService: validateSingleActiveSprint()
- [x] CommentResource: GET /by-issue/{issueId}, auto-set author on POST
- [x] AttachmentResource: POST /upload, GET /download/{id}, GET /by-issue/{issueId}
- [x] ActionHistoryResource: GET /by-issue/{issueId}

### Frontend — Sprint
- [x] SprintActiveBoard component (kanban + drag-drop)
- [x] SprintBacklogPlanning component (backlog ↔ sprint)
- [x] SprintBurndownChart component (pure SVG)
- [x] Sprint detail page with tabs (Board/Planning/Burndown)
- [x] i18n keys for sprint

### Frontend — Issue Drawer
- [x] IssueCommentList component
- [x] IssueAttachmentList component
- [x] IssueActivityFeed component
- [x] Integration into issue-detail-panel
- [x] Remove Comment/Attachment/History from sidebar

### Frontend — Infrastructure
- [x] Update IComment model with author
- [x] Register new FontAwesome icons
- [x] i18n keys for comment.form, attachment.form

### Tests
- [ ] Vitest unit tests for new components
- [ ] JUnit integration tests for new endpoints

### Documentation
- [x] Update fiche-classes.md (Comment author)
- [x] Update prompt status file
