package com.goldbookapp.model

data class GetTcsCollectorTypeModel(
    val data: List<Data>?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
){
    data class Data(
        val artificial_juridical_person: String?,
        val association_of_persons: String?,
        val body_of_individuals: String?,
        val company_non_resident: String?,
        val company_resident: String?,
        val cooperative_society: String?,
        val government: String?,
        val individual_huf_non_resident: String?,
        val individual_huf_resident: String?,
        val local_authority: String?,
        val partnership_firm: String?
    )
}
