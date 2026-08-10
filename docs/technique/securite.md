# Sécurité

## Vue d'ensemble

```mermaid
graph LR
    A[Client Angular] -->|POST /api/authenticate| B[AuthenticateController]
    B -->|validate| C[AuthenticationManager]
    C -->|loadUser| D[DomainUserDetailsService]
    D -->|query| E[(PostgreSQL)]
    B -->|create JWT| F[JwtEncoder]
    B -->|return token| A

    A -->|+ Authorization Bearer| G[SecurityFilterChain]
    G -->|validate| H[JwtDecoder]
    G -->|check| I[@PreAuthorize]
    I -->|call| J[ProjectPermissionService]
    J -->|query| E
```

## Authentification JWT

### Configuration

Le projet utilise **Spring Security 6** avec un **OAuth2 Resource Server JWT**.

| Paramètre | Valeur (dev) | Fichier |
|-----------|--------------|---------|
| Algorithme | `HS512` | `SecurityUtils.java` |
| Durée token standard | `86400` s (24h) | `application-dev.yml` |
| Durée remember-me | `2592000` s (30j) | `application-dev.yml` |
| Secret | Base64, variable `jhipster.security.authentication.jwt.base64-secret` | `application-secret-samples.yml` |
| Claim autorités | `auth` | `SecurityUtils.java` |
| Claim userId | `userId` | `SecurityUtils.java` |

### Flux de connexion

1. Le client envoie `POST /api/authenticate` avec `{ username, password, rememberMe }`.
2. `AuthenticateController` crée un `UsernamePasswordAuthenticationToken`.
3. `AuthenticationManager` valide les identifiants via `DomainUserDetailsService`.
4. Si valide, un JWT est créé avec :
   - `sub` = login
   - `auth` = autorités séparées par espaces (ex: `ROLE_ADMIN ROLE_USER`)
   - `userId` = ID de l'utilisateur
5. Le token est retourné dans le body (`id_token`) et dans le header `Authorization: Bearer <token>`.

### Stockage du token côté frontend

| Mode | Stockage |
|------|----------|
| Normal | `sessionStorage` (clé `jhi-authenticationToken`) |
| Remember-me | `localStorage` (clé `jhi-authenticationToken`) |

Le token est automatiquement ajouté à toutes les requêtes HTTP via l'**intercepteur** `auth.interceptor.ts` :

```typescript
req = req.clone({
  setHeaders: {
    Authorization: `Bearer ${token}`,
  },
});
```

### Vérification de l'authentification

```http
GET /api/authenticate
```

- `204 No Content` : authentifié
- `401 Unauthorized` : non authentifié

## Rôles et autorités

### Rôles globaux (Spring Security authorities)

| Rôle | Description |
|------|-------------|
| `ROLE_ADMIN` | Accès global, administration |
| `ROLE_PROJET_MANAGER` | Gestion de projets |
| `ROLE_DEVELOPER` | Développeur |
| `ROLE_USER` | Utilisateur basique |

Définis dans `AuthoritiesConstants.java`.

### Rôles projet (ProjectRole)

| Rôle | Description |
|------|-------------|
| `OWNER` | Propriétaire du projet |
| `MANAGER` | Gestionnaire |
| `MEMBER` | Membre (rôle par défaut des développeurs) |

Stockés dans la table `project_member` (entité `ProjectMember`).

### Application backend

Les sécurisations sont appliquées à deux niveaux :

1. **HTTP Security** (`SecurityConfiguration.java`) : endpoints publics vs protégés.
2. **Method Security** (`@PreAuthorize`) : contrôle fin par rôle/authorité sur chaque méthode.

**Endpoints publics** (confirmés dans `SecurityConfiguration`):

| Endpoint | Méthode | Description |
|----------|---------|-------------|
| `/api/authenticate` | POST | Login JWT |
| `/api/authenticate` | GET | Vérification auth |
| `/api/register` | POST | Inscription |
| `/api/activate` | GET | Activation compte |
| `/api/account/reset-password/init` | POST | Demande reset password |
| `/api/account/reset-password/finish` | POST | Confirmation reset password |
| `/index.html`, `/*.js`, `/*.css`, `/content/**`, `/resources/**` | GET | Assets statiques |
| `/swagger-ui/**` | GET | UI Swagger |
| `/websocket/tracker/**` | GET | WebSocket endpoint |
| `/management/health` | GET | Health check |
| `/management/info` | GET | Info application |
| `/management/prometheus` | GET | Metrics Prometheus |

