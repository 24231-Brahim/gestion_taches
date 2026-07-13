# Documentation Base de Données - Gestion de Tâches

## Technologies

- **Base de données**: PostgreSQL (prod) / H2 (dev)
- **ORM**: Hibernate/JPA
- **Migration**: Liquibase
- **Serveur Spring Boot**: 4.0.6 / Java 21

---

## Structures des Tables

### 1. `jhi_user`

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `login` | `varchar(50)` | UNIQUE, NOT NULL |
| `password_hash` | `varchar(60)` | NOT NULL |
| `first_name` | `varchar(50)` | |
| `last_name` | `varchar(50)` | |
| `email` | `varchar(191)` | UNIQUE |
| `image_url` | `varchar(256)` | |
| `activated` | `boolean` | NOT NULL, default `false` |
| `lang_key` | `varchar(10)` | |
| `activation_key` | `varchar(20)` | |
| `reset_key` | `varchar(20)` | |
| `created_by` | `varchar(50)` | NOT NULL |
| `created_date` | `timestamp` | |
| `reset_date` | `timestamp` | |
| `last_modified_by` | `varchar(50)` | |
| `last_modified_date` | `timestamp` | |

### 2. `jhi_authority`

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `name` | `varchar(50)` | PRIMARY KEY |

### 3. `jhi_user_authority` (table de jointure)

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `user_id` | `bigint` | FK → `jhi_user(id)` |
| `authority_name` | `varchar(50)` | FK → `jhi_authority(name)` |
| | | PRIMARY KEY composite (`user_id`, `authority_name`) |

### 4. `project`

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `name` | `varchar(100)` | NOT NULL |
| `description` | `varchar(500)` | |
| `project_key` | `varchar(10)` | NOT NULL, UNIQUE |
| `created_at` | `datetime` | NOT NULL |
| `owner_id` | `bigint` | FK → `jhi_user(id)` |

### 5. `project_member`

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `project_id` | `bigint` | FK → `project(id)`, NOT NULL |
| `user_id` | `bigint` | FK → `jhi_user(id)`, NOT NULL |
| `role` | `varchar(50)` | NOT NULL (`OWNER`/`MANAGER`/`MEMBER`) |
| `joined_at` | `datetime(6)` | NOT NULL |
| | | UNIQUE(`project_id`, `user_id`) |

### 6. `sprint`

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `name` | `varchar(100)` | NOT NULL |
| `goal` | `varchar(500)` | |
| `start_date` | `date` | |
| `end_date` | `date` | |
| `status` | `varchar(255)` | NOT NULL (`PLANNED`/`ACTIVE`/`COMPLETED`/`CANCELLED`) |
| `project_id` | `bigint` | FK → `project(id)`, NOT NULL |

### 7. `epic`

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

### 8. `issue`

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `title` | `varchar(200)` | NOT NULL |
| `description` | `varchar(5000)` | |
| `type` | `varchar(255)` | NOT NULL (`STORY`/`BUG`/`TASK`/`SUBTASK`/`IMPROVEMENT`) |
| `status` | `varchar(255)` | NOT NULL (`BACKLOG`/`TODO`/`IN_PROGRESS`/`IN_REVIEW`/`DONE`/`CANCELLED`) |
| `priority` | `varchar(255)` | NOT NULL (`LOWEST`/`LOW`/`MEDIUM`/`HIGH`/`HIGHEST`) |
| `created_at` | `datetime` | NOT NULL |
| `updated_at` | `datetime` | |
| `sprint_id` | `bigint` | FK → `sprint(id)` |
| `epic_id` | `bigint` | FK → `epic(id)` |
| `project_id` | `bigint` | FK → `project(id)`, NOT NULL |
| `assignee_id` | `bigint` | FK → `jhi_user(id)` |
| `created_by_id` | `bigint` | FK → `jhi_user(id)` |

### 9. `comment`

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `content` | `varchar(2000)` | NOT NULL |
| `created_at` | `datetime` | NOT NULL |
| `issue_id` | `bigint` | FK → `issue(id)`, NOT NULL |
| `author_id` | `bigint` | FK → `jhi_user(id)` |

