package com.goldbookapp.model

data class OpeningStockItemModel(
    val data: ArrayList<OpeningStockItemModelItem>
) {
    data class OpeningStockItemModelItem(
        val item_id: String,
        val item_stamp_id: String,
        val item_use_gold_color: String,
        val metal_color_id: String,
        val item_quantity: String,
        val item_size: String,
        val item_gross_wt: String,
        val item_wt_breakup: ItemWtBreakup,
        val item_net_wt: String,
        val item_touch: String,
        val item_wastage: String,
        val item_fine_wt: String,
        val item_rate: String,
        val item_charges_breakup: ItemChargesBreakup,
        val item_amount: String,
        val item_remarks: String,
        val item_is_studded: String,
        val item_maintain_stock_in_id: String,
        val item_maintain_stock_in_name: String,
        val item_metal_type_id: String,
        val item_metal_type_name: String,
        val item_unit_id: String,
        val item_unit_name: String,
        val item_use_stamp: String

    ) {
        data class ItemChargesBreakup(
            val charges_array: List<ChargesArray>,
            /*val index: String,*/
            val making_charge_array: MakingChargeArray,
            val total_charges: String
        ) {
            data class MakingChargeArray(
                val amount: String,
                val unit_id: String,
                val unit_name: String
            )

            data class ChargesArray(
                var amount: String,
                var label: String,
                var unit_id: String,
                var unit_name: String
            )
        }

        data class ItemWtBreakup(
            val index: String,
            val less_wt_array: List<AddLessWeightModel.AddLessWeightModelItem>,
            val total_less_wt: String,
            val total_less_wt_amount: String
        )

    }
}