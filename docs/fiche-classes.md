# Fiche Technique — Diagrammes UML du Projet

## 1. Diagramme de Classes (Class Diagram)

Ce diagramme modélise la structure statique du système : les entités métier (User, Project, Issue, etc.), leurs attributs, leurs types (enumérations) et les associations qui les relient (propriétaire, membres, sprint, epic, commentaires, etc.).

```mermaid
classDiagram
    class User {
        +Long id
        +String login
        +String firstName
        +String lastName
        +String email
    }

    class Project {
        +String name
        +String description
        +String project_key
        +Instant createdAt
    }

    class ProjectMember {
        +String role
        +Instant joinedAt
    }

    class Sprint {
        +String name
        +String goal
        +LocalDate startDate
        +LocalDate endDate
        +SprintStatus status
    }

    class Epic {
        +String title
        +String description
        +EpicStatus status
        +Priority priority
        +Instant createdAt
        +Instant updatedAt
        +LocalDate startDate
        +LocalDate endDate
    }

    class Issue {
        +String title
        +String description
        +IssueType type
        +IssueStatus status
        +Priority priority
        +Instant createdAt
        +Instant updatedAt
    }

    class Comment {
        +String content
        +Instant createdAt
        +User author
    }

    class Attachment {
        +String fileName
        +String filePath
        +Instant uploadedAt
    }

    class ActionHistory {
        +String action
        +String fieldChanged
        +String oldValue
        +String newValue
        +Instant createdAt
    }

    class Notification {
        +String message
        +Long issueId
        +String issueTitle
        +Long userId
        +Boolean isRead
        +Instant createdAt
    }

    class SprintStatus {
        <<enumeration>>
        PLANNED
        ACTIVE
        COMPLETED
        CANCELLED
    }

    class EpicStatus {
        <<enumeration>>
        TODO
        IN_PROGRESS
        DONE
        CANCELLED
    }

    class IssueType {
        <<enumeration>>
        STORY
        BUG
        TASK
        SUBTASK
        IMPROVEMENT
    }

    class IssueStatus {
        <<enumeration>>
        BACKLOG
        TODO
        IN_PROGRESS
        IN_REVIEW
        DONE
        CANCELLED
    }

    class Priority {
        <<enumeration>>
        LOWEST
        LOW
        MEDIUM
        HIGH
        HIGHEST
    }

    User "1" --> "*" Project : possède (owner)
    Project "1" --> "*" ProjectMember : contient (members)
    ProjectMember "*" --> "1" User : référence
    Project "1" --> "*" Sprint : contient
    Project "1" --> "*" Epic : contient
    Project "1" --> "*" Issue : contient
    Sprint "1" --> "*" Issue : regroupe
    Epic "1" --> "*" Issue : catégorise
    User "1" --> "*" Comment : écrit (author)
    User "1" --> "*" Issue : assigné (assignee)
    Issue "1" --> "*" Comment : reçoit
    Issue "1" --> "*" Attachment : contient
    Issue "1" --> "*" ActionHistory : trace
    Sprint --> SprintStatus
    Epic --> EpicStatus
    Issue --> IssueType
    Issue --> IssueStatus
    Epic --> Priority
    Issue --> Priority
```

---

## 2. Diagramme de Cas d'Utilisation (Use Case Diagram)

```mermaid
graph LR

User[Utilisateur]
Admin[Admin]
PM[Chef de Projet]
Dev[Développeur]

User --> Admin
User --> PM
User --> Dev

subgraph System["Système de Gestion de Projet"]

UC1["S'authentifier"]
UC2["Consulter le tableau de bord"]
UC3["Consulter les notifications"]

UC4["Gérer les projets"]
UC5["Gérer les membres"]
UC6["Gérer les sprints"]
UC7["Gérer les epics"]
UC8["Gérer les issues"]

UC9["Consulter le backlog"]
UC10["Utiliser le tableau Kanban"]
UC11["Consulter la Roadmap"]
UC12["Consulter le Burndown Chart"]

UC13["Commenter une issue"]
UC14["Ajouter une pièce jointe"]

UC15["Administrer le système"]

end

User --> UC1
User --> UC2
User --> UC3

Admin --> UC4
Admin --> UC5
Admin --> UC6
Admin --> UC7
Admin --> UC8
Admin --> UC15

PM --> UC4
PM --> UC5
PM --> UC6
PM --> UC7
PM --> UC8
PM --> UC9
PM --> UC10
PM --> UC11
PM --> UC12
PM --> UC13
PM --> UC14

Dev --> UC8
Dev --> UC9
Dev --> UC10
Dev --> UC11
Dev --> UC12
Dev --> UC13
Dev --> UC14
```
### Description des Cas d'Utilisation

