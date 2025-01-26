package com.goldbookapp.ui.activity.supplier

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputFilter
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.NewSupplierActivityBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.user.BackToLoginActivity
import com.goldbookapp.ui.activity.customer.CustSuppAddressDetailsActivity
import com.goldbookapp.ui.activity.customer.CustSuppTcsTdsMgmtActivity
import com.goldbookapp.ui.activity.viewmodel.NewSupplierViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.edit_supplier_activity.*
import kotlinx.android.synthetic.main.new_customer_activity.*
import kotlinx.android.synthetic.main.new_supplier_activity.*
import kotlinx.android.synthetic.main.recover_account_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.math.RoundingMode
import java.text.DecimalFormat

class NewSupplierActivity : AppCompatActivity() {
    private var isDefaultEnableCalledOnce : Boolean = false
    private lateinit var tcsTdsShareDataModel: TcsTdsShareDataModel
    private lateinit var viewModel: NewSupplierViewModel
    lateinit var binding: NewSupplierActivityBinding

    lateinit var prefs: SharedPreferences
    var is_tcs_applicable: String? = "0"
    lateinit var billingAddress: BillingAddressModel
    lateinit var shipppingAddress: ShippingAddressModel

    var selectedSupplierTypeID: String? = null
    var selectedTaxableTypeID: String? = null
    var selectedGSTTreatment: String? = null
    var openFineNewTerm :String? = null
    var openSilverFineNewTerm :String? = null
    var openCashNewTerm :String? = null

    val df = DecimalFormat("0.00")
    val df1 = DecimalFormat("0.000")
    lateinit var openingFineSuppUpdatedValue: String
    lateinit var openingSilverFineSuppUpdatedValue: String
    lateinit var openingCashSuppUpdatedValue: String
    lateinit var fineLimitSuppUpdatedValue: String
    lateinit var cashLimitSuppUpdatedValue: String

    lateinit var loginModel: LoginModel
    lateinit var dashbordModel: DashboardDetailsModel

    var is_tds_applicable: String? = "0"
    var selectedDeductorType: String? = ""
    var selectedNogType: String? = ""
    var selectedCollectorType: String? = ""
    var selectedNopType: String? = ""
    var selectedNatureofPaymentID: String? = ""
    var selectedNatureofGoodsID: String? = ""
    var isUserRestrLoadedOnce: Boolean = false

    var fineDefaultTermNameList: List<String>? = arrayListOf()
    var fineDefaultTermList: List<ItemDefaultTermModel.Data.Default_term>? = null

    var selectedFineDefaultTermName: String = ""
    var selectedSilverFineDefaultTermName: String = ""
    var selectedCashDefaultTermName: String = ""

