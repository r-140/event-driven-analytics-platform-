select * from {{ ref('customers') }} where country_code !~ '^[A-Z]{2}$'
