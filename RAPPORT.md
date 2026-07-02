# Rapport de Projet — Gestion de Tâches

---

## Page de Titre

| Champ                       | Valeur            |
| --------------------------- | ----------------- |
| **Nom du projet**           | Gestion de Tâches |
| **Nom de l'auteur**         | Brahim            |
| **Date**                    | Juin 2026         |
| **Lieu**                    | Stage S4          |
| **Encadrant / Institution** | [À compléter]     |

---

## Dédicace et Remerciements

Je tiens à remercier chaleureusement mon encadrant de stage pour son accompagnement, ses conseils avisés et sa disponibilité tout au long de ce projet. Sa rigueur technique et sa pédagogie m'ont permis de monter en compétence sur des sujets aussi variés que la sécurité des applications web, l'architecture microservices et le développement full-stack avec Spring Boot et Angular.

Je remercie également l'équipe pédagogique pour la qualité de la formation dispensée et pour m'avoir offert l'opportunité de réaliser ce stage au sein d'un environnement professionnel stimulant.

Enfin, je dédie ce travail à ma famille et à mes proches, pour leur soutien indéfectible et leurs encouragements tout au long de cette période.

---

## Sommaire

1. Introduction
2. Présentation du Projet
3. Problématique
4. Technologies & Outils Utilisés
5. Architecture du Système
6. Fonctionnalités Réalisées
7. Démonstration / Captures d'écran
8. Ce qui Reste à Faire
9. Planning & Avancement
10. Difficultés Rencontrées
11. Conclusion & Perspectives
12. Références & Bibliographie

---

## Table des Illustrations

| N°  | Figure                               | Description                                  |
| --- | ------------------------------------ | -------------------------------------------- |
| 1   | `./images/architecture-globale.png`  | Diagramme d'architecture générale du système |
| 2   | `./images/diagramme-classes.png`     | Diagramme de classes UML des entités JPA     |
| 3   | `./images/matrice-acces.png`         | Matrice des accès RBAC par contrôleur        |
| 4   | `./images/capture-home.png`          | Page d'accueil de l'application              |
| 5   | `./images/capture-liste-projets.png` | Liste des projets avec pagination            |
| 6   | `./images/capture-detail-issue.png`  | Détail d'une issue avec commentaires         |
| 7   | `./images/capture-sidebar.png`       | Sidebar collapsible et navigation            |
| 8   | `./images/capture-mobile.png`        | Vue mobile avec BottomNav                    |

---

## Introduction

La gestion de projet est un enjeu stratégique pour toute organisation souhaitant structurer, planifier et suivre efficacement ses activités. Dans un contexte où les équipes de développement adoptent de plus en plus les méthodologies agiles (Scrum, Kanban), disposer d'un outil de gestion de tâches adapté devient essentiel.

Le présent rapport détaille la conception, le développement et les fonctionnalités de **Gestion de Tâches**, une application web de gestion de projet inspirée de Jira, développée dans le cadre d'un stage S4. Ce document couvre l'architecture technique, les choix technologiques, les fonctionnalités réalisées ainsi que les perspectives d'évolution.

Après une présentation du projet et de la problématique adressée, nous détaillerons la stack technique, l'architecture du système, puis les fonctionnalités opérationnelles. Nous aborderons ensuite le planning, les difficultés rencontrées et les axes d'amélioration futurs.

---

## 1. Présentation du Projet

L'application **Gestion de Tâches** est une plateforme web complète de gestion de projets, calquée sur le modèle de Jira. Elle permet à des équipes de planifier, suivre et organiser leur travail à travers des sprints, des epics et des issues.

### 1.1 Contexte

Le projet a été initié dans le cadre d'un stage de fin d'année (S4) visant à mettre en pratique les compétences acquises en développement web full-stack, en sécurité des applications et en gestion de bases de données. L'objectif était de produire un outil fonctionnel, sécurisé et industrialisable.

### 1.2 Objectifs Généraux

- Offrir une gestion complète des projets avec un système de rôles et permissions (RBAC)
- Permettre aux utilisateurs de créer et suivre des issues (tâches, bugs, stories, améliorations)
- Organiser le travail par sprints et epics
- Assurer la traçabilité des modifications via un historique d'actions
- Proposer une interface utilisateur moderne, responsive et personnalisée
- Garantir la sécurité via une authentification JWT et un contrôle d'accès fin

### 1.3 Entités du Domaine

