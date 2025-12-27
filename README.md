# Media Server

Full-stack media streaming application with Angular 21 frontend and Spring Boot 4 backend, built as an Nx monorepo.

## Architecture

```
┌────────────────────┐  HTTP/WebSocket  ┌─────────────────┐         ┌─────────────┐
│  Angular App       │ ◄──────────────► │  Spring Boot    │  SDK    │  Mega.nz    │
│  (Frontend)        │                  │  (Backend)      │ ◄─────► │  Cloud      │
└────────────────────┘                  └────────┬────────┘         └─────────────┘
                                                 │
                                                 ▼
                                        ┌─────────────────┐
                                        │  PostgreSQL     │
                                        │  + File Storage │
                                        └─────────────────┘
```

## Project Structure

```
├── apps/
│   ├── frontend/          Angular 21 app
│   │   ├── src/app/
│   │   │   ├── features/      Movie list, detail, video player
│   │   │   ├── services/      API, WebSocket
│   │   │   └── store/         NgRx Signal Stores
│   │   ├── project.json       Nx project config
│   │   └── Dockerfile
│   ├── backend/           Spring Boot 4 app (Gradle)
│   │   ├── src/main/java/com/mediaserver/
│   │   │   ├── application/       Use cases and orchestration
│   │   │   ├── domain/            Domain models
│   │   │   └── infrastructure/    REST controllers, persistence
│   │   ├── build.gradle.kts   Gradle build file
│   │   ├── project.json       Nx project config
│   │   └── Dockerfile
│   └── e2e/               Playwright E2E tests
│       ├── tests/
│       │   ├── smoke/         Mocked backend tests
│       │   └── critical/      Real backend tests
│       ├── fixtures/          Test fixtures
│       └── pages/             Page Object Models
├── docker/
│   └── compose.ci.yml     CI Docker Compose
├── nx.json                Nx workspace config
├── settings.gradle.kts    Gradle settings
├── gradlew, gradlew.bat   Gradle wrapper
└── package.json           Root dependencies
```

## Features

- Video streaming with HTTP range requests
- Mega.nz downloads
- Real-time download progress via WebSocket
- PostgreSQL database for movie metadata
- Centralized video cache
- Hybrid E2E testing (mocked + real backend)

## Quick Start

```bash
# Install dependencies
npm install

# Start frontend dev server
npx nx serve frontend

# Start backend
npx nx serve backend
```

Access the app at http://localhost:4200.

## Development

### Backend

```bash
# Run backend
npx nx serve backend

# Build
npx nx build backend

# Run tests
npx nx test backend

# Format code
npx nx spotlessApply backend

# Static analysis
npx nx spotbugsMain backend
```

### Frontend

```bash
# Run frontend
npx nx serve frontend

# Build
npx nx build frontend

# Run tests
npx nx test frontend

# Lint
npx nx lint frontend
```

### E2E Tests

```bash
# Smoke tests (mocked backend)
npx nx e2e e2e -- --grep "@smoke"

# Critical tests (real backend)
docker compose -f docker/compose.ci.yml up -d
USE_REAL_BACKEND=true npx nx e2e e2e -- --grep "@critical"
docker compose -f docker/compose.ci.yml down
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/movies | List all movies |
| POST | /api/movies | Create movie |
| GET | /api/movies/{id} | Get movie details |
| DELETE | /api/movies/{id} | Delete movie |
| POST | /api/movies/{id}/download | Start Mega download |
| GET | /api/stream/{id} | Stream video |
| GET | /api/downloads | Active downloads |
| GET | /api/categories | List categories |

## Configuration

Environment variables:
- `DB_USERNAME` / `DB_PASSWORD` - Database credentials
- `MEDIA_STORAGE_PATH` - Video storage path
- `MEGA_EMAIL` / `MEGA_PASSWORD` - Mega.nz credentials

## CI/CD

GitHub Actions runs:
1. Backend lint (Spotless + SpotBugs)
2. Backend tests
3. Frontend lint (Prettier, ESLint, Stylelint)
4. Frontend tests
5. E2E smoke tests (mocked)
6. E2E critical tests (real backend, sharded)
7. Docker image build
