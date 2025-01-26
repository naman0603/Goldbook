package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import com.goldbookapp.model.ProfileDetailModel
import kotlinx.coroutines.Dispatchers

class AddItemViewModel (private val apiHelper: ApiHelper) : ViewModel() {

    var profileDetail: MutableLiveData<ProfileDetailModel> = MutableLiveData()

    fun getItemSearch( token: String?,
                       item_name: String?,
                       module: String?,
                       item_type: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(data = apiHelper.getItemSearch(token, item_name,
                    module,item_type)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getLedgerdd(
        token: String?,
        type: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.searchLedger(token, type)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


    fun getItemStamp( token: String?,
                     item_id: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getItemStamp(token, item_id)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getTagDetails( token: String?,
                       tag_number: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getTagDetails(token, tag_number)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
}