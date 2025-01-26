package com.goldbookapp.model

data class BranchDetailModel(
    val message: String?,
    val data: Data?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {

    data class Data(
        val id: String?,
        val branch_name: String?,
        val branch_code: String?,
        var branch_address: String?,
        val branch_contact_no: String?,
        val secondary_contact: String?,
        val contact_person_fname: String?,
        val contact_person_lname: String?,
        val branch_email: String?,
        val gst_tin_number: String?,
        val branch_type: String?,
        val country_id: String?,
        val country_name: String?,
        val state_id: String?,
        val state_name: String?,
        val city_id: String?,
        val city_name: String?,
        var area: String?,
        var landmark: String?,
        var pincode: String?,
        var status:String?
    )

}


