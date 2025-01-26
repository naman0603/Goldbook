package com.goldbookapp.model

data class TrasactionReferenceModel(
    val data: List<DataPaynRec>?,
    val message: String?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {

    data class DataPaynRec(
        val id: Number?,
        val transaction_id: String,
        val transaction_date: String?,
        val total_fine_wt: String?,
        val grand_total: String?,
        val contact_name: String?,
        val no_of_items: Number?,
        val invoice_number: String,
        var isChecked: Boolean?
    )
}


/*
data class Base(val data: List<Data1491802835>?, val message: String?, val code: Number?, val status: Boolean?)

data class Data1491802835(val id: Number?, val transaction_id: String?, val transaction_date: String?, val total_fine_wt: String?, val grand_total: String?, val contact_name: String?, val no_of_items: Number?, val invoice_number: String?)*/
