import com.goldbookapp.model.*

/*
data class OpeningStockDetailModel(
    val data: Dataa,
    val message: String?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Dataa(
        val id: Int,
        val invoice_number: String,
        val item: List<Item>,
        val transaction_date: String,
        val transaction_id: String,
        val transaction_type_id: Int,
        val transaction_type_name: String
    ) {
        data class Item(
            val item_charges: Int,
            val item_charges_breakup: ItemChargesBreakup,
            val item_fine_wt: String,
            val item_gold_color_id: Int,
            val item_gold_color_name: String,
            val item_gross_wt: String,
            val item_id: Int,
            val item_is_studded: Int,
            val item_less_wt: String,
            val item_maintain_stock_in_id: Int,
            val item_maintain_stock_in_name: String,
            val item_metal_type_id: Int,
            val item_metal_type_name: String,
            val item_name: String,
            val item_net_wt: String,
            val item_quantity: Int,
            val item_rate: String,
            val item_remarks: String,
            val item_stamp_id: Int,
            val item_stamp_name: String,
            val item_total: String,
            val item_touch: String,
            val item_type: String,
            val item_unit_array: List<ItemSearchModel.ItemSearch.Unit_array>,
            val item_unit_id: Int,
            val item_unit_name: String,
            val item_use_gold_color: Int,
            val item_use_stamp: Int,
            val item_wastage: String,
            val item_wt_breakup: ItemWtBreakup
        ) {
            data class ItemChargesBreakup(
                val charges_array: ChargesArray,
                val index: Int,
                val making_charge_array: MakingChargeArray,
                val total_charges: Int
            ) {
                data class ChargesArray(
                    val other_charge_0: OtherCharge0
                ) {
                    data class OtherCharge0(
                        val amount: Int,
                        val label: String,
                        val unit_id: String,
                        val unit_name: String
                    )
                }

                data class MakingChargeArray(
                    val amount: Int,
                    val unit_id: String,
                    val unit_name: String
                )
            }


            data class ItemWtBreakup(
                val index: Int,
                val less_wt_array: List<AddLessWeightModel.AddLessWeightModelItem>,
                val total_less_wt: Double,
                val total_less_wt_amount: Double
            )

        }
    }
}
*/
// result generated from /json

data class OpeningStockDetailModel(
    val data: OpeningStock?,
    val code: Number?,
    val message: String?,
    val status: Boolean?
) {

    data class OpeningStock(
        val id: Number?,
        val transaction_id: String?,
        val transaction_type_id: Number?,
        val transaction_type_name: String?,
        val transaction_date: String?,
        val reference: String?,
        val remarks: String?,
        val invoice_number: String?,
        val item: ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>?,
        val total_gross_wt: String?,
        val total_less_wt: String?,
        val total_net_wt: String?,
        val total_fine_wt: String?,
        val net_fine_due: String?,
        val total_misc_charges: String?,
        val total_quantity: String?,
        val sub_total: String?,
        val total_amount: String?,
        val silver_total_quantity: String?,
        val silver_total_gross_wt: String?,
        val silver_total_less_wt: String?,
        val silver_total_net_wt: String?,
        val silver_total_fine_wt: String?,
        val silver_total_misc_charges: String?,
        val silver_total_amount: String?,
        val other_total_quantity: String?,
        val other_total_gross_wt: String?,
        val other_total_net_wt: String?,
        val other_total_misc_charges: String?,
        val other_total_amount: String?,
    )


}

