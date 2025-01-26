package com.goldbookapp.model

data class CalculateSalesModel(
    val data: DataSales?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class DataSales(
        val is_show_round_off: String?,
        val igst_amount: String?,
        val cgst_amount: String?,
        val sgst_amount: String?,
        val total_net_wt_with_IRT: String?,
        val total_fine_wt_with_IRT: String?,
        val total_fine_wt_with_IRT_short_term: String?,
        val total_fine_wt_with_IRT_term: String?,
        val total_silver_fine_wt_with_IRT: String?,
        val total_silver_fine_wt_with_IRT_short_term: String?,
        val total_silver_fine_wt_with_IRT_term: String?,
        val total_amount_with_IRT: String?,
        val total_quantity: String?,
        val total_gross_wt: String?,
        val total_less_wt: String?,
        val total_net_wt: String?,
        val total_fine_wt: String?,
        val total_misc_charges: String?,
        val tcs_tds_taxable_amount: String?,
        val tcs_amount: String?,
        val tcs_percentage: String?,
        val tds_amount: String?,
        val tds_percentage: String?,
        val sub_total: String?,
        val final_total_amount: String?,
        val round_off_total: String?,
        val total_amount: String?,
        val grand_total: String?,
        val grand_total_short_term: String?,
        val grand_total_term: String?,
        val silver_total_quantity: String?,
        val silver_total_gross_wt: String?,
        val silver_total_less_wt: String?,
        val silver_total_net_wt: String?,
        val silver_total_fine_wt: String?,
        val silver_total_misc_charges: String?,
        val silver_total_amount: String?,
        val other_total_quantity: String?,
        val other_total_gross_wt: String?,
        val other_total_net_wt: String?,
        val other_total_misc_charges: String?,
        val other_total_amount: String?,
        val opening_cash_balance: String?,
        val opening_cash_balance_short_term: String?,
        val opening_cash_balance_term: String?,
        val running_cash_balance: String?,
        val running_cash_balance_short_term: String?,
        val running_cash_balance_term: String?,
        val closing_cash_balance: String?,
        val closing_cash_balance_short_term: String?,
        val closing_cash_balance_term: String?,
        val opening_fine_balance: String?,
        val opening_fine_balance_short_term: String?,
        val opening_fine_balance_term: String?,
        val opening_silver_fine_balance: String?,
        val opening_silver_fine_balance_short_term: String?,
        val opening_silver_fine_balance_term: String?,
        val running_fine_balance: String?,
        val running_fine_balance_short_term: String?,
        val running_fine_balance_term: String?,
        val closing_fine_balance: String?,
        val closing_fine_balance_short_term: String?,
        val closing_fine_balance_term: String?,
        val closing_silver_fine_balance: String?,
        val closing_silver_fine_balance_short_term: String?,
        val closing_silver_fine_balance_term: String?

        )

    data class Datac(
        val item: ArrayList<Item558106789>,
        val total_gross_wt: String?,
        val total_less_wt: String?,
        val total_net_wt: String?,
        val total_fine_wt: String?,
        val rate_cut_wt: String?,
        val net_fine_due: String?,
        val rate_cut_amount: String?,
        val gold_rate: String?,
        val total_misc_charges: String?,
        val taxable_amount: String?,
        val sgst_amount: String?,
        val sgst_percentage: String?,
        val cgst_amount: String?,
        val cgst_percentage: String?,
        val igst_amount: String?,
        val igst_percentage: String?,
        val tcs_taxable_amount: String?,
        val tcs_amount: String?,
        val tcs_percentage: String?,
        val sub_total: String?,
        val round_off_total: String?,
        val grand_total: String?,
        val closing_fine_balance: String?,
        val closing_cash_balance: String?,
        val opening_cash_balance: String?,
        val opening_fine_balance: String?,
        val opening_cash_default_term: String?,
        val opening_fine_default_term: String?,
        val closing_cash_default_term: String?,
        val closing_fine_default_term: String?

    ) {
        data class Item558106789(
            val item_id: String?,
            /*val transaction_item_id: String?,*/
            val item_name: String?,
            val quantity: String?,
            val unit_id: String?,
            val unit_name: String?,
            val gross_wt: String?,
            val total_less_wt: String?,
            var less_wt: ArrayList<LessWeights>?,
            val net_wt: String?,
            val fine_wt: String?,
            val cost: String?,
            val touch: String?,
            val wastage: String?,
            val remarks: String?,
            val charge: List<Charges1893561627>?
        ) {
            data class Charges1893561627(
                val name: String?, val amount: String?, val calculation_unit_id: String?,
                val calculation_unit_name: String?,/*, val transaction_item_charge_id: String?,*/
                val total_amount: String?/*, val transaction_item_id: String?*/
            )

            data class LessWeights(
                var name: String?, var amount: String?
            )
        }
    }
}










