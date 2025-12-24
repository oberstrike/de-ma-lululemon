# Media Server

Full-stack media streaming application with Angular frontend and Spring Boot backend.

## Architecture

```
┌─────────────────┐         ┌─────────────────┐         ┌─────────────┐
│  Angular App    │  HTTP   │  Spring Boot    │  SDK    │  Mega.nz    │
│  (Frontend)     │ ◄─────► │  (Backend)      │ ◄─────► │  Cloud      │
└─────────────────┘         └────────┬────────┘         └─────────────┘
                                     │
                                     ▼
                            ┌─────────────────┐
                            │  PostgreSQL     │
                            │  + File Storage │
                            └─────────────────┘
```

## Project Structure

```
├── frontend/          # Angular 18 app
│   ├── src/
│   │   ├── app/
│   │   │   ├── features/      # Movie list, detail, video player
│   │   │   ├── services/      # API, WebSocket
│   │   │   └── store/         # NgRx signals stores
│   │   └── environments/
│   └── Dockerfile
├── backend/           # Spring Boot 3 app
│   ├── src/main/java/com/mediaserver/
│   │   ├── controller/        # REST endpoints
│   │   ├── service/           # Business logic
│   │   ├── entity/            # JPA entities
│   │   └── repository/        # Data access
│   └── Dockerfile
└── docker-compose.yml
```

## Features

- Video streaming with HTTP range requests (seeking support)
- Mega.nz downloads (server-side, no CORS issues)
- Real-time download progress via WebSocket
- PostgreSQL database for movie metadata
- Centralized video cache

## Quick Start

```bash
# Run everything with Docker
docker-compose up

# Access the app at http://localhost
```

## Development

### Backend

```bash
cd backend

# Start PostgreSQL
docker-compose up -d db

# Run Spring Boot
./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend

# Install dependencies
npm install

# Start dev server (proxies to backend at :8080)
npm start

# Access at http://localhost:4200
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
- `MEGA_EMAIL` / `MEGA_PASSWORD` - Mega.nz credentials (optional)
