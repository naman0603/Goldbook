package com.goldbookapp.model

data class StockPrintModel(
    val records: String?,
    val data: Data,
    val message: String?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(
        val dateArray: DateArray?,
        val categories: List<Categories>?,
        val grand_total_net_wt: String?,
        val grand_total_fine_wt: String?
    ) {

        data class Categories(
            val category: String?,
            val items: List<Items>?,
            val sub_total_net_wt: String?,
            val sub_total_fine_wt: String?
        ) {
            data class Items(val item: String?, val entries: List<Entries>?) {
                data class Entries(val touch: String?, val net_wt: String?, val fine_wt: String?)
            }
        }

        data class DateArray(val from_date: String?, val to_date: String?)

    }
}


