package com.goldbookapp.model

data class GetUserCompanyModel(
    val message: String?,
    val data: Data?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {

    data class Data(
        val id: Number?,
        val company_name: String?,
        val country_id: String?,
        val country_name: String?,
        val country_code: Number?,
        val reg_address: String?,
        val area: String?,
        val landmark: String?,
        val state_id: String?,
        val state_name: String?,
        val city_id: Number?,
        val city_name: String?,
        val postal_code: String?,
        val contact_person_first_name: String?,
        val contact_person_last_name: String?,
        val mobile_number: String?,
        val alternate_number: String?,
        val email: String?,
        val fiscal_year_id: String?,
        val fiscal_year_name: String?,
        val date_from: String?,
        val date_to: String?,
        val pan_number: String?,
        val cin_number: String?,
        val default_term_balance: String?,
        val lock_status: Number?,
        val gst_register: String?,
        val gst_tin_number: String?
    )
}





