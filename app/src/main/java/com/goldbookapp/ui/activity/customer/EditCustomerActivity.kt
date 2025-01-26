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
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.EditCustomerActivityBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.viewmodel.EditCustomerViewModel
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
import kotlinx.android.synthetic.main.edit_customer_activity.*
import kotlinx.android.synthetic.main.new_customer_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.math.RoundingMode
import java.text.DecimalFormat

class EditCustomerActivity : AppCompatActivity() {
    private var isLoadedOnce: Boolean = false
    private lateinit var tcsTdsShareDataModel: TcsTdsShareDataModel
    private lateinit var viewModel: EditCustomerViewModel
    lateinit var binding: EditCustomerActivityBinding
    var is_tcs_applicable: String? = "0"
    lateinit var prefs: SharedPreferences

    lateinit var customerDetailModel: CustomerDetailModel

    lateinit var billingAddress: BillingAddressModel
    lateinit var shipppingAddress: ShippingAddressModel

    var selectedCustomerTypeID: String? = ""
    var selectedTaxableTypeID: String? = ""
    var selectedGSTTreatment: String? = null
    var openFineNewTerm: String? = ""
    var openSilverFineNewTerm: String? = ""
    var openCashNewTerm: String? = ""

    val df = DecimalFormat("0.00")
    val df1 = DecimalFormat("0.000")
    lateinit var openingFineNewCustUpdatedValue: String
    lateinit var openingSilverFineNewCustUpdatedValue: String
    lateinit var openingCashNewCustUpdatedValue: String
    lateinit var fineLimitNewCustUpdatedValue: String
    lateinit var cashLimitNewCustUpdatedValue: String

    lateinit var loginModel: LoginModel
    lateinit var dashboardModel: DashboardDetailsModel
    var customerID: String? = ""

    var is_tds_applicable: String? = "0"
    var selectedDeductorType: String? = ""
    var selectedNogType: String? = ""
    var selectedCollectorType: String? = ""
    var selectedNopType: String? = ""
    var selectedNatureofPaymentID: String? = ""
    var selectedNatureofGoodsID: String? = ""
    var isUserRestrLoadedOnce: Boolean = false
    var isFromDetails: Boolean = false

    var fineDefaultTermNameList: List<String>? = arrayListOf()
    var fineDefaultTermList: List<ItemDefaultTermModel.Data.Default_term>? = null

