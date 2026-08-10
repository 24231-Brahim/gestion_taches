# Tasks

Base URL : `http://localhost:8080/api/tasks`

## Schéma TaskDTO

| Champ | Type | Contraintes | Description |
|-------|------|-------------|-------------|
| `id` | `Long` | — | Identifiant unique |
| `title` | `String` | `@NotNull @Size(min=1, max=200)` | Titre de la tâche |
| `description` | `String` | `@Size(max=5000)` | Description |
| `status` | `TaskStatus` | `@NotNull` | Statut de la tâche |
| `priority` | `Priority` | `@NotNull` | Priorité |
| `createdAt` | `Instant` | `@NotNull` | Date de création |
| `updatedAt` | `Instant` | — | Date de dernière mise à jour |
| `storyPoints` | `Integer` | — | Points d'effort |
| `sprint` | `SprintDTO` | — | Sprint associé |
| `epic` | `EpicDTO` | — | Epic associé |
| `project` | `ProjectDTO` | `@NotNull` | Projet associé (au moins `id` requis) |
| `assignee` | `UserDTO` | — | Utilisateur assigné (`id`, `login`) |
| `createdBy` | `UserDTO` | — | Créateur (`id`, `login`) |

### TaskStatus

| Valeur | Description |
|--------|-------------|
| `NEW` | Nouveau |
| `IN_PROGRESS` | En cours |
| `READY_FOR_TEST` | Prêt pour test |
| `DONE` | Terminé |
| `NEEDS_INFO` | Besoin d'informations |

### Priority

| Valeur | Description |
|--------|-------------|
| `LOWEST` | Très basse |
| `LOW` | Basse |
| `MEDIUM` | Moyenne |
| `HIGH` | Haute |
| `HIGHEST` | Très haute |

## Workflow de statut

Contrairement aux Sprints et Epics, **aucune transition de statut n'est validée côté service** pour les tâches. Le statut peut être modifié librement via `PUT` ou `PATCH`.

Les seules règles métier documentées dans le code sont :

- La création fixe le statut à `NEW` si aucun statut n'est fourni.
- Le recalcul automatique du statut du Sprint parent :
  - `IN_PROGRESS` ou `READY_FOR_TEST` → Sprint `ACTIVE`
  - `DONE` → Sprint `COMPLETED`
  - Sinon → Sprint `PLANNED`
- Le recalcul automatique du statut de l'Epic parent :
  - `IN_PROGRESS` ou `READY_FOR_TEST` → Epic `IN_PROGRESS`
  - `DONE` → Epic `DONE`
  - Sinon → Epic `TODO`

## Permissions de modification

| Action | Rôle requis |
|--------|-------------|
| Créer une tâche | `ROLE_ADMIN`, `ROLE_PROJET_MANAGER`, `ROLE_DEVELOPER` (doit être `OWNER`/`MANAGER` du projet) |
| Modifier/supprimer une tâche | `ROLE_ADMIN`, `ROLE_PROJET_MANAGER`, `ROLE_DEVELOPER` |
| `OWNER`/`MANAGER` | Peut modifier n'importe quelle tâche du projet, y compris l'assigné |
| `MEMBER`/`DEVELOPER` | Peut modifier **seulement ses propres tâches assignées** ; ne peut pas changer l'assigné |

> Note : le rôle `ROLE_USER` (utilisateur basique) n'est **pas** autorisé sur les endpoints de tâche (seuls `ADMIN`, `PROJET_MANAGER`, `DEVELOPER` le sont).

## Notifications déclenchées

- **Assignation** : notification à l'utilisateur assigné.
- **Changement de statut** : notification à l'assigné (s'il n'est pas l'initiateur).
- **Passage à DONE** : si le créateur ou l'assigné est `ROLE_ADMIN`, une notification leur est envoyée.

## Endpoints

### 1. Créer une tâche dans un projet

