# DEVOPS — Gestion des Tâches

> Plan complet du cycle DevOps de A à Z pour le déploiement et l'exploitation du projet.

---

## 1. Gestion de version (Git)

- [ ] Définir une stratégie de branching (GitFlow / GitHub Flow / Trunk-based)
- [ ] Protéger la branche `main` / `master` (PR required, reviews, status checks)
- [ ] Configurer les hooks Husky existants (pre-commit → lint-staged)
- [ ] Ajouter un hook `commit-msg` (Conventional Commits : `feat:`, `fix:`, `chore:`, etc.)
- [ ] Ajouter un hook `pre-push` (tests unitaires avant push)
- [ ] Taguer les releases (`v1.0.0`, `v1.1.0`, etc.)

---

## 2. Intégration continue (CI)

### 2.1 GitHub Actions (recommandé)

- [ ] Créer `.github/workflows/ci.yml` avec les jobs :
  - **Build** : `mvnw verify -Pprod -DskipTests` (compilation + frontend)
  - **Tests backend** : `mvnw verify -Pdev` (JUnit 5 + JaCoCo)
  - **Tests frontend** : `npmw test` (Vitest)
  - **Tests E2E** : `npmw run e2e` (Cypress)
  - **Lint** : `npmw run lint` (ESLint) + Checkstyle (`mvnw checkstyle:check`)
  - **Analyse SonarQube** : `mvnw sonar:sonar`
  - **Scan de sécurité** : dépendances + SAST
- [ ] Déclencher sur `push` (toutes branches) et `pull_request` (vers `main`)
- [ ] Mettre en cache `.m2/repository` et `node_modules` pour accélérer les builds
- [ ] Ajouter un badge de statut CI dans le `README.md`

### 2.2 Alternative : GitLab CI

- [ ] Créer `.gitlab-ci.yml` avec stages (build, test, lint, sonar, package)
- [ ] Utiliser les runners GitLab (Docker executor)

### 2.3 Alternative : Jenkins

- [ ] Créer un `Jenkinsfile` pipeline déclaratif
- [ ] Stages : Checkout → Build → Test → Sonar → Package → Archive

---

## 3. Build & Package

- [ ] **Build Maven** : `./mvnw verify -Pprod` (JAR Spring Boot)
- [ ] **Build Docker** : `npm run java:docker` (via Jib, base `eclipse-temurin:25-jre-noble`)
- [ ] **Tag Docker** : `gestiontaches:<version>` et `gestiontaches:latest`
- [ ] **Push registry** : Docker Hub, GitHub Container Registry (ghcr.io), GitLab Registry, ou Nexus
- [ ] **Version sémantique** : générée automatiquement par CI (Git tags + commit hash)

---

## 4. Qualité du code

- [ ] **SonarQube** :
  - [ ] Démarrer SonarQube : `docker compose -f src/main/docker/sonar.yml up -d`
  - [ ] Lancer l'analyse : `./mvnw sonar:sonar`
  - [ ] Définir un Quality Gate (coverage ≥ 80%, bugs = 0, vulns = 0, code smells < seuil)
  - [ ] Bloquer le pipeline si le Quality Gate échoue
- [ ] **Checkstyle** : vérification du style Java (déjà configuré)
- [ ] **Spotless** : formatage automatique Java (déjà configuré)
- [ ] **ESLint** + **Prettier** : qualité frontend (déjà configuré avec Husky)
- [ ] **ArchUnit** : tests d'architecture Java (déjà configuré)
- [ ] Ajouter un seuil de couverture minimal dans `pom.xml` (JaCoCo)

---

## 5. Tests automatisés

- [ ] **Tests unitaires backend** : JUnit 5 (`./mvnw test`)
- [ ] **Tests d'intégration backend** : `./mvnw verify` (fichiers `*IT*`, `*IntTest*`)
- [ ] **Tests unitaires frontend** : Vitest (`npm run test`)
- [ ] **Tests E2E** : Cypress (`npm run e2e`)
- [ ] **Rapports de couverture** : JaCoCo (backend) + V8 (frontend)
- [ ] **Testcontainers** : PostgreSQL pour les tests d'intégration (déjà configuré)

---

## 6. Conteneurisation

- [ ] **Dockerfile** optimisé multi-stage (utiliser Jib existant ou créer un Dockerfile manuel)
  - Stage 1 : Build avec Maven + JDK 21
  - Stage 2 : Runtime avec JRE 21 (image alpine distroless)
- [ ] **Docker Compose production** (`docker-compose.prod.yml`) :
  - Service app (image construite)
  - Service PostgreSQL 18
  - Volume persistant pour la BDD
  - Réseau dédié
  - Healthchecks
  - Restart policy (`unless-stopped`)
- [ ] **`.dockerignore`** : exclure `node_modules/`, `target/`, `.git/`, etc.
- [ ] **Optimisation d'image** : minimal layers, utilisateur non-root (déjà dans entrypoint.sh)

---

## 7. Registre d'images / Artifacts

