# Media Server - Development Guidelines

## Project Overview

A Spring Boot 4.0 media server with Mega.nz integration for video streaming and management.

**Tech Stack:**
- Java 21, Spring Boot 4.0, PostgreSQL
- Liquibase for database migrations
- Testcontainers for integration testing
- Frontend: Angular 21, TypeScript, NgRx Signal Store

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

## Frontend

### Structure

```
frontend/src/app/
├── features/         # Feature components (lazy-loaded)
├── services/         # API and WebSocket services
├── store/            # NgRx Signal Stores
└── app.routes.ts     # Route definitions
```

### NgRx Signal Store Guidelines

Stores are located in `frontend/src/app/store/` and follow these patterns:

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
// ✅ Preferred
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

// ❌ Avoid: rxMethod with RxJS pipes
const loadData = rxMethod<void>(
  pipe(switchMap(() => api.getData().pipe(tapResponse({...}))))
);
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
cd frontend

# Development
npm start              # Start dev server

# Testing
npm test               # Run tests
npm run test:ci        # Run tests with coverage

# Linting & Formatting
npm run lint           # ESLint
npm run lint:styles    # Stylelint for SCSS
npm run format         # Prettier format
npm run check:all      # All checks

# Build
npm run build          # Development build
npm run build:prod     # Production build
```

### Pre-commit Hooks

Husky runs lint-staged on commit:
- Prettier formats `*.{ts,html,json,scss,css,md}`
- ESLint fixes `*.ts`
- Stylelint fixes `*.scss`
