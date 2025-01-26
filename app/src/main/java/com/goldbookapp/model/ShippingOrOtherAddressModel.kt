package com.goldbookapp.model

/*data class BillingAddressModel(val billing_address: Billing_address?) {

    data class Billing_address(
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
        val fax_no: String?,
        val is_shipping: String?
    )

}*/

data class ShippingOrOtherAddressModel(
    val type: String?, // contact/address
    val sub_type: String?, // shipping/other (if type = address)
    val contact_person_info_id: String?,
    val contact_salutation: String?,
    val contact_first_name: String?,
    val contact_last_name: String?,
    val contact_designation: String?,
    val contact_mobile_number: String?,
    val contact_secondary_contact: String?,
    val contact_email: String?,
    val contact_notes: String?,
    val address_line_1: String?, // rest of the fields for shipping/other
    val address_line_2: String?,
    val address_landmark: String?,
    val address_country_id: String?,
    val address_country_name: String?,
    val address_state_id: String?,
    val address_state_name: String?,
    val address_city_id: String?,
    val address_city_name: String?,
    val address_pin: String?,
    val address_contact_name: String?,
    val address_mobile_number: String?,
    val address_secondary_contact: String?,
    val address_fax: String?,
    val address_email: String?,
    val address_notes: String?

)

