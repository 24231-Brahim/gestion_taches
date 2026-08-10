# Déploiement

## Build du backend

### JAR (standard)

```bash
./mvnw verify -DskipTests --batch-mode -Pprod
```

Cela génère un JAR exécutable dans `target/` contenant :
- Le backend Spring Boot
- Le frontend Angular buildé en mode production
- Les migrations Liquibase

### JAR dev (avec profile dev)

```bash
./mvnw verify -DskipTests --batch-mode -Pdev,webapp
```

### WAR (pour Tomcat externe)

```bash
./mvnw verify -DskipTests --batch-mode -Pwar
```

### Lancer l'application

```bash
java -jar target/*.jar --spring.profiles.active=prod
```

## Build du frontend

```bash
npm run webapp:build:prod
```

Cela génère les assets dans `target/classes/static/` (lorsqu'intégré au build Maven) ou `dist/` (build standalone).

Le build Angular utilise `@angular-builders/custom-esbuild` avec les optimisations suivantes en production :

- `optimization: true`
- `outputHashing: all`
- `sourceMap: false`
- `namedChunks: false`
- `extractLicenses: true`

## Build Docker

### Avec Jib (plugin Maven)

```bash
# Image de production
./mvnw -ntp verify -DskipTests -Pprod jib:dockerBuild

# Image ARM64
./mvnw -ntp verify -DskipTests -Pprod jib:dockerBuild -Djib-maven-plugin.architecture=arm64

# Image dev
./mvnw -ntp verify -DskipTests -Pdev,webapp jib:dockerBuild
```

L'image Docker est basée sur `eclipse-temurin:17-jre` (ou similaire selon la config Jib).

### Avec Docker Compose

```bash
# Production
docker compose -f src/main/docker/app.yml up --wait

# Développement (avec PostgreSQL)
docker compose -f src/main/docker/services.yml up --wait
```

## Variables de déploiement en production

### Obligatoires

| Variable | Description |
|----------|-------------|
| `SPRING_PROFILES_ACTIVE` | `prod` (et éventuellement `api-docs`, `secret-samples`) |
| `SPRING_DATASOURCE_URL` | URL JDBC PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | User PostgreSQL |
| `SPRING_DATASOURCE_PASSWORD` | Password PostgreSQL |
| `jhipster.security.authentication.jwt.base64-secret` | Secret JWT (minimum 256 bits) |

### Optionnelles

| Variable | Description |
|----------|-------------|
| `SPRING_LIQUIBASE_URL` | URL JDBC pour Liquibase (si différent de datasource) |
| `MANAGEMENT_PROMETHEUS_METRICS_EXPORT_ENABLED` | `true`/`false` pour Prometheus |
| `SERVER_PORT` | Port HTTP (défaut: 8080) |
| `_JAVA_OPTIONS` | Options JVM (`-Xmx512m -Xms256m`) |

### TLS (optionnel)

Pour activer HTTPS, générer un keystore :

```bash
keytool -genkey -alias gestiontaches -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore keystore.p12 -validity 3650
```

Puis configurer dans `application-prod.yml` :

```yaml
server:
  port: 443
  ssl:
    key-store: classpath:config/tls/keystore.p12
    key-store-password: <password>
    key-store-type: PKKS12
    key-alias: gestiontaches
```

## Monitoring en production

### Actuator

L'application expose des endpoints de monitoring sous `/management/` :

```bash
curl http://localhost:8080/management/health
curl http://localhost:8080/management/info
curl http://localhost:8080/management/prometheus
```

### Prometheus

Configuré pour scraper `http://localhost:8080/management/prometheus`.

Les métriques exposées incluent :
- JVM (mémoire, threads, GC)
- HTTP (requêtes, temps de réponse)
- Datasource (connexions Hikari)
- Cache (Caffeine)

### Grafana

Dashboards provisionnés dans `src/main/docker/grafana/provisioning/` :
- Dashboard JVM (memory, threads, GC)

Accès : `http://localhost:3000` (admin/admin)

### JHipster Control Center

Pour utiliser le JHipster Control Center en production :

1. Lancer l'application avec un discovery actif (Consul, Eureka ou static).
2. Configurer `SPRING_CLOUD_DISCOVERY_CLIENT_SIMPLE_INSTANCES_GESTIONTACHES_0_URI`.
3. Le JWT secret doit correspondre à celui du Control Center.

Accès : `http://localhost:7419`

## Tests

### Tests unitaires backend

```bash
./mvnw -ntp verify -Dskip.installnodenpm -Dskip.npm --batch-mode
```

### Tests unitaires frontend

```bash
npm test
```

### Tests E2E (Cypress)

```bash
# Développement (interface Cypress)
npm run e2e:dev

# Headless (CI)
npm run e2e:headless
```

### Tests E2E avec Docker

```bash
npm run ci:e2e:prepare:docker
npm run ci:e2e:server:start
npm run ci:e2e:run
npm run ci:e2e:teardown:docker
```

## CI/CD

Le projet inclut des scripts CI dans `package.json` :

```bash
npm run ci:backend:test      # Javadoc + checkstyle + tests backend
npm run ci:frontend:build    # Build frontend
npm run ci:frontend:test     # Build + tests unitaires
npm run ci:e2e:package       # Package JAR + tests E2E
```
