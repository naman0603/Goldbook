package com.goldbookapp.model

// result generated from /json

data class CompanySetupModel(
    val message: String?,
    val data: Data?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(val company_info: Company_info?, val user_info: User_info?) {
        data class Company_info(
            val id: Number?,
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
            val created_at: String?,
            val updated_at: String?,
            val deleted_at: String?
        )

        data class User_info(
            val id: Number?,
            val name: String?,
            val username: String?,
            val email: String?,
            val email_verified_at: String?,
            val mobile_no: String?,
            val is_verified: Number?,
            val is_password_verify: Number?,
            val company_id: Number?,
            val branch_id: Number?,
            val user_type: String?,
            val birthdate: String?,
            val gender: String?,
            val profile_image: String?,
            val default_company_id: String?,
            val created_at: String?,
            val updated_at: String?
        )
    }
}








