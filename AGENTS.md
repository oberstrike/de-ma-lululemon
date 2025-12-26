# Media Server - Agent Guidelines

## Project Overview

Spring Boot 4.0 media server with Mega.nz integration for video streaming and management.

**Tech Stack**
- Java 21, Spring Boot 4.0, PostgreSQL
- Liquibase for database migrations
- Testcontainers for integration testing
- Frontend: Angular 21, TypeScript, NgRx Signal Store

## Architecture

Clean Architecture with strict layer separation:

```
backend/src/main/java/com/mediaserver/
├── domain/
│   ├── model/
│   ├── repository/
│   └── exception/
├── application/
│   ├── usecase/
│   ├── service/
│   ├── command/
│   └── port/
└── infrastructure/
    ├── persistence/
    │   ├── entity/
    │   ├── repository/
    │   ├── adapter/
    │   └── mapper/
    └── rest/
        ├── controller/
        ├── dto/
        └── mapper/
```

Layer dependencies:
- Domain depends on nothing
- Application depends only on Domain
- Infrastructure depends on Application and Domain

ArchUnit tests enforce these rules in `ArchitectureTest.java`.

## Code Style

### Formatting

Google Java Format (AOSP variant, 4-space indentation) via Spotless:

```bash
mvn spotless:check
mvn spotless:apply
```

### Naming Conventions

- DTOs: suffix `DTO`
- JPA Entities: suffix `JpaEntity`
- Use Cases: verb + noun
- Repositories: domain interface in `domain/repository/`
- Controllers: suffix `Controller`
- Mappers: suffix `Mapper`

### Static Analysis

SpotBugs with Find Security Bugs:

```bash
mvn compile spotbugs:check
```

Exclusions are in `spotbugs-exclude.xml`.

## Testing

Structure:

```
backend/src/test/java/com/mediaserver/
├── architecture/
├── domain/
├── application/
├── infrastructure/
├── integration/
└── config/
```

Guidelines:
1. Unit tests use Mockito
2. Integration tests use `@Testcontainers` with PostgreSQL
3. Controller tests use `@WebMvcTest` with mocked services
4. Architecture tests validate Clean Architecture rules

## Database

Liquibase migrations:
- `src/main/resources/db/changelog-master.yaml`
- `src/main/resources/db/changelog/001-initial-schema.yaml`

When adding schema changes:
1. Create a new changeset with incremental ID prefix
2. Include it in `changelog-master.yaml`
3. Do not modify deployed changesets

Tables:
- `movies`
- `categories`
- `download_tasks`

## Common Commands

```bash
mvn clean package
mvn spring-boot:test-run
mvn spotless:apply
mvn compile spotbugs:check
mvn verify
```

## CI/CD

GitHub Actions runs:
1. Spotless check
2. SpotBugs
3. Unit and integration tests
4. Docker image build

## Frontend

Structure:

```
frontend/src/app/
├── features/
├── services/
├── store/
└── app.routes.ts
```

NgRx Signal Store guidelines:
- use `withEntities`, `withState`, `withComputed`, `withMethods`, `withHooks`
- async methods use `async/await` with `firstValueFrom`
- use `@ngrx/signals/entities` helpers for entity ops
- expose computed signals for derived state
- add explicit method return types

Frontend commands:

```bash
npm start
npm test
npm run test:ci
npm run lint
npm run lint:styles
npm run format
npm run check:all
npm run build
npm run build:prod
```

Pre-commit hooks:
- Prettier formats `*.{ts,html,json,scss,css,md}`
- ESLint fixes `*.ts`
- Stylelint fixes `*.scss`
