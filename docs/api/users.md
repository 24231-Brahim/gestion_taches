# Utilisateurs & Autorités

Endpoints d'administration des utilisateurs et des rôles globaux.

## Vue d'ensemble

| Endpoint | Description | Rôle requis |
|----------|-------------|-------------|
| `GET /api/users` | Liste des utilisateurs (info publiques) | Authentifié |
| `GET /api/users/assignable` | Utilisateurs assignables à une tâche | Authentifié |
| `POST /api/admin/users` | Créer un utilisateur | `ROLE_ADMIN` |
| `PUT /api/admin/users` ou `PUT /api/admin/users/{login}` | Mettre à jour un utilisateur | `ROLE_ADMIN` |
| `GET /api/admin/users` | Lister les utilisateurs (détail complet) | `ROLE_ADMIN` |
| `GET /api/admin/users/{login}` | Récupérer un utilisateur | `ROLE_ADMIN` |
| `GET /api/admin/users/{login}/detail` | Vue détaillée admin d'un utilisateur | `ROLE_ADMIN` |
| `DELETE /api/admin/users/{login}` | Supprimer un utilisateur | `ROLE_ADMIN` |
| `POST /api/authorities` | Créer une autorité | `ROLE_ADMIN` |
| `GET /api/authorities` | Lister les autorités | `ROLE_ADMIN` |
| `GET /api/authorities/{id}` | Récupérer une autorité | `ROLE_ADMIN` |
| `PUT /api/authorities/{id}` | Mettre à jour une autorité | `ROLE_ADMIN` |
| `DELETE /api/authorities/{id}` | Supprimer une autorité | `ROLE_ADMIN` |

