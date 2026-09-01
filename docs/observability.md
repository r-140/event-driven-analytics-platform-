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
