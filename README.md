# Framingham CHD Risk Backend

**University demonstration project. Educational/research use only. NOT a validated clinical decision-support system.**

Standalone Java 21 + Spring Boot 3.5 backend with three-layer REST API, PostgreSQL,
Flyway, MapStruct, validation, standardized errors, Actuator, Swagger UI,
unit tests and a PostgreSQL Testcontainers integration test. No internal/corporate artifacts.

## What works now

- `POST /api/cases` saves a synthetic Framingham feature set.
- `GET /api/cases` lists saved cases (paginated).
- `GET /api/cases/{id}` reads one saved case.
- `GET /api/predictions` lists only **real** saved predictions (empty initially).
- `GET /api/predictions/{id}` reads one saved prediction, if present.
- `POST /api/predictions` loads a case, then calls `PredictionEngine`.
  The default implementation deliberately returns **HTTP 503** because no validated
  model has been installed. It never fabricates a clinical risk percentage.
- Database schema is versioned with Flyway; Hibernate checks it with `ddl-auto=validate`.

**Important:** public JSON field names match the 15 input features in the research dataset (including `BPMeds`, `BMI`, `sysBP`, `diaBP`).
Null predictor values represent missing source values. Imputation, one-hot encoding,
standardization, model export and calibration are NOT implemented. Model inference
must not be enabled until Python-to-Java parity has been tested.

## Project architecture

```text
controller -> service -> repository -> PostgreSQL
                |
                +----> PredictionEngine (currently unavailable)
```

Packages: `controller`, `dto`, `entity`, `repository`, `service`, `mapper`,
`ml`, `config`, `exception`.

## Requirements

- Docker Desktop / Docker Engine with Compose plugin for the easiest start.
- OR Java 21 + Gradle 8.14.x + PostgreSQL 16 for local Java development.
- For Testcontainers integration tests: running Docker Engine.

### Run locally with Docker Compose

1. Copy `.env.example` to `.env` and choose a **local demo-only** password.
2. Execute: `docker compose up --build -d`
3. Check: `http://localhost:8080/actuator/health`
4. Swagger UI: `http://localhost:8080/swagger-ui.html`
5. API examples: see `http/examples.http`.
6. Cleanup: `docker compose down` (preserves data in volume).

The backend is bound to `127.0.0.1:8080` by default so it is NOT automatically
public. Before publicly exposing it, add authentication/access controls and
restrict/disable admin surfaces such as Swagger where appropriate.

### Run with local Gradle

1. Start a PostgreSQL instance and create `framingham` database/user.
2. Set environment variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
3. Execute `gradle bootRun`.
4. Execute tests: `gradle test` (requires Docker for Testcontainers).

**Gradle Wrapper note:** This project archive was authored in a build environment
without Gradle or Maven repositories/network access. Thus the official
`gradle-wrapper.jar` could not be generated and is not included. Once Gradle is
installed, run `gradle wrapper --gradle-version 8.14.3`, and commit the resulting
`gradlew`, `gradlew.bat`, `gradle/wrapper/*` files. Docker builds use the Gradle
builder image directly and do not need a wrapper.

## Configuration

| Environment variable | Purpose |
| --- | --- |
| `DB_URL` | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/framingham` |
| `DB_USERNAME` | DB user (default `framingham`) |
| `DB_PASSWORD` | DB password (**required**, never commit `.env`) |
| `PORT` | HTTP port (default 8080) |
| `SPRING_PROFILES_ACTIVE` | `local` optional |

## Example request

```bash
curl -X POST http://localhost:8080/api/cases \
  -H 'Content-Type: application/json' \
  -d '{"male":1,"age":48,"education":2,"currentSmoker":0,"cigsPerDay":0,"BPMeds":0,"prevalentStroke":0,"prevalentHyp":0,"diabetes":0,"totChol":210.0,"sysBP":125.0,"diaBP":82.0,"BMI":27.3,"heartRate":71.0,"glucose":91.0}'
```

Successful response: `201 Created` with an `id`; then `GET /api/cases/{id}`.
Try `POST /api/predictions` with the saved `caseId` and
`modelKey=logistic-regression-full`: it should respond `503` until a real
validated model is integrated.

## To integrate the existing Python Logistic Regression model

1. Export the **complete** trained preprocessing+model pipeline to a Java-compatible
   representation (e.g. verified ONNX, where its steps are supported).
2. Record the exact feature order, data types, null/imputation policy, category
   encoding, scaling, model version, and class-probability interpretation.
3. Implement `ml.engine.PredictionEngine` with the appropriate runtime.
4. Cross-check prediction probabilities against Python on the same representative
   test cases **before** accepting and storing inference results.
5. Ensure model files are immutable/read-only in the deployed container; the backend
   must remain stateless for Swarm replicas.

## Next phase: Docker Swarm assignment

The assignment needs three **distinct application services**, not three copies of
this backend: minimal static frontend/Nginx + this Java backend + PostgreSQL.
One node is enough; the backend should have at least two replicas. Add an overlay
network, a persistent DB volume, restart/health checks, pinned registry images,
`stack.yml`, real scaling/failure/persistence tests, and Cloudflare Quick Tunnel
(or another external URL). `docker compose up` alone does NOT satisfy Swarm.

This ZIP contains only the **backend foundation**; the frontend, Swarm stack,
Cloudflare tunnel, research model and PDF submission are intentionally not included.

## Security notes

- Do not commit real patient data, credentials or secrets.
- Demo endpoints have **no authentication**; do not publish directly to the internet.
- Synthetic inputs are accepted for educational demonstration only.
- Model absence is deliberate and exposed as 503; this is not a clinical risk score.
- PostgreSQL is available only on the private Compose network.

## Verification status

The source files, configuration and SQL were generated and statically checked.
Compilation and integration tests MUST still be executed in a connected environment
that can resolve Maven Central dependencies and run Docker/Testcontainers.
Do not treat the included tests as having been executed successfully.
