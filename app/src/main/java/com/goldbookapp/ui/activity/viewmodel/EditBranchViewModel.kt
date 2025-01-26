package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import com.goldbookapp.model.ProfileDetailModel
import kotlinx.coroutines.Dispatchers


class EditBranchViewModel (private val apiHelper: ApiHelper) : ViewModel() {

    var profileDetail: MutableLiveData<ProfileDetailModel> = MutableLiveData()

    fun updateCompanyBranch(        token: String?,
                                    branch_name: String?,
                                    branch_id: String?,
                                    branch_code: String?,
                                    branch_address: String?,
                                    branch_contact_no: String?,
                                    secondary_contact:String?,
                                    contact_person_fname: String?,
                                    contact_person_lname: String?,
                                    branch_email: String?,
                                    business_location: String?,
                                    state_id: String?,
                                    city_id: String?,
                                    area: String?,
                                    landmark: String?,
                                    term_balance: String?,
                                    gst_register: String?,
                                    gst_tin_number: String?,
                                    pincode: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.updateCompanyBranch(token, branch_name,
                branch_id,
                branch_code,
                branch_address,
                branch_contact_no,
                secondary_contact,
                contact_person_fname,
                contact_person_lname,
                branch_email,
                business_location,
                state_id,
                city_id,
                area,
                landmark,
                term_balance,
                gst_register,
                gst_tin_number,
                pincode
            )))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

}