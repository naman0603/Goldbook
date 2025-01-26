package com.goldbookapp.model

data class SearchVendorModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(val vendor: List<Vendor>?) {
        data class Vendor(
           /* val full_name: String?,
            val vendor_id: String?,
            val gst_register: Number?,
            val place_of_supply: String?,
            val state_id: String?,
            val is_gst_applicable: Boolean?*/
            val full_name: String?,
            val display_name: String?,
            val vendor_id: String?,
            val gst_register: String?,
            val place_of_supply: String?,
            val state_id: String?,
            val cash_balance: String?,
            val fine_balance: String?,
            val opening_fine_default_term: String?,
            val opening_cash_default_term: String?
        )
    }
}
//data class Base(val data: Data?, val code: Number?, val message: String?, val status: Boolean?)
//
//data class Data(val vendor: List<Vendor231202503>?)
//
//data class Vendor231202503(val full_name: String?, val display_name: String?, val vendor_id: Number?, val gst_register: Number?, val place_of_supply: String?, val state_id: Number?)
