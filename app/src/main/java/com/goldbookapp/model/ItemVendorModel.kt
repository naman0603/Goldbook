package com.goldbookapp.model


data class ItemVendorModel(
    val data: List<Data427691210>?,
    val message: String?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data427691210(val full_name: String,val display_name:String, val vendor_id: String?)

}

