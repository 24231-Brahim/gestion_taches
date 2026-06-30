# Description du Projet — Gestion de Tâches

## Vue d'ensemble

Application web de **gestion de projet agile** (type Jira) générée avec **JHipster 9.1.0**. Permet de créer des projets, d'y ajouter des membres, de planifier des sprints/epics et de suivre des issues (tâches, bugs, stories).

## Stack Technique

| Couche       | Technologie                     |
|-------------|----------------------------------|
| Backend     | Java 21, Spring Boot 4.0.6      |
| Frontend    | Angular 21.2.14, TypeScript 5.9 |
| BDD         | PostgreSQL (prod), H2 (dev)     |
| ORM         | Spring Data JPA / Hibernate     |
| Auth        | JWT (OAuth2 Resource Server)    |
| Mapping     | MapStruct 1.6.3                 |
| Migrations  | Liquibase                       |
| Cache       | Caffeine (JCache)               |
| Build       | Maven + esbuild                 |
| Tests       | Vitest (front), JUnit (back), Cypress (E2E) |

## Structure des Répertoires (backend)

```
src/main/java/com/gestiontaches/
├── aop/              # Aspects (logging)
├── config/           # Configuration Spring (Security, Liquibase, Jackson, Cache...)
├── domain/           # Entités JPA
├── management/       # Monitoring / Actuator
├── repository/       # Spring Data JPA Repositories
├── security/         # JWT, AuthoritiesConstants, SecurityUtils
├── service/          # Logique métier + DTOs + Mappers
│   ├── dto/          # Data Transfer Objects
│   ├── mapper/       # MapStruct mappers
│   └── *.java        # Services
└── web/rest/         # Contrôleurs REST
```

## Structure des Répertoires (frontend)

```
src/main/webapp/app/
├── account/          # Login, Register, Settings, Password
├── admin/            # User management, Health, Logs, Metrics, etc.
├── config/           # FontAwesome icons, application config
├── core/             # Services globaux (auth, theme, alert, request)
├── entities/         # CRUD généré pour chaque entité
│   ├── project/      #   Project (liste, update, detail, service, model)
│   ├── sprint/
│   ├── epic/
│   ├── issue/
│   ├── comment/
│   ├── attachment/
│   └── action-history/
├── home/             # Page d'accueil
├── layouts/          # Topbar, Sidebar, BottomNav, Footer, Main
├── login/            # Page de login
└── shared/           # Composants partagés (alert, language, etc.)
```

## Entités du Domaine

### Project
- Table racine. Contient sprints, epics, issues.
- Champs : `id`, `name`, `description`, `project_key` (unique), `createdAt`
- Relations : `@ManyToOne User owner` (propriétaire), `@OneToMany Set<ProjectMember> projectMembers`
- Visibilité filtrée par propriétaire (ADMIN voit tout, les autres voient leurs projets)

### ProjectMember
- Remplace un `@ManyToMany` entre Project et User par une entité complète.
- Table `project_member`
- Champs : `id`, `project` (ManyToOne), `user` (ManyToOne), `role` (String), `joinedAt` (Instant)
- Contrainte d'unicité : `(project_id, user_id)`
- Endpoints : `GET/POST/PATCH/DELETE /api/projects/{id}/members/{userId}`

### Sprint
- Itération de développement dans un projet.
- Champs : `id`, `name`, `goal`, `startDate`, `endDate`, `status` (PLANNED/ACTIVE/COMPLETED/CANCELLED)
- Relation : `@ManyToOne Project`

### Epic
- Fonctionnalité transverse.
- Champs : `id`, `title`, `description`, `status` (TODO/IN_PROGRESS/DONE/CANCELLED), `priority`
- Relation : `@ManyToOne Project`

### Issue
- Unité de travail atomique.
- Champs : `id`, `title`, `description`, `type` (STORY/BUG/TASK/SUBTASK/IMPROVEMENT), `status` (BACKLOG/TODO/IN_PROGRESS/IN_REVIEW/DONE/CANCELLED), `priority` (LOWEST/LOW/MEDIUM/HIGH/HIGHEST)
- Relations : `@ManyToOne Project` (obligatoire), `@ManyToOne Sprint` (optionnel), `@ManyToOne Epic` (optionnel)

