package com.goldbookapp.model

data class VerifyPasswordModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    class Data()
}

