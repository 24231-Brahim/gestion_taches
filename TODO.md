# TODO — Projet Gestion des Tâches

## ✅ Fait

### Sécurité RBAC

- [x] Ajout des constantes `DEVELOPER` et `PROJET_MANAGER` dans `AuthoritiesConstants.java`
- [x] `@PreAuthorize` sur **ProjectResource** — POST/PUT/PATCH/DELETE : ADMIN + PROJET_MANAGER
- [x] `@PreAuthorize` sur **SprintResource** — POST/PUT/PATCH/DELETE : ADMIN + PROJET_MANAGER
- [x] `@PreAuthorize` sur **EpicResource** — POST/PUT/PATCH/DELETE : ADMIN + PROJET_MANAGER
- [x] `@PreAuthorize` sur **IssueResource** — POST/PUT/PATCH/DELETE : ADMIN + PROJET_MANAGER + DEVELOPER
- [x] `@PreAuthorize` sur **CommentResource** — POST : ADMIN + PROJET_MANAGER + DEVELOPER + USER ; PUT/PATCH/DELETE : ADMIN + PROJET_MANAGER + DEVELOPER
- [x] `@PreAuthorize` sur **AttachmentResource** — POST/PUT/PATCH/DELETE : ADMIN + PROJET_MANAGER + DEVELOPER
- [x] `@PreAuthorize` sur **ActionHistoryResource** — POST/PUT/PATCH/DELETE : ADMIN + PROJET_MANAGER + DEVELOPER

### Données de seed (Liquibase)

- [x] Ajout des utilisateurs `manager` (ROLE_PROJET_MANAGER) et `dev` (ROLE_DEVELOPER) dans `user.csv`
- [x] Assignation des rôles dans `user_authority.csv`

### Frontend — Correction du bouton Save (Project)

- [x] Ajout de `AlertService` dans `project-update.ts` pour afficher les erreurs HTTP
- [x] Retrait du validateur `required` sur `createdAt` et `id` dans `project-form.service.ts`
- [x] Remplacement du champ `datetime-local` visible par un champ caché pour `createdAt`

### BDD — Colonne `key` réservée SQL

- [x] Renommage de la colonne `key` → `project_key` dans `Project.java`
- [x] Renommage de la colonne `key` → `project_key` dans le changelog Liquibase

### Propriétaire et équipe (Project)

- [x] Ajout de `@ManyToOne User owner` dans `Project.java` — créateur du projet
- [x] Ajout de `@ManyToMany Set<User> members` dans `Project.java` — puis remplacé par `@OneToMany Set<ProjectMember> projectMembers`
- [x] Création de l'entité `ProjectMember` (id, project, user, role, joinedAt) remplaçant le `@ManyToMany`
- [x] `ProjectService.save()` : affecte automatiquement l'utilisateur courant comme `owner`
- [x] `ProjectService.findAll()` : ADMIN voit tout, les autres voient leurs projets uniquement
- [x] `ProjectService.findOne()` : pareil, filtré par propriétaire
- [x] Endpoints : `GET/POST/DELETE /api/projects/{id}/members` — gestion d'équipe (renvoie `ProjectMemberDTO`)
- [x] Changement du `@WithMockUser` dans `ProjectResourceIT.java` en `ROLE_ADMIN`

### Endpoint PATCH pour modification de rôle

- [x] Ajout de `ProjectService.updateMemberRole()` — modifie le rôle d'un membre avec `checkOwnership`
- [x] Ajout de `PATCH /api/projects/{id}/members/{userId}` dans `ProjectResource.java`

### Frontend — Section Membres dans le détail du projet

- [x] Ajout de `updateMemberRole()` dans `project.service.ts`
- [x] Création de la section "Membres" dans `project-detail.ts` (signals, CRUD complet)
- [x] UI complète dans `project-detail.html` (tableau liste + formulaire ajout + inline edit rôle + suppression)
- [x] Contrôle des boutons par `*jhiHasAnyAuthority(['ROLE_ADMIN', 'ROLE_PROJET_MANAGER'])`
- [x] Clés i18n ajoutées dans `fr.json` et `en.json`

