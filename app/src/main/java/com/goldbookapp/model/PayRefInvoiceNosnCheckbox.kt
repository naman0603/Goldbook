package com.goldbookapp.model

data class PayRefInvoiceNosnCheckbox(
    val selectedPayRefInvoiceNoList: List<PayRefRowData>?
) {
    data class PayRefRowData(val invoice_number: String,
                             var chb:Boolean,
                             val id: String,
                             val transaction_date: String?,
                             val total_fine_wt: String?,
                             val grand_total: String?,
                             val no_of_items: Number?)
}