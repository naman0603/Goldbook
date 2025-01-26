package com.goldbookapp.model

data class NatureOfGoodsModel(
    val code: Int,
    val data: List<DataNatureGood>,
    val message: String,
    val status: Boolean,
    val errormessage: LoginModel.Errormessage?
){
    data class DataNatureGood(
        val name: String,
        val nature_of_goods: String
    )
}