# Gestion des tâches

## Créer une tâche

Il existe plusieurs façons de créer une tâche :

- Depuis la page **Tasks** d'un projet
- Depuis un **Sprint** (onglet Board ou Planning)
- Depuis un **Epic** (détail de l'epic)

### Étapes de création

1. Cliquez sur le bouton **"Create task"** ou **"Créer une tâche"**.
2. Renseignez le formulaire :
   - **Titre** : intitulé de la tâche (obligatoire, 200 caractères maximum)
   - **Description** : détail de la tâche à réaliser (optionnelle, 5000 caractères maximum)
   - **Statut** : `NEW` (nouvelle), `IN_PROGRESS` (en cours), `READY_FOR_TEST` (prête pour test), `DONE` (terminée), `NEEDS_INFO` (information requise)
   - **Priorité** : `LOWEST`, `LOW`, `MEDIUM`, `HIGH` ou `HIGHEST`
   - **Projet** : projet auquel la tâche est rattachée
   - **Sprint** : sprint auquel la tâche est assignée (optionnel)
   - **Epic** : epic auquel la tâche est rattachée (optionnel)
   - **Assigné** : membre de l'équipe responsable de la tâche (optionnel)
3. Cliquez sur **"Sauvegarder"**.

💡 Astuce : si vous créez la tâche depuis un sprint ou un epic, le projet, le sprint ou l'epic sont pré-remplis automatiquement.

## Voir la liste des tâches

1. Ouvrez un projet, puis cliquez sur **Tasks** dans la barre latérale.
2. La page affiche un tableau avec : titre, statut, priorité, sprint, epic, assigné.
3. Utilisez la **barre de recherche** en haut pour filtrer par mot-clé.
4. Cliquez sur une ligne pour ouvrir le **panneau de détail** de la tâche.

💡 Astuce : la pagination en bas de page permet de naviguer entre les pages si la liste est longue.

## Consulter le détail d'une tâche

1. Cliquez sur une tâche dans la liste.
2. Le panneau de détail s'ouvre avec plusieurs onglets :
   - **Détails** : titre, description, statut, priorité, dates, projet, sprint, epic, créé par, assigné
   - **Commentaires** : échanges autour de la tâche
   - **Pièces jointes** : fichiers liés à la tâche
   - **Historique** : journal des modifications

3. Pour revenir à la liste, cliquez sur le bouton **Retour** ou fermez le panneau.

## Modifier le statut d'une tâche

### Via le panneau de détail

1. Ouvrez une tâche.
2. Dans le panneau de détail, utilisez le menu déroulant **Statut** pour sélectionner le nouveau statut.
3. La modification est sauvegardée automatiquement.

### Via le tableau Kanban

1. Ouvrez un sprint (onglet **Board**) ou allez dans **Mes Tâches** (vue Kanban).
2. Glissez-déposez la carte de la tâche vers la colonne correspondant au nouveau statut.

⚠️ Attention : le glisser-déposer n'est autorisé que pour les administrateurs, les chefs de projet et pour les tâches qui vous sont assignées.

## Assigner une tâche

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_DEVELOPER`

1. Ouvrez une tâche en mode édition (bouton **"Editer"**).
2. Dans le champ **Assigné**, sélectionnez le membre de l'équipe.
3. Cliquez sur **"Sauvegarder"**.

💡 Astuce : seuls les utilisateurs ayant le rôle `ROLE_DEVELOPER` ou `ROLE_PROJET_MANAGER` peuvent être assignés à une tâche.

## Modifier une tâche

1. Ouvrez une tâche.
2. Cliquez sur le bouton **"Editer"**.
3. Modifiez les champs souhaités.
4. Cliquez sur **"Sauvegarder"**.

## Supprimer une tâche

1. Ouvrez une tâche.
2. Cliquez sur l'icône **corbeille** dans la liste ou sur le bouton **"Supprimer"** dans le détail.
3. Confirmez la suppression.

⚠️ Attention : la suppression est immédiate et irréversible. Tous les commentaires et pièces jointes associés sont également supprimés.

## Ajouter un commentaire

1. Ouvrez une tâche.
2. Cliquez sur l'onglet **Commentaires**.
3. Dans la zone de texte en bas, saisissez votre commentaire.
4. Cliquez sur **"Envoyer"** ou appuyez sur la touche **Entrée**.

💡 Astuce : les commentaires sont triés du plus récent au plus ancien. Le nom de l'auteur et la date apparaissent pour chaque commentaire.

## Joindre un fichier

1. Ouvrez une tâche.
2. Cliquez sur l'onglet **Pièces jointes**.
3. Utilisez le bouton d'ajout pour sélectionner un fichier depuis votre ordinateur.
4. Le fichier est envoyé et apparaît dans la liste des pièces jointes.

⚠️ Attention : la taille maximale d'un fichier est de 20 Mo. Les formats acceptés dépendent de la configuration du serveur.

## Consulter l'historique

1. Ouvrez une tâche.
2. Cliquez sur l'onglet **Historique**.
3. La liste affiche chronologiquement chaque modification : action effectuée, ancienne valeur, nouvelle valeur, date et auteur.

---

## Mes Tâches

La page **Mes Tâches** donne une vue personnalisée de toutes les tâches qui vous sont assignées.

### Accéder à Mes Tâches

1. Dans la barre latérale, cliquez sur **Mes Tâches**.

### Changer le mode d'affichage

1. En haut à droite, utilisez le bouton **Liste / Kanban** pour basculer entre :
   - **Liste** : tableau classique avec colonnes triables
   - **Kanban** : colonnes par statut avec glisser-déposer

💡 Astuce : votre préférence est sauvegardée automatiquement.

### Consulter une tâche

1. Dans la liste ou le Kanban, cliquez sur une tâche.
2. Le panneau de détail s'ouvre avec les mêmes onglets que la vue standard (Détails, Commentaires, Pièces jointes, Historique).
3. Vous pouvez y changer le statut de la tâche directement.

## Filtrer et rechercher les tâches

- **Recherche textuelle** : utilisez la barre de recherche en haut de la liste pour filtrer par titre
- **Filtres** : utilisez les menus déroulants pour filtrer par statut, priorité ou assigné
- **Tri** : cliquez sur les en-têtes de colonnes pour trier (titre, statut, priorité, etc.)
- **Pagination** : naviguez entre les pages en bas de la liste
