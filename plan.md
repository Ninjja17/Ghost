# Shadow Engineer Product Plan

## Product Goal

Shadow Engineer is an evidence-based developer growth platform. It analyzes a developer's work, explains the patterns it finds, shows confidence and missing evidence, and turns the findings into measurable next actions.

The product must not present career outcomes as facts. It should distinguish between:

- Observed evidence: facts collected from connected sources.
- Computed metrics: deterministic scores calculated from evidence.
- AI interpretation: explanations and recommendations generated from those metrics.
- Scenarios: possible career directions, not guaranteed predictions.

## Current Status

### Completed

- React + TypeScript + Vite application scaffold.
- Shadow Engineer dashboard visual system.
- Overview, Evidence Ledger, and Career Trajectory views.
- Developer DNA score display.
- Career Twin comparison display.
- 30-day mission modal.
- Loading, error, responsive, and empty interaction states.
- GitHub public username connection flow.
- GitHub profile and repository fetching.
- Basic repository count, language, stars, and activity calculations.
- Evidence Ledger now derives language, activity, and public-reach signals from the connected GitHub snapshot.
- Developer DNA now uses transparent GitHub-derived heuristics with explicit evidence-gap notes.
- First-run onboarding now collects and persists name, experience level, target role, and growth goal.
- Current MVP controls are wired: mission modal, evidence navigation, notifications, refresh/source gating, and mentor-to-mission handoff.
- GitHub connections are now authenticated and user-owned with persisted GitHub account IDs, lookup, and disconnect endpoints.
- GitHub repository snapshot persistence is implemented with in-memory and PostgreSQL adapters; live ingestion verification is currently rate-limit blocked by GitHub.
- A versioned evidence record schema and deterministic GitHub snapshot extractor now exist with a passing unit test.
- Production build passes with `npm run build`.

### Current Limitations

- GitHub preview data now flows through a backend source endpoint; OAuth/private repository sync is still not implemented.
- Email/password authentication now uses HttpOnly cookies and hashed session records; account deletion, source revocation, and automated authorization tests remain.
- Spring Boot API foundation exists, but production OAuth and analysis APIs are not implemented.
- Report scores are still seeded demo values.
- No private repository access.
- No resume ingestion.
- No Gemini interpretation pipeline.
- Deterministic Mentor Council prototype now exists in the frontend; it is not yet connected to Gemini or backend evidence.
- No automated evaluation of AI output.
- No deployment configuration.

## Completion Definition

The first complete product is done when a new user can:

1. Create an account.
2. Set their name, experience level, and target role.
3. Connect GitHub securely with OAuth.
4. Optionally upload a resume.
5. Wait for an analysis job to complete.
6. View evidence-backed skills and behavior scores.
7. Understand the source behind every major insight.
8. Review career scenarios with confidence and uncertainty.
9. Receive a personalized 30-day action plan.
10. Mark actions complete and return later to see progress.
11. Correct inaccurate evidence or interpretations.
12. Delete their account and connected data.

## Implementation Phases

### Phase 0: Product Foundation

Goal: make the project easy to develop, test, and deploy.

- [ ] Add a complete README with local setup and product scope.
- [ ] Add `.env.example` and document all required variables.
- [ ] Add formatting and linting configuration.
- [ ] Add a reliable `npm run check` command for typecheck, lint, and build.
- [ ] Add a basic test runner for pure data and scoring functions.
- [ ] Add a consistent error and loading-state convention.
- [ ] Add route-level page structure for onboarding and dashboard.

Done when: a fresh clone can install, check, and run locally using documented commands.

### Phase 1: Real Frontend Product Flow

Goal: turn the dashboard prototype into a complete user-facing flow.

- [x] Build welcome and profile setup screens.
- [x] Collect name, experience level, target role, and career goal.
- [x] Build source connection screen for GitHub and future sources.
- [ ] Add onboarding progress and resumable state.
- [x] Add dashboard empty state before analysis exists.
- [ ] Add analysis progress state with meaningful stages.
- [ ] Add report navigation and deep links.
- [ ] Replace remaining static copy with typed view models.
- [ ] Add user correction controls to evidence rows.

