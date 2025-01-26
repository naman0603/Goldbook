package com.goldbookapp.model

data class SearchListPurchaseModel(
    val data: List<DataPurchase>?,
    val message: String?,
    val total_page: Int?,
    val page_limit: Int?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {

    data class DataPurchase(
        val id: Number?,
        val transaction_id: String,
      /*  val prefix: String?,
        val series: String?,*/
        val invoice_number: String,
        val transaction_date: String?,
        val total_fine_wt: Number?,
        val grand_total: String?,
        val customer_name: String?,
        val contact_name: String?,
        val total_items: Number?,
        val signature_verify: Number?,
        val status: String?,
        var isChecked:Boolean
    )
}