package com.goldbookapp.model

class OpeningStockItemCalculationModel(
    val data: ArrayList<OpeningStockItemCalcModelItem>
) {
    data class OpeningStockItemCalcModelItem(

        val item_id: String,
        val item_name: String,
        val item_quantity: String,
        val item_size: String,
        val item_gross_wt: String,
        val item_less_wt: String,
        val item_net_wt: String,
        val item_touch: String,
        val item_wastage: String,
        val item_fine_wt: String,
        val item_total: String,
        val item_remarks: String,
        val item_unit_id: String,
        val item_unit_name: String,
        val item_use_stamp: String,
        val item_stamp_id: String,
        val item_stamp_name: String,
        val item_use_gold_color: String,
        val item_gold_color_id: String,
        val item_gold_color_name: String,
        val item_metal_type_id: String,
        val item_metal_type_name: String,
        val item_maintain_stock_in_id: String,
        val item_maintain_stock_in_name: String,
        val item_rate: String,
        val item_rate_on: String,
        val item_amount: String,
        val item_charges: String,
        val item_discount: String,
        val item_type: String,
        val tag_no: String,
        val random_tag_id: String,
        val item_is_studded: String,
        val item_wt_breakup: OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup,
        val item_charges_breakup: /*ItemChargesBreakup*/OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup,
        val item_unit_array: List<ItemSearchModel.ItemSearch.Unit_array>,
        var tax_analysis_array: Tax_analysis_array?

    ) {
        data class Tax_analysis_array(
            val item_id: String,
            val item_name: String,
            val ledger_id: String,
            val ledger_name: String,
            val taxable_amount: String,
            val hsn: String,
            val gst_rate: String,
            val gst_rate_percentage: String,
            val igst_amount: String,
            val cgst_amount: String,
            val sgst_amount: String
        )
    }
}




