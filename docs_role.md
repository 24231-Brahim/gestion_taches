# Documentation Complète des Rôles — Gestion Tâches

> Application de gestion de projets et tâches (style JHipster/Jira)
> Stack : Spring Boot 4 + Angular 21 + PostgreSQL

---

## Table des matières

1. [Aperçu du projet](#1-apercu-du-projet)
2. [Les 4 types d'utilisateurs](#2-les-4-types-dutilisateurs)
3. [Rôle ADMIN — Administrateur](#3-rôle-admin--administrateur)
4. [Rôle PROJET_MANAGER — Chef de projet](#4-rôle-projet_manager--chef-de-projet)
5. [Rôle DEVELOPER — Développeur](#5-rôle-developer--développeur)
6. [Rôle USER — Utilisateur](#6-rôle-user--utilisateur)
7. [Matrice des permissions](#7-matrice-des-permissions)
8. [Hiérarchie des entités](#8-hiérarchie-des-entités)
9. [Navigation entre les pages](#9-navigation-entre-les-pages)
10. [Comptes de test](#10-comptes-de-test)

---

## 1. Aperçu du projet

**Gestion Tâches** est une application web de gestion de projets inspirée de Jira. Elle permet de :

- Créer et gérer des **projets** avec des membres
- Organiser le travail en **sprints**, **epics** et **tâches**
- Collaborer via des **commentaires** et **pièces jointes**
- Suivre l'avancement avec un **dashboard**, des **KPIs** et un **kanban**
- Gérer les **utilisateurs** et **rôles** (admin)

**Authentification** : JWT (stateless). L'utilisateur se connecte, reçoit un token, et ce token est envoyé avec chaque requête HTTP.

---

## 2. Les 4 types d'utilisateurs

| Rôle               | Login     | Mot de passe | Description                                |
| ------------------ | --------- | ------------ | ------------------------------------------ |
| **ADMIN**          | `admin`   | `admin`      | Administrateur système. A accès à TOUT.    |
| **PROJET_MANAGER** | `manager` | `manager`    | Chef de projet. Crée et gère les projets.  |
| **DEVELOPER**      | `dev`     | `dev`        | Développeur. Travaille sur les tâches.     |
| **USER**           | `user`    | `user`       | Utilisateur basique. Consulte et commente. |

> Chaque utilisateur peut avoir **plusieurs rôles** simultanément (ex: `admin` a `ROLE_ADMIN` + `ROLE_USER`).

---

## 3. Rôle ADMIN — Administrateur

### 3.1 Résumé

L'administrateur a un accès **complet et total** à toute l'application. Il est automatiquement considéré comme **OWNER** de tous les projets.

### 3.2 Ce qu'il peut faire

#### Gestion des utilisateurs

- Créer un utilisateur
- Modifier un utilisateur (nom, email, mot de passe, activer/désactiver)
- Supprimer un utilisateur
- Voir la liste de tous les utilisateurs
- Gérer les rôles système (authorities)

#### Gestion des projets

- Créer un projet (nom, description, clé unique)
- Modifier un projet
- Supprimer un projet
- Voir tous les projets
- Gérer les membres (ajouter, supprimer, changer leur rôle projet)
- Exporter les projets en CSV

#### Gestion des sprints

- Créer, modifier, supprimer un sprint
- Démarrer et clôturer un sprint
- Voir le backlog du projet

#### Gestion des epics

- Créer, modifier, supprimer un epic
- Voir le roadmap des epics
- Voir la liste des epics

#### Gestion des tâches

- Créer, modifier, supprimer une tâche
- Assigner un utilisateur à une tâche
- Changer le statut d'une tâche (via kanban ou formulaire)
- Voir les détails d'une tâche (onglets: détails, commentaires, pièces jointes, historique)

#### Gestion des commentaires

- Ajouter un commentaire sur n'importe quelle tâche
- Modifier n'importe quel commentaire
- Supprimer n'importe quel commentaire

#### Gestion des pièces jointes

- Uploader des fichiers sur les tâches
- Télécharger des pièces jointes
- Supprimer des pièces jointes

#### Dashboard

- Voir le dashboard complet avec KPIs, graphiques, listes récentes et timeline

#### Administration système

- Voir les métriques de l'application
- Voir les health checks (diagnostics)
- Voir la configuration de l'application
- Gérer les logs
- Voir la documentation API (Swagger)
- Accéder à la console H2 (en dev)

#### Notifications

- Voir ses notifications
- Marquer comme lu / marquer tout comme lu

#### Export CSV

- Exporter tous les projets
- Exporter toutes les tâches
- Exporter les tâches d'un projet spécifique
- Exporter la liste des utilisateurs

#### Compte

- Modifier son profil
- Changer son mot de passe
- Changer la langue (FR/EN)
- Changer le thème (clair/sombre)

### 3.3 Comment il fait — Étapes détaillées

#### Se connecter

```
1. Ouvre l'application → redirigé vers /login
2. Saisit "admin" + "admin"
3. Clique "Se connecter"
4. → Redirigé vers /home (dashboard avec KPIs s'affiche)
```

#### Créer un projet

```
1. Sidebar gauche → clique sur "Projects"
2. Page /project s'affiche (liste des projets)
3. Clique sur "Créer un projet" (bouton en haut)
4. Remplit : Nom, Description, Clé (ex: PRJ)
5. Clique "Sauvegarder"
6. → Retour à la liste, le projet apparaît
```

#### Gérer les membres d'un projet

```
1. Sidebar → Projects → clique sur un projet (clé)
2. Page /project/PRJ/view s'affiche (détails du projet)
3. Section "Members" visible
4. Clique "Add member" → sélectionne un utilisateur → clique "Save"
5. Pour modifier le rôle : clique sur l'icône crayon → change le rôle (OWNER/MANAGER/MEMBER) → sauvegarde
6. Pour supprimer : clique sur l'icône poubelle
```

#### Créer un sprint

```
1. Depuis le projet (clé PRJ), la sidebar affiche "Sprints"
2. Clique sur "Sprints" dans la sidebar → page /project/PRJ/sprint
3. Clique "Créer un sprint"
4. Remplit : Nom, Objectif, Date de début, Date de fin
5. Clique "Sauvegarder"
6. Le sprint apparaît dans la liste avec statut "PLANNED"
```

#### Démarrer un sprint

```
1. Page /project/PRJ/sprint
2. Le sprint doit être en statut "PLANNED"
3. Clique sur le sprint pour voir ses détails
4. Clique "Start Sprint"
5. Le statut passe à "ACTIVE"
```

#### Créer un epic

```
1. Sidebar → clique sur "Epics" (sous le projet PRJ)
2. Page /project/PRJ/epic s'affiche (roadmap par défaut)
3. Clique "Créer un epic"
4. Remplit : Titre, Description, Statut, Priorité, Date début/fin
5. Clique "Sauvegarder"
```

#### Créer une tâche

```
1. Sidebar → clique sur "Tasks" (sous le projet PRJ)
2. Page /project/PRJ/task s'affiche
3. Clique "Créer une tâche"
4. Remplit :
   - Titre (obligatoire)
   - Description
   - Type : STORY, BUG, TASK, SUBTASK, IMPROVEMENT
   - Statut : NEW, TODO, IN_PROGRESS, IN_REVIEW, DONE, CANCELLED
   - Priorité : LOWEST, LOW, MEDIUM, HIGH, HIGHEST
   - Story Points (nombre)
   - Sprint (sélection dans la liste)
   - Epic (sélection dans la liste)
   - Projet (pré-rempli si contexte projet)
   - Assignee (membres du projet)
5. Clique "Sauvegarder"
```

#### Modifier le statut d'une tâche (Kanban)

```
1. Page /project/PRJ/task
2. Clique sur l'onglet "Kanban" (icône grille)
3. Le tableau affiche 6 colonnes : NEW, TODO, IN_PROGRESS, IN_REVIEW, DONE, CANCELLED
4. Glisse-dépose une carte d'une colonne à l'autre
5. Le statut se met à jour automatiquement
```

#### Voir les détails d'une tâche

```
1. Page /project/PRJ/task
2. Clique sur le titre d'une tâche
3. Le panneau latéral s'ouvre OU page /project/PRJ/task/:id/view
4. 4 onglets disponibles :
   - Détails : titre, description, type, statut, priorité, dates, sprint, epic, projet, createur, assigné
   - Commentaires : liste des commentaires + formulaire pour ajouter
   - Pièces jointes : liste des fichiers + bouton uploader
   - Historique : journal des modifications
5. Bouton "Éditer" en bas pour modifier
```

#### Gérer les utilisateurs

```
1. Sidebar → Paramètres → "Utilisateurs"
2. Page /user-management s'affiche
3. Clique "Créer un utilisateur"
4. Remplit : Login, Email, Mot de passe, Prénom, Nom, Actif/Ou coché
5. Clique "Sauvegarder"
6. Pour modifier : clique sur un utilisateur → change les champs → sauvegarde
7. Pour supprimer : clique sur le bouton supprimer
```

#### Voir les métriques

```
1. Sidebar → Paramètres → "Métriques"
2. Page /admin/metrics s'affiche
3. Affiche : JVM, HTTP, Cache, DB, etc.
```

### 3.4 Navigation de l'ADMIN

```
/login
  └─→ /home (Dashboard avec KPIs)

Sidebar :
  ├─ Home → /home
  ├─ Projects → /project
  │     └─ [Projet sélectionné] :
  │           ├─ Sprints → /project/PRJ/sprint
  │           ├─ Epics → /project/PRJ/epic
  │           └─ Tasks → /project/PRJ/task
  ├─ Paramètres (accordéon) :
  │     ├─ Profil → /account/settings
  │     ├─ Thème → bascule clair/sombre
  │     ├─ Langue → FR/EN
  │     ├─ Utilisateurs → /user-management  ← UNIQUEMENT ADMIN
  │     ├─ Métriques → /admin/metrics  ← UNIQUEMENT ADMIN
  │     ├─ Diagnostics → /admin/health  ← UNIQUEMENT ADMIN
  │     ├─ Configuration → /admin/configuration  ← UNIQUEMENT ADMIN
  │     ├─ Logs → /admin/logs  ← UNIQUEMENT ADMIN
  │     ├─ API → /admin/docs  ← UNIQUEMENT ADMIN
  │     └─ Base de données → /h2-console  ← UNIQUEMENT ADMIN (dev uniquement)
  └─ Menu utilisateur (avatar) :
        ├─ Paramètres → /account/settings
        ├─ Mot de passe → /account/password
        └─ Déconnexion → retour à /login
```

---

## 4. Rôle PROJET_MANAGER — Chef de projet

### 4.1 Résumé

Le chef de projet **gère les projets** : les créer, les configurer, y ajouter des membres, et gérer tout le cycle de vie (sprints, epics, tâches). Il n'a **PAS** accès à l'administration système.

### 4.2 Ce qu'il peut faire

#### Gestion des projets

- **Créer** un projet
- **Modifier** un projet
- **Supprimer** un projet
- Voir tous les projets
- Gérer les **membres** (ajouter, supprimer, modifier leur rôle) — uniquement sur les projets où il est OWNER ou MANAGER
- Exporter les projets en CSV

#### Gestion des sprints

- Créer, modifier, supprimer un sprint
- Démarrer et clôturer un sprint

#### Gestion des epics

- Créer, modifier, supprimer un epic

#### Gestion des tâches

- Créer, modifier, supprimer une tâche
- Assigner un utilisateur à une tâche
- Changer le statut d'une tâche (kanban)

#### Commentaires et pièces jointes

- Ajouter/modifier/supprimer des commentaires
- Uploader/télécharger/supprimer des pièces jointes

#### Dashboard

- Voir le dashboard avec KPIs et graphiques

#### Notifications et compte

- Voir ses notifications
- Modifier son profil, changer mot de passe, langue, thème

### 4.3 Ce qu'il NE PEUT PAS faire

- Gérer les utilisateurs (CRUD) — page /user-management inaccessible
- Gérer les authorities (rôles système)
- Voir les métriques, health checks, configuration, logs, API docs
- Accéder à la console H2
- Exporter la liste des utilisateurs en CSV

### 4.4 Comment il fait — Étapes détaillées

#### Créer un projet

```
1. Clique "Projects" dans la sidebar
2. Page /project → clique "Créer un projet"
3. Remplit : Nom, Description, Clé (ex: MOB)
4. Clique "Sauvegarder"
5. Il devient automatiquement OWNER du projet
6. Le projet apparaît dans la liste
```

#### Ajouter des membres à un projet

```
1. Clique sur le projet → /project/MOB/view
2. Section "Members" → clique "Add member"
3. Sélectionne un utilisateur dans la liste déroulante
4. Clique "Save"
5. Le membre est ajouté avec le rôle par défaut "MEMBER"
6. Pour changer le rôle : icône crayon → choisir OWNER/MANAGER/MEMBER → sauvegarder
```

#### Créer un sprint

```
1. Sidebar affiche "Sprints" quand un projet est sélectionné
2. Clique "Sprints" → /project/MOB/sprint
3. Clique "Créer un sprint"
4. Remplit : Nom, Objectif, Dates
5. Clique "Sauvegarder"
```

#### Créer un epic

```
1. Sidebar → "Epics" → /project/MOB/epic
2. Vue roadmap par défaut, ou cliquer "Table" pour la vue liste
3. Clique "Créer un epic"
4. Remplit : Titre, Description, Statut, Priorité
5. Clique "Sauvegarder"
```

#### Créer une tâche

```
1. Sidebar → "Tasks" → /project/MOB/task
2. Clique "Créer une tâche"
3. Le projet est pré-rempli (contexte actuel)
4. Remplit : Titre, Type, Statut, Priorité, Story Points
5. Optionnel : assigne un sprint, un epic, un membre
6. Clique "Sauvegarder"
```

#### Gérer les tâches via Kanban

```
1. Page /project/MOB/task
2. Bascule en mode "Kanban" (onglet/icône)
3. Colonnes : NEW → TODO → IN_PROGRESS → IN_REVIEW → DONE
4. Glisse-dépose les cartes pour changer le statut
```

#### Exporter les tâches d'un projet en CSV

```
1. Page /project/MOB/task
2. Clique sur le bouton "Export CSV"
3. Un fichier CSV est téléchargé avec toutes les tâches du projet
```

### 4.5 Navigation du PROJET_MANAGER

```
/login
  └─→ /home (Dashboard avec KPIs)

Sidebar :
  ├─ Home → /home
  ├─ Projects → /project
  │     └─ [Projet sélectionné] :
  │           ├─ Sprints → /project/MOB/sprint
  │           ├─ Epics → /project/MOB/epic
  │           └─ Tasks → /project/MOB/task
  ├─ Paramètres (accordéon) :
  │     ├─ Profil → /account/settings
  │     ├─ Thème
  │     └─ Langue
  └─ Menu utilisateur :
        ├─ Paramètres → /account/settings
        ├─ Mot de passe → /account/password
        └─ Déconnexion

PAS de liens : Utilisateurs, Métriques, Diagnostics, Configuration, Logs, API, BDD
```

---

## 5. Rôle DEVELOPER — Développeur

### 5.1 Résumé

Le développeur **travaille sur les tâches**. Il peut créer, modifier et assigner des tâches, commenter et attacher des fichiers. Il ne peut **PAS** créer de projets ni gérer les membres.

### 5.2 Ce qu'il peut faire

#### Gestion des projets

- **Voir** la liste des projets
- **Voir** les détails d'un projet et ses membres
- **Modifier** un projet (si autorisé par le backend)
- **Supprimer** un projet (si autorisé par le backend)
- Exporter les projets en CSV

#### Gestion des sprints

- **Voir** la liste des sprints d'un projet
- **Voir** les détails d'un sprint

#### Gestion des epics

- **Voir** le roadmap et la liste des epics
- **Voir** les détails d'un epic

#### Gestion des tâches

- **Créer** une tâche
- **Modifier** une tâche (si créateur ou OWNER/MANAGER du projet)
- **Supprimer** une tâche (si créateur ou OWNER/MANAGER du projet)
- **Assigner** un membre à une tâche
- Changer le statut d'une tâche

#### Commentaires et pièces jointes

- Ajouter un commentaire
- Modifier son propre commentaire
- Supprimer son propre commentaire
- Uploader des pièces jointes
- Télécharger des pièces jointes
- Supprimer des pièces jointes

#### Dashboard

- Voir le dashboard avec KPIs

### 5.3 Ce qu'il NE PEUT PAS faire

- **Créer** un projet
- Gérer les membres d'un projet (ajouter/supprimer/changer rôle)
- Gérer les sprints (créer/modifier/supprimer/démarrer/clôturer)
- Gérer les epics (créer/modifier/supprimer)
- Gérer les utilisateurs
- Voir les métriques, health checks, etc.
- Exporter la liste des utilisateurs

### 5.4 Comment il fait — Étapes détaillées

#### Voir les projets

```
1. Clique "Projects" dans la sidebar
2. Liste de tous les projets s'affiche
3. Clique sur un projet pour voir ses détails (/project/PRJ/view)
4. La section "Members" affiche les membres du projet
```

#### Créer une tâche

```
1. Sélectionne un projet dans la sidebar (les liens Sprints/Epics/Tasks apparaissent)
2. Clique "Tasks" → /project/PRJ/task
3. Clique "Créer une tâche"
4. Le projet est pré-rempli
5. Remplit : Titre, Description, Type (STORY/BUG/TASK/SUBTASK/IMPROVEMENT)
6. Choisit le statut (NEW par défaut), la priorité, les story points
7. Optionnel : sélectionne un sprint, un epic, un assignee
8. Clique "Sauvegarder"
9. La tâche apparaît dans la liste
```

#### Modifier une tâche qu'on a créée

```
1. Page /project/PRJ/task
2. Clique sur le titre d'une tâche que LUI a créée
3. Le panneau de détail s'ouvre
4. Clique "Éditer"
5. Modifie les champs souhaités
6. Clique "Sauvegarder"
```

#### Assigner un membre à une tâche

```
1. Ouvre la tâche (clic sur le titre)
2. Clique "Éditer"
3. Dans le champ "Assignee", sélectionne un membre du projet
4. Clique "Sauvegarder"
```

#### Commenter une tâche

```
1. Ouvre la tâche
2. Clique sur l'onglet "Comments"
3. Tape son commentaire dans le champ de texte
4. Clique "Save"
5. Le commentaire apparaît avec son nom et la date
```

#### Attacher un fichier à une tâche

```
1. Ouvre la tâche
2. Clique sur l'onglet "Attachments"
3. Clique "Upload" ou glisse un fichier
4. Le fichier apparaît dans la liste
5. Pour télécharger : clique sur le nom du fichier
```

#### Voir l'historique d'une tâche

```
1. Ouvre la tâche
2. Clique sur l'onglet "History"
3. L'historique affiche toutes les modifications passées
```

### 5.5 Navigation du DEVELOPER

```
/login
  └─→ /home (Dashboard avec KPIs)

Sidebar :
  ├─ Home → /home
  ├─ Projects → /project
  │     └─ [Projet sélectionné] :
  │           ├─ Sprints → /project/PRJ/sprint (lecture seule)
  │           ├─ Epics → /project/PRJ/epic (lecture seule)
  │           └─ Tasks → /project/PRJ/task (CRUD)
  ├─ Paramètres (accordéon) :
  │     ├─ Profil → /account/settings
  │     ├─ Thème
  │     └─ Langue
  └─ Menu utilisateur :
        ├─ Paramètres → /account/settings
        ├─ Mot de passe → /account/password
        └─ Déconnexion

PAS de liens : Utilisateurs, Métriques, Diagnostics, Configuration, Logs, API, BDD
```

---

## 6. Rôle USER — Utilisateur

### 6.1 Résumé

L'utilisateur basique **consulte** les projets et **collabore** via les commentaires. Il peut gérer les sprints et epics mais ne crée pas de tâches directement. C'est le rôle le plus limité.

### 6.2 Ce qu'il peut faire

#### Gestion des projets

- **Voir** la liste des projets
- **Voir** les détails d'un projet
- **Modifier** un projet (selon les permissions backend)
- **Supprimer** un projet (selon les permissions backend)
- Exporter les projets en CSV

#### Gestion des sprints

- **Créer** un sprint
- **Modifier** un sprint
- **Supprimer** un sprint
- **Démarrer** et **clôturer** un sprint

#### Gestion des epics

- **Créer** un epic
- **Modifier** un epic
- **Supprimer** un epic

#### Gestion des tâches

- **Voir** la liste des tâches
- **Voir** les détails d'une tâche
- **Modifier** une tâche (si elle lui est assignée ou si autorisé)

#### Commentaires

- Ajouter un commentaire
- Modifier/supprimer son propre commentaire

#### Dashboard

- Voir une page d'accueil basique (pas de dashboard KPI)

### 6.3 Ce qu'il NE PEUT PAS faire

- **Créer** un projet
- Gérer les membres d'un projet
- **Créer** une tâche (directement — seulement via endpoint projet)
- Uploader des pièces jointes
- Gérer les utilisateurs
- Voir les métriques, health checks, etc.

### 6.4 Comment il fait — Étapes détaillées

#### Se connecter

```
1. Ouvre /login
2. Saisit "user" + "user"
3. Clique "Se connecter"
4. → Redirigé vers /home
5. La page affiche un message de bienvenue SIMPLE (PAS le dashboard KPI)
```

#### Voir les projets

```
1. Clique "Projects" dans la sidebar
2. Liste des projets s'affiche
3. Clique sur un projet pour voir ses détails
```

#### Créer un sprint

```
1. Sélectionne un projet dans la sidebar
2. Clique "Sprints" → /project/PRJ/sprint
3. Clique "Créer un sprint"
4. Remplit : Nom, Objectif, Dates
5. Clique "Sauvegarder"
```

#### Créer un epic

```
1. Clique "Epics" dans la sidebar
2. /project/PRJ/epic s'affiche
3. Clique "Créer un epic"
4. Remplit les champs
5. Clique "Sauvegarder"
```

#### Commenter une tâche

```
1. Va dans "Tasks" → /project/PRJ/task
2. Clique sur une tâche
3. Onglet "Comments"
4. Tape le commentaire → clique "Save"
```

### 6.5 Navigation du USER

```
/login
  └─→ /home (Page d'accueil basique, PAS de dashboard KPI)

Sidebar :
  ├─ Home → /home
  ├─ Projects → /project
  │     └─ [Projet sélectionné] :
  │           ├─ Sprints → /project/PRJ/sprint (CRUD)
  │           ├─ Epics → /project/PRJ/epic (CRUD)
  │           └─ Tasks → /project/PRJ/task (lecture + commentaire)
  ├─ Paramètres (accordéon) :
  │     ├─ Profil → /account/settings
  │     ├─ Thème
  │     └─ Langue
  └─ Menu utilisateur :
        ├─ Paramètres → /account/settings
        ├─ Mot de passe → /account/password
        └─ Déconnexion

PAS de liens : Utilisateurs, Métriques, Diagnostics, Configuration, Logs, API, BDD
```

---

## 7. Matrice des permissions

### 7.1 Permissions par rôle système

| Action                | ADMIN | PROJET_MANAGER |  DEVELOPER  |    USER     |
| --------------------- | :---: | :------------: | :---------: | :---------: |
| **Projets**           |       |                |             |             |
| Créer un projet       |  OUI  |      OUI       |     NON     |     NON     |
| Modifier un projet    |  OUI  |      OUI       |    OUI\*    |    OUI\*    |
| Supprimer un projet   |  OUI  |      OUI       |    OUI\*    |    OUI\*    |
| Voir la liste         |  OUI  |      OUI       |     OUI     |     OUI     |
| Voir les détails      |  OUI  |      OUI       |     OUI     |     OUI     |
| **Membres**           |       |                |             |             |
| Ajouter un membre     |  OUI  |    OUI\*\*     |     NON     |     NON     |
| Supprimer un membre   |  OUI  |    OUI\*\*     |     NON     |     NON     |
| Changer le rôle       |  OUI  |    OUI\*\*     |     NON     |     NON     |
| **Sprints**           |       |                |             |             |
| Créer un sprint       |  OUI  |      OUI       |     NON     |     OUI     |
| Modifier un sprint    |  OUI  |      OUI       |     NON     |     OUI     |
| Supprimer un sprint   |  OUI  |      OUI       |     NON     |     OUI     |
| Démarrer/Clôturer     |  OUI  |      OUI       |     NON     |     OUI     |
| Voir les sprints      |  OUI  |      OUI       |     OUI     |     OUI     |
| **Epics**             |       |                |             |             |
| Créer un epic         |  OUI  |      OUI       |     NON     |     OUI     |
| Modifier un epic      |  OUI  |      OUI       |     NON     |     OUI     |
| Supprimer un epic     |  OUI  |      OUI       |     NON     |     OUI     |
| Voir les epics        |  OUI  |      OUI       |     OUI     |     OUI     |
| **Tâches**            |       |                |             |             |
| Créer une tâche       |  OUI  |      OUI       |     OUI     |  NON\*\*\*  |
| Modifier une tâche    |  OUI  |      OUI       | OUI\*\*\*\* | OUI\*\*\*\* |
| Supprimer une tâche   |  OUI  |      OUI       | OUI\*\*\*\* |     NON     |
| Assigner un membre    |  OUI  |      OUI       |     OUI     |     NON     |
| Voir les tâches       |  OUI  |      OUI       |     OUI     |     OUI     |
| **Commentaires**      |       |                |             |             |
| Ajouter               |  OUI  |      OUI       |     OUI     |     OUI     |
| Modifier              |  OUI  |      OUI       | OUI\*\*\*\* | OUI\*\*\*\* |
| Supprimer             |  OUI  |      OUI       | OUI\*\*\*\* | OUI\*\*\*\* |
| **Pièces jointes**    |       |                |             |             |
| Uploader              |  OUI  |      OUI       |     OUI     |     NON     |
| Télécharger           |  OUI  |      OUI       |     OUI     |     OUI     |
| Supprimer             |  OUI  |      OUI       |     OUI     |     NON     |
| **Administration**    |       |                |             |             |
| Gérer les users       |  OUI  |      NON       |     NON     |     NON     |
| Gérer les authorities |  OUI  |      NON       |     NON     |     NON     |
| Métriques             |  OUI  |      NON       |     NON     |     NON     |
| Health checks         |  OUI  |      NON       |     NON     |     NON     |
| Configuration         |  OUI  |      NON       |     NON     |     NON     |
| Logs                  |  OUI  |      NON       |     NON     |     NON     |
| API docs              |  OUI  |      NON       |     NON     |     NON     |
| Console H2            |  OUI  |      NON       |     NON     |     NON     |
| **Dashboard**         |       |                |             |             |
| Dashboard KPI         |  OUI  |      OUI       | NON\*\*\*\* |     NON     |
| **Export CSV**        |       |                |             |             |
| Export projets        |  OUI  |      OUI       |     OUI     |     OUI     |
| Export tâches         |  OUI  |      OUI       |     OUI     |     OUI     |
| Export tâches projet  |  OUI  |    OUI\*\*     |     NON     |     NON     |
| Export users          |  OUI  |      NON       |     NON     |     NON     |

> \* Selon les permissions backend (vérification par `@PreAuthorize`)
> \*\* Uniquement si OWNER ou MANAGER du projet \*** Les USER ne peuvent pas créer de tâches via le formulaire standard
> \*\*** Selon la vérification côté frontend (créateur de la tâche ou OWNER/MANAGER du projet)

### 7.2 Permissions par rôle projet (dans un projet)

| Action                 | OWNER | MANAGER | MEMBER |
| ---------------------- | :---: | :-----: | :----: |
| Gérer les membres      |  OUI  |   OUI   |  NON   |
| Modifier le projet     |  OUI  |   OUI   |  NON   |
| Supprimer le projet    |  OUI  |   OUI   |  NON   |
| Créer des sprints      |  OUI  |   OUI   |  OUI   |
| Créer des epics        |  OUI  |   OUI   |  OUI   |
| Créer des tâches       |  OUI  |   OUI   |  OUI   |
| Exporter tâches projet |  OUI  |   OUI   |  NON   |

> **Note** : Un utilisateur avec `ROLE_ADMIN` est automatiquement OWNER de TOUS les projets.

---

## 8. Hiérarchie des entités

```
Projet
  ├── Membres (ProjectMember) ──→ Utilisateur (rôle: OWNER/MANAGER/MEMBER)
  ├── Sprints (Sprint)
  │     └── Tâches (Task) ──→ Assigné à un utilisateur
  ├── Epics (Epic)
  │     └── Tâches (Task)
  └── Tâches (Task)
        ├── Créé par → Utilisateur
        ├── Assigné à → Utilisateur
        ├── Commentaires (Comment) ──→ Auteur (Utilisateur)
        ├── Pièces jointes (Attachment)
        └── Historique (TaskHistory / TaskTransition)
```

### Statuts des entités

**Sprint** : `PLANNED` → `ACTIVE` → `COMPLETED` (ou `CANCELLED`)

**Epic** : `TODO` → `IN_PROGRESS` → `DONE` (ou `CANCELLED`)

**Tâche** : `NEW` → `TODO` → `IN_PROGRESS` → `IN_REVIEW` → `DONE` (ou `CANCELLED`)

**Priorité** : `LOWEST` < `LOW` < `MEDIUM` < `HIGH` < `HIGHEST`

**Type de tâche** : `STORY`, `BUG`, `TASK`, `SUBTASK`, `IMPROVEMENT`

---

## 9. Navigation entre les pages

### 9.1 Flux de navigation global

```
                    ┌─────────────┐
                    │   /login    │
                    └──────┬──────┘
                           │ connexion
                    ┌──────▼──────┐
                    │    /home    │
                    │  Dashboard  │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
       ┌──────▼──────┐    │     ┌──────▼──────┐
       │  /project   │    │     │  /account   │
       │   (liste)   │    │     │  (settings) │
       └──────┬──────┘    │     └─────────────┘
              │            │
    ┌─────────┼─────────┐  │
    │         │         │  │
┌───▼───┐ ┌──▼──┐ ┌───▼─┐│
│Sprints│ │Epics│ │Tasks││
└───┬───┘ └──┬──┘ └───┬─┘│
    │        │        │  │
    ▼        ▼        ▼  │
  Détail  Détail  Détail │
  Sprint   Epic    Task  │
                   │     │
            ┌──────┼─────┘
            │      │
         ┌──▼──┐ ┌─▼──────┐
         │Comm.│ │Attachm.│
         └─────┘ └────────┘
```

### 9.2 Tableau de correspondance URL → Page

| URL                             | Page                     | Rôles autorisés                  |
| ------------------------------- | ------------------------ | -------------------------------- |
| `/login`                        | Page de connexion        | Tous (non authentifié)           |
| `/home`                         | Dashboard / Accueil      | Tous (authentifié)               |
| `/account/register`             | Inscription              | Tous (non authentifié)           |
| `/account/settings`             | Paramètres du profil     | Tous (authentifié)               |
| `/account/password`             | Changer le mot de passe  | Tous (authentifié)               |
| `/notifications`                | Liste des notifications  | Tous (authentifié)               |
| `/project`                      | Liste des projets        | Tous (authentifié)               |
| `/project/new`                  | Créer un projet          | ADMIN, PROJET_MANAGER            |
| `/project/:key/view`            | Détails d'un projet      | Tous (authentifié)               |
| `/project/:key/edit`            | Modifier un projet       | ADMIN, PROJET_MANAGER            |
| `/project/:key/sprint`          | Liste des sprints        | Tous (authentifié)               |
| `/project/:key/sprint/new`      | Créer un sprint          | ADMIN, PROJET_MANAGER, USER      |
| `/project/:key/sprint/:id/view` | Détails d'un sprint      | Tous (authentifié)               |
| `/project/:key/sprint/:id/edit` | Modifier un sprint       | ADMIN, PROJET_MANAGER, USER      |
| `/project/:key/epic`            | Roadmap des epics        | Tous (authentifié)               |
| `/project/:key/epic/table`      | Liste des epics          | Tous (authentifié)               |
| `/project/:key/epic/new`        | Créer un epic            | ADMIN, PROJET_MANAGER, USER      |
| `/project/:key/epic/:id/view`   | Détails d'un epic        | Tous (authentifié)               |
| `/project/:key/epic/:id/edit`   | Modifier un epic         | ADMIN, PROJET_MANAGER, USER      |
| `/project/:key/task`            | Liste des tâches         | Tous (authentifié)               |
| `/project/:key/task/new`        | Créer une tâche          | ADMIN, PROJET_MANAGER, DEVELOPER |
| `/project/:key/task/:id/view`   | Détails d'une tâche      | Tous (authentifié)               |
| `/project/:key/task/:id/edit`   | Modifier une tâche       | ADMIN, PROJET_MANAGER, DEVELOPER |
| `/user-management`              | Gestion des utilisateurs | ADMIN uniquement                 |
| `/admin/metrics`                | Métriques                | ADMIN uniquement                 |
| `/admin/health`                 | Diagnostics              | ADMIN uniquement                 |
| `/admin/configuration`          | Configuration            | ADMIN uniquement                 |
| `/admin/logs`                   | Logs                     | ADMIN uniquement                 |
| `/admin/docs`                   | API docs                 | ADMIN uniquement                 |

### 9.3 Comment naviguer — Scénarios

#### Scénario 1 : Un ADMIN veut tout gérer

```
/login → /home (voit le dashboard complet)
       → /project (voit tous les projets)
       → /project/new (crée un projet)
       → /project/PRJ/view (voit détails + gère membres)
       → /project/PRJ/task (gère les tâches)
       → /project/PRJ/task/5/view (voit détails d'une tâche)
       → /user-management (gère les utilisateurs)
       → /admin/metrics (voit les métriques)
```

#### Scénario 2 : Un PROJET_MANAGER organise un projet

```
/login → /home (voit le dashboard)
       → /project (voit les projets)
       → /project/new (crée un projet)
       → /project/MOB/view (ajoute des membres)
       → /project/MOB/sprint (crée un sprint)
       → /project/MOB/epic (crée un epic)
       → /project/MOB/task (crée des tâches, les assigne)
```

#### Scénario 3 : Un DEVELOPER travaille sur les tâches

```
/login → /home (page d'accueil simple)
       → /project (voit les projets)
       → /project/PRJ/task (voit les tâches)
       → /project/PRJ/task/3/view (voit détails, commente, attache un fichier)
       → /project/PRJ/task/3/edit (modifie la tâche)
       → Kanban : glisse une carte pour changer le statut
```

#### Scénario 4 : Un USER consulte et commente

```
/login → /home (page d'accueil simple)
       → /project (voit les projets)
       → /project/PRJ/sprint (crée/modifie un sprint)
       → /project/PRJ/epic (crée/modifie un epic)
       → /project/PRJ/task (voit les tâches)
       → /project/PRJ/task/7/view (ajoute un commentaire)
```

---

## 10. Comptes de test

| Login     | Mot de passe | Rôle(s)                         | Dashboard             |
| --------- | ------------ | ------------------------------- | --------------------- |
| `admin`   | `admin`      | ROLE_ADMIN + ROLE_USER          | Dashboard KPI complet |
| `manager` | `manager`    | ROLE_PROJET_MANAGER + ROLE_USER | Dashboard KPI complet |
| `dev`     | `dev`        | ROLE_DEVELOPER + ROLE_USER      | Page d'accueil simple |
| `user`    | `user`       | ROLE_USER                       | Page d'accueil simple |

> **Note** : Le dashboard KPI s'affiche uniquement pour `ROLE_ADMIN` et `ROLE_PROJET_MANAGER`. Les autres rôles voient une page d'accueil basique avec un message de bienvenue.

---

_Document généré le 2026-07-20 — Projet gestion_taches (JHipster 9.1.0)_
