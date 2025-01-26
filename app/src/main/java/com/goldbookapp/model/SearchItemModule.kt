package com.goldbookapp.model

data class SearchItemModule(
    val data: ArrayList<Data61082640>?,
    val message: String?,
    val code: String?,
    val total_page: Int?,
    val page_limit: Int?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data61082640(
        val item_stock_id: String,
        val item_id: String?,
        val item_name: String?,
        val stock_in_hand: String?,
        val total_less_wt: String?,
        val net_wt: String?,
        val touch: String?,
        val making_charge: String?,
        val wastage: Number?,
        val category: String?,
        val category_id: Number?,
        val unit_id: String?,
        val unit_name: String?,
        val item_stock_type: String?,
        val customer_code:String?,
        val series: String?,
        val transaction_date:String?,
        val calculation_units: List<Calculation1893561627>?,
        val less_wt:List<CalculationPaymentModel.DataPayment.ItemPayment.LessWeights>
    ){
        data class Calculation1893561627(val calculation_unit_id: String,
                                     val calculation_unit_name: String)
    }
  /*  val code: String,
    val data: ArrayList<Data61082640>,
    val message: String,
    val status: Boolean,
    val errormessage: LoginModel.Errormessage?
){
    data class Data61082640(
        val category_name: String,
        val color: List<Any>,
        val id: Int,
        val is_studded: Int,
        val item_name: String,
        val maintain_stock_in: String,
        val metal_type: String,
        val stamp: List<Stamp>,
        val unit_name: String,
        val use_gold_color: Int,
        val use_stamp: Int
    ){
        data class Stamp(
            val item_stamp: String
        )
    }

*/

}

