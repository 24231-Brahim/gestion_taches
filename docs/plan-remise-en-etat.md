# Plan de remise en état — suppression d'entités & correction des suppressions

> **STATUT : EN ATTENTE DE VALIDATION**
> Ce document décrit TOUT ce qui reste à faire pour que l'application fonctionne :
> 1. la suppression de 4 entités + le champ `mentions` (demandée dans le prompt ci-dessous),
> 2. la correction des erreurs `500` lors des suppressions (tâche / utilisateur / projet),
> 3. l'**Opération A** : retrait d'un membre d'un projet → ses tâches sont automatiquement désassignées,
> 4. la remise en état de la base de données (dev, vide — recréation),
> 5. le nettoyage de la documentation.
>
> **Aucun changement de code n'est effectué tant que l'utilisateur n'a pas validé ce plan.**

---

## 1. Contexte & prompt utilisateur (référence)

> Projet JHipster 9.1.0 (Spring Boot 4.0.6 / Java 21 + Angular 21.2.14 + PostgreSQL 16).
> Objectif : simplifier le modèle en supprimant complètement 4 entités redondantes ou non essentielles.
> La base est en environnement **dev, vide** — pas besoin de migration `dropTable` avec préservation de données ;
> on retire directement les changelogs concernés et on recrée la base à zéro.

**Entités à supprimer complètement :**
1. `GroupMessage` — prototype de messagerie remplacé par le chat (`Conversation`/`ChatMessage`).
2. `TaskHistory` — table d'audit des modifications de tâches.
3. `ChatUserPresence` / `UserPresence` — présence « en ligne » du chat.
4. Le champ `mentions` de `ChatMessage` + sa table de jointure `chat_message_mentions`.

**Ne PAS toucher** : `Project`, `Sprint`, `Epic`, `Task`, `Comment`, `Attachment`, `Notification`,
`ProjectMember`, `Conversation`, `ConversationMember`, `ChatMessage` (hors champ `mentions`).

**Décisions validées par l'utilisateur :**
- ✅ Notification admins à la fermeture de sprint : **garder une notification équivalente** (sans `TaskHistory`).
- ✅ Dashboard « Activité récente » du développeur : **basculer sur les tâches récentes** déjà chargées.
- ✅ Nettoyer **aussi la documentation** (`README.md`, `docs/*.md`, `docs_role.md`).

---

## 2. Problèmes détectés (diagnostic)

### 2.1 Erreurs 500 lors des suppressions — violation de clés étrangères (FK sans `ON DELETE CASCADE`)

| Action | Cause | Conséquence |
|---|---|---|
| `DELETE /api/tasks/{id}` | `TaskService.delete` ne supprime pas les enfants. Ex. tâche **2985** : 1 `comment`, 1 `attachment`, 1 `notification` référencent `task_id` | `DataIntegrityViolationException` → **500** |
| `DELETE /api/admin/users/{login}` | `UserService.deleteUser` fait `userRepository.delete(user)` sans nettoyer : `project_member`, `task.assignee/created_by`, `comment.author`, `attachment.uploaded_by`, `notification.user/related_user`, `chat_message.sender`, `conversation_member`, `conversation.created_by` | **500** |
| `DELETE /api/projects/{id}` | `ProjectService.delete` nettoie presque tout mais **oublie `group_message`** (pas de `GroupMessageRepository.deleteByProjectId`) | **500** si le projet a des messages |

> Les messages répétés `Failed to load resource ... /api/tasks/2985 ... 500` dans la console =
> des `DELETE` qui échouent (chaque clic retente). Ce n'est **pas** un problème de permissions.

> ⚠️ **Nuance `group_message`** : son changelog `20260723000000_added_entity_GroupMessage.xml` est **absent
> de `master.xml`** → sur une base fraîche la table n'existe pas. Le 500 « projet » ci-dessus ne concerne que
> l'actuelle base dev (table créée avant le retrait du changelog de `master.xml`).

### 2.2 Retrait d'un membre du projet (Opération A)

`ProjectService.removeMember` supprime seulement la ligne `project_member`. Les tâches du projet
restent assignées à un membre qui n'est plus dans le projet → état incohérent.

**Opération A (choisie) :** à la suppression d'un membre, **désassigner ses tâches** du projet
(`assignee = null`). On ne supprime pas les tâches (elles appartiennent au projet).

