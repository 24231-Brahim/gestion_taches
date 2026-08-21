# Gestion Tâches — Documentation API

## Introduction

Cette documentation couvre l'API REST du projet **Gestion Tâches** (JHipster / Spring Boot 4.0.6 / Angular 21 / PostgreSQL).

- **Base URL** : `http://localhost:8080/api`
- **Format** : JSON (`application/json`) sauf mention contraire (upload `multipart/form-data`, SSE `text/event-stream`).
- **Authentification** : JWT Bearer token.
- **Profiles** : le profile `dev` active l'API docs (`springdoc-openapi`).

## Authentification JWT

### Obtenir un token

```http
POST /api/authenticate
Content-Type: application/json

{
  "username": "admin",
  "password": "admin",
  "rememberMe": false
}
```

**Réponse 200**
```json
{
  "id_token": "eyJhbGciOiJSUzI1NiJ9..."
}
```

Le token est également retourné dans le header `Authorization: Bearer <token>`.

### Vérifier l'authentification

```http
GET /api/authenticate
```

- `204 No Content` : l'utilisateur est authentifié.
- `401 Unauthorized` : non authentifié.

### Utiliser le token

Toutes les requêtes authentifiées doivent inclure :

```http
Authorization: Bearer eyJhbGciOiJSUzI1NiJ9...
```

**Durée de validité** (dev) :

| Type | Durée |
|------|-------|
| Token standard | 24 heures (`86400` s) |
| Remember-me | 30 jours (`2592000` s) |

**Algorithme et claims** :

- Algorithme de signature : **HS512** (`MacAlgorithm.HS512`, secret HMAC).
- Claims : `sub` (login), `auth` (autorités séparées par des espaces), `userId` (ID de l'utilisateur), `iat`/`exp`.

### Rôles / Autorités

| Rôle | Description |
|------|-------------|
| `ROLE_ADMIN` | Accès global, administration |
| `ROLE_PROJET_MANAGER` | Gestion de projets |
| `ROLE_DEVELOPER` | Développeur |
| `ROLE_USER` | Utilisateur basique |

Au niveau projet, les membres se voient attribuer un `ProjectRole` :

| ProjectRole | Description |
|-------------|-------------|
| `OWNER` | Propriétaire du projet |
| `MANAGER` | Gestionnaire |
| `MEMBER` | Membre (rôle par défaut des développeurs) |

## Pagination

Les endpoints de liste acceptent les paramètres standards JHipster :

| Paramètre | Description |
|-----------|-------------|
| `page` | Numéro de page (0-indexed) |
| `size` | Taille de page |
| `sort` | Tri, ex: `createdAt,desc` |

Les réponses paginées incluent les headers :

- `X-Total-Count` : nombre total d'éléments
- `X-Page`, `X-Size`, etc. (via `PaginationUtil`)

## Filtrage par critères

Les endpoints `GET` de Sprint, Epic et Task acceptent des filtres dynamiques via query params :

```
GET /api/tasks?status.equals=NEW&projectId.equals=1&assigneeId.equals=5
```

Opérateurs disponibles (standard JHipster) :

| Suffixe | Signification |
|---------|---------------|
| `.equals` | Égalité |
| `.notEquals` | Inégalité |
| `.in` | Dans une liste (séparée par `,`) |
| `.notIn` | Pas dans une liste |
| `.contains` | Contient (texte) |
| `.doesNotContain` | Ne contient pas |
| `.startsWith` | Commence par |
| `.doesNotStartWith` | Ne commence pas par |
| `.endsWith` | Finit par |
| `.doesNotEndWith` | Ne finit pas par |
| `.greaterThan` | Supérieur |
| `.greaterThanOrEqual` | Supérieur ou égal |
| `.lessThan` | Inférieur |
| `.lessThanOrEqual` | Inférieur ou égal |
| `.specified` | Présent / non présent (`true`/`false`) |

## Swagger UI

Le profile `api-docs` active `springdoc-openapi`. En développement, ce profile est inclus dans le profile `dev`.

L'interface Swagger est accessible à l'URL standard de springdoc-openapi 3.x :

- **Swagger UI** : `/swagger-ui/index.html` (autorisé sans authentification via `SecurityConfiguration`)
- **OpenAPI JSON** : `/v3/api-docs` (réservé à `ROLE_ADMIN`)

> Note : l'activation effective dépend du profile Spring actif au lancement de l'application.

## SSE (Server-Sent Events)

Deux flux SSE sont disponibles :

- `GET /api/notifications/stream` — Notifications en temps réel pour l'utilisateur connecté.
- `GET /api/events/stream` — Événements de changement d'entité (projet, sprint, epic).

## WebSocket / STOMP

Un endpoint WebSocket est configuré pour les notifications temps réel :

- **Endpoint STOMP** : `ws://localhost:8080/websocket/tracker` (avec fallback SockJS)
- **Broker** : simple broker sur `/queue` et `/topic`
- **Authentification** : le token JWT est passé dans le header `Authorization: Bearer <token>` lors de la connexion STOMP CONNECT.
- **Destinations utilisateur** : `/queue/notifications` (envoyé via `convertAndSendToUser`)

> Note : le chat lui-même utilise exclusivement des endpoints REST. Le WebSocket `/websocket/tracker` est configuré mais aucun endpoint de chat temps réel n'est documenté dans le code.

## Fichiers de documentation

### Ressources métier

- [README.md](./README.md) — Sommaire général (ce fichier)
- [project.md](./project.md) — Projects
- [sprint.md](./sprint.md) — Sprints
- [epic.md](./epic.md) — Epics
- [task.md](./task.md) — Tasks
- [comment.md](./comment.md) — Comments
- [attachment.md](./attachment.md) — Attachments
- [notification.md](./notification.md) — Notifications
- [project-member.md](./project-member.md) — Project Members
- [chat.md](./chat.md) — Chat (Conversations, Messages)

### Comptes, utilisateurs et autorités

- [account.md](./account.md) — Inscription, activation, compte, mots de passe
- [users.md](./users.md) — Administration des utilisateurs et des autorités

### Statistiques, recherche et export

- [dashboard.md](./dashboard.md) — KPIs dashboard, statistiques développeur et admin
- [recherche-export.md](./recherche-export.md) — Recherche globale, événements SSE, export CSV
