# Media Server - Development Guidelines

## Project Overview

A Spring Boot 4.0 media server with Mega.nz integration for video streaming and management.

**Tech Stack:**
- Java 21, Spring Boot 4.0, PostgreSQL
- Liquibase for database migrations
- Testcontainers for integration testing
- Frontend: React/TypeScript

## Architecture

This project follows **Clean Architecture** (Hexagonal Architecture) with strict layer separation:

```
backend/src/main/java/com/mediaserver/
├── domain/           # Core business logic (innermost layer)
│   ├── model/        # Domain entities (Movie, Category, DownloadTask)
│   ├── repository/   # Repository interfaces (ports)
│   └── exception/    # Domain-specific exceptions
│
├── application/      # Use cases and application services
│   ├── usecase/      # Use case interfaces (one per operation)
│   ├── service/      # Use case implementations
│   ├── command/      # Command objects for mutations
│   └── port/         # Input/output ports
│
├── infrastructure/   # External adapters (outermost layer)
│   ├── persistence/  # JPA entities, repositories, mappers
│   │   ├── entity/   # JPA entities (*JpaEntity)
│   │   ├── repository/  # Spring Data JPA repositories
│   │   ├── adapter/  # Repository interface implementations
│   │   └── mapper/   # Entity <-> Domain mappers
│   └── rest/         # REST API layer
│       ├── controller/  # REST controllers
│       ├── dto/      # Request/Response DTOs
│       └── mapper/   # DTO <-> Domain mappers
```

### Layer Dependencies

- **Domain** depends on nothing (pure Java)
- **Application** depends only on Domain
- **Infrastructure** depends on Application and Domain

These rules are enforced by ArchUnit tests in `ArchitectureTest.java`.

## Code Style

### Formatting

Code is formatted using **Google Java Format** (AOSP variant with 4-space indentation) via Spotless:

```bash
# Check formatting
mvn spotless:check

# Apply formatting
mvn spotless:apply
```

### Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| DTOs | Suffix with `DTO` (uppercase) | `MovieResponseDTO`, `CacheStatsDTO` |
| JPA Entities | Suffix with `JpaEntity` | `MovieJpaEntity`, `CategoryJpaEntity` |
| Use Cases | Verb + noun pattern | `GetMovieUseCase`, `CreateCategoryUseCase` |
| Repositories | Domain interface in `domain/repository/` | `MovieRepository` |
| Controllers | Suffix with `Controller` | `MovieController` |
| Mappers | Suffix with `Mapper` | `MovieRestMapper`, `MoviePersistenceMapper` |

### Static Analysis

**SpotBugs** with **Find Security Bugs** plugin runs on all code:

```bash
mvn compile spotbugs:check
```

Exclusions are configured in `spotbugs-exclude.xml` for documented false positives.

## Testing

### Test Structure

```
backend/src/test/java/com/mediaserver/
├── architecture/     # ArchUnit architecture tests
├── domain/           # Domain model unit tests
├── application/      # Use case unit tests
├── infrastructure/   # Controller and repository tests
├── integration/      # Integration tests with Testcontainers
└── config/           # Test configurations
```

### Running Tests

```bash
# All tests
mvn test

# Integration tests require Docker (skipped if unavailable)
# Uses Testcontainers with PostgreSQL 15
```

### Test Guidelines

1. Unit tests use Mockito for mocking dependencies
2. Integration tests use `@Testcontainers` with PostgreSQL
3. Controller tests use `@WebMvcTest` with mocked services
4. Architecture tests validate Clean Architecture rules

## Database

### Migrations

Database schema is managed with **Liquibase**:

- Master file: `src/main/resources/db/changelog-master.yaml`
- Changesets: `src/main/resources/db/changelog/001-initial-schema.yaml`

When adding new columns/tables:
1. Create a new changeset with incremental ID prefix (e.g., `002-`)
2. Include in `changelog-master.yaml`
3. Never modify already-deployed changesets

### Tables

- `movies` - Movie metadata, file paths, cache status
- `categories` - Movie categories with Mega paths
- `download_tasks` - Download queue and progress tracking

## Common Commands

```bash
# Build
mvn clean package

# Run with Testcontainers (local dev)
mvn spring-boot:test-run

# Format code
mvn spotless:apply

# Static analysis
mvn compile spotbugs:check

# Run all checks
mvn verify
```

## CI/CD

GitHub Actions pipeline (`.github/workflows/ci.yml`) runs:
1. Spotless formatting check
2. SpotBugs static analysis
3. Unit and integration tests
4. Docker image build
