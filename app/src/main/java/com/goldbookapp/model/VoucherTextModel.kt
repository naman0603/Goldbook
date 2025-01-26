package com.goldbookapp.model

data class VoucherTextModel(
    val data: String?,
    val message: String?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
)