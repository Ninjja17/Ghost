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

## Deploy With Render

The repository includes `render.yaml` and `backend/Dockerfile` for the API.

1. Create a Render Web Service from this repository using the Blueprint configuration.
2. Copy the deployed API URL, for example `https://shadow-engineer-api.onrender.com`.
3. In Vercel, add `VITE_API_URL=https://shadow-engineer-api.onrender.com/api/v1` for Production.
4. Redeploy the Vercel frontend.

The API must allow the exact Vercel origin through `FRONTEND_ORIGIN`. Current frontend origins include `https://ghost-l34h2nlf8-ninjja17s-projects.vercel.app` and `https://ghost-steel.vercel.app`. For cross-site production cookies, use `AUTH_COOKIE_SECURE=true` and `AUTH_COOKIE_SAME_SITE=None`.

## Authentication

Registration and login set an `HttpOnly`, `SameSite=Lax` cookie named `shadow_engineer_session`.
The frontend must send requests with credentials enabled. Raw session tokens are not returned in JSON.
