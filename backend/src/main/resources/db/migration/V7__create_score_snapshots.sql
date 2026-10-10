create table if not exists score_snapshots (
    id uuid primary key,
    user_id uuid not null unique references users(id) on delete cascade,
    connection_id uuid not null references github_connections(id) on delete cascade,
    builder_score integer not null check (builder_score between 0 and 100),
    finisher_score integer not null check (finisher_score between 0 and 100),
    learner_score integer not null check (learner_score between 0 and 100),
    architect_score integer not null check (architect_score between 0 and 100),
    collaborator_score integer not null check (collaborator_score between 0 and 100),
    formula_version varchar(40) not null,
    observed_at timestamptz not null
);
