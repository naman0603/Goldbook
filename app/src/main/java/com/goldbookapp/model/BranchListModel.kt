package com.goldbookapp.model


data class BranchListModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(val branch: List<Branches>?,val other_branches:List<Branches>?) {
        data class Branches(
            val id: String?,
            val company_id: Number?,
            val branch_name: String?,
            val branch_code: Number?,
            val branch_address: String?,
            val branch_contact_no: String?,
            val secondary_contact: String?,
            val contact_person_fname: String?,
            val contact_person_lname: String?,
            val branch_email: String?,
            val branch_type: Number?,
            val gst_tin_number: String?,
            val country_id: Number?,
            val state_id: String?,
            val city_id: Number?,
            val area: String?,
            val landmark: String?,
            val pincode: String?,
            val created_at: String?,
            val updated_at: String?,
            val deleted_at: String?,
            val country_name: String?,
            val state_name: String?,
            val city_name: String?
        )
    }
}




