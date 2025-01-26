package com.goldbookapp.model

data class PreferenceDetailModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
){
    data class Data(
        val id: String?,
        val enable_cheque_reg_for_bank_acc: String?,
        val round_off_for_sales: String?,
        val default_term: String?,
        val print_copies: String?
    )
}
