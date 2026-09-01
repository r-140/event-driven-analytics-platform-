select payment_id,invoice_id,amount as received_amount,currency,status,received_at from {{ ref('payments') }}
