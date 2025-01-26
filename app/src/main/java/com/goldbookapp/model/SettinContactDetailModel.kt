package com.goldbookapp.model

data class SettinContactDetailModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
){
    data class Data(
        val id: String?,
        val disable_credit_limit: String?,
        val stop_transaction_if_limit_over: String?
    )
}
