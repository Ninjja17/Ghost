create table if not exists evidence_records (
    id uuid primary key,
    user_id uuid not null references users(id) on delete cascade,
    connection_id uuid not null references github_connections(id) on delete cascade,
    evidence_type varchar(80) not null,
    claim varchar(240) not null,
    value varchar(500) not null,
    confidence integer not null check (confidence between 0 and 100),
    extractor_version varchar(40) not null,
    observed_at timestamptz not null
);

create index if not exists idx_evidence_records_user_id on evidence_records (user_id);
create index if not exists idx_evidence_records_connection_id on evidence_records (connection_id);
