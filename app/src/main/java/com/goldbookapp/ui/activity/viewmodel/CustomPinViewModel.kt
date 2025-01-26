package com.goldbookapp.ui.activity.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import kotlinx.coroutines.Dispatchers

class CustomPinViewModel (private val apiHelper: ApiHelper) : ViewModel() {

//    var profileDetail: MutableLiveData<ProfileDetailModel> = MutableLiveData()


    fun setAppLockPin(
        token: String?,
        pin: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.setAppLockPin(token, pin)))
        } catch (exception: Exception) {
           // Log.e("pin", exception.toString())
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
    fun checkPin(token: String?, pin: String?) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            //Log.e("token",token.toString())
            //Log.e("pin",pin.toString())
            emit(Resource.success(data = apiHelper.checkPin(token, pin)))

        } catch (exception: Exception) {
          //  Log.e("checkpin", exception.toString())
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun forgetPin(token: String?) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            //Log.e("token",token.toString())
            //Log.e("pin",pin.toString())
            emit(Resource.success(data = apiHelper.forgetPin(token)))

        } catch (exception: Exception) {
            //  Log.e("checkpin", exception.toString())
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
}