select customer_id,email,full_name,country_code,created_at from {{ ref('customers') }}