| Code | Nom | Acteurs | Description |
|------|-----|---------|-------------|
| UC1 | Gérer les projets | Admin, PM | Créer, modifier, supprimer un projet |
| UC2 | Gérer les membres | Admin, PM | Ajouter/retirer un membre, changer son rôle |
| UC3 | Gérer les sprints | Admin, PM | Créer, démarrer, compléter un sprint |
| UC4 | Gérer les epics | Admin, PM | Créer, modifier, supprimer un epic |
| UC5 | Gérer les issues | Admin, PM, DEV | CRUD + assignation + changement de statut |
| UC6 | Tableau Kanban | PM, DEV | Visualiser et glisser-déposer les issues |
| UC7 | Roadmap Epic | PM, DEV | Vue d'ensemble des epics avec progression |
| UC8 | Burndown Chart | PM, DEV | Graphique d'avancement du sprint |
| UC9 | Commenter une issue | PM, DEV, U | Ajouter/modifier/supprimer un commentaire |
| UC10 | Joindre un fichier | PM, DEV | Uploader un fichier sur une issue |
| UC11 | Dashboard | U | Voir les KPI, graphiques et activités récentes |
| UC12 | Notifications | U | Recevoir et consulter les notifications |
| UC13 | Administration | Admin | Gérer les utilisateurs, rôles, configuration |
| UC14 | Authentification | Tous | Se connecter / se déconnecter (JWT) |
| UC15 | Inscription | U | Créer un compte |

---

## 3. Diagramme de Séquence (Sequence Diagram)

Ce diagramme montre les interactions temporelles entre les acteurs et les composants du système pour des scénarios clés : assignation d'une issue, création d'un projet et changement de statut via le tableau Kanban.

### 3.1 Assignation d'une issue

```mermaid
sequenceDiagram
    actor DEV as Développeur
    participant Front as Frontend Angular
    participant API as IssueResource
    participant Service as IssueService
    participant Notif as NotificationService
    participant DB as Base de Données
    actor Assignee as Utilisateur assigné

    DEV->>Front: Glisser-déposer / Clique "Assigner"
    Front->>API: PATCH /api/issues/{id}/assign { userId }
    API->>API: Vérifier rôle (DEV ou PM)
    API->>Service: assign(issueId, user)
    Service->>DB: findById(issueId)
    Service->>DB: save(issue avec assignee)
    Service-->>API: IssueDTO
    API->>Notif: save(notification)
    Notif->>DB: INSERT notification
    API-->>Front: 200 OK + IssueDTO
    Front-->>DEV: Mise à jour de l'interface
    Note over Notif,Assignee: L'utilisateur assigné reçoit la notification
```

### 3.2 Création d'un projet

```mermaid
sequenceDiagram
    actor PM as Project Manager
    participant Front as Frontend Angular
    participant API as ProjectResource
    participant Service as ProjectService
    participant DB as Base de Données

    PM->>Front: Remplir formulaire projet
    Front->>Front: Validation des champs
    Front->>API: POST /api/projects
    API->>API: Vérifier rôle (ADMIN ou PM)
    API->>Service: save(projectDTO)
    Service->>Service: Récupérer utilisateur courant
    Service->>Service: Définir comme owner
    Service->>DB: INSERT project
    DB-->>Service: Project
    Service-->>API: ProjectDTO
    API-->>Front: 201 Created
    Front-->>PM: Projet créé, redirection
```

### 3.3 Changement de statut d'une issue (Drag & Drop Kanban)

```mermaid
sequenceDiagram
    actor DEV as Développeur
    participant Front as Kanban Board
    participant API as IssueResource
    participant Service as IssueService
    participant DB as Base de Données

    DEV->>Front: Glisser une issue vers "IN_PROGRESS"
    Front->>Front: dragIssueId = issue.id
    Front->>Front: onDrop(targetStatus = "IN_PROGRESS")
    Front->>API: PATCH /api/issues/{id} { status: "IN_PROGRESS" }
    API->>API: Vérifier rôle
    API->>Service: partialUpdate(issueDTO)
    Service->>DB: findById(id)
    Service->>DB: save(issue modifié)
    DB-->>Service: Issue mis à jour
    Service-->>API: IssueDTO
    API-->>Front: 200 OK
    Front->>Front: issue.status = "IN_PROGRESS"
    Front-->>DEV: Carte déplacée visuellement
```

---


## 8. Diagramme de Paquetages (Package Diagram)

Ce diagramme illustre la structure des paquetages du projet : l'organisation du backend (domain, repository, service, web.rest, config, security, aop) et du frontend (entities, core, shared, layouts, home, admin), avec leurs dépendances.

