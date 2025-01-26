package com.goldbookapp.model


data class UpdateUsernameModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {

    data class Data(val company: Company?) {

        data class Company(val company_id: Number?, val step: Number?)
    }
}
