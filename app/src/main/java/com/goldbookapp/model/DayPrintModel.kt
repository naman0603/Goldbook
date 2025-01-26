package com.goldbookapp.model

data class DayPrintModel(
    val records: String?,
    val data: Data,
    val message: String?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(
        val dateArray: DateArray?,
        val line_entries: List<Line_enries>?,
        val total_amount: String?,
        val total_qty: String?,
        val total_amount_term: String?,
        val total_qty_term: String?
    ) {
        data class DateArray(val from_date: String?, val to_date: String?)
        data class Line_enries(val date: String?, val entries: List<Entries>?) {
            data class Entries(
                val customer_name: String?,
                val series: String?,
                val amount: String?,
                val qty: String?,
                val amount_term: String?,
                val qty_term: String?
            )
        }

    }
}


// result generated from /json
/*

data class Base(val data: Data?, val code: Number?, val message: String?, val status: Boolean?)

data class Data(val dateArray: DateArray?, val line_entries: List<Line_entries1735651039>?, val total_amount: String?, val total_qty: String?, val total_amount_term: String?, val total_qty_term: String?)

data class DateArray(val from_date: String?, val to_date: String?)

data class Entries425380288(val customer_name: String?, val series: String?, val amount: String?, val qty: String?, val amount_term: String?, val qty_term: String?)


data class Line_entries1735651039(val date: String?, val entries: List<Entries425380288>?)



*/
