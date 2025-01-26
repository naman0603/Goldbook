package com.goldbookapp.model

data class CalculationPaymentModel(
    val data: DataPayment?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class DataPayment(
        val item: ArrayList<ItemPayment>,
        val sub_total_fine_wt: String?,
        val sub_total_cash_bank: String?,
        val closing_fine_balance: String?,
        val closing_cash_balance: String?,
        val opening_cash_balance: String?,
        val opening_fine_balance: String?,
        val opening_cash_default_term: String?,
        val opening_fine_default_term: String?,
        val closing_cash_default_term: String?,
        val closing_fine_default_term: String?

    ) {
        data class ItemPayment(
            val transaction_item_id: String?,
            val item_stock_id: String?,
            val item_id: String?,
            val item_name: String?,
            val unit_id: String?,
            val unit_name: String?,
            val quantity: Number?,
            val gross_wt: String?,
            val less_wt: List<LessWeights>?,
            val total_less_wt: String?,
            val net_wt: String?,
            val touch: String?,
            val wastage: String?,
            val fine_wt: String?,
            val amount: String?,
            val remarks: String?,
            val item_type: String?,
            val rate_cut_type: String?,
            val ledger_id: String?,
            val ledger_name: String?,
            val mode: String?,
            val recipient_bank_name: String?,
            val recipient_account_number: String?,
            val ifsc_code: String?,
            val instrument_number: String?,
            var gold_rate: String?
        ){data class LessWeights(
            var name: String?, var amount: String?
        )
        }


        }

}


