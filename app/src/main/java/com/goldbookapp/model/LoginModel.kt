package com.goldbookapp.model
// result generated from /json

data class LoginModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: Errormessage?
) {
    data class Data(
        val token_type: String?,
        val expires_in: Number?,
        var bearer_access_token: String?,
        var access_token: String?,
        val refresh_token: String?,
        var user_info: User_info?,
        val step: String?,
        var company_info: Company_info?,
        var branch_info: Branch_info?
    ) {
        data class Company_info(
            val id: String?,
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
            var gold_rate: Gold_rate?,
            val status: Number?,
            val step: Number?,
            val language: String?,
            val default_branch_id: String?,
            val gst_register: String?,
            val gst_tin_number: String?,
            val created_at: String?,
            val updated_at: String?,
            val deleted_at: String?,
            val country_name: String?,
            val state_name: String?,
            val city_name: String?,
            var general_settings : General_settings?,
            var tax_settings : Tax_settings?,
            var contact_settings : Contact_settings?

        ){
            data class Gold_rate(var bill_rate_amount: String?,
                                 var cash_rate_amount: String?,
                                 var type: String?)
            data class General_settings(var add_field_for_salesperson: String?,
                                        var enable_cheque_reg_for_bank_acc: String?,
                                        var round_off_for_sales: String?,
                                        var default_term: String?,
                                        var debit_term: String?,
                                        var debit_short_term: String?,
                                        var credit_term: String?,
                                        var credit_short_term: String?,
                                        var print_copies: String?
            )
            data class Tax_settings(var enable_gst: String?,
                                 var enable_tds: String?,
                                 var enable_tcs: String?)

            data class Contact_settings(var disable_credit_limit: String?,
                                    var stop_transaction_if_limit_over: String?)

        }
       /* data class Company_info(
            val id: String?,
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
        )*/

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
            val updated_at: String?,
            val pin: String?
        )
        data class Branch_info(
            val id: String?,
            val company_id: Number?,
            val branch_name: String?,
            val branch_code: String?,
            val branch_address: String?,
            val branch_contact_no: String?,
            val secondary_contact: String?,
            val contact_person_fname: String?,
            val contact_person_lname: String?,
            val branch_email: String?,
            val branch_type: String?, // 0 - NonGST, 1 - GST
            val gst_tin_number: String?,
            val country_id: Number?,
            val state_id: Number?,
            val city_id: Number?,
            val area: String?,
            val landmark: String?,
            val pincode: String?,
            val created_at: String?,
            val updated_at: String?,
            val deleted_at: String?
        )
    }
    data class Errormessage(
        val message: String?
    )
}