| Entité            | Description                                               | Relations clés                                       |
| ----------------- | --------------------------------------------------------- | ---------------------------------------------------- |
| **Project**       | Projet racine                                             | owner (User), projectMembers, sprints, epics, issues |
| **ProjectMember** | Association membre-équipe (remplace le ManyToMany)        | project (Project), user (User), role, joinedAt       |
| **Sprint**        | Itération de développement                                | Appartient à un Project                              |
| **Epic**          | Fonctionnalité transverse                                 | Appartient à un Project                              |
| **Issue**         | Unité de travail (Story, Bug, Task, Subtask, Improvement) | Appartient à un Project, optionnel : Sprint, Epic    |
| **Comment**       | Commentaire sur une issue                                 | Lié à une Issue                                      |
| **Attachment**    | Fichier joint à une issue                                 | Lié à une Issue                                      |
| **ActionHistory** | Audit des modifications                                   | Lié à une Issue                                      |

---

## 2. Problématique

Les solutions de gestion de projet existantes (Jira, Trello, Asana, Linear) sont soit payantes, soit surdimensionnées, soit impossibles à personnaliser. Une équipe ou une organisation souhaitant disposer d'un outil de gestion de tâches agile doit faire face aux défis suivants :

1. **Coût** — Les licences logicielles (notamment Jira) représentent un investissement récurrent non négligeable.
2. **Personnalisation** — Les outils SaaS imposent leur modèle de données et leurs workflows ; toute adaptation nécessite des extensions payantes ou des contournements hasardeux.
3. **Maîtrise des données** — L'hébergement chez un tiers soulève des questions de confidentialité et de conformité (RGPD).
4. **Flexibilité technique** — Avoir la main sur le code source permet d'intégrer des fonctionnalités métier spécifiques et de s'interfacer avec d'autres systèmes internes.

Le projet répond donc à la problématique suivante :

> **Comment développer une application web de gestion de projet agile, sécurisée, extensible et maîtrisée de bout en bout, en utilisant une stack technique moderne (Spring Boot + Angular) ?**

Les enjeux associés sont :

- **Sécurité** : authentification forte, gestion fine des permissions, protection des données
- **Expérience utilisateur** : interface intuitive, responsive, rapide
- **Maintenabilité** : code structuré, testé, documenté
- **Extensibilité** : architecture modulaire permettant d'ajouter facilement des fonctionnalités

---

## 3. Technologies & Outils Utilisés

### 3.1 Backend

| Technologie                     | Version | Rôle                                                                             |
| ------------------------------- | ------- | -------------------------------------------------------------------------------- |
| **Java**                        | 21      | Langage de programmation principal (LTS, performances, écosystème mature)        |
| **Spring Boot**                 | 4.0.6   | Framework d'application (injection de dépendances, configuration auto, sécurisé) |
| **Spring Data JPA / Hibernate** | —       | ORM et couche d'accès aux données                                                |
| **Spring Security**             | —       | Authentification JWT + RBAC (@PreAuthorize)                                      |
| **Liquibase**                   | —       | Gestion des migrations de schéma de base de données                              |
| **MapStruct**                   | 1.6.3   | Mapping automatique entité ↔ DTO                                                 |
| **PostgreSQL**                  | —       | Base de données relationnelle (prod)                                             |
| **H2**                          | —       | Base de données embarquée (développement/tests)                                  |
| **Maven**                       | —       | Build et gestion de dépendances                                                  |
| **Caffeine**                    | —       | Cache JCache (second-level Hibernate)                                            |
| **Jackson**                     | —       | Sérialisation/désérialisation JSON                                               |

**Justification des choix :**

- **Spring Boot 4.0.6** : version récente avec support Java 21, sécurité renforcée, Docker Compose intégré
- **Liquibase** : versionnement et reproductibilité des schémas (préférable à Hibernate DDL auto)
- **MapStruct** : génération de code à la compilation (pas de reflection, performances optimales)
- **Caffeine** : cache performant pour les entités fréquemment lues

### 3.2 Frontend

| Technologie       | Version | Rôle                                           |
| ----------------- | ------- | ---------------------------------------------- |
| **Angular**       | 21.2.14 | Framework SPA (structure, routing, réactivité) |
| **TypeScript**    | 5.9.3   | Typage statique, maintenabilité                |
| **Bootstrap 5**   | 5.3.8   | Framework CSS responsive                       |
| **FontAwesome**   | 7.2.0   | Icônes vectorielles                            |
| **ng-bootstrap**  | 20.0.0  | Composants Bootstrap natifs Angular            |
| **ngx-translate** | 17.0.0  | Internationalisation (i18n)                    |
| **RxJS**          | 7.8.2   | Programmation réactive                         |
| **esbuild**       | —       | Bundler (remplace Webpack, ~10× plus rapide)   |
| **Vitest**        | 4.1.7   | Tests unitaires (compatible esbuild)           |
| **Cypress**       | 15.16.0 | Tests end-to-end                               |

