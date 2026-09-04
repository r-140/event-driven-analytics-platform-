# Data-platform observability

The demo separates instantaneous infrastructure telemetry from historical pipeline metadata.

## Components

| Component | Purpose | URL |
|---|---|---|
| Airflow 3.3.1 | Orchestrates dbt and platform-health checks | `http://localhost:8080` |
| Prometheus | Stores Kafka and Spring metrics | `http://localhost:9090` |
| Kafka exporter | Exposes broker, topic, offset and consumer-group metrics | `http://localhost:9308/metrics` |
| Grafana | Visualizes infrastructure, orchestration, CDC and data quality | `http://localhost:3000` |

For the initial Airflow standalone credentials, inspect `docker compose logs airflow`.

## Dashboards

| Dashboard | Important signals |
|---|---|
| Kafka Overview | Broker availability, partitions, leader loss, throughput and consumer lag |
| CDC Overview | Connector/task state and stale CDC datasets |
| Airflow Overview | DAG error rate, task median/p95, retries, duration and recent failures |
| dbt & Airflow Operations | dbt run median/p95, error rate, duration and models ranked by seven-day failures |
| Freshness & Anomaly Detection | SLA breaches, last record time, volume shifts and unresolved anomalies |
| Semantic Query Service | Query rate, error rate, p95 latency, returned rows and model-level traffic |

## Detection rules

The `platform_health` DAG runs every minute. It polls every Kafka Connect connector and checks freshness for each CDC source stream and selected dbt models. Default demo SLAs are 10 minutes for CDC and 15 minutes for models.

## Why a dashboard can show `No data`

The dashboards are intentionally driven by different pipelines:

| Dashboard | Starts showing data after |
|---|---|
| Kafka | Kafka exporter is running and Prometheus has completed a scrape |
| CDC | Connectors are registered and `platform_health` has completed |
| Freshness & Anomaly Detection | Analytics migrations have run and `platform_health` has completed |
| dbt & Airflow Operations | At least one `dbt_analytics` run has completed |
| Semantic Query Service | At least one report has been executed in the analytics portal |
| Airflow Overview | Airflow has created DAG/task-run metadata |

`No data` is different from a valid zero. It normally means that the underlying metric series or SQL history row does not exist yet. After startup, use this sequence:

```bash
./bin/dev health
./bin/dev register-cdc
./bin/dev generate 100
docker compose exec airflow airflow dags trigger dbt_analytics
docker compose exec airflow airflow dags trigger platform_health
```

Wait for both DAG runs to finish, then run at least one report at <http://localhost:8090>. Prometheus targets can be checked at <http://localhost:9090/targets>.

If all non-Airflow dashboards are empty, first check application migrations and ingestion rather than Grafana itself:

```bash
docker compose ps --all
docker compose logs customer-service analytics-service kafka-exporter prometheus
curl -fsS http://localhost:9090/api/v1/query?query=kafka_brokers
```

The repository is mounted read-only in the Airflow container. dbt writes compiled SQL, `run_results.json`, and its log to the writable, ephemeral paths `/tmp/dbt-target` and `/tmp/dbt-logs`. `DBT_TARGET_PATH` and `DBT_LOG_PATH` configure those locations, and the DAG reads run metadata from the configured target path.

It creates or resolves anomaly records for:

- connector or connector-task failure;
- no new CDC data within the configured SLA;
- no successful model execution within the configured SLA;
- row-count change greater than 50% between observations.

The design avoids producing a new anomaly every minute: an unresolved anomaly remains open until the signal recovers. In production these rows would also drive PagerDuty, Slack or email notification through an Airflow callback, Grafana alert, or an event topic.

## Useful Kafka signals

- Consumer lag and maximum lag by group/topic/partition
- Records produced per second
- Offline or leaderless partitions
- Under-replicated replicas
- Partition skew and hot partitions
- Broker/controller availability
- Connect failed tasks and restart frequency
- Dead-letter-topic arrival rate
- End-to-end event latency: source commit timestamp to Bronze ingestion timestamp

The local Kafka exporter exposes the metrics supported without broker-side JMX. Production Kafka monitoring should also expose broker JMX/OpenTelemetry metrics for request latency, ISR changes, disk utilization, network saturation and controller health.

## Useful dbt/Airflow signals

Beyond median, p95 and error rate, monitor queue/scheduling delay, retry rate, skipped models, test failures by severity, rows processed, warehouse bytes/credits, critical-path duration, SLA misses, state-deferred model count and freshness of downstream exposures.
