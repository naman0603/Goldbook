package com.goldbookapp.model


data class ItemUnitModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {

    data class Data(val items: List<Items1783753572>?) {
        data class Items1783753572(
            val id: String?,
            val company_id: String?,
            val unit: String,
            val status: String?,
            val created_at: String?,
            val updated_at: String?,
            val deleted_at: String?
        )
    }


}

