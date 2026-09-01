{{ config(unique_key='invoice_id') }}
select (payload->>'id')::uuid invoice_id,(payload->>'customerId')::uuid customer_id,(payload->>'amount')::numeric(19,2) amount,payload->>'currency' currency,payload->>'status' status,(payload->>'issuedAt')::timestamptz issued_at,ingested_at
from {{ source('bronze','domain_events') }} where domain='invoice' and event_type='InvoiceIssued'
{% if is_incremental() %}and ingested_at > (select coalesce(max(ingested_at), '1970-01-01') from {{ this }}){% endif %}