Done when: a new user can complete onboarding and reach a believable empty or analyzing dashboard without manually editing code.

### Phase 2: Backend Foundation

Goal: move secrets, user state, and data fetching out of the browser.

Recommended initial architecture: Spring Boot modular monolith, PostgreSQL, and Redis only where caching is useful. Do not start with Kafka or separate deployable services.

- [x] Create Spring Boot project structure.
- [x] Add configuration profiles for local and PostgreSQL environments.
- [x] Add PostgreSQL connection configuration and a first Flyway migration.
- [x] Add health and readiness endpoints.
- [x] Add global API error format.
- [x] Add request validation and pagination conventions.
- [x] Add CORS configuration for the frontend origin.
- [x] Add request IDs for API tracing.
- [x] Add Docker Compose for local PostgreSQL and Redis.

Done when: the backend starts locally, runs migrations, exposes health endpoints, and can persist a test user.

### Phase 3: Authentication and User Profile

Goal: create secure, persistent user accounts.

- [x] Implement email/password authentication.
- [x] Add an expiring opaque session lifecycle.
- [x] Deliver sessions through HttpOnly cookies and store only session hashes.
- [x] Add user profile endpoints.
- [x] Add profile persistence interface with in-memory default and PostgreSQL implementation.
- [x] Store target role and experience level.
- [ ] Add account deletion endpoint.
- [ ] Add source disconnect endpoint.
- [ ] Protect all user-scoped resources.
- [ ] Add authorization tests preventing cross-user access.

Done when: users can sign in, refresh the app, see their own profile, and permanently delete their account data.

### Phase 4: GitHub Integration

Goal: securely collect useful GitHub evidence.

- [ ] Register a GitHub OAuth application.
- [x] Add a backend public-profile preview endpoint for normalized repository signals.
- [x] Persist the connected GitHub account against the authenticated user.
- [x] Add authenticated connection lookup and disconnect endpoints.
- [x] Persist repository snapshot metadata for each connected source.
- [x] Add authenticated repository retrieval endpoint.
- [ ] Implement server-side OAuth callback.
- [ ] Encrypt or securely store refresh/access tokens.
- [ ] Request the smallest practical permission scope.
- [ ] Add repository selection and private-repository consent.
- [ ] Collect repositories, languages, commits, pull requests, releases, topics, README files, and timestamps.
- [ ] Handle GitHub pagination and rate limits.
- [ ] Cache raw responses with source timestamps.
- [ ] Add sync status, retry, and partial-failure states.
- [ ] Add disconnect and token revocation behavior.

Done when: a user can connect GitHub without exposing a token to the browser and can inspect which repositories were analyzed.

### Phase 5: Evidence Engine

Goal: convert source data into traceable, reviewable facts.

- [ ] Define a versioned evidence schema.
- [x] Define a versioned evidence schema.
- [x] Prototype frontend evidence rows from GitHub snapshot data.
- [x] Persist and retrieve extracted evidence records.
- [x] Add deterministic evidence extraction test coverage.
- [ ] Create evidence records with source, reference, observed date, confidence, and extraction method.
- [ ] Detect languages and frameworks.
- [ ] Detect API, database, testing, Docker, CI/CD, cloud, and documentation signals.
- [ ] Detect project completion indicators such as releases, deployments, and documentation.
- [ ] Detect collaboration indicators from pull requests and reviews.
- [ ] Store evidence provenance for every extracted claim.
- [ ] Add deterministic extraction tests with representative fixtures.
- [ ] Mark missing data separately from negative evidence.

Done when: every dashboard claim can be traced to one or more evidence records.

### Phase 6: Scoring and Skill Graph

Goal: calculate transparent metrics before involving AI interpretation.

- [ ] Create a normalized skill taxonomy.
- [ ] Define skill categories and aliases.
- [ ] Calculate skill scores using deterministic rules.
- [ ] Calculate confidence separately from skill strength.
- [ ] Calculate Builder, Finisher, Learner, Architect, and Collaborator scores.
- [x] Prototype GitHub-derived Developer DNA heuristics for the dashboard.
- [ ] Store score formulas and version numbers.
- [ ] Add time-windowed snapshots for progress tracking.
- [ ] Add score explanations and contributing evidence IDs.
- [ ] Add calibration fixtures for high, medium, low, and unknown signals.

