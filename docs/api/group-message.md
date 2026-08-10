# Group Messages

Messages projet (annonces / messages de groupe), scopés à un projet.

Base URL : `http://localhost:8080/api/projects/{projectId}/group-messages`

## Schéma GroupMessageDTO

| Champ | Type | Contraintes | Description |
|-------|------|-------------|-------------|
| `id` | `Long` | — | Identifiant unique |
| `content` | `String` | `@NotNull @Size(min=1, max=5000)` | Contenu du message |
| `createdAt` | `Instant` | — | Date d'envoi (défini serveur si absent) |
| `sender` | `UserDTO` | — | Expéditeur (défini serveur à partir de l'utilisateur connecté) |
| `recipient` | `UserDTO` | — | Destinataire optionnel (voir « Visibilité ») |
| `project` | `ProjectDTO` | — | Projet associé (défini serveur à partir du chemin) |

## Visibilité

Un message n'est visible que pour :
- **tout le monde** (diffusion générale) si `recipient` est `null` ;
- le **destinataire** et l'**expéditeur** s'il a un `recipient`.

Les messages retournés sont triés par `createdAt` **croissant** (du plus ancien au plus récent).

## Contrôle d'accès

- L'utilisateur doit être authentifié.
- Les `ROLE_ADMIN` et `ROLE_PROJET_MANAGER` accèdent au projet sans vérification.
- Tout autre utilisateur doit être **membre du projet** (sinon erreur `notprojectmember`).

---

## 1. Envoyer un message

```http
POST /api/projects/{projectId}/group-messages
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : authentifié + membre du projet (ou `ADMIN`/`PROJET_MANAGER`).

**Corps de requête**

```json
{
  "content": "Réunion de synchronisation demain à 10h."
}
```

Avec destinataire privé (optionnel) :

```json
{
  "content": "Merci pour la revue de code.",
  "recipient": {
    "id": 6
  }
}
```

**Réponse 201 Created**

```json
{
  "id": 1,
  "content": "Réunion de synchronisation demain à 10h.",
  "createdAt": "2026-08-10T14:30:00Z",
  "sender": {
    "id": 5,
    "login": "johndoe"
  },
  "recipient": null,
  "project": {
    "id": 1,
    "name": "Mon Projet",
    "key": "MPROJ"
  }
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | Message avec ID (`idexists`), utilisateur introuvable (`usernotfound`) ou non membre du projet (`notprojectmember`) |
| 401 | Token manquant ou invalide |

---

## 2. Lister les messages visibles

```http
GET /api/projects/{projectId}/group-messages
Authorization: Bearer <token>
```

**Rôle requis** : authentifié + membre du projet (ou `ADMIN`/`PROJET_MANAGER`).

**Réponse 200 OK**

```json
[
  {
    "id": 1,
    "content": "Réunion de synchronisation demain à 10h.",
    "createdAt": "2026-08-10T14:30:00Z",
    "sender": {
      "id": 5,
      "login": "johndoe"
    },
    "recipient": null,
    "project": {
      "id": 1,
      "name": "Mon Projet",
      "key": "MPROJ"
    }
  },
  {
    "id": 2,
    "content": "Merci pour la revue de code.",
    "createdAt": "2026-08-10T15:00:00Z",
    "sender": {
      "id": 6,
      "login": "alice"
    },
    "recipient": {
      "id": 5,
      "login": "johndoe"
    },
    "project": {
      "id": 1,
      "name": "Mon Projet",
      "key": "MPROJ"
    }
  }
]
```

> Le message privé (`id=2`, destiné à `johndoe`) n'apparaît ici que parce que la requête est faite par `johndoe` (destinataire). Un autre membre du projet ne verrait que le message général (`id=1`).

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | Utilisateur introuvable (`usernotfound`) ou non membre du projet (`notprojectmember`) |
| 401 | Token manquant ou invalide |
