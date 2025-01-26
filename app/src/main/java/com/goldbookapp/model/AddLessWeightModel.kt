package com.goldbookapp.model


 data class AddLessWeightModel(
     val data : ArrayList<AddLessWeightModelItem>
 ){
    data class AddLessWeightModelItem(
        val label: String,
        val less_wt_final_wt: String,
        val less_wt_item_amount: String,
        val less_wt_item_id: String,
        val less_wt_item_name: String,
        val less_wt_item_per: String,
        val less_wt_item_per_name: String,
        val less_wt_item_rate: String,
        val less_wt_lbr_amount: String,
        val less_wt_lbr_per: String,
        val less_wt_lbr_per_name: String,
        val less_wt_lbr_rate: String,
        val less_wt_less_wt: String,
        val less_wt_maintain_stock_in_name: String,
        val less_wt_pieces: String,
        val less_wt_total_amount: String,
        val less_wt_unit_array: List<ItemSearchModel.ItemSearch.Unit_array>?,
        val less_wt_variation: String,
        val less_wt_weight: String,
        val less_wt_less_wt_converted: String,
        val less_wt_final_wt_converted: String
        //var less_wt_unit_name :String

    )

}

