package com.goldbookapp.model

data class ItemDetailModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage
) {

    data class Data(val item: Item?) {
        data class Item(
            /* val item_id: String?,
            val item_name: String?,
            val default_image_index: String?,
            val item_image: String?,
            val item_code: String?,
            val sales_wastage: String?,
            val sales_making_charges: String?,
            val purchase_wastage: String?,
            val purchase_making_charges: String?,
            val unit: String?,
            val notes: String?,
            val is_editable: String?,
            val item_stock_type: String?,
            val show_in_sales: String?,
            val show_in_purchase: String?,
            val is_raw_material: String?,
            val category_name: String?,
            val category_id: String?,
            val unit_id: String?,
            val unit_name: String?,
            val stock_in_hand: String?,
            val status: String?,
            val opening_stocks: List<OpeningStockModel.Openingstock>?,
            val image: List<Image1888495311>?,
            val item_preferred_vendor: ArrayList<ItemPrefVendor>?,
            val hsn_code : String?,
            val minimum_stock_level : String?,
            val maximum_stock_level : String?,
            val item_colour: ArrayList<ItemPrefColor>?*/
            val item_id: String?,
            val item_type: String?,
            val item_name: String?,
            val item_code: String?,
            val stock_in_hand: String?,
            val notes: String?,
            val is_editable: String?,
            val category_name: String?,
            val category_id: String?,
            val unit_id: String?,
            val unit_name: String?,
            val metal_type_id: String?,
            val metal_type: String?,
            val maintain_stock_in_id: String?,
            val maintain_stock_in: String?,
            val is_studded: String?,
            val status: String?,
            val tax_preference: String?,
            val sales_purchase_hsn: String?,
            val jobwork_labourwork_sac: String?,
            val sales_purchase_gst_rate_id: String?,
            val sales_purchase_gst_rate: String?,
            val jobwork_labourwork_gst_rate_id: String?,
            val jobwork_labourwork_gst_rate: String?,
            val sales_wastage: String?,
            val sales_making_charges: String?,
            val sales_rate: String?,
            val purchase_wastage: String?,
            val purchase_making_charges: String?,
            val purchase_rate: String?,
            val jobwork_rate: String?,
            val labourwork_rate: String?,
            val sales_ledger_id: String?,
            val purchase_ledger_id: String?,
            val jobwork_ledger_id: String?,
            val labourwork_ledger_id: String?,
            val discount_ledger_id: String?,
            val tag_prefix: String?,
            val use_stamp: String?,
            val product_wt: String?,
            val item_rate: String?,
            val stock_method: String?,
            val use_gold_color: String?,
            val min_stock_level_gm: String?,
            val max_stock_level_gm: String?,
            val min_stock_level_pcs: String?,
            val max_stock_level_pcs: String?,
            val sales_ledger_name: String?,
            val purchase_ledger_name: String?,
            val jobwork_ledger_name: String?,
            val labourwork_ledger_name: String?,
            val discount_ledger_name: String?,
            val image: List<Images>?,
            val item_preferred_vendor: List<ItemPrefVendor>?,
            val item_colour: List<ItemPrefColor>?


        ) {
            data class Images(val item_image_id: String?, val item_image: String?)
            data class ItemPrefVendor(val vendor: String?, val vendor_id: String?)
            data class ItemPrefColor(val colour_name: String?, val colour_id: String?)
        }
    }


}



