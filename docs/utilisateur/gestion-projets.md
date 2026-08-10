# Gestion des projets

## Voir la liste des projets

1. Dans la barre latérale, cliquez sur **Projects**.
2. La page affiche les projets sous forme de cartes. Chaque carte montre :
   - Le nom du projet et sa clé unique
   - Une courte description
   - La progression globale (barre de pourcentage)
   - Les avatars des membres
   - Le nom du responsable

💡 Astuce : utilisez le bouton **"Mes projets" / "Tous les projets"** en haut de la page pour filtrer la vue selon vos propres projets ou l'ensemble des projets auxquels vous avez accès.

## Créer un projet

**Rôle requis** : `ROLE_ADMIN` ou `ROLE_PROJET_MANAGER`

1. Sur la page Projects, cliquez sur le bouton **"Créer un projet"** en haut à droite.
2. Renseignez le formulaire :
   - **Nom** : nom du projet (obligatoire, 1 à 100 caractères)
   - **Description** : description du projet (optionnelle, 500 caractères maximum)
   - **Clé du projet** : identifiant court et unique du projet (obligatoire, 2 à 10 caractères, par exemple `PROJ1`, `DEV2026`)
3. Cliquez sur **"Sauvegarder"**.

💡 Astuce : la clé du projet est utilisée dans les URLs et les identifiants de tâches. Choisissez-la courte et explicite.

## Consulter un projet

1. Sur la page Projects, cliquez sur la carte du projet souhaité.
2. La page de détail s'affiche avec les informations du projet : nom, clé, description, date de création.
3. Trois onglets sont disponibles :
   - **Members** : gérer les membres du projet
   - **Discussion** : accéder au chat de projet

## Inviter des membres

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_USER`

1. Ouvrez un projet, puis cliquez sur l'onglet **Members**.
2. Cliquez sur le bouton **"Add member"**.
3. Dans la liste déroulante, sélectionnez l'utilisateur à ajouter.
4. Choisissez le **rôle** du membre dans le projet :
   - **OWNER** : propriétaire du projet, pleins droits
   - **MANAGER** : gestionnaire, peut modifier les tâches, sprints et epics
   - **MEMBER** : membre, peut consulter et contribuer
5. Cliquez sur **"Save"**.

💡 Astuce : si vous êtes administrateur global mais pas membre explicite du projet, un bandeau bleu "Accès administrateur" s'affiche pour vous en informer.

## Modifier le rôle d'un membre

1. Dans l'onglet **Members** du projet, repérez la ligne du membre dont vous voulez modifier le rôle.
2. Cliquez sur l'icône **crayon** à droite de la ligne.
3. Sélectionnez le nouveau rôle dans la liste déroulante.
4. Cliquez sur l'icône **coche** pour confirmer, ou sur **croix** pour annuler.

## Supprimer un membre

1. Dans l'onglet **Members**, cliquez sur l'icône **corbeille** à droite de la ligne du membre.
2. Confirmez la suppression.

⚠️ Attention : cette action est immédiate. Le membre perd l'accès au projet.

## Modifier un projet

1. Ouvrez un projet.
2. Cliquez sur le bouton **"Editer"** en haut à droite de la page.
3. Modifiez les champs souhaités (nom, description).
4. Cliquez sur **"Sauvegarder"**.

⚠️ Attention : la clé du projet ne peut pas être modifiée après la création.

## Supprimer un projet

1. Ouvrez un projet.
2. Cliquez sur le bouton **"Supprimer"** (bouton rouge) en haut à droite.
3. Confirmez la suppression dans la boîte de dialogue.

⚠️ Attention : la suppression d'un projet est **irréversible**. Toutes les tâches, sprints, epics, conversations et messages associés seront également supprimés.
