package com.goldbookapp.model

data class ItemDefaultTermModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(val default_term: List<Default_term>) {
        data class Default_term(
            val default_term: String?,
            val default_term_value: String?,
            val default_short_term: String?

        )
    }
}