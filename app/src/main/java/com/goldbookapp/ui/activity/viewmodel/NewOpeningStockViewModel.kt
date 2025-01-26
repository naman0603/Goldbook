package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import com.goldbookapp.model.ProfileDetailModel
import kotlinx.coroutines.Dispatchers
import okhttp3.RequestBody


class NewOpeningStockViewModel (private val apiHelper: ApiHelper) : ViewModel() {

    var profileDetail: MutableLiveData<ProfileDetailModel> = MutableLiveData()


    fun addOpeningStock(
        token: String?,
        transaction_date: RequestBody?,
        invoice_number: RequestBody?,
        item_json: RequestBody?,
        remarks: RequestBody?,
        reference: RequestBody?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.addOpeningStock(
                        token, transaction_date, invoice_number, item_json, remarks,
                        reference
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun openingStockCalculate(token: String?, item_json: RequestBody?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.getCalculateOpeningStock(
                        token, item_json)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


    fun editOpeningStock(
        token: String?,
        transaction_id: RequestBody?,
        transaction_date: RequestBody?,
        invoice_number: RequestBody?,
        item_json: RequestBody?,
        remarks: RequestBody?,
        reference: RequestBody?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.editOpeningStock(
                        token, transaction_id, transaction_date, invoice_number, item_json, remarks,
                        reference
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }





    fun getOpeningStockVoucherNoFromApi(token: String?, invoice_date: String?, transaction_id: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(data = apiHelper.getOpeningStockVoucherNoFromApi(token, invoice_date,transaction_id)))
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