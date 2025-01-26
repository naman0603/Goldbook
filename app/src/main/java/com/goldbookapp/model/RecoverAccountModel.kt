package com.goldbookapp.model

data class RecoverAccountModel(
    val data: Data?,
    val status: Boolean?,
    val message: String?,
    val code: String?,
    val errormessage: LoginModel.Errormessage?
) {

    class Data()


}

