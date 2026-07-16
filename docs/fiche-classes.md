# Fiche Technique — Diagrammes UML du Projet

## 1. Diagramme de Classes (Class Diagram)

Ce diagramme modélise la structure statique du système : les entités métier (User, Project, Task, etc.), leurs attributs, leurs types (enumérations) et les associations qui les relient (propriétaire, membres, sprint, epic, commentaires, etc.).

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
        +ProjectRole role
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

    class Task {
        +String title
        +String description
        +TaskStatus status
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

    class TaskHistory {
        +String action
        +String fieldChanged
        +String oldValue
        +String newValue
        +Instant createdAt
    }

    class Notification {
        +String message
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

    class TaskStatus {
        <<enumeration>>
        NEW
        TODO
        IN_PROGRESS
        IN_REVIEW
        DONE
        CANCELLED
    }

    class ProjectRole {
        <<enumeration>>
        OWNER
        MANAGER
        MEMBER
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
    ProjectMember --> ProjectRole
    Project "1" --> "*" Sprint : contient
    Project "1" --> "*" Epic : contient
    Project "1" --> "*" Task : contient
    Sprint "1" --> "*" Task : regroupe
    Epic "1" --> "*" Task : catégorise
    User "1" --> "*" Comment : écrit (author)
    User "1" --> "*" Task : assigné (assignee)
    Task "1" --> "*" Comment : reçoit
    Task "1" --> "*" Attachment : contient
    Task "1" --> "*" TaskHistory : trace
    Task "1" --> "*" Notification : déclenche
    User "1" --> "*" Notification : reçoit
    Sprint --> SprintStatus
    Epic --> EpicStatus
    Task --> TaskStatus
    Epic --> Priority
    Task --> Priority
```

---

## 2. Diagramme de Cas d'Utilisation (Use Case Diagram)

```mermaid
graph LR
    A["fa:fa-user Admin"]
    PM["fa:fa-user Chef de Projet"]
    DEV["fa:fa-user Développeur"]
    U["fa:fa-user Utilisateur"]

    subgraph Systeme["Système de Gestion de Tâches"]
        UC1("Gérer les projets")
        UC2("Gérer les membres")
        UC3("Gérer les sprints")
        UC4("Gérer les epics")
        UC5("Gérer les tâches")
        UC6("Tableau Kanban")
        UC7("Roadmap Epic")
        UC8("Burndown Chart")
        UC9("Commenter une tâche")
        UC10("Joindre un fichier")
        UC11("Consulter le dashboard")
        UC12("Gérer les notifications")
        UC13("Administrer le système")
        UC14("S'authentifier")
        UC15("S'inscrire")
    end

    A --> UC1
    A --> UC2
    A --> UC3
    A --> UC4
    A --> UC5
    A --> UC13

    PM --> UC1
    PM --> UC2
    PM --> UC3
    PM --> UC4
    PM --> UC5
    PM --> UC6
    PM --> UC7
    PM --> UC8

    DEV --> UC5
    DEV --> UC6
    DEV --> UC7
    DEV --> UC8
    DEV --> UC9
    DEV --> UC10

    U --> UC9
    U --> UC11
    U --> UC12
    U --> UC14
    U --> UC15
```

### Description des Cas d'Utilisation

| Code | Nom | Acteurs | Description |
|------|-----|---------|-------------|
| UC1 | Gérer les projets | Admin, PM | Créer, modifier, supprimer un projet |
| UC2 | Gérer les membres | Admin, PM | Ajouter/retirer un membre, changer son rôle |
| UC3 | Gérer les sprints | Admin, PM | Créer, démarrer, compléter un sprint |
| UC4 | Gérer les epics | Admin, PM | Créer, modifier, supprimer un epic |
| UC5 | Gérer les tâches | Admin, PM, DEV | CRUD + assignation + changement de statut |
| UC6 | Tableau Kanban | PM, DEV | Visualiser et glisser-déposer les tâches |
| UC7 | Roadmap Epic | PM, DEV | Vue d'ensemble des epics avec progression |
| UC8 | Burndown Chart | PM, DEV | Graphique d'avancement du sprint |
| UC9 | Commenter une tâche | PM, DEV, U | Ajouter/modifier/supprimer un commentaire |
| UC10 | Joindre un fichier | PM, DEV | Uploader un fichier sur une tâche |
| UC11 | Dashboard | U | Voir les KPI, graphiques et activités récentes |
| UC12 | Notifications | U | Recevoir et consulter les notifications |
| UC13 | Administration | Admin | Gérer les utilisateurs, rôles, configuration |
| UC14 | Authentification | Tous | Se connecter / se déconnecter (JWT) |
| UC15 | Inscription | U | Créer un compte |

---

## 3. Diagramme de Séquence (Sequence Diagram)

Ce diagramme montre les interactions temporelles entre les acteurs et les composants du système pour des scénarios clés : assignation d'une tâche, création d'un projet et changement de statut via le tableau Kanban.

### 3.1 Assignation d'une tâche

```mermaid
sequenceDiagram
    actor DEV as Développeur
    participant Front as Frontend Angular
    participant API as TaskResource
    participant Service as TaskService
    participant Notif as NotificationService
    participant DB as Base de Données
    actor Assignee as Utilisateur assigné

    DEV->>Front: Glisser-déposer / Clique "Assigner"
    Front->>API: PATCH /api/tasks/{id}/assign { userId }
    API->>API: Vérifier rôle (DEV ou PM)
    API->>Service: assign(taskId, user)
    Service->>DB: findById(taskId)
    Service->>DB: save(task avec assignee)
    Service-->>API: TaskDTO
    API->>Notif: save(notification)
    Notif->>DB: INSERT notification
    API-->>Front: 200 OK + TaskDTO
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

### 3.3 Changement de statut d'une tâche (Drag & Drop Kanban)

```mermaid
sequenceDiagram
    actor DEV as Développeur
    participant Front as Kanban Board
    participant API as TaskResource
    participant Service as TaskService
    participant DB as Base de Données

    DEV->>Front: Glisser une tâche vers "IN_PROGRESS"
    Front->>Front: dragTaskId = task.id
    Front->>Front: onDrop(targetStatus = "IN_PROGRESS")
    Front->>API: PATCH /api/tasks/{id} { status: "IN_PROGRESS" }
    API->>API: Vérifier rôle
    API->>Service: partialUpdate(taskDTO)
    Service->>DB: findById(id)
    Service->>DB: save(task modifiée)
    DB-->>Service: Task mise à jour
    Service-->>API: TaskDTO
    API-->>Front: 200 OK
    Front->>Front: task.status = "IN_PROGRESS"
    Front-->>DEV: Carte déplacée visuellement
```

---

## 4. Diagramme d'Activité (Activity Diagram)

Ce diagramme modélise les flux de travail du système : le cycle de vie complet d'une tâche (de la création à la validation ou l'annulation) et le processus de gestion d'un sprint (démarrage, complétion, annulation).

### Cycle de vie d'une Tâche

```mermaid
flowchart TD
    A[Début: Création d'une tâche] --> B[Statut: NEW]

    B --> C[Planifier dans un sprint]

    C --> D[Développement commence]
    D --> E[Statut: IN_PROGRESS]

    E --> F{Revue nécessaire?}
    F -->|Oui| G[Statut: IN_REVIEW]
    F -->|Non| H[Validation]

    G --> H
    H --> I{Approuvé?}
    I -->|Oui| J[Statut: DONE]
    I -->|Non| E
    I -->|Abandonné| K[Statut: CANCELLED]

    E -->|Abandonné| K
    B -->|Abandonné| K
    B --> E: Passer à TODO puis IN_PROGRESS
```

### Processus de Sprint

```mermaid
flowchart LR
    P[PLANNED] -->|Démarrer le sprint| A[ACTIVE]
    A -->|Compléter| C[COMPLETED]
    A -->|Annuler| X[CANCELLED]
    P -->|Annuler| X
    C -->|Réouvrir| A
```

---

## 5. Diagramme d'États (State Machine Diagram)

Ce diagramme décrit les différents états possibles des entités et les transitions autorisées : cycle de vie d'une tâche (NEW → TODO → IN_PROGRESS → IN_REVIEW → DONE), d'un sprint (PLANNED → ACTIVE → COMPLETED) et d'un epic (TODO → IN_PROGRESS → DONE).

### Task States

```mermaid
stateDiagram-v2
    [*] --> NEW
    NEW --> TODO: Priorisé
    TODO --> IN_PROGRESS: Démarré
    IN_PROGRESS --> IN_REVIEW: Revue demandée
    IN_PROGRESS --> TODO: Redéfini
    IN_REVIEW --> IN_PROGRESS: Modifications demandées
    IN_REVIEW --> DONE: Approuvé
    IN_PROGRESS --> DONE: Validé directement
    TODO --> CANCELLED: Abandonné
    IN_PROGRESS --> CANCELLED: Abandonné
    IN_REVIEW --> CANCELLED: Abandonné
    NEW --> CANCELLED: Abandonné
    DONE --> [*]
    CANCELLED --> [*]
```

### Sprint States

```mermaid
stateDiagram-v2
    [*] --> PLANNED
    PLANNED --> ACTIVE: Démarrer le sprint
    PLANNED --> CANCELLED: Annuler
    ACTIVE --> COMPLETED: Terminer
    ACTIVE --> CANCELLED: Annuler
    COMPLETED --> ACTIVE: Réouvrir
    COMPLETED --> [*]
    CANCELLED --> [*]
```

### Epic States

```mermaid
stateDiagram-v2
    [*] --> TODO
    TODO --> IN_PROGRESS: Travail commencé
    IN_PROGRESS --> DONE: Terminé
    TODO --> CANCELLED: Abandonné
    IN_PROGRESS --> CANCELLED: Abandonné
    DONE --> [*]
    CANCELLED --> [*]
```

---

## 6. Diagramme de Déploiement (Deployment Diagram)

Ce diagramme représente l'architecture physique du système : le client Angular (SPA + PWA), le serveur Spring Boot (API, services, sécurité, cache), la base de données PostgreSQL, le système de fichiers pour les uploads, et les outils de monitoring (Actuator, SonarQube).

```mermaid
flowchart TD
    subgraph Navigateur[Client - Navigateur]
        A[Application Angular SPA]
        PWA[Service Worker PWA]
    end

    subgraph Serveur[Serveur d'application]
        LB[Load Balancer]
        subgraph Spring[Spring Boot Server :8080]
            API[REST API Controllers]
            SVC[Services Métier]
            REP[Repositories JPA]
            SEC[Sécurité JWT]
            CACHE[Cache Caffeine]
        end
    end

    subgraph Stockage[Stockage de données]
        DB[(PostgreSQL :5432<br/>gestionTaches)]
        FS[Système de fichiers<br/>uploads/]
    end

    subgraph Monitoring
        ACTUATOR[Spring Actuator]
        SONAR[SonarQube :9001]
    end

    Navigateur -- HTTPS --> LB
    LB -- HTTP --> Spring
    API --> SVC
    SVC --> REP
    SVC --> CACHE
    SVC --> SEC
    REP --> DB
    API --> FS
    Spring --> ACTUATOR
    ACTUATOR --> SONAR
```

---

## 7. Diagramme de Composants (Component Diagram)

Ce diagramme montre l'organisation des modules logiciels du système : le frontend Angular (pages, services, modules partagés) et le backend Spring Boot (controllers REST, services métier, accès aux données, sécurité), ainsi que leurs interactions.

```mermaid
flowchart TD
    subgraph Frontend[Frontend Angular]
        App[Application Root]
        Router[Router]
        subgraph Pages[Pages & Composants]
            Dashboard
            Kanban
            SprintBoard
            EpicRoadmap
            TaskDetail
            Admin
        end
        subgraph Services[Services]
            TaskService
            SprintService
            ProjectService
            EpicService
            NotificationService
            AuthService
        end
        subgraph Shared[Modules Partagés]
            AlertComponent
            TranslateModule
            Pagination
            FilterComponent
        end
    end

    subgraph Backend[Backend Spring Boot]
        subgraph Controllers[REST Controllers]
            ProjectResource
            SprintResource
            EpicResource
            TaskResource
            CommentResource
            AttachmentResource
            NotificationResource
            AccountResource
        end
        subgraph ServicesLayer[Services Layer]
            ProjectService
            SprintService
            EpicService
            TaskService
            CommentService
            AttachmentService
            NotificationService
            MailService
        end
        subgraph DataAccess[Data Access]
            Repositories
            Mappers MapStruct
            DTOs
        end
        subgraph Security[Security]
            JWT
            BCrypt
            @PreAuthorize
        end
    end

    subgraph Database[Base de Données]
        Tables[Tables JPA]
        Liquibase[Migrations Liquibase]
    end

    App --> Router
    Router --> Pages
    Pages --> Services
    Pages --> Shared
    Services --> Controllers
    Controllers --> ServicesLayer
    ServicesLayer --> DataAccess
    DataAccess --> Tables
    Security --> Controllers
    Tables --> Liquibase
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
            Task
            Comment
            Attachment
            TaskHistory
            Notification
            ProjectMember
            User
        end
        subgraph repository[repository]
            ProjectRepository
            SprintRepository
            EpicRepository
            TaskRepository
            CommentRepository
            AttachmentRepository
            TaskHistoryRepository
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
            task[task/]
            sprint[sprint/]
            epic[epic/]
            project[project/]
            comment[comment/]
            attachment[attachment/]
            task_history[task-history/]
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

Ce diagramme présente un snapshot concret d'instances du système à un moment donné : un projet "Site Web" avec ses sprints, epics, tâches, commentaires et utilisateurs, illustrant les relations entre objets réels.

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

    class task1 {
        id = 101
        title = "Page de connexion"
        status = DONE
        priority = HIGH
    }

    class task2 {
        id = 102
        title = "Bouton "Mot de passe oublié""
        status = IN_PROGRESS
        priority = MEDIUM
    }

    class task3 {
        id = 103
        title = "Erreur 500 sur login"
        status = TODO
        priority = HIGHEST
    }

    class comment1 {
        content = "J'ai corrigé le style du bouton"
    }

    class user1 {
        login = "alice"
    }

    class user2 {
        login = "bob"
    }

    projet1 --> sprint1
    projet1 --> sprint2
    projet1 --> epic1
    projet1 --> task1
    projet1 --> task2
    projet1 --> task3
    sprint1 --> task1
    sprint1 --> task2
    epic1 --> task1
    epic1 --> task2
    user1 --> projet1 : owner
    task1 --> user2 : assignee
    task1 --> comment1
    user2 --> comment1 : author
```

---

## 10. Diagramme de Communication (Communication Diagram)

Ce diagramme montre les interactions entre les objets et acteurs lors de la création d'un commentaire sur une tâche, en mettant l'accent sur l'ordre des messages échangés (de l'ouverture de la tâche jusqu'à l'affichage du commentaire).

### Création d'un commentaire sur une tâche

```mermaid
sequenceDiagram
    actor User as Utilisateur
    participant D as TaskDetail
    participant F as Formulaire
    participant CS as CommentService
    participant API as CommentResource
    participant SVC as CommentService
    participant US as UserService
    participant REP as CommentRepository
    participant DB as Database

    User->>D: 1: Ouvre la tâche
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

## 11. Diagramme de Timing (Timing Diagram)

Ce diagramme illustre l'évolution temporelle d'un sprint sur 20 jours, du démarrage (PLANNED → ACTIVE) jusqu'à la complétion, en passant par les jalons clés (développement, revue, validation). Un tableau associé détaille les métriques temporelles d'une tâche.

### Cycle de vie d'un Sprint (20 jours)

```mermaid
flowchart LR
    subgraph J0[J0 - Début]
        P[PLANNED<br/>Backlog constitué]
    end
    subgraph J1[J1]
        A[ACTIVE<br/>Sprint démarré]
    end
    subgraph J5[J5]
        IP[IN PROGRESS<br/>50% des tâches]
    end
    subgraph J10[J10 - Mi-parcours]
        REV[IN REVIEW<br/>Premières revues]
    end
    subgraph J18[J18]
        D[DONE<br/>95% complété]
    end
    subgraph J20[J20 - Fin]
        COMP[COMPLETED<br/>Sprint terminé]
    end

    P -->|startSprint| A
    A --> IP
    IP --> REV
    REV --> D
    D -->|completeSprint| COMP
```

### Métriques temporelles d'une Tâche

| Jour | Événement | Statut | Commentaire |
|------|-----------|--------|-------------|
| J0 | Création | NEW | Tâche créée |
| J3 | Priorisation | TODO | Ajoutée au sprint courant |
| J5 | Développement | IN_PROGRESS | Travail commencé |
| J9 | Code review | IN_REVIEW | Pull request soumise |
| J10 | Validation | DONE | Merge effectué |
| J20 | Fin de sprint | DONE | Sprint complété |

---

## 12. Diagramme de Structure Composite (Composite Structure Diagram)

Ce diagramme décompose la structure interne d'une Task : ses propriétés (titre, statut, priorité), ses parties (commentaires, pièces jointes, historique) et ses références externes (projet, sprint, epic, assignee). Un tableau décrit les ports et interfaces associés.

### Structure interne d'une Task

```mermaid
flowchart TD
    subgraph Task[Task #id]
        direction LR
        subgraph Props[Propriétés]
            title
            description
            status
            priority
            createdAt
            updatedAt
        end
        subgraph Parts[Parties]
            CommentList[comments: Comment[]]
            AttachmentList[attachments: Attachment[]]
            HistoryList[history: TaskHistory[]]
        end
        subgraph Refs[Références Externes]
            Proj[project: Project]
            Spr[sprint: Sprint]
            Ep[epic: Epic]
            Assign[assignee: User]
        end
    end

    Props --- Parts
    Parts --- Refs
```

### Ports et Interfaces

| Port | Interface | Connecteur |
|------|-----------|------------|
| `TaskResource` | `REST: /api/tasks` | HTTP |
| `TaskService` | `TaskDTO ↔ Task` | MapStruct |
| `TaskRepository` | `JPA Repository` | Hibernate |
| `NotificationService` | `assign()` | Événement |

---

## 13. Diagramme de Profils (Profile Diagram)

Ce diagramme définit les stéréotypes UML personnalisés utilisés dans le projet (Entity, Service, RestController, DTO, Enum) avec leurs tags associés, et montre leur application aux classes concrètes du système.

### Stéréotypes et Tags appliqués au projet

```mermaid
classDiagram
    class <<stereotype>> Entity {
        +tableName: String
        +changelogDate: String
        +service: String = "serviceClass"
        +dto: String = "mapstruct"
        +pagination: String
    }

    class <<stereotype>> Service {
        +logging: boolean = true
        +transactional: boolean = true
    }

    class <<stereotype>> RestController {
        +basePath: String
        +entityName: String
    }

    class <<stereotype>> DTO {
        +mapstruct: boolean = true
    }

    class <<stereotype>> Enum

    Entity <|-- Project
    Entity <|-- Sprint
    Entity <|-- Epic
    Entity <|-- Task
    Entity <|-- Comment
    Entity <|-- Attachment
    Entity <|-- TaskHistory

    Service <|-- ProjectService
    Service <|-- SprintService
    Service <|-- TaskService

    RestController <|-- ProjectResource
    RestController <|-- SprintResource
    RestController <|-- TaskResource

    DTO <|-- ProjectDTO
    DTO <|-- TaskDTO

    Enum <|-- TaskStatus
    Enum <|-- SprintStatus
    Enum <|-- Priority
    Enum <|-- EpicStatus
    Enum <|-- ProjectRole
```

### Contraintes UML

| Stéréotype | Cible | Tags |
|------------|-------|------|
| `Entity` | Classes du package `domain/` | `tableName`, `changelogDate`, `service`, `dto`, `pagination` |
| `Service` | Classes du package `service/` | `logging`, `transactional` |
| `RestController` | Classes du package `web/rest/` | `basePath`, `entityName` |
| `DTO` | Classes du package `service/dto/` | `mapstruct` |
| `Enum` | Classes du package `domain/enumeration/` | — |

---

## 14. Diagramme de Vue d'Ensemble des Interactions (Interaction Overview Diagram)

Ce diagramme offre une vue d'ensemble du flux de création d'une tâche, combinant sous-diagrammes (création, commentaire, upload de fichier) et une référence vers un diagramme de séquence externe (assignation), permettant de visualiser un processus complet.

### Création d'une tâche avec commentaire et pièce jointe

```mermaid
flowchart TD
    subgraph Création[Créer une Tâche]
        A1[Créer la tâche]
        A2[Remplir titre, priorité]
        A3[Valider le formulaire]
        A4[Tâche créée avec succès]
    end

    subgraph AjoutComment[Ajouter un commentaire]
        B1[Saisir le texte]
        B2[Valider le commentaire]
    end

    subgraph AjoutPiece[Jointe une pièce]
        C1[Sélectionner un fichier]
        C2[Valider l'upload]
    end

    subgraph RefSequence[Séquence de référence: Assignation]
        D[Voir diagramme de séquence 3.1]
    end

    Début[Début] --> Création
    Création --> A1 --> A2 --> A3 --> A4

    A4 --> Choix{Que faire ensuite?}

    Choix -->|Commenter| AjoutComment
    AjoutComment --> B1 --> B2 --> Fin

    Choix -->|Joindre fichier| AjoutPiece
    AjoutPiece --> C1 --> C2 --> Fin

    Choix -->|Assigner| RefSequence
    RefSequence --> Fin

    Choix -->|Terminer| Fin[Fin]
```

Ce diagramme combine plusieurs diagrammes de séquence et d'activité pour montrer un flux de travail complet.

---

## Tableau récapitulatif des 14 diagrammes UML

| # | Diagramme UML | Type | Outil utilisé | Fichier/Package couvert |
|---|---|---|---|---|
| 1 | Diagramme de classes | Structure | Mermaid `classDiagram` | `domain/`, `domain/enumeration/` |
| 2 | Diagramme de cas d'utilisation | Comportement | Mermaid `graph` + `fa:fa-user` | Fonctionnalités du système |
| 3 | Diagramme de séquence | Comportement | Mermaid `sequenceDiagram` | Assignation, création projet, Kanban |
| 4 | Diagramme d'activité | Comportement | Mermaid `flowchart` | Cycle de vie tâche, processus sprint |
| 5 | Diagramme d'états | Comportement | Mermaid `stateDiagram-v2` | Task, Sprint, Epic |
| 6 | Diagramme de déploiement | Structure | Mermaid `flowchart` | Architecture physique |
| 7 | Diagramme de composants | Structure | Mermaid `flowchart` | Modules Angular + Spring |
| 8 | Diagramme de paquetages | Structure | Mermaid `flowchart` | Structure des packages |
| 9 | Diagramme d'objets | Structure | Mermaid `classDiagram` | Instances d'exemple |
| 10 | Diagramme de communication | Comportement | Mermaid `sequenceDiagram` | Création d'un commentaire |
| 11 | Diagramme de timing | Comportement | Mermaid `flowchart` | Cycle de sprint + métriques |
| 12 | Diagramme de structure composite | Structure | Mermaid `flowchart` | Structure interne d'une Task |
| 13 | Diagramme de profils | Structure | Mermaid `classDiagram` | Stéréotypes JHipster |
| 14 | Diagramme de vue d'ensemble des interactions | Comportement | Mermaid `flowchart` | Flux complet création tâche |

---

## Rôle de Chaque Table et Structure de la Base de Données

### `jhi_user`

Table gérée par JHipster. Contient les comptes utilisateurs avec authentification.

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `login` | `varchar(50)` | UNIQUE, NOT NULL |
| `password_hash` | `varchar(60)` | NOT NULL (BCrypt) |
| `first_name` | `varchar(50)` | |
| `last_name` | `varchar(50)` | |
| `email` | `varchar(191)` | UNIQUE |
| `image_url` | `varchar(256)` | |
| `activated` | `boolean` | NOT NULL, default `false` |
| `lang_key` | `varchar(10)` | |
| `activation_key` | `varchar(20)` | |
| `reset_key` | `varchar(20)` | |
| `reset_date` | `timestamp` | |
| `created_by` | `varchar(50)` | NOT NULL |
| `created_date` | `timestamp` | |
| `last_modified_by` | `varchar(50)` | |
| `last_modified_date` | `timestamp` | |

### `jhi_authority`

Table des rôles/autorités. Le nom du rôle sert de clé primaire.

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `name` | `varchar(50)` | PRIMARY KEY |

### `jhi_user_authority`

Table de jointure entre utilisateurs et rôles (ManyToMany).

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `user_id` | `bigint` | FK → `jhi_user(id)` |
| `authority_name` | `varchar(50)` | FK → `jhi_authority(name)` |
| | | PRIMARY KEY composite (`user_id`, `authority_name`) |

### `project`

Table racine du système. Représente un projet. Contient les sprints, epics et tâches. Un `project_key` unique sert d'identifiant court (ex. `PROJ`). Chaque projet a un propriétaire (`owner_id` → `jhi_user`) et une équipe via la table `project_member`.

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `name` | `varchar(100)` | NOT NULL |
| `description` | `varchar(500)` | |
| `project_key` | `varchar(10)` | NOT NULL, UNIQUE |
| `created_at` | `datetime` | NOT NULL |
| `owner_id` | `bigint` | FK → `jhi_user(id)` |

### `project_member`

Table de jointure enrichie entre Project et User. Remplace l'ancienne table de jointure `project_members`. Chaque entrée possède un identifiant, un rôle (`ProjectRole` : `OWNER`, `MANAGER`, `MEMBER`) et une date d'ajout. Contrainte d'unicité sur `(project_id, user_id)`.

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `project_id` | `bigint` | FK → `project(id)`, NOT NULL |
| `user_id` | `bigint` | FK → `jhi_user(id)`, NOT NULL |
| `role` | `varchar(50)` | NOT NULL (`OWNER`/`MANAGER`/`MEMBER`) |
| `joined_at` | `datetime(6)` | NOT NULL |
| | | UNIQUE(`project_id`, `user_id`) |

### `sprint`

Itération de développement dans un projet. Regroupe un ensemble de tâches à réaliser sur une période donnée. Peut être PLANNED, ACTIVE, COMPLETED ou CANCELLED.

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `name` | `varchar(100)` | NOT NULL |
| `goal` | `varchar(500)` | |
| `start_date` | `date` | |
| `end_date` | `date` | |
| `status` | `varchar(255)` | NOT NULL (`PLANNED`/`ACTIVE`/`COMPLETED`/`CANCELLED`) |
| `project_id` | `bigint` | FK → `project(id)`, NOT NULL |

### `epic`

Regroupement logique de tâches correspondant à une fonctionnalité transverse de grande envergure. Permet de suivre un objectif métier à travers plusieurs sprints.

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `title` | `varchar(200)` | NOT NULL |
| `description` | `varchar(1000)` | |
| `status` | `varchar(255)` | NOT NULL (`TODO`/`IN_PROGRESS`/`DONE`/`CANCELLED`) |
| `priority` | `varchar(255)` | NOT NULL (`LOWEST`/`LOW`/`MEDIUM`/`HIGH`/`HIGHEST`) |
| `created_at` | `datetime` | NOT NULL |
| `updated_at` | `datetime` | |
| `start_date` | `date` | |
| `end_date` | `date` | |
| `project_id` | `bigint` | FK → `project(id)`, NOT NULL |

### `task`

Unité de travail atomique. Suit un cycle de vie complet (NEW → TODO → IN_PROGRESS → IN_REVIEW → DONE). Liée à un projet (obligatoire), un sprint (optionnel) et/ou un epic (optionnel). Possède un assignee et un créateur.

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `title` | `varchar(200)` | NOT NULL |
| `description` | `varchar(5000)` | |
| `status` | `varchar(255)` | NOT NULL (`NEW`/`TODO`/`IN_PROGRESS`/`IN_REVIEW`/`DONE`/`CANCELLED`) |
| `priority` | `varchar(255)` | NOT NULL (`LOWEST`/`LOW`/`MEDIUM`/`HIGH`/`HIGHEST`) |
| `created_at` | `datetime` | NOT NULL |
| `updated_at` | `datetime` | |
| `sprint_id` | `bigint` | FK → `sprint(id)` |
| `epic_id` | `bigint` | FK → `epic(id)` |
| `project_id` | `bigint` | FK → `project(id)`, NOT NULL |
| `assignee_id` | `bigint` | FK → `jhi_user(id)` |
| `created_by_id` | `bigint` | FK → `jhi_user(id)` |

### `comment`

Commentaire texte attaché à une tâche. Possède un auteur (`author_id` → `jhi_user`). Permet la discussion et le suivi collaboratif.

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `content` | `varchar(2000)` | NOT NULL |
| `created_at` | `datetime` | NOT NULL |
| `task_id` | `bigint` | FK → `task(id)`, NOT NULL |
| `author_id` | `bigint` | FK → `jhi_user(id)` |

### `attachment`

Fichier joint à une tâche (capture d'écran, document, etc.). Stocke le chemin du fichier et son nom original.

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `file_name` | `varchar(255)` | NOT NULL |
| `file_path` | `varchar(1000)` | NOT NULL |
| `uploaded_at` | `datetime` | NOT NULL |
| `task_id` | `bigint` | FK → `task(id)`, NOT NULL |

### `task_history`

Trace d'audit détaillant chaque modification d'une tâche. Enregistre l'action effectuée, l'ancienne et la nouvelle valeur, ainsi que l'utilisateur ayant effectué la modification.

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `action` | `varchar(100)` | NOT NULL |
| `old_value` | `varchar(500)` | |
| `new_value` | `varchar(500)` | |
| `created_at` | `datetime` | NOT NULL |
| `task_id` | `bigint` | FK → `task(id)`, NOT NULL |
| `user_id` | `bigint` | FK → `jhi_user(id)`, NOT NULL |

### `notification`

Notification in-app pour informer un utilisateur (ex: assignation à une tâche). Contient un message, une référence vers la tâche et un statut de lecture.

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY, auto-increment |
| `message` | `varchar(500)` | NOT NULL |
| `task_id` | `bigint` | FK → `task(id)` |
| `task_title` | `varchar(200)` | |
| `user_id` | `bigint` | FK → `jhi_user(id)`, NOT NULL |
| `is_read` | `boolean` | NOT NULL, default `false` |
| `created_at` | `datetime(6)` | NOT NULL |

---

## Énumérations

| Enum | Valeurs | Utilisée par |
|------|---------|-------------|
| `SprintStatus` | `PLANNED`, `ACTIVE`, `COMPLETED`, `CANCELLED` | Sprint |
| `EpicStatus` | `TODO`, `IN_PROGRESS`, `DONE`, `CANCELLED` | Epic |
| `TaskStatus` | `NEW`, `TODO`, `IN_PROGRESS`, `IN_REVIEW`, `DONE`, `CANCELLED` | Task |
| `Priority` | `LOWEST`, `LOW`, `MEDIUM`, `HIGH`, `HIGHEST` | Task, Epic |
| `ProjectRole` | `OWNER`, `MANAGER`, `MEMBER` | ProjectMember |

---

## Dépendances entre Tables

| Table | Dépend de | Est utilisé par |
|-------|-----------|-----------------|
| jhi_user | — | Project (owner), ProjectMember, Task (assignee, createdBy), Comment (author), TaskHistory (user), Notification (user) |
| jhi_authority | — | jhi_user_authority |
| jhi_user_authority | jhi_user, jhi_authority | — |
| project | jhi_user (owner) | Sprint, Epic, Task, ProjectMember |
| project_member | project, jhi_user | — |
| sprint | project | Task |
| epic | project | Task |
| task | project, sprint, epic, jhi_user (assignee, createdBy) | Comment, Attachment, TaskHistory, Notification |
| comment | task, jhi_user (author) | — |
| attachment | task | — |
| task_history | task, jhi_user (user) | — |
| notification | task, jhi_user (user) | — |
