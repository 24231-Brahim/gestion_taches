# Fiche Technique — Diagrammes UML du Projet

## 1. Diagramme de Classes (Class Diagram)

Ce diagramme modélise la structure statique du système : les entités métier (User, Project, Task, etc.), leurs attributs, leurs types (enumérations) et les associations qui les relient (propriétaire, membres, sprint, epic, commentaires, chat, etc.).

```mermaid
classDiagram
    class User {
        +Long id
        +String login
        +String passwordHash
        +String firstName
        +String lastName
        +String email
        +String imageUrl
        +Boolean activated
        +String langKey
        +String activationKey
        +String resetKey
        +Instant resetDate
        +Instant createdDate
    }

    class Authority {
        +String name
    }

    class UserAuthority {
        <<join table>>
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
        +Integer storyPoints
        +Instant createdAt
        +Instant updatedAt
    }

    class Comment {
        +String content
        +Instant createdAt
    }

    class Attachment {
        +String fileName
        +String filePath
        +Instant uploadedAt
    }

    class TaskHistory {
        +String action
        +String oldValue
        +String newValue
        +Instant createdAt
    }

    class Notification {
        +String message
        +String taskTitle
        +Boolean isRead
        +Long relatedUserId
        +String relatedUserLogin
        +Instant createdAt
    }

    class GroupMessage {
        +String content
        +Instant createdAt
    }

    class Conversation {
        +ConversationType type
        +String name
        +Instant createdAt
    }

    class ConversationMember {
        +Instant joinedAt
        +Instant lastReadAt
    }

    class ChatMessage {
        +String content
        +Instant createdAt
        +Instant editedAt
        +Boolean deleted
        +Set~Long~ mentions
    }

    class UserPresence {
        +Instant lastActiveAt
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
        IN_PROGRESS
        READY_FOR_TEST
        DONE
        NEEDS_INFO
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

    class ConversationType {
        <<enumeration>>
        GENERAL
        DIRECT
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
    Task "1" --> "*" Comment : reçoit
    Task "1" --> "*" Attachment : contient
    Task "1" --> "*" TaskHistory : trace
    Task "1" --> "*" Notification : déclenche
    User "1" --> "*" Task : assigné (assignee)
    User "1" --> "*" Task : crée (createdBy)
    User "1" --> "*" Comment : écrit (author)
    User "1" --> "*" TaskHistory : effectue
    User "1" --> "*" Notification : reçoit
    User "1" --> "*" Attachment : téléverse (uploadedBy)
    User "1" --> "*" UserAuthority : possède
    Authority "1" --> "*" UserAuthority : associé à
    UserAuthority --> User
    UserAuthority --> Authority
    Sprint --> SprintStatus
    Epic --> EpicStatus
    Task --> TaskStatus
    Epic --> Priority
    Task --> Priority
    Project "1" --> "*" GroupMessage : contient
    GroupMessage "*" --> "1" User : émet (sender)
    GroupMessage "*" --> "1" User : destinataire (recipient)
    Project "1" --> "*" Conversation : contient
    Conversation "1" --> "*" ConversationMember : contient
    ConversationMember "*" --> "1" User : référence
    Conversation "1" --> "*" ChatMessage : contient
    ChatMessage "*" --> "1" User : émet (sender)
    ChatMessage "*" --> "1" ChatMessage : répond (parent)
    User "1" --> "*" UserPresence : suivi (presence)
    Conversation --> ConversationType
```

---
## 2. Diagramme Entité-Relation

