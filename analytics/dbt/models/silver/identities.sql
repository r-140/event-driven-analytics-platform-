{{ config(unique_key='identity_id') }}
select (payload->>'id')::uuid identity_id,(payload->>'customerId')::uuid customer_id,lower(payload->>'login') login,payload->>'status' status,(payload->>'registeredAt')::timestamptz registered_at,ingested_at
from {{ source('bronze','domain_events') }} where domain='identity'
{% if is_incremental() %}and ingested_at > (select coalesce(max(ingested_at), '1970-01-01') from {{ this }}){% endif %}
