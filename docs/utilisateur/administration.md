# Administration

Cette section est réservée aux utilisateurs disposant du rôle **`ROLE_ADMIN`**. Les fonctionnalités décrites ici ne sont pas accessibles aux autres rôles.

## Accéder à l'administration

1. Connectez-vous avec un compte administrateur.
2. Dans la barre latérale, dépliez la section **Paramètres**.
3. Les liens d'administration sont listés sous la section **Admin** :
   - Utilisateurs
   - Autorités
   - Toutes les tâches
   - Tous les membres
   - Toutes les notifications
   - Métriques
   - Diagnostics
   - Configuration
   - Logs
   - API (si activé)

## Gérer les utilisateurs

### Voir la liste des utilisateurs

1. Cliquez sur **Utilisateurs** dans le menu d'administration.
2. La page affiche un tableau avec tous les utilisateurs du système : identifiant, email, statut (activé / désactivé), langue, rôles, dates de création et de modification.
3. En haut, des **statistiques** résument le nombre total d'utilisateurs par rôle :
   - Total
   - Admins
   - Managers
   - Développeurs

### Créer un utilisateur

1. Sur la page Utilisateurs, cliquez sur le bouton **"Créer un nouvel utilisateur"**.
2. Renseignez le formulaire : nom d'utilisateur, email, mot de passe, rôles attribués.
3. Cliquez sur **"Sauvegarder"**.

💡 Astuce : le mot de passe doit respecter les règles de sécurité définies (minimum 4 caractères).

### Activer / Désactiver un utilisateur

1. Dans la liste, repérez la colonne **Activé**.
2. Cliquez sur le bouton correspondant pour changer le statut :
   - **Désactivé** : bouton rouge, l'utilisateur ne peut plus se connecter
   - **Activé** : bouton vert, l'utilisateur peut se connecter

### Modifier un utilisateur

1. Dans la liste, cliquez sur l'icône **crayon** sur la ligne de l'utilisateur.
2. Modifiez les informations souhaitées.
3. Cliquez sur **"Sauvegarder"**.

### Supprimer un utilisateur

1. Dans la liste, cliquez sur l'icône **corbeille** sur la ligne de l'utilisateur.
2. Confirmez la suppression.

⚠️ Attention : la suppression est irréversible. Assurez-vous que l'utilisateur n'est pas assigné à des tâches en cours avant de le supprimer.

## Gérer les autorités (rôles système)

1. Cliquez sur **Autorités** dans le menu d'administration.
2. La page liste tous les rôles système existants :
   - `ROLE_ADMIN` : administrateur global
   - `ROLE_USER` : utilisateur standard
   - `ROLE_DEVELOPER` : développeur
   - `ROLE_PROJET_MANAGER` : chef de projet
3. Pour chaque rôle, vous pouvez le consulter, le modifier ou le supprimer selon les besoins.

## Voir toutes les tâches

1. Cliquez sur **Toutes les tâches** dans le menu d'administration.
2. Cette vue affiche l'ensemble des tâches de tous les projets, avec possibilité de recherche, filtres et tri.
3. Elle permet de superviser l'activité globale et d'intervenir sur une tâche sans naviguer projet par projet.

## Voir tous les membres de projets

1. Cliquez sur **Tous les membres** dans le menu d'administration.
2. Le tableau affiche pour chaque membre : identifiant, projet, clé du projet, rôle dans le projet, date d'ajout.
3. Cela permet de vérifier qui a accès à quel projet et avec quel rôle.

## Voir toutes les notifications

1. Cliquez sur **Toutes les notifications** dans le menu d'administration.
2. La page liste l'ensemble des notifications du système, avec possibilité de recherche et de tri.
3. Cela permet de suivre l'activité globale et de détecter d'éventuels problèmes.

## Consulter les métriques

1. Cliquez sur **Métriques** dans le menu d'administration.
2. La page affiche des indicateurs de performance de l'application : temps de réponse, taux d'erreur, consommation mémoire, etc.

## Vérifier la santé de l'application

1. Cliquez sur **Diagnostics** dans le menu d'administration.
2. La page affiche l'état de santé global de l'application et de ses composants (base de données, espace disque, etc.).

## Consulter la configuration

1. Cliquez sur **Configuration** dans le menu d'administration.
2. La page liste toutes les propriétés de configuration de l'application (paramètres système, base de données, sécurité, etc.).

## Voir les logs

1. Cliquez sur **Logs** dans le menu d'administration.
2. La page affiche les journaux d'activité de l'application pour le diagnostic d'incidents.

## Accéder à la documentation API

1. Si l'option est activée, cliquez sur **API** dans le menu d'administration.
2. La page Swagger UI affiche la documentation interactive de toutes les API REST disponibles.
