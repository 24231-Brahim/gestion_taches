# Sprints

Base URL : `http://localhost:8080/api/sprints`

## Schéma SprintDTO

| Champ | Type | Contraintes | Description |
|-------|------|-------------|-------------|
| `id` | `Long` | — | Identifiant unique |
| `name` | `String` | `@NotNull @Size(min=1, max=100)` | Nom du sprint |
| `goal` | `String` | `@Size(max=500)` | Objectif du sprint |
| `startDate` | `LocalDate` | — | Date de début (format `YYYY-MM-DD`) |
| `endDate` | `LocalDate` | — | Date de fin (format `YYYY-MM-DD`) |
| `status` | `SprintStatus` | `@NotNull` | Statut du sprint |
| `project` | `ProjectDTO` | `@NotNull` | Projet associé (au moins `id` requis) |

### SprintStatus

| Valeur | Description |
|--------|-------------|
| `PLANNED` | Planifié |
| `ACTIVE` | En cours |
| `COMPLETED` | Terminé |
| `CANCELLED` | Annulé |

## Workflow de statut

Les transitions autorisées sont validées par le service :

```
PLANNED → ACTIVE
ACTIVE → COMPLETED
ACTIVE → CANCELLED
```

- Un sprint `COMPLETED` ou `CANCELLED` ne peut plus changer de statut.
- Un projet ne peut avoir qu'un seul sprint `ACTIVE` à la fois.
- Le statut est aussi recalculé automatiquement lors de la modification des tâches :
  - Si au moins une tâche est `IN_PROGRESS` ou `READY_FOR_TEST` → `ACTIVE`
  - Si toutes les tâches sont `DONE` → `COMPLETED`
  - Sinon → `PLANNED` (par défaut, sauf si déjà `COMPLETED`)

## Endpoints

### 1. Créer un sprint

```http
POST /api/sprints
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_USER` (doit être `OWNER`/`MANAGER` du projet)

**Corps de requête**

```json
{
  "name": "Sprint 1",
  "goal": "Livrer le MVP",
  "startDate": "2026-08-15",
  "endDate": "2026-08-30",
  "project": {
    "id": 1
  }
}
```

**Réponse 201 Created**

