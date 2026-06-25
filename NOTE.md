# Note d'implémentation — Sécurité RBAC et Propriété des Projets

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

| Méthode | Path                                  | Rôles autorisés       | Action            |
| ------- | ------------------------------------- | --------------------- | ----------------- |
| GET     | `/api/projects/{id}/members`          | ADMIN, PROJET_MANAGER | Lister l'équipe   |
| POST    | `/api/projects/{id}/members/{userId}` | ADMIN, PROJET_MANAGER | Ajouter un membre |
| DELETE  | `/api/projects/{id}/members/{userId}` | ADMIN, PROJET_MANAGER | Retirer un membre |

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
- `src/main/java/com/gestiontaches/domain/Project.java` — ajout owner + members
- `src/main/java/com/gestiontaches/service/dto/ProjectDTO.java` — ajout ownerId, ownerLogin, memberIds
- `src/main/java/com/gestiontaches/service/mapper/ProjectMapper.java` — mapping des nouveaux champs
- `src/main/java/com/gestiontaches/repository/ProjectRepository.java` — findByOwnerLogin
- `src/main/java/com/gestiontaches/service/ProjectService.java` — set owner, filtre findAll/findOne, gestion team, ownership checks, update() preserves owner
- `src/main/java/com/gestiontaches/web/rest/ProjectResource.java` — endpoints team, GET filtré
- `src/main/java/com/gestiontaches/config/SecurityConfiguration.java` — JwtAuthenticationConverter bean
- `src/main/resources/config/liquibase/changelog/20260625000000_added_entity_Project_owner.xml` — owner_id + table project_members
- `src/main/webapp/app/entities/project/project.model.ts` — ajout ownerId, ownerLogin, memberIds
- `src/main/webapp/app/entities/project/list/project.ts` — signal error() pour afficher les erreurs de chargement
- `src/main/webapp/app/entities/project/list/project.html` — affichage d'une alerte en cas d'erreur
- `src/test/java/com/gestiontaches/web/rest/ProjectResourceIT.java` — @WithMockUser avec ROLE_ADMIN
