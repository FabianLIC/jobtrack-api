# JobTrack API

REST API for tracking job applications, built with Java 21 and Spring Boot.

Keep track of every position you apply to: company, role, salary range, work mode,
application status and timeline notes — all through a clean, authenticated REST interface.

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1 |
| Security | Spring Security · JWT (JJWT) · BCrypt |
| Persistence | Spring Data JPA · Hibernate 7 |
| Database | PostgreSQL 17 |
| Validation | Jakarta Bean Validation |
| Documentation | OpenAPI 3.1 · Swagger UI |
| Testing | JUnit 5 · Mockito |
| Build | Maven |
| Containers | Docker · Docker Compose |

---

## Quick start

The only prerequisite is Docker. No JDK, no Maven, no PostgreSQL installation needed.

```bash
git clone https://github.com/FabianLIC/jobtrack-api.git
cd jobtrack-api

cp .env.example .env        # then edit the values

docker compose up --build
```

That's it. The API runs on `http://localhost:8080` and PostgreSQL starts alongside it
in its own container.

Interactive documentation: **http://localhost:8080/swagger-ui/index.html**

### Running without Docker

Requires JDK 21 and a running PostgreSQL instance.

```bash
docker compose up -d postgres     # or use your own database
./mvnw spring-boot:run            # .\mvnw.cmd on Windows
```

---

## Configuration

All sensitive values are read from environment variables, with development defaults so
the project runs out of the box.

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/jobtrack` | JDBC connection string |
| `DB_USERNAME` | `jobtrack_user` | Database user |
| `DB_PASSWORD` | `jobtrack_pass` | Database password |
| `JWT_SECRET` | *(development key)* | Signing key for JWT tokens — **must be at least 32 characters** |
| `JWT_EXPIRATION` | `86400000` | Token lifetime in milliseconds (24 hours) |

Copy `.env.example` to `.env` and set your own values. The `.env` file is gitignored and
never committed.

---

## Authentication

Every endpoint under `/api/applications` requires a valid JWT. Register or log in to get
one, then send it in the `Authorization` header.

### 1. Register

```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "Fabián",
  "email": "fabian@example.com",
  "password": "password123"
}
```

```json
HTTP/1.1 201 Created

{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "type": "Bearer",
  "email": "fabian@example.com",
  "name": "Fabián"
}
```

### 2. Log in

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "fabian@example.com",
  "password": "password123"
}
```

Returns `200 OK` with the same response shape.

### 3. Use the token

```http
GET /api/applications
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9...
```

Passwords are hashed with BCrypt and never stored or returned in plain text. Tokens are
stateless — the server keeps no session, it only verifies the signature.

---

## API reference

### Authentication — `/api/auth`

| Method | Endpoint | Description | Auth | Success |
|---|---|---|---|---|
| `POST` | `/register` | Create an account | No | `201` |
| `POST` | `/login` | Obtain a token | No | `200` |

### Applications — `/api/applications`

| Method | Endpoint | Description | Auth | Success |
|---|---|---|---|---|
| `GET` | `/` | List your applications, newest first | Yes | `200` |
| `GET` | `/{id}` | Get a single application | Yes | `200` |
| `POST` | `/` | Create an application | Yes | `201` |
| `PUT` | `/{id}` | Full replacement of an application | Yes | `200` |
| `DELETE` | `/{id}` | Delete an application | Yes | `204` |

### Example — create an application

```http
POST /api/applications
Content-Type: application/json
Authorization: Bearer <token>

{
  "company": "Indra Sistemas",
  "position": "Junior Java Developer",
  "status": "APPLIED",
  "location": "Madrid",
  "workMode": "HYBRID",
  "salaryMin": 22000,
  "salaryMax": 28000,
  "offerUrl": "https://www.infojobs.net/...",
  "source": "InfoJobs",
  "appliedAt": "2026-08-18"
}
```

Only `company` and `position` are required. `status` defaults to `SAVED` when omitted.