```json
{
  "id": 1,
  "name": "Sprint 1",
  "goal": "Livrer le MVP",
  "startDate": "2026-08-15",
  "endDate": "2026-08-30",
  "status": "PLANNED",
  "project": {
    "id": 1,
    "name": "Mon Projet",
    "description": "Description",
    "key": "MPROJ",
    "createdAt": "2026-08-10T12:00:00Z",
    "ownerId": 1,
    "ownerLogin": "admin",
    "projectMembers": []
  }
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | Le sprint possède déjà un ID (`idexists`) |
| 403 | Rôle insuffisant sur le projet |

---

### 2. Mettre à jour un sprint

```http
PUT /api/sprints/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_USER` (doit être `OWNER`/`MANAGER` du projet)

**Corps de requête**

```json
{
  "id": 1,
  "name": "Sprint 1 bis",
  "goal": "Livrer le MVP v2",
  "startDate": "2026-08-15",
  "endDate": "2026-08-30",
  "status": "ACTIVE",
  "project": {
    "id": 1
  }
}
```

**Réponse 200 OK**

```json
{
  "id": 1,
  "name": "Sprint 1 bis",
  "goal": "Livrer le MVP v2",
  "startDate": "2026-08-15",
  "endDate": "2026-08-30",
  "status": "ACTIVE",
  "project": {
    "id": 1,
    "name": "Mon Projet",
    "description": "Description",
    "key": "MPROJ",
    "createdAt": "2026-08-10T12:00:00Z",
    "ownerId": 1,
    "ownerLogin": "admin",
    "projectMembers": []
  }
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | ID manquant ou invalide, transition de statut non autorisée (`invalidstatus`), ou plusieurs sprints ACTIVE (`activeexists`) |
| 404 | Sprint non trouvé (`idnotfound`) |
| 403 | Rôle insuffisant |

---

### 3. Mise à jour partielle

```http
PATCH /api/sprints/{id}
Authorization: Bearer <token>
Content-Type: application/merge-patch+json
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_USER` (doit être `OWNER`/`MANAGER` du projet)

**Corps de requête**

```json
{
  "status": "ACTIVE"
}
```

**Réponse 200 OK**

Mêmes champs que `SprintDTO`.

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | ID manquant, transition de statut non autorisée |
| 404 | Sprint non trouvé |
| 403 | Rôle insuffisant |

---

### 4. Lister les sprints

```http
GET /api/sprints?page=0&size=20&sort=startDate,desc&projectId.equals=1
Authorization: Bearer <token>
```

**Rôle requis** : accès au projet (via `projectId` filtre) ou `ROLE_ADMIN`

**Paramètres de query**

| Paramètre | Type | Description |
|-----------|------|-------------|
| `page` | `int` | Numéro de page |
| `size` | `int` | Taille de page |
| `sort` | `String` | Tri |
| `projectId.equals` | `Long` | Filtrer par projet |
| `name.contains` | `String` | Filtrer par nom |
| `status.equals` | `SprintStatus` | Filtrer par statut |
| `startDate.*` | `LocalDate` | Filtrer par date de début |
| `endDate.*` | `LocalDate` | Filtrer par date de fin |

**Réponse 200 OK**

```json
[
  {
    "id": 1,
    "name": "Sprint 1",
    "goal": "Livrer le MVP",
    "startDate": "2026-08-15",
    "endDate": "2026-08-30",
    "status": "ACTIVE",
    "project": { "id": 1, "name": "Mon Projet", "key": "MPROJ", ... }
  }
]
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 403 | `projectId` requis si pas d'accès global (`ADMIN`) |

---

### 5. Compter les sprints

```http
GET /api/sprints/count?projectId.equals=1
Authorization: Bearer <token>
```

**Rôle requis** : accès au projet ou `ROLE_ADMIN`

**Réponse 200 OK**

```json
5
```

---

### 6. Récupérer un sprint

```http
GET /api/sprints/{id}
Authorization: Bearer <token>
```

**Rôle requis** : accès au projet

**Réponse 200 OK**

```json
{
  "id": 1,
  "name": "Sprint 1",
  "goal": "Livrer le MVP",
  "startDate": "2026-08-15",
  "endDate": "2026-08-30",
  "status": "ACTIVE",
  "project": { "id": 1, "name": "Mon Projet", "key": "MPROJ", ... }
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 404 | Sprint non trouvé |
| 403 | Pas d'accès au projet |

---

### 7. Supprimer un sprint

```http
DELETE /api/sprints/{id}
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_USER` (doit être `OWNER`/`MANAGER` du projet)

**Réponse 204 No Content**

---

### 8. Démarrer un sprint

```http
POST /api/sprints/{id}/start
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_USER` (doit être `OWNER`/`MANAGER` du projet)

Passe le statut de `PLANNED` à `ACTIVE`. Une notification est envoyée aux membres du projet (sauf l'initiateur).

**Réponse 200 OK**

```json
{
  "id": 1,
  "name": "Sprint 1",
  "goal": "Livrer le MVP",
  "startDate": "2026-08-15",
  "endDate": "2026-08-30",
  "status": "ACTIVE",
  "project": { "id": 1, "name": "Mon Projet", "key": "MPROJ", ... }
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | Sprint non trouvé, pas `PLANNED`, ou un autre sprint ACTIVE existe déjà |
| 403 | Rôle insuffisant |

---

### 9. Clôturer un sprint

```http
POST /api/sprints/{id}/close
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_USER` (doit être `OWNER`/`MANAGER` du projet)

Passe le statut de `ACTIVE` à `COMPLETED`. Les tâches non `DONE` sont déplacées vers le backlog et un historique est créé. Un rapport de vélocité est retourné.

**Réponse 200 OK**

```json
{
  "tachesPrevues": 10,
  "tachesTerminees": 7,
  "pourcentage": 70,
  "tachesReportees": 3
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | Sprint non trouvé ou pas `ACTIVE` |
| 403 | Rôle insuffisant |

---

### 10. Backlog du projet

```http
GET /api/projects/{projectId}/backlog
Authorization: Bearer <token>
```

**Rôle requis** : accès au projet

Retourne les tâches du projet qui ne sont assignées à aucun sprint (`sprint IS NULL`).

**Réponse 200 OK**

```json
[
  {
    "id": 1,
    "title": "Tâche sans sprint",
    "description": "...",
    "status": "NEW",
    "priority": "MEDIUM",
    "createdAt": "2026-08-10T12:00:00Z",
    "project": { "id": 1, "name": "Mon Projet", "key": "MPROJ" }
  }
]
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 403 | Pas d'accès au projet |
