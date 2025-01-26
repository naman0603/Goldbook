package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import kotlinx.coroutines.Dispatchers


class NewItemCatViewModel (private val apiHelper: ApiHelper) : ViewModel() {

    //var profileDetail: MutableLiveData<ProfileDetailModel> = MutableLiveData()

    fun addItemCategory(  token: String?,
                          category_name: String?,
                          category_code: String?,
                          status: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.addItemCategory(token, category_name,
                category_code,
                status
            )))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

}