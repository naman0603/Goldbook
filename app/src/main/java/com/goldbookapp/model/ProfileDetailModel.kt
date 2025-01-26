package com.goldbookapp.model

data class ProfileDetailModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {


    data class Data(val user: User?) {

        data class User(
            var name: String?,
            var date_of_birth: String?,
            var gender: String?,
            var mobile_no: String?,
            var email: String?,
            var imageurl: String?,
            var language: String?
        )
    }



}
//
//data class Base(val data: Data?, val message: String?, val code: Number?, val status: Boolean?)
//
//data class Data(val user: List<Any>?)

// result generated from /json
