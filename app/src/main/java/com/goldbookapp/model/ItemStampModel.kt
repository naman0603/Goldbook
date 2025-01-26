package com.goldbookapp.model

data class ItemStampModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(val stamp: List<Stamp>) {
        data class Stamp(
            val id: Int,
            val stamp_name: String
        )
    }
}