package com.goldbookapp.ui.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.FragmentContactInfoBinding
import com.goldbookapp.databinding.FragmentSupplierContactInfoBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.SupplierDetailModel
import com.goldbookapp.ui.fragment.viewmodel.SupplierContactInfoViewModel
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.Status
import com.google.gson.Gson
import kotlinx.android.synthetic.main.customer_detail_activity.*
import kotlinx.android.synthetic.main.fragment_supplier_contact_info.view.*
import java.lang.StringBuilder

class SupplierContactInfoFrgment(var supplierDetailModel: SupplierDetailModel, var contxt: Context) : Fragment(){
    var supplier_id: String? = ""
    lateinit var loginModel: LoginModel
    var isFirstime: Boolean = false
    private lateinit var viewModel: SupplierContactInfoViewModel

    lateinit var binding: FragmentSupplierContactInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_supplier_contact_info, container, false)
        val view = binding.root

        setupViewModel()
        val prefs = PreferenceHelper.defaultPrefs(contxt)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter
        supplier_id = supplierDetailModel.vendors!!.id
        isFirstime = true
        setupUIandListner(view)

        return view
    }
    override fun onResume() {
        super.onResume()

        when(isFirstime){
            true->{
                // nothing to load required as it is already loaded(updated data by calling setupuilistener)
                isFirstime = false
            }
            false->{
                if(supplier_id != null && !supplier_id!!.isBlank()) {
                        supplierDetailAPI(
                            loginModel.data?.bearer_access_token,
                            loginModel.data?.company_info?.id,
                            supplier_id
                        )
                }
            }
        }

    }
    fun supplierDetailAPI(token: String?,
                          company_id: String?,
                          supplier_id: String?){

        if(NetworkUtils.isConnected()) {

            viewModel.supplierDetail(token, company_id, supplier_id).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {

                                supplierDetailModel = it.data
                                setupUIandListner(binding.root)

                                CommonUtils.hideProgress()

                            } else {
                                when(it.data!!.code == Constants.ErrorCode){
                                    true-> {
                                        Toast.makeText(
                                            contxt,
                                            it.data.errormessage?.message,
                                            Toast.LENGTH_LONG
                                        )
                                            .show()
                                    }
                                    false->{
                                        CommonUtils.somethingWentWrong(contxt)
                                    }

                                }
                            }
                            CommonUtils.hideProgress()

                        }
                        Status.ERROR -> {
                            CommonUtils.hideProgress()

                        }
                        Status.LOADING -> {
                            CommonUtils.showProgress(contxt)
                        }
                    }
                }
            })
        }
    }
    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                SupplierContactInfoViewModel::class.java
            )
        binding.setLifecycleOwner(this)
    }

    fun setupUIandListner(root: View){

        root.tvNameSuppContactInfo.text = supplierDetailModel.vendors?.first_name
        root.tvMobileNumberSuppContactInfo.text = supplierDetailModel.vendors?.mobile_number
        root.tvEmailSuppContactInfo.text = supplierDetailModel.vendors?.email
        root.tvCreditFineSuppContactInfo.text = supplierDetailModel.vendors?.fine_limit
        root.tvCreditCashSuppContactInfo.text = supplierDetailModel.vendors?.cash_limit

        when(supplierDetailModel.vendors?.courier.isNullOrBlank()){
            true->{
                root.tvPreferredVendorSuppLabelContactInfo.visibility = View.GONE
                root.tvPreferredVendorSuppContactInfo.visibility = View.GONE
            }
            false->{
                root.tvPreferredVendorSuppLabelContactInfo.visibility = View.VISIBLE
                root.tvPreferredVendorSuppContactInfo.visibility = View.VISIBLE
                root.tvPreferredVendorSuppContactInfo.text = supplierDetailModel.vendors?.courier
            }

        }
        when(supplierDetailModel.vendors?.notes.isNullOrBlank()){
            true->{
                root.tvNotesSuppLabelContactInfo.visibility = View.GONE
                root.tvNotesSuppContactInfo.visibility = View.GONE
            }
            false->{
                root.tvNotesSuppLabelContactInfo.visibility = View.VISIBLE
                root.tvNotesSuppContactInfo.visibility = View.VISIBLE
                root.tvNotesSuppContactInfo.text = supplierDetailModel.vendors?.notes
            }

        }


        if(supplierDetailModel.billing_address != null) {
            root.cardContactInfoAddressSupp.visibility = View.VISIBLE

        var stringBuilder = StringBuilder()
        stringBuilder.append(if(supplierDetailModel.billing_address?.location?.isBlank()!!){""} else {supplierDetailModel.billing_address?.location + ","} )
        stringBuilder.append(if(supplierDetailModel.billing_address?.area?.isBlank()!!){""} else {supplierDetailModel.billing_address?.area + ","} )
        stringBuilder.append(if(supplierDetailModel.billing_address?.landmark?.isBlank()!!){""} else {supplierDetailModel.billing_address?.landmark + ","} )
        stringBuilder.append(if(supplierDetailModel.billing_address?.city_name?.isBlank()!!){""} else {supplierDetailModel.billing_address?.city_name + ","} )
        stringBuilder.append(if(supplierDetailModel.billing_address?.state_name?.isBlank()!!){""} else {supplierDetailModel.billing_address?.state_name + ","} )
        stringBuilder.append(if(supplierDetailModel.billing_address?.country_name?.isBlank()!!){""} else {supplierDetailModel.billing_address?.country_name + ","} )
        //stringBuilder.append(if(customerDetailModel.billing_address?.pincode?.isBlank()!!){""} else {" - " + customerDetailModel.billing_address?.pincode} + ",")

        if(stringBuilder.length > 0){
            stringBuilder.deleteCharAt(stringBuilder.length - 1)
        }

        root.tvAddressSuppContactInfo.text = stringBuilder
        }
        else root.cardContactInfoAddressSupp.visibility = View.GONE


    }


}