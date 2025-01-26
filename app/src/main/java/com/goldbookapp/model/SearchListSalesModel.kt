package com.goldbookapp.model

data class SearchListSalesModel(
    val data: List<Data1465085328>?,
    val message: String?,
    val total_page: Int?,
    val page_limit: Int?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {

    data class Data1465085328(
        val id: Number?,
        val transaction_id: String?,
        val transaction_date: String?,
        val contact_id: Number?,
        val contact_name: String?,
        val state_name: String?,
        val city_name: String?,
        val vendor_bill_number: String?,
        val total_items: Number?,
        val total_qty: String?,
        val total_net_wt: String?,
        val total_fine_wt: String?,
        val total_misc_charges: String?,
        val invoice_number: String?
    )
}