**Endpoints protégés** :

| Pattern | Rôle requis |
|---------|-------------|
| `/api/admin/**` | `ROLE_ADMIN` |
| `/api/**` (tout le reste) | Authentifié (`isAuthenticated()`) |
| `/v3/api-docs/**` | `ROLE_ADMIN` |
| `/management/**` (sauf health/info/prometheus) | `ROLE_ADMIN` |

### ProjectPermissionService

Service métier qui centralise les vérifications d'accès aux projets :

```java
public void requireProjectAccess(Long projectId)       // ADMIN bypass, sinon check membership
public void requireProjectRole(Long projectId, ProjectRole... roles)  // Check role
public ProjectRole getCurrentUserRole(Long projectId)   // Retourne OWNER pour ADMIN
```

**Règles** :
- `ROLE_ADMIN` a un accès global à tous les projets (bypass).
- Tout autre rôle doit être membre du projet (`ProjectMember`).
- Les opérations de modification (create/update/delete) requièrent généralement `OWNER` ou `MANAGER` sur le projet.

### Application frontend

#### Guard de routes

`UserRouteAccessService` est un `CanActivateFn` qui :
1. Vérifie si l'utilisateur est authentifié (`AccountService.identity()`).
2. Si oui, vérifie les autorités requises dans `route.data.authorities`.
3. Si non, redirige vers `/login`.

Exemple d'utilisation dans les routes :

```typescript
{
  path: 'admin',
  data: { authorities: [Authority.ADMIN] },
  canActivate: [UserRouteAccessService],
  loadChildren: () => import('./admin/admin.routes'),
}
```

#### Directive d'autorité

`jhiHasAnyAuthority` permet de conditionner l'affichage d'un élément HTML :

```html
<button *jhiHasAnyAuthority="['ROLE_ADMIN']">Admin only</button>
```

## CORS

Configuré dans `WebConfigurer.java` via `jhipster.cors.*`.

**Profil dev** (`application-dev.yml`) :

| Paramètre | Valeur |
|-----------|--------|
| `allowed-origins` | `http://localhost:8100,https://localhost:8100,http://localhost:4200,https://localhost:4200` |
| `allowed-origin-patterns` | `https://*.githubpreview.dev` |
| `allowed-methods` | `*` |
| `allowed-headers` | `*` |
| `exposed-headers` | `Authorization,Link,X-Total-Count,...` |
| `allow-credentials` | `true` |
| `max-age` | `1800` |

Le CORS est activé par défaut uniquement en dev. En prod, il est désactivé sauf configuration explicite.

## WebSocket / STOMP — Authentification

L'endpoint `/websocket/tracker` est configuré avec un **intercepteur STOMP** qui authentifie la connexion via JWT :

1. Le client envoie un frame STOMP `CONNECT` avec le header `Authorization: Bearer <token>`.
2. L'intercepteur décode le JWT via `JwtDecoder`.
3. L'authentification Spring est extraite et attachée à la session WebSocket (`accessor.setUser(authentication)`).

Cela permet d'identifier l'utilisateur dans les messages WebSocket ultérieurs.

## Endpoints publics vs protégés (récapitulatif)

| Endpoint | Accès | Description |
|----------|-------|-------------|
| `POST /api/authenticate` | Public | Login |
| `GET /api/authenticate` | Public | Check auth |
| `POST /api/register` | Public | Inscription |
| `GET /api/activate` | Public | Activation |
| `POST /api/account/reset-password/init` | Public | Reset password |
| `POST /api/account/reset-password/finish` | Public | Reset password |
| `GET /api/account` | Authentifié | Mon compte |
| `POST /api/account` | Authentifié | Modifier mon compte |
| `POST /api/account/change-password` | Authentifié | Changer mot de passe |
| `/api/admin/**` | `ROLE_ADMIN` | Administration |
| `/api/**` (reste) | Authentifié | API métier |
| `/v3/api-docs/**` | `ROLE_ADMIN` | OpenAPI spec |
| `/management/**` | `ROLE_ADMIN` sauf health/info/prometheus | Actuator |
| `/websocket/tracker/**` | Public (auth JWT dans CONNECT) | WebSocket |
