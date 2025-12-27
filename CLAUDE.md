# Media Server - Development Guidelines

## Project Overview

A Spring Boot 4.0 media server with Mega.nz integration for video streaming and management.

**Tech Stack:**
- Java 21, Spring Boot 4.0, PostgreSQL
- Gradle (Kotlin DSL) for build automation
- Liquibase for database migrations
- Testcontainers for integration testing
- Frontend: Angular 21, TypeScript, NgRx Signal Store
- E2E: Playwright with hybrid testing (mocked + real backend)
- Monorepo: Nx with @nx/gradle plugin

## Monorepo Structure

```
de-ma-lululemon/
├── apps/
│   ├── frontend/              # Angular 21 application
│   │   ├── src/
│   │   ├── project.json       # Nx project config
│   │   └── package.json
│   ├── backend/               # Spring Boot 4.0 API
│   │   ├── src/main/java/
│   │   ├── build.gradle.kts   # Gradle build file
│   │   └── project.json       # Nx project config
│   └── e2e/                   # Playwright E2E tests
│       ├── tests/
│       │   ├── smoke/         # Mocked backend tests (@smoke)
│       │   └── critical/      # Real backend tests (@critical)
│       ├── fixtures/          # Test fixtures
│       ├── pages/             # Page Object Models
│       └── playwright.config.ts
├── docker/
│   └── compose.ci.yml         # CI Docker Compose
├── gradle/
│   └── wrapper/               # Gradle wrapper
├── nx.json                    # Nx workspace config
├── package.json               # Root dependencies
├── settings.gradle.kts        # Gradle settings
├── gradlew, gradlew.bat       # Gradle wrapper scripts
└── tsconfig.base.json         # Base TypeScript config
```

## Backend Architecture

This project follows **Clean Architecture** (Hexagonal Architecture) with strict layer separation:

```
apps/backend/src/main/java/com/mediaserver/
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
npx nx spotlessCheck backend

# Apply formatting
npx nx spotlessApply backend
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
npx nx spotbugsMain backend
```

Exclusions are configured in `spotbugs-exclude.xml` for documented false positives.

## Testing

### Test Structure

```
apps/backend/src/test/java/com/mediaserver/
├── architecture/     # ArchUnit architecture tests
├── domain/           # Domain model unit tests
├── application/      # Use case unit tests
├── infrastructure/   # Controller and repository tests
├── integration/      # Integration tests with Testcontainers
└── config/           # Test configurations
```

### Running Tests

```bash
# Backend tests
npx nx test backend

# Frontend tests
npx nx test frontend

# E2E smoke tests (mocked backend)
npx nx e2e e2e -- --grep "@smoke"

# E2E critical tests (real backend)
USE_REAL_BACKEND=true npx nx e2e e2e -- --grep "@critical"
```

### Test Guidelines

1. Unit tests use Mockito for mocking dependencies
2. Integration tests use `@Testcontainers` with PostgreSQL
3. Controller tests use `@WebMvcTest` with mocked services
4. Architecture tests validate Clean Architecture rules
5. E2E smoke tests use mocked API responses for speed
6. E2E critical tests use real backend via Testcontainers

## E2E Testing

### Hybrid Testing Strategy

- **@smoke tests** - Run with mocked backend (fast, CI-friendly)
- **@critical tests** - Run with real backend via Testcontainers

### Page Object Model

Page objects are in `apps/e2e/pages/`:
- `MovieListPage` - Movie list/grid operations
- `MovieDetailPage` - Movie detail view
- `VideoPlayerPage` - Video player controls

### Running E2E Tests

```bash
# Smoke tests (mocked)
npx nx e2e e2e -- --grep "@smoke"

# Critical tests (with Docker Compose)
docker compose -f docker/compose.ci.yml up -d
USE_REAL_BACKEND=true npx nx e2e e2e -- --grep "@critical"
docker compose -f docker/compose.ci.yml down
```

## Database

### Migrations

Database schema is managed with **Liquibase**:

- Master file: `apps/backend/src/main/resources/db/changelog-master.yaml`
- Changesets: `apps/backend/src/main/resources/db/changelog/001-initial-schema.yaml`

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
# Nx Commands (recommended)
npx nx build backend          # Build backend
npx nx build frontend         # Build frontend
npx nx serve frontend         # Start frontend dev server
npx nx serve backend          # Start backend (bootRun)
npx nx test backend           # Run backend tests
npx nx test frontend          # Run frontend tests
npx nx e2e e2e                # Run all E2E tests

