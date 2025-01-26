package com.goldbookapp.model



data class UpdateGoldrateModel(
    val message: String?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
)

