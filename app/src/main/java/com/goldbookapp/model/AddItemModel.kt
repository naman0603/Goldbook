package com.goldbookapp.model

data class AddItemModel(
    val item_id: String?,
    val item_name: String?,
    val unit_id: String?,
    val quantity: String?,
    val gross_wt: String?,
    val less_wt: String?,
    val net_wt: String?,
    val touch: String?,
    val wastage: String?,
    val fine_wt: String?,
    val remarks: String?,
    val charge: List<Charge867235109>?
) {
    data class Charge867235109(val name: String?, val amount: String?, val unit_id: String?)
}