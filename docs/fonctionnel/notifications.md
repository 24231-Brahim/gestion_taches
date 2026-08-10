# Notifications

## Vue d'ensemble

L'application notifie les utilisateurs des événements importants, en **temps réel** et en **différé**. Chaque notification :

- est **persistée** dans l'historique de l'utilisateur destinataire (avec marqueur lu / non lu) ;
- est **poussée en temps réel** vers l'utilisateur connecté (WebSocket/STOMP, destination `/queue/notifications`) ;
- est également accessible via le **flux temps réel** (`GET /api/notifications/stream`, SSE).

## Toutes les notifications

| # | Événement | Destinataires | Contenu du message | Déclenchement |
|---|-----------|---------------|--------------------|---------------|
| 1 | **Création d'un sprint** | Tous les membres du projet, **sauf l'auteur** | « Un nouveau sprint "X" a été créé dans le projet Y » | Immédiat, à la création |
| 2 | **Démarrage d'un sprint** | Tous les membres du projet, **sauf l'auteur** | « Le sprint "X" a démarré » | Immédiat, au démarrage |
| 3 | **Assignation d'une tâche** | Le **nouvel assignataire** (sauf s'il est lui-même l'auteur de l'action) | « Vous avez été assigné à la tâche "..." » | Immédiat, à l'assignation ou réassignation |
| 4 | **Changement de statut d'une tâche** | L'**assignataire actuel** (sauf s'il est lui-même l'auteur de l'action) | « Le statut de la tâche "..." a changé pour STATUT » | Immédiat, à chaque changement de statut |
| 5 | **Tâche passée à `DONE`** | Le **créateur** s'il est `ADMIN`, et/ou l'**assignataire** s'il est `ADMIN` | « La tâche "..." que vous avez créée est passée à DONE » / « La tâche "..." qui vous est assignée est passée à DONE » | Immédiat, au passage en `DONE` |
| 6 | **Nouveau commentaire** | L'**assignataire** et le **créateur** de la tâche (sauf l'auteur du commentaire) | « Un nouveau commentaire a été ajouté sur la tâche "..." » | Immédiat, à l'ajout du commentaire |
| 7 | **Ajout au projet** | L'**utilisateur ajouté** (sauf s'il s'ajoute lui-même) | « Vous avez été ajouté au projet X » | Immédiat, à l'ajout du membre |
| 8 | **Suppression d'un projet** | Tous les membres du projet, **sauf l'auteur** | « Le projet X a été supprimé » | Immédiat, à la suppression |
| 9 | **Nouvel utilisateur inscrit** | Tous les `ADMIN` | « New user registered: login (email) » | Immédiat, à chaque inscription |
| 10 | **Entrée d'audit créée** | Tous les `ADMIN` | « Task "..." — ACTION » ou « Task "..." — ACTION (ancienne → nouvelle) » | Immédiat, à chaque entrée d'historique (manuelle ou déplacement au backlog) |
| 11 | **Tâche en retard** | Tous les `ADMIN` **+ l'assignataire** | « Task "..." is overdue — deadline has passed » | **Quotidien à 08h00** (tâche traitée une seule fois) |

## Détail des règles importantes

### Notification « tâche passée à DONE » (n°5)

Elle ne concerne **que les administrateurs** impliqués dans la tâche :
- si le **créateur** de la tâche est `ADMIN`, il est notifié ;
- si l'**assignataire** est `ADMIN`, il est notifié ;
- si créateur et assignataire sont la même personne, une seule notification est envoyée.

Cette règle existe pour signaler aux administrateurs l'achèvement des tâches qu'ils portent.

### Notification « tâche en retard » (n°11)

**Définition du retard** : une tâche **non terminée** (`DONE` exclu) est considérée en retard quand :
- elle est rattachée à un sprint dont la **date de fin est dépassée** ; **ou**
- elle est **sans sprint** et a été créée il y a **plus de 14 jours**.

**Rythme** : le contrôle est exécuté **chaque jour à 08h00**. Une tâche donnée n'est notifiée **qu'une seule fois** (le système mémorise qu'elle a déjà fait l'objet d'une notification « overdue »).

## Gestion de la boîte de réception

| Action | Comportement |
|--------|--------------|
| Lister mes notifications | Triées de la plus récente à la plus ancienne, paginées. |
| Compter mes notifications non lues | Nombre de notifications `isRead = false`. |
| Marquer une notification comme lue | Mise à jour du champ lu/non lu. |
| Tout marquer comme lu | Toutes mes notifications passent en « lue ». |
| Consultation par les administrateurs | Les ADMIN peuvent consulter les notifications de **tous** les utilisateurs. |

## Rétention

Les notifications de **plus de 15 jours** sont **supprimées automatiquement** chaque jour à **03h00**. Les messages ne sont pas conservés au-delà de cette durée.

## Récapitulatif des canaux

| Canal | Usage |
|-------|-------|
| Base de données (persistance) | Historique consultable, compteur non lu, rétention 15 jours. |
| WebSocket / STOMP (`/queue/notifications`) | Notification instantanée quand l'utilisateur est connecté. |
| SSE (`GET /api/notifications/stream`) | Flux temps réel des notifications de l'utilisateur connecté. |

## Points d'attention

- Un utilisateur qui déclenche une action sur lui-même (s'auto-assigner une tâche, changer le statut de sa propre tâche) **ne reçoit pas** de notification pour cette action : les destinataires excluent toujours l'auteur.
- Les notifications de **clôture de sprint** (déplacements de tâches au backlog) et d'**audit** sont destinées aux **administrateurs** — voir [audit.md](./audit.md).
