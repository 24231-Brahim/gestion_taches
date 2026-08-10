# Documentation technique — Gestion Tâches

Ce dossier contient la documentation technique officielle du projet **Gestion Tâches**, destinée aux développeurs qui rejoignent le projet.

## Stack technique confirmée

| Couche | Technologie | Version |
|--------|-------------|---------|
| Backend | Spring Boot (JHipster) | 4.0.6 |
| Frontend | Angular | 21.2.14 |
| Base de données | PostgreSQL | 18.4 |
| Build backend | Maven | (via `mvnw`) |
| Build frontend | npm / Angular CLI | 21.2.12 |
| Sécurité | Spring Security + JWT (OAuth2 Resource Server) | HS512 |
| Cache | Caffeine + Hibernate second-level cache | — |
| Messaging | Spring WebSocket + STOMP + SockJS | — |
| Monitoring | Prometheus + Grafana + Actuator | — |
| Qualité | SonarQube | 26.5.0 |

## Sommaire

| Fichier | Description |
|---------|-------------|
| [architecture.md](./architecture.md) | Architecture globale, couches backend, structure frontend |
| [modele-donnees.md](./modele-donnees.md) | Modèle de données, entités, enums, relations |
| [securite.md](./securite.md) | Authentification JWT, rôles, CORS, endpoints publics/protégés |
| [frontend.md](./frontend.md) | Structure Angular, routing, state management, composants |
| [chat-temps-reel.md](./chat-temps-reel.md) | WebSocket/STOMP, canaux, polling REST, présence |
| [infrastructure.md](./infrastructure.md) | Docker Compose, services, monitoring, variables d'environnement |
| [environnements.md](./environnements.md) | Profils dev/prod, configuration PostgreSQL, variables sensibles |
| [deploiement.md](./deploiement.md) | Build, packaging Docker, déploiement, monitoring |

## Arborescence rapide du projet

```
.
├── src/
│   ├── main/
│   │   ├── java/com/gestiontaches/
│   │   │   ├── config/          # Config Spring (sécurité, cache, DB, WebSocket)
│   │   │   ├── domain/         # Entités JPA
│   │   │   ├── repository/     # Spring Data JPA repositories
│   │   │   ├── service/        # Services métier + DTOs + critères
│   │   │   ├── web/rest/       # Contrôleurs REST
│   │   │   └── security/       # Security utils, UserDetailsService
│   │   ├── resources/
│   │   │   ├── config/         # application.yml, application-dev.yml, application-prod.yml
│   │   │   └── db/             # Liquibase changelogs
│   │   └── webapp/
│   │       └── app/
│   │           ├── core/       # Auth, interceptors, config
│   │           ├── shared/     # Composants partagés (pagination, tri, dates)
│   │           ├── entities/   # Modules métier (project, sprint, epic, task, chat...)
│   │           ├── layouts/    # Navbar, sidebar, error pages
│   │           ├── home/       # Dashboard
│   │           ├── login/      # Authentification
│   │           └── account/    # Gestion de compte
│   └── test/
├── docs/
│   ├── api/                    # Documentation API REST
│   └── technique/              # Documentation technique (ce dossier)
├── src/main/docker/            # Docker Compose files
├── pom.xml                     # Maven (backend)
├── package.json                # npm (frontend)
└── project-management.jdl      # Modèle de données JDL
```

## Commandes utiles

```bash
# Backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw verify -Pprod                    # Build JAR production
./mvnw verify -Pprod jib:dockerBuild    # Build Docker image

# Frontend
npm start                                # Dev server Angular (port 4200)
npm run webapp:build:prod                # Build production

# Docker
docker compose -f src/main/docker/postgresql.yml up --wait
docker compose -f src/main/docker/app.yml up --wait
docker compose -f src/main/docker/services.yml up --wait

# Tests
npm run backend:unit:test
npm run ci:frontend:test
npm run e2e:dev
```
