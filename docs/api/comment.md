# Comments

Base URL : `http://localhost:8080/api/comments`

## Schéma CommentDTO

| Champ | Type | Contraintes | Description |
|-------|------|-------------|-------------|
| `id` | `Long` | — | Identifiant unique |
| `content` | `String` | `@NotNull @Size(min=1, max=2000)` | Contenu du commentaire |
| `createdAt` | `Instant` | — | Date de création (défini serveur) |
| `task` | `TaskDTO` | `@NotNull` | Tâche associée (au moins `id` requis) |
| `author` | `UserDTO` | — | Auteur (`id`, `login`) |

## Endpoints

### 1. Créer un commentaire

```http
POST /api/comments
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER`, `ROLE_DEVELOPER` ou `ROLE_USER`

**Corps de requête**

```json
{
  "content": "Cette tâche est bloquée par la dépendance X.",
  "task": {
    "id": 1
  }
}
```

**Réponse 201 Created**

```json
{
  "id": 1,
  "content": "Cette tâche est bloquée par la dépendance X.",
  "createdAt": "2026-08-10T12:30:00Z",
  "task": {
    "id": 1,
    "title": "Implémenter login JWT",
    "status": "DONE",
    "priority": "HIGH",
    "project": { "id": 1, "name": "Mon Projet", "key": "MPROJ" }
  },
  "author": {
    "id": 5,
    "login": "johndoe"
  }
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | Le commentaire possède déjà un ID (`idexists`) |

---

### 2. Mettre à jour un commentaire

```http
PUT /api/comments/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_DEVELOPER`

**Corps de requête**

```json
{
  "id": 1,
  "content": "Message corrigé.",
  "task": {
    "id": 1
  }
}
```

**Réponse 200 OK**

```json
{
  "id": 1,
  "content": "Message corrigé.",
  "createdAt": "2026-08-10T12:30:00Z",
  "task": { "id": 1, "title": "Implémenter login JWT", ... },
  "author": { "id": 5, "login": "johndoe" }
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | ID manquant ou invalide |
| 404 | Commentaire non trouvé (`idnotfound`) |
| 403 | Rôle insuffisant |

---

### 3. Mise à jour partielle

```http
PATCH /api/comments/{id}
Authorization: Bearer <token>
Content-Type: application/merge-patch+json
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_DEVELOPER`

**Corps de requête**

```json
{
  "content": "Nouveau contenu partiel"
}
```

**Réponse 200 OK**

Mêmes champs que `CommentDTO`.

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | ID manquant ou invalide |
| 404 | Commentaire non trouvé |
| 403 | Rôle insuffisant |

---

### 4. Lister les commentaires d'une tâche

```http
GET /api/comments/by-task/{taskId}
Authorization: Bearer <token>
```

**Rôle requis** : aucun

**Réponse 200 OK**

```json
[
  {
    "id": 1,
    "content": "Cette tâche est bloquée.",
    "createdAt": "2026-08-10T12:30:00Z",
    "task": { "id": 1, "title": "Implémenter login JWT" },
    "author": { "id": 5, "login": "johndoe" }
  }
]
```

---

### 5. Lister tous les commentaires (paginé)

```http
GET /api/comments?page=0&size=20&sort=createdAt,desc
Authorization: Bearer <token>
```

**Rôle requis** : aucun

**Réponse 200 OK**

```json
[
  {
    "id": 1,
    "content": "Commentaire global",
    "createdAt": "2026-08-10T12:30:00Z",
    "task": { "id": 1, "title": "Implémenter login JWT" },
    "author": { "id": 5, "login": "johndoe" }
  }
]
```

Headers de pagination inclus.

---

### 6. Récupérer un commentaire

```http
GET /api/comments/{id}
Authorization: Bearer <token>
```

**Rôle requis** : aucun

**Réponse 200 OK**

```json
{
  "id": 1,
  "content": "Cette tâche est bloquée.",
  "createdAt": "2026-08-10T12:30:00Z",
  "task": { "id": 1, "title": "Implémenter login JWT" },
  "author": { "id": 5, "login": "johndoe" }
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 404 | Commentaire non trouvé |

---

### 7. Supprimer un commentaire

```http
DELETE /api/comments/{id}
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_DEVELOPER`

**Réponse 204 No Content**
