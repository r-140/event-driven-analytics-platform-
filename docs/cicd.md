# Data-platform CI/CD

CDC and dbt are independent release units. Application services have their own Java CI because an application build is neither a connector deployment nor an analytical-model deployment.

## CDC pipeline

Trigger paths: connector definitions, source services, Compose topology, and connector validation scripts.

CI performs:

1. JSON and connector-contract validation.
2. A disposable PostgreSQL/Kafka/Connect environment.
3. Registration of all five source connectors.
4. Correlated event generation.
5. Verification that four domain topics exist and that the Invoice topic receives records from two source services.

CD is a manually approved GitHub Environment deployment. Connector definitions are upserted through Kafka Connect's `PUT /connectors/{name}/config`; this makes deployment repeatable. The job waits for the connector and every task to reach `RUNNING`.

Configure each `staging`/`production` GitHub Environment with:

| Kind | Name |
|---|---|
| Variable | `KAFKA_CONNECT_URL` |
| Secret | `KAFKA_CONNECT_USERNAME` |
| Secret | `KAFKA_CONNECT_PASSWORD` |

Rollback means rerunning the workflow from a previous Git commit. Database WAL and Kafka compatibility must still be evaluated before rolling back a schema-affecting connector change.

## dbt pipeline

Trigger path: `analytics/dbt/**`.

CI creates representative Bronze events in disposable PostgreSQL and executes `dbt build`, which runs models plus generic and singular tests. It uploads `manifest.json`, `run_results.json`, and `catalog.json` for lineage, audit, and documentation.

CD runs the same `dbt build` against an approved environment. Configure:

| Kind | Name |
|---|---|
| Variables | `DBT_HOST`, `DBT_PORT`, `DBT_DATABASE` |
| Secrets | `DBT_USER`, `DBT_PASSWORD` |

The example uses PostgreSQL, matching the local demo. For Snowflake, replace `dbt-postgres` with `dbt-snowflake` and change only the deployment profile; project SQL should remain portable where possible.

Rollback is usually forward-fix or rerunning a previous manifest. Destructive model changes should use expand/contract releases: add the replacement model, migrate consumers, and remove the old model in a later release.

## Independence and ordering

The pipelines should not call each other by default. A new event contract may require coordinated releases, but compatibility permits safe ordering:

1. Deploy dbt/consumer support for both old and new event shapes.
2. Deploy the CDC/source change that starts producing the new shape.
3. Remove old-shape support only after retention and replay windows expire.

Use GitHub Environment approvals for production and restrict credentials: the CDC identity needs connector-management permissions, while dbt needs access only to its source schemas and owned target schemas.
