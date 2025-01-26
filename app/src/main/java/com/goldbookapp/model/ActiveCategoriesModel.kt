package com.goldbookapp.model

data class ActiveCategoriesModel(
    val data: List<Data1033514216>?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {

    data class Data1033514216(
        val item_category_id: String?,
        val category_name: String,
        val category_code: String?
    )

}
