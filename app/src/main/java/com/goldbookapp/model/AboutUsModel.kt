package com.goldbookapp.model

data class AboutUsModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(
        val title: String?,
        val description: String?,
        val extra_description: Extra_description?
    ) {
        data class Extra_description(
            val web: String?, val email: String?, val phone: String?
        )
    }
}

/*
data class Base(val data: Data?, val code: Number?, val message: String?, val status: Boolean?)

data class Data(val title: String?, val description: String?, val extra_description: Extra_description?)

data class Extra_description(val web: String?, val email: String?, val phone: String?)

*/


