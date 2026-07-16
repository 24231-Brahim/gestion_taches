# TODO — Projet Gestion des Tâches

## ✅ Fait

### Sécurité RBAC

- [x] Ajout des constantes `DEVELOPER` et `PROJET_MANAGER` dans `AuthoritiesConstants.java`
- [x] `@PreAuthorize` sur **ProjectResource** — POST/PUT/PATCH/DELETE : ADMIN + PROJET_MANAGER
- [x] `@PreAuthorize` sur **SprintResource** — POST/PUT/PATCH/DELETE : ADMIN + PROJET_MANAGER
- [x] `@PreAuthorize` sur **EpicResource** — POST/PUT/PATCH/DELETE : ADMIN + PROJET_MANAGER
- [x] `@PreAuthorize` sur **TaskResource** — POST/PUT/PATCH/DELETE : ADMIN + PROJET_MANAGER + DEVELOPER
- [x] `@PreAuthorize` sur **CommentResource** — POST : ADMIN + PROJET_MANAGER + DEVELOPER + USER ; PUT/PATCH/DELETE : ADMIN + PROJET_MANAGER + DEVELOPER
- [x] `@PreAuthorize` sur **AttachmentResource** — POST/PUT/PATCH/DELETE : ADMIN + PROJET_MANAGER + DEVELOPER

### Données de seed (Liquibase)

- [x] Ajout des utilisateurs `manager` (ROLE_PROJET_MANAGER) et `dev` (ROLE_DEVELOPER) dans `user.csv`
- [x] Assignation des rôles dans `user_authority.csv`

### Frontend — Correction du bouton Save (Project)

- [x] Ajout de `AlertService` dans `project-update.ts` pour afficher les erreurs HTTP
- [x] Retrait du validateur `required` sur `createdAt` et `id` dans `project-form.service.ts`
- [x] Remplacement du champ `datetime-local` visible par un champ caché pour `createdAt`

### BDD — Colonne `key` réservée SQL

- [x] Renommage de la colonne `key` → `project_key` dans `Project.java`
- [x] Renommage de la colonne `key` → `project_key` dans le changelog Liquibase

### Propriétaire et équipe (Project)

- [x] Ajout de `@ManyToOne User owner` dans `Project.java` — créateur du projet
- [x] Ajout de `@ManyToMany Set<User> members` dans `Project.java` — puis remplacé par `@OneToMany Set<ProjectMember> projectMembers`
- [x] Création de l'entité `ProjectMember` (id, project, user, role, joinedAt) remplaçant le `@ManyToMany`
- [x] `ProjectService.save()` : affecte automatiquement l'utilisateur courant comme `owner`
- [x] `ProjectService.findAll()` : ADMIN voit tout, les autres voient leurs projets uniquement
- [x] `ProjectService.findOne()` : pareil, filtré par propriétaire
- [x] Endpoints : `GET/POST/DELETE /api/projects/{id}/members` — gestion d'équipe (renvoie `ProjectMemberDTO`)
- [x] Changement du `@WithMockUser` dans `ProjectResourceIT.java` en `ROLE_ADMIN`

### Endpoint PATCH pour modification de rôle

- [x] Ajout de `ProjectService.updateMemberRole()` — modifie le rôle d'un membre avec `checkOwnership`
- [x] Ajout de `PATCH /api/projects/{id}/members/{userId}` dans `ProjectResource.java`

### Frontend — Section Membres dans le détail du projet

- [x] Ajout de `updateMemberRole()` dans `project.service.ts`
- [x] Création de la section "Membres" dans `project-detail.ts` (signals, CRUD complet)
- [x] UI complète dans `project-detail.html` (tableau liste + formulaire ajout + inline edit rôle + suppression)
- [x] Contrôle des boutons par `*jhiHasAnyAuthority(['ROLE_ADMIN', 'ROLE_PROJET_MANAGER'])`
- [x] Clés i18n ajoutées dans `fr.json` et `en.json`

### Nettoyage et optimisation

- [x] Ajout de `@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}))` sur `ProjectMember.java`
- [x] Suppression de la méthode inutilisée `deleteByProjectIdAndUserId` dans `ProjectMemberRepository.java`
- [x] Optimisation de `addMember()` pour éviter un double chargement du projet en BDD

### Bugs corrigés

- [x] **JWT — claim `auth` jamais parsé** : ajout d'un `JwtAuthenticationConverter` custom (lambda) dans `SecurityConfiguration.java` qui lit le claim `auth` et split par espace ; injecté via paramètre de méthode dans `filterChain()`
- [x] **`ProjectService.update()`** : charge maintenant l'entité existante avant d'appliquer les modifications pour ne pas écraser `owner_id` par NULL
- [x] **Ownership checks** : `delete()`, `getMembers()`, `addMember()`, `removeMember()` vérifient désormais que l'utilisateur est bien propriétaire du projet (sauf ADMIN)
- [x] **Frontend — `IProject` model** : ajout des champs `ownerId`, `ownerLogin`, `memberIds`
- [x] **Frontend — Erreur liste projets** : affichage d'un message d'erreur si le chargement échoue

