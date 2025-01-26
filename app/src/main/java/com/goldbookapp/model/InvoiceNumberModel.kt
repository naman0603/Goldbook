package com.goldbookapp.model

data class InvoiceNumberModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(
      /*  val prefix: String?,*/
        val series: String?
      /*  val suffix: String?*/)
}