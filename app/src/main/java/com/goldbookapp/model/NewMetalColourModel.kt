package com.goldbookapp.model

data class NewMetalColourModel (
    val data: Data?,
    val message: String?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
){
    data class Data(val company: List<Any>?)
}