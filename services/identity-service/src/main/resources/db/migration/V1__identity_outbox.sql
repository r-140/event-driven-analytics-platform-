create table identities(id uuid primary key,customer_id uuid not null,login varchar(320) not null unique,status varchar(30) not null,registered_at timestamptz not null);
create table outbox_events(id uuid primary key,aggregate_type varchar(100) not null,aggregate_id uuid not null,event_type varchar(100) not null,payload jsonb not null,occurred_at timestamptz not null);
