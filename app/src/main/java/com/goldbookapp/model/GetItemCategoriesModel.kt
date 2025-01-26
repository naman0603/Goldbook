package com.goldbookapp.model

data class GetItemCategoriesModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(val item: List<ItemCatInfo>?) {
        data class ItemCatInfo(
            val id: Number?,
            val company_id: Number?,
            val category_name: String?,
            val category_code: String?,
            val status: Number?,
            val created_by: Number?,
            val updated_by: Number?,
            val created_at: String?,
            val updated_at: String?,
            val deleted_at: Any?
        )
    }

}

