# Shadow Engineer

Shadow Engineer is an evidence-based developer growth platform. The current local MVP includes onboarding, authentication, GitHub source analysis, repository snapshots, evidence extraction, deterministic scoring, recommendations, and mentor interpretation.

## Local Development

Requirements:

- Node.js and npm
- Java 17+
- Maven 3.9+

Start both applications with PowerShell:

```powershell
.\start-local.ps1
```

Or start them separately:

```powershell
# Terminal 1
cd backend
Remove-Item Env:SPRING_PROFILES_ACTIVE -ErrorAction SilentlyContinue
mvn spring-boot:run
```

```powershell
# Terminal 2
npm install
npm run dev
```

Open `http://localhost:5173`.

The default backend profile uses in-memory repositories, so PostgreSQL is not required for local UI testing. Data resets when the backend restarts.

## Local Endpoints

- Frontend: `http://localhost:5173`
- API health: `http://localhost:8080/api/v1/health`
- API readiness: `http://localhost:8080/api/v1/ready`
- Unified report: `GET /api/v1/report`
- Evidence: `GET /api/v1/analysis/evidence`
- Scores: `GET /api/v1/analysis/scores`
- Interpretation: `GET /api/v1/report/interpretation`

## Checks

```powershell
npm run build
cd backend
mvn test
```

## PostgreSQL Mode

Start local infrastructure:

```powershell
cd backend
docker compose up -d postgres redis
$env:SPRING_PROFILES_ACTIVE = "postgres"
mvn spring-boot:run
```

## Product Plan

See [plan.md](plan.md) for the dependency-gated roadmap and remaining production work.
