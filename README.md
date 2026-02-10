# Bank System – Infraestrutura (Monorepo Skeleton)

Este repositório é um **esqueleto de monorepo** projetado para hospedar múltiplos **microsserviços** (ex.: `account-service`, `auth-service`, `audit-service`) juntamente com uma **infraestrutura local compartilhada**, baseada em Docker.

O objetivo inicial é fornecer **apenas a camada de infraestrutura**, documentação e padronização, permitindo que novos microsserviços sejam adicionados de forma incremental e organizada.

---

## 📁 Estrutura do repositório

bank-system/
services/ # Cada microsserviço viverá em seu próprio diretório
account-service/ # (placeholder – adicionar futuramente)
auth-service/ # (placeholder – adicionar futuramente)
audit-service/ # (placeholder – adicionar futuramente)
infra/
docker-compose.yml # Inicializa toda a stack de infraestrutura
prometheus/
prometheus.yml # Configuração de scrape de métricas
grafana/
provisioning/ # Datasources e dashboards provisionados automaticamente
docs/ # Documentação de arquitetura (opcional)
postman/ # Collections de API (opcional)


---

## 🎯 Objetivos do projeto

- Servir como **base de infraestrutura** para um sistema bancário distribuído
- Facilitar a adoção de **microsserviços com Spring Boot**
- Padronizar **observabilidade** (métricas e monitoramento)
- Demonstrar boas práticas de **arquitetura, DevOps e escalabilidade**
- Atuar como **repositório de portfólio profissional**

---

## 🧰 Tecnologias utilizadas (Infra)

- Docker & Docker Compose
- Prometheus
- Grafana
- PostgreSQL (quando os serviços forem adicionados)

---

## ✅ Pré-requisitos

- Docker Desktop (Windows/macOS) ou Docker Engine (Linux)
- Docker Compose v2

---

## 🚀 Inicialização rápida (somente infraestrutura)

A partir da raiz do repositório, execute:

```bash
docker compose -f infra/docker-compose.yml up -d
Serviços disponíveis:

Prometheus: http://localhost:9090

Grafana: http://localhost:3000

Usuário: admin

Senha: admin (alteração obrigatória no primeiro login)

➕ Adicionando um microsserviço futuramente (exemplo)
Criar o diretório do serviço:

services/account-service/
Adicionar o projeto Spring Boot (pom.xml, src/, Dockerfile, etc.)

Registrar o serviço no infra/docker-compose.yml:

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
Habilitar métricas:

Spring Boot Actuator

Micrometer Prometheus

Endpoint: /actuator/prometheus

Registrar o job no infra/prometheus/prometheus.yml.

🌐 Observações importantes sobre networking
Dentro do Docker Compose, os containers se comunicam pelo nome do serviço, não por localhost.

Correto:

http://prometheus:9090

http://localhost:9090

🛠️ Comandos úteis
Parar os containers:

docker compose -f infra/docker-compose.yml down
Parar e remover volumes:

docker compose -f infra/docker-compose.yml down -v
Ver logs:

docker compose -f infra/docker-compose.yml logs -f
🔮 Próximos passos planejados
Implementação dos microsserviços (Account, Auth, Audit)

CI/CD com GitHub Actions

Observabilidade avançada (alertas, SLAs)

API Gateway e Service Discovery

Segurança com OAuth2 / JWT
```

# Bank System – Infrastructure (Monorepo Skeleton) in English

This repository is a **monorepo skeleton** designed to host multiple **microservices** (e.g. `account-service`, `auth-service`, `audit-service`) along with a **shared local infrastructure stack**, fully containerized using Docker.

The initial focus is **infrastructure only**, allowing services to be added incrementally as the system evolves.

---

## 📁 Repository structure

bank-system/
services/ 
# Each microservice lives in its own folder
account-service/
# (placeholder – to be added later)
auth-service/
# (placeholder – to be added later)
audit-service/ # (placeholder – to be added later)
infra/
docker-compose.yml # One command to boot the entire stack
prometheus/
prometheus.yml # Metrics scraping configuration
grafana/
provisioning/ # Auto-provisioned datasources and dashboards
docs/ # Architecture notes (optional)
postman/ # API collections (optional)


---

## 🎯 Project goals

- Provide a **solid infrastructure baseline** for a distributed banking system
- Support **Spring Boot microservices**
- Standardize **observability and monitoring**
- Demonstrate **clean architecture, DevOps, and scalability**
- Serve as a **professional portfolio repository**

---

## 🧰 Infrastructure stack

- Docker & Docker Compose
- Prometheus
- Grafana
- PostgreSQL (used once services are introduced)

---

## ✅ Prerequisites

- Docker Desktop (Windows/macOS) or Docker Engine (Linux)
- Docker Compose v2

---

## 🚀 Quick start (infra only)

From the repository root:

```bash
docker compose -f infra/docker-compose.yml up -d
Available services:

Prometheus: http://localhost:9090

Grafana: http://localhost:3000

Default user: admin

Default password: admin (change required on first login)

➕ Adding a microservice later (example)
Create the service folder:

services/account-service/
Add your Spring Boot project (pom.xml, src/, Dockerfile, etc.)

Register the service in infra/docker-compose.yml:

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
Enable metrics:

Spring Boot Actuator

Micrometer Prometheus

Endpoint: /actuator/prometheus

Add a scrape job to prometheus.yml.

🌐 Networking notes
Inside Docker Compose, containers communicate using service names, not localhost.

Correct:

http://prometheus:9090
Incorrect:

http://localhost:9090
🛠️ Useful commands
Stop containers:

docker compose -f infra/docker-compose.yml down
Stop and remove volumes:

docker compose -f infra/docker-compose.yml down -v
View logs:

docker compose -f infra/docker-compose.yml logs -f
🔮 Planned next steps
Implement core microservices (Account, Auth, Audit)

CI/CD with GitHub Actions

Advanced observability (alerts, SLIs/SLOs)

API Gateway and Service Discovery

Security with OAuth2 / JWT