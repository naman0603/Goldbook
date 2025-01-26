package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import com.goldbookapp.model.ProfileDetailModel
import kotlinx.coroutines.Dispatchers


class NewOrgnizationViewModel (private val apiHelper: ApiHelper) : ViewModel() {

    var profileDetail: MutableLiveData<ProfileDetailModel> = MutableLiveData()

    fun addUserCompany(  token: String?,
                         company_name: String?,
                         reg_address: String?,
                         area: String?,
                         landmark: String?,
                         country_id:String?,
                         state_id: String?,
                         city_id: String?,
                         postal_code: String?,
                         contact_person_first_name: String?,
                         contact_person_last_name: String?,
                         mobile_number: String?,
                         alternate_number: String?,
                         email: String?,
                         fiscal_year_id: String?,
                         pan_number: String?,
                         cin_number: String?,
                         term_balance: String?,
                         default_term: String?,
                         gst_register: String?,
                         gst_tin_number: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.addUserCompany(token, company_name,
                reg_address,
                area,
                landmark,
                country_id,
                state_id,
                city_id,
                postal_code,
                contact_person_first_name,
                contact_person_last_name,
                mobile_number,
                alternate_number,
                email,
                fiscal_year_id,
                pan_number,
                cin_number,
                term_balance,
                default_term,
                gst_register,
                gst_tin_number)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

}