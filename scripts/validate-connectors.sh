#!/usr/bin/env bash
set -Eeuo pipefail
shopt -s nullglob
files=(infrastructure/debezium/register-*-outbox.json)
((${#files[@]} > 0)) || { echo 'No connector definitions found' >&2; exit 1; }
for file in "${files[@]}"; do
  jq -e '
    (.name | type == "string" and length > 0) and
    (.config["connector.class"] == "io.debezium.connector.postgresql.PostgresConnector") and
    (.config["transforms.outbox.type"] == "io.debezium.transforms.outbox.EventRouter") and
    (.config["transforms.outbox.route.topic.replacement"] | startswith("outbox.event.")) and
    (.config["table.include.list"] == "public.outbox_events") and
    (.config["slot.name"] | test("^[a-z0-9_]+$"))
  ' "$file" >/dev/null
  echo "validated $file"
done

# Two independent source connectors must converge on the Invoice domain topic.
for file in infrastructure/debezium/register-invoice-outbox.json infrastructure/debezium/register-invoice-adjustment-outbox.json; do
  [[ "$(jq -r '.config["transforms.outbox.route.topic.replacement"]' "$file")" == 'outbox.event.${routedByValue}' ]]
  [[ "$(jq -r '.config["transforms.outbox.route.by.field"]' "$file")" == 'aggregate_type' ]]
done
