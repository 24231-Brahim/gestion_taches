# Epics

Base URL : `http://localhost:8080/api/epics`

## Schéma EpicDTO

| Champ | Type | Contraintes | Description |
|-------|------|-------------|-------------|
| `id` | `Long` | — | Identifiant unique |
| `title` | `String` | `@NotNull @Size(min=1, max=200)` | Titre de l'epic |
| `description` | `String` | `@Size(max=1000)` | Description |
| `status` | `EpicStatus` | `@NotNull` | Statut de l'epic |
| `priority` | `Priority` | `@NotNull` | Priorité |
| `createdAt` | `Instant` | `@NotNull` | Date de création |
| `updatedAt` | `Instant` | — | Date de dernière mise à jour |
| `startDate` | `LocalDate` | — | Date de début |
| `endDate` | `LocalDate` | — | Date de fin |
| `project` | `ProjectDTO` | `@NotNull` | Projet associé (au moins `id` requis) |

### EpicStatus

| Valeur | Description |
|--------|-------------|
| `TODO` | À faire |
| `IN_PROGRESS` | En cours |
| `DONE` | Terminé |
| `CANCELLED` | Annulé |

### Priority

| Valeur | Description |
|--------|-------------|
| `LOWEST` | Très basse |
| `LOW` | Basse |
| `MEDIUM` | Moyenne |
| `HIGH` | Haute |
| `HIGHEST` | Très haute |

## Workflow de statut

Les transitions autorisées sont validées par le service :

```
TODO → IN_PROGRESS
IN_PROGRESS → DONE
IN_PROGRESS → CANCELLED
```

- Un epic `DONE` ou `CANCELLED` ne peut plus changer de statut.
- Le statut est aussi recalculé automatiquement selon les tâches :
  - Si au moins une tâche est `IN_PROGRESS` ou `READY_FOR_TEST` → `IN_PROGRESS`
  - Si toutes les tâches sont `DONE` → `DONE`
  - Sinon → `TODO`

## Endpoints

### 1. Créer un epic

```http
POST /api/epics
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_USER` (doit être `OWNER`/`MANAGER` du projet)

**Corps de requête**

```json
{
  "title": "Epic Authentification",
  "description": "Gestion de l'authentification et des autorisations",
  "status": "TODO",
  "priority": "HIGH",
  "startDate": "2026-08-01",
  "endDate": "2026-08-15",
  "project": {
    "id": 1
  }
}
```

**Réponse 201 Created**

```json
{
  "id": 1,
  "title": "Epic Authentification",
  "description": "Gestion de l'authentification et des autorisations",
  "status": "TODO",
  "priority": "HIGH",
  "createdAt": "2026-08-10T12:00:00Z",
  "updatedAt": null,
  "startDate": "2026-08-01",
  "endDate": "2026-08-15",
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
| 400 | L'epic possède déjà un ID (`idexists`) |
| 403 | Rôle insuffisant sur le projet |

---

### 2. Mettre à jour un epic

```http
PUT /api/epics/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_USER` (doit être `OWNER`/`MANAGER` du projet)

**Corps de requête**

```json
{
  "id": 1,
  "title": "Epic Authentification v2",
  "description": "Mise à jour de la description",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "startDate": "2026-08-01",
  "endDate": "2026-08-15",
  "project": {
    "id": 1
  }
}
```

**Réponse 200 OK**

```json
{
  "id": 1,
  "title": "Epic Authentification v2",
  "description": "Mise à jour de la description",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "createdAt": "2026-08-10T12:00:00Z",
  "updatedAt": "2026-08-10T14:00:00Z",
  "startDate": "2026-08-01",
  "endDate": "2026-08-15",
  "project": { "id": 1, "name": "Mon Projet", "key": "MPROJ", ... }
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | ID manquant ou invalide, transition de statut non autorisée (`invalidstatus`) |
| 404 | Epic non trouvé (`idnotfound`) |
| 403 | Rôle insuffisant |

---

### 3. Mise à jour partielle

```http
PATCH /api/epics/{id}
Authorization: Bearer <token>
Content-Type: application/merge-patch+json
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_USER` (doit être `OWNER`/`MANAGER` du projet)

**Corps de requête**

```json
{
  "status": "DONE"
}
```

**Réponse 200 OK**

Mêmes champs que `EpicDTO`.

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | ID manquant, transition de statut non autorisée |
| 404 | Epic non trouvé |
| 403 | Rôle insuffisant |

---

### 4. Lister les epics

```http
GET /api/epics?page=0&size=20&sort=createdAt,desc&projectId.equals=1&status.equals=IN_PROGRESS
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
| `title.contains` | `String` | Filtrer par titre |
| `status.equals` | `EpicStatus` | Filtrer par statut |
| `priority.equals` | `Priority` | Filtrer par priorité |
| `createdAt.*` | `Instant` | Filtrer par date de création |
| `updatedAt.*` | `Instant` | Filtrer par date de mise à jour |

**Réponse 200 OK**

```json
[
  {
    "id": 1,
    "title": "Epic Authentification",
    "description": "Gestion de l'authentification",
    "status": "IN_PROGRESS",
    "priority": "HIGH",
    "createdAt": "2026-08-10T12:00:00Z",
    "updatedAt": "2026-08-10T14:00:00Z",
    "startDate": "2026-08-01",
    "endDate": "2026-08-15",
    "project": { "id": 1, "name": "Mon Projet", "key": "MPROJ", ... }
  }
]
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 403 | `projectId` requis si pas d'accès global (`ADMIN`) |

---

### 5. Compter les epics

```http
GET /api/epics/count?projectId.equals=1
Authorization: Bearer <token>
```

**Rôle requis** : accès au projet ou `ROLE_ADMIN`

**Réponse 200 OK**

```json
3
```

---

### 6. Récupérer un epic

```http
GET /api/epics/{id}
Authorization: Bearer <token>
```

**Rôle requis** : accès au projet

**Réponse 200 OK**

```json
{
  "id": 1,
  "title": "Epic Authentification",
  "description": "Gestion de l'authentification",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "createdAt": "2026-08-10T12:00:00Z",
  "updatedAt": "2026-08-10T14:00:00Z",
  "startDate": "2026-08-01",
  "endDate": "2026-08-15",
  "project": { "id": 1, "name": "Mon Projet", "key": "MPROJ", ... }
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 404 | Epic non trouvé |
| 403 | Pas d'accès au projet |

---

### 7. Supprimer un epic

```http
DELETE /api/epics/{id}
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_USER` (doit être `OWNER`/`MANAGER` du projet)

**Réponse 204 No Content**