    lateinit var popupMenu: PopupMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.new_supplier_activity)

        setupViewModel()
        setupUIandListner()
    }

    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }

    override fun onResume() {
        super.onResume()

        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent) {
                handleConnectivityChange()
            }
        })

        if (prefs.contains(Constants.PREF_BILLING_ADDRESS_KEY) && prefs.contains(Constants.PREF_SHIPPING_ADDRESS_KEY)) {
            var billingAddress: BillingAddressModel = Gson().fromJson(
                prefs[Constants.PREF_BILLING_ADDRESS_KEY, ""],
                BillingAddressModel::class.java
            )
            var addressStringBuilder: StringBuilder = StringBuilder()
            addressStringBuilder
                .append(billingAddress.location.toString().trim()).append(", ")
                .append(billingAddress.area.toString().trim()).append(", ")
                .append(billingAddress.landmark.toString().trim()).append(", ")
                .append(billingAddress.country_name.toString().trim()).append(", ")
                .append(billingAddress.state_name.toString().trim()).append(", ")
                .append(billingAddress.city_name.toString().trim()).append(", ")
                .append(billingAddress.pincode.toString().trim()).append(", ")

            tv_billing_addressNewSupp.text =
                CommonUtils.removeUnwantedComma(addressStringBuilder.toString())

            btnCardAddContactOrAddressNewSupp.visibility = View.GONE
            linear_billing_addressNewSupplier.visibility = View.VISIBLE
        }

        // modify tds/tcs based on settings
        if (loginModel.data!!.company_info!!.tax_settings!!.enable_tcs.equals("1") ||
            loginModel.data!!.company_info!!.tax_settings!!.enable_tds.equals("1")
        ) {
            txtModifyTdsTcsNewSupp.visibility = View.VISIBLE
        } else {
            txtModifyTdsTcsNewSupp.visibility = View.GONE
        }
        getTcsTdsShareDataFromPref()
    }

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected) {
            // Network is available
            CommonUtils.hideInternetDialog()
            getDefaultTerm()
            when (loginModel.data!!.user_info!!.user_type.equals("user", true)) {
                // user type user
                true -> {
                    // apply restriciton
                    when (isUserRestrLoadedOnce) {
                        false -> {
                            defaultDisableAllButtonnUI()
                            userWiseRestriction(loginModel.data?.bearer_access_token)
                        }else->{

                    }
                    }

                }
                false -> {
                    if(!isDefaultEnableCalledOnce){
                        defaultEnableAllButtonnUI()
                        checkBranchType(true)
                    }
                }
            }
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))
        }
    }

    private fun checkBranchType(isFromAdmin: Boolean) {
        when(isFromAdmin){
            true -> {
                // admin
                isDefaultEnableCalledOnce = true
                checkBranchGstNotGst()
            }
            false->{
                // user
                when (binding.radiogroupTaxNewSupp.visibility == View.VISIBLE) {
                    true-> {
                        checkBranchGstNotGst()
                    }else->{

                }
                }
            }
        }

    }
    private fun checkBranchGstNotGst(){
        if (loginModel.data?.branch_info?.branch_type.equals("1", true)) { // GST branch

            selectedTaxableTypeID = "1"
            radioTaxExeNewSupp.isChecked = true
            radioTaxNewSupp.isChecked = true

//            radioTaxExeNewCust.isChecked = false
            //ll_NewCust_GstFields.visibility = View.VISIBLE

        } else { // NON-GST branch
            //  ll_NewCust_GstFields.visibility = View.GONE
            /*tvGSTNewCust.visibility = View.GONE
            tvGSTINNewCust.visibility = View.GONE*/
            selectedTaxableTypeID = "0"
//            radioTaxNewCust.isChecked = false
            radioTaxNewSupp.isChecked = true
            radioTaxExeNewSupp.isChecked = true

        }
    }
    private fun defaultDisableAllButtonnUI() {
        binding.lySuppOpeningfineBal.visibility = View.GONE
        binding.lySuppOpeningcashBal.visibility = View.GONE
        binding.tvFineNewSupp.visibility = View.GONE
        binding.tvCashNewSupp.visibility = View.GONE
        binding.radiogroupTaxNewSupp.visibility = View.GONE
        binding.tvGSTNewSupp.visibility = View.GONE
        binding.tvGSTINNewSupp.visibility = View.GONE
        binding.tvPANNewSupp.visibility = View.GONE
        binding.cradSuppAccountInfo.visibility = View.GONE
    }

    private fun defaultEnableAllButtonnUI() {
        binding.lySuppOpeningfineBal.visibility = View.VISIBLE
        binding.lySuppOpeningcashBal.visibility = View.VISIBLE
        binding.tvFineNewSupp.visibility = View.VISIBLE
        binding.tvCashNewSupp.visibility = View.VISIBLE
        binding.radiogroupTaxNewSupp.visibility = View.VISIBLE
        binding.tvGSTNewSupp.visibility = View.VISIBLE
        binding.tvGSTINNewSupp.visibility = View.VISIBLE
        binding.tvPANNewSupp.visibility = View.VISIBLE
        binding.cradSuppAccountInfo.visibility = View.VISIBLE
    }


    private fun userWiseRestriction(token: String?) {
        if (NetworkUtils.isConnected()) {
            viewModel.userWiseRestriction(token)
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {

                            Status.SUCCESS -> {
                                if (it.data?.status == true) {
                                    applyUserWiseRestriction(it.data.data)
                                    checkBranchType(false)
                                } else {
                                    when (it.data!!.code == Constants.ErrorCode) {
                                        true -> {
                                            Toast.makeText(
                                                this,
                                                it.data.errormessage.message,
                                                Toast.LENGTH_LONG
                                            )
                                                .show()
                                        }
                                        false -> {
                                            CommonUtils.somethingWentWrong(this)
                                        }

                                    }
                                }
                                CommonUtils.hideProgress()
                            }
                            Status.ERROR -> {
                                CommonUtils.hideProgress()
                            }
                            Status.LOADING -> {
                                CommonUtils.showProgress(this)
                            }
                        }
                    }
                })
        }
    }


    private fun applyUserWiseRestriction(data: UserWiseRestrictionModel.Data) {
        isUserRestrLoadedOnce = true
        for (i in 0 until data.fields!!.size) {
            if (data.fields!!.get(i).startsWith(getString(R.string.supp))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_fine_bal), true)) {
                    true -> {
                        binding.cradSuppAccountInfo.visibility = View.VISIBLE
                        binding.lySuppOpeningfineBal.visibility = View.VISIBLE
                    }else->{

                }
                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.supp))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_opening_bal), true)) {
                    true -> {
                        binding.cradSuppAccountInfo.visibility = View.VISIBLE
                        binding.lySuppOpeningcashBal.visibility = View.VISIBLE
                    }else->{

                }

                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.supp))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_fine_limit), true)) {
                    true -> {
                        binding.cradSuppAccountInfo.visibility = View.VISIBLE
                        binding.tvFineNewSupp.visibility = View.VISIBLE
                    }else->{

                }

                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.supp))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_cash_limit), true)) {
                    true -> {
                        binding.cradSuppAccountInfo.visibility = View.VISIBLE
                        binding.tvCashNewSupp.visibility = View.VISIBLE
                    }else->{

                }
                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.supp))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_tax_preference), true)) {
                    true -> {
                        binding.radioTaxNewSupp.isChecked = true
                        binding.radiogroupTaxNewSupp.visibility = View.VISIBLE
                        binding.tvGSTNewSupp.visibility = View.VISIBLE
                        binding.tvGSTINNewSupp.visibility = View.VISIBLE
                    }
                    false->{
                        radioTaxExeNewSupp.isChecked = true
                        selectedTaxableTypeID = "0"
                    }

                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.supp))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_pan_card), true)) {
                    true -> {
                        binding.tvPANNewSupp.visibility = View.VISIBLE
                    }else->{

                }

                }
            }

        }
    }


    private fun getTcsTdsShareDataFromPref() {
        if (prefs.contains(Constants.PREF_TCS_TDS_SHARE_DATA)) {
            val selectedTcsTdsDetails =
                object : TypeToken<TcsTdsShareDataModel>() {}.type
            tcsTdsShareDataModel = Gson().fromJson(
                prefs[Constants.PREF_TCS_TDS_SHARE_DATA, ""],
                selectedTcsTdsDetails
            )
            is_tcs_applicable = tcsTdsShareDataModel.is_tcs_applicable
            is_tds_applicable = tcsTdsShareDataModel.is_tds_applicable
            selectedDeductorType = tcsTdsShareDataModel.selectedDeductorType
            selectedNogType = tcsTdsShareDataModel.selectedNogType
            selectedCollectorType = tcsTdsShareDataModel.selectedCollectorType
            selectedNopType = tcsTdsShareDataModel.selectedNopType
            selectedNatureofPaymentID = tcsTdsShareDataModel.selectedNatureofPaymentID
            selectedNatureofGoodsID = tcsTdsShareDataModel.selectedNatureofGoodsID

        }
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                NewSupplierViewModel::class.java
            )
        binding.setLifecycleOwner(this)
    }

    private fun setupUIandListner() {
        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        )
        dashbordModel = Gson().fromJson(
            prefs[Constants.PREF_DASHBOARD_DETAIL_KEY, ""],
            DashboardDetailsModel::class.java
        )

        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.new_supplier)

        if (prefs.contains(Constants.PREF_BILLING_ADDRESS_KEY) && prefs.contains(Constants.PREF_SHIPPING_ADDRESS_KEY)) {
            prefs.edit().remove(Constants.PREF_BILLING_ADDRESS_KEY).apply()
            prefs.edit().remove(Constants.PREF_SHIPPING_ADDRESS_KEY).apply()
        }
        if (prefs.contains(Constants.PREF_TCS_TDS_SHARE_DATA)) {
            prefs.edit().remove(Constants.PREF_TCS_TDS_SHARE_DATA).apply()
        }

        df.roundingMode = RoundingMode.CEILING
        df1.roundingMode = RoundingMode.CEILING

        applyingDigitFilter()

        btnReset?.clickWithDebounce {
            startActivity(Intent(this, BackToLoginActivity::class.java))
        }

        binding.radiogroupTypeNewSupp.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                binding.radioBusinessNewSupp.id -> {
                    selectedSupplierTypeID = "business"
                    binding.tvCompanyNameNewSupp.visibility = View.VISIBLE
                }
                binding.radioIndividualNewSupp.id -> {
                    selectedSupplierTypeID = "individual"
                    binding.tvCompanyNameNewSupp.visibility = View.GONE
                    binding.txtCompanyNameNewSupp.setText("")
                }
            }
        })

        binding.radiogroupTaxNewSupp.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                binding.radioTaxExeNewSupp.id -> {
                    selectedTaxableTypeID = "0"
                    binding.tvGSTNewSupp.visibility = View.GONE
                    binding.tvGSTINNewSupp.visibility = View.GONE
                    //ll_NewSupp_GstFields.visibility = View.GONE
                }
                binding.radioTaxNewSupp.id -> {
                    selectedTaxableTypeID = "1"
                    binding.tvGSTNewSupp.visibility = View.VISIBLE
                    binding.tvGSTINNewSupp.visibility = View.VISIBLE

                    if(selectedGSTTreatment.equals("register",true) ||
                        selectedGSTTreatment.equals("composite")){
                        binding.tvGSTINNewSupp.visibility = View.VISIBLE
                    }
                    else{
                        binding.tvGSTINNewSupp.visibility = View.GONE
                    }
                    //ll_NewSupp_GstFields.visibility = View.VISIBLE
                }
            }
        })


        /*if (loginModel.data?.branch_info?.branch_type.equals("1", true)) { // GST branch

            selectedTaxableTypeID = "1"
            radioTaxNewSupp.isChecked = true
            // radioTaxExeNewSupp.isChecked = false
            //ll_NewSupp_GstFields.visibility = View.VISIBLE

        } else { // NON-GST branch
          //  ll_NewSupp_GstFields.visibility = View.GONE
            selectedTaxableTypeID = "0"
            // radioTaxNewSupp.isChecked = false
            radioTaxExeNewSupp.isChecked = true

        }*/
        binding.txtFirstNameNewSupp.doAfterTextChanged {
            if (selectedSupplierTypeID == getString(R.string.business).toLowerCase()) {
                if (binding.txtCompanyNameNewSupp.text.toString().trim().isBlank()) {
                    var companyName: String =
                        binding.txtFirstNameNewSupp.text.toString()
                            .trim() + " " + binding.txtLastNameNewSupp.text.toString().trim()
                    binding.txtDisNameNewSupp.setText(companyName.trim())
                } else {
                    binding.txtDisNameNewSupp.setText(binding.txtCompanyNameNewSupp.text.toString().trim())
                }
            } else {
                var companyName: String =
                    binding.txtFirstNameNewSupp.text.toString()
                        .trim() + " " + binding.txtLastNameNewSupp.text.toString().trim()
                binding.txtDisNameNewSupp.setText(companyName.trim())
            }
        }

        binding.txtLastNameNewSupp.doAfterTextChanged {
            if (selectedSupplierTypeID == getString(R.string.business).toLowerCase()) {
                if (binding.txtCompanyNameNewSupp.text.toString().trim().isBlank()) {
                    var companyName: String =
                        binding.txtFirstNameNewSupp.text.toString()
                            .trim() + " " + binding.txtLastNameNewSupp.text.toString().trim()
                    binding.txtDisNameNewSupp.setText(companyName.trim())
                } else {
                    binding.txtDisNameNewSupp.setText(binding.txtCompanyNameNewSupp.text.toString().trim())
                }
            } else {
                var companyName: String =
                    binding.txtFirstNameNewSupp.text.toString()
                        .trim() + " " + binding.txtLastNameNewSupp.text.toString().trim()
                binding.txtDisNameNewSupp.setText(companyName.trim())
            }
        }

        binding.txtCompanyNameNewSupp.doAfterTextChanged {
            if (binding.txtCompanyNameNewSupp.text.toString().trim().isBlank()) {
                var companyName: String =
                    binding.txtFirstNameNewSupp.text.toString()
                        .trim() + " " + binding.txtLastNameNewSupp.text.toString().trim()
                binding.txtDisNameNewSupp.setText(companyName.trim())
            } else {
                binding.txtDisNameNewSupp.setText(binding.txtCompanyNameNewSupp.text.toString().trim())
            }
        }

        binding.txtOpenFineNewSupp.doAfterTextChanged {

            val str: String = binding.txtOpenFineNewSupp.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtOpenFineNewSupp.setText(str2)
                binding.txtOpenFineNewSupp.setSelection(str2.length)
            }

            openingFineSuppUpdatedValue = df1.format(str2.toDouble())
        }

        binding.txtOpenFineNewSupp.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (this::openingFineSuppUpdatedValue.isInitialized) {
                when (binding.txtOpenFineNewSupp.text.isNullOrBlank()) {
                    true -> {
                        openingFineSuppUpdatedValue = "0.000"
                        binding.txtOpenFineNewSupp.setText(openingFineSuppUpdatedValue)
                        binding.txtOpenFineNewSupp.setSelection(openingFineSuppUpdatedValue.length)
                    }
                    else -> {
                        binding.txtOpenFineNewSupp.setText(openingFineSuppUpdatedValue)
                        binding.txtOpenFineNewSupp.setSelection(openingFineSuppUpdatedValue.length)
                    }
                }
            }
        }

        binding.txtOpenSilverFineNewSupp.doAfterTextChanged {

            val str: String = binding.txtOpenSilverFineNewSupp.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtOpenSilverFineNewSupp.setText(str2)
                binding.txtOpenSilverFineNewSupp.setSelection(str2.length)
            }

            openingSilverFineSuppUpdatedValue = df1.format(str2.toDouble())
        }

        binding.txtOpenSilverFineNewSupp.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (this::openingSilverFineSuppUpdatedValue.isInitialized) {
                when (binding.txtOpenSilverFineNewSupp.text.isNullOrBlank()) {
                    true -> {
                        openingSilverFineSuppUpdatedValue = "0.000"
                        binding.txtOpenSilverFineNewSupp.setText(openingSilverFineSuppUpdatedValue)
                        binding.txtOpenSilverFineNewSupp.setSelection(openingSilverFineSuppUpdatedValue.length)
                    }
                    else -> {
                        binding.txtOpenSilverFineNewSupp.setText(openingSilverFineSuppUpdatedValue)
                        binding.txtOpenSilverFineNewSupp.setSelection(openingSilverFineSuppUpdatedValue.length)
                    }
                }
            }
        }
        binding.txtOpenCashNewSupp.doAfterTextChanged {

            val str: String = binding.txtOpenCashNewSupp.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtOpenCashNewSupp.setText(str2)
                binding.txtOpenCashNewSupp.setSelection(str2.length)
            }

            openingCashSuppUpdatedValue = df.format(str2.toDouble())
        }

        binding.txtOpenCashNewSupp.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (this::openingCashSuppUpdatedValue.isInitialized) {
                when (binding.txtOpenCashNewSupp.text.isNullOrBlank()) {
                    true -> {
                        openingCashSuppUpdatedValue = "0.00"
                        binding.txtOpenCashNewSupp.setText(openingCashSuppUpdatedValue)
                        binding.txtOpenCashNewSupp.setSelection(openingCashSuppUpdatedValue.length)
                    }
                    else -> {
                        binding.txtOpenCashNewSupp.setText(openingCashSuppUpdatedValue)
                        binding.txtOpenCashNewSupp.setSelection(openingCashSuppUpdatedValue.length)
                    }
                }
            }
        }

        binding.txtFineNewSupp.doAfterTextChanged {

            val str: String = binding.txtFineNewSupp.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtFineNewSupp.setText(str2)
                binding.txtFineNewSupp.setSelection(str2.length)
            }

            fineLimitSuppUpdatedValue = df1.format(str2.toDouble())
        }
        binding.txtFineNewSupp.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (this::fineLimitSuppUpdatedValue.isInitialized) {
                when (binding.txtFineNewSupp.text.isNullOrBlank()) {
                    true -> {
                        fineLimitSuppUpdatedValue = "0.000"
                        binding.txtFineNewSupp.setText(fineLimitSuppUpdatedValue)
                        binding.txtFineNewSupp.setSelection(fineLimitSuppUpdatedValue.length)
                    }
                    else -> {
                        binding.txtFineNewSupp.setText(fineLimitSuppUpdatedValue)
                        binding.txtFineNewSupp.setSelection(fineLimitSuppUpdatedValue.length)
                    }
                }
            }
        }
        binding.txtCashNewSupp.doAfterTextChanged {

            val str: String = binding.txtCashNewSupp.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtCashNewSupp.setText(str2)
                binding.txtCashNewSupp.setSelection(str2.length)
            }

            cashLimitSuppUpdatedValue = df.format(str2.toDouble())
        }
        binding.txtCashNewSupp.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (this::cashLimitSuppUpdatedValue.isInitialized) {
                when (binding.txtCashNewSupp.text.isNullOrBlank()) {
                    true -> {
                        cashLimitSuppUpdatedValue = "0.00"
                        binding.txtCashNewSupp.setText(cashLimitSuppUpdatedValue)
                        binding.txtCashNewSupp.setSelection(cashLimitSuppUpdatedValue.length)
                    }
                    else -> {
                        binding.txtCashNewSupp.setText(cashLimitSuppUpdatedValue)
                        binding.txtCashNewSupp.setSelection(cashLimitSuppUpdatedValue.length)
                    }
                }
            }
        }


        radioBusinessNewSupp.isChecked = true
        selectedSupplierTypeID = "business"


        imgLeft?.clickWithDebounce {
            onBackPressed()
        }
        btnSaveAdd_AddSupplier?.clickWithDebounce {
            if (performValidation()) {
                if (NetworkUtils.isConnected()) {
                    binding.txtOpenFineNewSupp.clearFocus()
                    binding.txtOpenCashNewSupp.clearFocus()
                    addSupplierAPI(
                        loginModel?.data?.bearer_access_token,
                        loginModel?.data?.company_info?.id,
                        selectedSupplierTypeID,
                        binding.txtMrNewSupp.text.toString().trim(),
                        binding.txtFirstNameNewSupp.text.toString().trim(),
                        binding.txtLastNameNewSupp.text.toString().trim(),
                        binding.txtCompanyNameNewSupp.text.toString().trim(),
                        binding.txtCustCodeNewSupp.text.toString().trim(),
                        binding.txtMobileNewSupp.text.toString().trim(),
                        binding.txtAddNumberNewSupp.text.toString().trim(),
                        binding.txtEmailNewSupp.text.toString().trim(),
                        binding.txtOpenFineNewSupp.text.toString().trim(),
                        openFineNewTerm,
                        binding.txtOpenSilverFineNewSupp.text.toString().trim(),
                        openSilverFineNewTerm,
                        binding.txtOpenCashNewSupp.text.toString().trim(),
                        openCashNewTerm,
                        binding.txtFineNewSupp.text.toString().trim(),
                        binding.txtCashNewSupp.text.toString().trim(),
                        is_tcs_applicable,
                        selectedTaxableTypeID,
                        selectedGSTTreatment,
                        binding.txtGSTINNewSupp.text.toString().trim(),
                        binding.txtPANNewSupp.text.toString().trim(),
                        binding.txtCourierNewSupp.text.toString().trim(),
                        binding.txtNotesNewSupp.text.toString().trim(),
                        billingAddress.is_shipping,
                        Gson().toJson(billingAddress),
                        Gson().toJson(shipppingAddress),
                        binding.txtDisNameNewSupp.text.toString().trim(),
                        is_tds_applicable,
                        selectedDeductorType,
                        selectedCollectorType,
                        selectedNogType,
                        selectedNopType,
                        selectedNatureofPaymentID,
                        selectedNatureofGoodsID
                    )
                }
            }
        }
        binding.txtModifyTdsTcsNewSupp?.clickWithDebounce {
            startActivity(Intent(this, CustSuppTcsTdsMgmtActivity::class.java))
        }
        binding.txtGSTTretmentNewSupp?.clickWithDebounce {
            openGSTTretmentPopup()
        }

        binding.txtOpenFineTermNewSupp.clickWithDebounce {
            openFineTermMenu(fineDefaultTermNameList)
        }

        binding.txtOpenSilverFineTermNewSupp.clickWithDebounce {
            openSilverFineTermMenu(fineDefaultTermNameList)
        }

        binding.txtOpenCashTermNewSupp.clickWithDebounce {
            openCashTermMenu(fineDefaultTermNameList)
        }
        binding.matCardBillingAddressNewSupp?.clickWithDebounce {
            startActivity(
                Intent(
                    this,
                    CustSuppAddressDetailsActivity::class.java
                )
            )
        }


        saveBillingShippingAddressModel()

    }

    fun getDefaultTerm() {
        if (NetworkUtils.isConnected()) {
            viewModel.getDefaultTerm(
                loginModel?.data?.bearer_access_token
            ).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {

                            if (it.data?.status == true) {
                                fineDefaultTermNameList = ArrayList<String>()
                                fineDefaultTermList = it.data.data!!.default_term
                                fineDefaultTermNameList =
                                    fineDefaultTermList?.map { it.default_term.toString() }
                                binding.txtOpenFineTermNewSupp.setText(fineDefaultTermList!!.get(0).default_term)
                                selectedFineDefaultTermName =
                                    fineDefaultTermList!!.get(0).default_term!!
                                openFineNewTerm = fineDefaultTermList!!.get(0).default_term_value!!

                                binding.txtOpenSilverFineTermNewSupp.setText(fineDefaultTermList!!.get(0).default_term)
                                selectedSilverFineDefaultTermName = fineDefaultTermList!!.get(0).default_term!!
                                openSilverFineNewTerm = fineDefaultTermList!!.get(0).default_term_value!!

                                binding.txtOpenCashTermNewSupp.setText(fineDefaultTermList!!.get(0).default_term)
                                openCashNewTerm = fineDefaultTermList!!.get(0).default_term_value!!

                            } else {

                                when (it.data!!.code == Constants.ErrorCode) {
                                    true -> {
                                        Toast.makeText(
                                            this,
                                            it.data.errormessage?.message,
                                            Toast.LENGTH_LONG
                                        )
                                            .show()
                                    }
                                    false -> {
                                        CommonUtils.somethingWentWrong(this)
                                    }

                                }
                            }


                        }
                        Status.ERROR -> {

                        }
                        Status.LOADING -> {

                        }
                    }
                }
            })
        }

    }


    private fun openFineTermMenu(
        fineDefaultTermNameList: List<String>?
    ) {
        popupMenu = PopupMenu(
            this, binding.txtOpenFineTermNewSupp
        )
        for (i in 0 until fineDefaultTermList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                fineDefaultTermNameList!!.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtOpenFineTermNewSupp.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = fineDefaultTermNameList!!.indexOf(selected)

            selectedFineDefaultTermName =
                pos?.let { it1 -> fineDefaultTermList?.get(it1)?.default_term }.toString()

            openFineNewTerm =
                pos?.let { it1 -> fineDefaultTermList?.get(it1)?.default_term_value }.toString()


            true
        })

        popupMenu.show()
    }


    private fun openSilverFineTermMenu(
        fineDefaultTermNameList: List<String>?
    ) {
        popupMenu = PopupMenu(
            this, binding.txtOpenSilverFineTermNewSupp
        )
        for (i in 0 until fineDefaultTermList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                fineDefaultTermNameList!!.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtOpenSilverFineTermNewSupp.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = fineDefaultTermNameList!!.indexOf(selected)

            selectedSilverFineDefaultTermName =
                pos?.let { it1 -> fineDefaultTermList?.get(it1)?.default_term }.toString()

            openSilverFineNewTerm =
                pos?.let { it1 -> fineDefaultTermList?.get(it1)?.default_term_value }.toString()


            true
        })

        popupMenu.show()
    }

    private fun openCashTermMenu(
        fineDefaultTermNameList: List<String>?
    ) {
        popupMenu = PopupMenu(
            this, binding.txtOpenCashTermNewSupp
        )
        for (i in 0 until fineDefaultTermList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                fineDefaultTermNameList!!.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtOpenCashTermNewSupp.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = fineDefaultTermNameList!!.indexOf(selected)

            selectedCashDefaultTermName =
                pos?.let { it1 -> fineDefaultTermList?.get(it1)?.default_term }.toString()

            openCashNewTerm =
                pos?.let { it1 -> fineDefaultTermList?.get(it1)?.default_term_value }.toString()


            true
        })

        popupMenu.show()
    }



    fun openGSTTretmentPopup() {
        val popupMenu: PopupMenu = PopupMenu(this, txtGSTTretmentNewSupp)
        popupMenu.menu.add("Regular")
        popupMenu.menu.add("Unregistered")
        popupMenu.menu.add("Consumer")
        popupMenu.menu.add("Composition")

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            (binding.txtGSTTretmentNewSupp as TextInputEditText).setText(item.title)
            selectedGSTTreatment = when (item.title) {
                "Regular" -> {
                    binding.tvGSTINNewSupp.visibility = View.VISIBLE
                    "register"
                }
                "Unregistered" -> {
                    binding.tvGSTINNewSupp.visibility = View.GONE
                    "unregister"
                }
                "Consumer" -> {
                    binding.tvGSTINNewSupp.visibility = View.GONE
                    "consumer"
                }
                else -> {
                    binding.tvGSTINNewSupp.visibility = View.VISIBLE
                    "composite"
                }
            }
            true
        })

        popupMenu.show()
    }


    private fun applyingDigitFilter() {
        // applying filters to edit input number decimal fields(which have 2 or 3 decimal after .)
        binding.txtOpenFineNewSupp.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )
        binding.txtOpenCashNewSupp.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )

        binding.txtCashNewSupp.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )
        binding.txtFineNewSupp.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )
    }

    fun saveBillingShippingAddressModel() {


        val childBillingModel = BillingAddressModel(
            "",
            "",
            "",
            "",
            "",
            loginModel.data?.company_info?.country_id.toString(),
            loginModel.data?.company_info?.country_name,
            loginModel.data?.company_info?.state_id.toString(),
            loginModel.data?.company_info?.state_name,
            "",
            "",
            "",
            "",
            "",
            "",
            "1"
        )

        val prefs = PreferenceHelper.defaultPrefs(this)
        prefs[Constants.PREF_BILLING_ADDRESS_KEY] = Gson().toJson(childBillingModel) //setter


        val childShippingModel = ShippingAddressModel(
            "",
            "",
            "",
            "", "",
            loginModel.data?.company_info?.country_id.toString(),
            loginModel.data?.company_info?.country_name,
            loginModel.data?.company_info?.state_id.toString(),
            loginModel.data?.company_info?.state_name,
            "",
            "",
            "",
            "",
            "",
            ""
        )

        prefs[Constants.PREF_SHIPPING_ADDRESS_KEY] = Gson().toJson(childShippingModel) //setter

    }


    fun performValidation(): Boolean {

        if (binding.radiogroupTypeNewSupp.getCheckedRadioButtonId() == -1) { // no radio buttons are checked
            CommonUtils.showDialog(
                this,
                getString(R.string.select_business_or_invidual_karigar_msg)
            )
            return false
        } else if (binding.txtFirstNameNewSupp.text.toString().isBlank()) {
            CommonUtils.showDialog(
                this,
                getString(R.string.enter_first_name_msg)/*"Please Enter First Name"*/
            )
            binding.txtFirstNameNewSupp.requestFocus()
            return false
        } else if (selectedSupplierTypeID == getString(R.string.business).toLowerCase() && binding.txtCompanyNameNewSupp.text.toString()
                .isBlank()
        ) {
            CommonUtils.showDialog(
                this,
                getString(R.string.enter_company_name_msg)/*"Please Enter Company Name"*/
            )
            binding.txtCompanyNameNewSupp.requestFocus()
            return false
        } else if (binding.txtCustCodeNewSupp.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.enter_supplier_code_msg))
            binding.txtCustCodeNewSupp.requestFocus()
            return false
        } else if (binding.txtDisNameNewSupp.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.enter_display_name_msg))
            binding.txtDisNameNewSupp.requestFocus()
            return false
        } else if (binding.txtMobileNewSupp.text.toString().isBlank()) {
            CommonUtils.showDialog(
                this,
                getString(R.string.enter_mobile_no_msg)/*"Please enter Mobile number"*/
            )
            binding.txtMobileNewSupp.requestFocus()
            return false
        } else if (binding.txtMobileNewSupp.text?.length!! < 10) {
            CommonUtils.showDialog(
                this,
                getString(R.string.enter_valid_mobileno_msg)/*"Please enter valid Mobile number"*/
            )
            binding.txtMobileNewSupp.requestFocus()
            return false
        }
        when (binding.radiogroupTaxNewSupp.visibility == View.VISIBLE) {
            true -> {
                if (binding.radiogroupTaxNewSupp.getCheckedRadioButtonId() == -1) { // no radio buttons are checked
                    CommonUtils.showDialog(
                        this,
                        getString(R.string.select_tax_exempt_or_taxable_msg)/*"Please Select Tax Exempt or Taxable"*/
                    )
                    return false
                }

                if(selectedTaxableTypeID == "1" ){
                    when (selectedGSTTreatment.equals("register", true)) {
                        true -> {
                            if (binding.txtGSTINNewSupp.text.toString()
                                    .isBlank()
                            ) {
                                CommonUtils.showDialog(
                                    this,
                                    getString(R.string.enter_gstin_msg)/*"Please Enter GSTIN"*/
                                )
                                binding.txtGSTINNewSupp.requestFocus()
                                return false
                            } else if ( !CommonUtils.isValidGSTNo(
                                    binding.txtGSTINNewSupp.text.toString()
                                )
                            ) {
                                CommonUtils.showDialog(
                                    this,
                                    getString(R.string.enter_valid_gstin_msg)/*"Please Enter Valid GSTIN"*/
                                )
                                binding.txtGSTINNewSupp.requestFocus()
                                return false
                            }

                            when (binding.tvPANNewSupp.visibility == View.VISIBLE) {
                                true -> {
                                    if (binding.txtPANNewSupp.text.toString()
                                            .isBlank() || !CommonUtils.isValidPANDetail(txtPANNewSupp.text.toString())
                                    ) {
                                        CommonUtils.showDialog(
                                            this,
                                            getString(R.string.enter_correct_pandetails_msg)
                                        )
                                        binding.txtPANNewSupp.requestFocus()
                                        return false
                                    }
                                }
                                else->{

                                }
                            }
                        }
                        else->{

                        }
                    }

                    when (selectedGSTTreatment.equals("composite", true)) {
                        true -> {
                            if (binding.txtGSTINNewSupp.text.toString()
                                    .isBlank()
                            ) {
                                CommonUtils.showDialog(
                                    this,
                                    getString(R.string.enter_gstin_msg)/*"Please Enter GSTIN"*/
                                )
                                binding.txtGSTINNewSupp.requestFocus()
                                return false
                            } else if (!CommonUtils.isValidGSTNo(
                                    binding.txtGSTINNewSupp.text.toString()
                                )
                            ) {
                                CommonUtils.showDialog(
                                    this,
                                    getString(R.string.enter_valid_gstin_msg)/*"Please Enter Valid GSTIN"*/
                                )
                                binding.txtGSTINNewSupp.requestFocus()
                                return false
                            }

                            when (binding.tvPANNewSupp.visibility == View.VISIBLE) {
                                true -> {
                                    if (binding.txtPANNewSupp.text.toString()
                                            .isBlank() || !CommonUtils.isValidPANDetail(
                                            binding.txtPANNewSupp.text.toString()
                                        )
                                    ) {
                                        CommonUtils.showDialog(
                                            this,
                                            getString(R.string.enter_pan_details)
                                        )
                                        binding.txtPANNewSupp.requestFocus()
                                        return false
                                    }
                                }
                                else->{

                                }
                            }
                        }
                        else->{

                        }
                    }

                    when (binding.tvPANNewSupp.visibility == View.VISIBLE && (selectedGSTTreatment.equals(
                        "register",
                        true
                    ) || selectedGSTTreatment.equals("composite", true))) {
                        true -> {
                            if (binding.radioTaxNewSupp.isChecked == true && (binding.txtPANNewSupp.text.toString()
                                    .isBlank())
                            ) {
                                CommonUtils.showDialog(
                                    this,
                                    getString(R.string.enter_pan_details)
                                )
                                binding.txtPANNewSupp.requestFocus()
                                return false
                            }
                        }
                        else->{

                        }
                    }

                    if (selectedGSTTreatment.isNullOrBlank()) {
                        CommonUtils.showDialog(this, "Please Select GST Treatment")
                        return false
                    }
                }

            }
            else->{

            }
        }



        if (!prefs.contains(Constants.PREF_BILLING_ADDRESS_KEY) && !prefs.contains(Constants.PREF_SHIPPING_ADDRESS_KEY)) {
            CommonUtils.showDialog(this, getString(R.string.enter_address_details))
            return false
        } else if (is_tds_applicable.equals("1") && selectedDeductorType.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.modify_tcs_tds_details))
            return false
        } else if (is_tds_applicable.equals("1") && selectedNopType.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.modify_tcs_tds_details))
            return false
        }
        if (is_tcs_applicable.equals("1") && selectedCollectorType.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.modify_tcs_tds_details))
            return false
        } else if (is_tcs_applicable.equals("1") && selectedNogType.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.modify_tcs_tds_details))
            return false
        }


        billingAddress = Gson().fromJson(
            prefs[Constants.PREF_BILLING_ADDRESS_KEY, ""],
            BillingAddressModel::class.java
        )

        shipppingAddress = Gson().fromJson(
            prefs[Constants.PREF_SHIPPING_ADDRESS_KEY, ""],
            ShippingAddressModel::class.java
        )

        return true
    }


    fun openSuppNameTitlePopup(
        view: View?
    ) {
        val popupMenu: PopupMenu = PopupMenu(this, view)
        popupMenu.menu.add("Mr")
        popupMenu.menu.add("Mrs")
        popupMenu.menu.add("Ms")

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            (view as TextInputEditText).setText(item.title)
            true
        })

        popupMenu.show()
    }


    fun addSupplierAPI(
        token: String?,
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
        is_tds_applicable: String?,
        selectedDeductorType: String?,
        selectedCollectorType: String?,
        selectedNogType: String?,
        selectedNopType: String?,
        selectedNatureofPaymentID: String?,
        selectedNatureofGoodsID: String?
    ) {

        viewModel.addSupplier(
            token, company_id,
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
            selectedNatureofGoodsID
        ).observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        if (it.data?.status == true) {
                            Toast.makeText(
                                this,
                                it.data?.message,
                                Toast.LENGTH_LONG
                            )
                                .show()

                            onBackPressed()

                        } else {
                            when (it.data!!.code == Constants.ErrorCode) {
                                true -> {
                                    Toast.makeText(
                                        this,
                                        it.data.errormessage?.message,
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                }
                                false -> {
                                    CommonUtils.somethingWentWrong(this)
                                }

                            }
                        }

                        CommonUtils.hideProgress()
                    }
                    Status.ERROR -> {
                        CommonUtils.hideProgress()

                    }
                    Status.LOADING -> {
                        CommonUtils.showProgress(this)
                    }
                }
            }
        })
    }

}