# Semantic query layer and analytics portal

Grafana is reserved for operational observability. Business users access governed reports through the React portal at `http://localhost:8090`.

## Request flow

The browser loads the catalog, sends a semantic query object, and never sends SQL. `semantic-query-service` validates the requested model, metrics, dimensions, time grain, filters and ordering against `semantic-catalog.json`. Only then does it compile parameterized PostgreSQL SQL against dbt Gold facts.

```text
React portal -> semantic query AST -> catalog validation -> SQL compiler -> Gold facts
```

The Gold facts preserve row grain:

- `customer_facts`: one row per customer;
- `invoice_facts`: one row per invoice with issued, adjusted and net amounts;
- `payment_facts`: one row per payment.

Aggregation belongs to the semantic metric definition, allowing the same metric to be grouped by different dimensions and time grains.

## Guardrails

- table, column and expression identifiers come only from the version-controlled catalog;
- filter values are bound parameters;
- only `eq`, `in`, `gte` and `lte` filters are accepted;
- time grains are restricted to day, week, month, quarter and year;
- ordering is allowed only for selected output columns;
- response rows and database execution time are bounded;
- the JDBC pool is marked read-only.

The generated SQL is returned only to make the educational compiler behavior visible. A production API would normally expose it only to authorized developers.

## API

```text
GET  /api/semantic/catalog
GET  /api/semantic/reports
POST /api/semantic/query
```

Example:

```json
{"model":"invoice","metrics":["issued_amount","adjusted_amount","net_amount"],"dimensions":["currency"],"timeDimension":{"name":"issued_at","grain":"month"},"filters":[],"orderBy":[{"field":"issued_at_month","direction":"asc"}],"limit":1000}
```

## Production evolution

Add catalog compatibility checks, metric ownership, descriptions and deprecation state; explicit join graphs; row/column policies derived from identity; tenant filters injected server-side; Redis result caching; async execution for large reports; query cost estimation; audit storage; saved-report persistence; and metric lineage back to dbt manifest nodes.
