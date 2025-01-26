package com.goldbookapp.model

data class TermsOfServiceModel(
    val data: DataTos?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {

    data class DataTos(val services: List<TosDesc>?) {
        data class TosDesc(
            val id: Number?,
            val title: String?,
            val description: String?,
            val created_at: String?,
            val updated_at: String?,
            val deleted_at: Any?
        )

    }

}



