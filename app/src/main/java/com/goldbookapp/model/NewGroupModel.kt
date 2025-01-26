package com.goldbookapp.model

data class NewGroupModel (
   /* val data: Data?,
    val message: String?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
){
    data class Data(val group: List<Any>?)
}*/
    val code: Int,
    val data: List<Any>,
    val message: String,
    val status: Boolean,
    val errormessage: LoginModel.Errormessage?)