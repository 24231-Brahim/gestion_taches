# Note d'implémentation — Sécurité RBAC

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

## Utilisateurs de test (seed Liquibase)

| Login     | Mot de passe | Rôles                          |
| --------- | ------------ | ------------------------------ |
| `admin`   | `admin`      | ROLE_ADMIN, ROLE_USER          |
| `user`    | `user`       | ROLE_USER                      |
| `manager` | `user`       | ROLE_PROJET_MANAGER, ROLE_USER |
| `dev`     | `user`       | ROLE_DEVELOPER, ROLE_USER      |

## Fichiers modifiés

- `src/main/java/com/gestiontaches/security/AuthoritiesConstants.java` — ajout des constantes DEVELOPER et PROJET_MANAGER
- `src/main/java/com/gestiontaches/web/rest/ProjectResource.java` — @PreAuthorize
- `src/main/java/com/gestiontaches/web/rest/SprintResource.java` — @PreAuthorize
- `src/main/java/com/gestiontaches/web/rest/EpicResource.java` — @PreAuthorize
- `src/main/java/com/gestiontaches/web/rest/IssueResource.java` — @PreAuthorize
- `src/main/java/com/gestiontaches/web/rest/CommentResource.java` — @PreAuthorize
- `src/main/java/com/gestiontaches/web/rest/AttachmentResource.java` — @PreAuthorize
- `src/main/java/com/gestiontaches/web/rest/ActionHistoryResource.java` — @PreAuthorize
- `src/main/resources/config/liquibase/data/user.csv` — ajout users manager et dev
- `src/main/resources/config/liquibase/data/user_authority.csv` — assignation des rôles
