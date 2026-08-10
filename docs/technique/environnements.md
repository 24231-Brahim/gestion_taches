# Environnements

## Profils Spring

Le projet utilise les profils Spring suivants :

| Profil | Usage | Activé par défaut |
|--------|-------|-------------------|
| `dev` | Développement local | Oui (via `@spring.profiles.active@` dans `pom.xml`) |
| `prod` | Production | Non |
| `api-docs` | Activation springdoc-openapi | Inclus dans `dev` |
| `secret-samples` | Variables secrètes (JWT secret) | Inclus dans `dev` |
| `e2e` | Tests E2E | Non |

Le profile `dev` active automatiquement le groupe de profils défini dans `application.yml` :

```yaml
spring:
  profiles:
    group:
      dev:
        - secret-samples
        - api-docs
```

## Comparaison dev / prod

| Aspect | Dev | Prod |
|--------|-----|------|
| Port | `8080` | `8080` |
| PostgreSQL | `localhost:5432/gestion_taches` | `localhost:5432/gestionTaches` (ou via Docker) |
| User DB | `postgres` (sans password) | `gestionTaches` (sans password en Docker) |
| Hikari pool | max 15, min 5 | max 10, min 3 |
| Cache Caffeine | TTL 3600s, max 100 entrées | TTL 3600s, max 1000 entrées |
| CORS | Activé (origins: localhost:4200, 8100) | Désactivé par défaut |
| springdoc-openapi | Activé (via profile `api-docs`) | Désactivé par défaut |
| Liquibase context | `dev` | `prod` |
| Compression réponse | Désactivée | Activée (min 1024 octets) |
| Graceful shutdown | Désactivé | Activé |
| Prometheus metrics | Activées | Désactivées |
| Logs | DEBUG pour `com.gestiontaches` | INFO pour tous |
| Thymeleaf cache | Désactivé | Activé |
| DevTools restart | Activé | Désactivé |
| Mail | Désactivé (host: localhost:25) | Désactivé |

## Configuration PostgreSQL locale

### Base de données

| Paramètre | Valeur |
|-----------|--------|
| Nom de la base | `gestion_taches` (dev) / `gestionTaches` (prod) |
| Port | `5432` |
| User | `postgres` (dev) / `gestionTaches` (prod) |
| Password | (vide en dev, trust auth) |
| Driver | `org.postgresql.Driver` |
| Dialecte Hibernate | `org.hibernate.dialect.PostgreSQLDialect` |

### Connexion en dev

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/gestion_taches
spring.datasource.username=postgres
spring.datasource.password=
```

### Connexion en prod (Docker)

```properties
spring.datasource.url=jdbc:postgresql://postgresql:5432/gestionTaches
spring.datasource.username=gestionTaches
spring.datasource.password=
```

## Variables sensibles

### JWT Secret

Stocké dans `src/main/resources/config/application-secret-samples.yml` (non versionné, ignoré par git) :

```yaml
jhipster:
  security:
    authentication:
      jwt:
        base64-secret: <valeur_base64>
```

En production, ce secret doit être fourni via variable d'environnement ou secret Docker.

### Variables d'environnement Docker

| Variable | Description |
|----------|-------------|
| `SPRING_PROFILES_ACTIVE` | Profils Spring actifs |
| `SPRING_DATASOURCE_URL` | URL JDBC |
| `SPRING_DATASOURCE_USERNAME` | User DB |
| `SPRING_DATASOURCE_PASSWORD` | Password DB |
| `JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET` | Secret JWT (JHCC) |

## Liquibase

Les migrations sont gérées par Liquibase :

| Profil | Context |
|--------|---------|
| `dev` | `dev` |
| `prod` | `prod` |
| `e2e` | `e2e` |

Les changelogs sont dans `src/main/resources/db/` (non exploré en détail dans cette analyse).

## Cache

Configuré dans `CacheConfiguration.java` :

| Paramètre | Dev | Prod |
|-----------|-----|------|
| TTL | 3600s | 3600s |
| Max entrées | 100 | 1000 |
| Statistiques | Activées | Activées |

Caches configurés :

```
com.gestiontaches.repository.UserRepository.USERS_BY_LOGIN_CACHE
com.gestiontaches.repository.UserRepository.USERS_BY_EMAIL_CACHE
com.gestiontaches.domain.User
com.gestiontaches.domain.Authority
com.gestiontaches.domain.User.authorities
com.gestiontaches.domain.Project
com.gestiontaches.domain.Project.sprintses
com.gestiontaches.domain.Project.epicses
com.gestiontaches.domain.Project.tasks
com.gestiontaches.domain.Sprint
com.gestiontaches.domain.Epic
com.gestiontaches.domain.Task
com.gestiontaches.domain.Task.comments
com.gestiontaches.domain.Task.attachments
com.gestiontaches.domain.Comment
com.gestiontaches.domain.Attachment
```

## Actuator endpoints

Exposés selon la config `application.yml` :

| Endpoint | Accès | Description |
|----------|-------|-------------|
| `/management/health` | Public | Health check |
| `/management/health/**` | Public | Détails health |
| `/management/info` | Public | Info application (git, env) |
| `/management/prometheus` | Public | Métriques Prometheus |
| `/management/configprops` | `ROLE_ADMIN` | Configuration properties |
| `/management/env` | `ROLE_ADMIN` | Variables d'environnement |
| `/management/loggers` | `ROLE_ADMIN` | Loggers |
| `/management/threaddump` | `ROLE_ADMIN` | Thread dump |
| `/management/caches` | `ROLE_ADMIN` | État des caches |
| `/management/liquibase` | `ROLE_ADMIN` | État Liquibase |
| `/management/jhimetrics` | Read-only | Métriques JHipster |
| `/management/logfile` | `ROLE_ADMIN` | Log file |

## Langue par défaut

La langue par défaut de l'application est le français :

```java
// Constants.java
public static final String DEFAULT_LANGUAGE = "fr";
```