### 2.3 Tables orphelines en base dev

Les changelogs de `task_history`, `group_message` (orphelin — absent de `master.xml`), `chat_user_presence`
et `chat_message_mentions` sont retirés. La base dev est recréée à zéro (voir §6).

---

## 3. Partie A — Suppression des 4 entités + champ `mentions`

### A.1 GroupMessage (suppression totale)

**Backend — fichiers à SUPPRIMER :**
- `src/main/java/com/gestiontaches/domain/GroupMessage.java`
- `src/main/java/com/gestiontaches/repository/GroupMessageRepository.java`
- `src/main/java/com/gestiontaches/service/dto/GroupMessageDTO.java`
- `src/main/java/com/gestiontaches/service/GroupMessageService.java`
- `src/main/java/com/gestiontaches/service/mapper/GroupMessageMapper.java`
- `src/main/java/com/gestiontaches/web/rest/GroupMessageResource.java`

**Frontend — fichiers à SUPPRIMER :**
- `src/main/webapp/app/entities/group-message/` (dossier entier : `group-message.model.ts`,
  `service/group-message.service.ts`, `list/group-message-list.ts`, `list/group-message-list.html`)

**Frontend — fichiers à MODIFIER :**
- `src/main/webapp/app/entities/project/detail/project-detail.ts`
  - retirer `import { GroupMessageListComponent } from 'app/entities/group-message/list/group-message-list';` (≈ ligne 24)
  - retirer `GroupMessageListComponent,` de l'array `imports` (≈ ligne 76)
  - retirer le code mort lié à l'onglet `'discussion'` (`activeTab`, `setTab`, CSS de tab-bar) s'il n'est pas utilisé dans le template (le template actuel ne le rend pas).

**i18n — fichiers à SUPPRIMER :**
- `src/main/webapp/i18n/fr/groupMessage.json`
- `src/main/webapp/i18n/en/groupMessage.json`
- `src/main/webapp/i18n/ar/groupMessage.json`

**Config / DB / seed :**
- Supprimer `.jhipster/GroupMessage.json`
- Supprimer `src/main/resources/config/liquibase/changelog/20260723000000_added_entity_GroupMessage.xml`
  (déjà orphelin : absent de `master.xml`)
- `seed-data.sql` : retirer `group_message` de la liste `TRUNCATE` (ligne 30)

> Note : `group_message` n'est **pas** dans `project-management.jdl` → rien à faire côté JDL.

---

### A.2 TaskHistory (suppression totale)

**Backend — fichiers à SUPPRIMER :**
- `src/main/java/com/gestiontaches/domain/TaskHistory.java`
- `src/main/java/com/gestiontaches/repository/TaskHistoryRepository.java`
- `src/main/java/com/gestiontaches/service/dto/TaskHistoryDTO.java`
- `src/main/java/com/gestiontaches/service/mapper/TaskHistoryMapper.java`
- `src/main/java/com/gestiontaches/service/TaskHistoryService.java`
- `src/main/java/com/gestiontaches/web/rest/TaskHistoryResource.java`

**Backend — fichiers à MODIFIER :**
- `service/SprintService.java`
  - retirer `TaskHistoryRepository` (champ + paramètre constructeur + affectation)
  - dans `closeSprint` : supprimer le bloc de création `TaskHistory` (« TASK_MOVED_TO_BACKLOG ») (≈ lignes 241-249)
  - remplacer `notificationService.notifyAdminsOfTaskHistory(history)` par la **notification équivalente** (§3.2.1)
- `service/NotificationService.java`
  - retirer `notifyAdminsOfTaskHistory(TaskHistory)` (lignes 149-165) + import
  - **AJOUTER** `notifyAdminsOfTaskMovedToBacklog(Task task, String oldSprintName)` :
    notifie les admins « Task "X" — TASK_MOVED_TO_BACKLOG (Sprint: Y → Backlog) » en utilisant directement `task`
- `service/ProjectService.java`
  - retirer `TaskHistoryRepository` (champ + paramètre + affectation) + ligne `taskHistoryRepository.deleteByTaskProjectId(id)` (≈ ligne 294)
- `web/rest/UserResource.java`
  - retirer `TaskHistoryRepository` (champ + paramètre + affectation) et les lignes 235-236 de `getUserDetail` (requête `history`)
