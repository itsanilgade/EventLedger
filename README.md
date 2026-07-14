# Event Ledger

Production-minded implementation of the take-home assignment using two independently deployable Java 21 / Spring Boot services.

## Architecture

Client → **Event Gateway (8080)** → synchronous REST → **Account Service (8081)**

Each service owns a separate H2 database. The Gateway stores the immutable event record and the Account Service owns balances and transaction history.

## Requirements covered

- Idempotency by `eventId`, including safe reconciliation after a lost downstream response
- Conflict response when the same `eventId` is reused with a different payload
- Out-of-order event listing by original `eventTimestamp`
- CREDIT − DEBIT balance computation
- Bean Validation and consistent API errors
- Separate processes and separate embedded databases
- `X-Trace-Id` generation, propagation and JSON logging
- Health, readiness and liveness endpoints
- Prometheus metrics and custom event/transaction counters
- Resilience4j circuit breaker plus HTTP connect/read timeouts
- Graceful degradation: Gateway event reads remain available when Account Service is down
- Automated unit, web, resiliency, trace and integration tests
- Docker Compose, hardened non-root images, persistent volumes and resource limits
- Optional Kubernetes deployment manifest
- Swagger/OpenAPI documentation
- GitHub Actions build, test, coverage and container validation

## No local Java or Gradle required

A reviewer can validate the project entirely through GitHub Actions. The workflow installs Java 21 and Gradle 8.14.3, runs all tests, produces JaCoCo reports, creates boot JARs and builds both Docker images.

## Run with Docker

```bash
docker compose up --build
```

Gateway: `http://localhost:8080`  
Account Service: `http://localhost:8081`

## API documentation

- Gateway Swagger UI: `http://localhost:8080/swagger-ui.html`
- Account Swagger UI: `http://localhost:8081/swagger-ui.html`
- Gateway OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Account OpenAPI JSON: `http://localhost:8081/v3/api-docs`

## Health and metrics

- `/health` — assignment-specific diagnostic endpoint
- `/actuator/health`
- `/actuator/health/readiness`
- `/actuator/health/liveness`
- `/actuator/prometheus`

## Tests

CI command:

```bash
gradle clean test bootJar jacocoTestReport --no-daemon
```

Reports are uploaded as GitHub Actions artifacts.

## Resiliency decision

The Gateway uses a circuit breaker because repeated synchronous calls to an unhealthy Account Service would otherwise consume resources and increase latency. Short connection/read timeouts bound each attempt. The Gateway returns `503` while retaining its local event record, so event lookup/listing remains available. A later identical submission safely reconciles the event because the Account Service is independently idempotent by `eventId`.

## Data durability

The default profile uses in-memory H2 for simple development and tests. The `prod` profile uses file-backed H2 under `/data`; Docker Compose and Kubernetes mount separate persistent volumes for the two services.

## Kubernetes (optional)

Build and publish the images, update image references if needed, then run:

```bash
kubectl apply -f deploy/k8s/event-ledger.yaml
```

Cloud deployment is not required by the assignment; this manifest demonstrates deployment readiness without coupling the solution to AWS, Azure or GCP.

## Assumptions and trade-offs

- H2 is retained because the assignment explicitly permits an embedded database. A production financial system would normally use a managed relational database and schema migrations.
- Synchronous REST is retained because it is an assignment constraint. An outbox/queue would be a next step for guaranteed asynchronous recovery.
- Authentication and authorization are intentionally omitted because they are outside the stated scope.
