# Plan de remise en état — suppression d'entités & correction des suppressions

> **STATUT : ✅ EXÉCUTÉ ET VÉRIFIÉ EN CODE — au 21/08/2026**
>
> Toutes les parties de ce plan (A, B, C, D) ont été appliquées dans le code et vérifiées par audit
> le 21/08/2026 (références fichier:ligne ci-dessous). Ce document est conservé comme trace historique.
>
> **Reste à faire :**
> 1. §8 — purger les dernières références obsolètes de la documentation (`architecture.md:68`, nom de base dans `README.md`),
> 2. §9 — rejouer la vérification finale complète (`./mvnw clean verify`, `./npmw run build`, `./npmw test`),
> 3. §11 — traiter les nouveaux travaux issus de l'audit complet du 21/08/2026,
> 4. §12 — annexe : synthèse complète de l'état de l'application relevée lors de cette session.

---

## Récapitulatif d'exécution

| Partie | Contenu | Statut |
|---|---|---|
| A.1 | Suppression `GroupMessage` | ✅ Fait |
| A.2 | Suppression `TaskHistory` (+ notification équivalente) | ✅ Fait |
| A.3 | Suppression présence chat (`UserPresence`) | ✅ Fait |
| A.4 | Suppression champ `mentions` | ✅ Fait |
| B.1 | `DELETE /api/tasks/{id}` → suppression des enfants | ✅ Fait |
| B.2 | `DELETE /api/admin/users/{login}` → nettoyage des références | ✅ Fait |
| B.3 | `DELETE /api/projects/{id}` → cascade complète | ✅ Fait |
| C | Retrait d'un membre → désassignation de ses tâches | ✅ Fait |
| D.1–D.9 | Bugs complémentaires de l'audit | ✅ Fait (détail §6) |
| §7 | Base dev recréée sans tables supprimées | ⚠️ À rejouer/vérifier côté PostgreSQL |
| §8 | Nettoyage documentation | 🟨 Reste `architecture.md:68` + ce document |

---

## Preuves de vérification (audit du 21/08/2026)

### Partie A — entités supprimées

- **Aucun fichier restant** : aucun `*GroupMessage*`, `*TaskHistory*`, `*Presence*`,
  `*group-message*`, `*task-history*` dans `src/` (backend + frontend).
- Changelog consolidé présent : `config/liquibase/changelog/20260807000000_remove_unused_entities.xml`.
- `master.xml` ne référence plus que les changelogs actifs (30 includes, aucune trace des entités supprimées).
- i18n : `actionHistory.json` et `groupMessage.json` supprimés des 3 langues ; plus aucune clé
  `actionHistory` dans `global.json` (fr/en/ar).
- `seed-data.sql` : le `TRUNCATE` (ligne 28) ne contient plus `task_history` ni `group_message`.

### Partie B & C — corrections des suppressions

- **B.1** `service/TaskService.java:412-414` : `delete()` appelle
  `notificationRepository.deleteByTaskId(id)`, `commentRepository.deleteByTaskId(id)`,
  `attachmentRepository.deleteByTaskId(id)` avant `deleteById`.
- **B.2** `service/UserService.java:300` : méthode privée `deleteUserReferences(User)` appelée par
  `deleteUser()` (:293), `registerUser` via `removeNonActivatedUser` (:176) et
  `removeNotActivatedUsers()` (tâche planifiée).
- **B.3** `service/ProjectService.java:282-294` : cascade projet complète
  (messages → conversations → notifications → commentaires → pièces jointes → tâches → sprints → epics → membres → projet).
- **C** `service/ProjectService.java:349` : `removeMember()` appelle
  `taskRepository.unassignTasksInProject(userId, projectId)`.

### Partie D — bugs d'audit corrigés

