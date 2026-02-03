# bank-system (infra scaffolding)

This repository is a **monorepo skeleton** to host multiple microservices (e.g., `account-service`, `auth-service`, `audit-service`) plus a shared local infrastructure stack.

You asked to start with **only the infra + README + .gitignore**. The service folders are intentionally empty placeholders so you can add services gradually.

---

## Repository layout

```
bank-system/
  services/                 # each microservice will live in its own folder
    account-service/        # (create later)
    auth-service/           # (create later)
    audit-service/          # (create later)
  infra/
    docker-compose.yml      # one command to boot the stack
    prometheus/prometheus.yml
    grafana/provisioning/   # auto-provision Prometheus datasource + dashboards
  docs/                     # architecture notes (optional)
  postman/                  # collections (optional)
```

---

## Prerequisites

- Docker Desktop (Windows/macOS) or Docker Engine (Linux)
- Docker Compose v2

---

## Quick start (infra only)

From the repository root:

```bash
docker compose -f infra/docker-compose.yml up -d
```

Open:

- **Prometheus:** http://localhost:9090
- **Grafana:** http://localhost:3000  
  Default login: `admin` / `admin` (Grafana will ask you to change it)

---

## Adding a service later (example: account-service)

1. Create the folder:
   ```
   services/account-service/
   ```
2. Put your Spring Boot project there (pom.xml, src/, Dockerfile, etc.)
3. Add a service section in `infra/docker-compose.yml` pointing to the build context:
   ```yaml
   account-service:
     build: ../services/account-service
     ports:
       - "8080:8080"
     environment:
       SPRING_PROFILES_ACTIVE: docker
       SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/accountdb
       SPRING_DATASOURCE_USERNAME: postgres
       SPRING_DATASOURCE_PASSWORD: postgres
     depends_on:
       - postgres
   ```

4. Expose metrics (recommended):
   - Enable Spring Boot Actuator + Prometheus registry
   - Expose `/actuator/prometheus`
5. Then add a scrape job in `infra/prometheus/prometheus.yml` (template included).

---

## Notes on networking (important)

Inside Docker Compose, containers talk to each other by **service name**.
Example: Grafana should use:

- `http://prometheus:9090`

(not `localhost:9090`), because `localhost` inside the container means “the same container”.

---

## Common commands

Stop:
```bash
docker compose -f infra/docker-compose.yml down
```

Stop + remove volumes (wipes DB, dashboards state, etc.):
```bash
docker compose -f infra/docker-compose.yml down -v
```

See logs:
```bash
docker compose -f infra/docker-compose.yml logs -f
```

---

## Next steps (when you grow)

- Add per-service README files under `services/<service>/README.md`
- Add GitHub Actions workflow for CI
- Add alert rules (Prometheus) + alert channels (Grafana)

