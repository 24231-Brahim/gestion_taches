# Notes d'implémentation

## Design System — Architecture CSS et Thèmes

### Vue d'ensemble

L'application utilise une approche **"Softened Brutalism"** avec un système de design tokens via CSS custom properties. Le changement de thème (sombre/clair) est instantané car il repose uniquement sur le changement d'attribut `data-theme` sur `<html>`, sans re-rendu Angular.

### Fichiers clés

| Fichier                                  | Rôle                                                                   |
| ---------------------------------------- | ---------------------------------------------------------------------- |
| `index.html`                             | Définit `data-theme="light"` sur `<html>`                              |
| `content/scss/design-system.scss`        | Design tokens : couleurs, ombres, espacements, typographie             |
| `content/scss/_bootstrap-variables.scss` | Surcharge les variables SCSS Bootstrap avant compilation               |
| `content/scss/vendor.scss`               | Import Bootstrap avec les surcharges                                   |
| `content/scss/global.scss`               | Ré-écrit les classes Bootstrap avec des `var(--color-*)`               |
| `app/core/util/theme.service.ts`         | Service Angular qui toggle `data-theme` et persist dans `localStorage` |
| `app/layouts/navbar/navbar.ts`           | Bouton de bascule thème dans la barre de navigation                    |

### Flux de rendu

```
index.html (data-theme="light")
       │
       ▼
design-system.scss  ─── définit ───>  --color-bg, --color-text, ...
       │                                      │
       ▼                                      ▼
_bootstrap-variables.scss  ─── compile ───>  Bootstrap CSS (couleurs fixes)
       │
       ▼
global.scss  ─── surcharge ───>  .card, .btn, .table, ... avec var(--color-*)
       │
       ▼
Composants (navbar.scss, sidebar.scss, ...)  ─── utilisent aussi var(--color-*)
       │
       ▼
ThemeService.toggle()  ─── change data-theme sur <html> ───>  toutes les var() se mettent à jour
```

### Design tokens (`design-system.scss`)

Deux blocs CSS :

```scss
:root, [data-theme='light'] {
  --color-bg: #0f1419;
  --color-text: #dfe3ea;
  --color-primary: #97cbff;
  --color-primary-container: #25a7fd;
  --shadow-brutal: 4px 4px 0 var(--color-primary);
  --color-surface: #0f1419;
  --color-surface-container: #1b2025;
  --color-muted: #6a8fac;
  ...
}

[data-theme='light'] {
  --color-bg: #dff0ff;
  --color-text: #0a0a0f;
  --color-primary: #0077cc;
  --color-surface: #ffffff;
  --color-surface-container: #ffffff;
  --color-muted: #47657d;
  ...
}
```

Quand `data-theme` change, toutes les `var(--color-*)` sont recalculées par le navigateur instantanément. Le thème par défaut est clair.

### Bootstrap — pourquoi une double couche

1. `_bootstrap-variables.scss` surcharge les variables SCSS (ex: `$body-bg`, `$card-bg`) **avant compilation**. Ces valeurs sont fixes dans le CSS final.
2. `global.scss` **ré-écrit** les classes Bootstrap générées avec des `var(--color-*)` pour les rendre dynamiques.

Exemple pour `.form-control` :

```scss
// Bootstrap généré (fixe) :
.form-control {
  background-color: #0f1419;
  color: #dfe3ea;
}

// Surcharge dans global.scss (dynamique) :
.form-control {
  background-color: var(--color-surface);
  color: var(--color-on-surface);
  border: var(--border-width-thin) solid var(--color-outline);
}
```

Classes Bootstrap surchargées dans `global.scss` : `.btn`, `.card`, `.table`, `.modal-content`, `.modal-header`, `.modal-footer`, `.form-control`, `.alert-*`, `.badge`, `.form-check-input`, `ngb-pagination`, `ngb-progressbar`, `.dropdown-menu`.

### Composants — utilisation des tokens

Chaque composant utilise exclusivement `var(--color-*)` :

