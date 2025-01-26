package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import kotlinx.coroutines.Dispatchers

class EditGroupViewModel (private val apiHelper: ApiHelper) : ViewModel() {


    fun editGroup( token: String?,
                  group_name: String?,
                  nature_group_id: String?,
                  affect_gross_profit: String?,
                   is_bank_account: String?,
                  description: String?,
                  group_id: String?

    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(data = apiHelper.editGroup(token,
                    group_name,
                    nature_group_id,
                    affect_gross_profit,
                    is_bank_account,
                    description,group_id)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


    fun getNatureGroup( token: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getNatureGroup(token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }



}