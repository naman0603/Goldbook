package com.goldbookapp.model

data class ItemSearchModel(
    val code: String,
    val data: ArrayList<ItemSearch>,
    val message: String,
    val status: Boolean,
    val errormessage: LoginModel.Errormessage?
){
    data class ItemSearch(
        val category_name: String,
        val color: List<Color>,
        val id: String,
        val item_type: String,
        val is_studded: String,
        val item_name: String,
        val maintain_stock_in_id: String,
        val maintain_stock_in_name: String,
        val metal_type_id: String,
        val metal_type_name: String,
        val unit_id: String,
        val unit_name: String,
        var unit_value: String, // unit_value and net_wt fields are added to get updated value in add charge screen
        var net_wt: String,
        val use_gold_color: String,
        val use_stamp: String,
        val unit_array : List<Unit_array>,
        val sales_wastage: String,
        val sales_making_charges: String,
        val sales_purchase_hsn: String,
        val sales_ledger_id: String,
        val sales_ledger_name: String,
        val purchase_ledger_id: String,
        val purchase_ledger_name: String,
        val purchase_wastage: String,
        val purchase_making_charges: String,
        val tax_preference: String,
        val product_wt: String,
        val item_rate: String,
        val sales_purchase_gst_rate_id: String,
        val sales_purchase_gst_rate: String

    ){

        data class  Color(
            val id :String,
            val colour_name :String
        )

        data class  Unit_array(
            val id :String,
            val name:String
        )
    }
}




