import com.goldbookapp.model.*

data class PaymentDetailModel(
    val data: List<PaymentDetail>?,
    val message: String?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class PaymentDetail(
        val id: String?,
        val transaction_id: String?,
        val prefix: String?,
        val series: String?,
        val suffix: String?,
        val invoice_number: String?,
        val transaction_date: String?,
        val total_fine_wt: String?,
        val is_gst_applicable: String?,
        val place_of_supply: String?,
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
        val contact_id: String?,
        val contact_name: String?,
        val place_of_supply_id: String?,
        val no_of_items: String?,
        val opening_cash_balance: String?,
        val opening_fine_balance: String?,
        val closing_cash_balance: String?,
        val closing_fine_balance: String?,
        val opening_cash_default_term: String?,
        val opening_fine_default_term: String?,
        val closing_cash_default_term: String?,
        val closing_fine_default_term: String?,
        val is_reference: String?,
        val item: List<CalculationPaymentModel.DataPayment.ItemPayment>?,
        val reference: ArrayList<MultiplePaymentRefModel.TrasactionIdList>?,
        val reference_invoice_number:ArrayList<String>,
        val image: List<Image>?
    )
    {
        data class Image(val transaction_image_id: String?, val image: String?)

    }

}
/*
// result generated from /json

data class Base(val data: List<Data367661042>?, val message: String?, val code: Number?, val status: Boolean?)

data class Data367661042(val id: Number?, val transaction_id: String?, val prefix: String?, val series: String?, val suffix: String?, val invoice_number: String?, val transaction_date: String?, val total_fine_wt: String?, val is_gst_applicable: Number?, val place_of_supply: Any?, val total_gross_wt: String?, val total_less_wt: String?, val total_net_wt: String?, val total_misc_charges: String?, val rate_cut_amount: Any?, val net_fine_due: String?, val gold_rate: Any?, val sub_total: Any?, val tcs_amount: Any?, val tcs_percentage: Any?, val round_off_total: Any?, val grand_total: String?, val is_rate_cut: Number?, val rate_cut_wt: Any?, val remarks: Any?, val status: String?, val signature_verify: Number?, val customer_id: Number?, val customer_name: String?, val place_of_supply_id: Any?, val no_of_items: Number?, val opening_cash_balance: String?, val opening_fine_balance: String?, val closing_cash_balance: String?, val closing_fine_balance: String?, val opening_cash_default_term: String?, val opening_fine_default_term: String?, val closing_cash_default_term: String?, val closing_fine_default_term: String?, val is_reference: String?, val item: List<Item1648574423>?, val refer: List<List<Refer1714129848593757875>>?, val image: List<Image2000994460>?)

data class Image2000994460(val transaction_image_id: Number?, val image: String?)

data class Item1648574423(val transaction_item_id: Number?, val item_id: Any?, val item_stock_id: Any?, val item_name: String?, val unit_id: Any?, val unit_name: Any?, val quantity: Any?, val gross_wt: Any?, val less_wt: List<Any>?, val net_wt: Any?, val touch: String?, val wastage: Any?, val fine_wt: String?, val amount: String?, val mode: Any?, val rate_cut_type: String?, val item_type: Any?, val ledger_id: Any?, val recipient_bank_name: Any?, val recipient_account_number: Any?, val ifsc_code: Any?, val instrument_number: Any?, val remarks: String?, val ledger_name: String?, val cost: String?)

*/

// result generated from /json

