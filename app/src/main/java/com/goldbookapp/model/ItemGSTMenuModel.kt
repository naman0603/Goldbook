package com.goldbookapp.model

data class ItemGSTMenuModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(val gst: List<GSTMenu>) {
        data class GSTMenu(
            val id: Int,
            val name: String
        )
    }
}