```mermaid
flowchart TD
    subgraph Backend[com.gestiontaches]
        subgraph domain[domain]
            enumeration[enumeration/]
            Project
            Sprint
            Epic
            Issue
            Comment
            Attachment
            ActionHistory
            Notification
            ProjectMember
            User
        end
        subgraph repository[repository]
            ProjectRepository
            SprintRepository
            EpicRepository
            IssueRepository
            CommentRepository
            AttachmentRepository
            ActionHistoryRepository
            NotificationRepository
            ProjectMemberRepository
            UserRepository
        end
        subgraph service[service]
            direction TB
            dto[dto/]
            mapper[mapper/]
            criteria[criteria/]
        end
        subgraph web[web.rest]
            resources[REST Resources]
            errors[errors/]
        end
        subgraph config[config]
            SecurityConfiguration
            CacheConfiguration
            LiquibaseConfiguration
            JacksonConfiguration
        end
        subgraph security[security]
            jwt[JWT]
            AuthoritiesConstants
            SecurityUtils
        end
        subgraph aop[aop.logging]
            LoggingAspect
        end
    end

    subgraph Frontend[src/main/webapp/app]
        subgraph FE_entities[entities/]
            issue[issue/]
            sprint[sprint/]
            epic[epic/]
            project[project/]
            comment[comment/]
            attachment[attachment/]
            action_history[action-history/]
        end
        subgraph FE_core[core/]
            auth[auth/]
            interceptor[interceptor/]
            util[util/]
        end
        subgraph FE_shared[shared/]
            alert[alert/]
            date[date/]
            filter[filter/]
            sort[sort/]
            language[language/]
        end
        subgraph FE_layouts[layouts/]
            main[main/]
            navbar[navbar/]
            sidebar[sidebar/]
        end
        subgraph FE_home[home/]
            dashboard[dashboard/]
        end
        subgraph FE_admin[admin/]
            user_management[user-management/]
            health[health/]
            metrics[metrics/]
            logs[logs/]
        end
    end
```

### Dépendances entre Paquetages

| Paquetage | Dépend de |
|-----------|-----------|
| `web.rest` | `service`, `security`, `repository` |
| `service` | `repository`, `domain`, `service.dto`, `service.mapper` |
| `service.dto` | `domain` |
| `service.mapper` | `domain`, `service.dto` |
| `repository` | `domain` |
| `config` | `security`, `domain` |
| `entities/` (frontend) | `core/util`, `shared/` |
| `home/dashboard` | `core/config`, `entities/` |

---

## 9. Diagramme d'Objets (Object Diagram)

Ce diagramme présente un snapshot concret d'instances du système à un moment donné : un projet "Site Web" avec ses sprints, epics, issues, commentaires et utilisateurs, illustrant les relations entre objets réels.

### Exemple d'instances en cours d'exécution

```mermaid
classDiagram
    class projet1 {
        id = 1
        name = "Site Web"
        key = "SITE"
        createdAt = 2026-06-01
    }

    class sprint1 {
        id = 1
        name = "Sprint 1"
        status = ACTIVE
        startDate = 2026-06-15
        endDate = 2026-06-28
    }

    class sprint2 {
        id = 2
        name = "Sprint 2"
        status = PLANNED
        startDate = 2026-06-29
        endDate = 2026-07-12
    }

    class epic1 {
        id = 1
        title = "Authentification"
        status = IN_PROGRESS
        priority = HIGH
    }

    class issue1 {
        id = 101
        title = "Page de connexion"
        type = STORY
        status = DONE
        priority = HIGH
    }

    class issue2 {
        id = 102
        title = "Bouton "Mot de passe oublié""
        type = TASK
        status = IN_PROGRESS
        priority = MEDIUM
    }

    class issue3 {
        id = 103
        title = "Erreur 500 sur login"
        type = BUG
        status = TODO
        priority = HIGHEST
    }

    class comment1 {
        content = "J'ai corrigé le style du bouton"
    }

    class user1 {
        login = "alice"
        role = "PROJET_MANAGER"
    }

    class user2 {
        login = "bob"
        role = "DEVELOPER"
    }

    projet1 --> sprint1
    projet1 --> sprint2
    projet1 --> epic1
    projet1 --> issue1
    projet1 --> issue2
    projet1 --> issue3
    sprint1 --> issue1
    sprint1 --> issue2
    epic1 --> issue1
    epic1 --> issue2
    user1 --> projet1 : owner
    issue1 --> user2 : assignee
    issue1 --> comment1
    user2 --> comment1 : author
```

---

## 10. Diagramme de Communication (Communication Diagram)

Ce diagramme montre les interactions entre les objets et acteurs lors de la création d'un commentaire sur une issue, en mettant l'accent sur l'ordre des messages échangés (de l'ouverture de l'issue jusqu'à l'affichage du commentaire).

### Création d'un commentaire sur une issue

```mermaid
sequenceDiagram
    actor User as Utilisateur
    participant D as IssueDetail
    participant F as Formulaire
    participant CS as CommentService
    participant API as CommentResource
    participant SVC as CommentService
    participant US as UserService
    participant REP as CommentRepository
    participant DB as Database

    User->>D: 1: Ouvre l'issue
    User->>F: 2: Saisit le texte
    F->>CS: 3: submit()
    CS->>API: 4: POST /api/comments
    API->>API: 5: Vérifier propriété
    API->>SVC: 6: save()
    SVC->>US: 7: findUser()
    SVC->>REP: 8: save()
    REP->>DB: 9: INSERT
    DB-->>REP: 10: Comment
    REP-->>SVC: 11: CommentDTO
    SVC-->>API: 12: Response
    API-->>CS: 13: 201 Created
    CS-->>D: 14: Mettre à jour liste
    D-->>User: 15: Afficher commentaire
```

---