**Justification des choix :**

- **Angular 21** : framework mature, idéal pour les applications d'entreprise complexes
- **Bootstrap 5** : écosystème riche, compatible avec ng-bootstrap pour des composants accessibles
- **esbuild** : build extrêmement rapide, adoption par Angular CLI

### 3.3 Outils de Développement

| Outil                   | Rôle                                                                 |
| ----------------------- | -------------------------------------------------------------------- |
| **JHipster 9.1.0**      | Générateur de code (scaffolding entités, CRUD, tests, configuration) |
| **Docker Compose**      | Services auxiliaires (PostgreSQL, SonarQube)                         |
| **SonarQube**           | Analyse de qualité de code                                           |
| **JaCoCo**              | Couverture de code                                                   |
| **Checkstyle**          | Style de code Java                                                   |
| **ESLint + Prettier**   | Linting et formatage frontend                                        |
| **Husky + lint-staged** | Hooks Git (lint avant commit)                                        |

---

## 4. Architecture du Système

### 4.1 Vue d'ensemble

L'application suit une architecture **monolithique full-stack** avec séparation stricte en couches :

```
┌─────────────────────────────────────────────────┐
│                 Frontend Angular                 │
│  ┌─────────┐ ┌──────────┐ ┌──────────────────┐  │
│  │  Core    │ │  Shared  │ │    Entities      │  │
│  │ (services│ │(components│ │ (CRUD généré)   │  │
│  │  auth,   │ │ layouts, │ │  Project, Sprint │  │
│  │  http)   │ │  i18n)   │ │  Epic, Issue...  │  │
│  └─────────┘ └──────────┘ └──────────────────┘  │
└──────────────────────┬──────────────────────────┘
                       │ REST API (JSON)
                       │ Authentification JWT
┌──────────────────────▼──────────────────────────┐
│              Backend Spring Boot                 │
│  ┌──────────┐ ┌──────────┐ ┌─────────────────┐  │
│  │   Web/   │ │ Service  │ │   Repository    │  │
│  │   REST   │ │ Couche   │ │   (Spring Data  │  │
│  │ Controllers│ métier   │ │    JPA)         │  │
│  │ @PreAuth  │ │ DTOs,   │ │                 │  │
│  │           │ │ Mappers │ │                 │  │
│  └──────────┘ └──────────┘ └────────┬─────────┘  │
│  ┌──────────┐ ┌──────────┐         │            │
│  │ Security │ │  Config  │  ┌──────▼──────────┐ │
│  │ JWT, RBAC│ │ Cache,   │  │    Database     │ │
│  │          │ │ Jackson  │  │  PostgreSQL/H2  │ │
│  └──────────┘ └──────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────┘
```

### 4.2 Flux de Requête

1. L'utilisateur s'authentifie via `/api/authenticate` → obtient un JWT contenant son login, son userId et ses rôles dans le claim `auth`
2. Chaque requête suivante inclut le JWT dans l'en-tête `Authorization: Bearer <token>`
3. Spring Security valide le token via le `JwtAuthenticationConverter` custom qui extrait les autorités du claim `auth`
4. Les `@PreAuthorize` sur chaque endpoint REST vérifient les rôles requis
5. Le contrôleur REST appelle le service métier, qui utilise le repository Spring Data JPA
6. Les DTOs sont mappés depuis/vers les entités via MapStruct
7. La réponse JSON est renvoyée au frontend Angular

### 4.3 Diagramme de Classes (Entités JPA)

