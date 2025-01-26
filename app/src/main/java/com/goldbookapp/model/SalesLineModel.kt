package com.goldbookapp.model

data class SalesLineModel(
    val data: ArrayList<SaleLineModelDetails>
) {
    data class SaleLineModelDetails(
        val cash_amount: String?,
        val cash_ledger: String?,
        val cash_ledger_name: String?,
        val cash_description: String?,
        val bank_amount: String?,
        val bank_ledger: String?,
        val bank_ledger_name: String?,
        val bank_mode: String?,
        val cheque_number: String?,
        val cheque_date: String?,
        val favouring_name: String?,
        val deuct_charges: String?,
        val deuct_charges_percentage: String?,
        val bank_final_amt: String?,
        val recipient_bank: String?,
        val account_no: String?,
        val ifs_code: String?,
        val utr_number: String?,
        val bank_description: String?,
        val rcm_gold_rate: String?,
        val rate_cut_amount: String?,
        val rate_cut_fine_term: String?,
        val metal_type_id_rate_cut: String?, //rate cut selection ->gold or silver
        val rate_cut_fine: String?,
        val item_id: String?,
        val item_name: String?,
        val metal_type_id_metal: String?,
        val maintain_stock_in_name_metal: String?,
        /*val maintain_stock_in_id_metal: String?,*/
        val gross_wt: String?,
        val less_wt: String?,
        val net_wt: String?,
        val touch: String?,
        val wast: String?,
        val fine_wt: String?,
        //djustment
        val adjustment_fine: String?,
        val metal_type_id_adjustments: String?,
        val adjustment_amount: String?,
        val adjustment_ledger: String?,
        val adjustment_ledger_name: String?,
        val adjustment_description: String?,
        val type: String?,
        val transaction_type: String?,
        val transaction_title: String?

    )

}