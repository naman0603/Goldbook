package com.goldbookapp.model

data class GetItemListModel(
    val data: List<Data1077697879>?,
    val message: String?,
    val total_page: Int?,
    val page_limit: Int?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage
) {

    data class Data1077697879(
        val item_id: String?,
        val unit_id: String?,
        val unit_name: String?,
        val item_name: String?,
        val item_type: String?,
        val net_wt: String?,
        val gross_wt: String?,
        val is_editable: Number?
    )
}

