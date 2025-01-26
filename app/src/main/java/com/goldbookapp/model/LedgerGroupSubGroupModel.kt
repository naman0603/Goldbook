package com.goldbookapp.model

data class LedgerGroupSubGroupModel(
    val code: Int,
    val data: List<DataLedgerGroupSubGroup>,
    val message: String,
    val status: Boolean,
    val errormessage: LoginModel.Errormessage?
){
    data class DataLedgerGroupSubGroup(
        val group_id: Int,
        val group_name: String,
        val is_bank_account: Int,
        val is_duties_and_taxes: Int,
        val is_sub_account: Int,
        val sub_group_id: Int,
        val sub_group_name: String
    )
}