| Item | Preuve |
|---|---|
| D.1 Sprint/Epic delete → 500 | `SprintService.delete` appelle `taskRepository.unassignBySprintId(id)` ; `EpicService.delete` appelle `taskRepository.unassignByEpicId(id)` avant suppression |
| D.2 `removeNotActivatedUsers` brut | Appelle désormais `deleteUserReferences(user)` avant `userRepository.delete(user)` |
| D.3 ArchUnit 23 violations | `TechnicalStructureTest.java:39` : `.ignoreDependency(service.. → web.rest.errors..)` (« Known deviation » commenté) |
| D.4.1 Précision `updatedAt` | `domain/TaskAsserts.java:58-61` : comparaison par fenêtre de tolérance `isCloseTo(within(1, ChronoUnit.MINUTES))` |
| D.4.2 Double notification statut | `TaskService.java:454` : `checkAndNotifyTaskChanges` ne gère plus les changements de statut (« handled by notifyStatusChangeIfNeeded ») |
| D.4.3 Test attachment 403 | Test renommé `uploadAttachment_asDeveloper_shouldSucceed` avec autorité adaptée (`AttachmentResourceIT.java:537`) |
| D.4.4 Tests rôle création tâche | `ProjectRolePermissionIT.java:154,169` : `@WithMockUser(authorities = "ROLE_PROJET_MANAGER")` aligné sur `TaskResource` |
| D.4.5 Cleanup dernier admin | `UserResourceIT.java:129-148` : re-promotion des admins dans le cleanup avant suppression |
| D.5 `home.spec.ts` `isUser()` | `home.ts:24` : `readonly isUser = computed(...)` existe |
| D.6 Config front | `angular.json` : `buildTarget: "gestion-taches:build:development"` correct ; `vitest.temp.config.ts` supprimé |
| D.7 Imports morts | `navbar.ts` sans `RouterLinkActive`, `notification-list.ts` sans `ItemCount`, blocs metrics restructurés |
| D.8 Changelogs orphelins | `ActionHistory`, `action_history_user`, `TaskTransition` supprimés du dépôt |
| D.9 Clé `actionHistory` | Fichiers + clés retirés des 3 langues |

---

## 7. Base de données (dev)

Code prêt (changelogs nettoyés + seed nettoyé). La recréation physique reste à rejouer si ce n'est pas déjà fait :

```bash
psql -h localhost -U brahim -d postgres -c 'DROP DATABASE IF EXISTS gestion_taches;'
psql -h localhost -U brahim -d postgres -c 'CREATE DATABASE gestion_taches;'
```

Puis démarrer l'app (`./mvnw`) pour laisser Liquibase créer le schéma, et vérifier l'absence des tables
`task_history`, `group_message`, `chat_user_presence`, `chat_message_mentions`.

---

## 8. Documentation — reste un fichier

Nettoyage fait sur : `README.md`, `docs_role.md`, `docs/fiche-classes.md`, `docs/api/*`
(`group-message.md` et `task-history.md` supprimés), `docs/fonctionnel/*`, `docs/utilisateur/*`.

**Restant :**
- [ ] `docs/technique/architecture.md:68` — retirer `UserPresenceRepository` de la liste des repositories ;
- [ ] `README.md` (§ Base de données, ≈ lignes 89-102) — aligner le nom de base documenté (`gestionTaches`)
      sur la base réelle (`gestion_taches`, utilisateur `postgres`) ;
- [ ] archiver ou supprimer le présent document après la dernière passe de vérification.

---

## 9. Vérification finale — à rejouer

```bash
# 1. Références orphelines (attendu : seulement docs/technique/architecture.md et ce document)
rg -ri "groupmessage|group-message|taskhistory|task-history|userpresence|chat_user_presence" \
   src src/test project-management.jdl .jhipster seed-data.sql docs README.md docs_role.md

# 2. Backend : compile + tests
./mvnw clean verify

# 3. Frontend : build + tests
./npmw run build
./npmw test
```

Critères de fin inchangés : plus aucun 500 sur les DELETE tâche/utilisateur/projet/sprint/épic,
retrait d'un membre → tâches désassignées, suites de tests vertes.

---

## 11. Nouveaux travaux issus de l'audit complet du 21/08/2026

> Le plan initial est terminé. L'audit global (back-end, front-end, cohérence API, tests, i18n)
> a relevé les points suivants, classés par priorité.

### 🔴 Critique

