package com.goldbookapp.model


data class CustomPinModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
){
    data class Data(val special: Boolean?)
}



/*
// result generated from /json

data class Base(val data: Data?, val code: Number?, val message: String?, val status: Boolean?)

data class Data(val special: Boolean?)
*/
