package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import kotlinx.coroutines.Dispatchers

class UpdateContactViewModel (private val apiHelper: ApiHelper) : ViewModel() {

    fun updateContact( token: String?,
                       mobile_no: String?,
                       email: String?,
                       otp: String?
                  ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.updateContact(token, mobile_no, email,otp)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

}