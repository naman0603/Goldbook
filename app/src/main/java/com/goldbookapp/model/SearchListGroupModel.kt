package com.goldbookapp.model

data class SearchListGroupModel(
    val data: List<DataGroup>?,
    val message: String?,
    val total_page: Int?,
   val page_limit: Int?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {

    data class DataGroup(
        val affect_gross_profit: String,
        val group_description: String,
        val group_name: String,
        val is_bank_account: String,
        val is_system_ledger: String,
        val ledger_group_id: Int,
        val nature_group_id: String,
        val nature_group_name: String,
        val parent_group_name: String,
        val sub_group_id: String
    )
}