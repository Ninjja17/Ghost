# Shadow Engineer API

Spring Boot backend foundation for Shadow Engineer.

## Requirements

- Java 17 or newer
- Maven 3.9+
- Docker Desktop for PostgreSQL and Redis profile work

## Run Without Database

The default profile starts the API without a database so health and web contracts can be developed independently.

```powershell
mvn spring-boot:run
```

- `GET http://localhost:8080/api/v1/health`
- `GET http://localhost:8080/api/v1/ready`
- `GET http://localhost:8080/actuator/health`

## Run With PostgreSQL

Start local infrastructure:

```powershell
docker compose up -d
```

Start Spring Boot with migrations enabled:

```powershell
$env:SPRING_PROFILES_ACTIVE = "postgres"
mvn spring-boot:run
```

The PostgreSQL profile runs Flyway migrations from `src/main/resources/db/migration`.

## Configuration

| Variable | Default | Purpose |
| --- | --- | --- |
| `PORT` | `8080` | HTTP port |
| `FRONTEND_ORIGIN` | `http://localhost:5173` | Allowed browser origin |
| `DATABASE_URL` | local PostgreSQL URL | JDBC connection |
| `DATABASE_USERNAME` | `shadow` | Database user |
| `DATABASE_PASSWORD` | `shadow` | Database password |
| `AUTH_COOKIE_SECURE` | `false` | Set `true` when serving over HTTPS |

Do not commit credentials or local environment files.

## Authentication

Registration and login set an `HttpOnly`, `SameSite=Lax` cookie named `shadow_engineer_session`.
The frontend must send requests with credentials enabled. Raw session tokens are not returned in JSON.
