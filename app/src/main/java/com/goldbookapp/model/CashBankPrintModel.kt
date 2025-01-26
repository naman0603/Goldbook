package com.goldbookapp.model

data class CashBankPrintModel(
    val records: String?,
    val data: Data,
    val message: String?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(
        val ledgers: List<Ledgers>?,
        val dateArray: DateArray?
    ) {
        data class Ledgers(val ledger_name: String?, val dates: List<Dates>?, val total_amount: String?, val total_amount_term: String?)
        {
            data class Dates(val date: String?, val entries: List<Entries>?) {
                data class Entries(val customer_name: String?, val series: String?, val amount: String?, val amount_term: String?)
            }
        }
        data class DateArray(val from_date: String?, val to_date: String?)

    }
}

// result generated from /json