```
┌─────────────┐       ┌──────────────┐
│    User     │       │   Project    │
│─────────────│       │──────────────│
│ id          │       │ id           │
│ login       │◄──────│ name         │
│ email       │  owner│ description  │
│ authorities │       │ key (unique) │
└─────────────┘       │ createdAt    │
        ▲             │ owner ───────┼──► User
        │             │ projectMember│──► Set<ProjectMember>
        │             │ sprintses   │──► Set<Sprint>
        │             │ epicses     │──► Set<Epic>
        │             │ issueses    │──► Set<Issue>
        │             └──────────────┘
        │                      │
        │             ┌────────▼────────┐
        └─────────────┤  ProjectMember  │
                      │────────────────│
                      │ id             │
                      │ role           │
                      │ joinedAt       │
                      │ project        │──► Project
                      │ user           │──► User
                      └────────────────┘
                          │
                ┌─────────┼──────────┐
                │         │          │
       ┌────────▼──┐ ┌───▼──────┐ ┌─▼───────────┐
       │  Sprint   │ │   Epic   │ │   Issue      │
       │───────────│ │──────────│ │──────────────│
       │ id        │ │ id       │ │ id           │
       │ name      │ │ title    │ │ title         │
       │ goal      │ │ status   │ │ type (enum)   │
       │ startDate │ │ priority │ │ status (enum) │
       │ endDate   │ │ createdAt│ │ priority      │
       │ status    │ │ project  │ │ createdAt     │
       │ project   │ └──────────┘ │ updatedAt     │
       └───────────┘              │ sprint (opt.) │
                                  │ epic (opt.)   │
                                  │ project       │
                                  │ commentses    │──► Set<Comment>
                                  │ attachmentses │──► Set<Attachment>
│ histories     │──► Set<ActionHistory>
│ assignee      │──► User
                                   └───────┬───────┘
                                           │
                         ┌─────────────────┼──────────────────┬─────────────────────┐
                         │                 │                  │                     │
                ┌────────▼────┐  ┌─────────▼──────┐  ┌───────▼───────────┐  ┌──────▼──────────┐
                │   Comment   │  │   Attachment   │  │  ActionHistory     │  │  Notification   │
                │────────────│  │───────────────│  │───────────────────│  │─────────────────│
                │ id         │  │ id            │  │ id                │  │ id              │
                │ content    │  │ fileName      │  │ action            │  │ userId          │
                │ createdAt  │  │ filePath      │  │ fieldChanged      │  │ message         │
                │ issue      │  │ uploadedAt    │  │ oldValue          │  │ issueId         │
                └────────────┘  │ issue         │  │ newValue          │  │ issueTitle      │
                                └───────────────┘  │ createdAt         │  │ isRead          │
                                                   │ issue             │  │ createdAt       │
                                                   └───────────────────┘  └─────────────────┘
```

### 4.4 Architecture de Sécurité

```
┌─────────────┐     ┌─────────────────┐     ┌──────────────────┐
│  Angular    │────►│  Spring Security │────►│  JwtAuthConverter│
│  (JWT stocké│     │  filterChain()   │     │  claim "auth"    │
│  localStorage)    │  OAuth2 RS       │     │  split(" ") →    │
│             │     │  @PreAuthorize   │     │  GrantedAuthorities
└─────────────┘     └─────────────────┘     └──────────────────┘
                            │
                    ┌───────▼────────┐
                    │   Controller   │
                    │  (ProjectRes,  │
                    │   IssueRes...) │
                    │  @PreAuthorize │
                    └───────┬────────┘
                            │
                    ┌───────▼────────┐
                    │   Service      │
                    │ (ProjectService)│
                    │ checkOwnership │
                    └────────────────┘
```

---

## 5. Fonctionnalités Réalisées

### 5.1 Gestion des Projets (Project)

- CRUD complet avec endpoints REST
- Filtrage par propriétaire : l'ADMIN voit tous les projets, les autres utilisateurs voient uniquement leurs projets
- Gestion d'équipe (membres) : ajout/suppression de membres sur un projet
- Vérification de propriété (ownership) pour les opérations sensibles (suppression, gestion membres)

### 5.2 Gestion des Sprints, Epics et Issues

- CRUD complet pour chaque entité
- Filtrage et pagination avancée via des _criteria_ (IssueQueryService, SprintQueryService, EpicQueryService)
- Types d'issues : Story, Bug, Task, Subtask, Improvement
- Statuts : TODO, IN_PROGRESS, DONE (Issue) ; PLANNED, ACTIVE, COMPLETED (Sprint)
- Priorités : LOW, MEDIUM, HIGH, CRITICAL
- Association d'une issue à un sprint et/ou un epic

### 5.3 Commentaires, Attachements et Historique

