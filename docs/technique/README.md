# Documentation technique — GestionTâches

## Sommaire

Cette documentation technique s'adresse aux développeurs qui rejoignent le projet. Elle décrit l'architecture, le modèle de données, la sécurité, le frontend, le temps réel, l'infrastructure, les environnements et le déploiement.

### Fiches disponibles

1. [Architecture globale](./architecture.md)
2. [Modèle de données](./modele-donnees.md)
3. [Sécurité](./securite.md)
4. [Frontend Angular](./frontend.md)
5. [Chat temps réel](./chat-temps-reel.md)
6. [Infrastructure Docker](./infrastructure.md)
7. [Environnements (dev / prod)](./environnements.md)
8. [Déploiement](./deploiement.md)

### Stack technique

| Couche | Technologie | Version |
|--------|------------|---------|
| Backend | Spring Boot | 4.0.6 |
| Langage backend | Java | 21 |
| Frontend | Angular | 21.2.14 |
| Base de données | PostgreSQL | 16 (18.4 dans Docker) |
| Build backend | Maven | 3.2.5+ |
| Build frontend | npm | 11.15.0 (Node v24.16.0) |
| ORM | Hibernate | (via Spring Boot) |
| Migration DB | Liquibase | (via JHipster) |
| Sécurité | Spring Security + JWT (HS512) | |
| Temps réel | WebSocket / STOMP / SockJS | |
| Cache | Caffeine + JCache | |
| Monitoring | Micrometer + Prometheus | |
| Image Docker | Jib (eclipse-temurin:25-jre-noble) | |

### Génération du projet

Le projet a été généré avec **JHipster 9.1.0**. Le modèle de domaine est défini dans `project-management.jdl`.

### Conventions de nommage

- Packages Java : `com.gestiontaches.{couche}` (`domain`, `repository`, `service`, `web.rest`, `config`, `security`)
- Tables PostgreSQL : snake_case (ex: `chat_message`, `project_member`)
- Entités JPA : PascalCase correspondant (ex: `ChatMessage`, `ProjectMember`)
- Composants Angular : kebab-case avec préfixe `jhi-` (ex: `jhi-task-detail-panel`)
