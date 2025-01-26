package com.goldbookapp.utils

class Constants {
    companion object {

        const val IS_FROM_NEW_INVOICE: String = "isfromnewinvoice"
        const val ISSUE_RESOLUTION_LIST: String = "issue_resolution_list"
        const val SORT_TYPE_ASCENDING = "Asc"
        const val SORT_TYPE_DESCENDING = "Desc"
        const val ErrorCode = "422"
        const val ErrorCodeLogout = "401"
        const val Error = "200"
        const val AccessDeniedCode = "403"
        var apicallcount = 0
        var isDashboardLoadedOnce: Boolean = true;
        const val isFromListRestrict = "1"
        //const val signupRestrict = "signupRestrict"

        const val WebLinks = "weblinks"
        const val Todaysdate = "todaydate"
        const val FiscalYear = "fiscalYear"
        const val ReviewApp= "reviewApp"

        // restrict msg
        const val restrict_msg = "restrict_msg"

        const val isFromDemoCompany: String = "isFromDemoCompany"
        const val passcodeForDemoComp: String =
            "9af15b336e6a9619928537df30b2e6a2376569fcf9d7e773eccede65606529a0"

        const val DemoCompanyId = "325"
        const val DemoCompanyID = 325

        //pagination constant
        const val PAGE_START = 1
        const val PAGE_SIZE = 10

        // Preference
        const val PREF_COMPANY_REGISTER_KEY = "company_register"
        const val PREF_LOGIN_DETAIL_KEY = "login_detail"
        const val PREF_DASHBOARD_DETAIL_KEY = "dashboard_detail"
        const val PREF_PROFILE_DETAIL_KEY = "profile_detail"

        const val PREF_BILLING_ADDRESS_KEY = "billing_address"
        const val PREF_SHIPPING_ADDRESS_KEY = "shipping_address"
        const val PREF_COMPANY_ADDRESS_KEY = "company_address"
        const val PREF_BRANCH_ADDRESS_KEY = "branch_address"
        const val PREF_ADDITIONAL_iNFO_ADDRESS_KEY = "contact_shipping_other_address"

        const val PREF_MULTIPLE_OPENINGSTOCK = "multiple_openingstock_added"
        const val OPENINGSTOCK_DETAIL_KEY = "openingstock_detail"
        const val PREF_ADD_ITEM_KEY = "item_added"
        const val PREF_CHEQUE_BOOK_KEY = "cheque_added"
        const val PREF_CHEQUE_BOOK_EDITKEY = "cheque_edited"
        const val PREF_CHEQUE_BOOK_POS = "cheque_pos"

        const val PREF_INVENTORY_INFO_KEY = "inventoryinfo_added"
        const val PREF_ACCOUNTING_INFO_KEY = "accountinginfo_added"
        const val PREF_LESS_WEIGHT_INFO_KEY = "lessweight_added"
        const val PREF_LESS_WEIGHT_INFO_EDITKEY = "lessweight_edit"
        const val PREF_LESS_WEIGHT_POS = "lessweight_pos"
        const val LESS_WEIGHT_SAVE_TYPE: String = "lessweight_save_add"
        const val PREF_OPENINGSTOCK_INFO_KEY = "openingStockItem_added"
        const val PREF_LESS_WEIGHT_BREAKUP_INFO_KEY = "lessweight_breakup_added"
        const val PREF_MAKING_CHARGES_BREAKUP_INFO_KEY = "makingcharges_breakup_added"
        const val PREF_MAKING_CHARGES_KEY = "makingcharges_added"
        const val PREF_OTHER_CHARGES_KEY = "othercharges_added"
        const val PREF_OPENINGSTOCK_CALC_INFO_KEY = "openingStockItemCalc_added"
        const val PREF_SALES_LINE_INFO_KEY = "salesline_added"
        const val PREF_SALES_TAX_ANALYSIS_INFO_KEY = "salestaxAnalysis_added"
        const val IS_FROM_NEW_INVOICE_LESS_WEIGHT = "is_from_new_invoice_less_weight"
        const val PREF_SALES_TAX_ANALYSIS_LIST_KEY = "salesTaxAnalysisList"
        const val TAX_ANALYSIS_MODEL = "tax_analysis"
        //sorting pref keys (for all listing screens)

        const val PREF_CUST_SORT_TRACKNO = "customer_sort"
        const val PREF_SUPPL_SORT_TRACKNO = "supplier_sort"
        const val PREF_ITEM_SORT_TRACKNO = "item_sort"
        const val PREF_SALES_SORT_TRACKNO = "sales_sort"
        const val PREF_PURCHASE_SORT_TRACKNO = "purchase_sort"
        const val PREF_PAYMENT_SORT_TRACKNO = "payment_sort"
        const val PREF_RECEIPT_SORT_TRACKNO = "receipt_sort"
        const val PREF_OPENINGSTOCK_SORT_TRACKNO = "opening_sort"

        // Intent
        const val CUSTOMER_DETAIL_KEY = "customer_detail"
        const val ITEMCATEGORY_DETAIL_KEY = "itemcat_detail"
        const val SUPPLIER_DETAIL_KEY = "supplier_detail"
        const val NEWITEM_QTY_UNIT_KEY = "newitem_qty_unit"
        const val NEWITEM_ITEM_TYPE_KEY = "newitem_itemtype"
        const val NEWITEM_METAL_TYPE_KEY = "newitem_metaltype"
        const val SALES_DETAIL_KEY = "sale_detail"
        const val GROUP_DETAIL_KEY = "group_detail"
        const val LEDGER_DETAIL_KEY = "ledger_detail"
        const val OPENING_STOCK_DETAIL_KEY = "openingStock_detail"
        const val SELECTED_ITEM_DATA_MODEL = "selected_item"
        const val QR_DETAILS = "QrResponse"

        const val ORGS_DETAIL_KEY = "org_detail"
        const val BRANCH_DETAIL_KEY = "branch_detail"
        const val PURCHASE_DETAIL_KEY = "purchase_detail"
        const val PAYMENT_DETAIL_KEY = "payment_detail"
        const val RECEIPT_DETAIL_KEY = "receipt_detail"
        const val ITEM_ID_KEY = "item_id"
        const val ITEM_DETAIL_KEY = "item_detail"
        const val INVOICE_ITEM_POSITION_KEY = "invoice_item_position"
        const val OPENING_STOCK_POSITION_KEY = "openingStock_item_position"
        const val OPENING_STOCK_SAVE_TYPE : String = "openingStck_save_add"
        const val PAYMENT_ITEM_POSITION_KEY = "payment_item_position"
        const val RECEIPT_ITEM_POSITION_KEY = "receipt_item_position"
        const val INVOICE_GST_KEY = "invoice_gst"
        const val EDIT_ITEM = "Edit Item"
        const val ITEM_CATEGORY_LIST = "item_categories_list"
        const val IS_FOR_EDIT = "isForEdit"
        const val TAX_LIST_SALE = "Tax List"
        const val CUST_STATE_ID = "Customer State Id"
        //  mobile number/ email
        const val MobileNo = "Mobile No"
        const val Email = "Email"
        const val otp = "OTP"
        const val ModuleID = "ModuleID"
        const val PAYMENT_RECENT_TRANS_DETAIL_KEY = "recent_trans_payment"
        const val SALES_RECENT_TRANS_DETAIL_KEY = "recent_trans_sales"
        const val PURCHASE_RECENT_TRANS_DETAIL_KEY = "recent_trans_purchase"
        const val RECEIPT_RECENT_TRANS_DETAIL_KEY = "recent_trans_receipt"
        const val OPENING_STOCK_RECENT_TRANS_DETAIL_KEY = "recent_trans_openingstock"

        // Prefix/Postfix in UI
        const val WEIGHT_GM_APPEND: String = " gm"
        const val AMOUNT_RS_APPEND: String = "₹ "
        const val FINEWT_APPEND: String = "F.W.: "
        const val NETWT_APPEND: String = "N.W.: "
        const val TOUCH_APPEND: String = "Tch: "

        const val ENTRIES_APPEND: String = " entries"

        //CMS Pages path manipulatation text
        const val CMS_SERVICES: String = "services"
        const val CMS_POLICY: String = "policy"

        //Transaction Type (value:payment/receipt)
        const val TRANSACTION_TYPE: String = "transaction_type"
        const val TRANSACTION_ID: String = "transaction_id"

        //payment intent
        const val Payment_Row_No: String = "payment_row_no"
        const val SaleLine_Row_No: String = "saleline_row_no"
        const val is_Edit_Stock = "edit_stock"

        // payment prefs
        const val PREF_Payment_Ref_Selected_Trans_Ids = "payment_trans_ids"
        const val PREF_Payment_Ref_Selected_Invoice_Nos = "payment_invoice_nos"
        const val PREF_SELECTED_STOCK_ID_DETAILS = "selected_stock_id_details"
        const val isFromSelectedStock = "is_from_selectedstock"

        //Report Track no.
        const val ReportsTrackNo = "reportsTrackNo"


        //common for payment/receipt module
        const val PREF_ADD_ITEM_PAYMENT_KEY = "item_payment_added"
        const val PREF_CASH_BANK_METAL_SALES_KEY = "cashbank_sales_added"

        //receipt line nos
        const val Receipt_Row_No: String = "payment_row_no"
        const val Receipt_Ref_Selected_Trans_Ids = "payment_trans_ids"
        const val Receipt_Ref_Selected_Invoice_Nos = "payment_invoice_nos"
        //const val PREF_ADD_ITEM_RECEIPT_KEY = "item_payment_added"


        // setting tcs / tds
        const val TCS_TDS_NOG_NOP_KEY = "tcs_tds_nog_nop"
        const val NOG_NOP_TYPE = "nog_nop_type"
        const val isFromTcsNogAdd = "is_from_tcs"
        const val PREF_ADD_NOG_KEY = "nog_added"
        const val PREF_ADD_NOP_KEY = "nop_added"
        const val PREF_EDIT_NOG_KEY = "nog_edited"
        const val PREF_EDIT_NOP_KEY = "nop_edited"
        const val isFromTaxTcs = "is_from_tcs"

        //app lock
        const val PASSWORD_PREFERENCE_KEY: String = "PASSCODE"
        const val PINCODE: String = "PINCODE"


        // customer (new changes)
        const val isFromNewEditCustAddress =
            "is_from_new_edit_cust" // true (add/edit customer) / false (add/edit supplier)
        const val isFromNewCustAddress =
            "is_from_new_cust" // true (new customer) / false (new supplier)
        const val isFromEditCustAddress =
            "is_from_edit_cust" // true (edit customer) / false (edit supplier)
        const val PREF_ADD_EDIT_CUST_SHIPPING_OTHER_KEY = "add_edit_cust_shipping_other"
        const val PREF_ADD_EDIT_SUPP_SHIPPING_OTHER_KEY = "add_edit_supp_shipping_other"
        const val EDIT_CUST_POS_KEY = "cust_address_pos"
        const val EDIT_SUPP_POS_KEY = "supp_address_pos"
        const val CUST_TCS_TDS_EDIT = "edit_cust_tcs_tds_details"
        const val SUPP_TCS_TDS_EDIT = "edit_supp_tcs_tds_details"
        const val PREF_TCS_TDS_SHARE_DATA = "tcs_tds_share_data"
        const val IS_FROM_NEW_OPENING_STOCK = "edit_openingStock"


        const val PREF_LEDGER_SORT_TRACKNO = "ledger_sort"
        const val PREF_GROUP_SORT_TRACKNO = "group_sort"
        const val GroupID = "GroupID"
        const val CHEQUE_SAVE_TYPE: String = "cheque_save_add"
        const val CHEQUE_SAVE_FROM_ADD_EDIT: String = "cheque_save_add_edit"
        const val IS_FROM_NEW_CHEQUE: String = "isfromnewcheque"
        const val CHEQUE_SAVE_TYPE_EDIT: String = "cheque_save_edit"
        const val METALCOLOUR_DETAIL_KEY = "metalcolur_detail"
        const val METAL_SAVE_TYPE: String = "metal_save_add"

        const val ITEM_SAVE_TYPE: String = "item_save_add"


        // permission
        const val Change_Status = "change_status"
        const val Permission = "permission"


        const val ITEM_CHARGES_PER_DATA = "charges_detail"
        const val ISSUE_RECEIVE_POS = "issue_receive_position"
        const val ISSUE_RECEIVE_MODEL = "issueReceive_rowmodel"

    }
}