- **Commentaires** : création, modification, suppression sur les issues
- **Attachements** : suivi des fichiers joints (nom, chemin, date d'upload)
- **ActionHistory** : audit trail complet des modifications sur une issue (champ modifié, ancienne/v nouvelle valeur)

### 5.4 Sécurité RBAC

- **5 rôles** : ADMIN, USER, PROJET_MANAGER, DEVELOPER, ANONYMOUS
- **Matrice d'accès** par contrôleur avec `@PreAuthorize` :

| Endpoint                      | ADMIN | PROJET_MANAGER | DEVELOPER | USER |
| ----------------------------- | ----- | -------------- | --------- | ---- |
| CRUD Project                  | ✅    | ✅             | ❌        | ❌   |
| CRUD Sprint/Epic              | ✅    | ✅             | ❌        | ❌   |
| CRUD Issue                    | ✅    | ✅             | ✅        | ❌   |
| Assigner une Issue            | ✅    | ✅             | ✅        | ❌   |
| Comment (POST)                | ✅    | ✅             | ✅        | ✅   |
| Comment (PUT/PATCH/DEL)       | ✅    | ✅             | ✅        | ❌   |
| Attachment/ActionHistory CRUD | ✅    | ✅             | ✅        | ❌   |
| GET (lecture)                 | ✅    | ✅             | ✅        | ✅   |
| Gestion utilisateurs/rôles    | ✅    | ❌             | ❌        | ❌   |

### 5.5 Authentification JWT

- Token JWT avec signature HMAC-SHA512
- Claim custom `auth` contenant les rôles (séparés par espace)
- Claim custom `userId` pour identification rapide
- `JwtAuthenticationConverter` custom qui parse le claim `auth` (contournement du comportement Spring par défaut qui cherche dans `scope`)

### 5.6 Interface Utilisateur

- **Sidebar collapsible** (256px ↔ 80px) avec navigation par entité
- **BottomNav** mobile (visible <768px, 4 items : Accueil, Projets, Issues, Settings/Login)
- **Topbar** avec branding, hamburger mobile, dropdowns (admin, compte, langue)
- **Boutons brutals** : offset shadow + hover translate
- **Thème dark/light** : bascule instantanée via `data-theme` + persistance localStorage
- Design system custom (variables CSS dark/light)
- **Internationalisation (i18n)** via ngx-translate
- **Pagination** sur toutes les listes
- **Gestion des erreurs** avec AlertService sur tous les composants CRUD (Project, Sprint, Epic, Issue, Comment, Attachment, ActionHistory)

### 5.7 Données de Seed

- 4 utilisateurs de test : `admin` (ADMIN), `manager` (PROJET_MANAGER), `dev` (DEVELOPER), `user` (USER)
- Utilisateurs chargés via Liquibase (fichiers CSV)

### 5.8 Règles Métier — Ownership des Commentaires

Depuis un commentaire n'appartient qu'à son auteur, seul l'utilisateur ayant créé le commentaire ou un administrateur (PROJET_MANAGER / ADMIN) peut le modifier ou le supprimer. Une méthode utilitaire `checkCommentOwnership()` dans `CommentResource.java` vérifie que l'utilisateur courant est bien l'auteur du commentaire avant toute opération PUT/PATCH/DELETE ; si ce n'est pas le cas, une `BadRequestAlertException("Access denied")` est levée.

### 5.9 Assignation des Issues

L'assignation d'une issue à un utilisateur est une fonctionnalité propre au workflow de gestion de projet. Elle permet d'attribuer une tâche à un développeur ou à un chef de projet.

**Côté backend :**

- Endpoint `PATCH /api/issues/{id}/assign` dans `IssueResource.java` — accepte un body contenant l'ID de l'utilisateur assigné
- Seuls les rôles DEVELOPER, PROJET_MANAGER et ADMIN peuvent assigner une issue
- L'assignee est stocké via une relation `@ManyToOne` entre `Issue` et `User` (colonne `assignee_id`)
- `IssueService.assign()` met à jour l'assignee et sauvegarde l'entité
- À chaque assignation, une notification est automatiquement créée à destination de l'utilisateur assigné
- Endpoint `GET /api/users/assignable` retourne la liste des utilisateurs éligibles (rôles DEVELOPER et PROJET_MANAGER)
- `UserRepository.findAllByAuthorityNames()` permet la récupération par requête JPQL

**Côté frontend :**

- `IssueService.assign()` et `getAssignableUsers()` dans `issue.service.ts`
- Un `<select>` déroulant dans le drawer de détail (`IssueDetailPanel`) permet de choisir l'assignee
- Le chargement de la liste est déclenché par un `effect()` Angular dès que le drawer devient visible

### 5.10 Système de Notification

Un système de notification in-app a été mis en place pour informer les utilisateurs lorsqu'une issue leur est assignée.

**Côté backend :**

- Nouvelle entité `Notification.java` avec les champs : `id`, `userId` (destinataire), `message`, `issueId`, `issueTitle`, `isRead` (booléen), `createdAt`
- DTO et mapper MapStruct : `NotificationDTO.java`, `NotificationMapper.java`
- Repository : `NotificationRepository.java` — méthodes `findByUserIdOrderByCreatedAtDesc()` et `countByUserIdAndIsReadFalse()`
- Service : `NotificationService.java` — création, consultation, marquage comme lu
- Resource REST : `NotificationResource.java`
  - `GET /api/notifications` — liste des notifications de l'utilisateur courant
  - `GET /api/notifications/unread-count` — nombre de notifications non lues
  - `PATCH /api/notifications/{id}/read` — marquer une notification comme lue
- Le message généré est de la forme : _"\[username\] vous a assigné à l'issue #{id} : {title}"_
- Liquibase changelog : `20260702000000_added_entity_Notification.xml`

**Côté frontend :**

- `NotificationService` dans `app/core/util/notification.service.ts` — expose des signaux `unreadCount` et `notifications`
- _Polling_ toutes les 30 secondes pour rafraîchir le compteur et la liste
- Une icône de cloche (`fa-bell`) dans la **topbar** avec un badge rouge affichant le nombre de notifications non lues
- Un dropdown listant les notifications (message + date) avec les non lues surlignées
- Au clic sur une notification : marquage comme lu + navigation vers l'issue concernée

### 5.11 Corrections de Bugs

- **JWT claim `auth` jamais parsé** : ajout d'un `JwtAuthenticationConverter` custom dans `SecurityConfiguration.java`
- **`ProjectService.update()`** : chargement de l'entité existante avant application des modifications pour ne pas écraser `owner_id` par NULL
- **Ownership checks** sur `delete()`, `getMembers()`, `addMember()`, `removeMember()`
- **Colonne `key` renommée en `project_key`** (mot réservé SQL)

---

## 6. Démonstration / Captures d'écran

> **Note :** Les captures d'écran sont à réaliser et à placer dans le dossier `./images/`.

| Figure                                                     | Description                                                                |
| ---------------------------------------------------------- | -------------------------------------------------------------------------- |
| `![Page d'accueil](./images/capture-home.png)`             | Page d'accueil de l'application avec présentation et lien vers les projets |
| `![Liste des projets](./images/capture-liste-projets.png)` | Liste paginée des projets avec actions (voir, éditer, supprimer)           |
| `![Détail d'une issue](./images/capture-detail-issue.png)` | Vue détaillée d'une issue avec commentaires, historique et pièces jointes  |
| `![Sidebar collapsible](./images/capture-sidebar.png)`     | Sidebar de navigation (état déplié/replié)                                 |
| `![Vue mobile](./images/capture-mobile.png)`               | Interface en version mobile avec BottomNav                                 |
| `![Matrice des accès](./images/matrice-acces.png)`         | Tableau récapitulatif des permissions RBAC                                 |

---

## 7. Ce qui Reste à Faire

### 7.1 Priorité Haute

- [x] **Gestion des erreurs frontend unifiée** : appliquer le fix `onSaveError()` avec `AlertService` sur tous les composants (Sprint, Epic, Issue, Comment, Attachment, ActionHistory)
- [x] **Tests des nouveaux rôles** : mettre à jour les `@WithMockUser` dans les tests d'intégration et ajouter des tests pour DEVELOPER et PROJET_MANAGER
- [ ] **Tests frontend** : corriger les erreurs TypeScript préexistantes dans `sprint.spec.ts` pour permettre `ng test`
- [ ] **Tests complets des nouvelles fonctionnalités** : ajouter des tests d'intégration pour l'assignation des issues et les notifications

### 7.2 Priorité Moyenne

- [x] **Thème toggle dark/light** : ajouter un bouton de bascule avec persistance dans localStorage
- [ ] **Persistance de la sidebar** : sauvegarder l'état collapsed/expanded dans localStorage
- [ ] **Pages admin dans la sidebar** : ajouter les routes d'administration dans la navigation

### 7.3 Priorité Faible (Fonctionnalités Futures)

- [ ] **Tableau Kanbin complet** : intégration du drag-and-drop du statut des issues sur le dashboard
- [ ] **Page de liste des notifications** : route dédiée avec historique complet
- [ ] **Notifications par email** : envoi d'un email lors de l'assignation d'une issue

---

## 8. Planning & Avancement

### 8.1 Phase 1 — Initialisation du Projet (Semaine 1-2)

| Tâche                            | Statut     | Commentaire                        |
| -------------------------------- | ---------- | ---------------------------------- |
| Génération JHipster (modèle JDL) | ✅ Terminé | 7 entités déclarées                |
| Configuration BDD et Liquibase   | ✅ Terminé | H2 dev, PostgreSQL prod            |
| Authentification JWT             | ✅ Terminé | Custom converter pour claim `auth` |

### 8.2 Phase 2 — Fonctionnalités Métier (Semaine 3-5)

| Tâche                                 | Statut     | Commentaire                |
| ------------------------------------- | ---------- | -------------------------- |
| CRUD Project + owner + membres        | ✅ Terminé | Filtrage par propriétaire  |
| CRUD Sprint/Epic/Issue                | ✅ Terminé | Avec critères de recherche |
| CRUD Comment/Attachment/ActionHistory | ✅ Terminé | Audit trail fonctionnel    |
| RBAC (@PreAuthorize)                  | ✅ Terminé | Matrice complète déployée  |

### 8.3 Phase 3 — Frontend & Design (Semaine 6-7)

| Tâche                      | Statut     | Commentaire                     |
| -------------------------- | ---------- | ------------------------------- |
| Design system (CSS custom) | ✅ Terminé | Brutalist, dark/light variables |
| Sidebar collapsible        | ✅ Terminé | Desktop 256px/80px              |
| BottomNav mobile           | ✅ Terminé | 4 items responsifs              |
| Topbar + branding          | ✅ Terminé | Logo, favicon, dropdowns        |
| CRUD Angular généré        | ✅ Terminé | Pages liste/détail/update       |

### 8.4 Phase 4 — Tests & Corrections (Semaine 8-9)

| Tâche                                | Statut         | Commentaire                     |
| ------------------------------------ | -------------- | ------------------------------- |
| Correction bug JWT claim `auth`      | ✅ Terminé     | `JwtAuthenticationConverter`    |
| Correction `ProjectService.update()` | ✅ Terminé     | Préservation de `owner_id`      |
| Ownership checks                     | ✅ Terminé     | Méthode `checkOwnership()`      |
| Renommage `key` → `project_key`      | ✅ Terminé     | Conflit SQL résolu              |
| Tests unitaires backend              | 🔄 Partiel     | À compléter pour nouveaux rôles |
| Tests unitaires frontend             | ❌ Non démarré | Vitest à configurer davantage   |

### 8.5 Phase 5 — Nouvelles Fonctionnalités (Semaine 10-11)

| Tâche                          | Statut     | Commentaire                                    |
| ------------------------------ | ---------- | ---------------------------------------------- |
| Ownership des commentaires     | ✅ Terminé | Vérification auteur avant PUT/PATCH/DELETE     |
| Assignation des issues         | ✅ Terminé | PATCH + endpoint assignable users + UI drawer  |
| Système de notification        | ✅ Terminé | Entité + service + polling topbar + icône bell |
| Tests backend (91 tests)       | ✅ Terminé | 0 failure, 0 error                             |
| Correction seed data Liquibase | ✅ Terminé | `context="prod"` pour éviter conflit tests H2  |

### 8.6 Phase 6 — Finalisation (Semaine 12)

| Tâche                    | Statut      | Commentaire                                 |
| ------------------------ | ----------- | ------------------------------------------- |
| Gestion erreurs frontend | ✅ Terminé  | AlertService ajouté sur tous les composants |
| Thème toggle dark/light  | ✅ Terminé  | ThemeService + localStorage + icônes        |
| Documentation et rapport | 🔄 En cours | Présent document                            |

---

## 9. Difficultés Rencontrées

### 9.1 JWT et Claim `auth` non parsé par Spring Security

**Problème :** Le token JWT émis par JHipster stocke les rôles dans un claim custom appelé `auth`. Par défaut, Spring Security OAuth2 Resource Server cherche les rôles dans les claims standards `scope` ou `scp`. En conséquence, toutes les annotations `@PreAuthorize` échouaient silencieusement (retour HTTP 403).

**Solution :** Injection d'un bean `JwtAuthenticationConverter` personnalisé dans `SecurityConfiguration.java` qui lit le claim `auth`, le découpe par espace et crée des `SimpleGrantedAuthority`. Le bean est passé en paramètre à `filterChain()` pour éviter les problèmes de proxy CGLIB.

### 9.2 Écrasement de `owner_id` lors de la Mise à Jour d'un Projet

**Problème :** La méthode `ProjectService.update()` recevait un DTO où `owner` était null (car non soumis dans le formulaire Angular). En passant directement ce DTO à `projectMapper.partialUpdate()`, le champ `owner_id` était écrasé à NULL en base de données.

**Solution :** Chargement préalable de l'entité existante via `projectRepository.findById()` avant d'appliquer les modifications avec `partialUpdate()`. Ainsi, les champs non fournis dans le DTO conservent leur valeur en base.

### 9.3 Mot Réservé SQL — Colonne `key`

**Problème :** La colonne nommée `key` dans l'entité `Project` est un mot réservé SQL sur PostgreSQL (et d'autres SGBD). Liquibase générait des requêtes échouant à cause du conflit.

