package com.goldbookapp.ui.activity.customer

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
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.NewCustomerActivityBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.viewmodel.NewCustomerViewModel
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
import kotlinx.android.synthetic.main.add_cashbank_activity.*
import kotlinx.android.synthetic.main.new_customer_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.math.RoundingMode
import java.text.DecimalFormat

class NewCustomerActivity : AppCompatActivity() {
    private var isDefaultEnableCalledOnce: Boolean = false
    private lateinit var tcsTdsShareDataModel: TcsTdsShareDataModel
    private lateinit var viewModel: NewCustomerViewModel
    lateinit var binding: NewCustomerActivityBinding

    var is_tcs_applicable: String? = "0"
    lateinit var prefs: SharedPreferences

    lateinit var billingAddress: BillingAddressModel
    lateinit var shipppingAddress: ShippingAddressModel

    var selectedCustomerTypeID: String? = ""
    var selectedTaxableTypeID: String? = ""
    var selectedGSTTreatment: String? = null
    var openFineNewTerm: String? = null
    var openSilverFineNewTerm: String? = null
    var openCashNewTerm: String? = null

    val df = DecimalFormat("0.00")
    val df1 = DecimalFormat("0.000")
    lateinit var openingFineNewCustUpdatedValue: String
    lateinit var openingSilverFineNewCustUpdatedValue: String
    lateinit var openingCashNewCustUpdatedValue: String
    lateinit var fineLimitNewCustUpdatedValue: String
    lateinit var cashLimitNewCustUpdatedValue: String


    var is_tds_applicable: String? = "0"
    var selectedDeductorType: String? = ""
    var selectedNogType: String? = ""
    var selectedCollectorType: String? = ""
    var selectedNopType: String? = ""
    var selectedNatureofPaymentID: String? = ""
    var selectedNatureofGoodsID: String? = ""
    var isUserRestrLoadedOnce: Boolean = false

    lateinit var loginModel: LoginModel
    lateinit var dashboardModel: DashboardDetailsModel

    var fineDefaultTermNameList: List<String>? = arrayListOf()
    var fineDefaultTermList: List<ItemDefaultTermModel.Data.Default_term>? = null

    var selectedFineDefaultTermName: String = ""
    var selectedSilverFineDefaultTermName: String = ""
    var selectedCashDefaultTermName: String = ""

