# Transaction Service

This microservice is part of the **bank‑system** monorepo and is responsible for
all financial transactions. It provides REST endpoints for **deposit**,
**withdraw** and **transfer** operations and persists every transaction to its
own database. The service is idempotent: clients must supply an
`Idempotency-Key` header on each request to ensure that retries do not result
in duplicate transactions. See the idempotent consumer pattern described on
[microservices.io](https://microservices.io/patterns/communication-style/idempotent-consumer.html) which recommends storing processed
message identifiers to avoid duplicate processing【977473953271718†L20-L43】.

Banking applications often isolate transaction processing from other services
such as authentication or customer support. This separation improves
resilience: heavy transaction load will not impact authentication or support
features【565317044852456†L94-L105】. Each microservice manages its own data store
per the **database‑per‑service** pattern【565317044852456†L120-L141】.

The service also exposes a `/actuator/prometheus` endpoint via Spring Boot
Actuator so that **Prometheus** can scrape metrics【586696216700573†L363-L370】.

## Features

* **Endpoints**
  * `POST /transactions/deposit` – deposits funds into an account
  * `POST /transactions/withdraw` – withdraws funds from an account
  * `POST /transactions/transfer` – transfers funds between two accounts
  * `GET /transactions?accountId=…` – lists transactions for the specified
    account
* **Idempotency** – uses a unique `Idempotency‑Key` header to prevent
  duplicate processing on retry
* **Validation** – uses Java Bean Validation to validate incoming request
  payloads
* **Transaction management** – marks service methods as `@Transactional` to
  ensure atomicity
* **External integration** – communicates with the existing `account-service`
  to check balances and apply debits/credits (see `AccountClient`)
* **Observability** – includes Spring Boot Actuator and Micrometer Prometheus
  registry with healthchecks and `/actuator/prometheus` endpoint
* **Documentation** – integrates with `springdoc-openapi` to provide
  interactive Swagger UI at `/swagger-ui.html`
* **Tests** – includes a unit test for the service layer using JUnit and
  Mockito
* **Docker** – multi‑stage Dockerfile builds a small final image and exposes
  port 8082 with a healthcheck

## Running locally

To run the service in development you can use Maven:

```bash
mvn spring-boot:run
```

By default it uses an in‑memory H2 database and listens on port 8082. The
OpenAPI/Swagger UI is available at `http://localhost:8082/swagger-ui.html`. The
Prometheus scrape endpoint is available at
`http://localhost:8082/actuator/prometheus`【586696216700573†L363-L370】.

### Docker Compose

In a Docker Compose stack the service expects the `account-service` to be
available under the hostname `account-service`. A minimal snippet might look
like this:

```yaml
services:
  transaction-service:
    build: ./transaction-service
    ports:
      - "8082:8082"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SERVICES_ACCOUNT_BASE_URL: http://account-service:8081
    depends_on:
      - account-service
```

## Request examples

### Deposit

```http
POST /transactions/deposit HTTP/1.1
Idempotency-Key: 2e53b8fc-9d9c-4a18-9ebb-e1af3dc36a8c
Content-Type: application/json

{
  "accountId": "12345",
  "amount": 100.00
}
```

### Withdraw

```http
POST /transactions/withdraw HTTP/1.1
Idempotency-Key: 79a7b3ae-6f2c-42c1-9187-4ebdbca4cd6c
Content-Type: application/json

{
  "accountId": "12345",
  "amount": 50.00
}
```

### Transfer

```http
POST /transactions/transfer HTTP/1.1
Idempotency-Key: fbe2e5c2-1d51-4e20-9ccf-5d1931818378
Content-Type: application/json

{
  "accountId": "12345",
  "targetAccountId": "67890",
  "amount": 25.00
}
```

## Testing

Run unit tests with:

```bash
mvn test
```

The provided `TransactionServiceTest` exercises the service layer’s business
logic including idempotency, validation and balance checking using Mockito
stubs.