# Recherche, Événements temps réel & Export CSV

## Vue d'ensemble

| Endpoint | Description | Rôle requis |
|----------|-------------|-------------|
| `GET /api/search?q=...` | Recherche globale (projets, tâches, sprints, epics) | Authentifié |
| `GET /api/events/stream` | Flux SSE des changements d'entités | Authentifié |
| `GET /api/export/csv/projects` | Export CSV des projets | `ROLE_ADMIN` |
| `GET /api/export/csv/tasks` | Export CSV de toutes les tâches | `ROLE_ADMIN` |
| `GET /api/export/csv/projects/{projectId}/tasks` | Export CSV des tâches d'un projet | `ROLE_ADMIN` + `OWNER`/`MANAGER` du projet |
| `GET /api/export/csv/users` | Export CSV des utilisateurs | `ROLE_ADMIN` |

---

## 1. Recherche globale

```http
GET /api/search?q=login
Authorization: Bearer <token>
```

**Rôle requis** : authentifié.

La recherche est **scopée aux projets dont l'utilisateur est propriétaire ou membre**. Elle interroge quatre types d'entités et retourne au maximum **10 résultats par type**.

**Paramètres de query**

| Paramètre | Type | Requis | Description |
|-----------|------|--------|-------------|
| `q` | `String` | Oui | Requête de recherche (ignorée si vide) |

**Réponse 200 OK** — liste de `SearchResultDTO`.

| Champ | Type | Description |
|-------|------|-------------|
| `type` | `String` | `project`, `task`, `sprint` ou `epic` |
| `id` | `Long` | ID de l'entité |
| `title` | `String` | Nom/titre de l'entité |
| `description` | `String` | Description / objectif |
| `projectKey` | `String` | Clé du projet associé |
| `status` | `String` | Statut (tâches, sprints, epics) |
| `link` | `String` | Route front-end associée (ex : `/project/MPROJ/task/12/view`) |

```json
[
  {
    "type": "project",
    "id": 1,
    "title": "Mon Projet",
    "description": "Description du projet",
    "projectKey": "MPROJ",
    "status": null,
    "link": "/project/MPROJ/view"
  },
  {
    "type": "task",
    "id": 12,
    "title": "Implémenter login JWT",
    "description": "Créer le endpoint d'authentification",
    "projectKey": "MPROJ",
    "status": "IN_PROGRESS",
    "link": "/project/MPROJ/task/12/view"
  },
  {
    "type": "sprint",
    "id": 3,
    "title": "Sprint 2",
    "description": "Livrer le MVP",
    "projectKey": "MPROJ",
    "status": "ACTIVE",
    "link": "/project/MPROJ/sprint/3/view"
  },
  {
    "type": "epic",
    "id": 5,
    "title": "Epic Authentification",
    "description": "Gestion de l'authentification",
    "projectKey": "MPROJ",
    "status": "IN_PROGRESS",
    "link": "/project/MPROJ/epic/5/view"
  }
]
```

**Comportement**

- `q` vide ou sans utilisateur connecté → liste vide.
- L'utilisateur sans aucun projet → seuls les projets qu'il possède sont cherchés.

---

## 2. Flux SSE des événements d'entités

```http
GET /api/events/stream
Authorization: Bearer <token>
Accept: text/event-stream
```

**Rôle requis** : authentifié (`@PreAuthorize("isAuthenticated()")`).

Ouvre une connexion Server-Sent Events qui pousse chaque changement d'entité (création, mise à jour, suppression) au format suivant :

```
event: entity-change
data: {"entityType":"SPRINT","eventType":"UPDATED","entityId":3,"projectId":1}
```

**Payload** (`EntityChangeEvent`)

| Champ | Type | Valeurs possibles |
|-------|------|-------------------|
| `entityType` | `String` | `TASK`, `SPRINT`, `EPIC`, `PROJECT`, `PROJECT_MEMBER` |
| `eventType` | `String` | `CREATED`, `UPDATED`, `DELETED` |
| `entityId` | `Long` | ID de l'entité concernée |
| `projectId` | `Long` | ID du projet associé |

Les événements sont émis par les services métier lors des opérations de création, mise à jour et suppression (ex. : `SprintService`, `EpicService`, `TaskService`, gestion des membres).

---

## 3. Export CSV — Tous les projets

```http
GET /api/export/csv/projects
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN`.

**Réponse 200 OK** — `text/csv; charset=UTF-8`, `Content-Disposition: attachment; filename="projects.csv"`.

Colonnes : `ID,Key,Name,Description,CreatedAt,OwnerLogin`

```
ID,Key,Name,Description,CreatedAt,OwnerLogin
1,MPROJ,Mon Projet,Description du projet,2026-08-10T12:00:00Z,admin
```

---

## 4. Export CSV — Toutes les tâches

```http
GET /api/export/csv/tasks
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN`.

**Réponse 200 OK** — `attachment; filename="tasks.csv"`.

Colonnes : `ID,Title,Status,Priority,ProjectKey,SprintName,EpicTitle,AssigneeLogin,CreatedAt`

---

## 5. Export CSV — Tâches d'un projet

```http
GET /api/export/csv/projects/{projectId}/tasks
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN` **et** rôle `OWNER` ou `MANAGER` dans le projet.

**Réponse 200 OK** — `attachment; filename="project-{projectId}-tasks.csv"`.

Colonnes : `ID,Title,Status,Priority,AssigneeLogin,CreatedAt,UpdatedAt`

**Erreurs**

| Code | Signification |
|------|---------------|
| 403 | Pas `ROLE_ADMIN`, ou pas `OWNER`/`MANAGER` de ce projet |

---

## 6. Export CSV — Utilisateurs

```http
GET /api/export/csv/users
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN`.

**Réponse 200 OK** — `attachment; filename="users.csv"`.

Colonnes : `ID,Login,FirstName,LastName,Email,Activated,LangKey,CreatedBy,CreatedDate`

---

> Remarque sur l'échappement CSV : les valeurs contenant une virgule, des guillemets ou un saut de ligne sont encadrées par des guillemets doubles, avec doublage des guillemets internes (`""`).
