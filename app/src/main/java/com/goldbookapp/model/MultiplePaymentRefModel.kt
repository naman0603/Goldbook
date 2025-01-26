package com.goldbookapp.model

data class MultiplePaymentRefModel(
    val selectedPayRefList: List<TrasactionIdList>?
) {
    data class TrasactionIdList(val id: String?)
}