# Compte utilisateur & Authentification

Regroupe les endpoints relatifs au compte de l'utilisateur courant (`AccountResource`). L'obtention du JWT est documentée dans le [README](./README.md#authentification-jwt).

## Règles communes

- **Mot de passe** : longueur comprise entre **4** et **100** caractères (validation appliquée à l'inscription, au changement de mot de passe et à la réinitialisation).
- **Login** : `@NotBlank`, 1 à 50 caractères, expression régulière :
  `^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$`
  (accepte aussi bien un login classique qu'une adresse email).
- **Email** : `@Email @Size(min=5, max=254)`.
- **Langue** (`langKey`) : 2 à 10 caractères.

## Schéma AdminUserDTO

| Champ | Type | Contraintes | Description |
|-------|------|-------------|-------------|
| `id` | `Long` | — | Identifiant |
| `login` | `String` | `@NotBlank @Pattern(LOGIN_REGEX) @Size(1-50)` | Login |
| `firstName` | `String` | `@Size(max=50)` | Prénom |
| `lastName` | `String` | `@Size(max=50)` | Nom |
| `email` | `String` | `@Email @Size(5-254)` | Email |
| `imageUrl` | `String` | `@Size(max=256)` | URL d'avatar |
| `activated` | `boolean` | — | Compte activé |
| `langKey` | `String` | `@Size(2-10)` | Code langue |
| `createdBy` | `String` | — | Créé par |
| `createdDate` | `Instant` | — | Date de création |
| `lastModifiedBy` | `String` | — | Modifié par |
| `lastModifiedDate` | `Instant` | — | Date de modification |
| `authorities` | `Set<String>` | — | Rôles (`ROLE_ADMIN`, etc.) |

---

## 1. S'inscrire

```http
POST /api/register
Content-Type: application/json
```

**Rôle requis** : aucun (endpoint public).

Le compte est créé avec le rôle `ROLE_USER`, **désactivé** (`activated=false`), puis un email d'activation est envoyé. Une notification est également créée pour tous les administrateurs (« Nouvel utilisateur inscrit »).

**Corps de requête** (`ManagedUserVM` = `AdminUserDTO` + `password`)

```json
{
  "login": "johndoe",
  "firstName": "John",
  "lastName": "Doe",
  "email": "johndoe@example.com",
  "password": "secret",
  "langKey": "en"
}
```

**Réponse 201 Created** — corps vide.

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | Mot de passe invalide (`invalidpassword`), login déjà utilisé (`userexists`), ou email déjà utilisé (`emailexists`) |

---

## 2. Activer un compte

```http
GET /api/activate?key=<activationKey>
```

**Rôle requis** : aucun (endpoint public).

Active le compte correspondant à la clé reçue par email.

**Réponse 200 OK** — corps vide.

**Erreurs**

| Code | Signification |
|------|---------------|
| 500 | Aucun utilisateur trouvé pour cette clé d'activation |

---

## 3. Récupérer son compte

```http
GET /api/account
Authorization: Bearer <token>
```

**Rôle requis** : authentifié.

**Réponse 200 OK** — `AdminUserDTO` complet (avec `authorities`).

```json
{
  "id": 5,
  "login": "johndoe",
  "firstName": "John",
  "lastName": "Doe",
  "email": "johndoe@example.com",
  "imageUrl": null,
  "activated": true,
  "langKey": "en",
  "createdBy": "system",
  "createdDate": "2026-07-01T10:00:00Z",
  "lastModifiedBy": "johndoe",
  "lastModifiedDate": "2026-08-01T10:00:00Z",
  "authorities": ["ROLE_USER", "ROLE_DEVELOPER"]
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 401 | Token manquant ou invalide |
| 500 | Utilisateur courant introuvable |

---

## 4. Mettre à jour son compte

```http
POST /api/account
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : authentifié.

Met à jour `firstName`, `lastName`, `email`, `langKey` et `imageUrl` de l'utilisateur courant.

**Corps de requête**

```json
{
  "firstName": "Johnny",
  "lastName": "Doe",
  "email": "johnny.doe@example.com",
  "langKey": "fr"
}
```

**Réponse 200 OK** — corps vide.

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | Email déjà utilisé par un autre compte (`emailexists`) |
| 401 | Token manquant ou invalide |
| 500 | Utilisateur courant introuvable |

---

## 5. Changer son mot de passe

```http
POST /api/account/change-password
Authorization: Bearer <token>
Content-Type: application/json
```

**Rôle requis** : authentifié.

**Corps de requête** (`PasswordChangeDTO`)

```json
{
  "currentPassword": "secret",
  "newPassword": "nouveaumotdepasse"
}
```

**Réponse 200 OK** — corps vide.

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | Nouveau mot de passe de longueur invalide (`invalidpassword`) |
| 401 | Token manquant ou invalide |
| 500 | Mot de passe courant incorrect |

---

## 6. Demander une réinitialisation de mot de passe

```http
POST /api/account/reset-password/init
Content-Type: text/plain
```

**Rôle requis** : aucun (endpoint public).

Le corps est **une chaîne brute** (l'adresse email), non un JSON. Un email de réinitialisation est envoyé si le compte existe.

```
johndoe@example.com
```

**Réponse 200 OK** — corps vide.

> La réponse est identique que l'email existe ou non (protection contre l'énumération de comptes). Un avertissement est simplement journalisé côté serveur en cas d'email inconnu.

---

## 7. Finaliser la réinitialisation de mot de passe

```http
POST /api/account/reset-password/finish
Content-Type: application/json
```

**Rôle requis** : aucun (endpoint public).

**Corps de requête** (`KeyAndPasswordVM`)

```json
{
  "key": "6c1e2a3b-...",
  "newPassword": "nouveaumotdepasse"
}
```

**Réponse 200 OK** — corps vide.

**Erreurs**

| Code | Signification |
|------|---------------|
| 400 | Nouveau mot de passe de longueur invalide (`invalidpassword`) |
| 500 | Aucun utilisateur trouvé pour cette clé de réinitialisation |
