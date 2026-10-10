create table if not exists github_connections (
    id uuid primary key,
    user_id uuid not null unique references users(id) on delete cascade,
    github_user_id bigint not null,
    github_username varchar(39) not null,
    connected_at timestamptz not null default now(),
    last_synced_at timestamptz not null default now()
);

create unique index if not exists idx_github_connections_user_id on github_connections (user_id);