    var selectedFineDefaultTermName: String = ""
    var selectedSilverFineDefaultTermName: String = ""
    var selectedCashDefaultTermName: String = ""
    lateinit var popupMenu: PopupMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.edit_customer_activity)

        setupViewModel()
        setupUIandListner()
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

            tv_billing_address_EditCust.text =
                CommonUtils.removeUnwantedComma(addressStringBuilder.toString())

            btnCardAddContactOrAddressEditCust.visibility = View.GONE
            linear_billing_address_EditCust.visibility = View.VISIBLE

        }


        // modify tds/tcs based on settings
        if (loginModel.data!!.company_info!!.tax_settings!!.enable_tcs.equals("1") ||
            loginModel.data!!.company_info!!.tax_settings!!.enable_tds.equals("1")
        ) {
            txtModifyTdsTcsEditCust.visibility = View.VISIBLE
        } else {
            txtModifyTdsTcsEditCust.visibility = View.GONE
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
                false -> {
                    defaultEnableAllButtonnUI()
                    // setData(customerDetailModel)
                    //  checkBranchType(true)
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
                checkBranchGstNotGst()
            }
            false -> {
                // user
                when (binding.radiogroupTaxEditCust.visibility == View.VISIBLE) {
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
            radioTaxExeEditCust.isChecked = true
            radioTaxEditCust.isChecked = true

        } else { // NON-GST branch
            selectedTaxableTypeID = "0"
            radioTaxEditCust.isChecked = true
            radioTaxExeEditCust.isChecked = true
        }
    }

    private fun defaultDisableAllButtonnUI() {
        binding.lyEditOpeningFineBal.visibility = View.GONE
        binding.lyEditOpeningCashBal.visibility = View.GONE
        binding.tvFineEditCust.visibility = View.GONE
        binding.tvCashEditCust.visibility = View.GONE
        binding.radiogroupTaxEditCust.visibility = View.GONE
        binding.tvGSTEditCust.visibility = View.GONE
        binding.tvGSTINEditCust.visibility = View.GONE
        binding.tvPANEditCust.visibility = View.GONE
        binding.cardEditSuppAccountInfo.visibility = View.GONE
    }

    private fun defaultEnableAllButtonnUI() {
        binding.lyEditOpeningFineBal.visibility = View.VISIBLE
        binding.lyEditOpeningCashBal.visibility = View.VISIBLE
        binding.tvFineEditCust.visibility = View.VISIBLE
        binding.tvCashEditCust.visibility = View.VISIBLE
        binding.radiogroupTaxEditCust.visibility = View.VISIBLE
        binding.tvGSTEditCust.visibility = View.VISIBLE
        binding.tvGSTINEditCust.visibility = View.VISIBLE
        binding.tvPANEditCust.visibility = View.VISIBLE
        binding.cardEditSuppAccountInfo.visibility = View.VISIBLE
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                EditCustomerViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }

    private fun setupUIandListner() {

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
        tvTitle.setText(R.string.edit_customer)

        prefs.edit().remove(Constants.PREF_BILLING_ADDRESS_KEY).apply()
        prefs.edit().remove(Constants.PREF_SHIPPING_ADDRESS_KEY).apply()




        imgLeft?.clickWithDebounce {
            onBackPressed()
        }
        btnSaveAdd_EditCustomer?.clickWithDebounce {
            if (performValidation()) {
                if (NetworkUtils.isConnected()) {
                    binding.txtOpenFineEditCust.clearFocus()
                    binding.txtOpenCashEditCust.clearFocus()
                    binding.txtFineEditCust.clearFocus()
                    binding.txtCashEditCust.clearFocus()
                    updateCustomerAPI(
                        loginModel?.data?.bearer_access_token,
                        customerID,
                        loginModel?.data?.company_info?.id,
                        selectedCustomerTypeID,
                        binding.txtNameTitleEditCust.text.toString().trim(),
                        binding.txtFirstNameEditCust.text.toString().trim(),
                        binding.txtLastNameEditCust.text.toString().trim(),
                        binding.txtCompanyNameEditCust.text.toString().trim(),
                        binding.txtCustCodeEditCust.text.toString().trim(),
                        binding.txtDisNameEditCust.text.toString().trim(),
                        binding.txtMobileEditCust.text.toString().trim(),
                        binding.txtAddNumberEditCust.text.toString().trim(),
                        binding.txtEmailEditCust.text.toString().trim(),
                        binding.txtOpenFineEditCust.text.toString().trim(),
                        openFineNewTerm,
                        binding.txtOpenSilverFineEditCust.text.toString().trim(),
                        openSilverFineNewTerm,
                        binding.txtOpenCashEditCust.text.toString().trim(),
                        openCashNewTerm,
                        binding.txtFineEditCust.text.toString().trim(),
                        binding.txtCashEditCust.text.toString().trim(),
                        is_tcs_applicable,
                        selectedTaxableTypeID,
                        selectedGSTTreatment,
                        binding.txtGSTINEditCust.text.toString().trim(),
                        binding.txtPANEditCust.text.toString().trim(),
                        binding.txtCourierEditCust.text.toString().trim(),
                        binding.txtNotesEditCust.text.toString().trim(),
                        billingAddress?.is_shipping,
                        Gson().toJson(billingAddress),
                        if (this::shipppingAddress.isInitialized) {
                            Gson().toJson(shipppingAddress)
                        } else {
                            ""
                        },
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

        binding.txtModifyTdsTcsEditCust?.clickWithDebounce {
            startActivity(
                Intent(this, CustSuppTcsTdsMgmtActivity::class.java)
                    .putExtra(Constants.isFromEditCustAddress, true)
                    .putExtra(Constants.CUST_TCS_TDS_EDIT, Gson().toJson(customerDetailModel))
            )
        }
        binding.txtGSTTretmentEditCust?.clickWithDebounce {
            openGSTTretmentPopup()
        }

        binding.matCardBillingAddressEditCust?.clickWithDebounce {
            startActivity(Intent(this, CustSuppAddressDetailsActivity::class.java))
        }

        binding.txtOpenFineTermEditCust.clickWithDebounce {
            openFineTermMenu(fineDefaultTermNameList)
        }

        binding.txtOpenSilverFineTermEditCust.clickWithDebounce {
            openSilverFineTermMenu(fineDefaultTermNameList)
        }

        binding.txtOpenCashTermEditCust.clickWithDebounce {
            openCashTermMenu(fineDefaultTermNameList)
        }

        df.roundingMode = RoundingMode.CEILING
        df1.roundingMode = RoundingMode.CEILING

        applyingDigitFilter()

        binding.radiogroupTypeEditCust.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                binding.radioBusinessEditCust.id -> {
                    selectedCustomerTypeID = "business"
                    binding.tvCompanyNameEditCust.visibility = View.VISIBLE
                }
                binding.radioIndividualEditcust.id -> {
                    selectedCustomerTypeID = "individual"
                    binding.tvCompanyNameEditCust.visibility = View.GONE
                    binding.txtCompanyNameEditCust.setText("")
                }
            }
        })

        binding.radiogroupTaxEditCust.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                binding.radioTaxExeEditCust.id -> {
                    selectedTaxableTypeID = "0"
                    binding.tvGSTEditCust.visibility = View.GONE
                    binding.tvGSTINEditCust.visibility = View.GONE
                    //ll_EditCust_GstFields.visibility = View.GONE

                }
                binding.radioTaxEditCust.id -> {
                    selectedTaxableTypeID = "1"
                    binding.tvGSTEditCust.visibility = View.VISIBLE
                    binding.tvGSTINEditCust.visibility = View.VISIBLE

                    if (selectedGSTTreatment.equals("register", true) ||
                        selectedGSTTreatment.equals("composite")
                    ) {
                        binding.tvGSTINEditCust.visibility = View.VISIBLE
                    } else {
                        binding.tvGSTINEditCust.visibility = View.GONE
                    }
                    // ll_EditCust_GstFields.visibility = View.VISIBLE

                }
            }
        })

        binding.txtFirstNameEditCust.doAfterTextChanged {
            if (selectedCustomerTypeID == getString(R.string.business).toLowerCase()) {
                if (binding.txtCompanyNameEditCust.text.toString().trim().isBlank()) {
                    var companyName: String =
                        binding.txtFirstNameEditCust.text.toString()
                            .trim() + " " + binding.txtLastNameEditCust.text.toString().trim()
                    binding.txtDisNameEditCust.setText(companyName.trim())
                } else {
                    binding.txtDisNameEditCust.setText(binding.txtCompanyNameEditCust.text.toString().trim())
                }
            } else {
                var companyName: String =
                    binding.txtFirstNameEditCust.text.toString()
                        .trim() + " " + binding.txtLastNameEditCust.text.toString().trim()
                binding.txtDisNameEditCust.setText(companyName.trim())
            }
        }

        binding.txtLastNameEditCust.doAfterTextChanged {
            if (selectedCustomerTypeID == getString(R.string.business).toLowerCase()) {
                if (binding.txtCompanyNameEditCust.text.toString().trim().isBlank()) {
                    var companyName: String =
                        binding.txtFirstNameEditCust.text.toString()
                            .trim() + " " + binding.txtLastNameEditCust.text.toString().trim()
                    binding.txtDisNameEditCust.setText(companyName.trim())
                } else {
                    binding.txtDisNameEditCust.setText(binding.txtCompanyNameEditCust.text.toString().trim())
                }
            } else {
                var companyName: String =
                    binding.txtFirstNameEditCust.text.toString()
                        .trim() + " " + binding.txtLastNameEditCust.text.toString().trim()
                binding.txtDisNameEditCust.setText(companyName.trim())
            }
        }

        binding.txtCompanyNameEditCust.doAfterTextChanged {
            if (binding.txtCompanyNameEditCust.text.toString().trim().isBlank()) {
                var companyName: String =
                    binding.txtFirstNameEditCust.text.toString()
                        .trim() + " " + binding.txtLastNameEditCust.text.toString().trim()
                binding.txtDisNameEditCust.setText(companyName.trim())
            } else {
                binding.txtDisNameEditCust.setText(binding.txtCompanyNameEditCust.text.toString().trim())
            }
        }



        binding.txtOpenFineEditCust.doAfterTextChanged {

            val str: String = binding.txtOpenFineEditCust.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtOpenFineEditCust.setText(str2)
                binding.txtOpenFineEditCust.setSelection(str2.length)
            }

            openingFineNewCustUpdatedValue = df1.format(str2.toDouble())
        }

        binding.txtOpenFineEditCust.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (this::openingFineNewCustUpdatedValue.isInitialized) {
                when (binding.txtOpenFineEditCust.text.isNullOrBlank()) {
                    true -> {
                        openingFineNewCustUpdatedValue = "0.000"
                        binding.txtOpenFineEditCust.setText(openingFineNewCustUpdatedValue)
                        binding.txtOpenFineEditCust.setSelection(openingFineNewCustUpdatedValue.length)
                    }
                    else -> {
                        binding.txtOpenFineEditCust.setText(openingFineNewCustUpdatedValue)
                        binding.txtOpenFineEditCust.setSelection(openingFineNewCustUpdatedValue.length)
                    }
                }
            }
        }

        binding.txtOpenSilverFineEditCust.doAfterTextChanged {

            val str: String = binding.txtOpenSilverFineEditCust.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtOpenSilverFineEditCust.setText(str2)
                binding.txtOpenSilverFineEditCust.setSelection(str2.length)
            }

            openingSilverFineNewCustUpdatedValue = df1.format(str2.toDouble())
        }

        binding.txtOpenSilverFineEditCust.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (this::openingSilverFineNewCustUpdatedValue.isInitialized) {
                when (binding.txtOpenSilverFineEditCust.text.isNullOrBlank()) {
                    true -> {
                        openingSilverFineNewCustUpdatedValue = "0.000"
                        binding.txtOpenSilverFineEditCust.setText(openingSilverFineNewCustUpdatedValue)
                        binding.txtOpenSilverFineEditCust.setSelection(openingSilverFineNewCustUpdatedValue.length)
                    }
                    else -> {
                        binding.txtOpenSilverFineEditCust.setText(openingSilverFineNewCustUpdatedValue)
                        binding.txtOpenSilverFineEditCust.setSelection(openingSilverFineNewCustUpdatedValue.length)
                    }
                }
            }
        }


        binding.txtFineEditCust.doAfterTextChanged {

            val str: String = binding.txtFineEditCust.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtFineEditCust.setText(str2)
                binding.txtFineEditCust.setSelection(str2.length)
            }

            fineLimitNewCustUpdatedValue = df1.format(str2.toDouble())
        }
        binding.txtFineEditCust.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (this::fineLimitNewCustUpdatedValue.isInitialized) {
                when (binding.txtFineEditCust.text.isNullOrBlank()) {
                    true -> {
                        fineLimitNewCustUpdatedValue = "0.000"
                        binding.txtFineEditCust.setText(fineLimitNewCustUpdatedValue)
                        binding.txtFineEditCust.setSelection(fineLimitNewCustUpdatedValue.length)
                    }
                    else -> {
                        binding.txtFineEditCust.setText(fineLimitNewCustUpdatedValue)
                        binding.txtFineEditCust.setSelection(fineLimitNewCustUpdatedValue.length)
                    }
                }
            }
        }
        binding.txtCashEditCust.doAfterTextChanged {

            val str: String = binding.txtCashEditCust.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtCashEditCust.setText(str2)
                binding.txtCashEditCust.setSelection(str2.length)
            }

            cashLimitNewCustUpdatedValue = df.format(str2.toDouble())
        }
        binding.txtCashEditCust.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (this::cashLimitNewCustUpdatedValue.isInitialized) {
                when (binding.txtCashEditCust.text.isNullOrBlank()) {
                    true -> {
                        cashLimitNewCustUpdatedValue = "0.00"
                        binding.txtCashEditCust.setText(cashLimitNewCustUpdatedValue)
                        binding.txtCashEditCust.setSelection(cashLimitNewCustUpdatedValue.length)
                    }
                    else -> {
                        binding.txtCashEditCust.setText(cashLimitNewCustUpdatedValue)
                        binding.txtCashEditCust.setSelection(cashLimitNewCustUpdatedValue.length)
                    }
                }
            }
        }
        binding.txtOpenCashEditCust.doAfterTextChanged {

            val str: String = binding.txtOpenCashEditCust.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtOpenCashEditCust.setText(str2)
                binding.txtOpenCashEditCust.setSelection(str2.length)
            }

            openingCashNewCustUpdatedValue = df.format(str2.toDouble())
        }
        binding.txtOpenCashEditCust.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (this::openingCashNewCustUpdatedValue.isInitialized) {
                when (binding.txtOpenCashEditCust.text.isNullOrBlank()) {
                    true -> {
                        openingCashNewCustUpdatedValue = "0.00"
                        binding.txtOpenCashEditCust.setText(openingCashNewCustUpdatedValue)
                        binding.txtOpenCashEditCust.setSelection(openingCashNewCustUpdatedValue.length)
                    }
                    else -> {
                        binding.txtOpenCashEditCust.setText(openingCashNewCustUpdatedValue)
                        binding.txtOpenCashEditCust.setSelection(openingCashNewCustUpdatedValue.length)
                    }
                }
            }
        }


        if (intent.extras?.containsKey(Constants.CUSTOMER_DETAIL_KEY)!!) {
            var customer_str: String? = intent.getStringExtra(Constants.CUSTOMER_DETAIL_KEY)
            customerDetailModel = Gson().fromJson(
                customer_str,
                CustomerDetailModel::class.java
            )

            customerID = customerDetailModel.customers?.id
            setData(customerDetailModel)
        }


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
                                // txtOpenFineTermEditCust.setText(fineDefaultTermList!!.get(0).default_term)
                                // selectedFineDefaultTermName = fineDefaultTermList!!.get(0).default_term!!
                                //  openFineNewTerm = fineDefaultTermList!!.get(0).default_term_value!!

                                //txtOpenCashTermEditCust.setText(fineDefaultTermList!!.get(0).default_term)
                                // openCashNewTerm = fineDefaultTermList!!.get(0).default_term_value!!

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
                                    setData(customerDetailModel)
                                    //checkBranchType(false)

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
            if (data.fields!!.get(i).startsWith(getString(R.string.customer))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_fine_bal), true)) {
                    true -> {
                        binding.cardEditSuppAccountInfo.visibility = View.VISIBLE
                        binding.lyEditOpeningFineBal.visibility = View.VISIBLE
                    }else->{

                }
                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.customer))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_opening_bal), true)) {
                    true -> {
                        binding.cardEditSuppAccountInfo.visibility = View.VISIBLE
                        binding.lyEditOpeningCashBal.visibility = View.VISIBLE
                    }else->{

                }

                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.customer))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_fine_limit), true)) {
                    true -> {
                        binding.cardEditSuppAccountInfo.visibility = View.VISIBLE
                        binding.tvFineEditCust.visibility = View.VISIBLE
                    }else->{

                }

                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.customer))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_cash_limit), true)) {
                    true -> {
                        binding.cardEditSuppAccountInfo.visibility = View.VISIBLE
                        binding.tvCashEditCust.visibility = View.VISIBLE
                    }else->{

                }
                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.customer))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_tax_preference), true)) {
                    true -> {
                        //binding.radioTaxEditCust.isChecked = true
                        binding.radiogroupTaxEditCust.visibility = View.VISIBLE
                        binding.tvGSTEditCust.visibility = View.VISIBLE
                        /* binding.tvGSTINEditCust.visibility = View.VISIBLE*/
                    }
                    false -> {
                        // binding.radioTaxEditCust.isChecked = false
                        selectedTaxableTypeID = "0"
                    }

                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.customer))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_pan_card), true)) {
                    true -> {
                        binding.tvPANEditCust.visibility = View.VISIBLE
                    }
                    else->{

                    }

                }
            }

        }
    }




    fun saveBillingShippingAddressModel() {

        val childBillingModel = BillingAddressModel(
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
        )

        val prefs = PreferenceHelper.defaultPrefs(this)
        prefs[Constants.PREF_BILLING_ADDRESS_KEY] = Gson().toJson(childBillingModel) //setter


        val childShippingModel = ShippingAddressModel(
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "", "", "", ""
        )

        prefs[Constants.PREF_SHIPPING_ADDRESS_KEY] = Gson().toJson(childShippingModel) //setter

    }

    private fun applyingDigitFilter() {
        // applying filters to edit input number decimal fields(which have 2 or 3 decimal after .)
        txtOpenFineEditCust.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )
        txtOpenFineEditCust.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )
        txtFineEditCust.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )

        txtOpenCashEditCust.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )
        txtCashEditCust.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )
    }


    fun setData(customerDetailModel: CustomerDetailModel) {


        if (customerDetailModel.customers?.customer_type.equals(
                getString(R.string.business).toLowerCase(),
                ignoreCase = true
            )
        ) {
            binding.radioBusinessEditCust.isChecked = true
            selectedCustomerTypeID = "business"
        } else {
            selectedCustomerTypeID = "individual"
            binding.radioIndividualEditcust.isChecked = true
        }
        binding.txtNameTitleEditCust.setText(customerDetailModel.customers?.title)
        binding.txtFirstNameEditCust.setText(customerDetailModel.customers?.first_name)
        binding.txtLastNameEditCust.setText(customerDetailModel.customers?.last_name)
        binding.txtCompanyNameEditCust.setText(customerDetailModel.customers?.company_name)
        binding.txtCustCodeEditCust.setText(customerDetailModel.customers?.customer_code)
        binding.txtDisNameEditCust.setText(customerDetailModel.customers?.display_name)
        binding.txtMobileEditCust.setText(customerDetailModel.customers?.mobile_number)
        binding.txtAddNumberEditCust.setText(customerDetailModel.customers?.secondary_contact)
        binding.txtEmailEditCust.setText(customerDetailModel.customers?.email)
        binding.txtOpenFineEditCust.setText(customerDetailModel.customers?.opening_fine_balance)
        when (customerDetailModel.customers?.opening_fine_balance_term) {
            "debit" -> {
                when (dashboardModel.data!!.default_term!!.default_term) {
                    "debit_credit" -> {
                        binding.txtOpenFineTermEditCust.setText("Debit")
                        openFineNewTerm = "debit"
                    }
                    "udhar_jama" -> {
                        binding.txtOpenFineTermEditCust.setText("Udhar")
                        openFineNewTerm = "debit"
                    }
                    "receivable_payable" -> {
                        binding.txtOpenFineTermEditCust.setText("Receivable")
                        openFineNewTerm = "debit"
                    }
                    "len_den" -> {
                        binding.txtOpenFineTermEditCust.setText("Len")
                        openFineNewTerm = "debit"
                    }
                }
            }
            "credit" -> {
                when (dashboardModel.data!!.default_term!!.default_term) {
                    "debit_credit" -> {
                        binding.txtOpenFineTermEditCust.setText("Credit")
                        openFineNewTerm = "credit"
                    }
                    "udhar_jama" -> {
                        binding.txtOpenFineTermEditCust.setText("Jama")
                        openFineNewTerm = "credit"
                    }
                    "receivable_payable" -> {
                        binding.txtOpenFineTermEditCust.setText("Payable")
                        openFineNewTerm = "credit"
                    }
                    "len_den" -> {
                        binding.txtOpenFineTermEditCust.setText("Den")
                        openFineNewTerm = "credit"
                    }
                }
            }
        }

        binding.txtOpenSilverFineEditCust.setText(customerDetailModel.customers?.opening_silver_fine_balance)
        when (customerDetailModel.customers?.opening_silver_fine_term) {
            "debit" -> {
                when (dashboardModel.data!!.default_term!!.default_term) {
                    "debit_credit" -> {
                        binding.txtOpenSilverFineTermEditCust.setText("Debit")
                        openSilverFineNewTerm = "debit"
                    }
                    "udhar_jama" -> {
                        binding.txtOpenSilverFineTermEditCust.setText("Udhar")
                        openSilverFineNewTerm = "debit"
                    }
                    "receivable_payable" -> {
                        binding.txtOpenSilverFineTermEditCust.setText("Receivable")
                        openSilverFineNewTerm = "debit"
                    }
                    "len_den" -> {
                        binding.txtOpenSilverFineTermEditCust.setText("Len")
                        openSilverFineNewTerm = "debit"
                    }
                }
            }
            "credit" -> {
                when (dashboardModel.data!!.default_term!!.default_term) {
                    "debit_credit" -> {
                        binding.txtOpenSilverFineTermEditCust.setText("Credit")
                        openSilverFineNewTerm = "credit"
                    }
                    "udhar_jama" -> {
                        binding.txtOpenSilverFineTermEditCust.setText("Jama")
                        openSilverFineNewTerm = "credit"
                    }
                    "receivable_payable" -> {
                        binding.txtOpenSilverFineTermEditCust.setText("Payable")
                        openSilverFineNewTerm = "credit"
                    }
                    "len_den" -> {
                        binding.txtOpenSilverFineTermEditCust.setText("Den")
                        openSilverFineNewTerm = "credit"
                    }
                }
            }
        }
        //txtOpenFineTermEditCust.setText(customerDetailModel.customers?.opening_fine_default_term?.capitalize())
        binding.txtOpenCashEditCust.setText(customerDetailModel.customers?.opening_cash_balance)
        when (customerDetailModel.customers?.opening_cash_balance_term) {
            "debit" -> {
                when (dashboardModel.data!!.default_term!!.default_term) {
                    "debit_credit" -> {
                        binding.txtOpenCashTermEditCust.setText("Debit")
                        openCashNewTerm = "debit"
                    }
                    "udhar_jama" -> {
                        binding.txtOpenCashTermEditCust.setText("Udhar")
                        openCashNewTerm = "debit"
                    }
                    "receivable_payable" -> {
                        binding.txtOpenCashTermEditCust.setText("Receivable")
                        openCashNewTerm = "debit"
                    }
                    "len_den" -> {
                        binding.txtOpenCashTermEditCust.setText("Len")
                        openCashNewTerm = "debit"
                    }
                }
            }
            "credit" -> {
                when (dashboardModel.data!!.default_term!!.default_term) {
                    "debit_credit" -> {
                        binding.txtOpenCashTermEditCust.setText("Credit")
                        openCashNewTerm = "credit"
                    }
                    "udhar_jama" -> {
                        binding.txtOpenCashTermEditCust.setText("Jama")
                        openCashNewTerm = "credit"
                    }
                    "receivable_payable" -> {
                        binding.txtOpenCashTermEditCust.setText("Payable")
                        openCashNewTerm = "credit"
                    }
                    "len_den" -> {
                        binding.txtOpenCashTermEditCust.setText("Den")
                        openCashNewTerm = "credit"
                    }
                }
            }
        }

        // txtOpenCashTermEditCust.setText(customerDetailModel.customers?.opening_cash_default_term?.capitalize())
        binding.txtFineEditCust.setText(customerDetailModel.customers?.fine_limit)
        binding.txtCashEditCust.setText(customerDetailModel.customers?.cash_limit)
        binding.txtCourierEditCust.setText(customerDetailModel.customers?.courier)
        binding.txtNotesEditCust.setText(customerDetailModel.customers?.notes)


        if (customerDetailModel.customers?.gst_register == "0") {
            /*binding.radioTaxExeEditCust.isChecked = true
            selectedTaxableTypeID = "0"
            tvGSTEditCust.visibility = View.GONE
            tvGSTINEditCust.visibility = View.GONE*/
            selectedTaxableTypeID = "0"
            binding.radioTaxEditCust.isChecked = true
            binding.radioTaxExeEditCust.isChecked = true
        } else {
            //  Log.v("tax","true")
            /*binding.radioTaxEditCust.isChecked = true
            selectedTaxableTypeID = "1"
            tvGSTEditCust.visibility = View.VISIBLE
            tvGSTINEditCust.visibility = View.GONE*/

            selectedTaxableTypeID = "1"
            binding.radioTaxExeEditCust.isChecked = true
            binding.radioTaxEditCust.isChecked = true


        }


        /*when (customerDetailModel.customers?.is_editable.equals("1")) {
            true -> {
                tvOpenFineEditCust.isEnabled = true
                tvOpenCashEditCust.isEnabled = true
                tvOpenFineTermEditCust.isEnabled = true
                tvOpenCashTermEditCust.isEnabled = true
            }
            else -> {
                tvOpenFineEditCust.isEnabled = false
                tvOpenCashEditCust.isEnabled = false
                tvOpenFineTermEditCust.isEnabled = false
                tvOpenCashTermEditCust.isEnabled = false
            }
        }*/


        selectedGSTTreatment = customerDetailModel.customers?.gst_treatment?.toLowerCase()
        when (selectedGSTTreatment) {
            "register" -> {
                binding.txtGSTTretmentEditCust.setText("Regular")
                binding.tvGSTINEditCust.visibility = View.VISIBLE
            }
            "composite" -> {
                binding.txtGSTTretmentEditCust.setText("Composition")
                binding.tvGSTINEditCust.visibility = View.VISIBLE
            }
            "unregister" -> {
                binding.txtGSTTretmentEditCust.setText("Unregistered")
                binding.tvGSTINEditCust.visibility = View.GONE
            }

            "consumer" -> {
                binding.txtGSTTretmentEditCust.setText(customerDetailModel.customers?.gst_treatment?.capitalize())
                binding.tvGSTINEditCust.visibility = View.GONE
            }

        }

        binding.txtGSTINEditCust.setText(customerDetailModel.customers?.gst_tin_number)
        binding.txtPANEditCust.setText(customerDetailModel.customers?.pan_number)


        if (!customerDetailModel?.billing_address?.country_id.isNullOrBlank()) {
            if (customerDetailModel.customers?.is_shipping == "1") {
                customerDetailModel.billing_address?.is_shipping = "1"
                prefs[Constants.PREF_SHIPPING_ADDRESS_KEY] =
                    Gson().toJson(customerDetailModel.billing_address) //setter
            } else {
                prefs[Constants.PREF_SHIPPING_ADDRESS_KEY] =
                    Gson().toJson(customerDetailModel.shipping_address) //setter
            }

            prefs[Constants.PREF_BILLING_ADDRESS_KEY] =
                Gson().toJson(customerDetailModel.billing_address) //setter
        }

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

            tv_billing_address_EditCust.text =
                CommonUtils.removeUnwantedComma(addressStringBuilder.toString())

            btnCardAddContactOrAddressEditCust.visibility = View.GONE
            linear_billing_address_EditCust.visibility = View.VISIBLE

        }


    }

    fun performValidation(): Boolean {

        if (binding.radiogroupTypeEditCust.getCheckedRadioButtonId() == -1) { // no radio buttons are checked
            CommonUtils.showDialog(this, getString(R.string.select_business_or_individual_msg))
            return false
        } else if (binding.txtFirstNameEditCust.text.toString().isBlank()) {
            CommonUtils.showDialog(
                this,
                getString(R.string.enter_first_name_msg)/*"Please enter First Name"*/
            )
            binding.txtFirstNameEditCust.requestFocus()
            return false
        } else if (selectedCustomerTypeID == getString(R.string.business).toLowerCase() && binding.txtCompanyNameEditCust.text.toString()
                .isBlank()
        ) {
            CommonUtils.showDialog(this, getString(R.string.enter_company_name_msg))
            return false
        } else if (binding.txtCustCodeEditCust.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.enter_customer_code_msg))
            binding.txtCustCodeEditCust.requestFocus()
            return false
        } else if (binding.txtMobileEditCust.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.enter_mobile_no_msg))
            binding.txtMobileEditCust.requestFocus()
            return false
        } else if (binding.txtMobileEditCust.text?.length!! < 10) {
            CommonUtils.showDialog(this, getString(R.string.enter_valid_mobileno_msg))
            binding.txtMobileEditCust.requestFocus()
            return false
        } else if (!prefs.contains(Constants.PREF_BILLING_ADDRESS_KEY) && !prefs.contains(Constants.PREF_SHIPPING_ADDRESS_KEY)) {
            CommonUtils.showDialog(
                this,
                getString(R.string.enter_address_details_msg)/*"Please Enter Address Details"*/
            )
            return false
        }
        when (binding.radiogroupTaxEditCust.visibility == View.VISIBLE) {
            true -> {
                if (binding.radiogroupTaxEditCust.getCheckedRadioButtonId() == -1) { // no radio buttons are checked
                    CommonUtils.showDialog(
                        this,
                        getString(R.string.select_tax_exempt_or_taxable_msg)
                    )
                    return false
                }

                if (selectedTaxableTypeID == "1") {
                    when (selectedGSTTreatment.equals("register", true)) {
                        true -> {
                            if (binding.txtGSTINEditCust.text.toString()
                                    .isBlank()
                            ) {
                                CommonUtils.showDialog(
                                    this,
                                    getString(R.string.enter_gstin_msg)/*"Please Enter GSTIN"*/
                                )
                                binding.txtGSTINEditCust.requestFocus()
                                return false
                            } else if (!CommonUtils.isValidGSTNo(
                                    binding.txtGSTINEditCust.text.toString()
                                )/*txtGSTINEditCust.text?.length!! < 15*/) {
                                CommonUtils.showDialog(
                                    this,
                                    getString(R.string.enter_valid_gstin_msg)/*"Please Enter Valid GSTIN"*/
                                )
                                binding.txtGSTINEditCust.requestFocus()
                                return false
                            }

                            when (binding.tvPANEditCust.visibility == View.VISIBLE) {
                                true -> {
                                    if (binding.txtPANEditCust.text.toString()
                                            .isBlank() || !CommonUtils.isValidPANDetail(
                                            binding.txtPANEditCust.text.toString()
                                        )
                                    ) {
                                        CommonUtils.showDialog(
                                            this,
                                            getString(R.string.enter_correct_pandetails_msg)/*"Please Enter Correct PAN Details"*/
                                        )
                                        binding.txtPANEditCust.requestFocus()
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
                            if (binding.txtGSTINEditCust.text.toString()
                                    .isBlank()
                            ) {
                                CommonUtils.showDialog(
                                    this,
                                    getString(R.string.enter_gstin_msg)/*"Please Enter GSTIN"*/
                                )
                                binding.txtGSTINEditCust.requestFocus()
                                return false
                            } else if (!CommonUtils.isValidGSTNo(
                                    binding.txtGSTINEditCust.text.toString()
                                )/*txtGSTINEditCust.text?.length!! < 15*/) {
                                CommonUtils.showDialog(
                                    this,
                                    getString(R.string.enter_valid_gstin_msg)/*"Please Enter Valid GSTIN"*/
                                )
                                binding.txtGSTINEditCust.requestFocus()
                                return false
                            }

                            when (binding.tvPANEditCust.visibility == View.VISIBLE) {
                                true -> {
                                    if (binding.txtPANEditCust.text.toString()
                                            .isBlank() || !CommonUtils.isValidPANDetail(
                                            binding.txtPANEditCust.text.toString()
                                        )
                                    ) {
                                        CommonUtils.showDialog(
                                            this,
                                            getString(R.string.enter_correct_pandetails_msg)/*"Please Enter Correct PAN Details"*/
                                        )
                                        binding.txtPANEditCust.requestFocus()
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


                    when (binding.txtPANEditCust.visibility == View.VISIBLE && (selectedGSTTreatment.equals(
                        "register",
                        true
                    ) || selectedGSTTreatment.equals("composite", true))) {
                        true -> {

                            if (selectedTaxableTypeID == "1" && (binding.txtPANEditCust.text.toString()
                                    .isBlank() || !CommonUtils.isValidPANDetail(binding.txtPANEditCust.text.toString()))
                            ) {
                                CommonUtils.showDialog(
                                    this,
                                    getString(R.string.enter_correct_pandetails_msg)/*"Please Enter Correct PAN Details"*/
                                )
                                binding.txtPANEditCust.requestFocus()
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



        try {

            billingAddress = Gson().fromJson(
                prefs[Constants.PREF_BILLING_ADDRESS_KEY, ""],
                BillingAddressModel::class.java
            )

            shipppingAddress = Gson().fromJson(
                prefs[Constants.PREF_SHIPPING_ADDRESS_KEY, ""],
                ShippingAddressModel::class.java
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }


        return true
    }


    private fun openFineTermMenu(
        fineDefaultTermNameList: List<String>?
    ) {
        popupMenu = PopupMenu(
            this, binding.txtOpenFineTermEditCust
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
            binding.txtOpenFineTermEditCust.setText(item.title)
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
            this, binding.txtOpenSilverFineTermEditCust
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
            binding.txtOpenSilverFineTermEditCust.setText(item.title)
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
            this, binding.txtOpenCashTermEditCust
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
            binding.txtOpenCashTermEditCust.setText(item.title)
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
        val popupMenu: PopupMenu = PopupMenu(this, txtGSTTretmentEditCust)
        popupMenu.menu.add("Regular")
        popupMenu.menu.add("Unregistered")
        popupMenu.menu.add("Consumer")
        popupMenu.menu.add("Composition")

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            (binding.txtGSTTretmentEditCust as TextInputEditText).setText(item.title)
            selectedGSTTreatment = when (item.title) {
                "Regular" -> {
                    binding.tvGSTINEditCust.visibility = View.VISIBLE
                    "register"
                }
                "Unregistered" -> {
                    binding.tvGSTINEditCust.visibility = View.GONE
                    "unregister"
                }
                "Consumer" -> {
                    binding.tvGSTINEditCust.visibility = View.GONE
                    "consumer"
                }
                else -> {
                    binding.tvGSTINEditCust.visibility = View.VISIBLE
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

    fun updateCustomerAPI(
        token: String?,
        customer_id: String?,
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
        tax_deductor_type: String?,
        tax_collector_type: String?,
        selectedNogType: String?,
        selectedNopType: String?,
        selectedNatureofPaymentID: String?,
        selectedNatureofGoodsID: String?
    ) {

        viewModel.updateCustomer(
            token,
            customer_id,
            company_id,
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
            tax_deductor_type,
            tax_collector_type,
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