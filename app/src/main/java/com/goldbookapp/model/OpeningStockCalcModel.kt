package com.goldbookapp.model

data class OpeningStockCalcModel(
    val code: Int,
    val data: Data,
    val message: String,
    val status: Boolean,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(
        val item_json: ItemJson
    ) {
        data class ItemJson(
            val net_fine_due: String,
            val sub_total: String,
            val total_amount: String,
            val total_fine_wt: String,
            val total_gross_wt: String,
            val total_less_wt: String,
            val total_misc_charges: String,
            val total_net_wt: String,
            val total_quantity: String
        )
    }
}