```scss
// navbar.scss
.topbar {
  background-color: var(--color-surface);
  border-bottom: 2px solid var(--color-primary-container);
}
.topbar-icon-btn {
  color: var(--color-on-surface);
  &:hover {
    color: var(--color-primary);
    background-color: var(--color-surface-container-high);
  }
}

// sidebar.scss
.sidebar {
  background: var(--color-surface);
  border-right: 2px solid var(--color-outline-variant);
}
.sidebar-item {
  color: var(--color-on-surface);
  &.active {
    color: var(--color-primary-container);
    background-color: color-mix(in srgb, var(--color-primary-container) 12%, transparent);
  }
}
```

Aucun composant n'utilise de couleur en dur — tout passe par les design tokens.

### ThemeService (`app/core/util/theme.service.ts`)

```typescript
export type Theme = 'dark' | 'light';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly theme = signal<Theme>(this.loadInitialTheme());

  toggle(): void {
    const newTheme = this.theme() === 'dark' ? 'light' : 'dark';
    this.theme.set(newTheme);
    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
  }

  private loadInitialTheme(): Theme {
    const stored = localStorage.getItem('theme');
    return stored === 'dark' || stored === 'light' ? stored : 'dark';
  }
}
```

Le service est injecté dans `Navbar` et utilisé dans le template pour le bouton de bascule (icône soleil en mode sombre, lune en mode clair).

### Icônes FontAwesome ajoutées

- `faSun` — affichée quand le thème est sombre (pour suggérer "passer en clair")
- `faMoon` — affichée quand le thème est clair (pour suggérer "passer en sombre")

Déclarées dans `app/config/font-awesome-icons.ts` et enregistrées via `iconLibrary.addIcons(...fontAwesomeIcons)` dans `app.ts`.

---

## Sécurité RBAC et Propriété des Projets

## Rôles disponibles (dans `AuthoritiesConstants.java`)

| Constante        | Valeur                |
| ---------------- | --------------------- |
| `ADMIN`          | `ROLE_ADMIN`          |
| `USER`           | `ROLE_USER`           |
| `ANONYMOUS`      | `ROLE_ANONYMOUS`      |
| `DEVELOPER`      | `ROLE_DEVELOPER`      |
| `PROJET_MANAGER` | `ROLE_PROJET_MANAGER` |

## Matrice d'accès par contrôleur

### Project, Sprint, Epic

| Méthode                     | Rôles autorisés       |
| --------------------------- | --------------------- |
| POST / PUT / PATCH / DELETE | ADMIN, PROJET_MANAGER |
| GET                         | Tous les authentifiés |

### Issue

| Méthode                     | Rôles autorisés                  |
| --------------------------- | -------------------------------- |
| POST / PUT / PATCH / DELETE | ADMIN, PROJET_MANAGER, DEVELOPER |
| GET                         | Tous les authentifiés            |

### Comment

| Méthode              | Rôles autorisés                        |
| -------------------- | -------------------------------------- |
| POST                 | ADMIN, PROJET_MANAGER, DEVELOPER, USER |
| PUT / PATCH / DELETE | ADMIN, PROJET_MANAGER, DEVELOPER       |
| GET                  | Tous les authentifiés                  |

### Attachment

| Méthode                     | Rôles autorisés                  |
| --------------------------- | -------------------------------- |
| POST / PUT / PATCH / DELETE | ADMIN, PROJET_MANAGER, DEVELOPER |
| GET                         | Tous les authentifiés            |

### ActionHistory

| Méthode                     | Rôles autorisés                  |
| --------------------------- | -------------------------------- |
| POST / PUT / PATCH / DELETE | ADMIN, PROJET_MANAGER, DEVELOPER |
| GET                         | Tous les authentifiés            |

### UserResource (gestion des utilisateurs)

| Méthode | Rôles autorisés  |
| ------- | ---------------- |
| Toutes  | ADMIN uniquement |

### AuthorityResource (gestion des rôles)

| Méthode | Rôles autorisés  |
| ------- | ---------------- |
| Toutes  | ADMIN uniquement |

## Visibilité des Projets

La visibilité est gérée dans `ProjectService.java` au niveau des méthodes `findAll()` et `findOne()`.

### Règles

