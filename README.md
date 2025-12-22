# Media Server

Spring Boot backend for streaming videos with Mega.nz integration.

## Features

- Video streaming with HTTP range requests (seeking support)
- Mega.nz downloads (server-side, no CORS issues)
- PostgreSQL database for movie metadata
- WebSocket for real-time download progress
- Centralized video cache

## API Endpoints

### Movies
- `GET /api/movies` - List all movies
- `GET /api/movies/{id}` - Get movie by ID
- `POST /api/movies` - Create movie
- `PUT /api/movies/{id}` - Update movie
- `DELETE /api/movies/{id}` - Delete movie
- `POST /api/movies/{id}/download` - Start download

### Streaming
- `GET /api/stream/{movieId}` - Stream video
- `GET /api/stream/{movieId}/info` - Get stream info

### Downloads
- `GET /api/downloads` - Get active downloads
- `GET /api/downloads/{movieId}` - Get download progress

### Categories
- `GET /api/categories` - List categories
- `POST /api/categories` - Create category

## Running

```bash
# Start PostgreSQL
docker-compose up -d db

# Run application
./mvnw spring-boot:run

# Or with Docker
docker-compose up
```

## Configuration

Set environment variables:
- `DB_USERNAME` / `DB_PASSWORD` - Database credentials
- `MEDIA_STORAGE_PATH` - Video storage path
- `MEGA_EMAIL` / `MEGA_PASSWORD` - Mega.nz credentials (optional)
