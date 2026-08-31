select country_code, count(*) as customer_count, min(created_at) as first_customer_at, max(created_at) as latest_customer_at
from {{ ref('customers') }} group by country_code
