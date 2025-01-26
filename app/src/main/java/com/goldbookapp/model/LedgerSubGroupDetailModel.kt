package com.goldbookapp.model

data class LedgerSubGroupDetailModel(
    val data: DataSubGroupDetail,
    val code: Int,
    val message: String,
    val status: Boolean
){
    data class DataSubGroupDetail(
        val description: String,
        val group_id: Int,
        val group_name: String,
        val is_bank_account: Int,
        val sub_group_id: Int,
        val sub_group_name: String
    )
}