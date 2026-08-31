select currency,count(*) payment_count,sum(amount) total_amount,min(received_at) first_payment_at,max(received_at) latest_payment_at from {{ ref('payments') }} group by currency