### 10. `attachment`

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `file_name` | `varchar(255)` | NOT NULL |
| `file_path` | `varchar(1000)` | NOT NULL |
| `uploaded_at` | `datetime` | NOT NULL |
| `issue_id` | `bigint` | FK → `issue(id)`, NOT NULL |

### 11. `action_history`

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY |
| `action` | `varchar(100)` | NOT NULL |
| `field_changed` | `varchar(100)` | |
| `old_value` | `varchar(500)` | |
| `new_value` | `varchar(500)` | |
| `created_at` | `datetime` | NOT NULL |
| `issue_id` | `bigint` | FK → `issue(id)`, NOT NULL |
| `user_id` | `bigint` | FK → `jhi_user(id)` |

### 12. `notification`

| Colonne | Type | Contraintes |
|---------|------|-------------|
| `id` | `bigint` | PRIMARY KEY, auto-increment |
| `message` | `varchar(500)` | NOT NULL |
| `issue_id` | `bigint` | |
| `issue_title` | `varchar(200)` | |
| `user_id` | `bigint` | NOT NULL |
| `is_read` | `boolean` | NOT NULL, default `false` |
| `created_at` | `datetime(6)` | NOT NULL |

---

## Énumérations

| Enum | Valeurs |
|------|---------|
| `SprintStatus` | `PLANNED`, `ACTIVE`, `COMPLETED`, `CANCELLED` |
| `EpicStatus` | `TODO`, `IN_PROGRESS`, `DONE`, `CANCELLED` |
| `IssueStatus` | `BACKLOG`, `TODO`, `IN_PROGRESS`, `IN_REVIEW`, `DONE`, `CANCELLED` |
| `IssueType` | `STORY`, `BUG`, `TASK`, `SUBTASK`, `IMPROVEMENT` |
| `Priority` | `LOWEST`, `LOW`, `MEDIUM`, `HIGH`, `HIGHEST` |
| `ProjectRole` | `OWNER`, `MANAGER`, `MEMBER` |

---

## Diagramme de Classes (Mermaid)

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
        +String createdBy
        +LocalDateTime createdDate
        +LocalDateTime resetDate
        +String lastModifiedBy
        +LocalDateTime lastModifiedDate
    }

    class Authority {
        +String name
    }

    class Project {
        +Long id
        +String name
        +String description
        +String projectKey
        +LocalDateTime createdAt
    }

    class ProjectMember {
        +Long id
        +ProjectRole role
        +LocalDateTime joinedAt
    }

    class Sprint {
        +Long id
        +String name
        +String goal
        +LocalDate startDate
        +LocalDate endDate
        +SprintStatus status
    }

    class Epic {
        +Long id
        +String title
        +String description
        +EpicStatus status
        +Priority priority
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
        +LocalDate startDate
        +LocalDate endDate
    }

    class Issue {
        +Long id
        +String title
        +String description
        +IssueType type
        +IssueStatus status
        +Priority priority
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }

    class Comment {
        +Long id
        +String content
        +LocalDateTime createdAt
    }

    class Attachment {
        +Long id
        +String fileName
        +String filePath
        +LocalDateTime uploadedAt
    }

    class ActionHistory {
        +Long id
        +String action
        +String fieldChanged
        +String oldValue
        +String newValue
        +LocalDateTime createdAt
    }

    class Notification {
        +Long id
        +String message
        +Long issueId
        +String issueTitle
        +Boolean isRead
        +LocalDateTime createdAt
    }

    User "1" --> "*" Project : owner
    User "1" --> "*" ProjectMember : member
    User "1" --> "*" Issue : assignee
    User "1" --> "*" Issue : created_by
    User "1" --> "*" Comment : author
    User "1" --> "*" ActionHistory : user
    User "*" --> "*" Authority : authorities

    Project "1" --> "*" Sprint : contains
    Project "1" --> "*" Epic : contains
    Project "1" --> "*" Issue : contains
    Project "1" --> "*" ProjectMember : has

    Sprint "1" --> "*" Issue : groups
    Epic "1" --> "*" Issue : categorizes

    Issue "1" --> "*" Comment : receives
    Issue "1" --> "*" Attachment : has
    Issue "1" --> "*" ActionHistory : traced_by
