package com.goldbookapp.model

data class ItemUnitMenuModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(val unit: List<UnitMenu>) {
        data class UnitMenu(
            val id: Int,
            val name: String
        )
    }
}