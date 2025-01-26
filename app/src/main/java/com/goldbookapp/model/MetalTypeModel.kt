package com.goldbookapp.model

data class MetalTypeModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(val metal_type: List<MetalType>) {
        data class MetalType(
            val id: Int,
            val name: String
        )
    }
}