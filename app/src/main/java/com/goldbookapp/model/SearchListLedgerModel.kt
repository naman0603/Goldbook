package com.goldbookapp.model

data class SearchListLedgerModel(
    val data: List<DataLedger>?,
    val message: String?,
    val total_page: Int?,
    val page_limit: Int?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {

    data class DataLedger(
        val code: String,
        val group_name: String,
        val ledger_id: Int,
        val ledger_name: String,
        val notes: String,
        val sub_group_name: String
    )
}