package com.goldbookapp.model


    data class AddAccountInfoModel(
        val taxPreference: String,
        val salePurGst: String,
        val salepurHsn: String,
        val jobLabourGst: String,
        val jobLaburSac: String,
        val wastageSales: String,
        val makingchargeSales: String,
        val salesLedger: String,
        val wastagePurchase: String,
        val makingchargepurchase: String,
        val purchaseLedger: String,
        val jobworkRate: String,
        val jobworkLedger:String,
        val labourRate:String,
        val labourLedger:String,
        val salesRate : String,
        val purchaseRate : String,
        val salesLedgerName : String,
        val purchaseLedgerName: String,
        val jobwrkLedgerName:String,
        val labourLedgerName : String,
        val salePurGstName : String,
        val jobLabourGstName: String
        /*val discountLedger: String,
        val discountLedgerName: String*/


    )
