# Mises à jour nécessaires — gestion-taches

## Résumé du projet
- **Stack**: JHipster 9.1.0 (monolithe), Spring Boot 4.0.6, Angular 21.2.14, PostgreSQL 16
- **Build**: Maven + npm
- **Langages**: Java 21, TypeScript 5.9.3

## 1. Mises à jour de sécurité (critiques)

### 1.1 Externaliser le secret JWT
- **Fichier**: `.yo-rc.json:17`
- **Action**: Remplacer la valeur codée en dur de `jwtSecretKey` par une référence à une variable d'environnement ou un secret vault.
- **Impact**: Ce secret est actuellement visible dans le repo. En production, il doit être injecté via `JHIPSTER_JWT_SECRET_KEY` ou similaire.

### 1.2 Vérifier les mots de passe de démo
- **Fichiers**: `application-secret-samples.yml`, `seed-data.sql`
- **Action**: Confirmer que ces fichiers ne sont jamais déployés en production et que les profils `secret-samples` sont bien exclus du packaging prod.

## 2. Mises à jour backend (Maven)

### 2.1 Upgrade Spring Boot 4.0.6 → 4.1.0
- **Fichier**: `pom.xml:15`
- **Action**: Mettre à jour `<version>4.0.6</version>` vers `4.1.0` dans le parent Spring Boot.
- **Dépendances impactées**: Toutes les starters Spring Boot, Spring Security, Spring Data, Spring GraphQL, etc.
- **Validation**: Lancer `./mvnw verify` et les tests backend.

### 2.2 Mettre à jour les versions explicitement déclarées
- **Fichier**: `pom.xml` (propriétés)
- `springdoc-openapi-starter-webmvc-api.version`: `3.0.3` → `3.1.0`
- `archunit-junit5.version`: `1.4.2` → `1.5.0`
- `checkstyle.version`: `13.4.2` → `13.10.0`
- `mapstruct.version`: `1.6.3` → `1.7.0.Beta2` *(beta, évaluer la stabilité avant upgrade)*
- `config.version`: `1.4.8` → `1.4.9`
- `jacoco-maven-plugin.version`: `0.8.14` → dernière stable
- `jib-maven-plugin.version`: `3.5.1` → vérifier latest
- `spotless-maven-plugin.version`: `3.5.1` → vérifier latest
- `sonar-maven-plugin.version`: `5.6.0.6792` → vérifier latest

### 2.3 Dépendances avec mises à jour disponibles
- `com.github.ben-manes.caffeine:caffeine` / `jcache`: `3.2.3` → `3.2.4`
- `com.zaxxer:HikariCP`: `7.0.2` → `7.1.0`
- `org.postgresql:postgresql`: `42.7.10` → `42.7.13`
- `org.testcontainers:postgresql`: `1.20.1` → `1.21.4`
- `io.micrometer:micrometer-registry-prometheus`: `1.16.5` → `1.17.0`
- `org.apache.commons:commons-lang3`: `3.19.0` → `3.20.0`
- `org.hibernate.validator:hibernate-validator`: `9.0.1.Final` → `9.1.3.Final`
- `org.liquibase:liquibase-core`: `5.0.2` → `5.0.3`
- `org.springframework.security:spring-security-*`: `7.0.5` → `7.1.0`
- `org.springframework.data:spring-data-*`: `4.0.5` → `4.1.0`
- `org.hibernate.orm:hibernate-core`: `7.2.12.Final` → `8.0.0.Beta1` *(beta, attention)*

> Note: Beaucoup de ces versions sont gérées par le BOM JHipster / Spring Boot. Après upgrade du parent à 4.1.0, plusieurs se mettront à jour automatiquement.

## 3. Mises à jour frontend (npm)

### 3.1 Sass (critique — version très ancienne)
- **Fichier**: `package.json:137`
- **Actuel**: `"sass": "^1.57.1"`
- **Action**: Mettre à jour vers la dernière version stable 1.x (actuellement ~1.8x).
- **Validation**: `npm run build` et vérifier que le CSS compile correctement.

### 3.2 FontAwesome Angular (ancien)
- **Fichier**: `package.json:91`
- **Actuel**: `"@fortawesome/angular-fontawesome": "4.0.0"`
- **Action**: Évaluer la migration vers `@fortawesome/angular-fontawesome` v6+ et les packages d'icônes correspondants (`@fortawesome/free-solid-svg-icons` reste compatible).

