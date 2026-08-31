select a.* from {{ ref('invoice_adjustments') }} a left join {{ ref('invoices') }} i using(invoice_id) where i.invoice_id is null
