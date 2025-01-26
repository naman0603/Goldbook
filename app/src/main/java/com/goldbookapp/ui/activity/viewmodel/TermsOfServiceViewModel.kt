package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import com.goldbookapp.model.ProfileDetailModel
import kotlinx.coroutines.Dispatchers


class TermsOfServiceViewModel (private val apiHelper: ApiHelper) : ViewModel() {

    //var profileDetail: MutableLiveData<ProfileDetailModel> = MutableLiveData()
    fun termsOfService( /*token: String?,*/
        cms : String?,
        page_id: Int?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.termsOfService(/*token,*/ cms, page_id)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }






}