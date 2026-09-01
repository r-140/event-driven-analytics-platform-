{{ config(unique_key='payment_id') }}
select (payload->>'id')::uuid payment_id,(payload->>'invoiceId')::uuid invoice_id,(payload->>'amount')::numeric(19,2) amount,payload->>'currency' currency,payload->>'status' status,(payload->>'receivedAt')::timestamptz received_at,ingested_at
from {{ source('bronze','domain_events') }} where domain='payment'
{% if is_incremental() %}and ingested_at > (select coalesce(max(ingested_at), '1970-01-01') from {{ this }}){% endif %}
