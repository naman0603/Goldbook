package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import kotlinx.coroutines.Dispatchers
import okhttp3.RequestBody

class EditLedgerViewModel(private val apiHelper: ApiHelper) : ViewModel() {


    fun editLedgerDetails(
        token: String?,
        ledger_id:String?,
        name: String?,
        code: String?,
        is_sub_account:String?,
        group_id: String?,
        sub_group_id: String?,
        is_bank_account: String?,
        bank_name:String?,
        account_number:String?,
        ifsc_code:String?,
        branch_name:String?,
        is_duties_and_taxes:String?,
        type_of_duty:String?,
        is_tcs_applicable :String?,
        is_tds_applicable :String?,
        nature_of_goods:String?,
        nature_of_payment:String?,
        type_of_gst:String?,
        percentage_of_duty:String?,
        bill_by_bill_reference:String?,
        pan_card:String?,
        gst_treatment:String?,
        gstin:String?,
        notes:String?,
        opening_balance_type:String?,
        opening_balance:String?,
        cheque_register_array:String?

    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.editLedgerDetails(
                        token,
                        ledger_id,
                        name,
                        code,
                        is_sub_account,
                        group_id,
                        sub_group_id,
                        is_bank_account,
                        bank_name,
                        account_number,
                        ifsc_code,
                        branch_name,
                        is_duties_and_taxes,
                        type_of_duty,
                        is_tcs_applicable,
                        is_tds_applicable,
                        nature_of_goods,
                        nature_of_payment,
                        type_of_gst,
                        percentage_of_duty,
                        bill_by_bill_reference,
                        pan_card,
                        gst_treatment,
                        gstin,
                        notes,
                        opening_balance_type,
                        opening_balance,
                        cheque_register_array
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getNatureofGoods(
        token: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getNatureofGoods(token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getNatureOfPayment(
        token: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getNatureOfPayment(token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getParentGroup( token: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getParentGroup(token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getLedgerGroupSubGroup(
        token: String?,
        group_id: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getLedgerGroupSubGroup(token,group_id)))
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