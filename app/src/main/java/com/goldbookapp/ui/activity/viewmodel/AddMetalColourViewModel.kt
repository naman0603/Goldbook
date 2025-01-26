package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import kotlinx.coroutines.Dispatchers


class AddMetalColourViewModel (private val apiHelper: ApiHelper) : ViewModel() {

    //var profileDetail: MutableLiveData<ProfileDetailModel> = MutableLiveData()

    fun addMetalColour(  token: String?,
                         colour_name: String?,
                         colour_code: String?,
                          status: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.addMetalColour(token, colour_name,
                colour_code,
                status
            )))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

}