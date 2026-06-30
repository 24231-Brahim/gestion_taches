# Fiche Technique — Diagramme de Classes et Rôles des Tables

## Diagramme de Classes (Mermaid)

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
Commentaire texte attaché à une issue. Permet la discussion et le suivi collaboratif.

### Attachment
Fichier joint à une issue (capture d'écran, document, etc.). Stocke le chemin du fichier et son nom original.

### ActionHistory
Trace d'audit détaillant chaque modification d'une issue. Enregistre l'action, le champ modifié, l'ancienne et la nouvelle valeur.

---

## Dépendances entre Tables

| Table | Dépend de | Est utilisé par |
|-------|-----------|-----------------|
| User | — | Project (owner), ProjectMember |
| Project | User (owner) | Sprint, Epic, Issue, ProjectMember |
| ProjectMember | Project, User | — |
| Sprint | Project | Issue |
| Epic | Project | Issue |
| Issue | Project, Sprint, Epic | Comment, Attachment, ActionHistory |
| Comment | Issue | — |
| Attachment | Issue | — |
| ActionHistory | Issue | — |
