package com.goldbookapp.model

data class NewOrganizationModel(
    val message: String?,
    val data: List<Any>,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
)