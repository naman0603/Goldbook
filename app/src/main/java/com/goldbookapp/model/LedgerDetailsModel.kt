package com.goldbookapp.model

/*data class LedgerDetailsModel(
    val data: DataLedger,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage
){
    data class DataLedger(
        val ledger: Ledger
    ){
        data class Ledger(
            val enableGst: Int,
            val enableTcs: Int,
            val enableTds: Int,
            val ledgerClosingBalance: LedgerClosingBalance,
            val ledgerData: LedgerData
        )  {
            data class LedgerClosingBalance(
                val amount_default_short_term: String,
                val amount_default_term: String,
                val total_balance: Int
            )
            data class LedgerData(
                val account_String: String,
                val bank_name: String,
                val bill_by_bill_reference: String,
                val branch_name: String,
                val cheque_register: List<Any>,
                val code: String,
                val company_id: Int,
                val created_at: String,
                val created_by: Int,
                val goods_name: String,
                val group_id: Int,
                val group_name: String,
                val gst_treatment: String,
                val gstin: String,
                val id: String,
                val ifsc_code: String,
                val is_bank_account: String,
                val is_duties_and_taxes: String,
                val is_editable: String,
                val is_system_ledger: String,
                val is_tcs_applicable: String,
                val is_tds_applicable: String,
                val name: String,
                val nature_of_good_id: String,
                val nature_of_payment_id: String,
                val notes: String,
                val opening_balance: String,
                val opening_balance_type: String,
                val pan_card: String,
                val payment_name: String,
                val percentage_of_duty: String,
                val sub_group_id: Int,
                val sub_group_name: String,
                val type_of_duty: String,
                val type_of_gst: String,
                val updated_at: String,
                val updated_by: Int
            )

        }
    }

}*/


data class LedgerDetailsModel(
    val data: DataLedger,
    val code: String?,
    val message: String?,
    val status: Boolean?
) {

    data class DataLedger(val ledger: Ledger)

    data class Ledger(
        val ledgerData: LedgerData,
        val enableTcs: String?,
        val enableTds: String?,
        val enableGst: String?,
        val ledgerClosingBalance: LedgerClosingBalance
    )

    data class LedgerClosingBalance(
        val total_balance: String?,
        val amount_default_short_term: String?,
        val amount_default_term: String?
    )

    data class LedgerData(
        val id: String,
        val group_id: String,
        val company_id: String,
        val sub_group_id: String,
        val name: String,
        val created_by: String,
        val updated_by: String,
        val created_at: String,
        val updated_at: String,
        val code: String,
        val bank_name: String,
        val ifsc_code: String,
        val branch_name: String,
        val type_of_duty: String,
        val type_of_gst: String,
        val percentage_of_duty: String,
        val bill_by_bill_reference: String,
        val pan_card: String,
        val gst_treatment: String,
        val gstin: String,
        val is_tds_applicable: String,
        val nature_of_payment_id: String,
        val is_tcs_applicable: String,
        val nature_of_good_id: String,
        val is_sub_account :String,
        val notes: String,
        val group_name: String,
        val sub_group_name: String,
        val goods_name: String,
        val payment_name: String,
        val is_editable: String,
        val is_system_ledger: String,
        val is_bank_account: String,
        val is_duties_and_taxes: String,
        val cheque_register: List<AddChequeBookModel.AddChequeBookModelItem>,
        val opening_balance: String,
        val opening_balance_type: String,
        val account_number: String
    )
}