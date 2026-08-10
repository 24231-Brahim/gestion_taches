# Infrastructure

## Fichiers Docker Compose

Le projet contient plusieurs fichiers `docker-compose` destinés à différents usages :

### `src/main/docker/services.yml`

```yaml
name: gestiontaches
services:
  postgresql:
    extends:
      file: ./postgresql.yml
      service: postgresql
    profiles:
      - ''
      - prod
```

**Usage** : stack de développement complète (app + PostgreSQL).

```bash
docker compose -f src/main/docker/services.yml up --wait
```

### `src/main/docker/app.yml`

```yaml
name: gestiontaches
services:
  app:
    image: gestiontaches
    environment:
      - _JAVA_OPTIONS=-Xmx512m -Xms256m
      - SPRING_PROFILES_ACTIVE=prod,api-docs,secret-samples
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgresql:5432/gestionTaches
      - SPRING_DATASOURCE_USERNAME=gestionTaches
      - SPRING_DATASOURCE_PASSWORD=
      - SPRING_LIQUIBASE_URL=jdbc:postgresql://postgresql:5432/gestionTaches
    ports:
      - 127.0.0.1:8080:8080
    healthcheck:
      test: ["CMD-SHELL", "bash -c 'exec 3<>/dev/tcp/127.0.0.1/8080; ...'"]
      interval: 5s
      timeout: 5s
      retries: 40
    depends_on:
      postgresql:
        condition: service_healthy
  postgresql:
    extends:
      file: ./postgresql.yml
      service: postgresql
```

**Usage** : déploiement Docker de l'application en production.

```bash
docker compose -f src/main/docker/app.yml up --wait
```

### `src/main/docker/postgresql.yml`

```yaml
name: gestiontaches
services:
  postgresql:
    image: postgres:18.4
    environment:
      - POSTGRES_USER=gestionTaches
      - POSTGRES_HOST_AUTH_METHOD=trust
    healthcheck:
      test: ['CMD-SHELL', 'pg_isready -U $${POSTGRES_USER}']
      interval: 5s
      timeout: 5s
      retries: 10
    ports:
      - 127.0.0.1:5432:5432
```

**Usage** : base PostgreSQL seule.

```bash
docker compose -f src/main/docker/postgresql.yml up --wait
docker compose -f src/main/docker/postgresql.yml down -v
```

**Variables** :
- `POSTGRES_USER=gestionTaches` (nom d'utilisateur par défaut)
- `POSTGRES_HOST_AUTH_METHOD=trust` (pas de password en dev)
- Port exposé : `5432`

### `src/main/docker/monitoring.yml`

```yaml
name: gestiontaches
services:
  prometheus:
    image: prom/prometheus:v3.11.3
    volumes:
      - ./prometheus/:/etc/prometheus/
    command: ['--config.file=/etc/prometheus/prometheus.yml']
    ports:
      - 127.0.0.1:9090:9090
    network_mode: 'host'
  grafana:
    image: grafana/grafana:13.0.1
    volumes:
      - ./grafana/provisioning/:/etc/grafana/provisioning/
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
      - GF_USERS_ALLOW_SIGN_UP=false
      - GF_INSTALL_PLUGINS=grafana-piechart-panel
    ports:
      - 127.0.0.1:3000:3000
    network_mode: 'host'
```

**Usage** : monitoring Prometheus + Grafana.

```bash
docker compose -f src/main/docker/monitoring.yml up
```

**Accès** :
- Prometheus : `http://localhost:9090`
- Grafana : `http://localhost:3000` (admin/admin)

### `src/main/docker/sonar.yml`

```yaml
name: gestiontaches
services:
  sonar:
    container_name: sonarqube
    image: sonarqube:26.5.0.122743-community
    environment:
      - SONAR_FORCEAUTHENTICATION=false
    ports:
      - 127.0.0.1:9001:9000
      - 127.0.0.1:9000:9000
```

**Usage** : analyse de qualité de code SonarQube.

```bash
docker compose -f src/main/docker/sonar.yml up
```

**Accès** : `http://localhost:9000`

### `src/main/docker/jhipster-control-center.yml`

```yaml
name: gestiontaches
services:
  jhipster-control-center:
    image: 'jhipster/jhipster-control-center:v0.5.0'
    environment:
      - _JAVA_OPTIONS=-Xmx512m -Xms256m
      - SPRING_PROFILES_ACTIVE=prod,api-docs,static
      - SPRING_SECURITY_USER_PASSWORD=admin
      - JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET=<secret>
      - SPRING_CLOUD_DISCOVERY_CLIENT_SIMPLE_INSTANCES_GESTIONTACHES_0_URI=http://host.docker.internal:8080
    ports:
      - 127.0.0.1:7419:7419
```

**Usage** : JHipster Control Center (dashboard unifié).

```bash
docker compose -f src/main/docker/jhipster-control-center.yml up
```

**Accès** : `http://localhost:7419` (admin/admin)

**Important** : le JWT secret doit correspondre à celui de l'application (`jhipster.security.authentication.jwt.base64-secret`).

## Variables d'environnement Docker importantes

| Variable | Valeur (app.yml) | Description |
|----------|------------------|-------------|
| `SPRING_PROFILES_ACTIVE` | `prod,api-docs,secret-samples` | Profils actifs |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgresql:5432/gestionTaches` | URL JDBC |
| `SPRING_DATASOURCE_USERNAME` | `gestionTaches` | User PostgreSQL |
| `SPRING_DATASOURCE_PASSWORD` | (vide) | Password PostgreSQL |
| `SPRING_LIQUIBASE_URL` | `jdbc:postgresql://postgresql:5432/gestionTaches` | URL Liquibase |
| `_JAVA_OPTIONS` | `-Xmx512m -Xms256m` | Mémoire JVM |
| `MANAGEMENT_PROMETHEUS_METRICS_EXPORT_ENABLED` | `true` | Export Prometheus |
| `JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET` | (voir fichier) | Secret JWT pour JHCC |

## Configuration Prometheus

Fichier : `src/main/docker/prometheus/prometheus.yml`

Le scrape configuré cible l'application sur `http://localhost:8080/management/prometheus`.

## Configuration Grafana

Fichiers : `src/main/docker/grafana/provisioning/`

- `datasources/datasource.yml` : datasource Prometheus
- `dashboards/dashboard.yml` : dashboard JVM

## Scripts npm utiles

| Commande | Description |
|----------|-------------|
| `npm run services:up` | Démarre PostgreSQL via Docker |
| `npm run docker:db:up` | Démarre PostgreSQL seul |
| `npm run docker:db:down` | Arrête PostgreSQL |
| `npm run java:docker` | Build image Docker (prod) |
| `npm run java:docker:dev` | Build image Docker (dev) |
| `npm run java:docker:prod` | Build image Docker (prod, profile prod) |
