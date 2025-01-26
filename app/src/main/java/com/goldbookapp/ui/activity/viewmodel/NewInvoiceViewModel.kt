package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import com.goldbookapp.model.ProfileDetailModel
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import okhttp3.RequestBody

class NewInvoiceViewModel (private val apiHelper: ApiHelper) : ViewModel() {

    var profileDetail: MutableLiveData<ProfileDetailModel> = MutableLiveData()

   /* fun getSearchCustomer( token: String?,
                           company_id: String?,
                           search: String?,
                         offset: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(data = apiHelper.getSearchCustomer(token,company_id, search,
                    offset)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }*/
   fun getSearchContacts(
       token: String?,
       company_id: String?,
       search: String?,
       offset: String?,
       transaction_id  : String?
   ) = liveData(Dispatchers.IO) {
       emit(Resource.loading(data = null))
       try {
           emit(
               Resource.success(
                   data = apiHelper.getSearchContacts(
                       token, company_id, search,
                       offset,transaction_id
                   )
               )
           )
       } catch (exception: Exception) {
           emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
       }
   }

    fun getCalculateItem( token: String?,
                          calculate: String?,
                          contact_id: String?,
                          transaction_id: String?,
                          item: String?,
                          issue_receive_transaction: String?,
                          is_gst_applicable: String?,
                          tds_percentage: String?,
                          tcs_percentage: String?,
                          place_of_supply: String?,
                          tds_tcs_enable: String?,
                          sgst_ledger_id: String?,
                          cgst_ledger_id: String?,
                          igst_ledger_id: String?,
                          tcs_ledger_id: String?,
                          round_off_ledger_id: String?,
                          round_off_total: String?,
                          invoice_date: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(data = apiHelper.getCalculateItem(token, calculate,
                    contact_id,transaction_id, item,issue_receive_transaction, is_gst_applicable,
                    tds_percentage, tcs_percentage, place_of_supply, tds_tcs_enable,
                    sgst_ledger_id,cgst_ledger_id,igst_ledger_id,tcs_ledger_id,round_off_ledger_id,round_off_total, invoice_date)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun addNewInvoice(token: String?,
                      transaction_type_id: RequestBody?,
                      transaction_type_name: RequestBody?,
                      transaction_date: RequestBody?,
                      customer_code: RequestBody?,
                      display_name: RequestBody?,
                      contact_id: RequestBody?,
                      party_po_no: RequestBody?,
                      reference: RequestBody?,
                      renarks: RequestBody?,
                      invoice_number: RequestBody?,
                      item_json: RequestBody?,
                      issue_receive_transaction: RequestBody?,
                      place_of_supply: RequestBody?,
                      sgst_ledger_id: RequestBody?,
                      cgst_ledger_id: RequestBody?,
                      igst_ledger_id: RequestBody?,
                      tds_ledger_id: RequestBody?,
                      tds_percentage: RequestBody?,
                      tcs_ledger_id: RequestBody?,
                      tcs_percentage: RequestBody?,
                      tds_tcs_enable: RequestBody?,
                      round_off_ledger_id: RequestBody?,
                      round_off_total: RequestBody?,
                      branch_type: RequestBody?,
                      ledger_id: RequestBody?,
                      image: MultipartBody.Part?,
                      transaction_type: RequestBody?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.addnewinvoice(
                        token,
                        transaction_type_id,
                        transaction_type_name,
                        transaction_date,
                        customer_code,
                        display_name,
                        contact_id,
                        party_po_no,
                        reference,
                        renarks,
                        invoice_number,
                        item_json,
                        issue_receive_transaction,
                        place_of_supply,
                        sgst_ledger_id,
                        cgst_ledger_id,
                        igst_ledger_id,
                        tds_ledger_id,
                        tds_percentage,
                        tcs_ledger_id,
                        tcs_percentage,
                        tds_tcs_enable,
                        round_off_ledger_id,
                        round_off_total,
                        branch_type,
                        ledger_id,
                        image,
                        transaction_type
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

    fun editInvoice(token: String?,
                      transaction_id: RequestBody?,
                      transaction_type_id: RequestBody?,
                      transaction_type_name: RequestBody?,
                      transaction_date: RequestBody?,
                      customer_code: RequestBody?,
                      display_name: RequestBody?,
                      contact_id: RequestBody?,
                      party_po_no: RequestBody?,
                      reference: RequestBody?,
                    renarks: RequestBody?,
                      invoice_number: RequestBody?,
                      item_json: RequestBody?,
                      issue_receive_transaction: RequestBody?,
                      place_of_supply: RequestBody?,
                      sgst_ledger_id: RequestBody?,
                      cgst_ledger_id: RequestBody?,
                      igst_ledger_id: RequestBody?,
                      tds_ledger_id: RequestBody?,
                      tds_percentage: RequestBody?,
                      tcs_ledger_id: RequestBody?,
                      tcs_percentage: RequestBody?,
                      tds_tcs_enable: RequestBody?,
                      round_off_ledger_id: RequestBody?,
                    round_off_total: RequestBody?,
                      branch_type: RequestBody?,
                      ledger_id: RequestBody?,
                      image: MultipartBody.Part?,
                      transaction_type: RequestBody?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.editInvoice(
                        token,
                        transaction_id,
                        transaction_type_id,
                        transaction_type_name,
                        transaction_date,
                        customer_code,
                        display_name,
                        contact_id,
                        party_po_no,
                        reference,
                        renarks,
                        invoice_number,
                        item_json,
                        issue_receive_transaction,
                        place_of_supply,
                        sgst_ledger_id,
                        cgst_ledger_id,
                        igst_ledger_id,
                        tds_ledger_id,
                        tds_percentage,
                        tcs_ledger_id,
                        tcs_percentage,
                        tds_tcs_enable,
                        round_off_ledger_id,
                        round_off_total,
                        branch_type,
                        ledger_id,
                        image,
                        transaction_type
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }




    fun getInvoiceNumber(token: String?, invoice_date: String?,transaction_id:String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(data = apiHelper.getInvoiceNumber(token, invoice_date,transaction_id)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
    fun deleteNop( token: String?,
                   nop_id: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.deleteNatureOfPayment(token, nop_id)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getLedgerdd( token: String?,
                   type: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.searchLedger(token, type)))
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