### 3.3 Audit de sécurité npm
- **Action**: Lancer `npm audit` pour identifier les vulnérabilités réelles dans les dépendances frontend.
- **Fichier**: `package.json`

## 4. Outils et configuration

### 4.1 Vérifier Node.js
- **Exigé**: `>= 24.16.0` (déjà LTS actuel)
- **Action**: Aucune si la version installée est à jour.

### 4.2 Vérifier Java
- **Configuré**: Java 21
- **Action**: Aucune, la version est supportée.

### 4.3 AGENTS.md
- **Fichier manquant**: `AGENTS.md` à la racine
- **Action**: Créer si nécessaire pour documenter les workflows agents (optionnel selon le système).

## 6. Mises à jour de la documentation (`docs/`)

### 6.1 Fichiers à supprimer

| Fichier | Raison |
|---------|--------|
| `docs/api/group-message.md` | Entité `GroupMessage` supprimée (A.1) |
| `docs/api/task-history.md` | Entité `TaskHistory` supprimée (A.2) |
| `docs/fonctionnel/audit.md` | Tout le document décrit `TaskHistory` qui n'existe plus (A.2) |

### 6.2 `docs/fiche-classes.md`

- Supprimer la classe `GroupMessage` (lignes 101-105) et ses associations (lignes 204-206).
- Supprimer la classe `TaskHistory` (lignes 85-90) et ses associations (lignes 187-188, 192).
- Supprimer la classe `UserPresence` (lignes 125-127) et ses associations (ligne 213).
- Supprimer le champ `mentions` de `ChatMessage` (ligne 123) et mettre à jour la ligne 122 (`Set~Long~ mentions`).
- Dans le diagramme ER : supprimer `GROUP_MESSAGE`, `TASK_HISTORY`, `CHAT_USER_PRESENCE`, `CHAT_MESSAGE_MENTIONS` et leurs relations (lignes 342-388, 419).
- Mettre à jour le tableau "Dépendances entre Tables" : retirer `group_message`, `task_history`, `chat_user_presence`, `chat_message_mentions`.

### 6.3 `docs/api/README.md`

- Ligne 165 : retirer `[task-history.md](./task-history.md)`.
- Ligne 169 : retirer `[group-message.md](./group-message.md)`.
- Ligne 168 : mettre à jour `[chat.md](./chat.md)` pour refléter la suppression de présence et mentions.

### 6.4 `docs/api/chat.md`

- Supprimer le schéma `UserPresenceDTO` (lignes 49-55).
- Supprimer les champs `online` et `lastActiveAt` de `ChatMemberDTO` (lignes 46-47).
- Supprimer le champ `mentions` de `ChatMessageDTO` (ligne 35).
- Supprimer la section "Présence" entière (lignes 88-95).
- Supprimer la sous-section "Mentions" dans "Threads et mentions" (lignes 173-185).
- Supprimer les endpoints 10 et 11 (présence) (lignes 477-530).
- Nettoyer les exemples JSON : retirer `online`, `lastActiveAt`, `mentions` partout où ils apparaissent.

### 6.5 `docs/api/users.md`

