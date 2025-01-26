package com.goldbookapp.model

data class MaintainStockModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(val maintain_stock_in: List<MaintainStock>) {
        data class MaintainStock(
            val id: Int,
            val name: String
        )
    }
}