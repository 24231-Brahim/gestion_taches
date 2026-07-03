# Présentation Stage S4 — Gestion de Tâches

> **Durée :** 10-15 min | **Jury :** Académique | **Projet :** En cours (avancement ~85%)

---

## Slide 1 — Page de Garde (30 sec)

**Gestion de Tâches** — Application web de gestion de projet agile

- **Stagiaire :** Brahim
- **Stage :** DSI — 2 mois
- **Formation :** S4 (BUT Informatique)
- **Encadrant :** Med
- **Date :** Juin 2026

---

## Slide 2 — Sommaire (30 sec)

1. Contexte & Présentation de l'entreprise
2. Problématique & Objectifs
3. Stack technique
4. Architecture du système
5. Fonctionnalités réalisées
6. État d'avancement & démo
7. Difficultés rencontrées
8. Travail restant & perspectives
9. Compétences acquises
10. Conclusion

---

## Slide 3 — Contexte & Entreprise (1 min)

**Structure d'accueil :** DSI (Direction des Systèmes d'Information)

- Service en charge des infrastructures et applications informatiques
- Contexte : besoin d'un outil de gestion de projet interne, moderne et maîtrisé

**Contexte du projet :**

- Projet de fin d'année S4 — Mise en pratique des compétences full-stack
- Objectif : développer un outil type Jira, mais open-source, personnalisable et maîtrisé de bout en bout

---

## Slide 4 — Problématique (1 min)

**Constats :**

- Solutions existantes (Jira, Trello, Linear) → payantes, surdimensionnées, ou impossible à personnaliser
- Données hébergées chez un tiers → problématique RGPD / confidentialité
- Besoin de maîtrise totale du code et des données

**Problématique :**

> Comment développer une application web de gestion de projet agile, sécurisée, extensible et maîtrisée de bout en bout ?

**Enjeux :**

- Sécurité (JWT, RBAC)
- Expérience utilisateur (UI moderne, responsive)
- Maintenabilité (code structuré, testé, documenté)
- Extensibilité (architecture modulaire)

---

## Slide 5 — Objectifs (1 min)

**Objectifs initiaux (cahier des charges) :**

- Gestion complète des projets avec système de rôles (RBAC)
- Création et suivi d'issues (tâches, bugs, stories)
- Organisation par sprints et epics
- Traçabilité via historique d'actions
- Interface moderne, responsive, dark/light
- Authentification sécurisée JWT

---

## Slide 6 — Stack Technique (1 min)

**Backend :**
| Technologie | Rôle |
|-------------|------|
| Java 21 + Spring Boot 4.0.6 | API REST |
| Spring Security + JWT | Auth & RBAC |
| Spring Data JPA / Hibernate | ORM |
| Liquibase | Migrations BDD |
| MapStruct | Mapping DTO/Entité |
| PostgreSQL / H2 | Base de données |

**Frontend :**
| Technologie | Rôle |
|-------------|------|
| Angular 21.2 + TypeScript | SPA |
| Bootstrap 5 + ng-bootstrap | UI responsive |
| ngx-translate | i18n |
| RxJS | Programmation réactive |
| Vitest + Cypress | Tests |

**Outils :** JHipster 9.1 (scaffolding), Docker Compose, SonarQube, Git + Husky

---

## Slide 7 — Architecture du Système (1 min 30)

**Architecture :** Monolithique full-stack avec séparation stricte des couches

```
Angular SPA  ←→  REST API (JSON + JWT)  ←→  Spring Boot  ←→  PostgreSQL/H2
```

**Flux :**

1. Auth JWT → token dans le claim custom `auth`
2. `JwtAuthenticationConverter` custom parse le claim
3. `@PreAuthorize` sur chaque endpoint vérifie les rôles
4. Controller → Service → Repository (Spring Data JPA)
5. MapStruct mappe Entité ↔ DTO

**Sécurité :** 5 rôles (ADMIN, PROJET_MANAGER, DEVELOPER, USER, ANONYMOUS) + ownership checks

---

## Slide 8 — Fonctionnalités Réalisées (2 min)

**Modules métier :**

- ✅ CRUD Projets + gestion d'équipe + ownership
- ✅ CRUD Sprints (Board Kanban, Planning, Burndown) + validation sprint actif unique
- ✅ CRUD Epics (Roadmap timeline avec barres de progression)
- ✅ CRUD Issues (Backlog/Board, drag-drop Kanban, Detail Drawer)
- ✅ Commentaires (avec auteur, CRUD, ownership)
- ✅ Attachements (upload/download fichiers réels)
- ✅ ActionHistory (audit trail complet)

