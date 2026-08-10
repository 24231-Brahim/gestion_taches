# Projects

Base URL : `http://localhost:8080/api/projects`

## Schéma ProjectDTO

| Champ | Type | Contraintes | Description |
|-------|------|-------------|-------------|
| `id` | `Long` | — | Identifiant unique |
| `name` | `String` | `@NotNull @Size(min=1, max=100)` | Nom du projet |
| `description` | `String` | `@Size(max=500)` | Description |
| `key` | `String` | `@NotNull @Size(min=2, max=10)` | Clé unique du projet |
| `createdAt` | `Instant` | `@NotNull` | Date de création |
| `ownerId` | `Long` | — | ID du propriétaire |
| `ownerLogin` | `String` | — | Login du propriétaire |
| `projectMembers` | `ProjectMemberDTO[]` | — | Membres du projet |

## Endpoints

### 1. Créer un projet

```http
POST /api/projects
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : `ROLE_ADMIN` ou `ROLE_PROJET_MANAGER`

**Corps de requête**

```json
{
  "name": "Mon Projet",
  "description": "Description du projet",
  "key": "MPROJ",
  "ownerId": 1
}
```

**Réponse 201 Created**

```json
{
  "id": 1,
  "name": "Mon Projet",
  "description": "Description du projet",
  "key": "MPROJ",
  "createdAt": "2026-08-10T12:00:00Z",
  "ownerId": 1,
  "ownerLogin": "admin",
  "projectMembers": []
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | Le projet possède déjà un ID (`idexists`) |
| 401 | Token manquant ou invalide |
| 403 | Rôle insuffisant |

---

### 2. Mettre à jour un projet

```http
PUT /api/projects/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : `ROLE_ADMIN` ou `ROLE_PROJET_MANAGER`

**Corps de requête**

```json
{
  "id": 1,
  "name": "Mon Projet modifié",
  "description": "Nouvelle description",
  "key": "MPROJ",
  "ownerId": 1
}
```

**Réponse 200 OK**

```json
{
  "id": 1,
  "name": "Mon Projet modifié",
  "description": "Nouvelle description",
  "key": "MPROJ",
  "createdAt": "2026-08-10T12:00:00Z",
  "ownerId": 1,
  "ownerLogin": "admin",
  "projectMembers": []
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | ID manquant dans le corps (`idnull`) ou ID chemin ≠ ID corps (`idinvalid`) |
| 404 | Projet non trouvé (`idnotfound`) |
| 401 | Token manquant ou invalide |
| 403 | Rôle insuffisant |

---

### 3. Mise à jour partielle

```http
PATCH /api/projects/{id}
Authorization: Bearer <token>
Content-Type: application/merge-patch+json
```

**Rôle requis** : `ROLE_ADMIN` ou `ROLE_PROJET_MANAGER`

Seuls les champs non-nuls du corps sont mis à jour.

**Corps de requête**

```json
{
  "name": "Nouveau nom uniquement"
}
```

**Réponse 200 OK**

```json
{
  "id": 1,
  "name": "Nouveau nom uniquement",
  "description": "Description du projet",
  "key": "MPROJ",
  "createdAt": "2026-08-10T12:00:00Z",
  "ownerId": 1,
  "ownerLogin": "admin",
  "projectMembers": []
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | ID manquant ou invalide |
| 404 | Projet non trouvé |
| 401 | Token manquant ou invalide |
| 403 | Rôle insuffisant |

---

### 4. Lister les projets

```http
GET /api/projects?page=0&size=20&sort=name,asc
Authorization: Bearer <token>
```

**Rôle requis** : aucun (authentification recommandée pour le scope `mine`)

**Paramètres de query**

| Paramètre | Type | Défaut | Description |
|-----------|------|--------|-------------|
| `page` | `int` | `0` | Numéro de page |
| `size` | `int` | `20` | Taille de page |
| `sort` | `String` | — | Tri, ex: `createdAt,desc` |
| `scope` | `String` | `all` | `all` (tous) ou `mine` (seulement mes projets) |

**Réponse 200 OK**

```json
[
  {
    "id": 1,
    "name": "Mon Projet",
    "description": "Description du projet",
    "key": "MPROJ",
    "createdAt": "2026-08-10T12:00:00Z",
    "ownerId": 1,
    "ownerLogin": "admin",
    "projectMembers": []
  }
]
```

Headers de pagination inclus : `X-Total-Count`, etc.

---

### 5. Statistiques des projets (cards)

```http
GET /api/projects/progress
Authorization: Bearer <token>
```

**Rôle requis** : authentifié (`isAuthenticated()`)

**Réponse 200 OK**

```json
[
  {
    "projectId": 1,
    "totalTasks": 10,
    "doneTasks": 4,
    "activeSprintId": 2,
    "activeSprintName": "Sprint 1"
  }
]
```

---

### 6. Compter les membres

```http
GET /api/projects/members/count
Authorization: Bearer <token>
```

**Rôle requis** : aucun

**Réponse 200 OK**

```json
42
```

---

### 7. Mes membreships

```http
GET /api/projects/my-roles
Authorization: Bearer <token>
```

**Rôle requis** : authentifié (`isAuthenticated()`)

**Réponse 200 OK**

```json
[
  {
    "id": 1,
    "projectId": 1,
    "projectName": "Mon Projet",
    "projectKey": "MPROJ",
    "userId": 5,
    "userLogin": "johndoe",
    "role": "MEMBER",
    "joinedAt": "2026-08-01T10:00:00Z"
  }
]
```

---

### 8. Récupérer un projet par ID

```http
GET /api/projects/{id}
Authorization: Bearer <token>
```

**Rôle requis** : aucun

**Réponse 200 OK**

```json
{
  "id": 1,
  "name": "Mon Projet",
  "description": "Description du projet",
  "key": "MPROJ",
  "createdAt": "2026-08-10T12:00:00Z",
  "ownerId": 1,
  "ownerLogin": "admin",
  "projectMembers": []
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 404 | Projet non trouvé |
| 401 | Token manquant ou invalide |

---

### 9. Récupérer un projet par clé

```http
GET /api/projects/by-key/{key}
Authorization: Bearer <token>
```

**Rôle requis** : aucun

**Réponse 200 OK**

```json
{
  "id": 1,
  "name": "Mon Projet",
  "description": "Description du projet",
  "key": "MPROJ",
  "createdAt": "2026-08-10T12:00:00Z",
  "ownerId": 1,
  "ownerLogin": "admin",
  "projectMembers": []
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 404 | Projet non trouvé |

---

### 10. Supprimer un projet

```http
DELETE /api/projects/{id}
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_USER`

> Note : bien que `ROLE_USER` soit autorisé, le service vérifie en réalité les rôles projet. La suppression est réservée aux `OWNER`/`MANAGER` du projet.

**Réponse 204 No Content**

**Erreurs**

| Code | Signification |
|------|---------------|
| 401 | Token manquant ou invalide |
| 403 | Rôle insuffisant pour supprimer ce projet |

---

### 11. Lister les membres d'un projet

```http
GET /api/projects/{id}/members
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER`, `ROLE_DEVELOPER` ou `ROLE_USER` (doit être membre du projet)

**Réponse 200 OK**

```json
[
  {
    "id": 1,
    "projectId": 1,
    "projectName": "Mon Projet",
    "projectKey": "MPROJ",
    "userId": 5,
    "userLogin": "johndoe",
    "role": "OWNER",
    "joinedAt": "2026-08-01T10:00:00Z"
  }
]
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 403 | L'utilisateur n'est pas membre du projet |

---

### 12. Ajouter un membre

```http
POST /api/projects/{id}/members/{userId}
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_USER`

**Réponse 200 OK** (pas de corps)

**Erreurs**

| Code | Signification |
|------|---------------|
| 403 | Rôle insuffisant ou utilisateur déjà membre |

---

### 13. Retirer un membre

```http
DELETE /api/projects/{id}/members/{userId}
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_USER`

**Réponse 204 No Content**

---

### 14. Mettre à jour le rôle d'un membre

```http
PATCH /api/projects/{id}/members/{userId}
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_USER`

**Corps de requête**

```json
{
  "role": "MANAGER"
}
```

**Réponse 200 OK** (pas de corps)
