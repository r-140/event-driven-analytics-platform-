{{ config(unique_key='adjustment_id') }}
select (payload->>'id')::uuid adjustment_id,(payload->>'invoiceId')::uuid invoice_id,(payload->>'amount')::numeric(19,2) amount,payload->>'currency' currency,payload->>'reason' reason,(payload->>'adjustedAt')::timestamptz adjusted_at,ingested_at
from {{ source('bronze','domain_events') }} where domain='invoice' and event_type='InvoiceAdjusted'
{% if is_incremental() %}and ingested_at > (select coalesce(max(ingested_at), '1970-01-01') from {{ this }}){% endif %}
