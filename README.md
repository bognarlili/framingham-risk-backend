# Framingham CHD Risk Backend

**University demonstration project. Educational/research use only. NOT a validated clinical decision-support system.**

Standalone Java 21 + Spring Boot 3.5 backend with three-layer REST API, PostgreSQL,
Flyway, MapStruct, validation, standardized errors, Actuator, Swagger UI,
ONNX Runtime inference and unit tests. No internal/corporate artifacts.

## What works now

- `POST /api/cases` saves a synthetic Framingham feature set.
- `GET /api/cases` lists saved cases (paginated).
- `GET /api/cases/{id}` reads one saved case.
- `GET /api/predictions` lists saved predictions.
- `POST /api/assessments` (for the frontend) takes the same body as `POST /api/cases`,
  saves the case and then tries the prediction in one call. It always answers `201`;
  `modelStatus` is `AVAILABLE` (with `probability` and `summary`) or `UNAVAILABLE`
  (case saved, `probability` null, Hungarian `message`). Optional `?modelKey=`
  (default `logistic-regression-full`).
- `POST /api/predictions` loads a case, runs the requested model and saves the result.
  Models: `logistic-regression-full` (15 features) and `logistic-regression-reduced`
  (without `totChol` and `glucose`). An unknown `modelKey` answers **HTTP 503**.
- Database schema is versioned with Flyway; Hibernate checks it with `ddl-auto=validate`.

**Important:** public JSON field names match the 15 input features in the research dataset (including `BPMeds`, `BMI`, `sysBP`, `diaBP`).
**Units:** `totChol` and `glucose` are sent and stored in **mmol/L** (upper bounds 20 and 40
reject values entered in mg/dL). They are converted to the dataset's mg/dL only when model
features are built (`RiskCaseMapper.toFeatures`, factors 38.67 and 18.016).
Null predictor values represent missing source values; they reach the model as NaN and are
imputed by the model's own preprocessing.

## Project architecture

```text
controller -> service -> repository -> PostgreSQL
                |
                +----> PredictionEngine (ONNX Runtime, models in src/main/resources/models)
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

**Frontend (Nginx):** the `frontend` service is built from the sibling repository
`../framingham-risk-frontend` and served on `http://localhost:8090`. Nginx serves the
static files and forwards `/api/*` to the backend, so the browser talks to one origin and
no CORS configuration is needed. With the backend running from IntelliJ instead, set
`FRONTEND_BACKEND_URL=http://host.docker.internal:8082` in `.env` and start only the
frontend: `docker compose up -d --build --no-deps frontend`.

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
  -d '{"male":1,"age":48,"education":2,"currentSmoker":0,"cigsPerDay":0,"BPMeds":0,"prevalentStroke":0,"prevalentHyp":0,"diabetes":0,"totChol":5.4,"sysBP":125.0,"diaBP":82.0,"BMI":27.3,"heartRate":71.0,"glucose":5.1}'
```

Successful response: `201 Created` with an `id`; then `GET /api/cases/{id}`.
Then `POST /api/predictions` with the saved `caseId` and
`modelKey=logistic-regression-full` returns `201` with the probability (0–1).

## Models (ONNX)

The trained scikit-learn pipelines (preprocessing + logistic regression) are in
`model-conversion/models`. `model-conversion/convert.py` converts them to ONNX, checks the
result against scikit-learn (2000 random rows and the example case, tolerance 1e-5) and
writes `src/main/resources/models/<modelKey>.onnx`. Each file carries its `modelKey`,
`modelVersion` and feature list as metadata. `OnnxPredictionEngine` loads every file at
startup and feeds one float input per feature, named like the dataset column.
Besides `probabilities`, every model has a `contributions` output (coefficient × preprocessed
value, in log-odds); the engine sums it per input feature (metadata `contributionFeatures`)
and the result is stored with the prediction (`predictions.contributions`, JSONB).

Re-run the conversion (no local Python needed):

```bash
docker run --rm -v "$PWD/model-conversion:/work" -v "$PWD/src/main/resources/models:/out" \
  -w /work python:3.11.16-slim sh -c "pip install -q -r requirements.txt && python convert.py /out"
```

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
- The model output is a research estimate, not a clinical risk score.
- PostgreSQL is available only on the private Compose network.

## Verification status

The source files, configuration and SQL were generated and statically checked.
Compilation and integration tests MUST still be executed in a connected environment
that can resolve Maven Central dependencies and run Docker/Testcontainers.
Do not treat the included tests as having been executed successfully.
