package com.goldbookapp.model

data class ContactPrintModel(
    val records: String?,
    val data: Data,
    val message: String?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
){
    data class Data(val dateArray: DateArray?, val reportData: ReportData?){
        data class DateArray(val from_date: String?, val to_date: String?)
        data class ReportData(
            val opening_balance: Opening_balance?,
            val create_opening_balance: Create_opening_balance?,
            val contact_name: String?,
            val transactions: List<Transactions>?,
            val total_fine_wt: String?,
            val total_amount: String?,
            val total_fine_wt_term: String?,
            val total_amount_term: String?,
            val closing_fine_wt: String?,
            val closing_amount: String?,
            val closing_amount_term: String?,
            val closing_amount_short_term: String?,
            val closing_fine_wt_term: String?,
            val closing_fine_wt_short_term: String?
        ){
            data class Opening_balance(
                val opening_date: String?,
                val transaction_type: String?,
                val series: String?,
                val item_name: String?,
                val amount: String?,
                val amount_term: String?,
                val amount_short_term: String?,
                val fine_wt: String?,
                val fine_wt_term: String?,
                val fine_wt_short_term: String?
            )
            data class Create_opening_balance(
                val create_opening_date: String?,
                val transaction_type: String?,
                val series: String?,
                val item_name: String?,
                val amount: String?,
                val amount_term: String?,
                val amount_short_term: String?,
                val fine_wt: String?,
                val fine_wt_term: String?,
                val fine_wt_short_term: String?
            )
            class Transactions(
                val transaction_date: String?,
                val transaction_type: String?,
                val series: String?,
                val item_name: String?,
                val fine_wt: String?,
                val amount: String?,
                val fine_wt_term: String?,
                val amount_term: String?
            )
        }


    }
}




/*
// result generated from /json

data class Base(val data: Data?, val code: Number?, val message: String?, val status: Boolean?)

data class Data(val dateArray: DateArray?, val reportData: ReportData?)

data class DateArray(val from_date: String?, val to_date: String?)

data class Opening_balance(
    val opening_date: String?,
    val transaction_type: String?,
    val series: String?,
    val item_name: String?,
    val amount: String?,
    val amount_term: String?,
    val fine_wt: String?,
    val fine_wt_term: String?
)

data class ReportData(
    val opening_balance: Opening_balance?,
    val contact_name: String?,
    val transactions: List<Transactions483693240>?,
    val total_fine_wt: String?,
    val total_amount: String?,
    val total_fine_wt_term: String?,
    val total_amount_term: String?,
    val closing_fine_wt: String?,
    val closing_amount: String?,
    val closing_amount_term: String?,
    val closing_fine_wt_term: String?
)

data class Transactions483693240(
    val transaction_date: String?,
    val transaction_type: String?,
    val series: String?,
    val item_name: String?,
    val fine_wt: String?,
    val amount: String?,
    val fine_wt_term: String?,
    val credit_amount: String?
)
*/
