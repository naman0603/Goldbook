package com.goldbookapp.model

data class ReportSupportContactsModel(
    val data: List<Contacts>?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Contacts(val contact_id: String?, val display_name: String?)
}


