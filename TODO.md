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
- [x] Ajout de `@ManyToMany Set<User> members` dans `Project.java` — équipe du projet
- [x] `ProjectService.save()` : affecte automatiquement l'utilisateur courant comme `owner`
- [x] `ProjectService.findAll()` : ADMIN voit tout, les autres voient leurs projets uniquement
- [x] `ProjectService.findOne()` : pareil, filtré par propriétaire
- [x] Endpoints : `GET/POST/DELETE /api/projects/{id}/members` — gestion d'équipe
- [x] Changement du `@WithMockUser` dans `ProjectResourceIT.java` en `ROLE_ADMIN`

### Bugs corrigés

- [x] **JWT — claim `auth` jamais parsé** : ajout d'un `JwtAuthenticationConverter` custom (lambda) dans `SecurityConfiguration.java` qui lit le claim `auth` et split par espace ; injecté via paramètre de méthode dans `filterChain()`
- [x] **`ProjectService.update()`** : charge maintenant l'entité existante avant d'appliquer les modifications pour ne pas écraser `owner_id` par NULL
- [x] **Ownership checks** : `delete()`, `getMembers()`, `addMember()`, `removeMember()` vérifient désormais que l'utilisateur est bien propriétaire du projet (sauf ADMIN)
- [x] **Frontend — `IProject` model** : ajout des champs `ownerId`, `ownerLogin`, `memberIds`
- [x] **Frontend — Erreur liste projets** : affichage d'un message d'erreur si le chargement échoue

## 📝 À faire (prochaines étapes)

### Gestion des erreurs frontend

- [ ] Appliquer le même fix `onSaveError()` avec `AlertService` sur les autres composants (Sprint, Epic, Issue, Comment, Attachment, ActionHistory)

### Tests

- [ ] Vérifier que les `@WithMockUser` dans les tests IT sont à jour avec les nouveaux rôles
- [ ] Ajouter des tests pour les nouveaux rôles DEVELOPER et PROJET_MANAGER

### Fonctionnalités

- [ ] Définir les règles métier par entité (qui peut faire quoi)
- [ ] Implémenter la logique d'assignation des issues (uniquement DEVELOPER et PROJET_MANAGER)
- [ ] Ajouter un système de notification quand une issue est assignée