    lateinit var popupMenu: PopupMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.new_customer_activity)

        setupViewModel()
        setupUIandListner()


    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                NewCustomerViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }

    private fun setupUIandListner() {

        isUserRestrLoadedOnce = false
        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        )
        dashboardModel = Gson().fromJson(
            prefs[Constants.PREF_DASHBOARD_DETAIL_KEY, ""],
            DashboardDetailsModel::class.java
        )

        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.new_customer)

        if (prefs.contains(Constants.PREF_BILLING_ADDRESS_KEY) && prefs.contains(Constants.PREF_SHIPPING_ADDRESS_KEY)) {
            prefs.edit().remove(Constants.PREF_BILLING_ADDRESS_KEY).apply()
            prefs.edit().remove(Constants.PREF_SHIPPING_ADDRESS_KEY).apply()
        }
        if (prefs.contains(Constants.PREF_TCS_TDS_SHARE_DATA)) {
            prefs.edit().remove(Constants.PREF_TCS_TDS_SHARE_DATA).apply()
        }


        imgLeft?.clickWithDebounce {
            onBackPressed()
        }
        btnSaveAdd_AddCustomer?.clickWithDebounce {
            if (performValidation()) {
                if (NetworkUtils.isConnected()) {
                    binding.txtOpenFineNewCust.clearFocus()
                    binding.txtOpenCashNewCust.clearFocus()
                    binding.txtFineNewCust.clearFocus()
                    binding.txtCashNewCust.clearFocus()
                    addCustomerAPI(
                        loginModel?.data?.bearer_access_token,
                        loginModel?.data?.company_info?.id,
                        selectedCustomerTypeID,
                        binding.txtNameTitleNewCust.text.toString().trim(),
                        binding.txtFirstNameNewCust.text.toString().trim(),
                        binding.txtLastNameNewCust.text.toString().trim(),
                        binding.txtCompanyNameNewCust.text.toString().trim(),
                        binding.txtCustCodeNewCust.text.toString().trim(),
                        binding.txtDisNameNewCust.text.toString().trim(),
                        binding.txtMobileNewCust.text.toString().trim(),
                        binding.txtAddNumberNewCust.text.toString().trim(),
                        binding.txtEmailNewCust.text.toString().trim(),
                        binding.txtOpenFineNewCust.text.toString().trim(),
                        openFineNewTerm,
                        binding.txtOpenSilverFineNewCust.text.toString().trim(),
                        openSilverFineNewTerm,
                        binding.txtOpenCashNewCust.text.toString().trim(),
                        openCashNewTerm,
                        binding.txtFineNewCust.text.toString().trim(),
                        binding.txtCashNewCust.text.toString().trim(),
                        is_tcs_applicable,
                        selectedTaxableTypeID,
                        selectedGSTTreatment,
                        binding.txtGSTINNewCust.text.toString().trim(),
                        binding.txtPANNewCust.text.toString().trim(),
                        binding.txtCourierNewCust.text.toString().trim(),
                        binding.txtNotesNewCust.text.toString().trim(),
                        billingAddress.is_shipping,
                        Gson().toJson(billingAddress),
                        Gson().toJson(shipppingAddress),
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
        binding.txtModifyTdsTcsNewCust?.clickWithDebounce {
            startActivity(Intent(this, CustSuppTcsTdsMgmtActivity::class.java))
        }
        binding.txtGSTTretmentNewCust?.clickWithDebounce {
            openGSTTretmentPopup()
        }
        binding.matCardBillingAddress?.clickWithDebounce {
            startActivity(Intent(this, CustSuppAddressDetailsActivity::class.java))
        }

        binding.txtOpenFineTermNewCust.clickWithDebounce {
            openFineTermMenu(fineDefaultTermNameList)
        }

        binding.txtOpenSilverFineTermNewCust.clickWithDebounce {
            openSilverFineTermMenu(fineDefaultTermNameList)
        }

        binding.txtOpenCashTermNewCust.clickWithDebounce {
            openCashTermMenu(fineDefaultTermNameList)
        }


        df.roundingMode = RoundingMode.CEILING
        df1.roundingMode = RoundingMode.CEILING

        binding.radiogroupType.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                binding.radioBusiness.id -> {
                    // selectedCustomerTypeID = "0"
                    selectedCustomerTypeID = "business"
                    binding.tvCompanyNameNewCust.visibility = View.VISIBLE
                }
                binding.radioIndividual.id -> {
                    //  selectedCustomerTypeID = "1"
                    selectedCustomerTypeID = "individual"
                    binding.tvCompanyNameNewCust.visibility = View.GONE
                    binding.txtCompanyNameNewCust.setText("")
                }
            }
        })

        /* if (loginModel.data?.branch_info?.branch_type.equals("1", true)) { // GST branch

             selectedTaxableTypeID = "1"
             radioTaxExeNewCust.isChecked = true
             radioTaxNewCust.isChecked = true

 //            radioTaxExeNewCust.isChecked = false
             //ll_NewCust_GstFields.visibility = View.VISIBLE

         } else { // NON-GST branch
           //  ll_NewCust_GstFields.visibility = View.GONE
             *//*tvGSTNewCust.visibility = View.GONE
            tvGSTINNewCust.visibility = View.GONE*//*
            selectedTaxableTypeID = "0"
//            radioTaxNewCust.isChecked = false
            radioTaxNewCust.isChecked = true
            radioTaxExeNewCust.isChecked = true

        }*/
        applyingDigitFilter()

        binding.radiogroupTaxNewCust.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                binding.radioTaxExeNewCust.id -> {
                    selectedTaxableTypeID = "0"
                    binding.tvGSTNewCust.visibility = View.GONE
                    binding.tvGSTINNewCust.visibility = View.GONE
                    //ll_NewCust_GstFields.visibility = View.GONE

                }
                binding.radioTaxNewCust.id -> {
                    selectedTaxableTypeID = "1"
                    binding.tvGSTNewCust.visibility = View.VISIBLE
                    binding.tvGSTINNewCust.visibility = View.VISIBLE

                    if (selectedGSTTreatment.equals("register", true) ||
                        selectedGSTTreatment.equals("composite")
                    ) {
                        binding.tvGSTINNewCust.visibility = View.VISIBLE
                    } else {
                        binding.tvGSTINNewCust.visibility = View.GONE
                    }

                    //ll_NewCust_GstFields.visibility = View.VISIBLE
                }
            }
        })

        binding.txtFirstNameNewCust.doAfterTextChanged {
            if (selectedCustomerTypeID == getString(R.string.business).toLowerCase()) {
                if (binding.txtCompanyNameNewCust.text.toString().trim().isBlank()) {
                    var companyName: String =
                        binding.txtFirstNameNewCust.text.toString()
                            .trim() + " " + binding.txtLastNameNewCust.text.toString().trim()
                    binding.txtDisNameNewCust.setText(companyName.trim())
                } else {
                    binding.txtDisNameNewCust.setText(binding.txtCompanyNameNewCust.text.toString().trim())
                }
            } else {
                var companyName: String =
                    binding.txtFirstNameNewCust.text.toString()
                        .trim() + " " + binding.txtLastNameNewCust.text.toString().trim()
                binding.txtDisNameNewCust.setText(companyName.trim())
            }
        }

        binding.txtLastNameNewCust.doAfterTextChanged {
            if (selectedCustomerTypeID == getString(R.string.business).toLowerCase()) {
                if (binding.txtCompanyNameNewCust.text.toString().trim().isBlank()) {
                    var companyName: String =
                        binding.txtFirstNameNewCust.text.toString()
                            .trim() + " " + binding.txtLastNameNewCust.text.toString().trim()
                    binding.txtDisNameNewCust.setText(companyName.trim())
                } else {
                    binding.txtDisNameNewCust.setText(binding.txtCompanyNameNewCust.text.toString().trim())
                }
            } else {
                var companyName: String =
                    binding.txtFirstNameNewCust.text.toString()
                        .trim() + " " + binding.txtLastNameNewCust.text.toString().trim()
                binding.txtDisNameNewCust.setText(companyName.trim())
            }
        }

        binding.txtCompanyNameNewCust.doAfterTextChanged {
            if (binding.txtCompanyNameNewCust.text.toString().trim().isBlank()) {
                var companyName: String =
                    binding.txtFirstNameNewCust.text.toString()
                        .trim() + " " + binding.txtLastNameNewCust.text.toString().trim()
                binding.txtDisNameNewCust.setText(companyName.trim())
            } else {
                binding.txtDisNameNewCust.setText(binding.txtCompanyNameNewCust.text.toString().trim())
            }
        }



        binding.txtOpenFineNewCust.doAfterTextChanged {

            val str: String = binding.txtOpenFineNewCust.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtOpenFineNewCust.setText(str2)
                binding.txtOpenFineNewCust.setSelection(str2.length)
            }

            openingFineNewCustUpdatedValue = df1.format(str2.toDouble())
        }

        binding.txtOpenFineNewCust.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (this::openingFineNewCustUpdatedValue.isInitialized) {
                when (binding.txtOpenFineNewCust.text.isNullOrBlank()) {
                    true -> {
                        openingFineNewCustUpdatedValue = "0.000"
                        binding.txtOpenFineNewCust.setText(openingFineNewCustUpdatedValue)
                        binding.txtOpenFineNewCust.setSelection(openingFineNewCustUpdatedValue.length)
                    }
                    else -> {
                        binding.txtOpenFineNewCust.setText(openingFineNewCustUpdatedValue)
                        binding.txtOpenFineNewCust.setSelection(openingFineNewCustUpdatedValue.length)
                    }
                }
            }
        }

        binding.txtOpenSilverFineNewCust.doAfterTextChanged {

            val str: String = binding.txtOpenSilverFineNewCust.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtOpenSilverFineNewCust.setText(str2)
                binding.txtOpenSilverFineNewCust.setSelection(str2.length)
            }

            openingSilverFineNewCustUpdatedValue = df1.format(str2.toDouble())
        }

        binding.txtOpenSilverFineNewCust.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (this::openingSilverFineNewCustUpdatedValue.isInitialized) {
                when (binding.txtOpenSilverFineNewCust.text.isNullOrBlank()) {
                    true -> {
                        openingSilverFineNewCustUpdatedValue = "0.000"
                        binding.txtOpenSilverFineNewCust.setText(openingSilverFineNewCustUpdatedValue)
                        binding.txtOpenSilverFineNewCust.setSelection(openingSilverFineNewCustUpdatedValue.length)
                    }
                    else -> {
                        binding.txtOpenSilverFineNewCust.setText(openingSilverFineNewCustUpdatedValue)
                        binding.txtOpenSilverFineNewCust.setSelection(openingSilverFineNewCustUpdatedValue.length)
                    }
                }
            }
        }



        binding.txtFineNewCust.doAfterTextChanged {

            val str: String = binding.txtFineNewCust.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtFineNewCust.setText(str2)
                binding.txtFineNewCust.setSelection(str2.length)
            }

            fineLimitNewCustUpdatedValue = df1.format(str2.toDouble())
        }
        binding.txtFineNewCust.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (this::fineLimitNewCustUpdatedValue.isInitialized) {
                when (binding.txtFineNewCust.text.isNullOrBlank()) {
                    true -> {
                        fineLimitNewCustUpdatedValue = "0.000"
                        binding.txtFineNewCust.setText(fineLimitNewCustUpdatedValue)
                        binding.txtFineNewCust.setSelection(fineLimitNewCustUpdatedValue.length)
                    }
                    else -> {
                        binding.txtFineNewCust.setText(fineLimitNewCustUpdatedValue)
                        binding.txtFineNewCust.setSelection(fineLimitNewCustUpdatedValue.length)
                    }
                }
            }
        }
        binding.txtCashNewCust.doAfterTextChanged {

            val str: String = binding.txtCashNewCust.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtCashNewCust.setText(str2)
                binding.txtCashNewCust.setSelection(str2.length)
            }

            cashLimitNewCustUpdatedValue = df.format(str2.toDouble())
        }
        binding.txtCashNewCust.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (this::cashLimitNewCustUpdatedValue.isInitialized) {
                when (binding.txtCashNewCust.text.isNullOrBlank()) {
                    true -> {
                        cashLimitNewCustUpdatedValue = "0.00"
                        binding.txtCashNewCust.setText(cashLimitNewCustUpdatedValue)
                        binding.txtCashNewCust.setSelection(cashLimitNewCustUpdatedValue.length)
                    }
                    else -> {
                        binding.txtCashNewCust.setText(cashLimitNewCustUpdatedValue)
                        binding.txtCashNewCust.setSelection(cashLimitNewCustUpdatedValue.length)
                    }
                }
            }
        }
        binding.txtOpenCashNewCust.doAfterTextChanged {

            val str: String = binding.txtOpenCashNewCust.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtOpenCashNewCust.setText(str2)
                binding.txtOpenCashNewCust.setSelection(str2.length)
            }

            openingCashNewCustUpdatedValue = df.format(str2.toDouble())
        }
        binding.txtOpenCashNewCust.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (this::openingCashNewCustUpdatedValue.isInitialized) {
                when (binding.txtOpenCashNewCust.text.isNullOrBlank()) {
                    true -> {
                        openingCashNewCustUpdatedValue = "0.00"
                        binding.txtOpenCashNewCust.setText(openingCashNewCustUpdatedValue)
                        binding.txtOpenCashNewCust.setSelection(openingCashNewCustUpdatedValue.length)
                    }
                    else -> {
                        binding.txtOpenCashNewCust.setText(openingCashNewCustUpdatedValue)
                        binding.txtOpenCashNewCust.setSelection(openingCashNewCustUpdatedValue.length)
                    }
                }
            }
        }
        radioBusiness.isChecked = true
        selectedCustomerTypeID = "business"
        saveBillingShippingAddressModel()

    }

    private fun applyingDigitFilter() {
        // applying filters to edit input number decimal fields(which have 2 or 3 decimal after .)
        binding.txtOpenFineNewCust.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )
        binding.txtFineNewCust.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )

        binding.txtOpenCashNewCust.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )
        binding.txtCashNewCust.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )
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

            tv_billing_address.text =
                CommonUtils.removeUnwantedComma(addressStringBuilder.toString())

            binding.btnCardAddContactOrAddress.visibility = View.GONE
            linear_billing_address.visibility = View.VISIBLE
        }
        // modify tds/tcs based on settings
        if (loginModel.data!!.company_info!!.tax_settings!!.enable_tcs.equals("1") ||
            loginModel.data!!.company_info!!.tax_settings!!.enable_tds.equals("1")
        ) {
            binding.txtModifyTdsTcsNewCust.visibility = View.VISIBLE
        } else {
            binding.txtModifyTdsTcsNewCust.visibility = View.GONE
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
                        }
                        else->{

                        }
                    }
                }
                // user type user
                false -> {
                    if (!isDefaultEnableCalledOnce) {
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
        when (isFromAdmin) {
            true -> {
                // admin
                isDefaultEnableCalledOnce = true
                checkBranchGstNotGst()
            }
            false -> {
                // user
                when (binding.radiogroupTaxNewCust.visibility == View.VISIBLE) {
                    true -> {
                        checkBranchGstNotGst()
                    }else->{

                }
                }
            }
        }

    }

    private fun checkBranchGstNotGst() {
        if (loginModel.data?.branch_info?.branch_type.equals("1", true)) { // GST branch

            selectedTaxableTypeID = "1"
            binding.radioTaxExeNewCust.isChecked = true
            binding.radioTaxNewCust.isChecked = true

//            radioTaxExeNewCust.isChecked = false
            //ll_NewCust_GstFields.visibility = View.VISIBLE

        } else { // NON-GST branch
            //  ll_NewCust_GstFields.visibility = View.GONE
            /*tvGSTNewCust.visibility = View.GONE
            tvGSTINNewCust.visibility = View.GONE*/
            selectedTaxableTypeID = "0"
//            radioTaxNewCust.isChecked = false
            binding.radioTaxNewCust.isChecked = true
            binding.radioTaxExeNewCust.isChecked = true

        }
    }

    private fun defaultDisableAllButtonnUI() {
        binding.lyCustOpeningFineBal.visibility = View.GONE
        binding.lyCustOpeningCashBal.visibility = View.GONE
        binding.tvFineNewCust.visibility = View.GONE
        binding.tvCashNewCust.visibility = View.GONE
        binding.radiogroupTaxNewCust.visibility = View.GONE
        binding.tvGSTNewCust.visibility = View.GONE
        binding.tvGSTINNewCust.visibility = View.GONE
        binding.tvPANNewCust.visibility = View.GONE
        binding.tvPANNewCust.visibility = View.GONE
        binding.cardCustAccountInfo.visibility = View.GONE
    }

    private fun defaultEnableAllButtonnUI() {
        binding.lyCustOpeningFineBal.visibility = View.VISIBLE
        binding.lyCustOpeningCashBal.visibility = View.VISIBLE
        binding.tvFineNewCust.visibility = View.VISIBLE
        binding.tvCashNewCust.visibility = View.VISIBLE
        binding.radiogroupTaxNewCust.visibility = View.VISIBLE
        binding.tvGSTNewCust.visibility = View.VISIBLE
        binding.tvGSTINNewCust.visibility = View.VISIBLE
        binding.tvPANNewCust.visibility = View.VISIBLE
        binding.tvPANNewCust.visibility = View.VISIBLE
        binding.cardCustAccountInfo.visibility = View.VISIBLE
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
                                binding.txtOpenFineTermNewCust.setText(fineDefaultTermList!!.get(0).default_term)
                                selectedFineDefaultTermName =
                                    fineDefaultTermList!!.get(0).default_term!!
                                openFineNewTerm = fineDefaultTermList!!.get(0).default_term_value!!

                                selectedSilverFineDefaultTermName = fineDefaultTermList!!.get(0).default_term!!
                                openSilverFineNewTerm = fineDefaultTermList!!.get(0).default_term_value!!
                                binding.txtOpenSilverFineTermNewCust.setText(fineDefaultTermList!!.get(0).default_term)

                                binding.txtOpenCashTermNewCust.setText(fineDefaultTermList!!.get(0).default_term)
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
            if (data.fields!!.get(i).startsWith(getString(R.string.customers))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_fine_bal), true)) {
                    true -> {
                        binding.cardCustAccountInfo.visibility = View.VISIBLE
                        binding.lyCustOpeningFineBal.visibility = View.VISIBLE
                    }else->{

                }
                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.customers))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_opening_bal), true)) {
                    true -> {
                        binding.cardCustAccountInfo.visibility = View.VISIBLE
                        binding.lyCustOpeningCashBal.visibility = View.VISIBLE
                    }
                    else->{

                    }
                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.customers))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_fine_limit), true)) {
                    true -> {
                        binding.cardCustAccountInfo.visibility = View.VISIBLE
                        binding.tvFineNewCust.visibility = View.VISIBLE
                    }
                    else->{

                    }
                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.customers))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_cash_limit), true)) {
                    true -> {
                        binding.cardCustAccountInfo.visibility = View.VISIBLE
                        binding.tvCashNewCust.visibility = View.VISIBLE
                    }
                    else->{

                    }
                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.customers))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_tax_preference), true)) {
                    true -> {
                        radioTaxNewCust.isChecked = true
                        //radioTaxExeNewCust.isChecked = false
                        binding.radiogroupTaxNewCust.visibility = View.VISIBLE
                        binding.tvGSTNewCust.visibility = View.VISIBLE
                        binding.tvGSTINNewCust.visibility = View.VISIBLE

                    }
                    false -> {
                        //radioTaxNewCust.isChecked = false
                        binding.radioTaxExeNewCust.isChecked = true
                        selectedTaxableTypeID = "0"
                    }

                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.customers))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_pan_card), true)) {
                    true -> {
                        binding.tvPANNewCust.visibility = View.VISIBLE
                    }
                    else->{

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
            ""
        )

        prefs[Constants.PREF_SHIPPING_ADDRESS_KEY] = Gson().toJson(childShippingModel) //setter

    }


    fun performValidation(): Boolean {

        if (binding.radiogroupType.getCheckedRadioButtonId() == -1) { // no radio buttons are checked
            CommonUtils.showDialog(this, "Please Select Business or Individual")
            return false
        } else if (binding.txtFirstNameNewCust.text.toString().isBlank()) {
            CommonUtils.showDialog(this, "Please Enter First Name")
            binding.txtFirstNameNewCust.requestFocus()
            return false
        } else if (selectedCustomerTypeID == getString(R.string.business).toLowerCase() && binding.txtCompanyNameNewCust.text.toString()
                .isBlank()
        ) {
            CommonUtils.showDialog(this, "Please Enter Company Name")
            binding.txtCompanyNameNewCust.requestFocus()
            return false
        } else if (binding.txtCustCodeNewCust.text.toString().isBlank()) {
            CommonUtils.showDialog(this, "Please Enter Customer Code")
            binding.txtCustCodeNewCust.requestFocus()
            return false
        } else if (binding.txtDisNameNewCust.text.toString().isBlank()) {
            CommonUtils.showDialog(this, "Please Enter Display Name")
            binding.txtDisNameNewCust.requestFocus()
            return false
        } else if (binding.txtMobileNewCust.text.toString().isBlank()) {
            CommonUtils.showDialog(this, "Please Enter Mobile Number")
            binding.txtMobileNewCust.requestFocus()
            return false
        } else if (binding.txtMobileNewCust.text?.length!! < 10) {
            CommonUtils.showDialog(this, "Please Enter Valid Mobile Number")
            binding.txtMobileNewCust.requestFocus()
            return false
        }/*else if (selectedTaxableTypeID == "1" && (txtPANNewCust.text.toString()
                .isBlank() || !CommonUtils.isValidPANDetail(txtPANNewCust.text.toString()))
        ) {
            CommonUtils.showDialog(this, "Please Enter Correct PAN Details")
            txtPANNewCust.requestFocus()
            return false
        } */ else if (!prefs.contains(Constants.PREF_BILLING_ADDRESS_KEY) && !prefs.contains(
                Constants.PREF_SHIPPING_ADDRESS_KEY
            )
        ) {
            CommonUtils.showDialog(this, "Please Enter Address Details")
            return false
        }

        when (binding.radiogroupTaxNewCust.visibility == View.VISIBLE) {
            true -> {
                if (binding.radiogroupTaxNewCust.getCheckedRadioButtonId() == -1) { // no radio buttons are checked

                    CommonUtils.showDialog(this, "Please Select Tax Exempt or Taxable")
                    return false
                }

                if (selectedTaxableTypeID == "1") {
                    when (selectedGSTTreatment.equals("register", true)) {
                        true -> {
                            if (binding.txtGSTINNewCust.text.toString()
                                    .isBlank()
                            ) {

                                CommonUtils.showDialog(this, "Please Enter GSTIN")
                                binding.txtGSTINNewCust.requestFocus()
                                return false
                            } else if (!CommonUtils.isValidGSTNo(binding.txtGSTINNewCust.text.toString())/*txtGSTINNewCust.text?.length!! < 15*/) {
                                CommonUtils.showDialog(this, "Please Enter Valid GSTIN")
                                binding.txtGSTINNewCust.requestFocus()
                                return false
                            }
                            when (binding.tvPANNewCust.visibility == View.VISIBLE) {
                                true -> {
                                    if (binding.txtPANNewCust.text.toString()
                                            .isBlank() || !CommonUtils.isValidPANDetail(
                                            binding.txtPANNewCust.text.toString()
                                        )
                                    ) {
                                        CommonUtils.showDialog(
                                            this,
                                            "Please Enter Correct PAN Details"
                                        )
                                        binding.txtPANNewCust.requestFocus()
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
                            if (binding.txtGSTINNewCust.text.toString()
                                    .isBlank()
                            ) {

                                CommonUtils.showDialog(this, "Please Enter GSTIN")
                                binding.txtGSTINNewCust.requestFocus()
                                return false
                            } else if (!CommonUtils.isValidGSTNo(
                                    binding.txtGSTINNewCust.text.toString()
                                )/*txtGSTINNewCust.text?.length!! < 15*/) {
                                CommonUtils.showDialog(this, "Please Enter Valid GSTIN")
                                binding.txtGSTINNewCust.requestFocus()
                                return false
                            }
                            when (binding.tvPANNewCust.visibility == View.VISIBLE) {
                                true -> {
                                    if (binding.txtPANNewCust.text.toString()
                                            .isBlank() || !CommonUtils.isValidPANDetail(
                                            binding.txtPANNewCust.text.toString()
                                        )
                                    ) {
                                        CommonUtils.showDialog(
                                            this,
                                            "Please Enter Correct PAN Details"
                                        )
                                        binding.txtPANNewCust.requestFocus()
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

                    when (binding.tvPANNewCust.visibility == View.VISIBLE && (selectedGSTTreatment.equals(
                        "register",
                        true
                    ) || selectedGSTTreatment.equals("composite", true))) {
                        true -> {
                            if (binding.txtPANNewCust.text.toString()
                                    .isBlank() || !CommonUtils.isValidPANDetail(binding.txtPANNewCust.text.toString())
                            ) {
                                CommonUtils.showDialog(this, "Please Enter Correct PAN Details")
                                binding.txtPANNewCust.requestFocus()
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


        /* if (!prefs.contains(Constants.PREF_BILLING_ADDRESS_KEY) && !prefs.contains(Constants.PREF_SHIPPING_ADDRESS_KEY)) {
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
         }*/


        //getter
        billingAddress = Gson().fromJson(
            prefs[Constants.PREF_BILLING_ADDRESS_KEY, ""],
            BillingAddressModel::class.java
        )
        //getter
        shipppingAddress = Gson().fromJson(
            prefs[Constants.PREF_SHIPPING_ADDRESS_KEY, ""],
            ShippingAddressModel::class.java
        )
        return true
    }


    private fun openFineTermMenu(
        fineDefaultTermNameList: List<String>?
    ) {
        popupMenu = PopupMenu(
            this, binding.txtOpenFineTermNewCust
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
            binding.txtOpenFineTermNewCust.setText(item.title)
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
            this, binding.txtOpenSilverFineTermNewCust
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
            binding.txtOpenSilverFineTermNewCust.setText(item.title)
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
            this, binding.txtOpenCashTermNewCust
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
            binding.txtOpenCashTermNewCust.setText(item.title)
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
        popupMenu = PopupMenu(this, binding.txtGSTTretmentNewCust)
        popupMenu.menu.add("Regular")
        popupMenu.menu.add("Unregistered")
        popupMenu.menu.add("Consumer")
        popupMenu.menu.add("Composition")

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            (binding.txtGSTTretmentNewCust as TextInputEditText).setText(item.title)
            selectedGSTTreatment = when (item.title) {
                "Regular" -> {
                    binding.tvGSTINNewCust.visibility = View.VISIBLE
                    "register"
                }
                "Unregistered" -> {
                    binding.tvGSTINNewCust.visibility = View.GONE
                    "unregister"

                }
                "Consumer" -> {
                    binding.tvGSTINNewCust.visibility = View.GONE
                    "consumer"
                }
                else -> {
                    binding.tvGSTINNewCust.visibility = View.VISIBLE
                    "composite"
                }
            }
            true
        })

        popupMenu.show()
    }

    fun openNameTitlePopup(view: View?) {
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

    fun addCustomerAPI(
        token: String?,
        company_id: String?,
        customer_type: String?,
        title: String?,
        first_name: String?,
        last_name: String?,
        company_name: String?,
        customer_code: String?,
        display_name: String?,
        mobile_number: String?,
        secondary_contact: String?,
        email: String?,
        opening_fine_balance: String?,
        opening_fine_default_term: String?,
        opening_silver_fine_balance: String?,
        opening_silver_fine_default_term: String?,
        opening_cash_balance: String?,
        opening_cash_default_term: String?,
        fine_limit: String?,
        cash_limit: String?,
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
        is_tds_applicable: String?,
        selectedDeductorType: String?,
        selectedCollectorType: String?,
        selectedNogType: String?,
        selectedNopType: String?,
        selectedNatureofPaymentID: String?,
        selectedNatureofGoodsID: String?
    ) {

        viewModel.addCustomer(
            token, company_id,
            customer_type,
            title,
            first_name,
            last_name,
            company_name,
            customer_code,
            display_name,
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