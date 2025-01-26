package com.goldbookapp.model

data class UserCompanyListModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?

) {
    data class Data(val company: List<Company968753762>?)

    data class Company968753762(
        val id: Number?,
        val user_id: Number?,
        val company_id: String?,
        val created_at: String?,
        val updated_at: String?,
        val company_name: String?,
        val country_id: Number?,
        val reg_address: String?,
        val area: String?,
        val landmark: String?,
        val state_id: Number?,
        val city_id: Number?,
        val postal_code: String?,
        val contact_person_first_name: String?,
        val contact_person_last_name: String?,
        val mobile_number: String?,
        val alternate_number: String?,
        val email: String?,
        val fiscal_year_id: Number?,
        val pan_number: String?,
        val cin_number: String?,
        val default_term_balance: String?,
        val lock_status: Number?,
        val gold_rate: Number?,
        val status: Number?,
        val step: Number?,
        val language: String?,
        val default_branch_id: Number?,
        val deleted_at: Any?
    )


}


// result generated from /json

//data class Base(val data: Data?, val code: Number?, val message: String?, val status: Boolean?)
//
//
//data class Data(val company: List<Any>?)