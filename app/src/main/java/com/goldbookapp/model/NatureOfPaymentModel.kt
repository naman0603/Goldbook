package com.goldbookapp.model

data class NatureOfPaymentModel(
    val code: Int,
    val data: List<DataNaturePayment>,
    val message: String,
    val status: Boolean,
    val errormessage: LoginModel.Errormessage?
){
    data class DataNaturePayment(
        val name: String,
        val nature_of_payment: String
    )
}