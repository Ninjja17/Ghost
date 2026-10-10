create table if not exists github_repositories (
    id uuid primary key,
    connection_id uuid not null references github_connections(id) on delete cascade,
    github_repository_id varchar(80) not null,
    name varchar(200) not null,
    html_url varchar(500) not null,
    language varchar(80),
    stars integer not null default 0,
    forks integer not null default 0,
    size integer not null default 0,
    is_fork boolean not null default false,
    updated_at timestamptz,
    observed_at timestamptz not null default now(),
    unique (connection_id, github_repository_id)
);

create index if not exists idx_github_repositories_connection_id on github_repositories (connection_id);
