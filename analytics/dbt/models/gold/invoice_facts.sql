select i.invoice_id,i.customer_id,i.amount as issued_amount,i.currency,i.status,i.issued_at,coalesce(sum(a.amount),0) as adjusted_amount,i.amount-coalesce(sum(a.amount),0) as net_amount
from {{ ref('invoices') }} i left join {{ ref('invoice_adjustments') }} a using(invoice_id)
group by i.invoice_id,i.customer_id,i.amount,i.currency,i.status,i.issued_at
