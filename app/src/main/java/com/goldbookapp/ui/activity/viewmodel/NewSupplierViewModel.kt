package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import com.goldbookapp.model.ProfileDetailModel
import kotlinx.coroutines.Dispatchers

class NewSupplierViewModel (private val apiHelper: ApiHelper) : ViewModel() {
    var profileDetail: MutableLiveData<ProfileDetailModel> = MutableLiveData()

    fun addSupplier( token: String?,
                     company_id: String?,
                     customer_type: String?,
                     title: String?,
                     first_name: String?,
                     last_name: String?,
                     company_name: String?,
                     customer_code: String?,
                     mobile_number: String?,
                     secondary_contact: String?,
                     email: String?,
                     opening_fine_balance: String?,
                     opening_fine_default_term: String?,
                     opening_silver_fine_balance: String?,
                     opening_silver_fine_default_term: String?,
                     opening_cash_balance: String?,
                     opening_cash_default_term: String?,
                     fine_limit:String?,
                     cash_limit:String?,
                     is_tcs_applicable: String?,
                     gst_register: String?,
                     gst_treatment: String?,
                     gst_tin_number: String?,
                     pan_number: String?,
                     courier: String?,
                     notes: String?,
                     is_shipping: String?,
                     billing_address: String?,
                     shipping_address: String?,
                     display_name: String?,
                     is_tds_applicable : String?,
                     selectedDeductorType : String?,
                     selectedCollectorType : String?,
                     selectedNogType : String?,
                     selectedNopType : String?,
                     selectedNatureofPaymentID : String?,
                     selectedNatureofGoodsID : String?

    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(data = apiHelper.addSupplier(token, company_id,
                    customer_type,
                    title,
                    first_name,
                    last_name,
                    company_name,
                    customer_code,
                    mobile_number,
                    secondary_contact,
                    email,
                    opening_fine_balance,
                    opening_fine_default_term,
                    opening_silver_fine_balance,
                    opening_silver_fine_default_term,
                    opening_cash_balance,
                    opening_cash_default_term,
                    fine_limit,
                    cash_limit,
                    is_tcs_applicable,
                    gst_register,
                    gst_treatment,
                    gst_tin_number,
                    pan_number,
                    courier,
                    notes,
                    is_shipping,
                    billing_address,
                    shipping_address,
                    display_name,
                    is_tds_applicable,
                    selectedDeductorType,
                    selectedCollectorType,
                    selectedNogType,
                    selectedNopType,
                    selectedNatureofPaymentID,
                    selectedNatureofGoodsID)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun userWiseRestriction(token: String?) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.userWiseRestriction(token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getDefaultTerm( token: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(data = apiHelper.getDefaultTerm(token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

}