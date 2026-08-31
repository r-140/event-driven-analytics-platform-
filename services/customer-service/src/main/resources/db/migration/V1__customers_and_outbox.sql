create table customers (id uuid primary key, email varchar(320) not null unique, full_name varchar(200) not null, country_code varchar(2) not null, created_at timestamptz not null, updated_at timestamptz not null);
create table outbox_events (id uuid primary key, aggregate_type varchar(100) not null, aggregate_id uuid not null, event_type varchar(100) not null, payload jsonb not null, occurred_at timestamptz not null);
alter table customers replica identity full;
alter table outbox_events replica identity full;
