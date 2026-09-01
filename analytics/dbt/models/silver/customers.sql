{{ config(unique_key='customer_id') }}
select
  aggregate_id as customer_id,
  lower(payload ->> 'email') as email,
  payload ->> 'fullName' as full_name,
  upper(payload ->> 'countryCode') as country_code,
  (payload ->> 'createdAt')::timestamptz as created_at,
  ingested_at
from {{ source('bronze', 'customer_events') }}
where event_type = 'CustomerCreated'
{% if is_incremental() %}and ingested_at > (select coalesce(max(ingested_at), '1970-01-01') from {{ this }}){% endif %}
