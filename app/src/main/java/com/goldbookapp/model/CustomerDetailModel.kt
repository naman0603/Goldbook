package com.goldbookapp.model

// result generated from /json

data class CustomerDetailModel(
    val customers: Customers1414856671?,
    val billing_address: BillingAddressModel?,
    val shipping_address: ShippingAddressModel?,
    /* val transaction: List<Any>?,*/
    val code: String?,
    val message: String?,
    val data: List<Any>?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {


    data class Customers1414856671(
        val id: String?,
        val contact_type: String?,
        val company_id: Number?,
        val customer_type: String?,
        val title: String?,
        val first_name: String?,
        val last_name: String?,
        val customer_code: String?,
        val company_name: String?,
        val mobile_number: String?,
        val secondary_contact: String?,
        val email: String?,
        val is_tds_applicable: String?,
        val tax_collector_type: String?,
        val nature_of_good_id: String?,
        val nature_of_good_name: String?,
        val tax_deductor_type: String?,
        val nature_of_payment_id: String?,
        val nature_of_payment_name: String?,
        val opening_fine_balance: String?,
        val fine_balance: String?,
        val opening_fine_default_term: String?,
        val fine_balance_type: String?,
        val opening_silver_fine_balance: String?,
        val opening_silver_fine_default_term: String?,
        val opening_cash_balance: String?,
        val cash_balance: String?,
        val opening_cash_default_term: String?,
        val cash_balance_type: String?,
        val display_fine_balance: String?,
        val display_silver_fine_balance: String?,
        val display_fine_default_term: String?,
        val display_silver_fine_default_term: String?,
        val opening_cash_balance_term: String?,
        val opening_fine_balance_term: String?,
        val opening_silver_fine_term: String?,
        val fine_limit: String?,
        val cash_limit: String?,
        val gst_register: String?,
        val gst_treatment: String?,
        val gst_tin_number: String?,
        val pan_number: String?,
        val website: String?,
        val is_shipping: String?,
        val status: String?,
        val display_name: String?,
        val notes: String?,
        val courier: String?,
        val created_at: String?,
        val updated_at: String?,
        val deleted_at: String?,
        val is_editable: String?,
        val is_tcs_applicable: String?
    )


}

data class Billing_address(
    val customer_id: Number?,
    val company_id: Number?,
    val location: String?,
    val area: String?,
    val pincode: String?,
    val mobile_no: String?,
    val secondary_no: String?,
    val fax_no: String?,
    val landmark: String?,
    val country_id: String?,
    val country_name: String?,
    val state_id: String?,
    val state_name: String?,
    val city_id: String?,
    val city_name: String?
)

data class Customers(
    val id: Number?,
    val contact_type: String?,
    val customer_type: String?,
    val company_id: Number?,
    val title: String?,
    val first_name: String?,
    val last_name: String?,
    val customer_code: String?,
    val company_name: String?,
    val mobile_number: String?,
    val secondary_contact: String?,
    val email: String?,
    val is_tcs_applicable: String?,
    val is_tds_applicable: String?,
    val tax_collector_type: String?,
    val nature_of_good_id: String?,
    val nature_of_good_name: String?,
    val tax_deductor_type: String?,
    val nature_of_payment_id: String?,
    val nature_of_payment_name: String?,
    val gst_register: String?,
    val gst_treatment: String?,
    val gst_tin_number: String?,
    val fine_limit: String?,
    val cash_limit: String?,
    val website: String?,
    val pan_number: String?,
    val is_shipping: String?,
    val status: String?,
    val display_name: String?,
    val notes: String?,
    val courier: String?,
    val is_editable: String?,
    val created_at: String?,
    val updated_at: String?,
    val deleted_at: String?,
    val cash_balance: String?,
    val fine_balance: String?,
    val opening_cash_balance: String?,
    val opening_cash_default_term: String?,
    val opening_fine_balance: String?,
    val opening_fine_default_term: String?,
    val cash_balance_type: String?,
    val fine_balance_type: String?
)





