package com.goldbookapp.model


data class SetPinModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?
){
    data class Data(val special: Boolean?)
}


