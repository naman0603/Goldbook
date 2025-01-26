package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import kotlinx.coroutines.Dispatchers

class AddItemPaymentViewModel (private val apiHelper: ApiHelper) : ViewModel() {

//    var profileDetail: MutableLiveData<ProfileDetailModel> = MutableLiveData()


    // item_type -> cash (api for ledger)
    fun getSearchLedger( token: String?,
                       type: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(data = apiHelper.getSearchLedger(token, type)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getSearchItem( token: String?,
                       search: String?,
                       offset: String?,
                       transaction_type: String?,
                       transaction_id: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(data = apiHelper.getSearchItem(token, search,
                    offset, transaction_type, transaction_id)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

//
//    fun getItemUnit( token: String?,
//                     company_id: String?
//    ) = liveData(Dispatchers.IO){
//        emit(Resource.loading(data = null))
//        try {
//            emit(Resource.success(data = apiHelper.getItemUnit(token, company_id)))
//        } catch (exception: Exception) {
//            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
//        }
//    }
}