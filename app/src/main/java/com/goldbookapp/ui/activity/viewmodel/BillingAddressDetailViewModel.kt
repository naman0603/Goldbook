package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import kotlinx.coroutines.Dispatchers

class BillingAddressDetailViewModel (private val apiHelper: ApiHelper) : ViewModel() {

    fun getCountry() = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getCountry()))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getState(country: String?) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getState(country)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getCity(state: String?) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getCity(state)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


}