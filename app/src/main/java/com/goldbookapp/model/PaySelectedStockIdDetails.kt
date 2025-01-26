package com.goldbookapp.model



data class PaySelectedStockIdDetails(
    val selectedStockItemDetails: StockItemDetails?
) {
    data class StockItemDetails(val item_stock_id_position: Int?,
                                val item_stock_id: String?,
                                val item_id: String?,
                                val item_name: String?,
                                val unit_id: String?,
                                val unit_name: String?,
                                val stock_in_hand: String?,
                                val total_less_wt: String?,
                                val net_wt: String?,
                                val touch: String?,
                                val less_wt:List<CalculationPaymentModel.DataPayment.ItemPayment.LessWeights>

    )
}