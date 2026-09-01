from __future__ import annotations
from airflow.sdk import dag, task
from datetime import datetime, timezone
import os, uuid, requests, psycopg

DB=os.environ.get("ANALYTICS_DATABASE_URL","postgresql://platform:platform@analytics-db:5432/analytics")
CONNECT=os.environ.get("KAFKA_CONNECT_URL","http://connect:8083")
DATASETS={
 "cdc.customer":("bronze","select max(ingested_at),count(*) from bronze.customer_events",10),
 "cdc.invoice_issued":("bronze","select max(ingested_at),count(*) from bronze.domain_events where event_type='InvoiceIssued'",10),
 "cdc.invoice_adjusted":("bronze","select max(ingested_at),count(*) from bronze.domain_events where event_type='InvoiceAdjusted'",10),
 "cdc.payment":("bronze","select max(ingested_at),count(*) from bronze.domain_events where domain='payment'",10),
 "cdc.identity":("bronze","select max(ingested_at),count(*) from bronze.domain_events where domain='identity'",10),
 "model.customers":("silver","select max(executed_at),count(*) from observability.dbt_model_run_history where model_name like '%.customers' and status in('success','pass')",15),
 "model.invoices":("silver","select max(executed_at),count(*) from observability.dbt_model_run_history where model_name like '%.invoices' and status in('success','pass')",15),
 "model.payments":("silver","select max(executed_at),count(*) from observability.dbt_model_run_history where model_name like '%.payments' and status in('success','pass')",15),
 "model.customer_summary":("gold","select max(executed_at),count(*) from observability.dbt_model_run_history where model_name like '%.customer_summary' and status in('success','pass')",15),
}

def anomaly(conn,kind,dataset,severity,details,active):
 if active:
  conn.execute("insert into observability.anomaly_events values(%s,now(),%s,%s,%s,%s,null) on conflict do nothing",(uuid.uuid4(),kind,dataset,severity,details)) if not conn.execute("select 1 from observability.anomaly_events where anomaly_type=%s and dataset_name=%s and resolved_at is null",(kind,dataset)).fetchone() else None
 else: conn.execute("update observability.anomaly_events set resolved_at=now() where anomaly_type=%s and dataset_name=%s and resolved_at is null",(kind,dataset))

@dag(dag_id="platform_health",schedule="* * * * *",start_date=datetime(2026,1,1),catchup=False,is_paused_upon_creation=False,tags=["observability","cdc","quality"])
def platform_health():
 @task
 def connector_health():
  observed=datetime.now(timezone.utc)
  with psycopg.connect(DB) as conn:
   for name in requests.get(f"{CONNECT}/connectors",timeout=10).json():
    status=requests.get(f"{CONNECT}/connectors/{name}/status",timeout=10).json(); tasks=status.get("tasks",[]); failed=sum(t.get("state")!="RUNNING" for t in tasks); state=status.get("connector",{}).get("state","UNKNOWN")
    conn.execute("insert into observability.connector_status_history values(%s,%s,%s,%s,%s)",(observed,name,state,len(tasks),failed))
    anomaly(conn,"CONNECTOR_FAILURE",name,"critical",f"connector={state}, failed_tasks={failed}",state!="RUNNING" or failed>0)
 @task
 def data_freshness():
  observed=datetime.now(timezone.utc)
  with psycopg.connect(DB) as conn:
   for name,(layer,query,sla) in DATASETS.items():
    last,count=conn.execute(query).fetchone(); previous=conn.execute("select row_count from observability.freshness_history where dataset_name=%s order by observed_at desc limit 1",(name,)).fetchone(); previous=previous[0] if previous else None
    age=(observed-last).total_seconds()/60 if last else None; status="fresh" if age is not None and age<=sla else "stale"; change=((count-previous)*100/previous) if previous else None
    conn.execute("insert into observability.freshness_history values(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)",(observed,name,layer,last,age,sla,status,count,previous,change))
    anomaly(conn,"FRESHNESS_SLA",name,"critical" if layer=="bronze" else "warning",f"age_minutes={age}, sla_minutes={sla}",status=="stale")
    anomaly(conn,"VOLUME_CHANGE",name,"warning",f"row_count={count}, previous={previous}, change_pct={change}",change is not None and abs(change)>50)
 connector_health(); data_freshness()
platform_health()