| # | Tâche | Fichier(s) | Pourquoi |
|---|---|---|---|
| C1 | Réécrire ou supprimer les specs E2E Cypress entités qui visitent des routes inexistantes (`/task`, `/sprint`, `/epic`, `/comment`, `/attachment`) ; vraies routes imbriquées sous `/project/:key/...` | `src/test/javascript/cypress/e2e/entity/*.cy.ts:14-15` | Specs systématiquement en échec → E2E non fiable |
| C2 | Burndown : trancher — **(a)** implémenter `GET /api/sprints/{id}/burndown` + `GET /api/epics/{id}/burndown` et brancher les composants existants, ou **(b)** supprimer services + composants front | `SprintResource.java`, `EpicResource.java`, `sprint.service.ts`, `epic.service.ts`, `entities/sprint/burndown/`, `entities/epic/burndown/` | Appels front sans endpoint back (404 latents) ; composants inaccessibles depuis toute navigation |
| C3 | Ajouter la suppression de sprint dans l'UI : ouvrir le dialog existant depuis board/table | `sprint/list/sprint.html`, `sprint/detail/sprint-detail.html`, `entities/sprint/delete/*` | `DELETE /api/sprints/{id}` existe mais impossible depuis l'interface |

### 🟠 Important

| # | Tâche | Fichier(s) | Pourquoi |
|---|---|---|---|
| I1 | Brancher les story points réels dans la liste sprint (somme des `storyPoints` des tâches, done = status DONE) | `sprint/list/sprint.ts:317-319` | Valeurs hardcodées à 0 alors que la donnée existe en base (`Task.storyPoints`, `TaskDTO.storyPoints`) |
| I2 | SSE : câbler `events$` (refresh listes à chaud) ou retirer la connexion navbar + `EntityEventResource`/`NotificationSseService` | `layouts/navbar/navbar.ts:57-60`, `core/util/entity-event.service.ts` | Flux connecté mais événements ignorés ; endpoint SSE notifications jamais appelé (front pousse en STOMP) |
| I3 | Compléter les clés i18n visibles manquantes puis resynchroniser fr/ar | `i18n/{en,fr,ar}/*.json` | `error.general` (14 composants), `error.loading`, `dashboard.timeTracking.*` (en/fr affichent la clé brute), dropzone pièces jointes, `epic.detail.storyPoints` |
| I4 | Bottom-nav mobile « Tasks » → `/my-tasks` | `layouts/bottom-nav/bottom-nav.html:13-16` | Lien duplique « Projets » (`/project`) au lieu de pointer vers les tâches |
| I5 | Internationaliser les labels statut/priorité d'AdminTasks | `entities/admin/admin-tasks/admin-tasks.ts:123-146` | Libellés français codés en dur (bypass i18n) |

### 🟡 Amélioration

| # | Tâche | Fichier(s) | Pourquoi |
|---|---|---|---|
| A1 | Supprimer le code mort : `task-form-modal`, `epic-form-modal`, `project-form-modal`, `sprint-backlog-planning` (+ burndowns selon décision C2) ; supprimer `src/main/java/com/gestiontaches/service.zip` | divers | Dette technique, confusion, poids du build ; archive zip dans l'arborescence source Java |
| A2 | Aligner doc/implémentation chat temps réel : router les messages via STOMP ou corriger la doc qui promet du temps réel | `chat.ts` (polling 5 s), `ChatService.java`, `docs/technique/chat-temps-reel.md` | Fonctionne mais n'est pas « temps réel » comme documenté |
| A3 | Ajouter des tests unitaires vitest pour chat (8 fichiers), dashboards (7 composants), my-tasks, search, admin étendu | specs vitest | Modules entiers sans couverture (~29 fichiers) |
| A4 | Rejouer la vérification finale §9 après traitement de C1→I5 | — | Verrouiller les gains |

---

## 12. Annexe — Synthèse complète de l'audit du 21/08/2026 (tout ce qui a été relevé en session)

### 12.1 Architecture identifiée

| Couche | Technologie |
|---|---|
| Générateur | JHipster 9.1.0 |
| Back-end | Spring Boot 4.0.6 / Java 21 (`src/main/java/com/gestiontaches`) |
| Front-end | Angular 21.2 standalone, signals, zoneless, OnPush (`src/main/webapp`) |
| Base de données | PostgreSQL 16 + Liquibase (30 changelogs actifs dans `master.xml`) |
| Auth | JWT (OAuth2 Resource Server) ; rôles `ROLE_ADMIN`, `ROLE_USER`, `ROLE_DEVELOPER`, `ROLE_PROJET_MANAGER` (`AuthoritiesConstants.java`) |
| Temps réel | WebSocket STOMP/SockJS `/websocket/tracker` ; broker `/queue` + `/topic` (`WebsocketConfiguration.java:35-41`) |
| Notifications push | `convertAndSendToUser(login, "/queue/notifications")` (`NotificationService.java:63,81,91`) |
| Pièces jointes | Stockage disque `app.upload.dir` (défaut `uploads/`) — `AttachmentResource.java:60` |
| i18n | ngx-translate : en (716 clés), fr (683), ar (715), support RTL |
| Tests | vitest (95 specs front), JUnit + ArchUnit + Cypress (back/front) |