```mermaid
erDiagram
    USER {
        Long id PK
        String login
        String passwordHash
        String firstName
        String lastName
        String email
        String imageUrl
        Boolean activated
        String langKey
        String activationKey
        String resetKey
        Instant resetDate
        Instant createdDate
    }

    AUTHORITY {
        String name PK
    }

    USER_AUTHORITY {
        Long user_id FK
        String authority_name FK
    }

    PROJECT {
        Long id PK
        String name
        String description
        String project_key
        Instant createdAt
        Long owner_id FK
    }

    PROJECT_MEMBER {
        Long id PK
        String role
        Instant joinedAt
        Long project_id FK
        Long user_id FK
    }

    SPRINT {
        Long id PK
        String name
        String goal
        LocalDate startDate
        LocalDate endDate
        String status
        Long project_id FK
    }

    EPIC {
        Long id PK
        String title
        String description
        String status
        String priority
        Instant createdAt
        Instant updatedAt
        LocalDate startDate
        LocalDate endDate
        Long project_id FK
    }

    TASK {
        Long id PK
        String title
        String description
        String status
        String priority
        Integer storyPoints
        Instant createdAt
        Instant updatedAt
        Long project_id FK
        Long sprint_id FK
        Long epic_id FK
        Long assignee_id FK
        Long createdBy_id FK
    }

    COMMENT {
        Long id PK
        String content
        Instant createdAt
        Long task_id FK
        Long author_id FK
    }

    ATTACHMENT {
        Long id PK
        String fileName
        String filePath
        Instant uploadedAt
        Long task_id FK
        Long uploaded_by FK
    }

    TASK_HISTORY {
        Long id PK
        String action
        String oldValue
        String newValue
        Instant createdAt
        Long task_id FK
        Long user_id FK
    }

    NOTIFICATION {
        Long id PK
        String message
        String taskTitle
        Boolean isRead
        Instant createdAt
        Long task_id FK
        Long user_id FK
        Long related_user_id FK
        String related_user_login
    }

    GROUP_MESSAGE {
        Long id PK
        String content
        Instant createdAt
        Long sender_id FK
        Long recipient_id FK
        Long project_id FK
    }

    CHAT_CONVERSATION {
        Long id PK
        String type
        String name
        Long project_id FK
        Long created_by FK
        Instant createdAt
    }

    CHAT_CONVERSATION_MEMBER {
        Long id PK
        Long conversation_id FK
        Long user_id FK
        Instant joinedAt
        Instant lastReadAt
    }

    CHAT_MESSAGE {
        Long id PK
        String content
        Long conversation_id FK
        Long sender_id FK
        Long parent_message_id FK
        Instant createdAt
        Instant editedAt
        Boolean deleted
    }

    CHAT_MESSAGE_MENTIONS {
        Long message_id FK
        Long user_id FK
    }

    CHAT_USER_PRESENCE {
        Long id PK
        Long user_id FK
        Instant lastActiveAt
    }

    USER ||--o{ PROJECT : "possède (owner)"
    USER ||--o{ PROJECT_MEMBER : "référence"
    PROJECT ||--o{ PROJECT_MEMBER : "contient"
    PROJECT ||--o{ SPRINT : "contient"
    PROJECT ||--o{ EPIC : "contient"
    PROJECT ||--o{ TASK : "contient"
    SPRINT ||--o{ TASK : "regroupe"
    EPIC ||--o{ TASK : "catégorise"
    TASK ||--o{ COMMENT : "reçoit"
    TASK ||--o{ ATTACHMENT : "contient"
    TASK ||--o{ TASK_HISTORY : "trace"
    TASK ||--o{ NOTIFICATION : "déclenche"
    USER ||--o{ TASK : "assigné (assignee)"
    USER ||--o{ TASK : "crée (createdBy)"
    USER ||--o{ COMMENT : "écrit (author)"
    USER ||--o{ ATTACHMENT : "téléverse (uploadedBy)"
    USER ||--o{ TASK_HISTORY : "effectue"
    USER ||--o{ NOTIFICATION : "reçoit"
    USER ||--o{ USER_AUTHORITY : "possède"
    AUTHORITY ||--o{ USER_AUTHORITY : "associé à"
    PROJECT ||--o{ GROUP_MESSAGE : "contient"
    USER ||--o{ GROUP_MESSAGE : "émet (sender)"
    USER ||--o{ GROUP_MESSAGE : "destinataire (recipient)"
    PROJECT ||--o{ CHAT_CONVERSATION : "contient"
    CHAT_CONVERSATION ||--o{ CHAT_CONVERSATION_MEMBER : "contient"
    CHAT_CONVERSATION ||--o{ CHAT_MESSAGE : "contient"
    USER ||--o{ CHAT_CONVERSATION_MEMBER : "membre"
    USER ||--o{ CHAT_MESSAGE : "émet (sender)"
    CHAT_MESSAGE ||--o{ CHAT_MESSAGE : "répond (parent)"
    CHAT_MESSAGE ||--o{ CHAT_MESSAGE_MENTIONS : "mentionne"
    USER ||--o{ CHAT_USER_PRESENCE : "suivi (presence)"
```
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

