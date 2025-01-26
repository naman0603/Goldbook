package com.goldbookapp.model

data class MultipleVendorNewItemModel(
    val vendoridlist: List<PrefVendorList>?
) {
    data class PrefVendorList(val vendor_id: String?)
}