package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import com.goldbookapp.model.ProfileDetailModel
import kotlinx.coroutines.Dispatchers

class GeneratedReportCommonViewModel (private val apiHelper: ApiHelper) : ViewModel() {

    var profileDetail: MutableLiveData<ProfileDetailModel> = MutableLiveData()

    fun getSearchItem( token: String?,
                       search: String?,
                       offset: String?,
                       transaction_type: String?,
                       company_id: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(data = apiHelper.getSearchItem(token, search,
                    offset, transaction_type, company_id)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getItemUnit( token: String?,
                     company_id: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getItemUnit(token, company_id)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
}