| Rôle             | findAll()                      | findOne(id)                                    | delete/getMembers/addMember/removeMember |
| ---------------- | ------------------------------ | ---------------------------------------------- | ---------------------------------------- |
| `ADMIN`          | Tous les projets               | Aucun filtre                                   | Aucune restriction                       |
| `PROJET_MANAGER` | Uniquement ses propres projets | Vérifie que `owner.login == currentUser.login` | Vérifie que le projet lui appartient     |
| `DEVELOPER`      | Uniquement ses propres projets | Vérifie que `owner.login == currentUser.login` | N'a pas accès à ces endpoints            |

### Implémentation

**`findAll()`** — Si l'utilisateur a le rôle `ADMIN`, la méthode retourne tous les projets via `projectRepository.findAll(pageable)`. Sinon, elle récupère le login de l'utilisateur courant via `SecurityUtils.getCurrentUserLogin()` et appelle `projectRepository.findByOwnerLogin(login, pageable)` qui génère la requête JPQL `WHERE project.owner.login = ?1`.

**`findOne(id)`** — Même principe : `ADMIN` voit n'importe quel projet par ID (`findById(id)`). Les autres rôles doivent correspondre au propriétaire via `findByIdAndOwnerLogin(id, login)`.

### Repository (ProjectRepository.java)

```java
Page<Project> findByOwnerLogin(String login, Pageable pageable);

Optional<Project> findByIdAndOwnerLogin(Long id, String login);
```

Spring Data JPA résout `findByOwnerLogin` en traversant la relation `Project.owner.login` (équivalent à `findByOwner_Login`).

### Méthodes de vérification (checkOwnership)

Les opérations d'écriture (`delete`, `getMembers`, `addMember`, `removeMember`) utilisent une méthode privée `checkOwnership()` qui :

1. Charge le projet depuis la BDD
2. Si l'utilisateur n'est pas `ADMIN`, compare `project.owner.login` avec `SecurityUtils.getCurrentUserLogin()`
3. Lève une exception `"Access denied: you do not own this project"` en cas de mismatch

```java
private void checkOwnership(Project project) {
    if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(...);
        if (!project.getOwner().getLogin().equals(login)) {
            throw new RuntimeException("Access denied: you do not own this project");
        }
    }
}
```

### Nota

Le rôle `DEVELOPER` n'apparaît pas dans la matrice d'accès à `ProjectResource` car il n'a pas le droit de créer/modifier/supprimer des projets (seulement `ADMIN` et `PROJET_MANAGER`). `DEVELOPER` peut voir les projets via `GET` (authentifié) mais ne peut pas créer ou gérer les membres.

## Gestion d'Équipe (Project)

La relation `@ManyToMany Set<User> members` a été remplacée par une entité `ProjectMember` complète avec table `project_member`.

| Méthode | Path                                  | Rôles autorisés       | Retour                      |
| ------- | ------------------------------------- | --------------------- | --------------------------- |
| GET     | `/api/projects/{id}/members`          | ADMIN, PROJET_MANAGER | `Set<ProjectMemberDTO>`     |
| POST    | `/api/projects/{id}/members/{userId}` | ADMIN, PROJET_MANAGER | Ajoute un `ProjectMember`   |
| PATCH   | `/api/projects/{id}/members/{userId}` | ADMIN, PROJET_MANAGER | Modifie le rôle             |
| DELETE  | `/api/projects/{id}/members/{userId}` | ADMIN, PROJET_MANAGER | Supprime le `ProjectMember` |

### Entité ProjectMember

| Champ      | Type      | Description                                  |
| ---------- | --------- | -------------------------------------------- |
| `id`       | `Long`    | Identifiant unique                           |
| `project`  | `Project` | Projet associé                               |
| `user`     | `User`    | Membre de l'équipe                           |
| `role`     | `String`  | Rôle dans le projet (`MEMBER`, `LEAD`, etc.) |
| `joinedAt` | `Instant` | Date d'ajout au projet                       |

Contrainte d'unicité : `(project_id, user_id)`.

## Utilisateurs de test (seed Liquibase)

| Login     | Mot de passe | Rôles                          |
| --------- | ------------ | ------------------------------ |
| `admin`   | `admin`      | ROLE_ADMIN, ROLE_USER          |
| `user`    | `user`       | ROLE_USER                      |
| `manager` | `user`       | ROLE_PROJET_MANAGER, ROLE_USER |
| `dev`     | `user`       | ROLE_DEVELOPER, ROLE_USER      |