# Gradle Commands (alternative)
./gradlew :apps:backend:build         # Build backend
./gradlew :apps:backend:test          # Run tests
./gradlew :apps:backend:spotlessApply # Format code
./gradlew :apps:backend:spotbugsMain  # Static analysis

# Docker
docker compose -f docker/compose.ci.yml up -d   # Start services
docker compose -f docker/compose.ci.yml down    # Stop services
```

## CI/CD

GitHub Actions pipeline (`.github/workflows/ci.yml`) runs:
1. **Backend Lint** - Spotless formatting + SpotBugs analysis
2. **Backend Test** - Unit and integration tests
3. **Frontend Lint** - Prettier, ESLint, Stylelint
4. **Frontend Test** - Unit tests with coverage
5. **E2E Smoke** - Mocked backend Playwright tests
6. **E2E Critical** - Real backend Playwright tests (sharded)
7. **Docker Build** - Build container images

## Frontend

### Structure

```
apps/frontend/src/app/
├── features/         # Feature components (lazy-loaded)
├── services/         # API and WebSocket services
├── store/            # NgRx Signal Stores
└── app.routes.ts     # Route definitions
```

### NgRx Signal Store Guidelines

Stores are located in `apps/frontend/src/app/store/` and follow these patterns:

#### State Management

```typescript
export const ExampleStore = signalStore(
  { providedIn: 'root' },

  // Use withEntities for collections
  withEntities<Entity>(),

  // State for non-entity data
  withState<ExampleState>({
    loading: false,
    error: null,
  }),

  // Computed signals for derived state
  withComputed((state) => ({
    isLoading: computed(() => state.loading()),
    hasError: computed(() => state.error() !== null),
  })),

  // Methods for state mutations
  withMethods((store) => ({
    // ...methods
  })),

  // Auto-initialize with lifecycle hooks
  withHooks({
    onInit(store) {
      void store.load();
    },
  })
);
```

#### Async Methods

Use `async/await` with `firstValueFrom` instead of `rxMethod`:

```typescript
// Preferred
async loadData(): Promise<void> {
  patchState(store, { loading: true, error: null });
  try {
    const data = await firstValueFrom(api.getData());
    patchState(store, setAllEntities(data), { loading: false });
  } catch (err) {
    patchState(store, {
      error: err instanceof Error ? err.message : 'Failed to load',
      loading: false,
    });
  }
}
```

#### Entity Operations

Use `@ngrx/signals/entities` helpers:

```typescript
import { addEntity, removeEntity, setAllEntities, updateEntity, withEntities } from '@ngrx/signals/entities';

// Set all entities
patchState(store, setAllEntities(items));

// Add single entity
patchState(store, addEntity(item));

// Update entity
patchState(store, updateEntity({ id: itemId, changes: { status: 'DONE' } }));

// Remove entity
patchState(store, removeEntity(itemId));
```

#### Computed Signals

Always provide computed signals for filtered/derived state:

```typescript
withComputed((state) => ({
  // Alias for entities()
  items: computed(() => state.entities()),

  // Filtered views
  activeItems: computed(() => state.entities().filter(i => i.active)),

  // Loading/error helpers
  isLoading: computed(() => state.loading()),
  hasError: computed(() => state.error() !== null),
}))
```

#### Method Return Types

Always add explicit return types:

```typescript
withMethods((store) => ({
  async load(): Promise<void> { ... },
  setFilter(filter: string): void { ... },
  clearError(): void { ... },
}))
```

### Frontend Commands

```bash
# Using Nx (from root)
npx nx serve frontend          # Start dev server
npx nx test frontend           # Run tests
npx nx test frontend --coverage # Tests with coverage
npx nx lint frontend           # ESLint
npx nx build frontend          # Build

# From apps/frontend directory
npm start              # Start dev server
npm test               # Run tests
npm run test:ci        # Run tests with coverage
npm run lint           # ESLint
npm run lint:styles    # Stylelint for SCSS
npm run format         # Prettier format
npm run check:all      # All checks
npm run build          # Development build
npm run build:prod     # Production build
```

### Pre-commit Hooks

Husky runs lint-staged on commit:
- Prettier formats `*.{ts,html,json,scss,css,md}`
- ESLint fixes `*.ts`
- Stylelint fixes `*.scss`
