# Task Histories

Base URL : `http://localhost:8080/api/task-histories`

## Schéma TaskHistoryDTO

| Champ | Type | Contraintes | Description |
|-------|------|-------------|-------------|
| `id` | `Long` | — | Identifiant unique |
| `action` | `String` | `@NotNull @Size(max=100)` | Action effectuée |
| `oldValue` | `String` | `@Size(max=500)` | Ancienne valeur |
| `newValue` | `String` | `@Size(max=500)` | Nouvelle valeur |
| `createdAt` | `Instant` | `@NotNull` | Date de l'action |
| `task` | `TaskDTO` | `@NotNull` | Tâche concernée |
| `user` | `UserDTO` | — | Utilisateur ayant effectué l'action (`id`, `login`) |

## Endpoints

### 1. Créer un historique

```http
POST /api/task-histories
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_DEVELOPER`

**Corps de requête**

```json
{
  "action": "STATUS_CHANGED",
  "oldValue": "NEW",
  "newValue": "IN_PROGRESS",
  "task": {
    "id": 1
  }
}
```

**Réponse 201 Created**

```json
{
  "id": 1,
  "action": "STATUS_CHANGED",
  "oldValue": "NEW",
  "newValue": "IN_PROGRESS",
  "createdAt": "2026-08-10T14:00:00Z",
  "task": {
    "id": 1,
    "title": "Implémenter login JWT",
    "status": "IN_PROGRESS",
    "priority": "HIGH",
    "project": { "id": 1, "name": "Mon Projet", "key": "MPROJ" }
  },
  "user": {
    "id": 5,
    "login": "johndoe"
  }
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | L'historique possède déjà un ID (`idexists`) |
| 403 | Rôle insuffisant |

---

### 2. Mettre à jour un historique

```http
PUT /api/task-histories/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_DEVELOPER`

**Corps de requête**

```json
{
  "id": 1,
  "action": "STATUS_CHANGED",
  "oldValue": "NEW",
  "newValue": "DONE",
  "task": {
    "id": 1
  }
}
```

**Réponse 200 OK**

```json
{
  "id": 1,
  "action": "STATUS_CHANGED",
  "oldValue": "NEW",
  "newValue": "DONE",
  "createdAt": "2026-08-10T14:00:00Z",
  "task": { "id": 1, "title": "Implémenter login JWT" },
  "user": { "id": 5, "login": "johndoe" }
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | ID manquant ou invalide |
| 404 | Historique non trouvé (`idnotfound`) |
| 403 | Rôle insuffisant |

---

### 3. Mise à jour partielle

```http
PATCH /api/task-histories/{id}
Authorization: Bearer <token>
Content-Type: application/merge-patch+json
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_DEVELOPER`

**Corps de requête**

```json
{
  "newValue": "CANCELLED"
}
```

**Réponse 200 OK**

Mêmes champs que `TaskHistoryDTO`.

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | ID manquant ou invalide |
| 404 | Historique non trouvé |
| 403 | Rôle insuffisant |

---

### 4. Mes historiques récents

```http
GET /api/task-histories/mine
Authorization: Bearer <token>
```

**Rôle requis** : authentifié (`isAuthenticated()`)

Retourne les 10 historiques les plus récents de l'utilisateur connecté (pour le dashboard développeur).

**Réponse 200 OK**

```json
[
  {
    "id": 1,
    "action": "STATUS_CHANGED",
    "oldValue": "NEW",
    "newValue": "DONE",
    "createdAt": "2026-08-10T14:00:00Z",
    "task": { "id": 1, "title": "Implémenter login JWT" },
    "user": { "id": 5, "login": "johndoe" }
  }
]
```

---

### 5. Historiques d'une tâche

```http
GET /api/task-histories/by-task/{taskId}
Authorization: Bearer <token>
```

**Rôle requis** : aucun

**Réponse 200 OK**

```json
[
  {
    "id": 1,
    "action": "STATUS_CHANGED",
    "oldValue": "NEW",
    "newValue": "IN_PROGRESS",
    "createdAt": "2026-08-10T14:00:00Z",
    "task": { "id": 1, "title": "Implémenter login JWT" },
    "user": { "id": 5, "login": "johndoe" }
  }
]
```

---

### 6. Lister tous les historiques (paginé)

```http
GET /api/task-histories?page=0&size=20&sort=createdAt,desc
Authorization: Bearer <token>
```

**Rôle requis** : aucun

**Réponse 200 OK**

```json
[
  {
    "id": 1,
    "action": "STATUS_CHANGED",
    "oldValue": "NEW",
    "newValue": "IN_PROGRESS",
    "createdAt": "2026-08-10T14:00:00Z",
    "task": { "id": 1, "title": "Implémenter login JWT" },
    "user": { "id": 5, "login": "johndoe" }
  }
]
```

Headers de pagination inclus.

---

### 7. Récupérer un historique

```http
GET /api/task-histories/{id}
Authorization: Bearer <token>
```

**Rôle requis** : aucun

**Réponse 200 OK**

```json
{
  "id": 1,
  "action": "STATUS_CHANGED",
  "oldValue": "NEW",
  "newValue": "IN_PROGRESS",
  "createdAt": "2026-08-10T14:00:00Z",
  "task": { "id": 1, "title": "Implémenter login JWT" },
  "user": { "id": 5, "login": "johndoe" }
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 404 | Historique non trouvé |

---

### 8. Supprimer un historique

```http
DELETE /api/task-histories/{id}
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_DEVELOPER`

**Réponse 204 No Content**
