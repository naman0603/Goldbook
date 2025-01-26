package com.goldbookapp.model

data class SearchLedgerModel(
    val data: List<LedgerDetails>?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
){
    data class LedgerDetails(val ledger_id: String?, val name: String?)
}