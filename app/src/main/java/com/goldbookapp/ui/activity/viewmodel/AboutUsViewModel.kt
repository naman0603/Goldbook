package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import kotlinx.coroutines.Dispatchers

class AboutUsViewModel (private val apiHelper: ApiHelper) : ViewModel() {

//    var profileDetail: MutableLiveData<ProfileDetailModel> = MutableLiveData()



    fun aboutUs() = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.aboutUs()))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

}