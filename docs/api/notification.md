# Notifications

Base URL : `http://localhost:8080/api/notifications`

## Schéma NotificationDTO

| Champ | Type | Contraintes | Description |
|-------|------|-------------|-------------|
| `id` | `Long` | — | Identifiant unique |
| `message` | `String` | `@NotNull` | Message de la notification |
| `task` | `TaskDTO` | — | Tâche associée |
| `taskTitle` | `String` | — | Titre de la tâche associée (dénormalisé) |
| `user` | `UserDTO` | — | Utilisateur destinataire (`id`, `login`) |
| `projectKey` | `String` | — | Clé du projet associé |
| `relatedUserId` | `Long` | — | ID de l'utilisateur lié |
| `relatedUserLogin` | `String` | — | Login de l'utilisateur lié |
| `isRead` | `Boolean` | `@NotNull` | Indicateur de lecture |
| `createdAt` | `Instant` | `@NotNull` | Date de création |

## Événements déclenchant des notifications

| Événement | Destinataire(s) |
|-----------|-----------------|
| Création de sprint | Tous les membres du projet (sauf l'initiateur) |
| Démarrage de sprint | Tous les membres du projet (sauf l'initiateur) |
| Clôture de sprint (tâches non DONE déplacées) | Admins |
| Assignation de tâche | Utilisateur assigné |
| Changement de statut de tâche | Assigné (s'il n'est pas l'initiateur) |
| Passage à DONE (créateur/admin) | Créateur si `ROLE_ADMIN` |
| Passage à DONE (assigné/admin) | Assigné si `ROLE_ADMIN` et ≠ créateur |
| Nouvel utilisateur inscrit | Tous les admins |
| Tâche en retard (> 14 jours sans DONE) | Assigné + admins (quotidien à 8h) |

## Technologies temps réel

- **STOMP** : les notifications sont poussées via `/queue/notifications` (SimpMessageSendingOperations).
- **SSE** : endpoint `GET /api/notifications/stream` pour recevoir les notifications en continu.

## Endpoints

### 1. Lister mes notifications

```http
GET /api/notifications?page=0&size=20&sort=createdAt,desc
Authorization: Bearer <token>
```

**Rôle requis** : authentifié (`isAuthenticated()`)

Retourne les notifications de l'utilisateur connecté, triées par `createdAt` décroissant. Par défaut, limité à 20 éléments si non paginé.

**Réponse 200 OK**

```json
[
  {
    "id": 1,
    "message": "Vous avez été assigné à la tâche \"Implémenter login JWT\"",
    "task": {
      "id": 1,
      "title": "Implémenter login JWT",
      "status": "DONE",
      "priority": "HIGH",
      "project": { "id": 1, "name": "Mon Projet", "key": "MPROJ" }
    },
    "taskTitle": "Implémenter login JWT",
    "user": {
      "id": 5,
      "login": "johndoe"
    },
    "projectKey": "MPROJ",
    "relatedUserId": null,
    "relatedUserLogin": null,
    "isRead": false,
    "createdAt": "2026-08-10T14:00:00Z"
  }
]
```

Headers de pagination inclus.

---

### 2. Compter les non lues

```http
GET /api/notifications/unread-count
Authorization: Bearer <token>
```

**Rôle requis** : authentifié (`isAuthenticated()`)

**Réponse 200 OK**

```json
3
```

---

### 3. Marquer comme lue

```http
PATCH /api/notifications/{id}/read
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : authentifié (`isAuthenticated()`)

**Corps de requête**

```json
{
  "isRead": true
}
```

**Réponse 200 OK**

```json
{
  "id": 1,
  "message": "Vous avez été assigné à la tâche \"Implémenter login JWT\"",
  "task": { "id": 1, "title": "Implémenter login JWT" },
  "taskTitle": "Implémenter login JWT",
  "user": { "id": 5, "login": "johndoe" },
  "projectKey": "MPROJ",
  "isRead": true,
  "createdAt": "2026-08-10T14:00:00Z"
}
```

---

### 4. Marquer toutes comme lues

```http
PATCH /api/notifications/read-all
Authorization: Bearer <token>
```

**Rôle requis** : authentifié (`isAuthenticated()`)

**Réponse 200 OK** (pas de corps)

---

### 5. Flux SSE temps réel

```http
GET /api/notifications/stream
Authorization: Bearer <token>
Accept: text/event-stream
```

**Rôle requis** : authentifié (`isAuthenticated()`)

Retourne un flux SSE (`text/event-stream`) qui pousse les nouvelles notifications au format :

```
event: notification
data: {"id":1,"message":"...","isRead":false,"createdAt":"2026-08-10T14:00:00Z"}
```

---

### 6. Administrateur : toutes les notifications

```http
GET /api/admin/notifications?page=0&size=20&sort=createdAt,desc
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN`

Retourne toutes les notifications de tous les utilisateurs.

**Réponse 200 OK**

```json
[
  {
    "id": 1,
    "message": "Nouvel utilisateur inscrit: testuser (test@example.com)",
    "task": null,
    "taskTitle": null,
    "user": { "id": 10, "login": "testuser" },
    "projectKey": null,
    "isRead": false,
    "createdAt": "2026-08-10T10:00:00Z"
  }
]
```

Headers de pagination inclus.
