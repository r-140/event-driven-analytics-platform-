#!/usr/bin/env bash
set -Eeuo pipefail
: "${KAFKA_CONNECT_URL:?Set KAFKA_CONNECT_URL, for example https://connect.example.com}"
: "${KAFKA_CONNECT_USERNAME:?Set KAFKA_CONNECT_USERNAME}"
: "${KAFKA_CONNECT_PASSWORD:?Set KAFKA_CONNECT_PASSWORD}"
for file in infrastructure/debezium/register-*-outbox.json; do
  name="$(jq -r .name "$file")"
  jq '.config' "$file" | curl --fail --silent --show-error \
    --user "$KAFKA_CONNECT_USERNAME:$KAFKA_CONNECT_PASSWORD" \
    -X PUT "$KAFKA_CONNECT_URL/connectors/$name/config" \
    -H 'Content-Type: application/json' --data-binary @-
  echo
  running=false
  for attempt in {1..30}; do
    if curl --fail --silent --show-error --user "$KAFKA_CONNECT_USERNAME:$KAFKA_CONNECT_PASSWORD" \
      "$KAFKA_CONNECT_URL/connectors/$name/status" | jq -e '.connector.state == "RUNNING" and all(.tasks[]; .state == "RUNNING")' >/dev/null; then
      running=true
      break
    fi
    sleep 2
  done
  [[ "$running" == true ]] || { echo "$name did not reach RUNNING" >&2; exit 1; }
  echo "deployed $name"
done
