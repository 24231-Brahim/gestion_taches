# Gestion de Tâches

Système de gestion de projet agile (type Jira) généré avec **JHipster 9.1.0**.

| Technologie      | Choix                        |
| ---------------- | ---------------------------- |
| Backend          | Spring Boot 4.0.6 (Java 21)  |
| Frontend         | Angular 21.2.14              |
| Base de données  | PostgreSQL 16                |
| Build            | Maven + npm                  |
| Authentification | JWT (OAuth2 Resource Server) |
| Temps réel       | WebSocket / STOMP / SockJS   |
| Cache            | Caffeine                     |

### Entités

| Entité                 | Description                                                  |
| ---------------------- | ------------------------------------------------------------ |
| **Project**            | Projet racine avec clé unique, owner et membres              |
| **ProjectMember**      | Membre d'un projet avec rôle (OWNER / MANAGER / MEMBER)      |
| **Sprint**             | Itération de développement (PLANNED / ACTIVE / COMPLETED)    |
| **Epic**               | Fonctionnalité transverse (TODO / IN_PROGRESS / DONE)        |
| **Task**               | Unité de travail (NEW / IN_PROGRESS / READY_FOR_TEST / DONE) |
| **Comment**            | Commentaire sur une tâche                                    |
| **Attachment**         | Fichier joint à une tâche                                    |
| **TaskHistory**        | Audit des modifications d'une tâche                          |
| **Notification**       | Notification in-app (assignation, rappel, etc.)              |
| **GroupMessage**       | Message de messagerie de groupe lié à un projet              |
| **Conversation**       | Conversation de chat (GENERAL ou DIRECT)                     |
| **ConversationMember** | Membre d'une conversation avec suivi de dernière lecture     |
| **ChatMessage**        | Message de chat (threads, mentions, soft-delete)             |
| **UserPresence**       | Présence en ligne d'un utilisateur                           |

## Project Structure

```
gestion_taches/
├── .jhipster/                     # Configuration JHipster des entités générées
│   ├── Attachment.json
│   ├── Comment.json
│   ├── Epic.json
│   ├── GroupMessage.json
│   ├── Project.json
│   └── Sprint.json
├── .yo-rc.json                    # Configuration du générateur JHipster
├── project-management.jdl         # Modèle de données JDL (source de vérité)
├── pom.xml                        # Build Maven
├── package.json                   # Dépendances npm
├── angular.json                   # Configuration Angular
├── cypress.config.ts              # Tests E2E
├── docs/                          # Documentation du projet
│   ├── fiche-classes.md           # Diagrammes UML et ER
│   ├── role.md                    # Rôles et permissions détaillés
│   ├── fonctionnel/               # Spécifications fonctionnelles
│   ├── technique/                 # Documentation technique
│   ├── api/                       # Documentation API REST
│   └── utilisateur/               # Guides utilisateur
├── src/
│   ├── main/
│   │   ├── docker/                # Docker Compose (DB, Sonar, monitoring)
│   │   ├── java/com/gestiontaches/
│   │   │   ├── domain/            # Entités JPA et enums
│   │   │   ├── repository/        # Accès aux données (JPA)
│   │   │   ├── service/           # Logique métier
│   │   │   ├── web/rest/          # Contrôleurs REST
│   │   │   ├── security/          # Authentification JWT
│   │   │   └── config/            # Configuration Spring
│   │   ├── resources/             # Config YAML, i18n, données SQL
│   │   └── webapp/app/            # Frontend Angular
│   │       ├── entities/          # Modules métier (task, project, sprint, chat, ...)
│   │       ├── home/              # Dashboard admin et développeur
│   │       ├── my-tasks/          # Mes tâches assignées
│   │       ├── notifications/     # Notifications in-app
│   │       ├── layouts/           # Navbar, sidebar, footer
│   │       └── core/              # Services centraux, intercepteurs
│   └── test/                      # Tests Java et frontend
├── seed-data.sql                  # Données de démonstration PostgreSQL
├── seed-data.sh                   # Script de peuplement via API
└── uploads/                       # Fichiers uploadés
```

## Prérequis

Pour le développement, PostgreSQL 16 doit être installé localement :

| Élément         | Valeur          |
| --------------- | --------------- |
| PostgreSQL      | Version 16      |
| Base de données | `gestionTaches` |
| Hôte            | `localhost`     |
| Port            | `5432`          |
| Utilisateur     | `gestionTaches` |
| Mot de passe    | `gestionTaches` |

Étapes préalables :

1. Installer PostgreSQL 16.
2. Créer la base de données `gestionTaches` :
   ```sql
   CREATE DATABASE "gestionTaches";
   ```
3. Vérifier la connexion de l'utilisateur `gestionTaches` sur `localhost:5432` (voir `src/main/resources/config/application-dev.yml`).
   Si l'authentification échoue, modifier `pg_hba.conf` :
   ```
   host  all  all  127.0.0.1/32  trust
   ```
   Puis redémarrer PostgreSQL.

## Démarrage rapide

```bash
# Installer les dépendances frontend
./npmw install

# Terminal 1 : lancer le backend (Spring Boot)
./npmw run backend:start

# Terminal 2 : lancer le frontend (Angular dev server)
./npmw run start
```

