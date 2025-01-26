package com.goldbookapp.model


data class NewItemModel(
    val code: Int,
    val data: List<Any>,
    val message: String,
    val status: Boolean,
    val errormessage: LoginModel.Errormessage?)

