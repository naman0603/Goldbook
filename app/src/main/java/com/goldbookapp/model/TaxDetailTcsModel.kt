package com.goldbookapp.model

data class TaxDetailTcsModel(
    val data: Data,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
){
    data class Data(
        val id: String?,
        val enable_tcs: Int?,
        val tds_circle: String?,
        val tan_number: String?,
        val tcs_collector_type: String?,
        val tcs_person_responsible: String?,
        val tcs_designation: String?,
        val tcs_contact_number: String?,
        val tcs_collector_type_name: String?,
        val nature_of_goods: List<Nature_of_goods>?) {
        data class Nature_of_goods(
            val nature_of_goods_id: String?,
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