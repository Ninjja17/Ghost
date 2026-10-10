alter table users
    add column if not exists password_hash varchar(255);

create unique index if not exists idx_users_email_unique on users (lower(email)) where email is not null;
