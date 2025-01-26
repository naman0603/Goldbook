package com.goldbookapp.model

data class VerifyPhoneOTPModel(
    val data: Data?,
    val message: String?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(val company: Company?, val user_info: User_info?) {
        data class Company(val company_id: String?, val step: Number?)
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
            val branch_id: String?,
            val user_type: String?,
            val birthdate: String?,
            val gender: String?,
            val profile_image: String?,
            val default_company_id: Number?,
            val created_at: String?,
            val updated_at: String?,
            val token: Token?
        ) {
            data class Token(
                val token_type: String?,
                val expires_in: Number?,
                val access_token: String?,
                val refresh_token: String?
            )
        }
    }
}








