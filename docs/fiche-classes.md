# Fiche Technique — Diagrammes UML du Projet

## 1. Diagramme de Classes (Class Diagram)

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
flowchart TD
    subgraph Acteurs
        A[Admin]
        PM[Project Manager]
        DEV[Developer]
        U[User]
    end

    subgraph Système[Système de Gestion de Tâches]
        UC1[Gérer les projets]
        UC2[Gérer les membres]
        UC3[Gérer les sprints]
        UC4[Gérer les epics]
        UC5[Gérer les issues]
        UC6[Tableau Kanban]
        UC7[Roadmap Epic]
        UC8[Burndown Chart]
        UC9[Commenter une issue]
        UC10[Joindre un fichier]
        UC11[Consulter le dashboard]
        UC12[Gérer les notifications]
        UC13[Administrer le système]
        UC14[S'authentifier]
        UC15[S'inscrire]
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

## 4. Diagramme d'Activité (Activity Diagram)

### Cycle de vie d'une Issue

```mermaid
flowchart TD
    A[Début: Création d'une issue] --> B{Type?}
    B -->|STORY| C[Statut: BACKLOG]
    B -->|BUG| D[Statut: TODO]
    B -->|TASK| C
    B -->|SUBTASK| C
    B -->|IMPROVEMENT| C

    C --> E[Ajouter au backlog]
    D --> E
    E --> F[Planifier dans un sprint]

    F --> G[Développement commence]
    G --> H[Statut: IN_PROGRESS]

    H --> I{Revue nécessaire?}
    I -->|Oui| J[Statut: IN_REVIEW]
    I -->|Non| K[Validation]

    J --> K
    K --> L{Approuvé?}
    L -->|Oui| M[Statut: DONE]
    L -->|Non| H
    L -->|Abandonné| N[Statut: CANCELLED]

    H -->|Abandonné| N
    C -->|Abandonné| N
    D -->|Abandonné| N
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

### Issue States

```mermaid
stateDiagram-v2
    [*] --> BACKLOG
    BACKLOG --> TODO: Priorisé
    TODO --> IN_PROGRESS: Démarré
    IN_PROGRESS --> IN_REVIEW: Revue demandée
    IN_PROGRESS --> TODO: Redéfini
    IN_REVIEW --> IN_PROGRESS: Modifications demandées
    IN_REVIEW --> DONE: Approuvé
    IN_PROGRESS --> DONE: Validé directement
    TODO --> CANCELLED: Abandonné
    IN_PROGRESS --> CANCELLED: Abandonné
    IN_REVIEW --> CANCELLED: Abandonné
    BACKLOG --> CANCELLED: Abandonné
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
            IssueDetail
            Admin
        end
        subgraph Services[Services]
            IssueService
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
            IssueResource
            CommentResource
            AttachmentResource
            NotificationResource
            AccountResource
        end
        subgraph ServicesLayer[Services Layer]
            ProjectService
            SprintService
            EpicService
            IssueService
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

### Création d'un commentaire sur une issue

```mermaid
flowchart TD
    1[Utilisateur] -->|2: Saisit le texte| F[Formulaire]
    1 -->|1: Ouvre l'issue| D[IssueDetail]
    F -->|3: submit()| CS[CommentService.create]
    CS -->|4: POST /api/comments| API[CommentResource]
    API -->|5: checkCommentOwnership| API
    API -->|6: save| SVC[CommentService]
    SVC -->|7: findUser| US[UserService]
    SVC -->|8: save| REP[CommentRepository]
    REP -->|9: INSERT| DB[(Database)]
    DB -->|10: Comment| REP
    REP -->|11: CommentDTO| SVC
    SVC -->|12: Response| API
    API -->|13: 201 Created| CS
    CS -->|14: Mise à jour liste| D
    D -->|15: Affiche nouveau commentaire| 1
```

**Liens :**
- `1` → `D` : navigation
- `F` → `CS` : appel de méthode
- `CS` → `API` : requête HTTP
- `API` → `SVC` : appel service
- `SVC` → `REP` : persistence JPA
- `REP` → `DB` : requête SQL

---

## 11. Diagramme de Timing (Timing Diagram)

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
        IP[IN PROGRESS<br/>50% des issues]
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

### Métriques temporelles d'une Issue

| Jour | Événement | Statut | Commentaire |
|------|-----------|--------|-------------|
| J0 | Création | BACKLOG | Issue créée dans le backlog |
| J3 | Priorisation | TODO | Ajoutée au sprint courant |
| J5 | Développement | IN_PROGRESS | Travail commencé |
| J9 | Code review | IN_REVIEW | Pull request soumise |
| J10 | Validation | DONE | Merge effectué |
| J20 | Fin de sprint | DONE | Sprint complété |

---

## 12. Diagramme de Structure Composite (Composite Structure Diagram)

### Structure interne d'une Issue

```mermaid
flowchart TD
    subgraph Issue[Issue #id]
        direction LR
        subgraph Props[Propriétés]
            title
            description
            type
            status
            priority
            createdAt
            updatedAt
        end
        subgraph Parts[Parties]
            CommentList[comments: Comment[]]
            AttachmentList[attachments: Attachment[]]
            HistoryList[history: ActionHistory[]]
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
| `IssueResource` | `REST: /api/issues` | HTTP |
| `IssueService` | `IssueDTO ↔ Issue` | MapStruct |
| `IssueRepository` | `JPA Repository` | Hibernate |
| `NotificationService` | `assign()` | Événement |

---

## 13. Diagramme de Profils (Profile Diagram)

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
    Entity <|-- Issue
    Entity <|-- Comment
    Entity <|-- Attachment
    Entity <|-- ActionHistory

    Service <|-- ProjectService
    Service <|-- SprintService
    Service <|-- IssueService

    RestController <|-- ProjectResource
    RestController <|-- SprintResource
    RestController <|-- IssueResource

    DTO <|-- ProjectDTO
    DTO <|-- IssueDTO

    Enum <|-- IssueStatus
    Enum <|-- SprintStatus
    Enum <|-- Priority
    Enum <|-- IssueType
    Enum <|-- EpicStatus
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

### Création d'une issue avec commentaire et pièce jointe

```mermaid
flowchart TD
    subgraph Création[Créer une Issue]
        A1[Créer l'issue]
        A2[Remplir titre, type, priorité]
        A3[Valider le formulaire]
        A4[Issue créée avec succès]
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
| 2 | Diagramme de cas d'utilisation | Comportement | Mermaid `flowchart` | Fonctionnalités du système |
| 3 | Diagramme de séquence | Comportement | Mermaid `sequenceDiagram` | Assignation, création projet, Kanban |
| 4 | Diagramme d'activité | Comportement | Mermaid `flowchart` | Cycle de vie issue, processus sprint |
| 5 | Diagramme d'états | Comportement | Mermaid `stateDiagram-v2` | Issue, Sprint, Epic |
| 6 | Diagramme de déploiement | Structure | Mermaid `flowchart` | Architecture physique |
| 7 | Diagramme de composants | Structure | Mermaid `flowchart` | Modules Angular + Spring |
| 8 | Diagramme de paquetages | Structure | Mermaid `flowchart` | Structure des packages |
| 9 | Diagramme d'objets | Structure | Mermaid `classDiagram` | Instances d'exemple |
| 10 | Diagramme de communication | Comportement | Mermaid `flowchart` | Création d'un commentaire |
| 11 | Diagramme de timing | Comportement | Mermaid `flowchart` | Cycle de sprint + métriques |
| 12 | Diagramme de structure composite | Structure | Mermaid `flowchart` | Structure interne d'une Issue |
| 13 | Diagramme de profils | Structure | Mermaid `classDiagram` | Stéréotypes JHipster |
| 14 | Diagramme de vue d'ensemble des interactions | Comportement | Mermaid `flowchart` | Flux complet création issue |

---

## Rôle de Chaque Table

### Project
Table racine du système. Représente un projet (ex. une application, un produit). Contient les sprints, epics et issues. Un `key` unique sert d'identifiant court (ex. `PROJ`). Chaque projet a un propriétaire (`owner_id` → `jhi_user`) et une équipe via la table `project_member`.

### ProjectMember
Table de jointure enrichie entre Project et User. Remplace l'ancienne table de jointure `project_members`. Chaque entrée possède un identifiant unique, un rôle (`MEMBER`, `LEAD`, etc.) et une date d'ajout. Permet une gestion d'équipe plus fine qu'un simple ManyToMany.

### Sprint
Itération de développement dans un projet. Regroupe un ensemble d'issues à réaliser sur une période donnée. Peut être PLANIFIÉ, ACTIF, TERMINÉ ou ANNULÉ.

### Epic
Regroupement logique d'issues correspondant à une fonctionnalité transverse de grande envergure. Permet de suivre un objectif métier à travers plusieurs sprints.

### Issue
Unité de travail atomique. Peut être un Story, Bug, Task, Subtask ou Improvement. Suit un cycle de vie complet (BACKLOG → DONE). Liée à un sprint et/ou un epic.

### Comment
Commentaire texte attaché à une issue. Possède un auteur (`author_id` → `jhi_user`). Permet la discussion et le suivi collaboratif.

### Attachment
Fichier joint à une issue (capture d'écran, document, etc.). Stocke le chemin du fichier et son nom original.

### ActionHistory
Trace d'audit détaillant chaque modification d'une issue. Enregistre l'action, le champ modifié, l'ancienne et la nouvelle valeur.

---

## Dépendances entre Tables

| Table | Dépend de | Est utilisé par |
|-------|-----------|-----------------|
| User | — | Project (owner), ProjectMember, Issue (assignee), Comment (author) |
| Project | User (owner) | Sprint, Epic, Issue, ProjectMember |
| ProjectMember | Project, User | — |
| Sprint | Project | Issue |
| Epic | Project | Issue |
| Issue | Project, Sprint, Epic | Comment, Attachment, ActionHistory |
| Comment | Issue, User (author) | — |
| Attachment | Issue | — |
| ActionHistory | Issue | — |
