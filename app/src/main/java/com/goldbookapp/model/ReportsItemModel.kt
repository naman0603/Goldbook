package com.goldbookapp.model

data class ReportsItemModel(
    val data: List<Items>?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {

    data class Items(
        val item_id: String?, val item_name: String?
    )

}
