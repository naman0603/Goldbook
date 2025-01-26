
import com.goldbookapp.model.CalculateSalesModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.SaleDetailModel

data class PurchaseDetailModel(
    val data: List<DataP>?,
    val message: String?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class DataP(
        val id: String?,
        val transaction_id: String?,
        val vendor_bill_number: String?,
        val vendor_bill_date: String?,
        val prefix: String?,
        val series: String?,
        val invoice_number: String?,
        val transaction_date: String?,
        val total_fine_wt: String?,
        val is_gst_applicable: String?,
        val place_of_supply: String?,
        val place_of_supply_id: String?,
        val total_gross_wt: String?,
        val total_less_wt: String?,
        val total_net_wt: String?,
        val total_misc_charges: String?,
        val rate_cut_amount: String?,
        val net_fine_due: String?,
        val gold_rate: String?,
        val sub_total: String?,
        val tcs_amount: String?,
        val tcs_percentage: String?,
        val round_off_total: String?,
        val grand_total: String?,
        val is_rate_cut: String?,
        val rate_cut_wt: String?,
        val remarks: String?,
        val status: String?,
        val signature_verify: String?,
       /* val vendor_id: String?,
        val vendor_name : String?,*/
        val contact_name:String?,
        val contact_id: String?,
        val no_of_items : String?,
        val item: List<String/*CalculateSalesModel.Datac.Item558106789*/>?,
        val tax: List<TaxDetailP>?,
        val image: List<ImageP>?,
        val opening_cash_balance: String?,
        val opening_fine_balance: String?,
        val closing_cash_balance: String?,
        val closing_fine_balance: String?,
        val opening_cash_default_term: String?,
        val opening_fine_default_term: String?,
        val closing_cash_default_term: String?,
        val closing_fine_default_term: String?
       /* val sgst: SaleDetailModel.Data1930630491.SGSTLedger,
        val cgst: SaleDetailModel.Data1930630491.CGSTLedger,
        val igst: SaleDetailModel.Data1930630491.IGSTLedger,
        val tds: SaleDetailModel.Data1930630491.TCSLedger,
        val round_off: SaleDetailModel.Data1930630491.RoundOffLedger*/
    )
    {
        /*data class ItemP(
            val transaction_item_id: Number?,
            val item_id: Number?,
            val item_name: String?,
            val unit_id: Number?,
            val unit_name: String?,
            val quantity: Number?,
            val gross_wt: Number?,
            val less_wt: Number?,
            val net_wt: Number?,
            val touch: Number?,
            val wastage: Number?,
            val fine_wt: Number?,
            val remarks: String?,
            val cost: String?,
            val charge: List<ChargesP>?
        )
        {
            data class ChargesP(
                val transaction_item_charge_id: Number?,
                val transaction_item_id: Number?,
                val name: String?,
                val amount: Number?,
                val total_amount: String?,
                val calculation_unit_id: Number?,
                val calculation_unit_name: String?
            )
        }*/
        data class TaxDetailP(val tax_name: String?, val tax_percentage: Number?, val tax_amount: Number)

        data class ImageP(val transaction_image_id: Number?, val image: String?)

    }

}