### Comment
- Commentaire texte attaché à une issue.
- Champs : `content`, `createdAt`
- Relation : `@ManyToOne Issue`

### Attachment
- Fichier joint à une issue.
- Champs : `fileName`, `filePath`, `uploadedAt`
- Relation : `@ManyToOne Issue`

### ActionHistory
- Audit trail des modifications d'une issue.
- Champs : `action`, `fieldChanged`, `oldValue`, `newValue`, `createdAt`
- Relation : `@ManyToOne Issue`

### User (géré par JHipster)
- Entité gérée automatiquement par JHipster (table `jhi_user`).
- Ne pas modifier. On l'utilise comme cible de `@ManyToOne`.
- Champs : `id`, `login`, `password` (hashé), `firstName`, `lastName`, `email`, `authorities`

## Architecture en Couches (Backend)

```
Controller (web/rest/)
    ↓ Appelle
Service (service/)
    ↓ Utilise
Repository (repository/)  ───  Database
    ↑
Mapper (service/mapper/)  ───  DTO (service/dto/)
```

### Règles strictes
- **Controller** : ne contient aucune logique métier. Valide les entrées, appelle le service, retourne ResponseEntity.
- **Service** : logique métier, `@Transactional`. Orchestre les repositories et les mappers.
- **Repository** : Spring Data JPA interfaces. Méthodes dérivées du nom (ex: `findByOwnerLogin`).
- **Mapper** : MapStruct. Convertit Entity ↔ DTO. Les `@Mapping` plate les relations (ex: `projectId` → `project.id`).
- **DTO** : Classes sérialisables (pas d'annotations JPA). Utilisées dans l'API REST.

## Sécurité

### Rôles (AuthoritiesConstants.java)
| Rôle              | Constante           |
|-------------------|---------------------|
| ADMIN             | `ROLE_ADMIN`        |
| USER              | `ROLE_USER`         |
| DEVELOPER         | `ROLE_DEVELOPER`    |
| PROJET_MANAGER    | `ROLE_PROJET_MANAGER` |
| ANONYMOUS        | `ROLE_ANONYMOUS`    |

### Matrice d'accès (@PreAuthorize)

| Entité                        | POST/PUT/PATCH/DELETE               | GET |
|-------------------------------|-------------------------------------|-----|
| Project, Sprint, Epic         | ADMIN, PROJET_MANAGER               | Tous |
| Issue                         | ADMIN, PROJET_MANAGER, DEVELOPER    | Tous |
| Comment (POST)                | ADMIN, PROJET_MANAGER, DEVELOPER, USER | Tous |
| Comment (PUT/PATCH/DELETE)    | ADMIN, PROJET_MANAGER, DEVELOPER    | Tous |
| Attachment, ActionHistory     | ADMIN, PROJET_MANAGER, DEVELOPER    | Tous |
| UserResource                  | ADMIN uniquement                    | ADMIN |

### Authentification JWT
- Token stocké dans `localStorage` côté Angular.
- Claim custom `auth` contient les rôles (ex: `"ROLE_ADMIN ROLE_USER"`).
- Claim custom `userId` pour identification rapide.
- `JwtAuthenticationConverter` custom dans `SecurityConfiguration.java` parse le claim `auth` (Spring cherche par défaut dans `scope`/`scp`).

### Ownership (Project)
- Les opérations de modification (`delete`, `getMembers`, `addMember`, `removeMember`) vérifient que l'utilisateur courant est le `owner` du projet.
- `checkOwnership()` compare `project.owner.login` avec `SecurityUtils.getCurrentUserLogin()`.
- ADMIN peut outrepasser cette vérification.
- `findAll()` / `findOne()` filtrent les projets par propriétaire (sauf ADMIN).

## Design System & Thème

### "Softened Brutalism"
- Bords durs (`border-radius: 0`), bordures épaisses (2-3px solid).
- Ombres offset flat (pas de blur), pas de dégradés.
- Polices : **Audiowide** (titres, uppercase) + **JetBrains Mono** (UI, monospace).

### Fichiers CSS clés
- `content/scss/design-system.scss` : design tokens (CSS custom properties `--color-*`)
- `content/scss/_bootstrap-variables.scss` : surcharge variables Bootstrap (avant compilation)
- `content/scss/global.scss` : réécriture des classes Bootstrap avec `var(--color-*)`
- `content/scss/vendor.scss` : ordre d'import (design-system → bootstrap-variables → bootstrap)

### Thème dark/light
- Attribut `data-theme` sur `<html>` (valeurs : `"dark"` / `"light"`).
- `ThemeService` (signal Angular) : toggle + persist dans `localStorage`.
- Changement instantané (pas de re-rendu) car tout repose sur `var(--color-*)`.

## Layout

- **Topbar** : sticky, branding + hamburger mobile + dropdowns (admin, compte, langue)
- **Sidebar** : collapsible 256px ↔ 80px, overlay mobile, navigation par entité
- **BottomNav** : visible <768px, 4 items (Accueil, Projects, Issues, Settings/Login)
- **Main** : layout shell combinant topbar + sidebar + content + footer + bottom-nav
- Communication hamburger ↔ sidebar : via `document.body.classList` + `MutationObserver`

## Frontend — Patterns Angular

### Signaux (Signals)
- Utilisation de `signal()` / `computed()` pour la réactivité (ex: `isSaving`, `projects`).
- Pas de `Zone.js`-heavy patterns, on privilégie les signaux.

### Services
- `ProjectService` (étend `ProjectsService`) : CRUD + `getMembers()`, `addMember()`, `removeMember()`.
- `ProjectsService` : `httpResource` pour la liste projet avec params réactifs.
- `AlertService` : ajout d'alertes (type, message).
- `ThemeService` : gestion du thème dark/light.

### Ressource HTTP
`httpResource()` est utilisé pour les listes en lecture. Les mutations (POST/PUT/DELETE) utilisent `http` classique + `Observable`.

### Internationalisation (i18n)
ngx-translate : fichiers `fr.json` / `en.json`. Tous les textes UI passent par `translate` pipe ou directive.

## Configuration et Démarrage

### Prérequis
- Java 21
- Node.js >= 24.16.0
- Docker (optionnel, pour PostgreSQL)

### Commandes principales
```bash
npm run backend:start     # Démarre le backend Spring Boot
npm run start             # Démarre le frontend Angular (dev)
npm run watch             # Démarre les deux simultanément
npm test                  # Tests frontend (Vitest)
npm run backend:unit:test # Tests backend (JUnit)
npm run e2e               # Tests E2E (Cypress)
npm run lint              # Lint ESLint
npm run build             # Build production
```

## Utilisateurs de Test (seed Liquibase)

| Login    | Password | Rôles                          |
|----------|----------|--------------------------------|
| `admin`  | `admin`  | ROLE_ADMIN, ROLE_USER          |
| `user`   | `user`   | ROLE_USER                      |
| `manager`| `user`   | ROLE_PROJET_MANAGER, ROLE_USER |
| `dev`    | `user`   | ROLE_DEVELOPER, ROLE_USER      |

## Points d'Attention

1. **Claim JWT `auth`** : Spring Security ne le parse pas par défaut. Le bean `JwtAuthenticationConverter` dans `SecurityConfiguration.java` est indispensable.
2. **`ProjectService.update()`** : charge l'entité existante avant `partialUpdate()` pour ne pas écraser `owner_id` par NULL.
3. **Colonne `project_key`** : renommée car `key` est un mot réservé SQL.
4. **MapStruct** : les mappers sont des interfaces compilées à la compilation (pas de reflection). Toute modification de champ dans une entité nécessite une mise à jour du mapper.
5. **Liquibase** : toute modification de schéma BDD passe par un nouveau changelog (ne jamais modifier un existant déjà appliqué en prod).

## Fichiers Documentaires

- `NOTE.md` — Documentation technique complète (architecture, RBAC, design system, todo)
- `TODO.md` — État d'avancement (fait / à faire)
- `RAPPORT.md` — Rapport de stage détaillé
- `docs/fiche-classes.md` — Diagramme de classes UML (Mermaid)
- `frontend-design-system.md` — Spécifications du design system
