package com.goldbookapp.model

data class EditItemCatModel(

    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
){
    data class Data(val item: List<Any>?)
}




