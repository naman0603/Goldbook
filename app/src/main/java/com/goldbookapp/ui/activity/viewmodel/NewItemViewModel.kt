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


class NewItemViewModel(private val apiHelper: ApiHelper) : ViewModel() {

    var profileDetail: MutableLiveData<ProfileDetailModel> = MutableLiveData()

    fun addNewItem(
        token: String?,
        item_type: RequestBody?,
        item_name: RequestBody?,
        item_code: RequestBody?,
        category_id: RequestBody?,
        notes: RequestBody?,
        metal_type_id: RequestBody?,
        maintain_stock_in_id: RequestBody?,
        unit_id: RequestBody?,
        is_studded: RequestBody?,
        stock_method: RequestBody?,
        tax_preference: RequestBody?,
        sales_wastage: RequestBody?,
        sales_making_charges: RequestBody?,
        purchase_wastage: RequestBody?,
        purchase_making_charges: RequestBody?,
        jobwork_rate: RequestBody?,
        labourwork_rate: RequestBody?,
        sales_purchase_gst_rate_id: RequestBody?,
        sales_purchase_hsn: RequestBody?,
        jobwork_labourwork_gst_rate_id: RequestBody?,
        jobwork_labourwork_sac: RequestBody?,
        sales_rate: RequestBody?,
        purchase_rate: RequestBody?,
        sales_ledger_id: RequestBody?,
        purchase_ledger_id: RequestBody?,
        jobwork_ledger_id: RequestBody?,
        labourwork_ledger_id: RequestBody?,
        discount_ledger_id: RequestBody?,
        tag_prefix: RequestBody?,
        use_stamp: RequestBody?,
        use_gold_color: RequestBody?,
        min_stock_level_gm: RequestBody?,
        min_stock_level_pcs: RequestBody?,
        max_stock_level_gm: RequestBody?,
        max_stock_level_pcs: RequestBody?,
        product_wt: RequestBody?,
        item_rate: RequestBody?,
        vendor_id: RequestBody?,
        gold_colour: RequestBody?,
        item_image: MultipartBody.Part?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.addNewItem(
                        token, item_type, item_name, item_code, category_id,
                        notes, metal_type_id, maintain_stock_in_id, unit_id,
                        is_studded, stock_method, tax_preference,
                        sales_wastage,
                        sales_making_charges,
                        purchase_wastage,
                        purchase_making_charges,
                        jobwork_rate,
                        labourwork_rate,
                        sales_purchase_gst_rate_id,
                        sales_purchase_hsn,
                        jobwork_labourwork_gst_rate_id,
                        jobwork_labourwork_sac,
                        sales_rate,
                        purchase_rate,
                        sales_ledger_id,
                        purchase_ledger_id,
                        jobwork_ledger_id,
                        labourwork_ledger_id,
                        discount_ledger_id,
                        tag_prefix,
                        use_stamp,
                        use_gold_color,
                        min_stock_level_gm,
                        min_stock_level_pcs,
                        max_stock_level_gm,
                        max_stock_level_pcs,
                        product_wt,
                        item_rate,
                        vendor_id,
                        gold_colour,
                        item_image
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getItemCategory(
        token: String?,
        company_id: String?,
        offset: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.getItemCategory(
                        token,
                        offset
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun searchItemCategory(
        token: String?,
        offset: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.searchItemCategory(
                        token,
                        offset
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


    fun getLedgerdd(
        token: String?,
        type: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.searchLedger(token, type)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getItemGSTMenu(
        token: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getItemGSTMenu(token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


    fun getItemUnitMenu(
        token: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getItemUnitMenu(token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


    fun getMaintainStock(
        token: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getMaintainStock(token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


    fun getMetalType(
        token: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getMetalType(token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


    fun getItemVendors(
        token: String?,
        company_id: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getItemVendors(token, company_id)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getMetalColours(
        token: String?,
        status: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getMetalColour(token, status)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun userWiseRestriction(token: String?) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.userWiseRestriction(token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun editItem(
        token: String?,
        item_id: RequestBody?,
        item_type: RequestBody?,
        item_name: RequestBody?,
        item_code: RequestBody?,
        category_id: RequestBody?,
        notes: RequestBody?,
        metal_type_id: RequestBody?,
        maintain_stock_in_id: RequestBody?,
        unit_id: RequestBody?,
        is_studded: RequestBody?,
        stock_method: RequestBody?,
        tax_preference: RequestBody?,
        sales_wastage: RequestBody?,
        sales_making_charges: RequestBody?,
        purchase_wastage: RequestBody?,
        purchase_making_charges: RequestBody?,
        jobwork_rate: RequestBody?,
        labourwork_rate: RequestBody?,
        sales_purchase_gst_rate_id: RequestBody?,
        sales_purchase_hsn: RequestBody?,
        jobwork_labourwork_gst_rate_id: RequestBody?,
        jobwork_labourwork_sac: RequestBody?,
        sales_rate: RequestBody?,
        purchase_rate: RequestBody?,
        sales_ledger_id: RequestBody?,
        purchase_ledger_id: RequestBody?,
        jobwork_ledger_id: RequestBody?,
        labourwork_ledger_id: RequestBody?,
        discount_ledger_id: RequestBody?,
        tag_prefix: RequestBody?,
        use_stamp: RequestBody?,
        use_gold_color: RequestBody?,
        min_stock_level_gm: RequestBody?,
        min_stock_level_pcs: RequestBody?,
        max_stock_level_gm: RequestBody?,
        max_stock_level_pcs: RequestBody?,
        product_wt: RequestBody?,
        item_rate: RequestBody?,
        vendor_id: RequestBody?,
        gold_colour: RequestBody?,
        item_image: MultipartBody.Part?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = apiHelper.editItem(
                        token, item_id,item_type, item_name, item_code, category_id,
                        notes, metal_type_id, maintain_stock_in_id, unit_id,
                        is_studded, stock_method, tax_preference,
                        sales_wastage,
                        sales_making_charges,
                        purchase_wastage,
                        purchase_making_charges,
                        jobwork_rate,
                        labourwork_rate,
                        sales_purchase_gst_rate_id,
                        sales_purchase_hsn,
                        jobwork_labourwork_gst_rate_id,
                        jobwork_labourwork_sac,
                        sales_rate,
                        purchase_rate,
                        sales_ledger_id,
                        purchase_ledger_id,
                        jobwork_ledger_id,
                        labourwork_ledger_id,
                        discount_ledger_id,
                        tag_prefix,
                        use_stamp,
                        use_gold_color,
                        min_stock_level_gm,
                        min_stock_level_pcs,
                        max_stock_level_gm,
                        max_stock_level_pcs,
                        product_wt,
                        item_rate,
                        vendor_id,
                        gold_colour,
                        item_image
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


}