# Sprint / Comment / Attachment / Action-history — État d'avancement

## ✅ Terminé (backend)
- Ajout du champ `author` (User) dans `Comment.java`, `CommentDTO`, `CommentMapper`
- Création du changelog Liquibase `20260701000002_added_comment_author.xml`
- `CommentRepository.findByIssueIdOrderByCreatedAtDesc`
- `AttachmentRepository.findByIssueIdOrderByUploadedAtDesc`
- `ActionHistoryRepository.findByIssueIdOrderByCreatedAtDesc`
- `SprintRepository.findByProjectIdAndStatus`
- `CommentService.findByIssueId`, `AttachmentService.findByIssueId`, `ActionHistoryService.findByIssueId`
- `SprintService.validateSingleActiveSprint()`
- `GET /api/comments/by-issue/{issueId}` avec auto‑set author
- `POST /api/attachments/upload`, `GET /api/attachments/download/{id}`, `GET /api/attachments/by-issue/{issueId}`
- `GET /api/action-histories/by-issue/{issueId}`
- Compilation backend OK

## ✅ Terminé (frontend)
- Retrait des liens Comment/Attachment/History dans la sidebar
- `IComment.author` dans `comment.model.ts`
- Composant `SprintActiveBoard` (kanban + drag-drop)
- Composant `SprintBacklogPlanning` (backlog ↔ sprint drag-drop)
- Composant `SprintBurndownChart` (SVG pur, sans librairie)
- Composant `IssueCommentList` (CRUD comments dans le drawer)
- Composant `IssueAttachmentList` (upload/download/suppression)
- Composant `IssueActivityFeed` (historique dans le drawer)
- Page sprint détail refondue avec tabs (Board / Planning / Burndown)
- Issue detail panel intégrant les trois sous-composants
- Clés i18n ajoutées (en/fr) pour sprint.board/planning/burndown/action, comment.form, attachment.form
- Nouvelles icônes FontAwesome enregistrées (faChartLine, faPlay, faUndo, faCloudUploadAlt)
- Compilation frontend OK (zero TS errors)

## 🔜 Restant
- Tests unitaires Vitest pour les nouveaux composants
- Tests d'intégration JUnit pour les nouveaux endpoints
- Mise à jour du TODO.md / NOTE.md