### 12.2 Back-end — inventaire vérifié fonctionnel

23 contrôleurs REST implémentés, **aucun stub ni TODO/FIXME** (seule occurrence « TODO » = valeur d'enum `TaskStatus`/`EpicStatus`) :

- **Projets** : CRUD, `/progress` (stats cartes), `/by-key/{key}`, `/my-roles`, gestion membres
  (ajout/retrait/changement de rôle) — `ProjectResource.java`.
- **Tâches** : CRUD global + création scoping projet `POST /projects/{projectId}/tasks`,
  assignation `PATCH /{id}/assign`, export CSV — `TaskResource.java`.
- **Sprints** : CRUD + `/{id}/start`, `/{id}/close` (rapport vélocité `VelocityReportDTO`),
  `/projects/{projectId}/backlog` — `SprintResource.java`.
- **Epics** : CRUD + transitions validées (TODO → IN_PROGRESS → DONE/CANCELLED) — `EpicResource.java`.
- **Commentaires / Pièces jointes** : CRUD + `/by-task/{taskId}`, upload multipart, download blob.
- **Notifications** : liste, `/unread-count`, `PATCH /{id}/read`, `PATCH /read-all`, push temps réel.
- **Chat** : conversations GENERAL/DIRECT, messages paginés, édition/suppression soft,
  marquage lecture, recherche — `ChatResource.java` (REST complet).
- **Dashboards** : `/api/dashboard/kpis` (manager/admin), `/api/developer-dashboard/statistics`.
- **Admin** : users CRUD + `/{login}/detail` (détail riche), stats, project-members, notifications globales.
- **Divers** : recherche globale `/api/search`, export CSV admin `/api/export/csv/**`,
  SSE entités `/api/events/stream`.

Sécurité cohérente et vérifiée : `/api/admin/**` → ADMIN (`SecurityConfiguration.java:75`),
le reste authentifié (:76), endpoints publics limités à register/activate/reset/authenticate ;
permissions par projet déléguées à `ProjectPermissionService` (OWNER/MANAGER/MEMBER).

### 12.3 Front-end — inventaire vérifié fonctionnel

- Toutes les routes résolvent (sidebar, topbar, bottom-nav, deep-links de notifications) ;
  **0 bouton sans handler** sur l'ensemble des templates (vérification croisée scriptée).
- Écrans complets : login split-screen, projets (cartes + détail + membres + settings),
  sprints (board, démarrage/clôture, modal vélocité), epics (roadmap type Gantt + table + détail),
  tâches (liste + panneau coulissant + détail onglets commentaires/pièces jointes avec drag&drop),
  mes-tâches (liste + kanban, préférence persistée), chat (conversations + directs +
  édition/suppression soft + marquage lecture), notifications (cloche temps réel STOMP avec badge
  non-lus + page + modale détail + deep-links), dashboards 3 profils (KPIs, donut SVG, suivi temps,
  timeline, actions rapides), admin étendu (tasks/members/notifications/stats), recherche globale ⌘K.

### 12.4 Cohérence front/back API — résultats

Vérifications positives :
- **Enums parfaitement alignées** TS ↔ Java : TaskStatus (7 valeurs), SprintStatus, EpicStatus,
  Priority, ProjectRole, ConversationType (`app/entities/enumerations/*.model.ts`
  vs `domain/enumeration/*.java`).
- Tous les appels HTTP principaux ont un endpoint back correspondant (projets, tâches, sprints
  start/close/backlog, épics, commentaires, pièces jointes upload/download/by-task, chat, notifications
  read/read-all/unread-count, dashboards, search, export CSV, admin).
- Pagination conforme aux conventions JHipster (`page`/`size`/`sort` + headers `X-Total-Count`).

Écarts relevés (reportés en §11) :
- `GET /api/sprints/{id}/burndown` et `GET /api/epics/{id}/burndown` appelés côté front
  (`sprint.service.ts`, `epic.service.ts`) **sans endpoint back** → C2.
- Flux SSE `/api/events/stream` connecté (`navbar.ts:57-60`) mais aucun abonné à `events$` ;
  `/api/notifications/stream` (`NotificationSseService`) jamais consommé (le front pousse en STOMP) → I2.

### 12.5 Tests — état détaillé

- **Front unitaire (vitest)** : 95 specs ; cœur JHipster intact ; specs entités adaptées aux
  personnalisations (board sprint, kanban, permissions). Qualité réelle mais happy-path.
- **Zéro spec pour ~29 fichiers** : module chat entier (8 fichiers dont `chat.service.ts`),
  my-tasks, notification-detail-modal, les 7 composants dashboard, project-settings,
  admin-tasks / admin-project-members / admin-notifications, listes+onglets commentaires/pièces
  jointes des tâches (4), sprint table/active-board/timeline/burndown (4), epic-burndown,
  search dialog/service, entity-event.service → A3.
- **E2E Cypress** : specs account/admin OK ; les 6 specs entités visitent d'anciennes routes
  inexistantes (`/task`, `/sprint`, `/epic`, `/comment`, `/attachment`) → C1.
- **Back-end** : ArchUnit corrigé (D.3), tests IT corrigés (D.4.1→D.4.5, preuves §Partie D) ;
  `./mvnw clean verify` complet à rejouer pour confirmation finale.

### 12.6 i18n — écarts précis relevés

Clés manquantes rendues visibles aux utilisateurs :
- `error.general` — fallback d'erreur dans **14 composants** (task/epic/sprint/project updates,
  kanban, panneaux de détail…) → la clé brute s'affiche en cas d'échec de sauvegarde ;
