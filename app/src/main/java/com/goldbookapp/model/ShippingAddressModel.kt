package com.goldbookapp.model

/*data class ShippingAddressModel(val shipping_address: Shipping_address?) {

    data class Shipping_address(
        val location: String?,
        val area: String?,
        val landmark: String?,
        val country_id: String?,
        val country_name: String?,
        val state_id: String?,
        val state_name: String?,
        val city_id: String?,
        val city_name: String?,
        val pincode: String?,
        val secondary_no: String?,
        val mobile_no: String?,
        val fax_no: String?
    )

}*/

data class ShippingAddressModel(
    val customer_id: String?,
    val company_id: String?,
    val location: String?,
    val area: String?,
    val landmark: String?,
    val country_id: String?,
    val country_name: String?,
    val state_id: String?,
    val state_name: String?,
    val city_id: String?,
    val city_name: String?,
    val pincode: String?,
    val mobile_no: String?,
    val secondary_no: String?,
    val fax_no: String?
)