package com.goldbookapp.model

data class GetUserCompaniesModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(val company: List<Companies>?) {
        data class Companies(
            val id: String?,
            val user_id: Number?,
            val company_id: Number?,
            val created_at: String?,
            val updated_at: String?,
            val company_name: String?,
            val country_id: String?,
            val reg_address: String?,
            val area: String?,
            val landmark: String?,
            val state_id: String?,
            val city_id: String?,
            val postal_code: String?,
            val contact_person_first_name: String?,
            val contact_person_last_name: String?,
            val mobile_number: String?,
            val alternate_number: String?,
            val email: String?,
            val fiscal_year_id: String?,
            val pan_number: String?,
            val cin_number: String?,
            val default_term_balance: String?,
            val lock_status: Number?,
            val gold_rate: Number?,
            val status: Number?,
            val step: Number?,
            val language: String?,
            val default_branch_id: Number?,
            val deleted_at: String?
        )
    }

}


data class Base(val message: String?, val data: Data?, val code: Number?, val status: Boolean?)

data class Company657077117(val company_id: Number?, val id: Number?, val company_name: String?, val country_id: Number?, val reg_address: String?, val area: String?, val landmark: String?, val state_id: Number?, val city_id: Number?, val postal_code: String?, val contact_person_first_name: String?, val contact_person_last_name: String?, val mobile_number: String?, val alternate_number: String?, val email: String?, val fiscal_year_id: Any?, val pan_number: String?, val cin_number: Any?, val default_term_balance: String?, val lock_status: Number?, val gold_rate: Number?, val status: Number?, val step: Number?, val language: String?, val default_branch_id: Number?, val gst_register: Number?, val gst_tin_number: String?, val created_at: String?, val updated_at: String?, val deleted_at: Any?, val date_format_id: Any?, val date_separator_id: Any?, val logo: Any?, val theme_set: Any?)