- Ligne 223 : retirer `recentActivity` du schéma `UserAdminDetailDTO` (et la description ligne 209 si elle mentionne l'historique).

### 6.6 `docs/api/notification.md`

- Ligne 26 : modifier "Clôture de sprint (tâches non DONE déplacées) | Admins (via TaskHistory)" → "Clôture de sprint (tâches non DONE déplacées) | Admins".

### 6.7 `docs/api/sprint.md`

- Ligne 343 : modifier "un historique est créé" → "une notification est envoyée aux administrateurs".

### 6.8 `docs/technique/README.md`

- Mettre à jour Spring Boot : `4.0.6` → `4.1.0`.
- Vérifier et mettre à jour PostgreSQL si nécessaire (18.4 dans Docker).
- Retirer toute mention à `TaskHistory`, `GroupMessage`, `UserPresence` dans les descriptions de tables.

### 6.9 `docs/technique/architecture.md`

- Ligne 68 : retirer `UserPresenceRepository` de la liste des repositories.
- Vérifier qu'aucune mention à `TaskHistory`, `GroupMessage`, `UserPresence` ne subsiste.

### 6.10 `docs/technique/modele-donnees.md`

- Supprimer la section `TaskHistory` entière (lignes 152-163).
- Supprimer la section `UserPresence` entière (lignes 238-245).
- Supprimer le champ `mentions` de `ChatMessage` (ligne 231).
- Mettre à jour le diagramme Mermaid ER : retirer `TASK_HISTORY`, `USER_PRESENCE`, `CHAT_MESSAGE_MENTIONS` et leurs relations.
- Mettre à jour la note ligne 49 : ne plus mentionner `UserPresence`.
- Mettre à jour les relations de `Task` (retirer `1 ── * TaskHistory` ligne 129).
- Mettre à jour la section "ChatMessage" (retirer `mentions`).
- Ligne 349 : retirer la mention de `TaskHistory` dans "Règles métier".

### 6.11 `docs/technique/chat-temps-reel.md`

- Supprimer la section "Polling de la présence" (lignes 106-120).
- Supprimer la sous-section "Mentions" (lignes 173-185).
- Mettre à jour le tableau récapitulatif (lignes 196-202) : retirer présence et mentions.
- Nettoyer les références à `UserPresenceDTO`, `heartbeat()`, `PRESENCE_POLL_INTERVAL`.

### 6.12 `docs/fonctionnel/README.md`

- Ligne 27 : retirer "Audit | Journal des actions tracées" du tableau des modules.
- Ligne 66 : retirer la référence à `[audit.md](./audit.md)`.

### 6.13 `docs/fonctionnel/roles-et-permissions.md`

- Supprimer la section "Audit (TaskHistory)" entière (lignes 137-144).

### 6.14 `docs/fonctionnel/chat.md`

- Supprimer la section "Présence en ligne" (lignes 55-59).
- Supprimer la section "Mentions" (lignes 61-65).
- Mettre à jour le tableau récapitulatif (lignes 67-77) : retirer les lignes liées à la présence.

### 6.15 `docs/fonctionnel/notifications.md`

- Ligne 24 : modifier "Entrée d'audit créée | Tous les ADMIN | Task "..." — ACTION | Immédiat, à chaque entrée d'historique (manuelle ou déplacement au backlog)" → garder la notification de clôture de sprint mais sans référence à TaskHistory.
- Ligne 71 : retirer la référence à `[audit.md](./audit.md)`.

### 6.16 `docs/fonctionnel/cycle-de-vie-sprint.md`

- Ligne 70 : retirer la référence à `[audit.md](./audit.md)`.
- Ligne 118 : modifier "Clôture : tâches reportées au backlog | Tous les ADMIN | Task "..." — TASK_MOVED_TO_BACKLOG (...) | ..." pour enlever la référence à l'historique.

### 6.17 `docs/utilisateur/messagerie.md`

- Supprimer les lignes 69-75 (section "Voir qui est en ligne" — pastilles, compteur online, heartbeat).
- Ligne 22 : vérifier et retirer toute mention à la présence dans la description de l'interface.

### 6.18 `docs/utilisateur/gestion-taches.md`

- Ligne 43 : retirer "**Historique** : journal des modifications" de la liste des onglets.
- Lignes 105-109 : supprimer la section "Consulter l'historique".
- Ligne 132 : retirer "Historique" de la liste des onglets dans "Mes Tâches".

### 6.19 `docs/utilisateur/demarrage.md`

- Ligne 34 : vérifier si "activité récente" fait référence à TaskHistory ; si oui, remplacer par "tâches récentes".

### 6.20 `docs/utilisateur/administration.md`

- Vérifier les lignes faisant référence à l'historique ou à TaskHistory et les mettre à jour.

### 6.21 Versions à mettre à jour dans la documentation

| Fichier | Ancienne valeur | Nouvelle valeur |
|---------|----------------|-----------------|
| `docs/technique/README.md` (et autres docs citant les versions) | Spring Boot 4.0.6 | Spring Boot 4.1.0 |
| `docs/api/README.md` | Spring Boot 4.0.6 | Spring Boot 4.1.0 |

## 7. Plan d'exécution recommandé

1. **Sauvegarder** l'état actuel (`git status`, `git stash` si nécessaire).
2. **Sécurité d'abord** : externaliser `jwtSecretKey` dans `.yo-rc.json`.
3. **Upgrade Spring Boot** : passer le parent à `4.1.0`, résoudre les conflits de dépendances.
4. **Mettre à jour les propriétés Maven** explicitement déclarées.
5. **Lancer les tests backend** : `./mvnw verify`.
6. **Mettre à jour Sass** et lancer `npm run build`.
7. **Lancer `npm audit`** et corriger les vulnérabilités critiques/high.
8. **FontAwesome** : évaluer la migration selon les icônes utilisées dans le codefront.
9. **Tests E2E** : lancer Cypress si applicable.
10. **Documentation** : appliquer les modifications listées en §6.
