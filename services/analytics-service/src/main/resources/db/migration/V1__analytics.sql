create schema if not exists bronze;
create schema if not exists silver;
create schema if not exists gold;
create table bronze.customer_events (event_id uuid primary key, aggregate_id uuid not null, event_type text not null, payload jsonb not null, occurred_at timestamptz not null, ingested_at timestamptz not null default now());
create table silver.customers (customer_id uuid primary key, email text not null, full_name text not null, country_code varchar(2) not null, created_at timestamptz not null);
create table gold.customer_summary (country_code varchar(2) primary key, customer_count bigint not null, refreshed_at timestamptz not null);