**Solution :** Renommage de la colonne en `project_key` via l'annotation `@Column(name = "project_key")` et mise à jour du changelog Liquibase correspondant.

### 9.4 Conflit de Z-index Sidebar / Overlay Mobile

**Problème :** Sur mobile, l'overlay semi-transparent qui masque le contenu lors de l'ouverture de la sidebar devait être en dessous de la sidebar (pour que celle-ci reste cliquable) mais au-dessus du contenu.

**Solution :** Ajustement des indices de profondeur CSS : sidebar `z-index: 1040`, overlay `z-index: 1035`. Correction également d'un `overflow-x: hidden` sur le body qui tronquait le bouton toggle.

### 9.5 Communication Hamburger ↔ Sidebar

**Problème :** La sidebar peut être ouverte via le hamburger dans la topbar (mobile) ou via le toggle à l'intérieur de la sidebar (desktop). Ces deux composants sont indépendants et ne partagent pas de state Angular.

**Solution :** Utilisation d'une classe CSS `sidebar-open` sur `document.body` + `MutationObserver` dans la sidebar pour détecter les changements provenant du hamburger. Approche pragmatique sans introduire de service partagé complexe.

---

## 10. Conclusion & Perspectives

### 10.1 Synthèse

Le projet **Gestion de Tâches** a permis de développer une application web complète de gestion de projet agile, couvrant l'ensemble du spectre : modélisation des données, API REST sécurisée, interface utilisateur responsive, tests et déploiement.

