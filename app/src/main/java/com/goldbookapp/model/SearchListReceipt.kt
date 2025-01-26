package com.goldbookapp.model

data class SearchListReceipt(
    val data: List<DataReceipt>?,
    val message: String?,
    val total_page: Int?,
    val page_limit: Int?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {

    data class DataReceipt(
        val id: String?,
        val transaction_id: String,
      /*  val prefix: String?,
        val series: String?,*/
        val invoice_number: String,
        val transaction_date: String?,
        val total_fine_wt: String?,
        val total_misc_charges: String?,
        val contact_name: String?,
        val total_items: String?,
        val status: String?,
        val signature_verify: String?
    )
}