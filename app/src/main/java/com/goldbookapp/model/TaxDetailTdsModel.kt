package com.goldbookapp.model


data class TaxDetailTdsModel(
    val data: Data,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
){
    data class Data(
        val id: String?,
        val enable_tds: Int?,
        val tds_circle: String?,
        val tan_number: String?,
        val tds_deductor_type: String?,
        val tds_person_responsible: String?,
        val tds_designation: String?,
        val tds_contact_number: String?,
        val tds_deductor_type_name: String?,
        val nature_of_payment: List<Nature_of_payment>?
    ) {
        data class Nature_of_payment(
            val nature_of_payment_id: String?,
            val name: String?,
            val section: String?,
            val payment_code: String?,
            val rate_with_pan: String?,
            val rate_without_pan: String?,
            val rate_other_with_pan: String?,
            val rate_other_without_pan: String?
        )
    }
}