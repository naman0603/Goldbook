package com.goldbookapp.model

data class CityModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {

    data class Data(val city: List<City1394158508>?) {


        data class City1394158508(
            val id: String?,
            val state_id: Number?,
            val name: String,
            val created_at: String?,
            val updated_at: String?,
            val deleted_at: Any?
        )
    }
}
