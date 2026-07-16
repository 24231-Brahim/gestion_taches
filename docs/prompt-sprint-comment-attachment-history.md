# Sprint / Comment / Attachment / TaskHistory — État d'avancement

## ✅ Terminé (backend)
- Ajout du champ `author` (User) dans `Comment.java`, `CommentDTO`, `CommentMapper`
- Création du changelog Liquibase `20260701000002_added_comment_author.xml`
- `CommentRepository.findByTaskIdOrderByCreatedAtDesc`
- `AttachmentRepository.findByTaskIdOrderByUploadedAtDesc`
- `TaskHistoryRepository.findByTaskIdOrderByCreatedAtDesc`
- `SprintRepository.findByProjectIdAndStatus`
- `CommentService.findByTaskId`, `AttachmentService.findByTaskId`, `TaskHistoryService.findByTaskId`
- `SprintService.validateSingleActiveSprint()`
- `GET /api/comments/by-task/{taskId}` avec auto‑set author
- `POST /api/attachments/upload`, `GET /api/attachments/download/{id}`, `GET /api/attachments/by-task/{taskId}`
- `GET /api/task-histories/by-task/{taskId}`
- Compilation backend OK

## ✅ Terminé (frontend)
- Retrait des liens Comment/Attachment/TaskHistory dans la sidebar
- `IComment.author` dans `comment.model.ts`
- Composant `SprintActiveBoard` (kanban + drag-drop)
- Composant `SprintBacklogPlanning` (backlog ↔ sprint drag-drop)
- Composant `SprintBurndownChart` (SVG pur, sans librairie)
- Composant `TaskCommentList` (CRUD comments dans le drawer)
- Composant `TaskAttachmentList` (upload/download/suppression)
- Composant `TaskActivityFeed` (historique dans le drawer)
- Page sprint détail refondue avec tabs (Board / Planning / Burndown)
- Task detail panel intégrant les trois sous-composants
- Clés i18n ajoutées (en/fr) pour sprint.board/planning/burndown/action, comment.form, attachment.form
- Nouvelles icônes FontAwesome enregistrées (faChartLine, faPlay, faUndo, faCloudUploadAlt)
- Compilation frontend OK (zero TS errors)

## 🔜 Restant
- Tests unitaires Vitest pour les nouveaux composants
- Tests d'intégration JUnit pour les nouveaux endpoints
- Mise à jour du TODO.md / NOTE.md