**Sécurité :**

- ✅ RBAC complet (@PreAuthorize) — matrice d'accès par contrôleur
- ✅ Authentification JWT avec converter custom

**Interface :**

- ✅ Dashboard KPIs (6 cartes, graphiques SVG, timeline, quick actions)
- ✅ Sidebar collapsible + BottomNav mobile
- ✅ Thème dark/light + Design system brutaliste
- ✅ i18n (français/anglais)
- ✅ Gestion erreurs unifiée (AlertService)

**+ :** Assignation d'issues + Système de notifications avec polling

---

## Slide 9 — Démo / Captures d'écran (2 min)

> **À préparer :** 3-4 captures clés ou démo live

1. **Dashboard** — KPIs, graphiques, timeline
2. **Liste projets** — Pagination, filtrage, gestion membres
3. **Issue Detail Drawer** — Commentaires, pièces jointes, historique
4. **Sprint Board** — Kanban drag-drop, burndown chart
5. **Epic Roadmap** — Timeline progression
6. **Vue mobile** — BottomNav, responsive
7. **Matrice RBAC** — Permissions par rôle

---

## Slide 10 — État d'Avancement (1 min) ⚠️

**Où j'en suis :**

```
████████████████████████████░░░░   ~85%
```

| Module                       | Statut      |
| ---------------------------- | ----------- |
| Backend (API, sécurité, BDD) | ✅ 100%     |
| Frontend (pages principales) | ✅ 95%      |
| Dashboard & Design system    | ✅ 100%     |
| Tests backend (91 tests)     | ✅ 100%     |
| Tests frontend               | ❌ ~30%     |
| Documentation                | 🔄 En cours |

**Ce qui est fait :** Fonctionnalités métier complètes, sécurité, UI/UX
**Ce qui reste :** Tests frontend, persistance sidebar, pages admin

---

## Slide 11 — Difficultés Rencontrées (1 min 30)

**1. JWT — Claim `auth` non parsé par Spring Security**

- **Problème :** Spring cherchait les rôles dans `scope` au lieu de `auth`
- **Solution :** `JwtAuthenticationConverter` custom

**2. Écrasement de `owner_id` lors de la mise à jour d'un projet**

- **Problème :** Le DTO vide écrasait le propriétaire en base
- **Solution :** Chargement préalable de l'entité avant `partialUpdate()`

**3. Conflit SQL — colonne `key`**

- **Problème :** `key` est un mot réservé PostgreSQL
- **Solution :** Renommage en `project_key`

**4. Z-index Sidebar / Overlay mobile**

- **Problème :** Overlay mal positionné par rapport à la sidebar
- **Solution :** Ajustement des z-index (sidebar 1040, overlay 1035)

---

## Slide 12 — Travail Restant & Perspectives (1 min)

**Court terme (prioritaire) :**

- 🔲 Tests frontend (sprint.spec.ts, Vitest)
- 🔲 Tests d'intégration (assignation, notifications)
- 🔲 Persistance état sidebar (localStorage)
- 🔲 Pages admin dans la navigation

**Moyen terme :**

- 🔲 Tableau Kanban sur le dashboard
- 🔲 Page dédiée liste des notifications
- 🔲 Notifications par email

**Long terme :**

- 🔲 Évolution vers microservices
- 🔲 Déploiement continu (CI/CD)

---

## Slide 13 — Compétences Acquises (1 min)

**Techniques :**

- Architecture full-stack Spring Boot + Angular
- Sécurisation d'API REST (JWT, RBAC, ownership)
- Gestion de migrations BDD (Liquibase)
- Mapping ORM (JPA/Hibernate, MapStruct)
- Design system CSS (variables, dark/light, brutalism)
- Tests d'intégration (Spring Boot, JUnit)
- Utilisation de JHipster

**Transversales :**

- Gestion de projet en autonomie
- Résolution de problèmes (debugging, contournements)
- Documentation technique

---

## Slide 14 — Conclusion (30 sec)

- Application fonctionnelle couvrant le périmètre initial
- Stack technique moderne et cohérente
- Code industrialisable (tests, documentation, architecture)
- Projet enrichissant qui m'a permis de monter en compétence sur tout le spectre du développement web

> **"Un outil de gestion de projet agile… géré de manière agile."**

---

## Slide 15 — Remerciements (30 sec)

- **Encadrant :** Med
- **Équipe DSI** pour l'accueil et l'accompagnement
- **Corps enseignant** pour la formation

---

## Slide 16 — Questions

**Merci pour votre attention.**

Des questions ?