- [ ] Choisir un registre (Docker Hub, GitHub Container Registry, GitLab Registry, Nexus, Harbor)
- [ ] Authentifier le CI au registre (secrets)
- [ ] Push automatique des images après merge sur `main`
- [ ] Nettoyage des anciennes images (politique de rétention)

---

## 8. Déploiement continu (CD)

### 8.1 Prérequis

- [ ] Serveur de production (VM, VPS, ou Kubernetes)
- [ ] Nom de domaine et certificat SSL/TLS (Let's Encrypt / Certbot)
- [ ] Reverse proxy (Nginx, Caddy, ou Traefik)
- [ ] Base de données PostgreSQL managée ou conteneurisée

### 8.2 Stratégie de déploiement

- [ ] **Déploiement manuel** (MVP) :
  - `scp` ou `rsync` du JAR / Docker image → serveur
  - `systemctl restart gestiontaches` ou `docker compose up -d`
- [ ] **Déploiement automatisé** (CI/CD) :
  - **SSH Deploy** : CI scp + restart via SSH
  - **Docker Compose** : CI déploie `docker-compose.prod.yml` via SSH
  - **Kubernetes** : CI met à jour l'image dans le manifest et `kubectl apply`
- [ ] **Blue-Green deployment** ou **Rolling update** (K8s)
- [ ] **Rollback** : script ou Helm `rollback`

### 8.3 Étapes CD

- [ ] Créer une workflow GitHub Actions `deploy.yml` :
  - Déclencheur : push sur `main` + tag `v*`
  - Build Docker image
  - Push vers le registre
  - Déploiement SSH ou K8s
  - Health check post-deploy
  - Notification Slack/Discord/Email

---

## 9. Infrastructure as Code (IaC)

### 9.1 Docker Compose (simple)

- [ ] Consolider `src/main/docker/` en un fichier `docker-compose.prod.yml`
- [ ] Services : app, postgresql, prometheus, grafana
- [ ] Variables d'environnement externalisées (fichier `.env`)

### 9.2 Terraform (avancé)

- [ ] Créer des modules Terraform :
  - `modules/vm` : instance cloud (DigitalOcean, AWS EC2, OVH)
  - `modules/dns` : enregistrement A + certificat TLS
  - `modules/postgres` : base de données managée (optionnel)
- [ ] Provisionner l'infrastructure : `terraform apply`

### 9.3 Ansible (avancé)

- [ ] Playbooks pour configurer le serveur :
  - Installation Docker + Docker Compose
  - Déploiement des stacks
  - Configuration du reverse proxy Nginx
  - Sécurisation (ufw, fail2ban, automatic updates)

### 9.4 Kubernetes (très avancé)

- [ ] Créer les manifests :
  - `Deployment` : app (replicas: 2, resources limits/requests, liveness/readiness probes)
  - `Service` : ClusterIP + LoadBalancer
  - `ConfigMap` / `Secrets` : configuration
  - `PersistentVolumeClaim` : BDD
  - `StatefulSet` : PostgreSQL (ou operator)
  - `Ingress` : TLS + routing
  - `HorizontalPodAutoscaler` : scaling automatique
- [ ] Package avec **Helm** (chart personnalisé)
- [ ] Namespace dédié : `gestion-taches`

---

## 10. Configuration & Secrets

- [ ] Externaliser la configuration :
  - Variables d'environnement (Spring `application-prod.yml` avec `${VAR}`)
  - Fichier `.env` pour Docker Compose
- [ ] **Gestion des secrets** :
  - Ne jamais commiter les secrets dans Git
  - Utiliser les secrets GitHub Actions / GitLab CI pour le CI
  - En production : Docker secrets, HashiCorp Vault, ou AWS Secrets Manager
  - Support `_FILE` déjà présent dans `entrypoint.sh` pour les fichiers secrets Docker
- [ ] Profils Spring :
  - `dev` : H2, devtools, debug
  - `prod` : PostgreSQL, optimisation, TLS

---

## 11. Base de données (Migrations)

- [ ] **Liquibase** : déjà configuré et fonctionnel
- [ ] Convention de nommage des changelogs : `YYYYMMDDHHMMSS_description.xml`
- [ ] Ne jamais modifier un changelog déjà appliqué en production (ajouter un nouveau)
- [ ] Backup BDD automatique avant chaque déploiement
- [ ] Script de rollback Liquibase si nécessaire

---

## 12. Monitoring & Observabilité

### 12.1 Métriques

- [ ] **Prometheus** : déjà configuré (Micrometer)
  - `docker compose -f src/main/docker/monitoring.yml up -d`
- [ ] **Grafana** : déjà configuré (dashboards JVM)
  - Port 3001 (modifier le port dans monitoring.yml)
  - Dashboard personnalisé : requêtes HTTP, cache Hibernate, BDD, JVM heap
- [ ] Endpoints Actuator : `/management/metrics`, `/management/health`, `/management/prometheus`

### 12.2 Logs

- [ ] **Centralisation des logs** :
  - ELK Stack (Elasticsearch + Logstash + Kibana)
  - Ou Loki + Promtail + Grafana (plus léger)
- [ ] **Structured logging** : format JSON pour Logstash
- [ ] **Niveaux de log** : configurable via `/management/loggers`
- [ ] Rotation des logs : `logback-spring.xml` (taille max, historique)

### 12.3 Alerting

- [ ] **Alertmanager** (Prometheus) :
  - Disque > 80%
  - CPU > 90%
  - Mémoire > 90%
  - App down (5xx > seuil)
  - Certificat SSL expire dans < 30 jours
- [ ] **Canaux de notification** : Slack / Discord / Email

### 12.4 Uptime & APM

- [ ] Health check endpoint : `/management/health`
- [ ] Service externe : UptimeRobot, Better Uptime, ou Pingdom
- [ ] APM (optionnel) : Jaeger / Zipkin (tracing) ou Datadog / New Relic

---

## 13. Sécurité

### 13.1 Sécurité applicative

- [ ] **Dépendances** : `./mvnw org.owasp:dependency-check:check` (OWASP Dependency Check)
- [ ] **SAST** : SonarQube (bugs, vulnérabilités, code smells)
- [ ] **DAST** : OWASP ZAP en CI (scan de l'app déployée)
- [ ] **SBoM** : CycloneDX (`./mvnw org.cyclonedx:cyclonedx-maven-plugin:makeBom`)
- [ ] **Headers de sécurité** : X-Content-Type-Options, CSP, HSTS, X-Frame-Options
- [ ] **SSL/TLS** : Let's Encrypt avec auto-renouvellement (Certbot / Caddy)
- [ ] **JWT** : clé forte (HMAC-SHA512), rotation régulière, expiration courte
- [ ] **Rate limiting** : Spring Cloud Gateway ou Nginx `limit_req`

### 13.2 Sécurité infrastructure

- [ ] Firewall (ufw / iptables) : ports 22, 80, 443, 8080 uniquement
- [ ] SSH : clé uniquement, pas de root, port custom ou fail2ban
- [ ] Mises à jour automatiques (unattended-upgrades)
- [ ] Scan de vulnérabilités Docker (Trivy / Clair)
- [ ] Utilisateur non-root dans le conteneur (déjà configuré)

---

## 14. Sauvegarde & Reprise (Backup & DR)

- [ ] **Backup PostgreSQL** :
  - Script `pg_dump` quotidien
  - Rétention : 7 jours (quotidien), 4 semaines (hebdomadaire), 12 mois (mensuel)
  - Stockage : S3 / Backblaze / serveur distant
- [ ] **Backup des secrets** : Vault export / Bitwarden
- [ ] **Test de restauration** mensuel
- [ ] **DR Plan** : documentation de la procédure de reprise complète

---

## 15. Collaboration & Communication

- [ ] Notifications CI/CD vers Slack/Discord/Teams
  - Build failed → channel #dev
  - Déploiement réussi → channel #general
- [ ] Intégration des PR avec le tracking (GitHub Issues / Jira / Linear)
- [ ] **Clean code** : revue de code obligatoire (PR approuvée par au moins 1 pair)
- [ ] **Documentation** : maintenir DEVOPS.md et README.md à jour

---

## 16. Roadmap DevOps

| Phase                             | Priorité | Durée estimée |
| --------------------------------- | -------- | ------------- |
| CI de base (build + test + lint)  | Haute    | 1-2 jours     |
| SonarQube + Quality Gate          | Haute    | 1 jour        |
| Conteneurisation Docker           | Haute    | 1 jour        |
| Déploiement manuel (MVP)          | Haute    | 1 jour        |
| Pipeline CD automatisé            | Moyenne  | 2-3 jours     |
| Gestion des secrets               | Haute    | 1 jour        |
| Monitoring (Prometheus + Grafana) | Moyenne  | 2 jours       |
| Backup BDD                        | Haute    | 1 jour        |
| IaC (Terraform / Ansible)         | Basse    | 3-5 jours     |
| Kubernetes                        | Basse    | 5-10 jours    |
| DAST + SBoM + Scan sécurité       | Moyenne  | 2-3 jours     |
| Centralisation des logs           | Basse    | 2 jours       |

---

## 17. Commandes utiles

```bash
# Build production
./mvnw verify -Pprod

# Build Docker image
npm run java:docker

# Lancer SonarQube
docker compose -f src/main/docker/sonar.yml up -d
./mvnw sonar:sonar

# Lancer monitoring
docker compose -f src/main/docker/monitoring.yml up -d

# Lancer l'app complète
docker compose -f src/main/docker/app.yml up -d

# Tests
./mvnw test                    # Unit backend
./mvnw verify                  # Integration backend
npm run test                   # Unit frontend
npm run e2e                    # E2E Cypress

# OWASP Dependency Check
./mvnw org.owasp:dependency-check:check

# SBoM CycloneDX
./mvnw org.cyclonedx:cyclonedx-maven-plugin:makeBom
```

---

## 18. Références

- Documentation JHipster DevOps : https://www.jhipster.tech/devops/
- Spring Boot Docker : https://spring.io/guides/topicals/spring-boot-docker/
- GitHub Actions : https://docs.github.com/en/actions
- SonarQube : https://docs.sonarqube.org/