`api-test.http` contains ready-to-run requests covering every endpoint, including error
cases. Open it in VS Code with the
[REST Client](https://marketplace.visualstudio.com/items?itemName=humao.rest-client)
extension.

---

## Architecture

Layered architecture with a clear separation of concerns:

```
HTTP request
    │
    ▼
JwtAuthenticationFilter ── validates the token, populates the security context
    │
    ▼
Controller ──────────────── receives requests, returns HTTP status codes
    │
    ▼
Service ─────────────────── business logic, transactions, entity ⇄ DTO mapping
    │
    ▼
Repository ──────────────── data access (Spring Data JPA)
    │
    ▼
PostgreSQL
```

**Key design decisions:**

- **DTOs at the boundary.** Entities never leave the service layer. This prevents
  lazy-loading serialization errors, avoids exposing password hashes, and decouples the
  public API contract from the persistence model.
- **Ownership checks on every operation.** Users can only read, update or delete their
  own applications. Requesting someone else's resource returns `404`, not `403`, so the
  API never reveals which IDs exist.
- **Stateless authentication.** No server-side sessions. Any instance can validate any
  token, which makes horizontal scaling trivial.
- **Adapter over inheritance for security.** `UserPrincipal` wraps the `User` entity to
  satisfy Spring Security's `UserDetails` contract, keeping framework concerns out of the
  domain model.
- **Constructor injection** throughout, with `final` fields — no field injection.
- **Centralized error handling** via `@RestControllerAdvice`, producing one consistent
  error format across the whole API.

---

## Data model

```
users
  ├── id, name, email (unique), password (BCrypt), role, created_at
  │
  └──< applications                    (one user → many applications)
         ├── id, company, position, status, location, work_mode,
         │   salary_min, salary_max, offer_url, source,
         │   applied_at, created_at, updated_at, user_id
         │
         └──< notes                    (one application → many notes)
                └── id, content, created_at, application_id
```

**Enums** are persisted as strings (`@Enumerated(EnumType.STRING)`), keeping the data
readable and safe against reordering. Hibernate generates matching `CHECK` constraints at
the database level:

- `ApplicationStatus` — `SAVED`, `APPLIED`, `INTERVIEW`, `OFFER`, `REJECTED`
- `WorkMode` — `ONSITE`, `HYBRID`, `REMOTE`
- `Role` — `USER`, `ADMIN`

Audit fields (`created_at`, `updated_at`) are managed automatically through JPA lifecycle
callbacks.

---

## Error handling

All errors return the same shape, regardless of type:

```json
{
  "errorAt": "2026-09-01T15:16:08.290852",
  "statusCode": 404,
  "error": "Not Found",
  "message": "Application not found with id: 1",
  "failedPath": "/api/applications/1"
}
```

Validation failures add a `failedErrors` map so clients can highlight the exact fields
that were rejected:

```json
{
  "statusCode": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "failedPath": "/api/applications",
  "failedErrors": {
    "salaryMin": "The salary must be positive number",
    "salaryMax": "The salary must be positive number"
  }
}
```

| Status | When |
|---|---|
| `400` | Validation failed, or malformed JSON |
| `401` | Missing, expired or invalid token · bad credentials |
| `404` | Resource does not exist, or does not belong to the requesting user |
| `409` | Email already registered |
| `500` | Unexpected server error |

Internal details — stack traces, class names, SQL fragments — are never returned to the
client. They are written to the server log instead.

---

## Testing

```bash
./mvnw test          # .\mvnw.cmd on Windows
```

Unit tests cover the service layer with Mockito, including the ownership checks that
isolate users from each other. The mocks keep them fast — the full suite runs in well
under a second.

---

## Project structure

```
src/main/java/com/fabianlicea/jobtrack/
├── config/         Spring Security configuration
├── controller/     REST endpoints
├── dto/            request/response records
├── exceptions/     custom exceptions and global handler
├── model/          JPA entities and enums
├── repository/     Spring Data JPA interfaces
├── security/       JWT service, authentication filter, user details
└── service/        business logic and transactions
```

---

## Roadmap

- [x] Domain model and JPA relationships
- [x] Repository layer with derived query methods
- [x] Service layer with transaction management
- [x] DTOs with Bean Validation
- [x] Full CRUD with correct HTTP semantics
- [x] Global exception handling
- [x] Unit tests (JUnit 5 · Mockito)
- [x] Interactive documentation (OpenAPI · Swagger UI)
- [x] Authentication and authorization (Spring Security · JWT)
- [x] Externalized configuration
- [x] Containerization (Docker · Docker Compose)
- [ ] Endpoint tests (MockMvc)
- [ ] Partial updates via `PATCH`
- [ ] Notes endpoints
- [ ] Application statistics
- [ ] CI pipeline (GitHub Actions)

### Known limitations

`PUT` performs a full replacement: fields omitted from the request body are set to null.
A `PATCH` endpoint for partial updates is planned.

`spring.jpa.hibernate.ddl-auto=update` is a development convenience. It adds tables and
columns but never removes or renames them. A production setup would use versioned
migrations (Flyway or Liquibase) instead.

The `Note` entity is mapped and persisted, but no endpoints expose it yet.

---

## About

Built as a portfolio project while learning the Spring ecosystem. The goal was not just to
make it work, but to understand and be able to justify every design decision — why DTOs
instead of entities, why `EnumType.STRING`, why `orphanRemoval`, why the token is
stateless, why the transaction boundary sits where it does.

**Fabián Licea** · [GitHub](https://github.com/FabianLIC)