## JWT — Correction du claim `auth`

Le token JWT stocke les rôles dans un claim custom `auth` (défini dans `SecurityUtils.AUTHORITIES_CLAIM`). Sans configuration, Spring Security cherche les rôles dans `scope`/`scp` → **toutes les `@PreAuthorize` échouent**.

**Fix :** `SecurityConfiguration.java` expose un bean `JwtAuthenticationConverter` avec un _convertisseur custom_ en lambda qui :

- Lit les autorités depuis le claim `AUTHORITIES_CLAIM = "auth"`
- Découpe la valeur (ex: `"ROLE_ADMIN ROLE_USER"`) par espace via `auth.split(" ")`
- Crée un `SimpleGrantedAuthority` pour chaque rôle sans préfixe

Le bean est **injecté dans `filterChain()`** via paramètre de méthode pour éviter les problèmes de proxy CGLIB inter-beans.

## Vérification de propriété (Project)

Toutes les opérations de modification sur un projet (`delete`, `getMembers`, `addMember`, `removeMember`) vérifient que l'utilisateur courant en est le propriétaire (`owner`). Seul `ADMIN` peut outrepasser cette règle.

## Fichiers modifiés

- `src/main/java/com/gestiontaches/security/AuthoritiesConstants.java` — ajout des constantes DEVELOPER et PROJET_MANAGER
- `src/main/java/com/gestiontaches/domain/Project.java` — ajout owner + members (puis remplacé par `@OneToMany projectMembers`)
- `src/main/java/com/gestiontaches/domain/ProjectMember.java` — nouvelle entité (id, project, user, role, joinedAt) + `@Table(uniqueConstraints)`
- `src/main/java/com/gestiontaches/repository/ProjectMemberRepository.java` — nouveau repository + suppression code mort
- `src/main/java/com/gestiontaches/service/dto/ProjectDTO.java` — ajout ownerId, ownerLogin, projectMembers (Set<ProjectMemberDTO>)
- `src/main/java/com/gestiontaches/service/dto/ProjectMemberDTO.java` — nouveau DTO
- `src/main/java/com/gestiontaches/service/mapper/ProjectMapper.java` — mapping des nouveaux champs
- `src/main/java/com/gestiontaches/service/mapper/ProjectMemberMapper.java` — nouveau mapper
- `src/main/java/com/gestiontaches/repository/ProjectRepository.java` — findByOwnerLogin
- `src/main/java/com/gestiontaches/service/ProjectService.java` — set owner, filtre findAll/findOne, gestion team via ProjectMemberRepository, ownership checks, update() preserves owner, updateMemberRole
- `src/main/java/com/gestiontaches/web/rest/ProjectResource.java` — endpoints team (retourne `Set<ProjectMemberDTO>`), GET filtré + PATCH /members/{userId}
- `src/main/java/com/gestiontaches/config/SecurityConfiguration.java` — JwtAuthenticationConverter bean
- `src/main/resources/config/liquibase/changelog/20260625000000_added_entity_Project_owner.xml` — owner_id + table project_members
- `src/main/resources/config/liquibase/changelog/20260629000000_added_entity_ProjectMember.xml` — table project_member avec migration des données
- `src/main/webapp/app/entities/project/project.model.ts` — ajout `IProjectMember` + `projectMembers`
- `src/main/webapp/app/entities/project/service/project.service.ts` — ajout `getMembers()`, `addMember()`, `removeMember()`, `updateMemberRole()`
- `src/main/webapp/app/entities/project/detail/project-detail.ts` — gestion des membres (signals, CRUD)
- `src/main/webapp/app/entities/project/detail/project-detail.html` — section "Membres" UI complète
- `src/main/webapp/i18n/fr/project.json` — clés de traduction membres
- `src/main/webapp/i18n/en/project.json` — clés de traduction membres
- `src/test/java/com/gestiontaches/web/rest/ProjectResourceIT.java` — @WithMockUser avec ROLE_ADMIN

---

## État d'avancement des fonctionnalités (Juin 2026)

### ✅ Implémenté

