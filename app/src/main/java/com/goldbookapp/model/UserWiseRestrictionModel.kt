package com.goldbookapp.model

data class  UserWiseRestrictionModel(
    val data: Data,
    val message: String?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage
) {
    data class Data(var permission: ArrayList<String>?, var fields: ArrayList<String>?)
}
