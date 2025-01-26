package com.goldbookapp.ui.activity.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import kotlinx.coroutines.Dispatchers

class AddContactOrAddressViewModel (private val apiHelper: ApiHelper) : ViewModel() {


    fun deletecontactaddressinfo( token: String?,
                                  type: String?,
                                  edit_id: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.deletecontactaddressinfo(token, type, edit_id)))
        } catch (exception: Exception) {
           // Log.v("deleteNogexception",exception.toString())
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
}