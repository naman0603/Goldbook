package com.goldbookapp.model

data class SearchListCustomerModel(
    val data: List<Data1037062284>?,
    val code: String?,
    val message: String?,
    val total_page: Int?,
    val page_limit: Int?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {

    data class Data1037062284(
        val customer_id: String?,
        val title: String?,
        val first_name: String?,
        val last_name: String?,
        val display_name: String?,
        val date_for_sorting: String?,
        val email: String?,
        val fine_balance: String?,
        val cash_balance: String?,
        val is_editable: String?,
        val opening_fine_default_term: String?,
        val opening_cash_default_term: String?,
        val silver_fine_balance: String?,
        val opening_silver_fine_default_term: String?

    )
}







