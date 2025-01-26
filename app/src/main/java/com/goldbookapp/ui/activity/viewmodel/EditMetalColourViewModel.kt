package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import kotlinx.coroutines.Dispatchers


class EditMetalColourViewModel (private val apiHelper: ApiHelper) : ViewModel() {

    //var profileDetail: MutableLiveData<ProfileDetailModel> = MutableLiveData()

    fun editMetalColour(  token: String?,
                             metal_colour_id: String?,
                             colour_name : String?,
                             colour_code: String?,
                          status: Number?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.editMetalColour(token, metal_colour_id,colour_name,
                colour_code,
                status
            )))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message?: "Error Occurred!"))
        }
    }

}