```

---

## Diagramme Entité-Relation (Mermaid)

```mermaid
erDiagram
    jhi_user ||--o{ project : "owns"
    jhi_user ||--o{ project_member : "member"
    jhi_user ||--o{ issue : "assignee"
    jhi_user ||--o{ issue : "created_by"
    jhi_user ||--o{ comment : "author"
    jhi_user ||--o{ action_history : "performs"
    jhi_user }|--|{ jhi_authority : "authorities"

    project ||--o{ sprint : "contains"
    project ||--o{ epic : "contains"
    project ||--o{ issue : "contains"
    project ||--o{ project_member : "has"

    sprint ||--o{ issue : "groups"
    epic ||--o{ issue : "categorizes"

    issue ||--o{ comment : "receives"
    issue ||--o{ attachment : "has"
    issue ||--o{ action_history : "traced_by"

    jhi_user {
        bigint id PK
        varchar login UK
        varchar password_hash
        varchar first_name
        varchar last_name
        varchar email UK
        boolean activated
    }

    jhi_authority {
        varchar name PK
    }

    project {
        bigint id PK
        varchar name
        varchar description
        varchar project_key UK
        datetime created_at
        bigint owner_id FK
    }

    project_member {
        bigint id PK
        bigint project_id FK
        bigint user_id FK
        varchar role
        datetime joined_at
    }

    sprint {
        bigint id PK
        varchar name
        varchar goal
        date start_date
        date end_date
        varchar status
        bigint project_id FK
    }

    epic {
        bigint id PK
        varchar title
        varchar description
        varchar status
        varchar priority
        datetime created_at
        datetime updated_at
        date start_date
        date end_date
        bigint project_id FK
    }

    issue {
        bigint id PK
        varchar title
        varchar description
        varchar type
        varchar status
        varchar priority
        datetime created_at
        datetime updated_at
        bigint sprint_id FK
        bigint epic_id FK
        bigint project_id FK
        bigint assignee_id FK
        bigint created_by_id FK
    }

    comment {
        bigint id PK
        varchar content
        datetime created_at
        bigint issue_id FK
        bigint author_id FK
    }

    attachment {
        bigint id PK
        varchar file_name
        varchar file_path
        datetime uploaded_at
        bigint issue_id FK
    }

    action_history {
        bigint id PK
        varchar action
        varchar field_changed
        varchar old_value
        varchar new_value
        datetime created_at
        bigint issue_id FK
        bigint user_id FK
    }

    notification {
        bigint id PK
        varchar message
        bigint issue_id
        varchar issue_title
        bigint user_id
        boolean is_read
        datetime created_at
    }
```

---

## Schéma Relationnel Résumé

```
jhi_user (id, login, password_hash, first_name, last_name, email, image_url, activated, lang_key, activation_key, reset_key, created_by, created_date, reset_date, last_modified_by, last_modified_date)

jhi_authority (name)

jhi_user_authority (user_id, authority_name)

project (id, name, description, project_key, created_at, owner_id)

project_member (id, project_id, user_id, role, joined_at)

sprint (id, name, goal, start_date, end_date, status, project_id)

epic (id, title, description, status, priority, created_at, updated_at, start_date, end_date, project_id)

issue (id, title, description, type, status, priority, created_at, updated_at, sprint_id, epic_id, project_id, assignee_id, created_by_id)

comment (id, content, created_at, issue_id, author_id)

attachment (id, file_name, file_path, uploaded_at, issue_id)

action_history (id, action, field_changed, old_value, new_value, created_at, issue_id, user_id)

notification (id, message, issue_id, issue_title, user_id, is_read, created_at)
```

---

## Matrice des Dépendances

| Table | Dépend de |
|-------|-----------|
| `jhi_user` | — |
| `jhi_authority` | — |
| `jhi_user_authority` | `jhi_user`, `jhi_authority` |
| `project` | `jhi_user` (owner) |
| `project_member` | `project`, `jhi_user` |
| `sprint` | `project` |
| `epic` | `project` |
| `issue` | `project`, `sprint`, `epic`, `jhi_user` (assignee, created_by) |
| `comment` | `issue`, `jhi_user` (author) |
| `attachment` | `issue` |
| `action_history` | `issue`, `jhi_user` |
| `notification` | `jhi_user` |
