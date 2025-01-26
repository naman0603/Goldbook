package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import kotlinx.coroutines.Dispatchers

class EditSubGroupViewModel(private val apiHelper: ApiHelper) : ViewModel() {


    fun editSubGroup(
        token: String?,
        group_name: String?,
        ledger_group_id: String?,
        is_bank_account: String?,
        description: String?,
        sub_group_id: String?

    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.editSubGroup(
                        token,
                        group_name,
                        ledger_group_id,
                        is_bank_account,
                        description, sub_group_id
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


    fun getParentGroup(
        token: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getParentGroup(token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


}