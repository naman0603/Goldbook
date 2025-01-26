package com.goldbookapp.model

data class DashboardDetailsModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(
        val to_collect: To_collect?,
        val to_pay: To_pay?,
        val stock_value: Stock_value?,
        val monthly_sale: Monthly_sale?,
        val recent_transactions: List<Recent_transactions>?,
        val default_term : Default_term?
    ) {
        data class To_collect(val fine_balance: String?, val fine_default_term: String?, val cash_balance: String?, val cash_default_term: String?, val silver_fine_balance: String?, val silver_fine_default_term: String? )
        data class To_pay(val fine_balance: String?, val fine_default_term: String?, val cash_balance: String?, val cash_default_term: String?, val silver_fine_balance: String?, val silver_fine_default_term: String?)
        data class Stock_value(val item_count: String?, val fine_wt: String?)
        data class Monthly_sale(val total_net: String?, val total_amount: String?)
        data class Recent_transactions(
            val display_name: String?,
            val total_fine_wt: String?,
            val transaction_date: String?,
            val no_of_items: Number?,
            val transaction_number: String?,
            val module: String?,
            val module_id: String?
        )
        data class Default_term(
            val id: String?,
            val company_id: String?,
            val add_field_for_salesperson: String?,
            val enable_cheque_reg_for_bank_acc: Number?,
            val round_off_for_sales: String?,
            val default_term: String?,
            val debit_term: String?,
            val debit_short_term: String?,
            val credit_term: String?,
            val credit_short_term: String?,
            val print_copies: String?,
            val created_at: String?,
            val updated_at: String?
        )
    }
}
