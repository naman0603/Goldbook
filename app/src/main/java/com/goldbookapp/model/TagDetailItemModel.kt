package com.goldbookapp.model



data class TagDetailItemModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(
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
val item_discount: String,
        val item_charges: String,
        val type: String,
        val item_type: String,
        val item_is_studded: String,
        val item_wt_breakup: OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup,
        val item_charges_breakup:
OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup,
        val item_unit_array: List<ItemSearchModel.ItemSearch.Unit_array>,
        val item_tax_preference: String,
        val item_sales_purchase_hsn: String,
        val item_sales_purchase_gst_rate: String,
        val item_gst_rate_percentage: String,
        val item_sales_making_charges: String,
        val item_sales_rate: String,
        val item_sales_ledger_id: String,
        val item_sales_ledger_name: String,
        val tag_no: String,
        val random_tag_id: String

    )
}
