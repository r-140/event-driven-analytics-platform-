create schema if not exists observability;
create table observability.dbt_run_history(run_id uuid primary key,dag_id text not null,started_at timestamptz not null,finished_at timestamptz not null,duration_seconds numeric not null,status text not null,models_total int not null,models_failed int not null,error_message text);
create table observability.dbt_model_run_history(run_id uuid not null,model_name text not null,status text not null,execution_time_seconds numeric not null,executed_at timestamptz not null,error_message text,primary key(run_id,model_name));
create table observability.connector_status_history(observed_at timestamptz not null,connector_name text not null,connector_state text not null,total_tasks int not null,failed_tasks int not null,primary key(observed_at,connector_name));
create table observability.freshness_history(observed_at timestamptz not null,dataset_name text not null,layer text not null,last_record_at timestamptz,age_minutes numeric,sla_minutes int not null,status text not null,row_count bigint not null,previous_row_count bigint,volume_change_pct numeric,primary key(observed_at,dataset_name));
create table observability.anomaly_events(id uuid primary key,detected_at timestamptz not null,anomaly_type text not null,dataset_name text not null,severity text not null,details text not null,resolved_at timestamptz);
create index on observability.dbt_model_run_history(executed_at,status);
create index on observability.freshness_history(observed_at,status);
create index on observability.anomaly_events(detected_at,resolved_at);
