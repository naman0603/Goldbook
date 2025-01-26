package com.goldbookapp.model

data class NewItemCatModel(
    val message: String?,
    val data: Data?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {


    data class Data(val item: Item?) {

        data class Item(
            val company_id: Number?,
            val category_name: String?,
            val category_code: String?,
            val status: Number?,
            val created_by: Number?,
            val updated_by: Number?,
            val updated_at: String?,
            val created_at: String?,
            val id: Number?
        )
    }


}
