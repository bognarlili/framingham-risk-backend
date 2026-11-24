# Framingham CHD Risk Backend

**University demonstration project. Educational/research use only. NOT a validated clinical decision-support system.**

Java 21 + Spring Boot 3.5 REST API that estimates the 10-year coronary heart disease risk
with logistic regression models (scikit-learn, converted to ONNX). Users log in with
Keycloak and keep a personal journal of their assessments; guests can estimate without
anything being saved.

## Architecture

```text
browser ──► Nginx (frontend, :8090) ──/─────────► static files
                         ├──/auth/realms, /auth/resources ──► Keycloak ──► Postgres (keycloak db)
                         └──/api/──────────────► Spring backend ──► Postgres (framingham db)
                                                    │ JWT check with Keycloak's keys
                                                    └ ONNX Runtime (models in resources/models)
```

- Everything is reached through one origin (Nginx), so no CORS is needed and the same setup
  works on localhost and behind a tunnel with a changing URL.
- Backend layers: `controller → service → repository`; packages `controller`, `dto`,
  `entity`, `repository`, `service`, `mapper`, `ml`, `config`, `exception`.
- The database schema is versioned with Flyway; Hibernate only validates it.

## API

| Endpoint | Access | Description |
| --- | --- | --- |
| `POST /api/assessments` | guest or user | Risk estimate. With a valid token it is also saved to the user's journal (`saved`, `entryId`). Optional `?modelKey=` (default `logistic-regression-full`). |
| `GET /api/journal` | user | Own entries, newest first (`?page=`, `?size=`, default 50): values, probability, contributions. |
| `DELETE /api/journal/{id}` | user | Deletes an own entry (`204`); another user's entry answers `404`. |
| `/actuator/health`, `/swagger-ui.html` | public | Health check, API documentation (with bearer authorization). |

- JSON field names are the dataset's 15 features (`male`, `age`, `education`, `currentSmoker`,
  `cigsPerDay`, `BPMeds`, `prevalentStroke`, `prevalentHyp`, `diabetes`, `totChol`, `sysBP`,
  `diaBP`, `BMI`, `heartRate`, `glucose`).
- **Units:** `totChol` and `glucose` are sent and stored in **mmol/L** (upper bounds 20 and 40
  reject mg/dL values). They are converted to the dataset's mg/dL only for the model
  (`RiskCaseMapper.toFeatures`, factors 38.67 and 18.016).
- `probability` is in [0, 1]. `contributions` give, per feature, how much it moved the
  log-odds (positive = higher risk).
- Errors use one JSON shape (`ApiError`): `400` with per-field `details`, `401`/`403`
  with `WWW-Authenticate`, `404`, `503` for an unknown model.

## Authentication (Keycloak)

- Realm `framingham` is imported on Keycloak's first start from `keycloak/realm-framingham.json`:
  email registration/login, password policy, brute-force protection, Hungarian UI, public
  client `framingham-frontend` (authorization code + PKCE, relative redirect URIs).
- Access tokens carry the audience `framingham-api`. The backend checks the signature with the
  realm's keys (`KEYCLOAK_JWK_SET_URI`), expiry, the issuer path `/auth/realms/framingham`
  (the host varies with the public URL; the signature proves the origin) and the audience.
- A journal entry belongs to the token's `sub`; every journal query is limited to it.
- Keycloak has its own database and user in the same Postgres
  (`docker/postgres/init/10-keycloak-database.sh`, runs when the volume is created).
- Admin console: `http://localhost:8180/auth/admin` (user `admin`, password from `.env`).
  It is not exposed through Nginx.

## Models (ONNX)

The trained scikit-learn pipelines (preprocessing + logistic regression) are in
`model-conversion/models`:

| modelKey | Features |
| --- | --- |
| `logistic-regression-full` | all 15 |
| `logistic-regression-reduced` | without `totChol` and `glucose` |

`model-conversion/convert.py` converts them to ONNX, checks probabilities and contributions
against scikit-learn (2000 random rows and the example case) and writes
`src/main/resources/models/<modelKey>.onnx`. Each file carries `modelKey`, `modelVersion`,
`features` and `contributionFeatures` as metadata. `OnnxPredictionEngine` loads every file at
startup and feeds one float input per feature; missing values go in as NaN and are imputed by
the model. Besides `probabilities`, every model has a `contributions` output (coefficient ×
preprocessed value), which the engine sums per input feature.

Re-run the conversion (no local Python needed):

```bash
docker run --rm -v "$PWD/model-conversion:/work" -v "$PWD/src/main/resources/models:/out" \
  -w /work python:3.11.16-slim sh -c "pip install -q -r requirements.txt && python convert.py /out"
```

## Run locally with Docker Compose

1. Copy `.env.example` to `.env` and set local demo-only passwords
   (`DB_PASSWORD`, `KEYCLOAK_DB_PASSWORD`, `KEYCLOAK_ADMIN_PASSWORD`).
2. `docker compose up -d --build` (Keycloak needs about a minute on first start).
3. Open `http://localhost:8090`, register, and estimate.
4. `docker compose down` stops everything and keeps the data (`-v` also deletes it).

| Service | Address (localhost only) |
| --- | --- |
| frontend (Nginx) | `http://localhost:8090` |
| backend | `http://localhost:8081` (Swagger: `/swagger-ui.html`) |
| Keycloak admin console | `http://localhost:8180/auth/admin` |
| Postgres | `localhost:5432` |

The `frontend` service is built from the sibling repository `../framingham-risk-frontend`.

**Backend from IntelliJ:** run it with `PORT=8082` and `DB_PASSWORD`; it uses Keycloak's keys
from `http://localhost:8180` by default. Set `FRONTEND_BACKEND_URL=http://host.docker.internal:8082`
in `.env` and restart only the frontend: `docker compose up -d --no-deps frontend`.

**Existing Postgres volume:** the Keycloak database is created only for a new volume. For an
older volume run the script once:
`docker compose exec database sh /docker-entrypoint-initdb.d/10-keycloak-database.sh`.

## Configuration

| Environment variable | Purpose |
| --- | --- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Application database (password **required**, never commit `.env`) |
| `KEYCLOAK_JWK_SET_URI` | Keycloak's signing keys (default `http://localhost:8180/auth/realms/framingham/protocol/openid-connect/certs`) |
| `PORT` | HTTP port (default 8080) |
| `SPRING_PROFILES_ACTIVE` | `local` optional (debug logging) |
| `KEYCLOAK_DB_PASSWORD`, `KEYCLOAK_ADMIN_PASSWORD` | Compose only: Keycloak's database user and first admin |
| `FRONTEND_BACKEND_URL` | Compose only: where Nginx forwards `/api` |

## Next phase: Docker Swarm

The assignment needs distinct application services (Nginx frontend, this backend, PostgreSQL,
here also Keycloak), at least two backend replicas, an overlay network, a persistent DB volume,
restart/health checks, pinned images, `stack.yml`, scaling/failure/persistence tests and an
external URL (Cloudflare Quick Tunnel). `docker compose up` alone does NOT satisfy Swarm.

## Security notes

- Do not commit real patient data, credentials or secrets (`.env` is ignored).
- Only guests' estimates are unsaved; logged-in users' values are stored in their journal and
  can be deleted by them.
- All published ports are bound to `127.0.0.1`. Before exposing the stack publicly, consider
  disabling Swagger and use strong Keycloak passwords.
- The model output is a research estimate, not a clinical risk score.