L'application est accessible sur [http://localhost:9000](http://localhost:9000).

## Scripts utiles

```bash
./npmw run backend:start        # Démarrer le backend seul
./npmw run start                # Démarrer le frontend seul
./npmw run watch                # Backend + frontend en parallèle
./npmw run build                # Build de production
./mvnw verify                   # Tests backend + frontend
./npmw test                     # Tests unitaires frontend (Vitest)
./npmw e2e                      # Tests E2E (Cypress)
./npmw lint                     # Lint ESLint
./npmw prettier:check           # Vérification du formatage
```

## Build production

```bash
./mvnw -Pprod clean verify
java -jar target/*.jar
```

L'application est accessible sur [http://localhost:8080](http://localhost:8080).

## Tests

### Backend

```bash
./mvnw verify
```

### Frontend (unitaires)

```bash
./npmw test
```

### E2E (Cypress)

```bash
# Terminal 1
./npmw run app:start

# Terminal 2
./npmw run e2e
```

## Docker

### Services dépendants (PostgreSQL, etc.)

```bash
docker compose -f src/main/docker/services.yml up -d
docker compose -f src/main/docker/services.yml down
```

### Image Docker de l'application

```bash
./mvnw -ntp verify -DskipTests -Pprod jib:dockerBuild
docker compose -f src/main/docker/app.yml up
```

## Authentification et rôles

L'application utilise **JWT** avec 4 rôles globaux :

| Rôle               | Login     | Mot de passe | Description                           |
| ------------------ | --------- | ------------ | ------------------------------------- |
| **ADMIN**          | `admin`   | `admin`      | Administrateur système, accès complet |
| **PROJET_MANAGER** | `manager` | `user`       | Chef de projet, gère les projets      |
| **DEVELOPER**      | `dev`     | `user`       | Développeur, travaille sur les tâches |
| **USER**           | `user`    | `user`       | Utilisateur, consulte et commente     |

Des rôles par projet (OWNER / MANAGER / MEMBER) sont gérés via `ProjectMember`.

## Fonctionnalités principales

- **Projets** : CRUD, membres, rôles, export CSV
- **Sprints** : PLANNED → ACTIVE → COMPLETED, backlog, clôture automatique
- **Epics** : Regroupement de tâches, roadmap, statuts automatiques
- **Tâches** : Kanban, assignation, story points, cycle de vie complet
- **Commentaires & pièces jointes** : Collaboration sur les tâches
- **Chat** : Conversations GENERAL et DIRECT par projet, mentions, threads, présence en ligne
- **Notifications** : Alertes en temps réel (SSE), assignation, rappels
- **Recherche et exports** : Recherche plein texte, export CSV projets/tâches/utilisateurs
- **Dashboards** : KPIs admin/manager, dashboard développeur
- **Audit** : Historique des modifications (TaskHistory)

## Documentation

| Document                                        | Description                               |
| ----------------------------------------------- | ----------------------------------------- |
| [docs/fiche-classes.md](docs/fiche-classes.md)  | Diagrammes UML et ER                      |
| [docs/role.md](docs_role.md)                    | Rôles, permissions et matrice d'accès     |
| [docs/fonctionnel/](docs/fonctionnel/README.md) | Spécifications fonctionnelles             |
| [docs/technique/](docs/technique/README.md)     | Architecture, déploiement, infrastructure |
| [docs/api/](docs/api/README.md)                 | Endpoints REST                            |
| [docs/utilisateur/](docs/utilisateur/README.md) | Guides utilisateur                        |

## Monitoring

```bash
# SonarQube
docker compose -f src/main/docker/sonar.yml up -d
./mvnw -Pprod clean verify sonar:sonar

# JHipster Control Center
docker compose -f src/main/docker/jhipster-control-center.yml up
```

## Qualité de code

```bash
./npmw lint                        # ESLint
./npmw prettier:check              # Vérification du formatage
./mvnw checkstyle:check            # Checkstyle Java
```

##stack technique détaillée

| Couche          | Technologie                        |
| --------------- | ---------------------------------- |
| Backend         | Spring Boot 4.0.6, Java 21         |
| Frontend        | Angular 21.2.14, TypeScript 5.9    |
| Base de données | PostgreSQL 16, Liquibase           |
| Sécurité        | Spring Security, JWT (HS512)       |
| Temps réel      | WebSocket, STOMP, SockJS, SSE      |
| Cache           | Caffeine, JCache                   |
| Monitoring      | Micrometer, Prometheus, Grafana    |
| Build backend   | Maven 3.2.5+                       |
| Build frontend  | Node v24.16.0, npm 11.15.0         |
| Tests backend   | JUnit 5, ArchUnit, Testcontainers  |
| Tests frontend  | Vitest, Cypress                    |
| Qualité         | Checkstyle, Spotless, SonarQube    |
| Image Docker    | Jib (eclipse-temurin:25-jre-noble) |

## Remarques

- Le chat utilise **REST polling** pour les messages (intervalle 5s) et la présence (intervalle 30s), via WebSocket pour le tracking uniquement.
- L'entité `Task` n'a pas de champ `type` (pas de distinction Story/Bug/Task/Subtask/Improvement).
- Les migrations de base de données sont gérées par Liquibase.
