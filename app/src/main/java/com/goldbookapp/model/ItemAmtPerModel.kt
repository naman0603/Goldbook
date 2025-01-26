package com.goldbookapp.model

data class ItemAmtPerModel(
    val data: ArrayList<DataItemAmtPer>

)
{
    data class DataItemAmtPer(
        val unit: String,
        val maintain_stock_in: String,
        val fix: String
    )
}




