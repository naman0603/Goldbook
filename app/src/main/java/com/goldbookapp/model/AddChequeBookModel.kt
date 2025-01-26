package com.goldbookapp.model

class AddChequeBookModel : ArrayList<AddChequeBookModel.AddChequeBookModelItem>(){
    data class AddChequeBookModelItem(
        val chequeFrom: Int,
        val chequeName: String,
        val chequeTo: Int,
        val totalCheque: String
    )
}