package com.goldbookapp.model

data class StateModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(val state: List<State693361839>?) {

        data class State693361839(
            val id: String?,
            val country_id: Number?,
            val name: String,
            val created_at: String?,
            val updated_at: String?,
            val deleted_at: Any?
        )
    }
}