La stack technique **Spring Boot 4.0.6 + Angular 21.2.14** s'est révélée parfaitement adaptée : Spring Boot offre une maturité et un écosystème inégalés pour le back-end, tandis qu'Angular structure efficacement le front-end d'une application complexe. L'utilisation de **JHipster** a considérablement accéléré le développement initial (scaffolding, génération de CRUD, configuration sécurité) tout en laissant une totale liberté de personnalisation.

Le système de sécurité **RBAC** avec cinq rôles distincts et une matrice d'accès fine par contrôleur répond aux exigences de confidentialité et de séparation des responsabilités. La gestion de la propriété des projets (ownership) ajoute une couche supplémentaire de contrôle d'accès au niveau métier.

### 10.2 Compétences Acquises

- Architecture d'une application web full-stack avec Spring Boot et Angular
- Sécurisation d'API REST avec JWT et contrôle d'accès basé sur les rôles (RBAC)
- Gestion de migrations de base de données avec Liquibase
- Mapping objet-relationnel avec JPA/Hibernate et MapStruct
- Design de système de permissions personnalisé (ownership)
- Développement d'interface responsive avec Bootstrap 5 et CSS custom
- Utilisation de JHipster comme accélérateur de développement

### 10.3 Perspectives

Les trois fonctionnalités ajoutées lors de cette phase — règles métier sur les commentaires, assignation des issues et notifications — complètent le socle fonctionnel de l'application en répondant à des besoins concrets de workflow agile. L'assignation avec notification automatique crée un circuit de communication entre les membres de l'équipe, tandis que la protection des commentaires renforce la fiabilité des données.

