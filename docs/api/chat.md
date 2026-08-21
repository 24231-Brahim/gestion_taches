# Chat

Base URL : `http://localhost:8080/api/projects/{projectId}/chat`

> Note : l'ensemble des endpoints chat nécessite que l'utilisateur soit membre du projet. Les conversations privées (`DIRECT`) ne sont accessibles qu'à leurs participants.

## Entités

### ConversationDTO

| Champ | Type | Description |
|-------|------|-------------|
| `id` | `Long` | Identifiant unique |
| `type` | `ConversationType` | `GENERAL` ou `DIRECT` |
| `name` | `String` | Nom optionnel (pour futurs canaux nommés, ignoré pour GENERAL/DIRECT) |
| `projectId` | `Long` | ID du projet |
| `createdAt` | `Instant` | Date de création |
| `lastMessageAt` | `Instant` | Horodatage du dernier message (pour le tri dans la sidebar) |
| `lastMessagePreview` | `String` | Extrait du dernier message |
| `unreadCount` | `long` | Nombre de messages non lus pour l'utilisateur courant |
| `participants` | `ChatMemberDTO[]` | Membres de la conversation (avec rôle) |

### ChatMessageDTO

| Champ | Type | Description |
|-------|------|-------------|
| `id` | `Long` | Identifiant unique |
| `content` | `String` | `@NotNull @Size(min=1, max=5000)` — contenu du message |
| `createdAt` | `Instant` | Date d'envoi |
| `editedAt` | `Instant` | Date de modification (si édition) |
| `deleted` | `Boolean` | `false` par défaut — suppression douce |
| `conversationId` | `Long` | ID de la conversation |
| `sender` | `UserDTO` | Expéditeur (`id`, `login`) |
| `parentMessageId` | `Long` | ID du message parent (threads — architecture, pas encore connecté à l'UI) |

### ChatMemberDTO

| Champ | Type | Description |
|-------|------|-------------|
| `userId` | `Long` | ID de l'utilisateur |
| `userLogin` | `String` | Login |
| `role` | `ProjectRole` | Rôle dans le projet |
| `joinedAt` | `Instant` | Date d'adhésion au projet |
| `lastReadAt` | `Instant` | Dernière lecture dans cette conversation (null hors contexte) |

### ConversationType

| Valeur | Description |
|--------|-------------|
| `GENERAL` | Canal général partagé avec tous les membres du projet |
| `DIRECT` | Conversation privée entre deux membres |

## Types de canaux

### GENERAL

- Un projet possède exact un canal `GENERAL` (créé automatiquement au premier accès).
- Visible par tous les membres du projet.

### DIRECT

- Conversation privée entre exactement deux membres.
- Créée via `POST /api/projects/{projectId}/chat/conversations/direct/{userId}`.
- Seuls les deux participants y ont accès.

## WebSocket / STOMP

Un endpoint WebSocket est configuré pour le temps réel :

- **Endpoint** : `/websocket/tracker` (avec fallback SockJS)
- **Broker** : simple broker sur `/queue` et `/topic`
- **Authentification** : header `Authorization: Bearer <token>` dans le message STOMP CONNECT.
- **Notifications** : poussées sur `/queue/notifications` via `convertAndSendToUser`.

> Note : le chat lui-même utilise **exclusivement des endpoints REST**. Le WebSocket `/websocket/tracker` est configuré pour les notifications temps réel mais aucun endpoint de chat temps réel n'est documenté dans le code. Les messages sont récupérés via `GET /messages` (pagination par `beforeId`/`limit`).

## Threads

- **Threads** : un message peut répondre à un autre via `parentMessageId`. C'est de l'architecture, pas encore connecté à l'UI.

## Endpoints

### 1. Lister mes conversations

```http
GET /api/projects/{projectId}/chat/conversations
Authorization: Bearer <token>
```

**Rôle requis** : authentifié (`isAuthenticated()`)

Les conversations sont triées : GENERAL en premier, puis les DIRECT par `lastMessageAt` décroissant.

**Réponse 200 OK**

```json
[
  {
    "id": 1,
    "type": "GENERAL",
    "name": null,
    "projectId": 1,
    "createdAt": "2026-08-01T10:00:00Z",
    "lastMessageAt": "2026-08-10T12:00:00Z",
    "lastMessagePreview": "Bonjour à tous !",
    "unreadCount": 2,
    "participants": []
  },
  {
    "id": 2,
    "type": "DIRECT",
    "name": null,
    "projectId": 1,
    "createdAt": "2026-08-02T10:00:00Z",
    "lastMessageAt": "2026-08-10T11:00:00Z",
    "lastMessagePreview": "Salut !",
    "unreadCount": 0,
    "participants": [
      {
        "userId": 5,
        "userLogin": "johndoe",
        "role": "MEMBER",
        "joinedAt": "2026-08-01T10:00:00Z",
        "lastReadAt": "2026-08-10T11:00:00Z"
      }
    ]
  }
]
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 403 | L'utilisateur n'est pas membre du projet |

---

### 2. Ouvrir / créer une conversation directe

```http
POST /api/projects/{projectId}/chat/conversations/direct/{userId}
Authorization: Bearer <token>
```

**Rôle requis** : authentifié (`isAuthenticated()`)

Si une conversation DIRECT existe déjà entre l'utilisateur courant et `{userId}`, elle est retournée. Sinon, elle est créée.

**Réponse 201 Created**

```json
{
  "id": 2,
  "type": "DIRECT",
  "name": null,
  "projectId": 1,
  "createdAt": "2026-08-02T10:00:00Z",
  "lastMessageAt": null,
  "lastMessagePreview": null,
  "unreadCount": 0,
  "participants": [
    {
      "userId": 5,
      "userLogin": "johndoe",
      "role": "MEMBER",
      "joinedAt": "2026-08-01T10:00:00Z",
      "lastReadAt": null
    }
  ]
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 403 | L'utilisateur courant ou `{userId}` n'est pas membre du projet |

---

### 3. Lister les messages d'une conversation

```http
GET /api/projects/{projectId}/chat/conversations/{conversationId}/messages?beforeId=50&limit=30
Authorization: Bearer <token>
```

**Rôle requis** : authentifié (`isAuthenticated()`)

**Paramètres de query**

| Paramètre | Type | Défaut | Description |
|-----------|------|--------|-------------|
| `beforeId` | `Long` | — | ID du dernier message connu (pour pagination descendante) |
| `limit` | `int` | `30` | Nombre maximum de messages retournés |

Les messages sont triés par `createdAt` croissant (du plus ancien au plus récent).

**Réponse 200 OK**

```json
[
  {
    "id": 1,
    "content": "Bonjour à tous !",
    "createdAt": "2026-08-01T10:00:00Z",
    "editedAt": null,
    "deleted": false,
    "conversationId": 1,
    "sender": {
      "id": 1,
      "login": "admin"
    },
    "parentMessageId": null
  }
]
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 403 | L'utilisateur n'est pas membre de la conversation |

---

### 4. Envoyer un message

```http
POST /api/projects/{projectId}/chat/conversations/{conversationId}/messages
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : authentifié (`isAuthenticated()`)

**Corps de requête**

```json
{
  "content": "Bonjour, quelqu'un peut m'aider ?"
}
```

**Réponse 201 Created**

```json
{
  "id": 3,
  "content": "Bonjour, quelqu'un peut m'aider ?",
  "createdAt": "2026-08-10T12:05:00Z",
  "editedAt": null,
  "deleted": false,
  "conversationId": 1,
  "sender": {
    "id": 5,
    "login": "johndoe"
  },
  "parentMessageId": null
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | Le message possède déjà un ID (`idexists`) |
| 403 | L'utilisateur n'est pas membre de la conversation |

---

### 5. Éditer un message

```http
PATCH /api/projects/{projectId}/chat/messages/{messageId}
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : authentifié (`isAuthenticated()`)

Seul l'expéditeur du message peut l'éditer.

**Corps de requête**

```json
{
  "content": "Bonjour, quelqu'un peut m'aider s'il vous plaît ?"
}
```

**Réponse 200 OK**

```json
{
  "id": 3,
  "content": "Bonjour, quelqu'un peut m'aider s'il vous plaît ?",
  "createdAt": "2026-08-10T12:05:00Z",
  "editedAt": "2026-08-10T12:06:00Z",
  "deleted": false,
  "conversationId": 1,
  "sender": {
    "id": 5,
    "login": "johndoe"
  },
  "parentMessageId": null
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 403 | L'utilisateur n'est pas l'expéditeur du message |

---

### 6. Supprimer un message

```http
DELETE /api/projects/{projectId}/chat/messages/{messageId}
Authorization: Bearer <token>
```

**Rôle requis** : authentifié (`isAuthenticated()`)

Suppression douce (`deleted = true`). Seul l'expéditeur peut supprimer son message.

**Réponse 200 OK** (pas de corps)

**Erreurs**

| Code | Signification |
|------|---------------|
| 403 | L'utilisateur n'est pas l'expéditeur du message |

---

### 7. Marquer une conversation comme lue

```http
POST /api/projects/{projectId}/chat/conversations/{conversationId}/read
Authorization: Bearer <token>
```

**Rôle requis** : authentifié (`isAuthenticated()`)

Met à jour `lastReadAt` du membre courant dans la conversation.

**Réponse 200 OK** (pas de corps)

**Erreurs**

| Code | Signification |
|------|---------------|
| 403 | L'utilisateur n'est pas membre de la conversation |

---

### 8. Lister les membres du projet (chat)

```http
GET /api/projects/{projectId}/chat/members
Authorization: Bearer <token>
```

**Rôle requis** : authentifié (`isAuthenticated()`)

Retourne les membres du projet avec leur rôle.

**Réponse 200 OK**

```json
[
  {
    "userId": 1,
    "userLogin": "admin",
    "role": "OWNER",
    "joinedAt": "2026-08-01T10:00:00Z",
    "lastReadAt": null
  },
  {
    "userId": 5,
    "userLogin": "johndoe",
    "role": "MEMBER",
    "joinedAt": "2026-08-02T10:00:00Z",
    "lastReadAt": "2026-08-10T11:00:00Z"
  }
]
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 403 | L'utilisateur n'est pas membre du projet |

---

### 9. Rechercher des messages

```http
GET /api/projects/{projectId}/chat/search?q=login&limit=30
Authorization: Bearer <token>
```

**Rôle requis** : authentifié (`isAuthenticated()`)

Recherche plein texte dans les conversations accessibles par l'utilisateur.

**Paramètres de query**

| Paramètre | Type | Défaut | Description |
|-----------|------|--------|-------------|
| `q` | `String` | — | Requête de recherche |
| `limit` | `int` | `30` | Nombre maximum de résultats |

**Réponse 200 OK**

```json
[
  {
    "id": 1,
    "content": "Bonjour @johndoe, peux-tu vérifier ?",
    "createdAt": "2026-08-01T10:00:00Z",
    "editedAt": null,
    "deleted": false,
    "conversationId": 1,
    "sender": { "id": 1, "login": "admin" },
    "parentMessageId": null
  }
]
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 403 | L'utilisateur n'est pas membre du projet |
