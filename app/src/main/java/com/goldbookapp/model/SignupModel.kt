package com.goldbookapp.model


data class SignupModel(
    val data: Data?,
    val message: String?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(val user: User?) {
        data class User(val OTP: Number?, val userInfo: UserInfo?){
            data class UserInfo(
                var bearer_access_token: String?,
                var access_token: String?,
                var company_id: String?,
                var name: String?,
                var username: String?,
                var email: String?,
                var password: String?,
                var mobile_no: String?,
                var step: String?
            )
        }
    }

    constructor(): this(Data(Data.User(1, Data.User.UserInfo("", "", "", "", "", "", "","",""))),
        "",
        "-1",
        true,
        null)
}


// result generated from /json
//
//data class Base(val data: Data?, val status: Boolean?, val message: String?, val code: Number?)
//
//data class Data(val user: User?)
//
//data class User(val OTP: Number?, val userInfo: UserInfo?)
//
//data class UserInfo(val name: String?, val email: String?, val password: String?, val mobile_no: String?, val username: String?, val step: Number?)
//
//



