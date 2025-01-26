package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import kotlinx.coroutines.Dispatchers

class SaveTaxesViewModel (private val apiHelper: ApiHelper) : ViewModel() {
    fun getdetailGstApi(token: String?) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getdetailGstApi(token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
    fun getState(country: String?) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getState(country)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    // common tcs/tds/gst
    fun saveTcsTdsDetailApi(token: String?,
                            type: String?,
                            enable_gst: Int?,
                            gst_state_id: String?,
                            gstin: String?,
                            registration_date: String?,
                            periodicity_of_gst1: String?,
                            enable_tcs: Int?,
                            enable_tds: Int?,
                            tan_number: String?,
                            tds_circle: String?,
                            tcs_collector_type: String?,
                            tcs_person_responsible: String?,
                            tcs_designation: String?,
                            tcs_contact_number: String?,
                            nature_of_goods: String?,
                            tds_deductor_type: String?,
                            tds_person_responsible: String?,
                            tds_designation: String?,
                            tds_contact_number: String?,
                            nature_of_payment: String?) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.saveTcsDetailApi(
                        token,
                        type,
                        enable_gst,
                        gst_state_id,
                        gstin,
                        registration_date,
                        periodicity_of_gst1,
                        enable_tcs,
                        enable_tds,
                        tan_number,
                        tds_circle,
                        tcs_collector_type,
                        tcs_person_responsible,
                        tcs_designation,
                        tcs_contact_number,
                        nature_of_goods,
                        tds_deductor_type,
                        tds_person_responsible,
                        tds_designation,
                        tds_contact_number,
                        nature_of_payment
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
}