package com.goldbookapp.ui.ui.send

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import com.goldbookapp.model.ProfileDetailModel
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import okhttp3.RequestBody

class NewPaymentViewModel (private val apiHelper: ApiHelper) : ViewModel() {

    var profileDetail: MutableLiveData<ProfileDetailModel> = MutableLiveData()

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
    fun getPaymentInvoiceNumber(token: String?, invoice_date: String?, transaction_id: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(data = apiHelper.getPaymentInvoiceNumber(token, invoice_date,transaction_id)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
    fun getCalculateItemPayment(token: String?,
                                item: String?,
                                contact_id: String?,
                                ledger_contact_type: String?,
                                transaction_id: String,
                                transaction_date: String?,
                                issue_receive_transaction: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(data = apiHelper.getCalculateItemPayment(token, item, contact_id,ledger_contact_type,
                    transaction_id, transaction_date, issue_receive_transaction)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
    fun addNewPayment(token: String?,
                      transaction_type_id: RequestBody?,
                      transaction_type_name: RequestBody?,
                      transaction_date: RequestBody?,
                      contactId: RequestBody?,
                      ledger_contact_type: RequestBody?,
                      invoice_number: RequestBody?,
                      item_json: RequestBody?,
                      issue_receive_transaction: RequestBody?,
                      is_gst_applicable: RequestBody?,
                      party_po_no: RequestBody?,
                      reference: RequestBody?,
                      remarks: RequestBody?,
                      image: MultipartBody.Part?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.addnewpayment(
                        token,
                        transaction_type_id,
                        transaction_type_name,
                        transaction_date,
                        contactId,
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

    fun editPayment(token: String?,
                      transaction_type_id: RequestBody?,
                      transaction_type_name: RequestBody?,
                      transaction_id: RequestBody?,
                    transaction_date: RequestBody?,
                      contactId: RequestBody?,
                    ledger_contact_type: RequestBody?,
                      invoice_number: RequestBody?,
                      item_json: RequestBody?,
                      issue_receive_transaction: RequestBody?,
                      is_gst_applicable: RequestBody?,
                      party_po_no: RequestBody?,
                      reference: RequestBody?,
                      remarks: RequestBody?,
                      image: MultipartBody.Part?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.editPayment(
                        token,
                        transaction_type_id,
                        transaction_type_name,
                        transaction_id,
                        transaction_date,
                        contactId,
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