### Design System & Theming

- [x] Création de `design-system.scss` : CSS custom properties (dark/light), reset, typo (Audiowide + JetBrains Mono), accessibilité
- [x] Surcharge Bootstrap dark theme dans `_bootstrap-variables.scss` (hex values, border-radius: 0, border-width: 3px)
- [x] Réécriture de `global.scss` : boutons brutals (offset shadow + hover translate), cards, inputs, alerts, tables, pagination, modals, layout shell, page-heading
- [x] Google Fonts (Audiowide, JetBrains Mono) + `data-theme="dark"` dans `index.html`
- [x] `vendor.scss` réordonné : design-system → bootstrap-variables → bootstrap

### Layout — Restructuration complète

- [x] `Sidebar` component : collapsible 256px ↔ 80px, navigation entities + admin, overlay mobile, toggle chevron
- [x] `BottomNav` component : 4 items (Accueil, Projects, Tasks, Settings/Login), visible <768px
- [x] `Navbar` transformée en topbar : brand + logo + hamburger mobile + dropdowns (admin, account, langue)
- [x] `Main` layout shell : topbar + sidebar + content + footer + bottom-nav
- [x] Communication mobile hamburger ↔ sidebar via `document.body.classList` + MutationObserver
- [x] Fix z-index sidebar/overlay (sidebar au-dessus de l'overlay)
- [x] Fix `overflow-x: hidden` qui coupait le bouton toggle de la sidebar

### Branding

- [x] Suppression des images JHipster (logo-jhipster.png, favicon.ico, family_members svg/png)
- [x] `logo.webp` défini comme logo du projet (topbar + favicon)

### Icônes FontAwesome

- [x] Ajout des icônes manquantes dans `font-awesome-icons.ts` : folder, bookmark, bug, comment, paperclip, history, cog, chevron-left, chevron-right, user-circle
- [x] Ajout des icônes dashboard : rocket, check-circle, exclamation-circle

### Dashboard d'accueil

- [x] Création du composant Dashboard avec grille KPI (6 cartes), graphiques (progress + donut SVG), listes (projets récents + tâches récentes), timeline (activité récente), quick actions
- [x] Repositionnement Quick Actions entre KPIs et graphiques (full width)
- [x] Layout Quick Actions en ligne horizontale (flex row, wrap)
- [x] Correction des 4 cartes KPI vides — ajout des icônes FontAwesome manquantes
- [x] Ajout du style `.item-meta` manquant dans la liste "Recent Tasks"
- [x] Données de seed (Liquibase) : 2 projets, 4 sprints, 4 epics, 10 issues, 5 commentaires, 6 membres, 7 actions
- [x] Bannières loading/error sur le dashboard

### Task — Refactoring complet (Jira-like)

- [x] Backend : ajout de `assignee` (User many-to-one) dans `Task.java`, `TaskDTO.java`, `TaskMapper.java`
- [x] Liquibase : nouveau changelog `20260701000001_added_issue_assignee.xml` (FK `assignee_id` → `jhi_user`)
- [x] Task list refactored : onglets Backlog (table paginée) / Board (kanban), recherche textuelle, tri/filtres existants
- [x] TaskKanbanBoard : drag-and-drop par colonne `TaskStatus`, cartes compactes (priorité, assigné), `@HostListener('document:dragend')`
- [x] TaskDetailPanel : drawer latéral avec statut (select), description éditable, infos (sprint, epic, priority, dates, project), sections (comments, attachments)
- [x] TaskHelper (`task-helper.ts`) : maps couleur/icône/label pour Priority, TaskStatus

### Epic — Roadmap view

- [x] Backend : ajout de `startDate` / `endDate` (LocalDate) dans `Epic.java`, `EpicDTO.java`
- [x] Liquibase : nouveau changelog `20260701000000_added_epic_dates.xml` (colonnes `start_date`, `end_date`)
- [x] EpicRoadmap : timeline horizontale barres par epic, filtre par statut, barre de progression %, responsive verticale mobile
- [x] Route epic par défaut → `EpicRoadmap`

### Frontend — i18n & icons

- [x] Ajout icônes FontAwesome : `faArrowDown`, `faArrowUp`, `faClipboardList`, `faColumns`, `faExclamationTriangle`, `faGripVertical`, `faLayerGroup`, `faThLarge`, `faTimesCircle`, `faUserCheck`
- [x] Clés i18n (en/fr) : `task.home.backlog/board/search`, `task.assignee`, `epic.home.roadmap/details`, `epic.startDate/endDate`, `global.messages.empty/notAssigned/unassigned`

## ✅ Fait (Tâche 1 — Rôles projet fonctionnels)

### Backend — Rôles projet

- [x] Création de l'enum `ProjectRole` (OWNER, MANAGER, MEMBER)
- [x] `ProjectMember.java` / `ProjectMemberDTO.java` : passage de `String` → `ProjectRole` avec `@Enumerated(EnumType.STRING)`
- [x] `ProjectPermissionService.java` : service de vérification des rôles projet par utilisateur connecté (fallback login si JWT userId absent)
- [x] `ProjectService.java` : remplacement de `checkOwnership()` par vérifications de rôle (OWNER: tout ; MANAGER: update + manage members sauf OWNER ; MEMBER: read only)
- [x] `TaskService.java` : tout membre peut créer ; OWNER/MANAGER peuvent tout modifier/supprimer ; MEMBER peut seulement modifier/supprimer ses propres tâches
- [x] `SprintService.java` / `EpicService.java` : OWNER/MANAGER seulement pour CRUD
- [x] `@PreAuthorize` élargi à `ROLE_USER` sur ProjectResource, SprintResource, EpicResource (le contrôle fin est dans les services)
- [x] Liquibase changelog `20260705000000_added_project_role_enum.xml` : validation des rôles existants
- [x] `ProjectMemberRepository.java` : ajout de `countByProjectIdAndRole()`
- [x] `TaskResource.createTaskForProject()` : suppression du check manuel d'ownership (délégué au service)

### Frontend — Rôles projet

- [x] Création de l'enum TypeScript `ProjectRole`
- [x] `IProjectMember.role` typé `ProjectRole | null`
- [x] `project-detail.ts` : signaux `userProjectRole`, `canManageMembers`, `canManageSprints`, `canCreateTasks`
- [x] `project-detail.html` : remplacement des `*jhiHasAnyAuthority` par conditions basées sur le rôle projet
- [x] Sélecteur de rôle en dropdown (OWNER/MANAGER/MEMBER) dans le formulaire d'édition

### Tests

- [x] 21 tests d'intégration dans `ProjectRolePermissionIT.java` couvrant OWNER/MANAGER/MEMBER/outsider pour toutes les opérations CRUD

## ✅ Fait (Tâche 2 — Liens admin dans la sidebar)

- [x] Injection de `ProfileService` dans la sidebar pour `inProduction` et `openAPIEnabled`
- [x] Section "Administration" dans la sidebar (visible seulement pour ROLE_ADMIN)
- [x] Liens : User Management, Metrics, Health, Configuration, Logs, API (conditionnel), H2 (conditionnel, dev only)
- [x] Icônes et traductions i18n existantes réutilisées
- [x] `.sidebar-section-label` style pour l'en-tête de section

## ✅ Fait (Tâche 3 — Renommage Issue → Task)

### Backend

- [x] Création de l'entité `Task.java` (remplace `Issue.java`) avec champs : title, description, status (TaskStatus), priority, createdAt, updatedAt
- [x] Création de l'enum `TaskStatus` (NEW, IN_PROGRESS, READY_FOR_TEST, DONE, NEEDS_INFO) — remplace `IssueStatus`
- [x] Suppression de l'enum `IssueType` (absente du schéma cible)
- [x] Création de `TaskRepository.java`, `TaskService.java`, `TaskQueryService.java`, `TaskCriteria.java`
- [x] Création de `TaskDTO.java`, `TaskMapper.java`, `TaskResource.java` (`/api/tasks`)
- [x] Modification de `Comment.java` : relation `issue` → `task`
- [x] Modification de `Attachment.java` : relation `issue` → `task`
- [x] Modification de `Project.java` : collection `issueses` → `tasks`
- [x] Modification de `Notification.java` : conversion des champs bruts `issueId`/`userId` en relations JPA `@ManyToOne` vers Task et User
- [x] Modification de `NotificationDTO.java` : mappings JPA + setters backward-compatible
- [x] Modification de `NotificationRepository.java` : requêtes JPA avec `user.id`
- [x] Modification de `NotificationMapper.java` : mappings pour Task et User
- [x] Modification de `CommentMapper.java` : `issueId` → `taskId`
- [x] Modification de `AttachmentMapper.java` : `issueId` → `taskId`
- [x] Modification de `CommentRepository.java` : `findByIssueIdOrderByCreatedAtDesc` → `findByTaskIdOrderByCreatedAtDesc`
- [x] Modification de `AttachmentRepository.java` : `findByIssueIdOrderByUploadedAtDesc` → `findByTaskIdOrderByUploadedAtDesc`
- [x] Modification de `CommentService.java` : `findByIssueId` → `findByTaskId`
- [x] Modification de `AttachmentService.java` : `findByIssueId` → `findByTaskId`
- [x] Modification de `CacheConfiguration.java` : `Issue` → `Task`
- [x] Modification de `CommentResource.java` : endpoint `/by-issue/{issueId}` → `/by-task/{taskId}`
- [x] Modification de `AttachmentResource.java` : upload `issueId` → `taskId`, endpoint `/by-issue` → `/by-task`
- [x] Modification de `CsvExportResource.java` : `Issue` → `Task`
- [x] Modification de `Sprint.java`, `Epic.java`, `ProjectMember.java` : `@JsonIgnoreProperties` `issueses` → `tasks`

### Suppression

- [x] Suppression de `ActionHistory.java` et de tous ses fichiers associés (repository, service, DTO, mapper, resource, tests)
- [x] Suppression de tous les anciens fichiers `Issue*.java` (domain, repository, service, DTO, mapper, resource, criteria)
- [x] Suppression de `.jhipster/Issue.json` et `.jhipster/ActionHistory.json`

### Liquibase

- [x] Création de `20260713000000_rename_issue_to_task.xml` : rename table `issue` → `task`, rename colonnes `issue_id` → `task_id`, drop table `action_history`, drop column `type`

### Tests

- [x] Création de `TaskTest.java`, `TaskTestSamples.java`, `TaskAsserts.java`
- [x] Création de `TaskResourceIT.java` (tests d'intégration CRUD)
- [x] Création de `TaskCriteriaTest.java`, `TaskMapperTest.java`, `TaskDTOTest.java`
- [x] Modification de `CommentTest.java`, `AttachmentTest.java`, `ProjectTest.java` : `Issue` → `Task`
- [x] Modification de `CommentAsserts.java`, `AttachmentAsserts.java` : `getIssue()` → `getTask()`
- [x] Modification de `CommentResourceIT.java`, `AttachmentResourceIT.java` : `IssueResourceIT` → `TaskResourceIT`
- [x] Modification de `ProjectRolePermissionIT.java` : `IssueDTO` → `TaskDTO`, `IssueStatus` → `TaskStatus`

## 📝 À faire (prochaines étapes)

- [ ] Persister l'état collapsed de la sidebar (localStorage)
- [ ] Corriger les erreurs TS dans `sprint.spec.ts` pour permettre `ng test`
- [ ] Ajouter une page de liste des notifications complète (avec route dédiée)
- [ ] Tester le flux assignation + notification avec l'UI

### Règles métier — Ownership Comment

- [x] `CommentResource.checkCommentOwnership()` : seul l'auteur du commentaire (ou ADMIN/PROJET_MANAGER) peut PUT/PATCH/DELETE
- [x] Lève `BadRequestAlertException("Access denied")` si non autorisé

### Assignation des tâches

- [x] `PATCH /api/tasks/{id}/assign` dans `TaskResource` — validation du rôle DEVELOPER ou PROJET_MANAGER
- [x] `TaskService.assign()` — set l'assignee JPA et save + crée une notification
- [x] `GET /api/users/assignable` dans `PublicUserResource` — liste des DEVELOPER + PROJET_MANAGER
- [x] `UserRepository.findAllByAuthorityNames()` + `UserService.getAssignableUsers()`
- [x] Frontend `TaskService.assign()` + `getAssignableUsers()` dans `task.service.ts`
- [x] Frontend `TaskDetailPanel` — selecteur d'assignee (dropdown avec lazy load via `effect`)
- [x] `PATCH /api/tasks/{id}/assign` crée une notification avec message "[username] vous a assigné à la tâche #{id} : {title}"

### Notifications

- [x] Entité `Notification.java` — id, user (ManyToOne), task (ManyToOne), taskTitle, isRead, createdAt
- [x] DTO `NotificationDTO.java`, Mapper `NotificationMapper.java`
- [x] Repository `NotificationRepository.java` — findByUser_id, countByUser_idAndIsReadFalse
- [x] Service `NotificationService.java` — save, partialUpdate, findByUserId, countUnread
- [x] Resource `NotificationResource.java` — GET /api/notifications, GET /api/notifications/unread-count, PATCH /{id}/read
- [x] Liquibase changelog `20260702000000_added_entity_Notification.xml` inclus dans `master.xml`
- [x] Frontend `NotificationService` — polling toutes les 30s, signal `unreadCount` + `notifications`
- [x] Navbar — icône `faBell` avec badge du nombre non lu, dropdown liste des notifications
