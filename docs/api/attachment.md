# Attachments

Base URL : `http://localhost:8080/api/attachments`

## Schéma AttachmentDTO

| Champ | Type | Contraintes | Description |
|-------|------|-------------|-------------|
| `id` | `Long` | — | Identifiant unique |
| `fileName` | `String` | `@NotNull @Size(min=1, max=255)` | Nom original du fichier |
| `filePath` | `String` | `@NotNull @Size(max=1000)` | Chemin de stockage sur le serveur |
| `uploadedAt` | `Instant` | `@NotNull` | Date d'upload |
| `task` | `TaskDTO` | `@NotNull` | Tâche associée (au moins `id` requis) |
| `uploadedBy` | `UserDTO` | — | Utilisateur ayant uploadé (`id`, `login`) |

> Note : le fichier est stocké sur le disque dans le répertoire configuré par `app.upload.dir` (défaut : `uploads`). Le nom de fichier stocké est préfixé par un UUID pour éviter les collisions.

## Endpoints

### 1. Uploader un fichier

```http
POST /api/attachments/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_DEVELOPER`

**Paramètres multipart**

| Paramètre | Type | Description |
|-----------|------|-------------|
| `file` | `file` | Fichier à uploader |
| `taskId` | `Long` | ID de la tâche associée |

**Réponse 201 Created**

```json
{
  "id": 1,
  "fileName": "document.pdf",
  "filePath": "550e8400-e29b-41d4-a716-446655440000_document.pdf",
  "uploadedAt": "2026-08-10T13:00:00Z",
  "task": {
    "id": 1,
    "title": "Implémenter login JWT",
    "status": "DONE",
    "priority": "HIGH",
    "project": { "id": 1, "name": "Mon Projet", "key": "MPROJ" }
  },
  "uploadedBy": {
    "id": 5,
    "login": "johndoe"
  }
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | Tâche non trouvée, nom de fichier manquant, ou fichier vide |

---

### 2. Télécharger un fichier

```http
GET /api/attachments/download/{id}
Authorization: Bearer <token>
```

**Rôle requis** : aucun

**Réponse 200 OK**

- `Content-Type` : `application/octet-stream`
- `Content-Disposition` : `attachment; filename="document.pdf"`

**Erreurs**

| Code | Signification |
|------|---------------|
| 404 | Attachment non trouvé ou fichier absent du disque (`filenotfound`) |

---

### 3. Créer un attachment (JSON)

```http
POST /api/attachments
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_DEVELOPER`

Permet de créer une référence d'attachment sans upload physique (utile pour des liens externes).

**Corps de requête**

```json
{
  "fileName": "document.pdf",
  "filePath": "chemin/vers/fichier.pdf",
  "task": {
    "id": 1
  }
}
```

**Réponse 201 Created**

```json
{
  "id": 1,
  "fileName": "document.pdf",
  "filePath": "chemin/vers/fichier.pdf",
  "uploadedAt": "2026-08-10T13:00:00Z",
  "task": { "id": 1, "title": "Implémenter login JWT" },
  "uploadedBy": { "id": 5, "login": "johndoe" }
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | L'attachment possède déjà un ID (`idexists`) |

---

### 4. Mettre à jour un attachment

```http
PUT /api/attachments/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_DEVELOPER`

**Corps de requête**

```json
{
  "id": 1,
  "fileName": "document_v2.pdf",
  "filePath": "chemin/vers/fichier_v2.pdf",
  "task": {
    "id": 1
  }
}
```

**Réponse 200 OK**

```json
{
  "id": 1,
  "fileName": "document_v2.pdf",
  "filePath": "chemin/vers/fichier_v2.pdf",
  "uploadedAt": "2026-08-10T13:00:00Z",
  "task": { "id": 1, "title": "Implémenter login JWT" },
  "uploadedBy": { "id": 5, "login": "johndoe" }
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | ID manquant ou invalide |
| 404 | Attachment non trouvé (`idnotfound`) |
| 403 | Rôle insuffisant |

---

### 5. Mise à jour partielle

```http
PATCH /api/attachments/{id}
Authorization: Bearer <token>
Content-Type: application/merge-patch+json
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_DEVELOPER`

**Corps de requête**

```json
{
  "fileName": "nouveau_nom.pdf"
}
```

**Réponse 200 OK**

Mêmes champs que `AttachmentDTO`.

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | ID manquant ou invalide |
| 404 | Attachment non trouvé |
| 403 | Rôle insuffisant |

---

### 6. Lister les attachments d'une tâche

```http
GET /api/attachments/by-task/{taskId}
Authorization: Bearer <token>
```

**Rôle requis** : aucun

**Réponse 200 OK**

```json
[
  {
    "id": 1,
    "fileName": "document.pdf",
    "filePath": "550e8400-e29b-41d4-a716-446655440000_document.pdf",
    "uploadedAt": "2026-08-10T13:00:00Z",
    "task": { "id": 1, "title": "Implémenter login JWT" },
    "uploadedBy": { "id": 5, "login": "johndoe" }
  }
]
```

---

### 7. Lister tous les attachments (paginé)

```http
GET /api/attachments?page=0&size=20&sort=uploadedAt,desc
Authorization: Bearer <token>
```

**Rôle requis** : aucun

**Réponse 200 OK**

```json
[
  {
    "id": 1,
    "fileName": "document.pdf",
    "filePath": "550e8400-e29b-41d4-a716-446655440000_document.pdf",
    "uploadedAt": "2026-08-10T13:00:00Z",
    "task": { "id": 1, "title": "Implémenter login JWT" },
    "uploadedBy": { "id": 5, "login": "johndoe" }
  }
]
```

Headers de pagination inclus.

---

### 8. Récupérer un attachment

```http
GET /api/attachments/{id}
Authorization: Bearer <token>
```

**Rôle requis** : aucun

**Réponse 200 OK**

```json
{
  "id": 1,
  "fileName": "document.pdf",
  "filePath": "550e8400-e29b-41d4-a716-446655440000_document.pdf",
  "uploadedAt": "2026-08-10T13:00:00Z",
  "task": { "id": 1, "title": "Implémenter login JWT" },
  "uploadedBy": { "id": 5, "login": "johndoe" }
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 404 | Attachment non trouvé |

---

### 9. Supprimer un attachment

```http
DELETE /api/attachments/{id}
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_DEVELOPER`

**Réponse 204 No Content**
