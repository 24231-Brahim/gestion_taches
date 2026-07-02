# Notes de développement

## 2026-07-01 — Refactoring Sprint / Comment / Attachment / Action-history

### Résumé
Refonte complète de la page Sprint (board kanban + planification + burndown) et intégration des commentaires/pièces-jointes/historique dans le drawer de détail d'issue.

### Changements Backend

**Comment — nouveau champ `author`**
- `Comment.java` : ajout `@ManyToOne(fetch = LAZY) private User author`
- `CommentDTO.java` : ajout `private UserDTO author`
- `CommentMapper.java` : mappe `author → author.id` et `author.login`
- Nouveau changelog Liquibase : `20260701000002_added_comment_author.xml`
- `CommentResource.java` :
  - `POST /api/comments` : auto-set `author` via `SecurityUtils.getCurrentUserLogin()` + `UserService.getUserWithAuthoritiesByLogin()`
  - `GET /api/comments/by-issue/{issueId}` : nouveau endpoint dédié

**Attachment — upload fichier réel**
- `AttachmentResource.java` :
  - `POST /api/attachments/upload?issueId=` : multipart file + stockage disque
  - `GET /api/attachments/download/{id}` : retourne le fichier
  - `GET /api/attachments/by-issue/{issueId}` : liste filtrée
- Stockage : `${app.upload.dir:uploads}` (configurable)

**ActionHistory**
- `GET /api/action-histories/by-issue/{issueId}` : nouveau endpoint

**Sprint — validation**
- `SprintService.validateSingleActiveSprint()` : rejette si tentative d'ACTIVER un sprint alors qu'un autre sprint du même projet est déjà ACTIF

### Changements Frontend

**Sidebar**
- Retrait des liens Comment / Attachment / Action-history (plus de routes dédiées)

**IComment model**
- Nouveau champ `author?: { id: number; login: string } | null`

**Nouveaux composants Sprint (standalone, OnPush)**
- `sprint-active-board` : kanban 6 colonnes (BACKLOG→CANCELLED), drag-drop natif, boutons start/complete/reopen
- `sprint-backlog-planning` : deux colonnes (backlog ↔ sprint), drag-drop
- `sprint-burndown-chart` : SVG pur + 4 stat cards (total/done/remaining/velocity)

**Nouveaux composants Issue (standalone, OnPush)**
- `issue-comment-list` : CRUD commentaires, RBAC (author peut supprimer le sien, admins/managers/devs tous)
- `issue-attachment-list` : upload par drag-drop + clic, download, suppression
- `issue-activity-feed` : historique chronologique

**Page Sprint détail refondue**
- Layout : header (nom, goal, dates, statut, actions) + tabs (Board/Planning/Burndown)
- Board : `SprintActiveBoard` avec issues filtrées par sprint
- Planning : `SprintBacklogPlanning` avec toutes les issues du projet
- Burndown : `SprintBurndownChart` avec comptage

**Issue drawer**
- Remplacement des placeholders par les 3 sous-composants

### Icons FontAwesome ajoutées
- `faChartLine` (burndown tab)
- `faPlay` (start sprint)
- `faUndo` (reopen sprint)
- `faCloudUploadAlt` (attachment drop zone)
