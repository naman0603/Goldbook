package com.goldbookapp.model

data class LedgerGroupDetailModel(
    val code: Int,
    val `data`: DataGroupDetail,
    val message: String,
    val status: Boolean
){
    data class DataGroupDetail(
        val affect_gross_profit: String,
        val description: String,
        val group_id: Int,
        val group_name: String,
        val is_bank_account: Int,
        val is_system_ledger: Int,
        val nature_id: Int,
        val nature_name: String
    )
}