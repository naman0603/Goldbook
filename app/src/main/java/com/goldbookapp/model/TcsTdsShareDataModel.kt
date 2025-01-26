package com.goldbookapp.model

data class TcsTdsShareDataModel(
    val is_tcs_applicable: String?,
    val is_tds_applicable: String?,
    val selectedDeductorType: String?,
    val selectedCollectorType: String?,
    val selectedNogType: String?,
    val selectedNopType: String?,
    val selectedNatureofPaymentID: String?,
    val selectedNatureofGoodsID: String?
)