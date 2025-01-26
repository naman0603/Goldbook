package com.goldbookapp.model

data class UpdatePasswordModel(
    val data: DataUpdate?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: Errormessage?
) {
    data class DataUpdate(
        val token_type: String?,
        val expires_in: Number?,
        val access_token: String?,
        var bearer_access_token: String?,
        val refresh_token: String?,
        val company_id: String?
    )

    data class Errormessage(
        val message: String?
    )

}