Le schéma `AdminUserDTO` est décrit dans [account.md](./account.md#schéma-adminuserdto).

> Note : bien que le contrôleur public le documente comme « autorisé pour tous », la configuration de sécurité impose l'authentification sur tout `/api/**`. Les endpoints `/api/users*` exigent donc un token.

---

## Utilisateurs publics

### 1. Lister les utilisateurs

```http
GET /api/users?page=0&size=20&sort=login,asc
Authorization: Bearer <token>
```

**Rôle requis** : authentifié.

**Paramètres de query**

| Paramètre | Type | Description |
|-----------|------|-------------|
| `page` | `int` | Numéro de page |
| `size` | `int` | Taille de page |
| `sort` | `String` | Tri. Propriétés autorisées : `id`, `login`, `firstName`, `lastName`, `email`, `activated`, `langKey` |

**Réponse 200 OK** — liste de `UserDTO` (`id`, `login` ; champs complets inclus selon l'implémentation).

Headers de pagination inclus (`X-Total-Count`, ...).

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | Propriété de tri non autorisée |
| 401 | Token manquant ou invalide |

### 2. Lister les utilisateurs assignables

```http
GET /api/users/assignable
Authorization: Bearer <token>
```

**Rôle requis** : authentifié.

Retourne les utilisateurs possédant le rôle `ROLE_DEVELOPER` ou `ROLE_PROJET_MANAGER` (ceux qui peuvent être assignés à une tâche via `PATCH /api/tasks/{id}/assign`).

**Réponse 200 OK** — liste de `UserDTO`.

```json
[
  { "id": 5, "login": "johndoe" },
  { "id": 6, "login": "alice" }
]
```

---

## Administration des utilisateurs (`/api/admin/users`)

### 3. Créer un utilisateur

```http
POST /api/admin/users
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : `ROLE_ADMIN`.

Le login et l'email ne doivent pas déjà être utilisés. Un email de création est envoyé à l'utilisateur.

**Corps de requête** (`AdminUserDTO` — sans `id`)

```json
{
  "login": "johndoe",
  "firstName": "John",
  "lastName": "Doe",
  "email": "johndoe@example.com",
  "activated": true,
  "langKey": "en",
  "authorities": ["ROLE_USER", "ROLE_DEVELOPER"]
}
```

**Réponse 201 Created** — `AdminUserDTO` complet.

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | `id` présent (`idexists`), login déjà utilisé (`userexists`) ou email déjà utilisé (`emailexists`) |
| 401 | Token manquant ou invalide |
| 403 | Rôle insuffisant |

### 4. Mettre à jour un utilisateur

```http
PUT /api/admin/users
# ou
PUT /api/admin/users/{login}
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : `ROLE_ADMIN`.

**Corps de requête** (`AdminUserDTO` — l'`id` est requis)

```json
{
  "id": 5,
  "login": "johndoe",
  "firstName": "Johnny",
  "lastName": "Doe",
  "email": "johnny.doe@example.com",
  "activated": true,
  "langKey": "fr",
  "authorities": ["ROLE_USER", "ROLE_DEVELOPER"]
}
```

**Réponse 200 OK** — `AdminUserDTO` mis à jour.

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | Email ou login déjà utilisé par un **autre** utilisateur (`emailexists` / `userexists`) |
| 404 | Utilisateur non trouvé |
| 401/403 | Non authentifié / pas `ROLE_ADMIN` |

### 5. Lister les utilisateurs (admin)

```http
GET /api/admin/users?page=0&size=20&sort=createdDate,desc
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN`.

**Paramètres de query**

| Paramètre | Type | Description |
|-----------|------|-------------|
| `page` | `int` | Numéro de page |
| `size` | `int` | Taille de page |
| `sort` | `String` | Tri. Propriétés autorisées : `id`, `login`, `firstName`, `lastName`, `email`, `activated`, `langKey`, `createdBy`, `createdDate`, `lastModifiedBy`, `lastModifiedDate` |

**Réponse 200 OK** — liste paginée de `AdminUserDTO`.

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | Propriété de tri non autorisée |
| 403 | Rôle insuffisant |

### 6. Récupérer un utilisateur

```http
GET /api/admin/users/{login}
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN`.

**Réponse 200 OK** — `AdminUserDTO` complet.

**Erreurs**

| Code | Signification |
|------|---------------|
| 404 | Utilisateur non trouvé |
| 403 | Rôle insuffisant |

### 7. Vue détaillée d'un utilisateur

```http
GET /api/admin/users/{login}/detail
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN`.

Retourne l'utilisateur avec ses tâches assignées, ses adhésions aux projets et son activité récente (20 dernières entrées d'historique).

**Réponse 200 OK** (`UserAdminDetailDTO`)

| Champ | Type | Description |
|-------|------|-------------|
| `id`, `login`, `firstName`, `lastName`, `email` | — | Informations de base |
| `activated` | `boolean` | Compte activé |
| `langKey` | `String` | Code langue |
| `createdBy` | `String` | Créé par |
| `createdDate` | `Instant` | Date de création |
| `authorities` | `Set<String>` | Rôles globaux |
| `tasks` | `TaskSummaryDTO[]` | Tâches assignées (titre, statut, priorité, projet, sprint, epic) |
| `projects` | `ProjectMembershipDTO[]` | Adhésions projet (projet, `ProjectRole`, `joinedAt`) |
| `recentActivity` | `TaskHistoryEntryDTO[]` | 20 dernières actions (action, ancienne/nouvelle valeur, date, tâche) |

**Erreurs**

| Code | Signification |
|------|---------------|
| 404 | Utilisateur non trouvé |
| 403 | Rôle insuffisant |

### 8. Supprimer un utilisateur

```http
DELETE /api/admin/users/{login}
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN`.

**Réponse 204 No Content**

**Erreurs**

| Code | Signification |
|------|---------------|
| 403 | Rôle insuffisant |
| 500 | Utilisateur non trouvé (tentative de suppression) |

---

## Autorités (`/api/authorities`)

L'entité `Authority` se réduit à son nom (`@NotNull @Size(max=50)`, utilisé comme identifiant). Les autorités suivantes sont **protégées** — impossibles à modifier ou supprimer : `ROLE_ADMIN`, `ROLE_USER`, `ROLE_DEVELOPER`, `ROLE_PROJET_MANAGER`.

Tous les endpoints d'autorités exigent `ROLE_ADMIN`.

### 9. Créer une autorité

```http
POST /api/authorities
Authorization: Bearer <token>
Content-Type: application/json
```

**Corps de requête**

```json
{
  "name": "ROLE_SUPERVISEUR"
}
```

**Réponse 201 Created** — l'autorité créée.

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | Autorité déjà existante (`idexists`) |
| 403 | Rôle insuffisant |

### 10. Lister les autorités

```http
GET /api/authorities
Authorization: Bearer <token>
```

**Réponse 200 OK**

```json
[
  { "name": "ROLE_ADMIN" },
  { "name": "ROLE_USER" }
]
```

### 11. Récupérer une autorité

```http
GET /api/authorities/{name}
Authorization: Bearer <token>
```

**Réponse 200 OK** — l'autorité.

**Erreurs** : 404 si introuvable.

### 12. Mettre à jour une autorité

```http
PUT /api/authorities/{name}
Authorization: Bearer <token>
Content-Type: application/json
```

**Corps de requête** : autorité dont le `name` doit correspondre à celui du chemin.

**Réponse 200 OK** — l'autorité mise à jour.

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | `name` du corps ≠ `name` du chemin (`namemismatch`) ou autorité protégée (`protectedauthority`) |
| 403 | Rôle insuffisant |

### 13. Supprimer une autorité

```http
DELETE /api/authorities/{name}
Authorization: Bearer <token>
```

**Réponse 204 No Content**

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | Autorité protégée (`protectedauthority`) |
| 403 | Rôle insuffisant |
