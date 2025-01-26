package com.goldbookapp.model

data class TransactionHistoryModel(
    //val transactionsum: Transactionsum?,
    val data: List<Data>?,
    val code: String?,
    val message: String?,
    val total_page: Int?,
    val page_limit: Int?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(
        val transaction_type: String?,
        val transaction_number: String?,
        val total_net_wt: String?,
       /* val ledger_type: String?,
        val fine_wt: String?,
        val amount: String?,*/
        val credit_fine_wt: String?,
        val credit_amount: String?,
        val debit_fine_wt: String?,
        val debit_amount: String?,
        val created_date: String?,
        val created_time: String?,
        val module: String?,
        val module_id: String?

    )
    data class Transactionsum(
        val sum_entries: String?,
        val credit_sum_fine_wt: String?,
        val credit_sum_amount: String?,
        val debit_sum_fine_wt: String?,
        val debit_sum_amount: String?
    )

}