- **Thème toggle dark/light** : `ThemeService` avec signal Angular, persisté dans `localStorage`, icônes `faSun`/`faMoon` dans la navbar
- **Design system complet** : `design-system.scss`, `global.scss`, composants utilisant `var(--color-*)` exclusivement
- **Layout responsive** : Sidebar collapsible (256px↔80px), BottomNav mobile (<768px), Topbar sticky
- **RBAC** : 5 rôles avec `@PreAuthorize` sur tous les endpoints
- **Projet owner/membres** : auto-assignation owner, filtrage par propriétaire, endpoints de gestion d'équipe
- **JWT custom converter** : parse le claim `auth` pour Spring Security OAuth2 RS
- **Assignation des issues** : endpoint `PATCH /api/issues/{id}/assign`, liste des utilisateurs assignables, sélecteur dans le drawer
- **Système de notification** : entité `Notification`, service + polling 30s, icône cloche avec badge dans la topbar, dropdown
- **Ownership des commentaires** : vérification auteur avant PUT/PATCH/DELETE
- **Sprint refactoring** : board kanban 6 colonnes, planning backlog↔sprint, burndown chart SVG
- **Issue refactoring** : onglets Backlog/Board, drawer latéral avec sections comments/attachments/history intégrées
- **Epic roadmap** : timeline horizontale avec barres de progression, filtre par statut

### ❌ Non implémenté (reste à faire)

- **Tests des nouveaux rôles** : `@WithMockUser` obsolètes dans les IT, tests manquants pour DEVELOPER et PROJET_MANAGER
- **Pages admin dans la sidebar** : User Management, Metrics, Health, Configuration, Logs, API, H2 Console absents
- **Persistance sidebar** : l'état collapsed n'est pas sauvegardé dans `localStorage`

### ✅ Réalisé (après la rédaction initiale)

- **Table `ProjectMember`** : remplacement du `@ManyToMany Set<User> members` par une entité `ProjectMember` complète (id, project, user, role, joinedAt) avec `ProjectMemberRepository`, `ProjectMemberDTO`, `ProjectMemberMapper`, migration Liquibase de la table `project_members` vers `project_member`.
- **Unique constraint JPA** : ajout de `@Table(uniqueConstraints)` sur `ProjectMember.java` pour matcher la contrainte Liquibase.
- **Nettoyage repository** : suppression de la méthode inutilisée `deleteByProjectIdAndUserId()`.
- **Optimisation `addMember()`** : chargement unique du projet au lieu de deux requêtes.
- **Endpoint PATCH /members/{userId}** : modification du rôle d'un membre avec `checkOwnership`.
- **Frontend — Section Membres** : tableau liste + ajout + inline edit rôle + suppression dans `project-detail`, avec conditionnement des boutons par `*jhiHasAnyAuthority`, refetch automatique après chaque mutation, clés i18n fr/en.
- **Règle métier — Ownership commentaire** : `CommentResource.checkCommentOwnership()` vérifie que seul l'auteur (ou ADMIN/PM) peut modifier/supprimer.
- **Assignation des issues** : `PATCH /api/issues/{id}/assign` avec validation rôle, `GET /api/users/assignable`, sélecteur dans le drawer.
- **Système de notification** : entité `Notification`, service backend, polling frontend 30s, icône `faBell` avec badge dans topbar, dropdown liste, marquage lu + navigation.
- **Sprint refactoring** : 3 onglets (Board/Planning/Burndown), `SprintActiveBoard` kanban drag-drop, `SprintBacklogPlanning`, `SprintBurndownChart` SVG.
- **Comment/Attachment/ActionHistory endpoints** : `GET /by-issue/{issueId}` pour chaque entité, upload multipart files.
- **Issue drawer** : remplacement des placeholders par `IssueCommentList`, `IssueAttachmentList`, `IssueActivityFeed`.

### Dashboard d'accueil

- **DashboardComponent** : 4 appels `httpResource` (projects, issues, doneCount, memberCount), 6 KPI cards computed via signals, graphiques SVG/HTML purs (barres progression + donut), timeline (10 dernières activity), quick actions (4 boutons).
- **Composants standalone** : `KpiCardComponent`, `DashboardChartsComponent`, `DashboardListsComponent`, `DashboardTimelineComponent`, `DashboardQuickActionsComponent` — tous en `ChangeDetectionStrategy.OnPush`.
- **Seeds Liquibase** : 7 fichiers CSV (project, sprint, epic, issue, comment, project_member, action_history) chargés via `20260630000000_added_seed_data.xml`.

