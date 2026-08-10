# Sprints et Epics

## Créer un sprint

**Rôle requis** : `ROLE_PROJET_MANAGER` ou `ROLE_ADMIN`

1. Ouvrez un projet, puis cliquez sur **Sprints** dans la barre latérale (ou naviguez vers `/project/{clé}/sprint`).
2. Cliquez sur le bouton **"Créer un sprint"**.
3. Renseignez le formulaire :
   - **Nom** : nom du sprint (obligatoire)
   - **Objectif** : description de l'objectif du sprint (optionnelle)
   - **Date de début** : date à laquelle le sprint commence
   - **Date de fin** : date à laquelle le sprint se termine
   - **Statut** : initialement `PLANNED` (planifié)
4. Cliquez sur **"Sauvegarder"**.

## Démarrer un sprint

1. Dans la liste des sprints, sélectionnez le sprint que vous souhaitez démarrer.
2. Si son statut est `PLANNED`, le bouton **"Démarrer"** (icône play) apparaît.
3. Cliquez sur **"Démarrer"**. Le statut passe à `ACTIVE`.

⚠️ Attention : un seul sprint peut être actif à la fois par projet. Si un autre sprint est déjà actif, le bouton Démarrer est désactivé.

## Compléter un sprint

1. Ouvrez un sprint dont le statut est `ACTIVE`.
2. Cliquez sur le bouton **"Compléter"** (icône coche verte).
3. Une fenêtre de **rapport de vélocité** s'affiche :
   - **Tâches prévues** : nombre de story points prévus dans le sprint
   - **Tâches terminées** : nombre de story points effectivement complétés
   - **Tâches reportées** : nombre de story points non terminés
   - **Pourcentage** : ratio accompli / prévu
4. Cliquez sur **"Fermer"** pour revenir au sprint.

Le statut du sprint passe alors à `COMPLETED`.

## Suivre la progression du sprint

La page d'un sprint affiche en haut :

- Une **barre de progression** indiquant le nombre de tâches terminées sur le nombre total (ex: `3 / 5 — 60%`)
- Le **nom du sprint**, son objectif, ses dates et son statut

## Utiliser le tableau de board (Kanban)

1. Dans un sprint actif ou planifié, sélectionnez l'onglet **Board**.
2. Le board affiche les tâches réparties en colonnes selon leur statut : `NEW`, `IN_PROGRESS`, `READY_FOR_TEST`, `DONE`, `NEEDS_INFO`.
3. Pour déplacer une tâche vers une autre colonne :
   - **Glissez-déposez** la carte de la tâche vers la colonne souhaitée (si vous êtes `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou si la tâche vous est assignée)
   - ou cliquez sur la tâche pour ouvrir le panneau de détail et changer son statut via le menu déroulant

💡 Astuce : utilisez les filtres en haut du board pour afficher uniquement les tâches d'un assigné ou d'une priorité donnée.

## Planifier le backlog

1. Dans un sprint, sélectionnez l'onglet **Planning**.
2. La vue affiche deux colonnes :
   - **Backlog** : tâches non encore assignées à un sprint
   - **Sprint** : tâches actuellement dans le sprint
3. Pour ajouter une tâche au sprint : glissez-la depuis le Backlog vers le Sprint.
4. Pour retirer une tâche du sprint : glissez-la depuis le Sprint vers le Backlog.

⚠️ Attention : le glisser-déposer est désactivé quand le sprint est `ACTIVE` ou `COMPLETED`. La planification n'est possible que sur un sprint `PLANNED`.

## Voir la table des sprints

1. Dans la page Sprints, cliquez sur le bouton **"Table"** en haut à droite.
2. La vue tableau liste tous les sprints du projet avec : nom, statut, dates de début/fin, nombre de tâches.

## Consulter le burndown chart

1. Ouvrez un sprint.
2. Cliquez sur l'onglet **Burndown**.
3. Le graphique affiche :
   - La ligne idéale de burndown (pente théorique)
   - La ligne réelle de burndown (progression effective)
   - Des statistiques : tâches terminées, tâches restantes, vélocité

## Consulter la timeline

1. Ouvrez un sprint.
2. Cliquez sur l'onglet **Timeline**.
3. La vue chronologique affiche chaque tâche sous forme de barre horizontale, positionnée selon sa date de début et de fin.

---

## Créer un Epic

**Rôle requis** : `ROLE_PROJET_MANAGER` ou `ROLE_ADMIN`

1. Ouvrez un projet, puis cliquez sur **Epics** dans la barre latérale.
2. Cliquez sur le bouton **"Créer un epic"**.
3. Renseignez le formulaire :
   - **Titre** : nom de l'epic (obligatoire)
   - **Description** : détail de l'epic (optionnelle)
   - **Statut** : `TODO`, `IN_PROGRESS`, `DONE` ou `CANCELLED`
   - **Priorité** : `LOWEST`, `LOW`, `MEDIUM`, `HIGH` ou `HIGHEST`
   - **Date de début** : date de début prévue
   - **Date de fin** : date de fin prévue
4. Cliquez sur **"Sauvegarder"**.

## Consulter la Roadmap des Epics

1. Sur la page Epics, cliquez sur le bouton **"Roadmap"** en haut à droite.
2. La vue roadmap affiche tous les epics sous forme de barres horizontales, avec :
   - Le titre et la progression de chaque epic
   - Les dates de début et de fin
   - Un code couleur selon le statut (TODO = gris, IN_PROGRESS = bleu, DONE = vert, CANCELLED = rouge)
3. Utilisez les filtres en haut pour trier par statut, projet ou priorité.

## Consulter le détail d'un Epic

1. Dans la liste des epics ou dans la roadmap, cliquez sur un epic.
2. La page de détail affiche :
   - Les informations de l'epic (titre, description, statut, priorité, dates)
   - Des statistiques : nombre total de tâches, tâches terminées, en cours, à faire, story points
   - Les sprints associés
3. Plusieurs onglets sont disponibles :
   - **Tasks** : tableau des tâches liées à l'epic
   - **Kanban** : vue Kanban des tâches de l'epic
   - **Burndown** : graphique de progression de l'epic
   - **Timeline** : vue chronologique des tâches

## Créer une tâche depuis un Epic

1. Ouvrez un epic.
2. Cliquez sur le bouton **"Create Task"**.
3. Renseignez le formulaire de tâche (voir le guide [Gestion des tâches](./gestion-taches.md)).
