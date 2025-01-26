package com.goldbookapp.ui.activity.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import com.goldbookapp.model.ProfileDetailModel
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import okhttp3.RequestBody


class EditItemViewModel (private val apiHelper: ApiHelper) : ViewModel() {

    var profileDetail: MutableLiveData<ProfileDetailModel> = MutableLiveData()

    fun editItem( token: String?,
                    company_id: RequestBody?,
                    item_id: RequestBody?,
                    item_name: RequestBody?,
                    item_code: RequestBody?,
                  item_stock_type: RequestBody?,
                    category_id: RequestBody?,
                    unit: RequestBody?,
                    vendor_id: RequestBody?,
                    sales_wastage: RequestBody?,
                    sales_making_charge: RequestBody?,
                    purchase_wastage: RequestBody?,
                    purchase_making_charge: RequestBody?,
                  opening_stocks: RequestBody?,
//                    quantity: RequestBody?,
//                    gross_wt: RequestBody?,
//                    less_wt: RequestBody?,
//                    touch: RequestBody?,
                   /* making_charge: RequestBody?,*/
                    notes: RequestBody?,
                    default_image_index: RequestBody?,
                  show_in_sales : RequestBody? ,
                  show_in_purchase: RequestBody?,
                  is_raw_material : RequestBody?,
                  colour_id : RequestBody?,
                  hsn_code : RequestBody?,
                  minimum_stock_level : RequestBody?,
                  maximum_stock_level : RequestBody?,
                    item_image: MultipartBody.Part?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.updateItem(token, company_id, item_id, item_name, item_code, item_stock_type,category_id, unit,
                vendor_id,
                sales_wastage,
                sales_making_charge,
                purchase_wastage,
                purchase_making_charge,
                opening_stocks,
//                quantity,
//                gross_wt,
//                less_wt,
//                touch,
//                making_charge,
                notes,
                default_image_index,
                show_in_sales,
                show_in_purchase,
                is_raw_material,
                colour_id,
                hsn_code,
                minimum_stock_level,
                maximum_stock_level,
                item_image)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getItemCategory( token: String?,
                         company_id: String?,
                         offset: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getItemCategory(token,
                offset)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
    fun searchItemCategory( token: String?,
                            offset: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.searchItemCategory(token,
                offset)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getItemUnit( token: String?,
                         company_id: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getItemUnit(token, company_id)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getItemVendors( token: String?,
                     company_id: String?
    ) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getItemVendors(token, company_id)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getMetalColours(token: String?,
                        status: String?) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getMetalColour(token,status)))
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

}