Done when: the same evidence produces reproducible scores and the UI can explain how each score was formed.

### Phase 7: AI Interpretation and Mentor Council

Goal: use Gemini to explain evidence, not invent facts.

- [ ] Add a backend Gemini client with timeout and retry policy.
- [ ] Define strict JSON response schemas.
- [ ] Send only summarized evidence and relevant score inputs to the model.
- [ ] Generate a report narrative grounded in evidence IDs.
- [ ] Generate gap explanations and recommendations.
- [x] Define four mentor lenses: Engineer, Architect, Recruiter, and Product.
- [x] Add a deterministic frontend Mentor Council prototype with evidence, recommendations, questions, and synthesis.
- [ ] Add mentor perspectives only after the single synthesis report is reliable.
- [ ] Enforce that every claim includes supporting evidence or an explicit uncertainty label.
- [ ] Add prompt versioning.
- [ ] Store model, prompt version, timestamp, and output status.
- [ ] Add fallback behavior when Gemini is unavailable.

Done when: the AI report never silently fabricates evidence and invalid or ungrounded responses are rejected before display.

### Phase 8: Career Scenarios and Action Planning

Goal: turn analysis into useful, measurable decisions.

- [ ] Define a small initial persona library: Backend Engineer, Product Engineer, and AI Engineer.
- [ ] Define persona requirements and evidence mappings.
- [ ] Calculate fit and confidence separately.
- [ ] Show missing evidence and skill gaps for every scenario.
- [ ] Replace deterministic-looking predictions with clearly labeled scenarios.
- [ ] Generate weekly action plans from verified gaps.
- [ ] Give every task completion criteria.
- [ ] Allow users to accept, edit, skip, and complete tasks.
- [ ] Track progress against the next analysis snapshot.

Done when: every recommendation is actionable, measurable, and tied to a known gap or opportunity.

### Phase 9: Resume and Additional Sources

Goal: improve context beyond public GitHub activity.

- [ ] Add secure resume upload.
- [ ] Extract resume text without storing unnecessary raw data.
- [ ] Let users review extracted skills before saving them.
- [ ] Mark resume-only evidence separately from repository evidence.
- [ ] Add LinkedIn and LeetCode only after the GitHub workflow is stable.
- [ ] Add source conflict resolution when sources disagree.

Done when: users can see which conclusions come from GitHub, resume, or future sources independently.

### Phase 10: Trust, Privacy, and Governance

Goal: make the product safe enough for real user data.

- [ ] Add privacy policy and clear data-use explanation.
- [ ] Add data export.
- [ ] Add source-level deletion.
- [ ] Add full account deletion verification.
- [ ] Add audit trail for analysis jobs and source access.
- [ ] Add rate limits and abuse protection.
- [ ] Add prompt-injection handling for README and repository text.
- [ ] Treat repository content as untrusted input.
- [ ] Prevent user data from being used for model training unless explicitly consented.
- [ ] Add redaction rules for secrets detected in imported content.

Done when: a user can understand, control, export, and delete their data, and untrusted repository text cannot control system behavior.

### Phase 11: Evaluation and Quality

Goal: ensure the product is accurate, useful, and consistent.

- [ ] Create a reviewed calibration dataset.
- [ ] Test evidence extraction against labeled repository examples.
- [ ] Test score calculations against expected outputs.
- [ ] Add LLM-as-judge evaluation for report grounding, usefulness, completeness, and uncertainty handling.
- [ ] Add deterministic validation for required evidence IDs and JSON schemas.
- [ ] Track hallucination, unsupported claims, and user correction rates.
- [ ] Compare model and prompt versions before release.
- [ ] Add manual review for low-confidence or high-impact reports.

Done when: evaluation results are reproducible and no prompt/model change ships without a comparison against the baseline.

