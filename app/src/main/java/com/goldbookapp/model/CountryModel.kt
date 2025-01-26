package com.goldbookapp.model

data class CountryModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(val country: List<Country1948430004>?) {
        data class Country1948430004(
            val id: String?,
            val country_name: String,
            val country_code: Number?,
            val created_at: String?,
            val updated_at: String?,
            val deleted_at: Any?
        )
    }
}



