package com.goldbookapp.model

data class ErrorModel(
    val data: List<Any>?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
)


