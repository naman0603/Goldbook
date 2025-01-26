package com.goldbookapp.ui.activity.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import kotlinx.coroutines.Dispatchers

class ReportTypesCommonViewModel(private val apiHelper: ApiHelper) : ViewModel() {
    fun contactReportPrint(
        token: String?,
        from_date: String?,
        to_date: String?,
        report_type: String?,
        all_contacts: String?,
        type_of_contact: String?,
        contact_id: String?,
        all_cash_ledgers: String?,
        cash_ledger_id: String?,
        all_bank_ledgers: String?,
        bank_ledger_id: String?,
        period: String?,
        all_item_categories: String?,
        item_category_id: String?,
        all_items: String?,
        item_id: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.contactReportPrint(
                        token,
                        from_date,
                        to_date,
                        report_type,
                        all_contacts,
                        type_of_contact,
                        contact_id,
                        all_cash_ledgers,
                        cash_ledger_id,
                        all_bank_ledgers,
                        bank_ledger_id,
                        period,
                        all_item_categories,
                        item_category_id,
                        all_items,
                        item_id
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun dayReportPrint(
        token: String?,
        from_date: String?,
        to_date: String?,
        report_type: String?,
        all_contacts: String?,
        type_of_contact: String?,
        contact_id: String?,
        all_cash_ledgers: String?,
        cash_ledger_id: String?,
        all_bank_ledgers: String?,
        bank_ledger_id: String?,
        period: String?,
        all_item_categories: String?,
        item_category_id: String?,
        all_items: String?,
        item_id: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.dayReportPrint(
                        token,
                        from_date,
                        to_date,
                        report_type,
                        all_contacts,
                        type_of_contact,
                        contact_id,
                        all_cash_ledgers,
                        cash_ledger_id,
                        all_bank_ledgers,
                        bank_ledger_id,
                        period,
                        all_item_categories,
                        item_category_id,
                        all_items,
                        item_id
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun cashbankReportPrint(
        token: String?,
        from_date: String?,
        to_date: String?,
        report_type: String?,
        all_contacts: String?,
        type_of_contact: String?,
        contact_id: String?,
        all_cash_ledgers: String?,
        cash_ledger_id: String?,
        all_bank_ledgers: String?,
        bank_ledger_id: String?,
        period: String?,
        all_item_categories: String?,
        item_category_id: String?,
        all_items: String?,
        item_id: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.cashbankReportPrint(
                        token,
                        from_date,
                        to_date,
                        report_type,
                        all_contacts,
                        type_of_contact,
                        contact_id,
                        all_cash_ledgers,
                        cash_ledger_id,
                        all_bank_ledgers,
                        bank_ledger_id,
                        period,
                        all_item_categories,
                        item_category_id,
                        all_items,
                        item_id
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun stocktPrint(
        token: String?,
        from_date: String?,
        to_date: String?,
        report_type: String?,
        all_contacts: String?,
        type_of_contact: String?,
        contact_id: String?,
        all_cash_ledgers: String?,
        cash_ledger_id: String?,
        all_bank_ledgers: String?,
        bank_ledger_id: String?,
        period: String?,
        all_item_categories: String?,
        item_category_id: String?,
        all_items: String?,
        item_id: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.stockReportPrint(
                        token,
                        from_date,
                        to_date,
                        report_type,
                        all_contacts,
                        type_of_contact,
                        contact_id,
                        all_cash_ledgers,
                        cash_ledger_id,
                        all_bank_ledgers,
                        bank_ledger_id,
                        period,
                        all_item_categories,
                        item_category_id,
                        all_items,
                        item_id
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


    fun salesPurchaseReportPrint(
        token: String?,
        from_date: String?,
        to_date: String?,
        report_type: String?,
        all_contacts: String?,
        type_of_contact: String?,
        contact_id: String?,
        all_cash_ledgers: String?,
        cash_ledger_id: String?,
        all_bank_ledgers: String?,
        bank_ledger_id: String?,
        period: String?,
        all_item_categories: String?,
        item_category_id: String?,
        all_items: String?,
        item_id: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.salesPurchaseReportPrint(
                        token,
                        from_date,
                        to_date,
                        report_type,
                        all_contacts,
                        type_of_contact,
                        contact_id,
                        all_cash_ledgers,
                        cash_ledger_id,
                        all_bank_ledgers,
                        bank_ledger_id,
                        period,
                        all_item_categories,
                        item_category_id,
                        all_items,
                        item_id
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun ledgerReportPrint(
        token: String?,
        from_date: String?,
        to_date: String?,
        report_type: String?,
        all_contacts: String?,
        type_of_contact: String?,
        contact_id: String?,
        all_cash_ledgers: String?,
        cash_ledger_id: String?,
        all_bank_ledgers: String?,
        bank_ledger_id: String?,
        period: String?,
        all_item_categories: String?,
        item_category_id: String?,
        all_items: String?,
        item_id: String?,
        all_ledgers: String?,
        ledger_id: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.ledgerReportPrint(
                        token,
                        from_date,
                        to_date,
                        report_type,
                        all_contacts,
                        type_of_contact,
                        contact_id,
                        all_cash_ledgers,
                        cash_ledger_id,
                        all_bank_ledgers,
                        bank_ledger_id,
                        period,
                        all_item_categories,
                        item_category_id,
                        all_items,
                        item_id,
                        all_ledgers,
                        ledger_id
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


    fun getSearchContacts(
        token: String?,
        company_id: String?,
        search: String?,
        offset: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.reportSupportContacts(
                        token, company_id, search,
                        offset
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getReportsItemCategories(token: String?, search: String?) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getReportsItemCategories(token, search)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getReportsItems(token: String?, search: String?, item_category_id: String?) =
        liveData(Dispatchers.IO) {
            emit(Resource.loading(data = null))
            try {
                emit(
                    Resource.success(
                        data = apiHelper.getReportsItems(
                            token,
                            search,
                            item_category_id
                        )
                    )
                )
            } catch (exception: Exception) {
                emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
            }
        }

    // item_type -> cash (api for ledger)
    fun getSearchLedger(
        token: String?,
        type: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(data = apiHelper.getSearchLedger(token, type))
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun userWiseRestriction(token: String?) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.userWiseRestriction(token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
}