- `service/dto/UserAdminDetailDTO.java`
  - retirer le champ `recentActivity` (ligne 29) + la classe imbriquée `TaskHistoryEntryDTO` (lignes 305-370)
- `service/UserAdminDetailMapper.java`
  - retirer le paramètre `List<TaskHistory> history`, `toHistoryEntry(...)` et les imports
- `service/TaskService.java` : **vérifier** qu'il n'y a aucune référence (aucune actuellement).

**Frontend — fichiers à SUPPRIMER :**
- `src/main/webapp/app/entities/task-history/` (dossier entier)
- `src/main/webapp/app/entities/task/activity/task-activity-feed.ts` + `.html`
- `src/main/webapp/app/entities/task/detail/tabs/task-history-tab.ts` + `.html`

**Frontend — fichiers à MODIFIER :**
- `src/main/webapp/app/entities/task/detail/task-detail.ts` : retirer `import { TaskHistoryTab } ...` + entrée `TaskHistoryTab,` dans `imports`
- `src/main/webapp/app/entities/task/detail/task-detail.html` : retirer le bouton d'onglet « History » (lignes 26-29) et le `@case ('history')` (lignes 118-120)
- `src/main/webapp/app/home/dashboard/developer-dashboard.component.ts`
  - retirer `recentActivityResource`, `recentActivity`, `ACTIVITY_COLORS`, `formatDate` (si plus utilisé)
  - retirer `recentActivityResource` de `loading()` et `error()`
  - remplacer `<jhi-dashboard-timeline [activitiesOverride]="recentActivity()" />` par `<jhi-dashboard-timeline [tasks]="recentTasks()" />`
- `src/main/webapp/app/entities/admin/user-management/service/user-management.service.ts`
  - retirer `recentActivity?: ITaskHistoryEntry[];` (ligne 24) et l'interface `ITaskHistoryEntry` (lignes 50-58)
- `src/main/webapp/app/entities/admin/user-management/user-admin-detail/user-admin-detail.html`
  - retirer la section « Activité récente » (lignes 169-211)
- `user-admin-detail.ts` : **GARDER** `findDetail` et le composant tels quels — ils servent encore aux
  sections Tâches/Projets/informations ; ne retirer que la section HTML ci-dessus.
- `src/main/webapp/app/home/dashboard/timeline.component.ts`
  - retirer l'input `activitiesOverride` (lignes 106-109) + son traitement dans `activities()`
    et le commentaire ligne 110 (« e.g. TaskHistory entries ») : deviennent **morts** une fois
    que `developer-dashboard` passe à `[tasks]`.

