package com.goldbookapp.model

data class ReportsItemCategoryModel(
    val data: List<Categories>?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {

    data class Categories(
        val item_category_id: String?, val category_name: String?
    )

}
