package com.goldbookapp.model

data class MultipleColorNewItemModel(
    val coloridlist: List<PrefColorList>?
) {
    data class PrefColorList(val colour_id: String?)
}