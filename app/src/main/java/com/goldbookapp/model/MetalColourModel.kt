package com.goldbookapp.model

data class MetalColourModel(
    val code: Int,
    val data: List<DataMetalColour>,
    val message: String,
    val status: Boolean,
    val errormessage: LoginModel.Errormessage?

){
    data class DataMetalColour(
        val colour_code: String,
        val colour_name: String,
        val metal_colour_id: String,
        val status: String
    )
}