package com.goldbookapp.model

data class SettingsGstDetailModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
){
    data class Data(
        val id: String?,
        val enable_gst: String?,
        val gstin: String?,
        val registration_date: String?,
        val periodicity_of_gst1: String?,
        val country_id: String?,
        val gst_state_id: String?,
        val state_name: String?
    )
}
