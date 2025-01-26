package com.goldbookapp.model

data class ParentGroupModel(
    val code: Int,
    val data: List<DataParentGroup>,
    val message: String,
    val status: Boolean,
    val errormessage: LoginModel.Errormessage?
){
    data class DataParentGroup(
        val ledger_group_id: Int,
        val ledger_group_name: String
    )
}