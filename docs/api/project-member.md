# Project Members

Base URL : `http://localhost:8080/api/admin/project-members`

> Note : l'endpoint listant les project members est restreint aux administrateurs globaux. La gestion des membres (ajout, suppression, changement de rôle) se fait via les endpoints intégrés à `ProjectResource` (`/api/projects/{id}/members/...`).

## Schéma ProjectMemberDTO

| Champ | Type | Contraintes | Description |
|-------|------|-------------|-------------|
| `id` | `Long` | — | Identifiant unique |
| `projectId` | `Long` | — | ID du projet |
| `projectName` | `String` | — | Nom du projet |
| `projectKey` | `String` | — | Clé du projet |
| `userId` | `Long` | — | ID de l'utilisateur |
| `userLogin` | `String` | — | Login de l'utilisateur |
| `role` | `ProjectRole` | `@NotNull` | Rôle dans le projet |
| `joinedAt` | `Instant` | `@NotNull` | Date d'adhésion |

### ProjectRole

| Valeur | Description |
|--------|-------------|
| `OWNER` | Propriétaire |
| `MANAGER` | Gestionnaire |
| `MEMBER` | Membre |

## Endpoints

### 1. Lister tous les project members (admin)

```http
GET /api/admin/project-members?page=0&size=20&sort=joinedAt,desc
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN`

**Paramètres de query**

| Paramètre | Type | Description |
|-----------|------|-------------|
| `page` | `int` | Numéro de page |
| `size` | `int` | Taille de page |
| `sort` | `String` | Tri |

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
  },
  {
    "id": 2,
    "projectId": 1,
    "projectName": "Mon Projet",
    "projectKey": "MPROJ",
    "userId": 6,
    "userLogin": "alice",
    "role": "MEMBER",
    "joinedAt": "2026-08-02T10:00:00Z"
  }
]
```

Headers de pagination inclus.

**Erreurs**

| Code | Signification |
|------|---------------|
| 403 | Rôle insuffisant (pas `ROLE_ADMIN`) |
