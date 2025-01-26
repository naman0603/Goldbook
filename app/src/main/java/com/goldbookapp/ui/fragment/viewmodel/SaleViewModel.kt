package com.goldbookapp.ui.fragment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import kotlinx.coroutines.Dispatchers

class SaleViewModel(private val apiHelper: ApiHelper) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is tools Fragment"
    }
    val text: LiveData<String> = _text
    fun searchListSales(token: String?, curret_page:Int?, name: String?, sort_by_column: String?,
                           sort_type: String?,date_range_from: String?,date_range_to: String?) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.searchListSales(token, curret_page, name, sort_by_column, sort_type,date_range_from,date_range_to)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
    fun userLimitAccess(token: String?) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.userLimitAccess(token)))
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