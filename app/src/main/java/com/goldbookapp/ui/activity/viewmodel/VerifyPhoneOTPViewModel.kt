package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import kotlinx.coroutines.Dispatchers

class VerifyPhoneOTPViewModel (private val apiHelper: ApiHelper) : ViewModel() {

    fun verifyOTP(otp: String?,
                  mobile_no: String?,
                  name: String?,
                  email : String?,
                  password : String?,
                  username: String?,
    otp_email:String?) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.verifyOTP(otp, mobile_no, name, email, password, username,otp_email)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun companyRegister(name: String?,
                        password: String?,
                        email: String?,
                        mobile_no: String?,
                        username: String?,
                        resend: Boolean?) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.userRegister(name, password, email, mobile_no,username, resend)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

}