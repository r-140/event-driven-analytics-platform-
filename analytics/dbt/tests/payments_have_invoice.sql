select p.* from {{ ref('payments') }} p left join {{ ref('invoices') }} i using(invoice_id) where i.invoice_id is null