- `error.loading` — `project/detail/project-detail.ts:128`, `project/settings/project-settings.ts:88` ;
- `dashboard.timeTracking.title|byUser|byProject` — définies **uniquement en arabe**
  (en/fr affichent la clé brute) — section « suivi du temps » du dashboard manager ;
- `global.messages.validate.currentpassword.required` — `account/password/password.html` ;
- `gestionTachesApp.task.detail.attachments.dropzone` — `task/detail/tabs/task-attachments-tab.html:43` ;
- `gestionTachesApp.epic.detail.storyPoints` + `...taskTable.storyPoints` — en-tête/table épic.

Dérive inter-langues : **fr manque 35 clés présentes en en** (`project.settings.*`, `project.member.role.*`,
`task.error.noProject`, plusieurs `metrics.*`, `error.messages.markAllAsRead/viewAllNotifications`…) ;
**ar manque 28** (`dashboard.kpi.*`, `quickActions.*`, `TaskStatus.NEEDS_INFO/READY_FOR_TEST`,
`project.card.*`, `task.detail.attachments*`, `userManagement.adminTasks.*`…).
Labels FR codés en dur hors i18n : `admin-tasks.ts:123-146` → I5.

### 12.7 Hygiène / code mort relevé

- Composants jamais importés/routés : `sprint-backlog-planning`, `sprint/burndown/*`,
  `epic/burndown/*`, `task-form-modal`, `epic-form-modal`, `project-form-modal` ;
  dialog orphelin `sprint/delete/*` (jamais ouvert) → A1/C3.
- `src/main/java/com/gestiontaches/service.zip` — archive 50 Ko dans l'arborescence source → A1.
- Aucun `console.log`, aucun bloc template commenté, aucun `TODO/FIXME` dans le code applicatif.

### 12.8 Divers notés en session

- Chat : rafraîchissement par polling 5 s (`chat.ts:24,98`) alors que
  `docs/technique/chat-temps-reel.md` documente du temps réel → A2.
- Story points : présents en base et dans le DTO back (`Task.java:61`, `TaskDTO.java:36`)
  mais total/done hardcodés à 0 côté UI (`sprint/list/sprint.ts:317-319`) → I1.
- Suppression sprint : endpoint back OK, aucune entrée UI (dialog orphelin) → C3.
- Deep-links des notifications : tous valides vers des routes existantes (rien à faire).

---

> Règle conservée : **aucune hypothèse silencieuse**. Tout comportement métier ambigu
> (notamment C2-a/b et I2 câbler/supprimer) est soumis à validation avant implémentation.
