# Rôles et permissions

## Les deux niveaux de rôles

### 1. Rôles globaux (niveau compte)

Attribués au compte utilisateur. Ils ouvrent l'accès aux écrans et aux actions :

| Rôle global | Profil | Ce que cela permet |
|-------------|--------|--------------------|
| `ROLE_ADMIN` | Administrateur de la plateforme | Toutes les fonctions, y compris la zone d'administration réservée (`/api/admin/**`), les statistiques globales, les exports CSV. Est considéré **OWNER** de tous les projets. |
| `ROLE_PROJET_MANAGER` | Chef de projet | Créer des projets, gérer des équipes. N'a **pas** de passe-droit sur les projets : il reste soumis aux rôles projet. |
| `ROLE_DEVELOPER` | Développeur | Travailler sur les tâches, commenter, ajouter des pièces jointes, consulter le tableau de bord développeur. |
| `ROLE_USER` | Utilisateur de base | Connexion, inscription, accès aux projets dont il est membre. Peut détenir un rôle projet (ex. OWNER d'un projet créé). |

Un compte créé par **inscription libre** (`POST /api/register`) reçoit le rôle `ROLE_USER` (et doit être activé). La zone `/api/admin/**` est strictement réservée à `ROLE_ADMIN`.

### 2. Rôles projet (niveau membre)

Chaque membre d'un projet détient un de ces trois rôles :

| Rôle projet | Privilèges |
|-------------|------------|
| `OWNER` | Propriétaire. Tous les droits **dans le projet**, y compris supprimer le projet et changer les rôles. Le créateur du projet en devient automatiquement l'OWNER. |
| `MANAGER` | Gestionnaire. Tous les droits sauf : supprimer le projet, changer les rôles, retirer l'OWNER. |
| `MEMBER` | Membre. Lecture des contenus du projet, modification de **ses propres** tâches assignées, commentaires, pièces jointes, chat. |

**Règle d'héritage** : `OWNER` hérite de toutes les permissions de `MANAGER` et `MEMBER` ; `MANAGER` hérite de celles de `MEMBER`.

## Accès aux projets (visibilité)

| Situation | Accès |
|-----------|-------|
| Utilisateur `ADMIN` | Voit **tous** les projets, même sans en être membre. |
| Membre du projet (n'importe quel rôle) | Voit le projet et son contenu. |
| Non membre (y compris `PROJET_MANAGER`, `DEVELOPER`, `USER`) | Ne voit **pas** le projet. |

La liste « Mes projets » (`mineOnly`) restreint la vue aux projets de l'utilisateur, y compris pour les administrateurs.

## Matrice des permissions par module

Légende : ✅ autorisé · ❌ refusé · 🔒 autorisé si l'utilisateur a le rôle projet indiqué (les rôles globaux autres qu'`ADMIN` restent soumis à cette condition).

> Les cases « globaux » correspondent au **filtre d'entrée** appliqué par l'application ; le rôle projet réel est ensuite vérifié. `ADMIN` (global) est toujours accepté, quel que soit son rôle projet (il est assimilé à `OWNER`).

### Projets

| Action | ADMIN (global) | PROJET_MANAGER (global) | DEVELOPER (global) | USER (global) | Rôle projet requis |
|--------|---------------|------------------------|--------------------|---------------|-------------------|
| Créer un projet | ✅ | ✅ | ❌ | ❌ | — (devient OWNER) |
| Modifier un projet | ✅ | 🔒 | ❌ | ❌ | OWNER / MANAGER |
| Supprimer un projet | ✅ | 🔒 | ❌ | 🔒 | **OWNER** |
| Voir un projet | ✅ | 🔒 | 🔒 | 🔒 | Membre (n'importe quel rôle) |
| Voir la liste des projets | ✅ | 🔒 | 🔒 | 🔒 | Membre (ou OWNER d'un projet) |

### Gestion des membres

| Action | ADMIN (global) | PROJET_MANAGER (global) | DEVELOPER (global) | USER (global) | Rôle projet requis |
|--------|---------------|------------------------|--------------------|---------------|-------------------|
| Lister les membres | ✅ | 🔒 | 🔒 | 🔒 | Membre (n'importe quel rôle) |
| Ajouter un membre | ✅ | 🔒 | ❌ | 🔒 | OWNER / MANAGER |
| Retirer un membre | ✅ | 🔒 | ❌ | 🔒 | OWNER / MANAGER |
| Changer le rôle d'un membre | ✅ | 🔒 | ❌ | 🔒 | **OWNER** |

**Règles de protection** :
- Un `MANAGER` ne peut **pas retirer** l'`OWNER` du projet.
- On ne peut ni retirer ni déclasser le **dernier** `OWNER` d'un projet.
- Un nouveau membre rejoint toujours avec le rôle **`MEMBER`** (le rôle ne peut être modifié que par un OWNER).

### Tâches

| Action | ADMIN (global) | PROJET_MANAGER (global) | DEVELOPER (global) | USER (global) | Rôle projet requis |
|--------|---------------|------------------------|--------------------|---------------|-------------------|
| Créer une tâche | ✅ | 🔒 | 🔒 | ❌ | OWNER / MANAGER |
| Modifier une tâche (n'importe laquelle) | ✅ | 🔒 | 🔒 | ❌ | OWNER / MANAGER |
| Modifier une tâche qui lui est assignée | ✅ | 🔒 | 🔒 | ❌ | **MEMBER** (si assigné) |
| Changer l'assignation d'une tâche | ✅ | 🔒 | 🔒 | ❌ | **OWNER / MANAGER** |
| Supprimer une tâche | ✅ | 🔒 | 🔒 | ❌ | OWNER / MANAGER |
| Voir les tâches | ✅ | ✅ | ✅ | ✅ | — (lecture authentifiée) |

**Règles détaillées** :
- Un `MEMBER` (rôle projet typique des développeurs) ne peut modifier qu'une tâche **qui lui est assignée**, et ne peut pas en changer l'assignataire (même en passant par une modification générale).
- L'assignataire d'une tâche doit obligatoirement être **membre du projet**.
- La tâche est créée par l'utilisateur courant, avec le statut **NEW** par défaut.

### Sprints

| Action | ADMIN (global) | PROJET_MANAGER (global) | DEVELOPER (global) | USER (global) | Rôle projet requis |
|--------|---------------|------------------------|--------------------|---------------|-------------------|
| Créer un sprint | ✅ | 🔒 | ❌ | 🔒 | OWNER / MANAGER |
| Modifier un sprint | ✅ | 🔒 | ❌ | 🔒 | OWNER / MANAGER |
| Démarrer un sprint | ✅ | 🔒 | ❌ | 🔒 | OWNER / MANAGER |
| Clôturer un sprint | ✅ | 🔒 | ❌ | 🔒 | OWNER / MANAGER |
| Supprimer un sprint | ✅ | 🔒 | ❌ | 🔒 | OWNER / MANAGER |
| Voir les sprints / le backlog | ✅ | ✅ | ✅ | ✅ | — (lecture authentifiée) |

> Note : le filtre global des endpoints de sprint **exclut** `DEVELOPER`. Un développeur ne peut donc pas déclencher de gestion de sprint, même s'il devait être OWNER/MANAGER d'un projet.

### Épics

| Action | ADMIN (global) | PROJET_MANAGER (global) | DEVELOPER (global) | USER (global) | Rôle projet requis |
|--------|---------------|------------------------|--------------------|---------------|-------------------|
| Créer un épic | ✅ | 🔒 | ❌ | 🔒 | OWNER / MANAGER |
| Modifier un épic | ✅ | 🔒 | ❌ | 🔒 | OWNER / MANAGER |
| Supprimer un épic | ✅ | 🔒 | ❌ | 🔒 | OWNER / MANAGER |
| Voir les épics | ✅ | ✅ | ✅ | ✅ | — (lecture authentifiée) |

### Commentaires et pièces jointes

| Action | ADMIN (global) | PROJET_MANAGER (global) | DEVELOPER (global) | USER (global) | Règle complémentaire |
|--------|---------------|------------------------|--------------------|---------------|---------------------|
| Commenter une tâche | ✅ | ✅ | ✅ | ❌ | Être membre du projet du projet de la tâche |
| Modifier/supprimer un commentaire | ✅ | ✅ | 🔒 | ❌ | ADMIN/PROJET_MANAGER toujours ; sinon **son propre** commentaire |
| Ajouter une pièce jointe | ✅ | ✅ | ✅ | ❌ | Être membre du projet de la tâche |
| Modifier/supprimer une pièce jointe | ✅ | ✅ | 🔒 | ❌ | ADMIN/PROJET_MANAGER toujours ; sinon **sa propre** pièce jointe |

> Un utilisateur qui n'est pas membre du projet d'une tâche ne peut pas y laisser de commentaire ni de pièce jointe, même s'il connaît l'identifiant de la tâche.

### Chat

| Action | ADMIN (global) | PROJET_MANAGER (global) | DEVELOPER (global) | USER (global) | Règle complémentaire |
|--------|---------------|------------------------|--------------------|---------------|---------------------|
| Voir le canal # Général | ✅ | ✅ | 🔒 | 🔒 | Être membre du projet |
| Créer une conversation privée | ✅ | ✅ | 🔒 | 🔒 | Les 2 interlocuteurs membres du projet |
| Envoyer un message | ✅ | ✅ | 🔒 | 🔒 | Accès à la conversation |
| Modifier/supprimer un message | ✅ | ✅ | 🔒 | 🔒 | **Son propre** message uniquement |

### Administration, tableaux de bord et exports

| Action | ADMIN (global) | PROJET_MANAGER (global) | DEVELOPER (global) | USER (global) |
|--------|---------------|------------------------|--------------------|---------------|
| Zone d'administration (utilisateurs, autorités, notifications admin, stats globales) | ✅ | ❌ | ❌ | ❌ |
| Exports CSV (projets, utilisateurs, rapports) | ✅ | ❌ | ❌ | ❌ |
| Tableau de bord développeur (statistiques développeur) | ✅ | ✅ | ✅ | ❌ |
| KPIs du tableau de bord principal | ✅ | 🔒 (membres) | 🔒 (membres) | 🔒 (membres) |

### Audit (TaskHistory)

| Action | ADMIN (global) | PROJET_MANAGER (global) | DEVELOPER (global) | USER (global) |
|--------|---------------|------------------------|--------------------|---------------|
| Ajouter / modifier / supprimer une entrée d'historique | ✅ | ✅ | ✅ | ❌ |
| Consulter l'historique d'une tâche / son propre historique | ✅ | ✅ | ✅ | ✅ |

Voir [audit.md](./audit.md) pour les règles de contenu.

## Actions publiques (sans compte)

- Connexion (`POST /api/authenticate`).
- Inscription (`POST /api/register`) — le compte est créé en `ROLE_USER` puis activé.
- Activation du compte et réinitialisation de mot de passe.
- Accès à la documentation Swagger.

## Points clés à retenir

1. **`ADMIN` est globalement OWNER** : il contourne tous les contrôles de rôle projet.
2. **`PROJET_MANAGER` n'a aucun passe-droit projet** : il doit être OWNER ou MANAGER du projet concerné.
3. **La création de contenu structurant** (projets, épics, sprints, tâches, membres) est réservée à **OWNER/MANAGER**.
4. **`MEMBER` ne gère pas** : il exécute (ses tâches, commentaires, pièces jointes, chat) et consulte.
5. Un **développeur** (`DEVELOPER`) ne peut jamais gérer les sprints (filtre global) ; son rôle projet est généralement `MEMBER`.
6. La **suppression d'un projet** et la **gestion des rôles** sont strictement réservées à l'**OWNER**.
