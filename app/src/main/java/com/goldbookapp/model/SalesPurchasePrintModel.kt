package com.goldbookapp.model

data class SalesPurchasePrintModel(
    val records: String?,
    val data: Data,
    val message: String?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
){
    data class Data(
        val dateArray: DateArray?,
        val reportData: List<ReportData>?,
        val grand_total_net_wt: String?,
        val grand_total_fine_wt: String?,
        val grand_total_amount: String?
    ) {
        data class DateArray(val from_date: String?, val to_date: String?)
        data class ReportData(
            val customer_name: String?,
            val entries: List<Entries>?,
            val sub_total_net_wt: String?,
            val sub_total_fine_wt: String?,
            val sub_total_amount: String?
        )
        data class Entries(
            val series: String?,
            val item_name: String?,
            val transaction_date: String?,
            val net_wt: String?,
            val fine_wt: String?,
            val total_charges: String?
        )


    }
}

/*
// result generated from /json

data class Base(val data: Data?, val code: Number?, val message: String?, val status: Boolean?)

data class Data(val dateArray: DateArray?, val reportData: List<ReportData745642023>?, val grand_total_net_wt: String?, val grand_total_fine_wt: String?, val grand_total_amount: String?)

data class DateArray(val from_date: String?, val to_date: String?)

data class Entries(val series: String?, val item_name: String?, val transaction_date: String?, val net_wt: String?, val fine_wt: String?, val total_charges: String?)

data class ReportData745642023(val customer_name: String?, val entries: Entries?, val sub_total_net_wt: String?, val sub_total_fine_wt: String?, val sub_total_amount: String?)
*/
