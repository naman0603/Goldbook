package com.goldbookapp.model



    data class NewInvoiceSaveStatusModel(
        val data: List<Any?>,
        val message: String?,
        val code: String?,
        val status: Boolean?,
        val errormessage: LoginModel.Errormessage?
    )