À court terme, les priorités sont la correction des tests frontend et l'ajout de tests d'intégration pour les nouvelles fonctionnalités. À moyen terme, l'ajout d'un tableau Kanban complet, d'une page de notifications dédiée, et d'un système de notifications par email permettrait de rapprocher encore l'outil des standards du marché. À plus long terme, l'application pourrait évoluer vers une architecture microservices pour gérer la montée en charge et permettre le déploiement indépendant des différents modules.

Le socle technique actuel est solide et extensible : l'architecture en couches, le typage strict (TypeScript, Java), les tests et la documentation permettent d'envisager sereinement l'ajout de nouvelles fonctionnalités.

---

## 11. Références & Bibliographie

1. **JHipster Documentation** — _JHipster 9.1.0_ — [https://www.jhipster.tech/documentation-archive/v9.1.0](https://www.jhipster.tech/documentation-archive/v9.1.0)
2. **Spring Boot Reference** — _Spring Boot 4.0.6_ — [https://docs.spring.io/spring-boot/docs/4.0.6/reference/html/](https://docs.spring.io/spring-boot/docs/4.0.6/reference/html/)
3. **Spring Security** — _OAuth2 Resource Server JWT Configuration_ — [https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
4. **Angular Documentation** — _Angular 21_ — [https://angular.dev/docs](https://angular.dev/docs)
5. **Bootstrap 5** — _Documentation officielle_ — [https://getbootstrap.com/docs/5.3/](https://getbootstrap.com/docs/5.3/)
6. **Liquibase** — _Database Changelog_ — [https://docs.liquibase.com/](https://docs.liquibase.com/)
7. **PostgreSQL** — _Documentation 16_ — [https://www.postgresql.org/docs/16/](https://www.postgresql.org/docs/16/)
8. **Cypress** — _End-to-End Testing_ — [https://docs.cypress.io/](https://docs.cypress.io/)
9. **MapStruct** — _Java Bean Mappings_ — [https://mapstruct.org/documentation/](https://mapstruct.org/documentation/)
10. **Hibernate ORM** — _6.x Documentation_ — [https://hibernate.org/orm/documentation/6.x/](https://hibernate.org/orm/documentation/6.x/)
