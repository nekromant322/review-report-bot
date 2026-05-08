# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

**Build:**
```bash
mvn -B package -f pom.xml
mvn clean package -DskipTests
```

**Test & code style:**
```bash
mvn verify
```

**Run a single test:**
```bash
mvn test -Dtest=ReportServiceTest
```

**Run locally:**
```bash
mvn spring-boot:run
```

**Docker:**
```bash
docker-compose up -d
```

## Architecture

Spring Boot 2.7.10 Telegram bot for mentoring review management. Java 11 source, JDK 21 runtime.

**Entry points:**
- `TelegramApplication.java` — Spring Boot main class; enables Feign, JPA, scheduling
- `MentoringReviewBot.java` — extends `TelegramLongPollingCommandBot`; registers all commands and delegates non-command updates to `NonCommandUpdateHandler`

**Key layers:**

| Package | Purpose |
|---|---|
| `commands/` | 50+ Telegram command handlers implementing `MentoringReviewCommand` |
| `controller/` | REST endpoints (admin panel, pricing page, payment details, incoming reviews) |
| `service/` | Business logic; `SendMessageService` wraps all Telegram message delivery |
| `service/update_handler/` | Processes free-text messages and inline callback buttons |
| `model/` | JPA entities (PostgreSQL via Hibernate, migrations via Liquibase) |
| `repository/` | Spring Data JPA repos |
| `sheduler/` | `DailyScheduler` fires every minute to deliver scheduled messages |
| `contants/` | Enums: `UserType` (STUDENT/MENTOR/DEV), `ServiceType`, `Command`, `PayStatus`; `MessageConstants` holds all Russian-language message templates |

**Payment flow:** `ClientPaymentRequestServiceProvider` abstracts payment types (resume review, mentoring subscription, personal call). Payment processing uses LifePay SBP integration.

**Database:** Liquibase manages schema (`src/main/resources/db/changelog/`); `spring.jpa.hibernate.ddl-auto=none`.

**Security:** Spring Security protects `/promocodepanel` and `/promocode` routes (ROLE_admin). CSRF disabled. Credentials set via `OWNER_USERNAME`/`OWNER_PASSWORD`.

## Required environment variables

| Variable | Description |
|---|---|
| `TELEGRAM_BOT_TOKEN` | Bot API token |
| `TELEGRAM_BOT_NAME` | Bot username |
| `OWNER_PASSWORD` | Admin panel password |
| `LIFE_PAY_LOGIN` / `LIFE_PAY_KEY` | LifePay payment integration |
| `DB_HOST`, `DB_PORT`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | Database (defaults work with docker-compose) |
| `TIMEZONE_API_KEY` | External timezone lookup service |

Optional pricing overrides: `REVIEW_PRICE` (6000), `MENTORING_PRICE` (50000), `CALL_PRICE` (10000).

## CI/CD

- **PR:** `test_and_verify_codestyle.yml` — builds and runs `mvn verify`
- **Push to main:** `deploy.yml` — builds Docker image, pushes to `nekromant322/review-bot` on DockerHub, SSH-deploys to `194.87.96.29` via docker-compose
