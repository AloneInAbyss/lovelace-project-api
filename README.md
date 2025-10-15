# Lovelace Project API

A REST API built with Spring Boot for the Project Lovelace website, featuring JWT authentication, MongoDB for data storage, and Redis for caching.

## Quick Start

### 1. Setup .env file

Copy the `.env.example` file and rename it to `.env.local`.

### 2. Running with Docker Compose

```bash
docker compose -f 'docker-compose.dev.yml' up -d --build
```

The API will be available at `http://localhost:8080`


### Extra: Stopping the Application

By default data from docker containers is stored in persistant volumes. You can delete them with the following command:

```bash
docker compose down -v
```

## Monitoring

Health check and monitoring endpoints are available at:
- `http://localhost:8080/actuator`
- `http://localhost:8080/actuator/health`

