package com.goldbookapp.model


data class AddInventoryInfoModel(
    val stockMethod: String,
    val tagInventoryInfo: String,
    val stamp: String,
    val use_gold_colour:String,
    val seletedColors: String,
    val minStockGms: String,
    val minStockPcs: String,
    val maxStockGms: String,
    val maxStockPcs: String,
    val product_wt: String,
    val item_rate: String,
    val selectedVendors: String,
    val colorNamesNIds : String,
    val vendorNamesNIds : String
)


