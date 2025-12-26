# Media Server

Full-stack media streaming application with Angular 18 frontend and Spring Boot 4 backend.

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
├── frontend/          Angular 18 app
│   ├── src/
│   │   ├── app/
│   │   │   ├── features/      Movie list, detail, video player
│   │   │   ├── services/      API, WebSocket
│   │   │   └── store/         NgRx signals stores
│   │   └── environments/
│   └── Dockerfile
├── backend/           Spring Boot 4 app
│   ├── src/main/java/com/mediaserver/
│   │   ├── application/       Use cases and orchestration
│   │   ├── config/            Spring configuration
│   │   ├── domain/            Domain models
│   │   ├── dto/               Shared DTOs
│   │   ├── event/             Domain and progress events
│   │   ├── exception/         Exception types
│   │   ├── infrastructure/
│   │   │   ├── persistence/   JPA adapters
│   │   │   └── rest/          REST controllers, mappers
│   │   └── service/           Application services
│   └── Dockerfile
└── docker-compose.yml
```

## Features

- Video streaming with HTTP range requests
- Mega.nz downloads
- Real-time download progress via WebSocket
- PostgreSQL database for movie metadata
- Centralized video cache

## Quick Start

```bash
docker-compose up
```

Access the app at http://localhost.

## Development

### Backend

```bash
cd backend
docker-compose up -d db
./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm start
```

Access the app at http://localhost:4200.

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
