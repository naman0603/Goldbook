package com.goldbookapp.ui.activity.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import com.goldbookapp.model.ProfileDetailModel
import kotlinx.coroutines.Dispatchers


class SaleDetailsViewModel (private val apiHelper: ApiHelper) : ViewModel() {

    var profileDetail: MutableLiveData<ProfileDetailModel> = MutableLiveData()


    fun saleDetail( token: String?,
                    transaction_id: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.saleDetail(token, transaction_id)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
    fun voucherText( token: String?,
                     transaction_type: String?,
                     transaction_id: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.voucherText(token,transaction_type, transaction_id)))
        } catch (exception: Exception) {
            //  Log.e("vouchertexterror",exception.toString())
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun deleteSale( token: String?,
                            id: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.deleteSale(token, id)))
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