```http
POST /api/projects/{projectId}/tasks
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_DEVELOPER` (doit être `OWNER`/`MANAGER` du projet)

**Corps de requête**

```json
{
  "title": "Implémenter login JWT",
  "description": "Créer le endpoint d'authentification",
  "status": "NEW",
  "priority": "HIGH",
  "storyPoints": 5,
  "project": {
    "id": 1
  },
  "assignee": {
    "id": 5
  }
}
```

**Réponse 201 Created**

```json
{
  "id": 1,
  "title": "Implémenter login JWT",
  "description": "Créer le endpoint d'authentification",
  "status": "NEW",
  "priority": "HIGH",
  "createdAt": "2026-08-10T12:00:00Z",
  "updatedAt": null,
  "storyPoints": 5,
  "sprint": null,
  "epic": null,
  "project": {
    "id": 1,
    "name": "Mon Projet",
    "description": "Description",
    "key": "MPROJ",
    "createdAt": "2026-08-10T12:00:00Z",
    "ownerId": 1,
    "ownerLogin": "admin",
    "projectMembers": []
  },
  "assignee": {
    "id": 5,
    "login": "johndoe"
  },
  "createdBy": {
    "id": 1,
    "login": "admin"
  }
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | La tâche possède déjà un ID (`idexists`) |
| 403 | Rôle insuffisant ou utilisateur non membre du projet |

---

### 2. Créer une tâche (générique)

```http
POST /api/tasks
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_DEVELOPER`

Même corps que ci-dessus. Le `project.id` doit être fourni.

**Réponse 201 Created**

---

### 3. Mettre à jour une tâche

```http
PUT /api/tasks/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_DEVELOPER`

**Corps de requête**

```json
{
  "id": 1,
  "title": "Implémenter login JWT — terminé",
  "description": "Créer le endpoint d'authentification",
  "status": "DONE",
  "priority": "HIGH",
  "storyPoints": 5,
  "project": {
    "id": 1
  },
  "assignee": {
    "id": 5
  }
}
```

**Réponse 200 OK**

```json
{
  "id": 1,
  "title": "Implémenter login JWT — terminé",
  "description": "Créer le endpoint d'authentification",
  "status": "DONE",
  "priority": "HIGH",
  "createdAt": "2026-08-10T12:00:00Z",
  "updatedAt": "2026-08-10T14:00:00Z",
  "storyPoints": 5,
  "sprint": null,
  "epic": null,
  "project": { "id": 1, "name": "Mon Projet", "key": "MPROJ", ... },
  "assignee": { "id": 5, "login": "johndoe" },
  "createdBy": { "id": 1, "login": "admin" }
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | ID manquant ou invalide |
| 403 | Modification d'une tâche qui n'est pas la sienne (pour `MEMBER`/`DEVELOPER`), ou changement d'assigné interdit |
| 404 | Tâche non trouvée (`idnotfound`) |

---

### 4. Mise à jour partielle

```http
PATCH /api/tasks/{id}
Authorization: Bearer <token>
Content-Type: application/merge-patch+json
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_DEVELOPER`

Seuls les champs non-nuls du corps sont mis à jour.

**Corps de requête**

```json
{
  "status": "IN_PROGRESS"
}
```

**Réponse 200 OK**

Mêmes champs que `TaskDTO`.

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | ID manquant ou invalide |
| 403 | Modification interdite (voir règles ci-dessus) |
| 404 | Tâche non trouvée |

---

### 5. Lister les tâches

```http
GET /api/tasks?page=0&size=20&sort=createdAt,desc&projectId.equals=1&status.equals=NEW
Authorization: Bearer <token>
```

**Rôle requis** : accès au projet (via `projectId` filtre) ou `ROLE_ADMIN`, ou requête auto-scopée (`assigneeId.equals=<monId>`)

**Paramètres de query**

| Paramètre | Type | Description |
|-----------|------|-------------|
| `page` | `int` | Numéro de page |
| `size` | `int` | Taille de page |
| `sort` | `String` | Tri |
| `projectId.equals` | `Long` | Filtrer par projet |
| `projectId.in` | `Long` | Filtrer par plusieurs projets (séparés par `,`) |
| `status.equals` | `TaskStatus` | Filtrer par statut |
| `priority.equals` | `Priority` | Filtrer par priorité |
| `assigneeId.equals` | `Long` | Filtrer par assigné |
| `sprintId.equals` | `Long` | Filtrer par sprint |
| `epicId.equals` | `Long` | Filtrer par epic |
| `title.contains` | `String` | Recherche dans le titre |
| `description.contains` | `String` | Recherche dans la description |
| `createdAt.*` | `Instant` | Filtrer par date de création |
| `updatedAt.*` | `Instant` | Filtrer par date de mise à jour |

**Réponse 200 OK**

```json
[
  {
    "id": 1,
    "title": "Implémenter login JWT",
    "description": "Créer le endpoint d'authentification",
    "status": "DONE",
    "priority": "HIGH",
    "createdAt": "2026-08-10T12:00:00Z",
    "updatedAt": "2026-08-10T14:00:00Z",
    "storyPoints": 5,
    "sprint": null,
    "epic": null,
    "project": { "id": 1, "name": "Mon Projet", "key": "MPROJ" },
    "assignee": { "id": 5, "login": "johndoe" },
    "createdBy": { "id": 1, "login": "admin" }
  }
]
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 403 | `projectId` ou `assigneeId` requis si pas d'accès global (`ADMIN`) |

---

### 6. Compter les tâches

```http
GET /api/tasks/count?projectId.equals=1&status.equals=NEW
Authorization: Bearer <token>
```

**Rôle requis** : accès au projet ou `ROLE_ADMIN`

**Réponse 200 OK**

```json
12
```

---

### 7. Récupérer une tâche

```http
GET /api/tasks/{id}
Authorization: Bearer <token>
```

**Rôle requis** : accès au projet, ou la tâche est assignée à l'utilisateur connecté

**Réponse 200 OK**

```json
{
  "id": 1,
  "title": "Implémenter login JWT",
  "description": "Créer le endpoint d'authentification",
  "status": "DONE",
  "priority": "HIGH",
  "createdAt": "2026-08-10T12:00:00Z",
  "updatedAt": "2026-08-10T14:00:00Z",
  "storyPoints": 5,
  "sprint": null,
  "epic": null,
  "project": { "id": 1, "name": "Mon Projet", "key": "MPROJ" },
  "assignee": { "id": 5, "login": "johndoe" },
  "createdBy": { "id": 1, "login": "admin" }
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 404 | Tâche non trouvée |
| 403 | Pas d'accès au projet et pas auto-assigné |

---

### 8. Assigner une tâche

```http
PATCH /api/tasks/{id}/assign
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_DEVELOPER` (doit être `OWNER`/`MANAGER` du projet)

L'utilisateur assigné doit avoir le rôle `ROLE_DEVELOPER` ou `ROLE_PROJET_MANAGER` et être membre du projet.

**Corps de requête**

```json
{
  "userId": 5
}
```

**Réponse 200 OK**

```json
{
  "id": 1,
  "title": "Implémenter login JWT",
  "status": "NEW",
  "priority": "HIGH",
  "project": { "id": 1, "name": "Mon Projet", "key": "MPROJ" },
  "assignee": { "id": 5, "login": "johndoe" },
  "createdBy": { "id": 1, "login": "admin" }
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | `userId` manquant, utilisateur non trouvé, ou rôle invalide (`invalidrole`) |
| 403 | Rôle insuffisant |
| 404 | Tâche non trouvée |

---

### 9. Supprimer une tâche

```http
DELETE /api/tasks/{id}
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_DEVELOPER` (doit être `OWNER`/`MANAGER` du projet)

**Réponse 204 No Content**
