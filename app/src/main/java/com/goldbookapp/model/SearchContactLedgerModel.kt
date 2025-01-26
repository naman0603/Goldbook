package com.goldbookapp.model

data class SearchContactLedgerModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(val contact: List<Contact>?) {
        data class Contact(
            /* val full_name: String?,
             val vendor_id: String?,
             val gst_register: Number?,
             val place_of_supply: String?,
             val state_id: String?,
             val is_gst_applicable: Boolean?*/
            val contact_type: String?,
            val contact_name: String?,
            val contact_id: String?

        )
    }
}
