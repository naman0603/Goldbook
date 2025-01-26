package com.goldbookapp.model

data class MultipleReceiptRefModel(
    val selectedReceiptRefList: List<TrasactionIdList>?
) {
    data class TrasactionIdList(val id: String?)
}