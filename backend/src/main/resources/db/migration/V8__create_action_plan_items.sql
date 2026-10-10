create table if not exists action_plan_items (
    id uuid primary key,
    user_id uuid not null references users(id) on delete cascade,
    week_number integer not null check (week_number between 1 and 52),
    title varchar(200) not null,
    details varchar(1000) not null,
    completion_criteria varchar(1000) not null,
    status varchar(30) not null default 'pending',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists idx_action_plan_items_user_id on action_plan_items (user_id);
