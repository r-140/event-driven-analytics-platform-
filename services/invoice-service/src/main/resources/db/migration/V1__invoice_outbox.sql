create table invoices(id uuid primary key,customer_id uuid not null,amount numeric(19,2) not null,currency varchar(3) not null,status varchar(30) not null,issued_at timestamptz not null);
create table outbox_events(id uuid primary key,aggregate_type varchar(100) not null,aggregate_id uuid not null,event_type varchar(100) not null,payload jsonb not null,occurred_at timestamptz not null);
