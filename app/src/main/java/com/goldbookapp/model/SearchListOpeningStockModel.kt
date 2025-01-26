package com.goldbookapp.model

data class SearchListOpeningStockModel(
    val data: List<DataOpeningStock>?,
    val message: String?,
    val total_page: Int?,
    val page_limit: Int?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {

    data class DataOpeningStock(
        val id: String?,
        val transaction_id: String?,
        val transaction_date: String?,
        val item_id: String?,
        val item_name: String?,
        val quantity: String?,
        val net_wt: String?,
        val touch: String?,
        val fine_wt: String?,
        val gross_wt: String?,
        val stamp: String?,
        val use_gold_color: String?,
        val gold_color: String?,
        val metal_type_name: String?,
        val transaction_item_id: String?,
        val invoice_number: String?
    )
}