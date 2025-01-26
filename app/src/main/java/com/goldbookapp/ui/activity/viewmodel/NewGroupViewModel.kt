package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import kotlinx.coroutines.Dispatchers

class NewGroupViewModel (private val apiHelper: ApiHelper) : ViewModel() {


    fun addGroup( token: String?,
                  group_name: String?,
                  ledger_group_id: String?,
                  nature_group_id: String?,
                  affect_gross_profit: String?,
                  is_bank_account: String?,
                  description: String?,
                  make_this_sub_group: String?

    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(data = apiHelper.addGroup(token,
                    group_name,
                    ledger_group_id,
                    nature_group_id,
                    affect_gross_profit,
                    is_bank_account,
                    description,make_this_sub_group)))
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

    fun getParentGroup( token: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getParentGroup(token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

}