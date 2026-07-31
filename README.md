# Recruitment Management System

A REST API for managing the end-to-end recruitment pipeline: companies, job postings, candidate applications, interviews, feedback, offers and dashboards — secured with JWT authentication and role-based access control.

Built with **Spring Boot 4.1.0 / Java 17**, **JPA/Hibernate**, **JWT**, **MapStruct**, **Swagger/OpenAPI**, H2 for local development and **PostgreSQL** (via Docker) for realistic runs.

> **Live demo:** https://recruitment-management-system-izme.onrender.com

---

## Table of Contents

1. [Features](#features)
2. [Tech Stack](#tech-stack)
3. [Project Structure](#project-structure)
4. [Roles & Access Control](#roles--access-control)
5. [Getting Started (Local, H2)](#getting-started-local-h2)
6. [Docker & PostgreSQL](#docker--postgresql)
7. [Configuration](#configuration)
8. [API Reference](#api-reference)
9. [Business Rules & State Machines](#business-rules--state-machines)
10. [Testing](#testing)
11. [Logging](#logging)
12. [Deployment Notes](#deployment-notes)
13. [Author](#author)

---

## Features

- **JWT authentication** (register / login, stateless, HS384 signing)
- **Role-based access control** — Admin, Recruiter, Interviewer, Candidate
- **Company management** (CRUD)
- **Job postings** (CRUD + DRAFT/OPEN/CLOSED lifecycle)
- **Candidate profiles** with **resume upload/download** (PDF/DOC/DOCX, stored on disk)
- **Applications** — candidates apply to open jobs (one per job), recruiters drive the pipeline
- **Interviews & feedback** — scheduling (auto-advances the pipeline), interviewer-only status changes and ratings
- **Offers** — created by recruiters, accepted/declined by candidates (cascades to the application)
- **Dashboard statistics** for admin and recruiter
- **Swagger/OpenAPI** UI with bearer-token support
- **Structured SLF4J logging** with business-event logs, per-environment levels and **correlation IDs** (`X-Request-Id`)
- **Global exception handling** with consistent error responses
- Two databases: H2 (zero-config local dev) and PostgreSQL (Docker/production-like)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language / Runtime | Java 17 |
| Framework | Spring Boot 4.1.0 (Web MVC, Security, Data JPA, Validation) |
| Database | H2 (dev, in-memory) / PostgreSQL 16 (via Docker) |
| ORM | Spring Data JPA + Hibernate |
| Auth | JWT (jjwt 0.12.6) + Spring Security 7 |
| Mapping | MapStruct 1.6.3 + Lombok |
| Logging | SLF4J + Logback (business events, MDC correlation IDs) |
| API Docs | springdoc-openapi 2.7.0 |
| Tests | JUnit 5, Mockito, Spring Boot Test (MockMvc) |
| Build | Maven (mvnw wrapper) |
| Container | Docker / Docker Compose |

---

## Project Structure

```
src/main/java/com/recruitment/
├── config/          # DataInitializer (seed admin+interviewer), OpenApiConfig
├── controller/      # REST controllers
├── dto/             # Request/Response records
├── enums/           # ApplicationStatus, JobStatus, InterviewStatus, InterviewType, OfferStatus, RoleType
├── exception/       # ApiError, GlobalExceptionHandler, NotFoundException, ForbiddenException
├── mapper/          # MapStruct mappers (Entity <-> DTO)
├── model/           # JPA entities: User, Role, Candidate, Company, JobPosting, Application, Interview, Feedback, Offer
├── repository/      # Spring Data JPA repositories
├── security/        # JwtTokenProvider, JwtAuthenticationFilter, RequestIdFilter, CustomUserDetailsService, SecurityConfig
└── service/         # Business logic (SLF4J business-event logs)
src/main/resources/
├── application.yaml                    # Base config (port, profiles, env-driven secrets)
├── application-local.yaml              # H2 dev config (GITIGNORED)
├── application-local.example.yaml      # Template for the local profile
├── application-postgres.yaml           # PostgreSQL profile (env-driven datasource)
└── logback-spring.xml                  # Console appender, MDC requestId pattern, env-driven level
src/test/java/com/recruitment/
├── AuthFlowIntegrationTest.java
└── service/         # JobPostingServiceTest, ApplicationServiceTest, OfferServiceTest
src/test/resources/application-test.yaml
Dockerfile                              # Multi-stage image (builds jar in-container)
docker-compose.yml                      # postgres + app stack
render.yaml                             # Render Blueprint (web service + secrets)
.env.example                            # Template for deployment secrets (real .env is gitignored)
```

### Domain model

```
User ──< Role (ManyToMany)
Candidate ──< Application >── JobPosting >── Company
Application ──< Interview >── User (interviewer)
Interview ──< Feedback >── User (author)
Application ──1── Offer
```

---

## Roles & Access Control

| Route pattern | Allowed roles |
|---|---|
| `/api/auth/**` | public |
| `/swagger-ui/**`, `/v3/api-docs/**`, `/error` | public |
| `/api/admin/**` | ADMIN |
| `/api/recruiter/**` | ADMIN, RECRUITER |
| `/api/interviewer/**` | ADMIN, INTERVIEWER |
| `/api/me/**` and everything else | any authenticated user |

**How roles are assigned:**
- **CANDIDATE** — every self-registration creates a CANDIDATE user.
- **ADMIN / INTERVIEWER** — seeded at startup (see below); other privileged accounts are created by provisioning in the database.

### Seeded accounts

| Email | Password | Role |
|---|---|---|
| `admin@recruitment.com` | `admin123` | ADMIN |
| `interviewer@recruitment.com` | `interviewer123` | INTERVIEWER |

> ⚠️ Dev-only credentials. Override before any real deployment (see [Configuration](#configuration)).

---

## Getting Started (Local, H2)

Zero configuration — H2 runs in-memory, no database to install.

**Prerequisites:** JDK 17, no other services required.

```bash
# from the project root
./mvnw spring-boot:run
```

The app starts on **http://localhost:8081** using the `local` profile (H2 in-memory).

Quick smoke test:

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@recruitment.com","password":"admin123"}'
```

> Note: because H2 is in-memory, all data is lost on restart. Use PostgreSQL (below) when you want persistence.

---

## Docker & PostgreSQL

There are two ways to use PostgreSQL.

### A. Full stack (app + database) in Docker

```bash
docker compose up -d           # multi-stage build (compiles jar in-container), starts postgres + app
```

- App: **http://localhost:8081** (container)
- PostgreSQL: host port **5433** → container 5432
  - Database `recruitment`, user `recruitment`, password `recruitment` (defaults)

```bash
docker compose down            # stop containers (data volume is kept)
docker compose down -v         # stop and wipe the database volume
docker compose logs -f app     # follow app logs
```

### B. App on the host, database in Docker

```bash
docker compose up -d postgres  # start only PostgreSQL
SPRING_PROFILES_ACTIVE=postgres ./mvnw spring-boot:run
```

The app connects to `localhost:5433` by default (`application-postgres.yaml`).

> Port 5433 (not 5432) is used because a native PostgreSQL commonly occupies 5432 on this machine. Change the mapping in `docker-compose.yml` if desired.

---

## Configuration

All secrets are **environment-variable driven** with dev defaults. Production values are supplied via `.env` or the shell at deployment time.

| Variable | Default | Used for |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `local` | `local` = H2, `postgres` = PostgreSQL |
| `PORT` | `8081` | HTTP port |
| `DB_URL` | `jdbc:postgresql://localhost:5433/recruitment` | PostgreSQL JDBC URL (postgres profile) |
| `DB_USERNAME` | `recruitment` | PostgreSQL user |
| `DB_PASSWORD` | `recruitment` | PostgreSQL password |
| `JWT_SECRET` | dev placeholder | JWT signing key (≥32 bytes) |
| `JWT_EXPIRATION_MS` | `86400000` | Token lifetime (1 day) |
| `UPLOAD_DIR` | `uploads` | Resume storage directory |
| `LOG_LEVEL` | `INFO` | Log level for `com.recruitment` (e.g. `DEBUG`, `WARN`) |
| `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` | `recruitment` | PostgreSQL container init (docker-compose) |

**HTTPS:** terminate TLS at a reverse proxy (nginx/Caddy/Traefik), or configure Spring Boot SSL directly — see the commented `server.ssl.*` block in `application.yaml`.

---

## API Reference

Interactive docs: **http://localhost:8081/swagger-ui/index.html** (add a `Bearer <token>` after logging in).

All JSON endpoints require `Authorization: Bearer <token>` except `/api/auth/**`.

### Auth
| Method | Path | Body | Description |
|---|---|---|---|
| POST | `/api/auth/register` | `{name, email, password}` | Register (creates CANDIDATE). Returns `{token, email, roles}` |
| POST | `/api/auth/login` | `{email, password}` | Login. Returns `{token, email, roles}` |

### Current user
| Method | Path | Description |
|---|---|---|
| GET | `/api/me` | Current user `{id, name, email, roles}` |

### Candidate profile (self-service)
| Method | Path | Body | Description |
|---|---|---|---|
| GET | `/api/me/candidate` | — | Get profile (auto-creates from the user on first access) |
| PUT | `/api/me/candidate` | `{firstName, lastName, phone}` | Update profile |
| POST | `/api/me/candidate/resume` | multipart `file` | Upload resume (PDF/DOC/DOCX, max 5 MB) |
| GET | `/api/me/candidate/resume` | — | Download resume |

### Companies (`/api/recruiter/**`)
| Method | Path | Body | Description |
|---|---|---|---|
| POST | `/api/recruiter/companies` | `{name, description, location, website}` | Create company |
| GET | `/api/recruiter/companies` | — | List companies |
| GET | `/api/recruiter/companies/{id}` | — | Get company |
| PUT | `/api/recruiter/companies/{id}` | `{name, description, location, website}` | Update company |
| DELETE | `/api/recruiter/companies/{id}` | — | Delete company (204) |

### Job postings (`/api/recruiter/jobs`)
| Method | Path | Query / Body | Description |
|---|---|---|---|
| POST | `/api/recruiter/jobs` | `{title, description, location, salaryRange, companyId}` | Create (status DRAFT) |
| GET | `/api/recruiter/jobs` | `?companyId=&status=` | List (filterable) |
| GET | `/api/recruiter/jobs/{id}` | — | Get job |
| PUT | `/api/recruiter/jobs/{id}` | same as create | Update job |
| PATCH | `/api/recruiter/jobs/{id}/status` | `{status}` | Change status (see state machine) |
| DELETE | `/api/recruiter/jobs/{id}` | — | Delete job (204) |

### Applications
| Method | Path | Body / Query | Role | Description |
|---|---|---|---|---|
| POST | `/api/me/applications` | `{jobId}` | candidate | Apply to an OPEN job (once per job) |
| GET | `/api/me/applications` | `?status=` | candidate | List own applications |
| GET | `/api/me/applications/{id}` | — | candidate | Own application detail |
| GET | `/api/recruiter/jobs/{jobId}/applications` | — | recruiter/admin | Applications for a job |
| PATCH | `/api/recruiter/applications/{id}/status` | `{status}` | recruiter/admin | Advance pipeline status |

### Interviews & feedback
| Method | Path | Body | Role | Description |
|---|---|---|---|---|
| POST | `/api/recruiter/applications/{applicationId}/interviews` | `{scheduledAt, type, interviewerEmail}` | recruiter/admin | Schedule interview (auto-advances pipeline) |
| GET | `/api/recruiter/applications/{applicationId}/interviews` | — | recruiter/admin | Interviews for an application |
| GET | `/api/interviewer/interviews/me` | `?status=` | interviewer | My interviews |
| PATCH | `/api/interviewer/interviews/{id}/status` | `{status}` | assigned interviewer/admin | Complete/cancel a SCHEDULED interview |
| POST | `/api/interviewer/interviews/{id}/feedback` | `{rating, comments}` | assigned interviewer/admin | Submit feedback (1–5 stars, interview must be COMPLETED) |
| GET | `/api/interviewer/interviews/{id}/feedbacks` | — | interviewer/admin | Feedback list |
| GET | `/api/recruiter/interviews/{id}/feedbacks` | — | recruiter/admin | Feedback list |

### Offers
| Method | Path | Body | Role | Description |
|---|---|---|---|---|
| POST | `/api/recruiter/applications/{applicationId}/offer` | `{salary, joiningDate}` | recruiter/admin | Create offer (application must be OFFERED) |
| GET | `/api/recruiter/applications/{applicationId}/offer` | — | recruiter/admin | Offer for an application |
| GET | `/api/recruiter/offers` | `?status=` | recruiter/admin | List offers |
| GET | `/api/me/applications/{applicationId}/offer` | — | candidate | View my offer |
| POST | `/api/me/applications/{applicationId}/offer/response` | `{status}` | candidate | ACCEPT or DECLINE (cascades to application) |

### Dashboard
| Method | Path | Role |
|---|---|---|
| GET | `/api/admin/dashboard/stats` | ADMIN |
| GET | `/api/recruiter/dashboard/stats` | ADMIN, RECRUITER |

Returns: company/job/candidate/application counts, applications grouped by status, offer counts, average feedback rating, and interview counts.

### Error format

```json
{ "timestamp": "2026-07-31T10:00:00", "status": 400, "message": "..." }
```

- 400 validation / business-rule violation, 401 bad credentials, 403 forbidden, 404 not found, 409 duplicate data, 500 unexpected error.

---

## Business Rules & State Machines

### Job status
```
DRAFT ──► OPEN ──► CLOSED
               ▲       │
               └───────┘   (CLOSED can reopen to OPEN)
```
Applications are only accepted for jobs with status **OPEN**.

### Application pipeline
```
APPLIED ──► SCREENING ──► TECHNICAL_INTERVIEW ──► HR_INTERVIEW ──► OFFERED ──► ACCEPTED / REJECTED
   │            │                │                    │                │
   └────────────┴────────► REJECTED (allowed at any active stage; terminal)
```
- A candidate can apply **once per job**.
- Scheduling a TECHNICAL interview moves the application to `TECHNICAL_INTERVIEW`; an HR interview moves it to `HR_INTERVIEW`.
- Interviews can only be scheduled for applications in the active pipeline (`SCREENING`, `TECHNICAL_INTERVIEW`, `HR_INTERVIEW`).

### Interview status
```
SCHEDULED ──► COMPLETED
       └────► CANCELLED
```
Only the assigned interviewer (or an admin) can change status; only SCHEDULED interviews can be changed; feedback requires a COMPLETED interview.

### Offer status
```
PENDING ──► ACCEPTED   (candidate accept → application ACCEPTED)
     └────► DECLINED   (candidate decline → application REJECTED)
```
Offers can only be created for applications at `OFFERED` status; one offer per application.

---

## Testing

```bash
./mvnw test
```

24 tests, all passing:
- **Unit tests (Mockito):** `JobPostingServiceTest`, `ApplicationServiceTest`, `OfferServiceTest` — status transitions, duplicate application prevention, offer rules.
- **Integration tests (MockMvc + Spring Security):** `AuthFlowIntegrationTest` — register/login returns tokens, `/api/me`, seeded admin login, candidate blocked from admin routes.
- **Context test:** `RecruitmentManagementSystemApplicationTests`.

Tests use a dedicated in-memory H2 profile (`application-test.yaml`), so no external services are required.

---

## Logging

The project uses **SLF4J** (interface) with **Logback** (implementation) — included by default in Spring Boot, no extra dependencies.

- **Business-event logs** — key actions are logged at `INFO` with the relevant identifiers, e.g. `New user registered: <email>`, `Candidate <email> applied to job <id>`, `Offer <id> ACCEPTED by <email>`.
- **Error logs** — the global exception handler logs full stack traces at `ERROR`.
- **No secrets** — passwords, tokens and sensitive data are never logged (message parameters use `{}` placeholders).
- **Correlation IDs** — every request gets an ID (from the `X-Request-Id` header, or a generated UUID) stored in the MDC and printed on every log line, so you can trace a single request through all services. The same ID is echoed back in the `X-Request-Id` response header.
- **Levels per environment** — `logback-spring.xml` sets the `com.recruitment` level from the `LOG_LEVEL` env var (default `INFO`); local dev additionally enables `DEBUG` via `application-local.yaml`.

Example output:

```
2026-07-31T20:41:25.181+05:30 INFO  [http-nio-8081-exec-1] c.r.service.AuthService [9da99047-fd0b-40fe-8687-3a3c10ed14dd] - New user registered: logtest@example.com
```

On Render, these logs are captured automatically in the **Logs** tab of your service.

---

## Deployment Notes

1. **Secrets:** copy `.env.example` to `.env` and set `POSTGRES_PASSWORD` and `JWT_SECRET` to strong, unique values. `.env` is gitignored and never committed.
2. **Default accounts:** change/disable the seeded `admin@recruitment.com` / `interviewer@recruitment.com` credentials before exposing the app.
3. **Database:** the `postgres_data` volume stores credentials from *first* initialization — changing `POSTGRES_PASSWORD` later requires recreating the volume (`docker compose down -v`).
4. **Schema:** `ddl-auto: update` is convenient for development but not safe for production schema changes — introduce Flyway/Liquibase migrations before real deployment.
5. **HTTPS:** terminate TLS at a reverse proxy or configure `server.ssl.*` (see `application.yaml`).
6. **Uploads:** resumes are stored on local disk (`UPLOAD_DIR`). In a multi-instance deployment, mount a shared volume/object store.

---

## Author

**Manu Kumar H N**
- LinkedIn: [Manu Kumar H N](https://www.linkedin.com/in/manu-kumar-h-n-6879482b7)