### Phase 12: Deployment and Operations

Goal: run the product reliably for real users.

- [ ] Create Docker images for frontend and backend.
- [ ] Configure PostgreSQL backups.
- [ ] Configure environment secrets in the deployment provider.
- [ ] Add staging and production environments.
- [ ] Add CI for check, test, and build.
- [ ] Add deployment health checks.
- [ ] Add error monitoring and performance monitoring.
- [ ] Add background jobs for repository sync and AI analysis.
- [ ] Add job retries and dead-letter handling.
- [ ] Add usage and cost monitoring for Gemini calls.
- [ ] Add basic product analytics with privacy-aware events.

Done when: a staging deployment can process a new user from sign-in through report generation and recover from failed analysis jobs.

## Suggested Build Order

1. Phase 0: foundation.
2. Phase 1: complete frontend flow.
3. Phase 2: backend foundation.
4. Phase 3: authentication.
5. Phase 4: secure GitHub integration.
6. Phase 5: evidence engine.
7. Phase 6: deterministic scoring.
8. Phase 7: AI interpretation.
9. Phase 8: career scenarios and action plans.
10. Phase 10: trust and privacy hardening.
11. Phase 11: evaluation.
12. Phase 12: deployment.
13. Phase 9: additional sources after the core loop is validated.

## Correct Execution Order From Here

The existing frontend and mentor work remains useful as a prototype, but no new presentation or agent features should be added until these gates are complete:

1. **Foundation gate:** README, environment templates, frontend check command, backend test command, and a small deterministic test suite.
2. **Identity gate:** authenticated profile creation, login, logout, session persistence, account ownership, and account deletion.
3. **Source gate:** authenticated GitHub OAuth, source connection persistence, source disconnect, sync status, and rate-limit handling.
4. **Data gate:** persist repositories and raw source snapshots, then define versioned evidence records with provenance.
5. **Analysis gate:** deterministic extraction and scoring from persisted evidence. No seeded report values may remain in production paths.
6. **Report gate:** build the report API from an analysis snapshot and connect the dashboard to that API.
7. **Interpretation gate:** connect Gemini to the report API with structured schemas, evidence IDs, uncertainty, retries, and fallback behavior.
8. **Action gate:** persist career scenarios and editable action plans, then connect mission completion to stored user state.
9. **Trust gate:** add privacy controls, deletion/export, prompt-injection defenses, authorization tests, and LLM-as-judge evaluation.
10. **Release gate:** staging deployment, migrations, backups, monitoring, cost controls, and a complete user-flow test.

**Current correct next step:** finish the data gate. The GitHub connection and repository snapshot prototypes exist, but they need deterministic evidence extraction, repository persistence tests, and a versioned evidence schema before more mentor or career UI work.

**Explicitly paused:** Gemini mentor agents, additional source integrations, broad career prediction, Kafka, and microservice extraction. These remain downstream consumers of the analysis snapshot and should not define the data model.

## First Release Scope

The first public beta should include only:

- Account creation.
- GitHub OAuth.
- Public repository analysis.
- Evidence ledger.
- Five Developer DNA scores.
- Three career scenarios.
- One evidence-backed AI report.
- One editable 30-day action plan.
- Progress snapshots.
- Correction, disconnect, export, and delete controls.

Defer Kafka, LinkedIn, LeetCode, complex multi-agent orchestration, and broad career prediction until this loop is validated with real users.

## Product Success Metrics

- Percentage of users who complete GitHub connection.
- Time from connection to first understandable insight.
- Percentage of report claims with accepted evidence.
- User correction rate per report.
- Action-plan completion rate.
- Return rate after the next analysis snapshot.
- Percentage of users who report that the recommendation changed what they built next.
- AI report groundedness and usefulness evaluation scores.
- Gemini cost per completed report.

## Working Rule

Complete one phase at a time. Before moving forward:

1. Implement the smallest vertical slice.
2. Run typecheck, tests, and build.
3. Manually exercise the user flow.
4. Update this file with what changed and what remains.
5. Do not add a new service until the current module has a clear ownership boundary and a measured need.