Unité de travail atomique. Suit un cycle de vie complet (NEW → IN_PROGRESS → READY_FOR_TEST → DONE, avec NEEDS_INFO en cas de blocage). Liée à un projet (obligatoire), un sprint (optionnel) et/ou un epic (optionnel). Possède un assignee et un créateur.

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `title` | `varchar(200)` | NOT NULL |
| `description` | `varchar(5000)` | |
| `status` | `varchar(255)` | NOT NULL (`NEW`/`IN_PROGRESS`/`READY_FOR_TEST`/`DONE`/`NEEDS_INFO`) |
| `priority` | `varchar(255)` | NOT NULL (`LOWEST`/`LOW`/`MEDIUM`/`HIGH`/`HIGHEST`) |
| `story_points` | `integer` | |
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

Fichier joint à une tâche (capture d'écran, document, etc.). Stocke le chemin du fichier et son nom original. L'utilisateur qui a téléversé le fichier est enregistré (`uploaded_by`).

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `file_name` | `varchar(255)` | NOT NULL |
| `file_path` | `varchar(1000)` | NOT NULL |
| `uploaded_at` | `datetime` | NOT NULL |
| `task_id` | `bigint` | FK → `task(id)`, NOT NULL |
| `uploaded_by` | `bigint` | FK → `jhi_user(id)` |

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
| `user_id` | `bigint` | FK → `jhi_user(id)` |

### `notification`

Notification in-app pour informer un utilisateur (ex: assignation à une tâche). Contient un message, une référence vers la tâche et un statut de lecture. Les champs `related_user_id` / `related_user_login` permettent la navigation vers un utilisateur lié (ex. auteur d'une action).

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY, auto-increment |
| `message` | `varchar(500)` | NOT NULL |
| `task_id` | `bigint` | FK → `task(id)` |
| `task_title` | `varchar(200)` | |
| `user_id` | `bigint` | FK → `jhi_user(id)`, NOT NULL |
| `related_user_id` | `bigint` | FK → `jhi_user(id)` |
| `related_user_login` | `varchar(50)` | |
| `is_read` | `boolean` | NOT NULL, default `false` |
| `created_at` | `datetime(6)` | NOT NULL |

### `group_message`

Message de messagerie de groupe lié à un projet (prototype initial du chat). Contient l'expéditeur (`sender_id`) et un destinataire optionnel (`recipient_id`).

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `content` | `varchar(5000)` | NOT NULL |
| `created_at` | `datetime(6)` | |
| `sender_id` | `bigint` | FK → `jhi_user(id)`, NOT NULL |
| `recipient_id` | `bigint` | FK → `jhi_user(id)` |
| `project_id` | `bigint` | FK → `project(id)`, NOT NULL |

### `chat_conversation`

Conversation de chat rattachée à un projet. Un projet possède un canal général (`GENERAL`, type `# General`) et des conversations directes (`DIRECT`) entre deux membres.

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `type` | `varchar(20)` | NOT NULL (`GENERAL`/`DIRECT`) |
| `name` | `varchar(100)` | |
| `project_id` | `bigint` | FK → `project(id)`, NOT NULL |
| `created_by` | `bigint` | FK → `jhi_user(id)` |
| `created_at` | `datetime` | NOT NULL |

### `chat_conversation_member`

Association entre une conversation et un utilisateur. Porte l'horodatage de dernière lecture (`last_read_at`) utilisé pour le calcul des messages non lus. Contrainte d'unicité sur `(conversation_id, user_id)`.

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `conversation_id` | `bigint` | FK → `chat_conversation(id)`, NOT NULL |
| `user_id` | `bigint` | FK → `jhi_user(id)`, NOT NULL |
| `joined_at` | `datetime` | NOT NULL |
| `last_read_at` | `datetime` | |
| | | UNIQUE(`conversation_id`, `user_id`) |

### `chat_message`

Message à l'intérieur d'une conversation. Supporte les fils de discussion (`parent_message_id`), les mentions (`chat_message_mentions`) et la suppression logique (`deleted`).

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `content` | `varchar(5000)` | NOT NULL |
| `conversation_id` | `bigint` | FK → `chat_conversation(id)`, NOT NULL |
| `sender_id` | `bigint` | FK → `jhi_user(id)`, NOT NULL |
| `parent_message_id` | `bigint` | FK → `chat_message(id)` |
| `created_at` | `datetime` | NOT NULL |
| `edited_at` | `datetime` | |
| `deleted` | `boolean` | NOT NULL, default `false` |

### `chat_message_mentions`

Table de jointure (collection) entre un message et les identifiants d'utilisateurs mentionnés (`@login`).

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `message_id` | `bigint` | FK → `chat_message(id)`, NOT NULL |
| `user_id` | `bigint` | FK → `jhi_user(id)` |

### `chat_user_presence`

Marqueur de présence d'un utilisateur pour le chat. Une ligne par utilisateur avec la date de sa dernière activité. Un utilisateur est considéré en ligne si `last_active_at` est récent (fenêtre configurée dans `ChatService`).

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `user_id` | `bigint` | FK → `jhi_user(id)`, NOT NULL, UNIQUE |
| `last_active_at` | `datetime` | NOT NULL |

---

## Énumérations

| Enum | Valeurs | Utilisée par |
|------|---------|-------------|
| `SprintStatus` | `PLANNED`, `ACTIVE`, `COMPLETED`, `CANCELLED` | Sprint |
| `EpicStatus` | `TODO`, `IN_PROGRESS`, `DONE`, `CANCELLED` | Epic |
| `TaskStatus` | `NEW`, `IN_PROGRESS`, `READY_FOR_TEST`, `DONE`, `NEEDS_INFO` | Task |
| `Priority` | `LOWEST`, `LOW`, `MEDIUM`, `HIGH`, `HIGHEST` | Task, Epic |
| `ProjectRole` | `OWNER`, `MANAGER`, `MEMBER` | ProjectMember |
| `ConversationType` | `GENERAL`, `DIRECT` | Conversation |

---

## Dépendances entre Tables

| Table | Dépend de | Est utilisé par |
|-------|-----------|-----------------|
| jhi_user | — | Project (owner), ProjectMember, Task (assignee, createdBy), Comment (author), Attachment (uploadedBy), TaskHistory (user), Notification (user, relatedUser), GroupMessage (sender, recipient), Conversation (createdBy), ConversationMember (user), ChatMessage (sender), UserPresence (user) |
| jhi_authority | — | jhi_user_authority |
| jhi_user_authority | jhi_user, jhi_authority | — |
| project | jhi_user (owner) | Sprint, Epic, Task, ProjectMember, GroupMessage, Conversation |
| project_member | project, jhi_user | — |
| sprint | project | Task |
| epic | project | Task |
| task | project, sprint, epic, jhi_user (assignee, createdBy) | Comment, Attachment, TaskHistory, Notification |
| comment | task, jhi_user (author) | — |
| attachment | task, jhi_user (uploadedBy) | — |
| task_history | task, jhi_user (user) | — |
| notification | task, jhi_user (user, relatedUser) | — |
| group_message | project, jhi_user (sender, recipient) | — |
| chat_conversation | project, jhi_user (createdBy) | ConversationMember, ChatMessage |
| chat_conversation_member | chat_conversation, jhi_user | — |
| chat_message | chat_conversation, jhi_user (sender), chat_message (parent) | chat_message_mentions |
| chat_message_mentions | chat_message, jhi_user | — |
| chat_user_presence | jhi_user | — |
