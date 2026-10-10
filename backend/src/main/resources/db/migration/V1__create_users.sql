create table if not exists users (
    id uuid primary key,
    name varchar(120) not null,
    email varchar(320),
    target_role varchar(120),
    experience_level varchar(40),
    growth_goal varchar(500),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists idx_users_email on users (email);
