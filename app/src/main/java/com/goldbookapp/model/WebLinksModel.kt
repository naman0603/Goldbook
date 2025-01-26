package com.goldbookapp.model

data class WebLinksModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(
        val terms: String?, val privacy: String?
    )
}

