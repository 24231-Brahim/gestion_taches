# Dashboards & Statistiques

Endpoints agrégés servant les tableaux de bord de l'application. Aucun n'accepte de paramètre de requête ; chaque réponse est calculée à la volée (lecture seule, `@Transactional(readOnly = true)`).

## Vue d'ensemble

| Endpoint | Description | Rôle requis |
|----------|-------------|-------------|
| `GET /api/dashboard/kpis` | KPI globaux (ou scopés aux projets de l'utilisateur) | Authentifié |
| `GET /api/developer-dashboard/statistics` | Statistiques personnelles de l'utilisateur courant | `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_DEVELOPER` |
| `GET /api/admin/stats` | Statistiques système complètes | `ROLE_ADMIN` |

---

## 1. KPI du dashboard principal

```http
GET /api/dashboard/kpis
Authorization: Bearer <token>
```

**Rôle requis** : authentifié (`isAuthenticated()`), imposé par la configuration globale `/api/**`.

**Portée** :
- `ROLE_ADMIN` : les KPI couvrent **tous** les projets, membres et tâches de l'application.
- Tous les autres rôles (y compris `ROLE_PROJET_MANAGER`) : les KPI sont **scopés aux projets dont l'utilisateur est propriétaire ou membre**. Un utilisateur sans projet reçoit un objet avec `projectProgress: []` et `taskDistribution: []`.

**Réponse 200 OK** (`DashboardKpiDTO`)

| Champ | Type | Description |
|-------|------|-------------|
| `totalProjects` | `long` | Nombre de projets dans le périmètre |
| `activeProjects` | `long` | Projets ayant au moins une tâche et pas encore 100 % `DONE` |
| `totalTasks` | `long` | Nombre total de tâches |
| `completedTasks` | `long` | Nombre de tâches `DONE` |
| `overdueTasks` | `long` | Nombre de tâches non `DONE` (statut différent de `DONE`) |
| `teamMembers` | `long` | Nombre d'utilisateurs distincts membres des projets du périmètre |
| `totalTimeSpentSeconds` | `long` | Temps total passé — **toujours `0`** (champ non renseigné par ce controller) |
| `timeSpentByUser` | `array` | **Toujours `null`** (non renseigné) |
| `timeSpentByProject` | `array` | **Toujours `null`** (non renseigné) |
| `projectProgress` | `ProjectProgressDTO[]` | Progression par projet, triée par % de tâches `DONE` croissant, limitée à **10** projets |
| `taskDistribution` | `TaskStatusCountDTO[]` | Répartition du nombre de tâches par statut |

**Sous-DTOs**

`ProjectProgressDTO` :

| Champ | Type | Description |
|-------|------|-------------|
| `projectId` | `Long` | ID du projet |
| `projectName` | `String` | Nom du projet |
| `totalTasks` | `long` | Tâches totales |
| `doneTasks` | `long` | Tâches `DONE` |

`TaskStatusCountDTO` :

| Champ | Type | Description |
|-------|------|-------------|
| `status` | `String` | Statut de tâche (`NEW`, `IN_PROGRESS`, `READY_FOR_TEST`, `DONE`, `NEEDS_INFO`) |
| `count` | `long` | Nombre de tâches |

**Exemple de réponse**

```json
{
  "totalProjects": 4,
  "activeProjects": 2,
  "totalTasks": 87,
  "completedTasks": 41,
  "overdueTasks": 46,
  "teamMembers": 12,
  "totalTimeSpentSeconds": 0,
  "timeSpentByUser": null,
  "timeSpentByProject": null,
  "projectProgress": [
    {
      "projectId": 3,
      "projectName": "Projet Alpha",
      "totalTasks": 20,
      "doneTasks": 5
    },
    {
      "projectId": 1,
      "projectName": "Mon Projet",
      "totalTasks": 30,
      "doneTasks": 20
    }
  ],
  "taskDistribution": [
    { "status": "NEW", "count": 22 },
    { "status": "IN_PROGRESS", "count": 15 },
    { "status": "READY_FOR_TEST", "count": 6 },
    { "status": "DONE", "count": 41 },
    { "status": "NEEDS_INFO", "count": 3 }
  ]
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 401 | Token manquant ou invalide |
| 403 | Utilisateur courant introuvable (login absent du contexte de sécurité) |

---

## 2. Statistiques du dashboard développeur

```http
GET /api/developer-dashboard/statistics
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN`, `ROLE_PROJET_MANAGER` ou `ROLE_DEVELOPER`.

**Portée** : toutes les valeurs concernent **uniquement l'utilisateur connecté** (ses tâches assignées et les projets dont il est membre), jamais des totaux système.

**Réponse 200 OK** (`DeveloperDashboardStatisticsDTO`)

| Champ | Type | Description |
|-------|------|-------------|
| `assignedTasksTotal` | `long` | Nombre total de tâches assignées à l'utilisateur |
| `inProgressTasks` | `long` | Tâches en cours (`IN_PROGRESS`) |
| `doneTasks` | `long` | Tâches terminées (`DONE`) |
| `overdueTasks` | `long` | Tâches en retard |
| `memberProjectsCount` | `long` | Nombre de projets dont l'utilisateur est membre |
| `taskDistribution` | `TaskStatusCountDTO[]` | Répartition des tâches par statut |

**Exemple de réponse**

```json
{
  "assignedTasksTotal": 15,
  "inProgressTasks": 3,
  "doneTasks": 8,
  "overdueTasks": 2,
  "memberProjectsCount": 3,
  "taskDistribution": [
    { "status": "NEW", "count": 2 },
    { "status": "IN_PROGRESS", "count": 3 },
    { "status": "READY_FOR_TEST", "count": 1 },
    { "status": "DONE", "count": 8 },
    { "status": "NEEDS_INFO", "count": 1 }
  ]
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 401 | Token manquant ou invalide |
| 403 | Rôle insuffisant (un simple `ROLE_USER` est refusé) |

---

## 3. Statistiques administrateur

```http
GET /api/admin/stats
Authorization: Bearer <token>
```

**Rôle requis** : `ROLE_ADMIN` (imposé au niveau de la classe).

**Réponse 200 OK** (`Map<String, Object>`)

| Champ | Type | Description |
|-------|------|-------------|
| `totalUsers` | `long` | Nombre total d'utilisateurs |
| `totalProjects` | `long` | Nombre total de projets |
| `totalTasks` | `long` | Nombre total de tâches |
| `usersByRole` | `Map<String, Long>` | Nombre d'utilisateurs par rôle (`ROLE_ADMIN`, `ROLE_PROJET_MANAGER`, `ROLE_DEVELOPER`, `ROLE_USER`) |

**Exemple de réponse**

```json
{
  "totalUsers": 24,
  "totalProjects": 4,
  "totalTasks": 87,
  "usersByRole": {
    "ROLE_ADMIN": 1,
    "ROLE_PROJET_MANAGER": 3,
    "ROLE_DEVELOPER": 12,
    "ROLE_USER": 8
  }
}
```

**Erreurs**

| Code | Signification |
|------|---------------|
| 401 | Token manquant ou invalide |
| 403 | Rôle insuffisant (pas `ROLE_ADMIN`) |
