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
import com.goldbookapp.model.CustomerDetailModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.fragment.viewmodel.ContactInfoViewModel
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.Status
import com.google.gson.Gson
import kotlinx.android.synthetic.main.customer_detail_activity.*
import kotlinx.android.synthetic.main.fragment_contact_info.view.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.lang.StringBuilder

class ContactInfoFragment(var customerDetailModel: CustomerDetailModel, var contxt: Context) : Fragment(){

    private lateinit var viewModel: ContactInfoViewModel
    var customer_id: String? = ""
    lateinit var loginModel: LoginModel
    lateinit var binding: FragmentContactInfoBinding
    var isFirstime: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contact_info, container, false)
        val view = binding.root

        setupViewModel()
        val prefs = PreferenceHelper.defaultPrefs(contxt)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter
        customer_id = customerDetailModel.customers!!.id
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
                if(customer_id != null && !customer_id!!.isBlank()) {
                        customerDetailAPI(
                            loginModel.data?.bearer_access_token,
                            loginModel.data?.company_info?.id,
                            customer_id
                        )
                }
            }
        }

    }
    fun customerDetailAPI(token: String?,
                          company_id: String?,
                          customer_id: String?){

        if(NetworkUtils.isConnected()) {

            viewModel.customerDetail(token, company_id, customer_id).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {

                                customerDetailModel = it.data
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
                ContactInfoViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }

    fun setupUIandListner(root: View){

        root.tvNameContactInfo.text = customerDetailModel.customers?.first_name
        root.tvMobileNumberContactInfo.text = customerDetailModel.customers?.mobile_number
        root.tvEmailContactInfo.text = customerDetailModel.customers?.email
        root.tvCreditFineContactInfo.text = customerDetailModel.customers?.fine_limit
        root.tvCreditCashContactInfo.text = customerDetailModel.customers?.cash_limit
        when(customerDetailModel.customers?.courier.isNullOrBlank()){
            true->{
                root.tvPreferredVendorLabelContactInfo.visibility = View.GONE
                root.tvPreferredVendorContactInfo.visibility = View.GONE
            }
            false->{
                root.tvPreferredVendorLabelContactInfo.visibility = View.VISIBLE
                root.tvPreferredVendorContactInfo.visibility = View.VISIBLE
                root.tvPreferredVendorContactInfo.text = customerDetailModel.customers?.courier
            }

        }
        when(customerDetailModel.customers?.notes.isNullOrBlank()){
            true->{
                root.tvNotesContactInfo.visibility = View.GONE
                root.tvNotesLabelContactInfo.visibility = View.GONE
            }
            false->{
                root.tvNotesContactInfo.visibility = View.VISIBLE
                root.tvNotesLabelContactInfo.visibility = View.VISIBLE
                root.tvNotesContactInfo.text = customerDetailModel.customers?.notes
            }

        }

        if(customerDetailModel.billing_address != null) {
            root.cardContactInfoAddress.visibility = View.VISIBLE

            var addressStringBuilder: StringBuilder = StringBuilder()
            addressStringBuilder
                .append(customerDetailModel.billing_address?.location?.trim()).append(", ")
                .append(customerDetailModel.billing_address?.area?.trim()).append(", ")
                .append(customerDetailModel.billing_address?.landmark?.trim()).append(", ")
                .append(customerDetailModel.billing_address?.country_name?.trim()).append(", ")
                .append(customerDetailModel.billing_address?.state_name?.trim()).append(", ")
                .append(customerDetailModel.billing_address?.city_name?.trim()).append(", ")
                .append(customerDetailModel.billing_address?.pincode?.trim()).append(", ")

            root.tvAddressContactInfo.text = CommonUtils.removeUnwantedComma(addressStringBuilder.toString())

        }
        else root.cardContactInfoAddress.visibility = View.GONE

    }
}