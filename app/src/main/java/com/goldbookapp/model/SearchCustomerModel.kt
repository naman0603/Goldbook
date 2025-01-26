package com.goldbookapp.model

data class SearchCustomerModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(val customer: List<Customer304919607>?) {
        data class Customer304919607(
            val full_name: String?,
            val display_name: String?,
            val customer_id: String?,
            val gst_register: Number?,
            val place_of_supply: String?,
            val state_id: String?,
            val is_gst_applicable: Boolean?
        )
    }
}
