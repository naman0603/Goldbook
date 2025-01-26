//package com.goldbookapp.model
//
//data class OpeningStockModel(
//    var quantity: String?,
//    var unit: String?,
//    var gross_wt: String?,
//    var less_wt: String?,
//    var net_wt: String?,
//    var touch: String?,
//    var fine_wt: String?
//)


package com.goldbookapp.model

data class OpeningStockModel(
    var opening_stocks: ArrayList<Openingstock>
) {
    data class Openingstock(
        var quantity: String?,
        var unit_name: String?,
        var gross_wt: String?,
        var less_wt: String?,
        var net_wt: String?,
        var touch: String?,
        var fine_wt: String?
    )
}