**i18n :**
- SUPPRIMER `src/main/webapp/i18n/en/actionHistory.json` et `src/main/webapp/i18n/fr/actionHistory.json`
- `global.json` (fr/en) : retirer la clé `"actionHistory"` (ligne 21) si elle n'est plus utilisée
  (la clé n'existe **pas** dans `ar/global.json` → rien à faire en ar)
- `task.json` (en/fr/ar) : retirer les clés devenues mortes : `task.detail.history`, `task.detail.noHistory`,
  `task.detail.tab.history`, `task.history`, `task.system`
- `user-management.json` (fr/en/ar) : retirer les clés mortes `userManagement.adminDetail.activity`
  et `userManagement.adminDetail.noActivity` (lignes 44-45) — plus de section « Activité récente »

**Tests à adapter :**
- `src/test/java/com/gestiontaches/service/SprintServiceTest.java` : retirer le mock `TaskHistoryRepository`
  et les `verify(taskHistoryRepository, ...)` / `verify(...notifyAdminsOfTaskHistory...)` ; adapter si nouvelle notification
- `src/test/java/com/gestiontaches/service/NotificationServiceTest.java` : adapter/retirer les 3 tests utilisant `TaskHistory`
- `src/test/java/com/gestiontaches/web/rest/ProjectResourceIT.java` : retirer la persistance + l'assertion
  `task_history` (lignes 581-586, 629) **et** la ligne `message.setMentions(new HashSet<>(...))` (ligne 608,
  casse la compilation après A.4 — garder l'import `HashSet`, utilisé aussi ligne 649)

**Liquibase / seed / JDL :**
- Supprimer `src/main/resources/config/liquibase/changelog/20260714000001_added_entity_TaskHistory.xml`
- `master.xml` : retirer l'`<include ...TaskHistory.xml.../>` (ligne 50)
- `seed-data.sql` : retirer `task_history` du `TRUNCATE` (ligne 28)
- `project-management.jdl` : retirer l'entité `TaskHistory` (lignes 73-77), la relation `TaskHistory{user} to User`
  (ligne 115), la relation `Task{history} to TaskHistory{task required}` (ligne 132), `TaskHistory` de `paginate`
  (ligne 139) et de `service` (ligne 141)

---

### A.3 UserPresence (présence en ligne du chat — suppression totale)

**Backend — fichiers à SUPPRIMER :**
- `src/main/java/com/gestiontaches/domain/UserPresence.java`
- `src/main/java/com/gestiontaches/repository/UserPresenceRepository.java`
- `src/main/java/com/gestiontaches/service/dto/UserPresenceDTO.java`
- `src/main/java/com/gestiontaches/service/mapper/UserPresenceMapper.java`

**Backend — fichiers à MODIFIER :**
- `service/ChatService.java` :
  - retirer `ONLINE_THRESHOLD` (ligne 44), `UserPresenceRepository` + `UserPresenceMapper` (champs, paramètres, affectations)
  - `sendMessage` : retirer `updatePresence(projectId, sender.getId());` (ligne 162) — **garder** `markRead`
  - retirer les méthodes `updatePresence`, `getPresence`, `fillPresence`, `presenceByUserIds`, `toUserPresenceDTO`
  - `getMembers` : ne plus remplir la présence (garder la liste des membres)
  - `toConversationDTO` : ne plus construire `presenceByUser` ni la passer aux participants
  - retirer les imports `UserPresenceDTO`, `UserPresenceMapper`, `Duration`
- `service/dto/ChatMemberDTO.java` : retirer `online` + `lastActiveAt` (champs, getters/setters, `toString`)
- `service/dto/ConversationDTO.java` : corriger le commentaire « with role/presence » (ligne 38)
- `web/rest/ChatResource.java` : retirer les 2 endpoints `/presence` (POST heartbeat, GET présence) + import `UserPresenceDTO` + commentaires

**Frontend :**
- `src/main/webapp/app/entities/chat/chat.model.ts` : retirer `IUserPresence` + champs `online`/`lastActiveAt` de `IChatMember`
- `src/main/webapp/app/entities/chat/service/chat.service.ts` : retirer `RestUserPresence`, `updatePresence()`,
  `getPresence()`, `convertPresenceFromServer()`, la conversion `lastActiveAt`
- `src/main/webapp/app/entities/chat/chat.ts` : retirer `PRESENCE_POLL_INTERVAL`, `heartbeat()`, et l'abonnement
  `interval(PRESENCE_POLL_INTERVAL)` qui appelle `heartbeat()` — **garder** le refresh des membres (`loadMembers()`)
- `src/main/webapp/app/entities/chat/member-list/member-list.html` : retirer la pastille « en ligne » + badge online/offline
- `src/main/webapp/app/entities/chat/member-list/member-list.scss` : retirer `.chat-member-dot`, `.chat-member-dot.online`, `.chat-member-online`
- `src/main/webapp/app/entities/chat/chat-header/chat-header.ts` : retirer `onlineCount`
- `src/main/webapp/app/entities/chat/chat-header/chat-header.html` : retirer le bloc `@if (onlineCount() > 0)`
- `src/main/webapp/app/entities/chat/chat-header/chat-header.scss` : retirer `.chat-header-online`, `.chat-header-dot`

**i18n — `chat.json` (en/fr/ar) :**
- retirer `chat.members.online`, `chat.members.offline`, `chat.header.onlineCount`

**Liquibase :** dans `20260805000001_added_chat.xml`, retirer le changeSet `20260805000001-7` (`chat_user_presence`),
sa `addUniqueConstraint`, son `addForeignKeyConstraint` (`fk_chat_user_presence__user_id`)

> ⚠️ Ne PAS confondre avec le WebSocket JHipster (`/websocket/tracker`, STOMP) utilisé pour les
> notifications : celui-ci reste en place.

---

### A.4 Mentions `@user` (suppression)

**Backend :**
- `domain/ChatMessage.java` : retirer le champ `@ElementCollection mentions` (lignes 69-76) + accesseurs (182-193)
  + imports `HashSet`/`Set` + mention dans le Javadoc de classe
- `service/dto/ChatMessageDTO.java` : retirer le champ `mentions` + accesseurs + imports
- `repository/ChatMessageRepository.java` : retirer `deleteMentionsByProjectId` (lignes 19-24)
- `service/ChatService.java` : retirer `MENTION_PATTERN`, `extractMentionIds(...)`, les appels
  `message.mentions(extractMentionIds(...))` (lignes 159, 173), imports `Matcher`/`Pattern`
- `service/ProjectService.java` : retirer `chatMessageRepository.deleteMentionsByProjectId(id)` (ligne 287) + commentaire

**Frontend :**
- `src/main/webapp/app/entities/chat/chat.model.ts` : retirer `mentions?: Set<number> | null;` de `IChatMessage`
- `src/main/webapp/app/entities/chat/service/chat.service.ts` : retirer la ligne `mentions:` de `convertMessageFromServer`

**Liquibase :** dans `20260805000001_added_chat.xml`, retirer le changeSet `20260805000001-4` (`chat_message_mentions`),
son `addForeignKeyConstraint` (`fk_chat_message_mentions__message_id`) et son index `idx_chat_message_mentions__message_id`

> Il n'existe **aucune** UI d'autocomplétion/surlignage `@user` : rien à retirer côté composants de chat.

---

## 4. Partie B — Correction des erreurs 500 (suppressions)

### B.1 Supprimer une tâche → supprimer ses enfants

Dans `service/TaskService.java` → `delete(Long id)` : avant `taskRepository.deleteById(id)`, supprimer les enfants.

- **AJOUTER** dans `repository/CommentRepository.java` :
  `@Modifying @Query("DELETE FROM Comment c WHERE c.task.id = :taskId") int deleteByTaskId(...)`
- **AJOUTER** dans `repository/AttachmentRepository.java` :
  `@Modifying @Query("DELETE FROM Attachment a WHERE a.task.id = :taskId") int deleteByTaskId(...)`
- **AJOUTER** dans `repository/NotificationRepository.java` :
  `@Modifying @Query("DELETE FROM Notification n WHERE n.task.id = :taskId") int deleteByTaskId(...)`
- Injecter ces repositories dans `TaskService` et appeler les 3 `deleteByTaskId(id)` avant `deleteById`.

> `task_history` n'existant plus (Partie A), il n'y a plus de dépendance à gérer pour elle.

### B.2 Supprimer un utilisateur → nettoyer les références

Dans `service/UserService.java` → `deleteUser(String login)` : avant `userRepository.delete(user)`, nettoyer dans l'ordre :

1. **Notifications** (adressées OU liées) — `NotificationRepository` :
   `DELETE FROM Notification n WHERE n.user.id = :userId OR n.relatedUserId = :userId`
2. **Membres de projets** — `ProjectMemberRepository` : `DELETE FROM ProjectMember pm WHERE pm.user.id = :userId`
3. **Tâches assignées** — `TaskRepository` : `UPDATE Task t SET t.assignee = null WHERE t.assignee.id = :userId`
   (cohérent avec l'Opération A — on garde les tâches, on désassigne)
4. **Tâches créées** — `TaskRepository` : `UPDATE Task t SET t.createdBy = null WHERE t.createdBy.id = :userId`
   (`created_by_id` est nullable)
5. **Commentaires** — `CommentRepository` : `UPDATE Comment c SET c.author = null WHERE c.author.id = :userId`
6. **Pièces jointes** — `AttachmentRepository` : `UPDATE Attachment a SET a.uploadedBy = null WHERE a.uploadedBy.id = :userId`
7. **Messages de chat** — `ChatMessageRepository` : `DELETE FROM ChatMessage cm WHERE cm.sender.id = :userId`
8. **Membres de conversations** — `ConversationMemberRepository` : `DELETE FROM ConversationMember cm WHERE cm.user.id = :userId`
9. **Conversations créées** — `ConversationRepository` : `UPDATE Conversation c SET c.createdBy = null WHERE c.createdBy.id = :userId`

> Les tables `task_history`, `group_message`, `chat_user_presence`, `chat_message_mentions` n'existant plus
> (Partie A), leurs FK ne bloquent plus.

### B.3 Supprimer un projet → cascade déjà en place

`ProjectService.delete` est **déjà correct** et complet pour les entités conservées :
mentions → messages → conversation members → conversations → notifications → commentaires → pièces jointes →
tâches → sprints → epics → membres → projet.

- Après la Partie A : retirer uniquement `deleteMentionsByProjectId` et `taskHistoryRepository.deleteByTaskProjectId`
  (déjà prévu en A.2 / A.4).
- Plus de problème `group_message` (entité supprimée en A.1).

---

## 5. Partie C — Opération A : retrait d'un membre du projet → désassignation

Comportement cible : quand un membre est retiré d'un projet, ses tâches du projet passent en `assignee = null`
(on ne les supprime pas).

Dans `service/ProjectService.java` → `removeMember(Long projectId, Long userId)` :

- **AJOUTER** dans `repository/TaskRepository.java` :
  `@Modifying @Query("UPDATE Task t SET t.assignee = null WHERE t.assignee.id = :userId AND t.project.id = :projectId")
  int unassignTasksInProject(@Param("userId") Long userId, @Param("projectId") Long projectId)`
- Dans `removeMember`, après `projectMemberRepository.delete(member)` :
  `taskRepository.unassignTasksInProject(userId, projectId);`
- (Le message « supprimer les tâches » est écarté : les tâches appartiennent au projet.)

---

## 6. Base de données (dev)

1. **Recréer la base** (elle est vide en dev) :
   ```bash
   psql -h localhost -U brahim -d postgres -c 'DROP DATABASE IF EXISTS gestion_taches;'
   psql -h localhost -U brahim -d postgres -c 'CREATE DATABASE gestion_taches;'
   ```
   Le démarrage de l'app (`./mvnw`) rejouera Liquibase avec les changelogs nettoyés → tables recréées sans
   `task_history`, `group_message`, `chat_user_presence`, `chat_message_mentions`.
2. **`seed-data.sql`** : retirer `task_history` et `group_message` du `TRUNCATE` (§A.1, §A.2).
3. Le répertoire `target/h2db/` est un artefact de test — ignoré.

---

## 7. Documentation à nettoyer

Après validation du code, mettre à jour les fichiers markdown qui citent les entités supprimées :
- `README.md` (lignes 26-32, 42, 207-211, 263) — `GroupMessage`, `TaskHistory`, présence, mentions
  (+ optionnel : aligner la section Base de données, lignes 89-102, qui documente `gestionTaches` ≠ `gestion_taches`/`postgres` réel)
- `docs/fiche-classes.md`, `docs_role.md` (lignes 92, 242, 584-589, 840 — onglet Historique / TaskHistory)
- `docs/api/group-message.md` (SUPPRIMER), `docs/api/task-history.md` (SUPPRIMER), `docs/api/chat.md`,
  `docs/api/README.md`, `docs/api/users.md`, `docs/api/notification.md`, `docs/api/sprint.md` (ligne 343)
- `docs/technique/README.md`, `docs/technique/modele-donnees.md`, `docs/technique/chat-temps-reel.md`,
  `docs/technique/architecture.md`
- `docs/fonctionnel/README.md`, `docs/fonctionnel/audit.md` (**SUPPRIMER** : toute la fonctionnalité `TaskHistory`
  disparaît), `docs/fonctionnel/roles-et-permissions.md` (ligne 137), `docs/fonctionnel/chat.md`,
  `docs/fonctionnel/notifications.md` (lignes 24, 71), `docs/fonctionnel/cycle-de-vie-sprint.md` (lignes 70, 118)
- `docs/utilisateur/messagerie.md` (lignes 22, 69-75 — présence), `docs/utilisateur/demarrage.md` (ligne 34),
  `docs/utilisateur/gestion-taches.md` (lignes 43, 105-109, 132 — onglet Historique)

---

## 8. Vérification finale

```bash
# 1. Références orphelines (doit remonter seulement les docs/README restants avant nettoyage)
rg -ri "groupmessage|group-message|taskhistory|task-history|userpresence|chat_user_presence|presence|mentions" \
   src src/test project-management.jdl .jhipster seed-data.sql --type-add 'all:*.{java,ts,tsx,html,json,jdl,xml,scss}' -t all

# 2. Backend : compile + tests
./mvnw clean verify

# 3. Frontend : build + tests
./npmw run build
./npmw test
```

Critères de fin :
- `./mvnw clean verify` OK (compilation + tests).
- `./npmw run build` et `./npmw test` OK.
- Plus de 500 sur `DELETE /api/tasks/{id}`, `DELETE /api/admin/users/{login}`, `DELETE /api/projects/{id}`.
- Retrait d'un membre → ses tâches du projet réapparaissent sans assignee.
- `rg` ci-dessus ne remonte que les fichiers de docs restants (ou plus rien après §7).

---

## 9. Guide d'exécution pour l'assistant

Ordre de travail recommandé (chaque étape est suivie d'une recompilation pour détecter les références cassées) :

### Étape 1 — Base saine avant tout
- Lire les fichiers touchés avant de les modifier.
- Travailler par petites passes et recompiler entre chaque passe.

### Étape 2 — Partie A.1 (GroupMessage)
1. Supprimer les 6 fichiers backend + le dossier frontend `entities/group-message/`.
2. Nettoyer `project-detail.ts`.
3. Supprimer i18n (3 langues), `.jhipster/GroupMessage.json`, le changelog orphelin.
4. `seed-data.sql` : retirer `group_message` du TRUNCATE.
5. Recompiler : `./mvnw -q compile` + `./npmw run build`.

### Étape 3 — Partie A.2 (TaskHistory)
1. Supprimer les 6 fichiers backend + dossiers/fichiers frontend (`task-history/`, `task-activity-feed*`, `task-history-tab*`).
2. Modifier `SprintService`, `NotificationService` (ajouter la notification équivalente), `ProjectService`,
   `UserResource`, `UserAdminDetailDTO`, `UserAdminDetailMapper`.
3. Frontend : `task-detail.ts/html`, `developer-dashboard.component.ts`, `timeline.component.ts` (retirer
   `activitiesOverride`), `user-management.service.ts`, `user-admin-detail.html`.
4. i18n (`actionHistory.json`, `global.json`, `task.json`, `user-management.json`).
5. Tests : `SprintServiceTest`, `NotificationServiceTest`, `ProjectResourceIT` (lignes 581-586, 608, 629).
6. Liquibase + `master.xml` + `seed-data.sql` + `project-management.jdl`.
7. Recompiler + tests : `./mvnw clean verify`.

### Étape 4 — Partie A.3 (présence)
1. Supprimer `UserPresence` (+ repo, DTO, mapper).
2. Nettoyer `ChatService`, `ChatMemberDTO`, `ConversationDTO`, `ChatResource`.
3. Frontend chat : `chat.model.ts`, `chat.service.ts`, `chat.ts`, `member-list.html/scss`, `chat-header.ts/html/scss`.
4. i18n `chat.json`.
5. Liquibase `20260805000001_added_chat.xml`.
6. Recompiler.

### Étape 5 — Partie A.4 (mentions)
1. Nettoyer `ChatMessage`, `ChatMessageDTO`, `ChatMessageRepository`, `ChatService`, `ProjectService`.
2. Frontend : `chat.model.ts`, `chat.service.ts`.
3. Liquibase `20260805000001_added_chat.xml`.
4. Recompiler.

### Étape 6 — Partie B (correction des 500)
1. B.1 : méthodes `deleteByTaskId` + `TaskService.delete`.
2. B.2 : méthodes de nettoyage utilisateur + `UserService.deleteUser`.
3. Recompiler.

### Étape 7 — Partie C (Opération A)
1. `TaskRepository.unassignTasksInProject` + `ProjectService.removeMember`.
2. Recompiler.

### Étape 8 — Base de données
- Recréer la base (§6) puis lancer l'app pour laisser Liquibase créer le schéma.
- Vérifier qu'aucune table `task_history`, `group_message`, `chat_user_presence`, `chat_message_mentions` n'existe.

### Étape 9 — Vérification finale (§8)
- `./mvnw clean verify`
- `./npmw run build` + `./npmw test`
- `rg` des références orphelines

### Étape 10 — Documentation (§7)
- Mettre à jour `README.md`, `docs/*` selon la liste §7.

> Règle : **aucune hypothèse silencieuse**. Tout comportement métier ambigu est signalé à l'utilisateur
> avant d'implémenter. Garder le style de code existant (nommage, structure des packages).
