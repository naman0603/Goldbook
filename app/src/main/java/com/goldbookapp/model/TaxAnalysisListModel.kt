package com.goldbookapp.model

data class TaxAnalysisListModel(
    val data: ArrayList<TaxAnalysisList>
) {
    data class TaxAnalysisList(
        val item_id: String?,
        val item_name: String?,
        val ledger_id: String?,
        val ledger_name: String?,
        val taxable_amount: String?,
        val hsn: String?,
        val gst_rate: String?,
        val gst_rate_percentage: String?,
        val igst_amount: String?,
        val cgst_amount: String?,
        val sgst_amount: String?
    )
}
