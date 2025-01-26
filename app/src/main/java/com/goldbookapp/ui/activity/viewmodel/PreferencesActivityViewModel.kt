package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import kotlinx.coroutines.Dispatchers

class PreferencesActivityViewModel (private val apiHelper: ApiHelper) : ViewModel() {
    fun getdetailPreferenceApi(token: String?) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getdetailPreferenceApi(token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun savePreferenceApi(token: String?,
                          enable_cheque_reg_for_bank_acc: Int?,
                          round_off_for_sales: Int?,
                          default_term: String?,
                          print_copies: String?) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.savePreferenceApi(token,enable_cheque_reg_for_bank_acc,round_off_for_sales,default_term,print_copies)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
}