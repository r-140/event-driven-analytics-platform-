from __future__ import annotations
from airflow.sdk import dag, task
from datetime import datetime, timezone
from pathlib import Path
import json, os, subprocess, time, uuid
import psycopg

DB=os.environ.get("ANALYTICS_DATABASE_URL","postgresql://platform:platform@analytics-db:5432/analytics")
PROJECT=Path("/opt/platform/analytics/dbt")
TARGET=Path(os.environ.get("DBT_TARGET_PATH","/tmp/dbt-target"))

@dag(dag_id="dbt_analytics",schedule="*/5 * * * *",start_date=datetime(2026,1,1),catchup=False,is_paused_upon_creation=False,tags=["dbt","analytics"])
def dbt_analytics():
 @task
 def build():
  run_id=uuid.uuid4(); started=datetime.now(timezone.utc); tick=time.monotonic(); error=None
  result=subprocess.run(["dbt","build","--profiles-dir","/opt/airflow/dbt-profile","--target","airflow"],cwd=PROJECT,text=True,capture_output=True)
  finished=datetime.now(timezone.utc); artifact=TARGET/"run_results.json"; rows=[]
  if artifact.exists():
   for item in json.loads(artifact.read_text()).get("results",[]):
    rows.append((run_id,item.get("unique_id","unknown"),item.get("status","unknown"),item.get("execution_time",0),finished,item.get("message")))
  failed=sum(1 for r in rows if r[2] not in {"success","pass","skipped"}); status="success" if result.returncode==0 else "failed"
  if result.returncode: error=(result.stderr or result.stdout)[-4000:]
  with psycopg.connect(DB) as conn:
   conn.execute("insert into observability.dbt_run_history values(%s,%s,%s,%s,%s,%s,%s,%s,%s)",(run_id,"dbt_analytics",started,finished,time.monotonic()-tick,status,len(rows),failed,error))
   conn.executemany("insert into observability.dbt_model_run_history values(%s,%s,%s,%s,%s,%s)",rows)
  if result.returncode: raise RuntimeError(error)
 build()
dbt_analytics()
