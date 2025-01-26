package com.goldbookapp.model

data class ChangeStatusItemModel(
    val data: Data?,
    val message: String?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(val vendors: List<Any>?)
}
