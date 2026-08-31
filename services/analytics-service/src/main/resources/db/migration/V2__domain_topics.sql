create table bronze.domain_events(event_id uuid primary key,domain varchar(30) not null,event_type varchar(100) not null,payload jsonb not null,occurred_at timestamptz not null,ingested_at timestamptz not null default now());
create table silver.invoices(invoice_id uuid primary key,customer_id uuid not null,amount numeric(19,2) not null,currency varchar(3) not null,status varchar(30) not null,issued_at timestamptz not null);
create table silver.payments(payment_id uuid primary key,invoice_id uuid not null,amount numeric(19,2) not null,currency varchar(3) not null,status varchar(30) not null,received_at timestamptz not null);
create table silver.identities(identity_id uuid primary key,customer_id uuid not null,login text not null,status varchar(30) not null,registered_at timestamptz not null);
create table gold.payment_summary(currency varchar(3) primary key,payment_count bigint not null,total_amount numeric(19,2) not null,refreshed_at timestamptz not null);
