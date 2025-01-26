package com.goldbookapp.model


data class SaleDetailModel(
    val data: Data?,
    val message: String?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {

    data class CgstData(
        val title: String?,
        val tax_percentage: String?,
        val ledger_id: String?,
        val ledger_name: String?,
        val tax_amount: String?
    )

    data class Charges_array983149595(
        val amount: String?,
        val unit_id: String?,
        val unit_name: String?,
        val label: String?
    )

    data class Data(val transactionData: TransactionData?, val IRTData: List<IRTData490504599>?)

    data class IRTData490504599(
        val IRTDetails: IRTDetails?,
        val type: String?,
        val transaction_type: String?,
        val transaction_title: String?,
        val fine_wt: String?,
        val amount: String?
    )

    data class IRTDetails(
        val cash_amount: String?,
        val cash_ledger: String?,
        val cash_ledger_name: String?,
        val cash_description: String?,
        val bank_amount: String?,
        val bank_ledger: String?,
        val bank_ledger_name: String?,
        val bank_mode: String?,
        val cheque_number: String?,
        val cheque_date: String?,
        val favouring_name: String?,
        val deuct_charges: String?,
        val deuct_charges_percentage: String?,
        val bank_final_amt: String?,
        val recipient_bank: String?,
        val account_no: String?,
        val ifs_code: String?,
        val utr_number: String?,
        val bank_description: String?,
        val metal_type_id_rate_cut: String?,
        val rate_cut_fine: String?,
        val rcm_gold_rate: String?,
        val rate_cut_amount: String?,
        val rate_cut_fine_term: String?,
        val item_id: String?,
        val item_name: String?,
        val metal_type_id_metal: String?,
        val maintain_stock_in_name_metal: String?,
        val gross_wt: String?,
        val less_wt: String?,
        val net_wt: String?,
        val touch: String?,
        val wast: String?,
        val fine_wt: String?,
        val adjustment_fine: String?,
        val metal_type_id_adjustments: String?,
        val adjustment_amount: String?,
        val adjustment_ledger: String?,
        val adjustment_ledger_name: String?,
        val adjustment_description: String?,
        val type: String?,
        val transaction_type: String?,
        val transaction_title: String?
    )




    data class IgstData(
        val title: String?,
        val tax_percentage: String?,
        val ledger_id: String?,
        val ledger_name: String?,
        val tax_amount: String?
    )

    data class Item1427117511(
        val item_id: String,
        val item_name: String,
        val item_quantity: String,
        val item_size: String,
        val item_gross_wt: String,
        val item_less_wt: String,
        val item_net_wt: String,
        val item_touch: String,
        val item_wastage: String,
        val item_fine_wt: String,
        val item_total: String,
        val item_remarks: String,
        val item_unit_id: String,
        val item_unit_name: String,
        val item_use_stamp: String,
        val item_stamp_id: String,
        val item_stamp_name: String,
        val item_use_gold_color: String,
        val item_gold_color_id: String,
        val item_gold_color_name: String,
        val item_metal_type_id: String,
        val item_metal_type_name: String,
        val item_maintain_stock_in_id: String,
        val item_maintain_stock_in_name: String,
        val item_rate: String,
        val item_rate_on: String,
        val item_charges: String,
        val item_discount: String,
        val item_type: String,
        val tag_no: String,
        val random_tag_id: String,
        val item_is_studded: String,
        val item_wt_breakup: OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup,
        val item_charges_breakup: OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup,
        val item_unit_array: List<ItemSearchModel.ItemSearch.Unit_array>,
        val item_tax_preference: String,
        val item_sales_purchase_hsn: String,
        val item_sales_purchase_gst_rate: String,
        val item_gst_rate_percentage: String,
        val item_sales_making_charges: String,
        val item_sales_rate: String,
        val item_sales_ledger_id: String,
        val item_sales_ledger_name: String,
        val item_purchase_making_charges: String,
        val item_purchase_rate: String,
        val item_purchase_ledger_id: String,
        val item_purchase_ledger_name: String,
        val tax_analysis_array: OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem.Tax_analysis_array
    )

    data class Item_charges_breakup(
        val making_charge_array: Making_charge_array?,
        val charges_array: List<Charges_array983149595>?,
        val total_charges: String?
    )

    data class Item_unit_array1388883558(val id: String?, val text: String?)

    data class Item_wt_breakup(
        val less_wt_array: List<Less_wt_array503027708>?,
        val total_less_wt: String?,
        val total_less_wt_amount: String?
    )

    data class Less_wt_array503027708(
        val less_wt_item_id: String?,
        val less_wt_item_name: String?,
        val less_wt_pieces: String?,
        val less_wt_weight: String?,
        val less_wt_less_wt: String?,
        val less_wt_variation: String?,
        val less_wt_final_wt: String?,
        val less_wt_item_rate: String?,
        val less_wt_item_per: String?,
        val less_wt_item_per_name: String?,
        val less_wt_item_amount: String?,
        val less_wt_lbr_rate: String?,
        val less_wt_lbr_per: String?,
        val less_wt_lbr_per_name: String?,
        val less_wt_lbr_amount: String?,
        val less_wt_total_amount: String?,
        val label: String?,
        val less_wt_maintain_stock_in_name: String?,
        val less_wt_unit_array: List<Less_wt_unit_array800165465>?
    )

    data class Less_wt_unit_array800165465(val id: String?, val text: String?)

    data class Making_charge_array(
        val amount: String?,
        val unit_id: String?,
        val unit_name: String?
    )

    data class RoundOffLedgerData(
        val title: String?,
        val tax_percentage: String?,
        val ledger_id: String?,
        val ledger_name: String?,
        val tax_amount: String?
    )

    data class SgstData(
        val title: String?,
        val tax_percentage: String?,
        val ledger_id: String?,
        val ledger_name: String?,
        val tax_amount: String?
    )

    data class Tax_analysis_array(
        val item_id: String?,
        val item_name: String?,
        val ledger_id: String?,
        val ledger_name: String?,
        val taxable_amount: String?,
        val hsn: String?,
        val gst_rate: String?,
        val gst_rate_percentage: String?,
        val igst_amount: String?,
        val cgst_amount: String?,
        val sgst_amount: String?
    )

    data class TcsData(
        val title: String?,
        val tax_percentage: String?,
        val ledger_id: String?,
        val ledger_name: String?,
        val tax_amount: String?
    )

    data class TdsData(
        val title: String?,
        val tax_percentage: String?,
        val ledger_id: String?,
        val ledger_name: String?,
        val tax_amount: String?
    )

    data class TransactionData(
        val id: Number?,
        val transaction_id: String?,
        val transaction_type_id: String?,
        val transaction_type_name: String?,
        val transaction_date: String?,
        val customer_code: String?,
        val display_name: String?,
        val contact_id: String?,
        val vendor_bill_number: String?,
        val vendor_bill_date: String?,
        val is_gst_applicable: String?,
        val place_of_supply_id: String?,
        val place_of_supply: String?,
        val party_po_no: String?,
        val tds_tcs_enable: String?,
        val reference: String?,
        val remarks: String?,
        val is_tcs_applicable: String?,
        val is_tds_applicable: String?,
        val invoice_number: String?,
        val image: List<Images>?,
        val item: ArrayList<Item1427117511>?,
        val sgstData: SgstData?,
        val cgstData: CgstData?,
        val igstData: IgstData?,
        val tcsData: TcsData?,
        val tdsData: TdsData?,
        val roundOffLedgerData: RoundOffLedgerData?,
        val is_show_round_off: String?,
        val igst_amount: String?,
        val cgst_amount: String?,
        val sgst_amount: String?,
        val total_net_wt_with_IRT: String?,
        val total_fine_wt_with_IRT: String?,
        val total_fine_wt_with_IRT_short_term: String?,
        val total_fine_wt_with_IRT_term: String?,
        val total_silver_fine_wt_with_IRT: String?,
        val total_silver_fine_wt_with_IRT_short_term: String?,
        val total_silver_fine_wt_with_IRT_term: String?,
        val total_amount_with_IRT: String?,
        val total_quantity: String?,
        val total_gross_wt: String?,
        val total_less_wt: String?,
        val total_net_wt: String?,
        val total_fine_wt: String?,
        val total_misc_charges: String?,
        val tcs_tds_taxable_amount: String?,
        val tcs_amount: String?,
        val tcs_percentage: String?,
        val tds_amount: String?,
        val tds_percentage: String?,
        val sub_total: String?,
        val round_off_total: String?,
        val total_amount: String?,
        val grand_total: String?,
        val grand_total_short_term: String?,
        val grand_total_term: String?,
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
        val final_total_amount: String?,
        val opening_cash_balance: String?,
        val opening_cash_balance_short_term: String?,
        val opening_cash_balance_term: String?,
        val running_cash_balance: String?,
        val running_cash_balance_short_term: String?,
        val running_cash_balance_term: String?,
        val closing_cash_balance: String?,
        val closing_cash_balance_short_term: String?,
        val closing_cash_balance_term: String?,
        val opening_fine_balance: String?,
        val opening_fine_balance_short_term: String?,
        val opening_fine_balance_term: String?,
        val opening_silver_fine_balance: String?,
        val opening_silver_fine_balance_short_term: String?,
        val opening_silver_fine_balance_term: String?,
        val running_fine_balance: String?,
        val running_fine_balance_short_term: String?,
        val running_fine_balance_term: String?,
        val closing_fine_balance: String?,
        val closing_fine_balance_short_term: String?,
        val closing_fine_balance_term: String?,
        val closing_silver_fine_balance: String?,
        val closing_silver_fine_balance_short_term: String?,
        val closing_silver_fine_balance_term: String?
    ){
        data class Images(val image_id: String?, val image: String?)
    }


}




