package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import kotlinx.coroutines.Dispatchers

class SettingsContactViewModel (private val apiHelper: ApiHelper) : ViewModel() {
    fun getdetailContactApi(token: String?) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getdetailContactApi(token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun saveContactApi(token: String?,
                       disable_credit_limit: Int?,
                       stop_transaction_if_limit_over: Int?) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.saveContactApi(token,disable_credit_limit,stop_transaction_if_limit_over)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
}