### Nettoyage et optimisation

- [x] Ajout de `@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}))` sur `ProjectMember.java`
- [x] Suppression de la méthode inutilisée `deleteByProjectIdAndUserId` dans `ProjectMemberRepository.java`
- [x] Optimisation de `addMember()` pour éviter un double chargement du projet en BDD

### Bugs corrigés

- [x] **JWT — claim `auth` jamais parsé** : ajout d'un `JwtAuthenticationConverter` custom (lambda) dans `SecurityConfiguration.java` qui lit le claim `auth` et split par espace ; injecté via paramètre de méthode dans `filterChain()`
- [x] **`ProjectService.update()`** : charge maintenant l'entité existante avant d'appliquer les modifications pour ne pas écraser `owner_id` par NULL
- [x] **Ownership checks** : `delete()`, `getMembers()`, `addMember()`, `removeMember()` vérifient désormais que l'utilisateur est bien propriétaire du projet (sauf ADMIN)
- [x] **Frontend — `IProject` model** : ajout des champs `ownerId`, `ownerLogin`, `memberIds`
- [x] **Frontend — Erreur liste projets** : affichage d'un message d'erreur si le chargement échoue

### Design System & Theming

- [x] Création de `design-system.scss` : CSS custom properties (dark/light), reset, typo (Audiowide + JetBrains Mono), accessibilité
- [x] Surcharge Bootstrap dark theme dans `_bootstrap-variables.scss` (hex values, border-radius: 0, border-width: 3px)
- [x] Réécriture de `global.scss` : boutons brutals (offset shadow + hover translate), cards, inputs, alerts, tables, pagination, modals, layout shell, page-heading
- [x] Google Fonts (Audiowide, JetBrains Mono) + `data-theme="dark"` dans `index.html`
- [x] `vendor.scss` réordonné : design-system → bootstrap-variables → bootstrap

### Layout — Restructuration complète

- [x] `Sidebar` component : collapsible 256px ↔ 80px, navigation entities + admin, overlay mobile, toggle chevron
- [x] `BottomNav` component : 4 items (Accueil, Projects, Issues, Settings/Login), visible <768px
- [x] `Navbar` transformée en topbar : brand + logo + hamburger mobile + dropdowns (admin, account, langue)
- [x] `Main` layout shell : topbar + sidebar + content + footer + bottom-nav
- [x] Communication mobile hamburger ↔ sidebar via `document.body.classList` + MutationObserver
- [x] Fix z-index sidebar/overlay (sidebar au-dessus de l'overlay)
- [x] Fix `overflow-x: hidden` qui coupait le bouton toggle de la sidebar

### Branding

- [x] Suppression des images JHipster (logo-jhipster.png, favicon.ico, family_members svg/png)
- [x] `logo.webp` défini comme logo du projet (topbar + favicon)

### Icônes FontAwesome

- [x] Ajout des icônes manquantes dans `font-awesome-icons.ts` : folder, bookmark, bug, comment, paperclip, history, cog, chevron-left, chevron-right, user-circle

## 📝 À faire (prochaines étapes)

### Layout — Finitions

- [ ] Ajouter les pages admin dans la sidebar (User Management, Metrics, Health, Configuration, Logs, API, H2)
- [ ] Persister l'état collapsed de la sidebar (localStorage)

### Tests

- [ ] Vérifier que les `@WithMockUser` dans les tests IT sont à jour avec les nouveaux rôles
- [ ] Ajouter des tests pour les nouveaux rôles DEVELOPER et PROJET_MANAGER

### Fonctionnalités

- [ ] Définir les règles métier par entité (qui peut faire quoi)
- [ ] Implémenter la logique d'assignation des issues (uniquement DEVELOPER et PROJET_MANAGER)
- [ ] Ajouter un système de notification quand une issue est assignée
