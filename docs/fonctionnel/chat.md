# Chat du projet

## Vue d'ensemble

Chaque projet dispose d'un **espace de discussion** organisé en conversations. Le chat fonctionne en **requêtes REST classiques** (les messages sont chargés à la demande ; il n'y a pas de flux temps réel de messages dans le chat, contrairement aux notifications).

Toutes les fonctionnalités du chat exigent d'être **membre du projet** (les `ADMIN` et `PROJET_MANAGER` globaux y ont accès même sans en être membres).

## 1. Les conversations

| Type | Intitulé | Description |
|------|----------|-------------|
| `GENERAL` | **# Général** | Canal partagé du projet, **créé automatiquement** au premier accès. Tous les membres du projet y participent. |
| `DIRECT` | Conversation privée | Discussion privée entre **deux membres** du projet. |

### Règles de création

- Le **# Général** est créé automatiquement (si absent) dès qu'un membre consulte les conversations du projet. Tous les membres présents à ce moment y sont ajoutés.
- Une conversation **privée** est créée à la demande entre deux membres :
  - impossible avec soi-même ;
  - les **deux interlocuteurs doivent être membres** du projet ;
  - si une conversation existe déjà entre les deux, elle est réutilisée (pas de doublon).

### Accès

| Conversation | Accès |
|--------------|-------|
| # Général | Tout membre du projet. |
| Privée | Uniquement les **participants** de la conversation (ou ADMIN/PROJET_MANAGER globaux). |

La liste des conversations affiche, pour chacune : le dernier message (extrait), le nombre de **messages non lus**, et les participants.

## 2. Les messages

### Envoi

- Contenu obligatoire, **de 1 à 5000 caractères**.
- Envoyer un message **marque la conversation comme lue** pour l'auteur.

### Consultation

- **Pagination par curseur** : les messages sont chargés du **plus ancien au plus récent**, page par page (remontée dans l'historique). Taille de page limitée à **100 messages**.
- La recherche permet de retrouver des messages dans toutes les conversations accessibles.

### Modification

- Un utilisateur ne peut modifier que **ses propres messages**.
- La modification horodate la correction (champ « modifié le »).

### Suppression

- Un utilisateur ne peut supprimer que **ses propres messages**.
- La suppression est **douce** (soft delete) : le message n'est pas physiquement effacé, son contenu est remplacé par *« message deleted »*.

## 3. Récapitulatif des actions

| Action | Qui peut le faire | Règle |
|--------|-------------------|-------|
| Consulter les conversations | Membre du projet | Le # Général est toujours présent ; les privées nécessitent d'être participant. |
| Ouvrir une conversation privée | Membre du projet | Les deux interlocuteurs doivent être membres ; impossible avec soi-même. |
| Envoyer un message | Participant de la conversation | Contenu 1–5000 caractères. |
| Modifier un message | **Auteur** uniquement | Le contenu est remplacé et horodaté. |
| Supprimer un message | **Auteur** uniquement | Suppression douce, contenu remplacé. |
| Rechercher des messages | Membre du projet | Sur les conversations accessibles. |
| Voir les membres du projet | Membre du projet | Avec leur rôle dans le projet. |
| Marquer une conversation comme lue | Participant | Met à jour le compteur de non-lus. |
