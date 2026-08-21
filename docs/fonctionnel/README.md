# Gestion Tâches — Documentation fonctionnelle

## Introduction

Cette documentation décrit le **comportement métier** de l'application **Gestion Tâches**, un outil de gestion de projets agiles (type Jira). Elle s'adresse à des lecteurs non techniques : chefs de projet, développeurs, utilisateurs avancés.

L'application repose sur quatre niveaux d'organisation :

1. **Projet** : cadre de travail d'une équipe (un propriétaire, des membres, un canal de discussion).
2. **Épic** : thématique de travail de haut niveau qui regroupe des tâches.
3. **Sprint** : période de travail itérative dans laquelle on planifie des tâches.
4. **Tâche** : unité de travail à réaliser (assignée à une personne, rattachée à un sprint et/ou un épic).

Des fonctions transverses complètent l'outil : commentaires, pièces jointes, chat de projet, notifications en temps réel, recherche, tableaux de bord, exports et journal d'audit.

## Les modules

| Module | Description | Fiche |
|--------|-------------|-------|
| Projets | Création, membres, rôles, suppression | [roles-et-permissions.md](./roles-et-permissions.md) |
| Épics | Regroupement de tâches par thématique | [cycle-de-vie-task.md](./cycle-de-vie-task.md) |
| Sprints | Planification et déroulement des itérations | [cycle-de-vie-sprint.md](./cycle-de-vie-sprint.md) |
| Tâches | Cycle de vie d'une tâche (statuts, assignation) | [cycle-de-vie-task.md](./cycle-de-vie-task.md) |
| Commentaires & pièces jointes | Discussions et fichiers liés à une tâche | [roles-et-permissions.md](./roles-et-permissions.md) |
| Chat | Conversations du projet (# Général + messages privés) | [chat.md](./chat.md) |
| Notifications | Alertes en temps réel et en différé | [notifications.md](./notifications.md) |

## Les deux niveaux de rôles

L'application distingue deux systèmes de rôles distincts :

- **Rôles globaux** (niveau compte) : `ADMIN`, `PROJET_MANAGER`, `DEVELOPER`, `USER`. Ils déterminent les fonctions accessibles dans l'application (administration, gestion de projets, tableaux de bord…).
- **Rôles projet** (niveau membre) : `OWNER`, `MANAGER`, `MEMBER`. Ils déterminent ce qu'un membre peut faire **dans un projet donné**.

> Le rôle global `ADMIN` est le seul qui **dépasse** les rôles projet : un administrateur est considéré comme `OWNER` de tous les projets. Tous les autres utilisateurs, y compris les `PROJET_MANAGER`, sont limités aux projets dont ils sont membres.

Voir la matrice complète dans [roles-et-permissions.md](./roles-et-permissions.md).

## Convention de dénomination

- **Statuts de tâche** : `NEW` (Nouveau), `IN_PROGRESS` (En cours), `READY_FOR_TEST` (Prêt pour test), `DONE` (Terminé), `NEEDS_INFO` (Besoin d'infos).
- **Statuts de sprint** : `PLANNED` (Planifié), `ACTIVE` (En cours), `COMPLETED` (Clôturé), `CANCELLED` (Annulé).
- **Statuts d'épic** : `TODO` (À faire), `IN_PROGRESS` (En cours), `DONE` (Terminé), `CANCELLED` (Annulé).
- **Priorités** : `LOWEST` (La plus basse), `LOW` (Basse), `MEDIUM` (Moyenne), `HIGH` (Haute), `HIGHEST` (La plus haute).

> **Note importante** : dans cette application, une tâche n'a **pas de type** (pas de distinction Story / Bug / Tâche technique / Sous-tâche / Amélioration). Toutes les tâches suivent le même cycle de vie. Voir [cycle-de-vie-task.md](./cycle-de-vie-task.md).

## Règles transverses principales

- **Un seul sprint actif** par projet à la fois.
- Les statuts d'un **épic** et d'un **sprint** se recalculent automatiquement selon l'état des tâches qu'ils contiennent.
- La clôture d'un sprint **repousse automatiquement** les tâches non terminées dans le backlog.
- Un **MEMBER** ne peut modifier que les tâches qui lui sont assignées, et ne peut **jamais** en changer l'assignation.
- Les **notifications** sont persistées, poussées en temps réel et purgées après 15 jours ; voir [notifications.md](./notifications.md).

## Navigation

- **Fiches fonctionnelles** :
  - [roles-et-permissions.md](./roles-et-permissions.md) — Rôles, matrices de permissions, règles d'accès
  - [cycle-de-vie-task.md](./cycle-de-vie-task.md) — Cycle de vie des tâches et des épics
  - [cycle-de-vie-sprint.md](./cycle-de-vie-sprint.md) — Cycle de vie des sprints, backlog, vélocité
  - [notifications.md](./notifications.md) — Toutes les notifications et leurs destinataires
  - [chat.md](./chat.md) — Conversations, messages
- **Documentation technique / API REST** : [docs/api/](../api/README.md) — endpoints, payloads, réponses.
