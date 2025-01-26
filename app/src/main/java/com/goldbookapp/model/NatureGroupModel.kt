package com.goldbookapp.model

data class NatureGroupModel(
    val code: Int,
    val `data`: List<DataNatureGroup>,
    val message: String,
    val status: Boolean,
    val errormessage: LoginModel.Errormessage?
){

    data class DataNatureGroup(
        val name: String,
        val nature_group_id: Int
    )
}