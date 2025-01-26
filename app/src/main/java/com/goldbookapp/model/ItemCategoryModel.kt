package com.goldbookapp.model

data class ItemCategoryModel(
    val data: List<Data2101931085>?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {

    data class Data2101931085(
        val id: String?,
        val company_id: Number?,
        val category_name: String,
        val category_code: String?,
        val status: Number?,
        val created_by: Number?,
        val updated_by: Number?,
        val created_at: String?,
        val updated_at: String?,
        val deleted_at: Any?
    )

}