---

## Issue — Refactoring complet (Jira-like, Juillet 2026)

### Architecture

```
issue/
├── list/issue.ts/.html          → Onglets Backlog (table) / Board (kanban), search
├── kanban/issue-kanban-board.ts/.html → Colonnes drag-and-drop par IssueStatus
├── detail/issue-detail-panel.ts/.html  → Drawer latéral avec infos + inline edit
├── issue-helper.ts              → Maps couleur/icône/label (IssueType, Priority, Status)
├── issue.model.ts               + assignee (UserDTO)
└── service/issue.service.ts     (inchangé)
```

### Composants

| Composant          | Signal Inputs / Outputs             | Description                                       |
| ------------------ | ----------------------------------- | ------------------------------------------------- |
| `Issue (list)`     | —                                   | Onglets Backlog/Board, search, pagination, drawer |
| `IssueKanbanBoard` | `issues: IIssue[]`, `selectIssue`   | 4 colonnes (TODO, IN_PROGRESS, DONE, CANCELLED)   |
| `IssueDetailPanel` | `issue: IIssue`, `visible: boolean` | Drawer overlay + sidebar + sections               |

### Drawer (IssueDetailPanel)

Layout : overlay → drawer flex-row (main 2/3 + sidebar 1/3).

- Main : header (type icon + badge + title) → description (textarea inline edit) → metadata sections (sprint, epic, comments placeholder, attachments placeholder, history placeholder).
- Sidebar : status (select → `partialUpdate`), assignee (avatar + login), priority (icon + label), type (icon + label), dates (createdAt, updatedAt), project.

### Kanban (IssueKanbanBoard)

- 4 colonnes générées depuis `Object.keys(IssueStatus)`.
- Drag-and-drop natif HTML5 (`dragstart`, `dragover`, `drop`, `dragend`).
- `partialUpdate` optimiste sur drop → rollback message via `AlertService`.
- Cartes : type icon + id + title + priority icon + assignee initials.

### Epic — Roadmap view

```
epic/
├── roadmap/epic-roadmap.ts/.html  → Timeline horizontale
├── epic.model.ts                  + startDate, endDate
├── epic.routes.ts                 → default → EpicRoadmap
└── service/epic.service.ts       + LocalDate conversion
```

- Roadmap ligne par epic : barre de progression (calculée via doneIssues/totalIssues), dates (start → end), status dot + label.
- Filtre client-side par statut (select).
- Légende en bas : statuts avec `keyvalue` pipe + `statusColors`.
- Responsive : flex-column sur mobile (<768px).

### Backend — Nouveaux champs

#### Issue.assignee

| Fichier            | Changement                                                                |
| ------------------ | ------------------------------------------------------------------------- |
| `Issue.java`       | `@ManyToOne User assignee`                                                |
| `IssueDTO.java`    | `UserDTO assignee`                                                        |
| `IssueMapper.java` | `@Mapping(target = "assignee", source = "assignee")` + `toDtoUserLogin()` |

#### Epic.startDate / endDate

| Fichier        | Changement                                 |
| -------------- | ------------------------------------------ |
| `Epic.java`    | `LocalDate startDate`, `LocalDate endDate` |
| `EpicDTO.java` | `LocalDate startDate`, `LocalDate endDate` |

### Notes

- Les sections Comments, Attachments, History dans le drawer sont intégrées avec des composants réels : `IssueCommentList`, `IssueAttachmentList`, `IssueActivityFeed` — CRUD complet, upload/download fichiers, historique chronologique.
- Le drawer utilise `FormsModule` + `[(ngModel)]` pour l'édition inline de la description. La sauvegarde se fait via `partialUpdate` à la perte de focus (`(blur)`).
- Les icônes FontAwesome ont été ajoutées de manière atomique dans `font-awesome-icons.ts` pour le tree-shaking.
- Les clés i18n suivent le pattern JHipster `gestionTachesApp.{entity}.{field}` sauf pour les globales (`global.messages.*`).
