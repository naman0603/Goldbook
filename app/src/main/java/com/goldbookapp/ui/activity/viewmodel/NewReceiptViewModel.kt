package com.goldbookapp.ui.ui.send

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import okhttp3.RequestBody

class NewReceiptViewModel(private val apiHelper: ApiHelper) : ViewModel() {

    //var data: MutableLiveData<MutableList<CalculationPaymentModel.DataPayment.ItemPayment>> = MutableLiveData()

    fun getSearchContactsLedger(
        token: String?

    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.getSearchContactLedger(
                        token
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getReceiptInvoiceNumber(
        token: String?, invoice_date: String?, transaction_id: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.getReceiptInvoiceNumber(
                        token,
                        invoice_date,
                        transaction_id
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getCalculateItemReceipt(
        token: String?,
        item: String?,
        contact_id: String?,
        ledger_contact_type: String?,
        transaction_id: String,
        transaction_date: String?,
        issue_receive_transaction: String?


    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.getCalculateItemReceipt(
                        token, item, contact_id,ledger_contact_type,
                        transaction_id, transaction_date, issue_receive_transaction
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun addNewReceipt(
        token: String?,
        transaction_type_id: RequestBody?,
        transaction_type_name: RequestBody?,
        transaction_date: RequestBody?,
        contactID: RequestBody?,
        ledger_contact_type: RequestBody?,
        invoice_number: RequestBody?,
        item_json: RequestBody?,
        issue_receive_transaction: RequestBody?,
        is_gst_applicable: RequestBody?,
        party_po_no: RequestBody?,
        reference: RequestBody?,
        remarks: RequestBody?,
        image: MultipartBody.Part?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.addnewReceipt(
                        token,
                        transaction_type_id,
                        transaction_type_name,
                        transaction_date,
                        contactID,
                        ledger_contact_type,
                        invoice_number,
                        item_json,
                        issue_receive_transaction,
                        is_gst_applicable,
                        party_po_no,
                        reference,
                        remarks,
                        image
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun editReceipt(
        token: String?,
        transaction_id: RequestBody?,
        transaction_type_id: RequestBody?,
        transaction_type_name: RequestBody?,
        transaction_date: RequestBody?,
        contact_id: RequestBody?,
        ledger_contact_type: RequestBody?,
        invoice_number: RequestBody?,
        item_json: RequestBody?,
        issue_receive_transaction: RequestBody?,
        is_gst_applicable: RequestBody?,
        party_po_no: RequestBody?,
        reference: RequestBody?,
        remarks: RequestBody?,
        image: MultipartBody.Part?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.editReceipt(
                        token,
                        transaction_id,
                        transaction_type_id,
                        transaction_type_name,
                        transaction_date,
                        contact_id,
                        ledger_contact_type,
                        invoice_number,
                        item_json,
                        issue_receive_transaction,
                        is_gst_applicable,
                        party_po_no,
                        reference,
                        remarks,
                        image
                    )
                )
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

    fun getDefaultTerm( token: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(data = apiHelper.getDefaultTerm(token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
}