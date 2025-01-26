package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import kotlinx.coroutines.Dispatchers

class SignupViewModel (private val apiHelper: ApiHelper) : ViewModel() {

    fun userSignUP(name: String?,
                     password: String?,
                     email: String?,
                     mobile_no: String?,
                        resend: Boolean?) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.userSignUP(name, password, email, mobile_no, resend)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

}