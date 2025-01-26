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
import com.goldbookapp.databinding.EditSupplierActivityBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.customer.CustSuppAddressDetailsActivity
import com.goldbookapp.ui.activity.customer.CustSuppTcsTdsMgmtActivity
import com.goldbookapp.ui.activity.viewmodel.EditSuplierViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.SupplierDetailModel
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
import kotlinx.android.synthetic.main.edit_supplier_activity.*
import kotlinx.android.synthetic.main.new_customer_activity.*
import kotlinx.android.synthetic.main.new_supplier_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.math.RoundingMode
import java.text.DecimalFormat

class EditSupplierActivity : AppCompatActivity() {
    private var isLoadedOnce : Boolean = false
    private lateinit var tcsTdsShareDataModel: TcsTdsShareDataModel
    private lateinit var viewModel: EditSuplierViewModel
    lateinit var binding: EditSupplierActivityBinding
    lateinit var prefs: SharedPreferences
    var is_tcs_applicable: String? = "0"
    lateinit var billingAddress: BillingAddressModel
    lateinit var shipppingAddress: ShippingAddressModel

    var selectedSupplierTypeID: String? = null
    var selectedTaxableTypeID: String? = null
    var selectedGSTTreatment: String? = null

    val df = DecimalFormat("0.00")
    val df1 = DecimalFormat("0.000")
    lateinit var openingFineSuppUpdatedValue: String
    lateinit var openingSilverFineSuppUpdatedValue: String
    lateinit var fineLimitSuppUpdatedValue: String
    lateinit var cashLimitSuppUpdatedValue: String
    lateinit var openingCashSuppUpdatedValue: String
    var openFineNewTerm :String? = null
    var openSilverFineNewTerm :String? = null
    var openCashNewTerm :String? = null
    lateinit var loginModel: LoginModel
    lateinit var dashboardModel: DashboardDetailsModel

    var vendorID: String? = ""
    var is_tds_applicable: String? = "0"
    var selectedDeductorType: String? = ""
    var selectedNogType: String? = ""
    var selectedCollectorType: String? = ""
    var selectedNopType: String? = ""
    var selectedNatureofPaymentID: String? = ""
    var selectedNatureofGoodsID: String? = ""

    lateinit var supplierDetailModel: SupplierDetailModel
    var isUserRestrLoadedOnce: Boolean = false

    var fineDefaultTermNameList: List<String>? = arrayListOf()
    var fineDefaultTermList: List<ItemDefaultTermModel.Data.Default_term>? = null

    var selectedFineDefaultTermName: String = ""
    var selectedSilverFineDefaultTermName: String = ""
    var selectedCashDefaultTermName: String = ""
    lateinit var popupMenu: PopupMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.edit_supplier_activity)

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

            tv_billing_address_EditSupp.text =
                CommonUtils.removeUnwantedComma(addressStringBuilder.toString())

            btnCardAddContactOrAddressEditSupp.visibility = View.GONE
            linear_billing_address_EditSupp.visibility = View.VISIBLE
        }
        // modify tds/tcs based on settings
        if (loginModel.data!!.company_info!!.tax_settings!!.enable_tcs.equals("1") ||
            loginModel.data!!.company_info!!.tax_settings!!.enable_tds.equals("1")
        ) {
            txtModifyTdsTcsEditSupp.visibility = View.VISIBLE
        } else {
            txtModifyTdsTcsEditSupp.visibility = View.GONE
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

                    when (isLoadedOnce) {
                        false -> {
                            defaultEnableAllButtonnUI()
                            setData(supplierDetailModel, false);
                        }else->{

                    }
                    }
                }
            }
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))
        }
    }

    private fun defaultDisableAllButtonnUI() {
        binding.lyEditSuppOpeningCashBal.visibility = View.GONE
        binding.lyEditSuppOpeningBal.visibility = View.GONE
        binding.tvFineEditSupp.visibility = View.GONE
        binding.tvCashEditSupp.visibility = View.GONE
        binding.radiogroupTaxEditSupp.visibility = View.GONE
        binding.tvGSTEditSupp.visibility = View.GONE
        binding.tvGSTINEditSupp.visibility = View.GONE
        binding.tvPANEditSupp.visibility = View.GONE
        binding.cardEditSuppAccountInfo.visibility = View.GONE
    }

    private fun defaultEnableAllButtonnUI() {
        binding.lyEditSuppOpeningCashBal.visibility = View.VISIBLE
        binding.lyEditSuppOpeningBal.visibility = View.VISIBLE
        binding.tvFineEditSupp.visibility = View.VISIBLE
        binding.tvCashEditSupp.visibility = View.VISIBLE
        binding.radiogroupTaxEditSupp.visibility = View.VISIBLE
        binding.tvGSTEditSupp.visibility = View.VISIBLE
        binding.tvGSTINEditSupp.visibility = View.VISIBLE
        binding.tvPANEditSupp.visibility = View.VISIBLE
        binding.cardEditSuppAccountInfo.visibility = View.VISIBLE
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
                                    setData(supplierDetailModel, true);
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
                        binding.cardEditSuppAccountInfo.visibility = View.VISIBLE
                        binding.lyEditSuppOpeningBal.visibility = View.VISIBLE
                    }else->{

                }
                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.supp))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_opening_bal), true)) {
                    true -> {
                        binding.cardEditSuppAccountInfo.visibility = View.VISIBLE
                        binding.lyEditSuppOpeningCashBal.visibility = View.VISIBLE
                    }else->{

                }

                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.supp))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_fine_limit), true)) {
                    true -> {
                        binding.cardEditSuppAccountInfo.visibility = View.VISIBLE
                        binding.tvFineEditSupp.visibility = View.VISIBLE
                    }else->{

                }

                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.supp))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_cash_limit), true)) {
                    true -> {
                        binding.cardEditSuppAccountInfo.visibility = View.VISIBLE
                        binding.tvCashEditSupp.visibility = View.VISIBLE
                    }else->{

                }
                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.supp))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_tax_preference), true)) {
                    true -> {
                        binding.radiogroupTaxEditSupp.visibility = View.VISIBLE
                        //radioTaxEditSupp.isChecked = true
                        binding.tvGSTEditSupp.visibility = View.VISIBLE
                    }
                    false -> {
                        //binding.radioTaxEditSupp.isChecked = false
                        selectedTaxableTypeID = "0"
                    }

                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.supp))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_pan_card), true)) {
                    true -> {
                        binding.tvPANEditSupp.visibility = View.VISIBLE
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
                EditSuplierViewModel::class.java
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
        tvTitle.setText(R.string.edit_supplier)

        prefs.edit().remove(Constants.PREF_BILLING_ADDRESS_KEY).apply()
        prefs.edit().remove(Constants.PREF_SHIPPING_ADDRESS_KEY).apply()



        imgLeft?.clickWithDebounce {
            onBackPressed()
        }
        btnSaveAdd_EditSupplier?.clickWithDebounce {
            if (performValidation()) {
                if (NetworkUtils.isConnected()) {
                    binding.txtOpenFineEditSupp.clearFocus()
                    binding.txtOpenCashEditSupp.clearFocus()
                    updateSupplierAPI(
                        loginModel?.data?.bearer_access_token,
                        vendorID,
                        loginModel?.data?.company_info?.id,
                        selectedSupplierTypeID,
                        binding.txtMrEditSupp.text.toString().trim(),
                        binding.txtFirstNameEditSupp.text.toString().trim(),
                        binding.txtLastNameEditSupp.text.toString().trim(),
                        binding.txtCompanyNameEditSupp.text.toString().trim(),
                        binding.txtCustCodeEditSupp.text.toString().trim(),
                        binding.txtDisNameEditSupp.text.toString().trim(),
                        binding.txtMobileEditSupp.text.toString().trim(),
                        binding.txtAddNumberEditSupp.text.toString().trim(),
                        binding.txtEmailEditSupp.text.toString().trim(),
                        binding.txtOpenFineEditSupp.text.toString().trim(),
                        openFineNewTerm,
                        binding.txtOpenSilverFineEditSupp.text.toString().trim(),
                        openSilverFineNewTerm,
                        binding.txtOpenCashEditSupp.text.toString().trim(),
                        openCashNewTerm,
                        binding.txtFineEditSupp.text.toString().trim(),
                        binding.txtCashEditSupp.text.toString().trim(),
                        is_tcs_applicable,
                        selectedTaxableTypeID,
                        selectedGSTTreatment,
                        binding.txtGSTINEditSupp.text.toString().trim(),
                        binding.txtPANEditSupp.text.toString().trim(),
                        binding.txtCourierEditSupp.text.toString().trim(),
                        binding.txtNotesEditSupp.text.toString().trim(),
                        billingAddress?.is_shipping,
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
        binding.matCardBillingAddressEditSupp?.clickWithDebounce {
            startActivity(Intent(this, CustSuppAddressDetailsActivity::class.java))
        }
        binding.txtModifyTdsTcsEditSupp?.clickWithDebounce {
            startActivity(
                Intent(this, CustSuppTcsTdsMgmtActivity::class.java)
                    .putExtra(Constants.isFromEditCustAddress, false)
                    .putExtra(Constants.SUPP_TCS_TDS_EDIT, Gson().toJson(supplierDetailModel))
            )
        }
        binding.txtGSTTretmentEditSupp?.clickWithDebounce {
            openGSTTretmentPopup()
        }

        binding.txtOpenFineTermEditSupp.clickWithDebounce {
            openFineTermMenu(fineDefaultTermNameList)
        }

        binding.txtOpenSilverFineTermEditSupp.clickWithDebounce {
            openSilverFineTermMenu(fineDefaultTermNameList)
        }

        binding.txtOpenCashTermEditSupp.clickWithDebounce {
            openCashTermMenu(fineDefaultTermNameList)
        }

        df.roundingMode = RoundingMode.CEILING
        df1.roundingMode = RoundingMode.CEILING

        applyingDigitFilter()

        binding.radiogroupTypeEditSupp.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                binding.radioBusinessEditSupp.id -> {
                    selectedSupplierTypeID = "business"
                    binding.tvCompanyNameEditSupp.visibility = View.VISIBLE
                }
                binding.radioIndividualEditSupp.id -> {
                    selectedSupplierTypeID = "individual"
                    binding.tvCompanyNameEditSupp.visibility = View.GONE
                    binding.txtCompanyNameEditSupp.setText("")
                }
            }
        })

        binding.radiogroupTaxEditSupp.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                binding.radioTaxExeEditSupp.id -> {
                    selectedTaxableTypeID = "0"
                    binding.tvGSTEditSupp.visibility = View.GONE
                    binding.tvGSTINEditSupp.visibility = View.GONE
                    //ll_EditSupp_GstFields.visibility = View.GONE
                }
                binding.radioTaxEditSupp.id -> {
                    selectedTaxableTypeID = "1"
                    binding.tvGSTEditSupp.visibility = View.VISIBLE
                    binding.tvGSTINEditSupp.visibility = View.VISIBLE

                    if(selectedGSTTreatment.equals("register",true) ||
                        selectedGSTTreatment.equals("composite")){
                        binding.tvGSTINEditSupp.visibility = View.VISIBLE
                    }
                    else{
                        binding.tvGSTINEditSupp.visibility = View.GONE
                    }
                    //ll_EditSupp_GstFields.visibility = View.VISIBLE
                }
            }
        })
        binding.txtFirstNameEditSupp.doAfterTextChanged {
            if (selectedSupplierTypeID == getString(R.string.business).toLowerCase()) {
                if (binding.txtCompanyNameEditSupp.text.toString().trim().isBlank()) {
                    var companyName: String =
                        binding.txtFirstNameEditSupp.text.toString()
                            .trim() + " " + binding.txtLastNameEditSupp.text.toString().trim()
                    binding.txtDisNameEditSupp.setText(companyName.trim())
                } else {
                    binding.txtDisNameEditSupp.setText(binding.txtCompanyNameEditSupp.text.toString().trim())
                }
            } else {
                var companyName: String =
                    binding.txtFirstNameEditSupp.text.toString()
                        .trim() + " " + binding.txtLastNameEditSupp.text.toString().trim()
                binding.txtDisNameEditSupp.setText(companyName.trim())
            }
        }

        binding.txtLastNameEditSupp.doAfterTextChanged {
            if (selectedSupplierTypeID == getString(R.string.business).toLowerCase()) {
                if (binding.txtCompanyNameEditSupp.text.toString().trim().isBlank()) {
                    var companyName: String =
                        binding.txtFirstNameEditSupp.text.toString()
                            .trim() + " " + binding.txtLastNameEditSupp.text.toString().trim()
                    binding.txtDisNameEditSupp.setText(companyName.trim())
                } else {
                    binding.txtDisNameEditSupp.setText(binding.txtCompanyNameEditSupp.text.toString().trim())
                }
            } else {
                var companyName: String =
                    binding.txtFirstNameEditSupp.text.toString()
                        .trim() + " " + binding.txtLastNameEditSupp.text.toString().trim()
                binding.txtDisNameEditSupp.setText(companyName.trim())
            }
        }

        binding.txtCompanyNameEditSupp.doAfterTextChanged {
            if (binding.txtCompanyNameEditSupp.text.toString().trim().isBlank()) {
                var companyName: String =
                    binding.txtFirstNameEditSupp.text.toString()
                        .trim() + " " + binding.txtLastNameEditSupp.text.toString().trim()
                binding.txtDisNameEditSupp.setText(companyName.trim())
            } else {
                binding.txtDisNameEditSupp.setText(binding.txtCompanyNameEditSupp.text.toString().trim())
            }
        }

        binding.txtOpenFineEditSupp.doAfterTextChanged {

            val str: String = binding.txtOpenFineEditSupp.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtOpenFineEditSupp.setText(str2)
                binding.txtOpenFineEditSupp.setSelection(str2.length)
            }

            openingFineSuppUpdatedValue = df1.format(str2.toDouble())
        }

        binding.txtOpenFineEditSupp.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (this::openingFineSuppUpdatedValue.isInitialized) {
                when (binding.txtOpenFineEditSupp.text.isNullOrBlank()) {
                    true -> {
                        openingFineSuppUpdatedValue = "0.000"
                        binding.txtOpenFineEditSupp.setText(openingFineSuppUpdatedValue)
                        binding.txtOpenFineEditSupp.setSelection(openingFineSuppUpdatedValue.length)
                    }
                    else -> {
                        binding.txtOpenFineEditSupp.setText(openingFineSuppUpdatedValue)
                        binding.txtOpenFineEditSupp.setSelection(openingFineSuppUpdatedValue.length)
                    }
                }
            }
        }

        binding.txtOpenSilverFineEditSupp.doAfterTextChanged {

            val str: String = binding.txtOpenSilverFineEditSupp.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtOpenSilverFineEditSupp.setText(str2)
                binding.txtOpenSilverFineEditSupp.setSelection(str2.length)
            }

            openingSilverFineSuppUpdatedValue = df1.format(str2.toDouble())
        }

        binding.txtOpenSilverFineEditSupp.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (this::openingSilverFineSuppUpdatedValue.isInitialized) {
                when (binding.txtOpenSilverFineEditSupp.text.isNullOrBlank()) {
                    true -> {
                        openingSilverFineSuppUpdatedValue = "0.000"
                        binding.txtOpenSilverFineEditSupp.setText(openingSilverFineSuppUpdatedValue)
                        binding.txtOpenSilverFineEditSupp.setSelection(openingSilverFineSuppUpdatedValue.length)
                    }
                    else -> {
                        binding.txtOpenSilverFineEditSupp.setText(openingSilverFineSuppUpdatedValue)
                        binding.txtOpenSilverFineEditSupp.setSelection(openingSilverFineSuppUpdatedValue.length)
                    }
                }
            }
        }

        binding.txtOpenCashEditSupp.doAfterTextChanged {

            val str: String = binding.txtOpenCashEditSupp.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtOpenCashEditSupp.setText(str2)
                binding.txtOpenCashEditSupp.setSelection(str2.length)
            }

            openingCashSuppUpdatedValue = df.format(str2.toDouble())
        }

        binding.txtOpenCashEditSupp.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (this::openingCashSuppUpdatedValue.isInitialized) {
                when (binding.txtOpenCashEditSupp.text.isNullOrBlank()) {
                    true -> {
                        openingCashSuppUpdatedValue = "0.00"
                        binding.txtOpenCashEditSupp.setText(openingCashSuppUpdatedValue)
                        binding.txtOpenCashEditSupp.setSelection(openingCashSuppUpdatedValue.length)
                    }
                    else -> {
                        binding.txtOpenCashEditSupp.setText(openingCashSuppUpdatedValue)
                        binding.txtOpenCashEditSupp.setSelection(openingCashSuppUpdatedValue.length)
                    }
                }
            }
        }

        binding.txtFineEditSupp.doAfterTextChanged {

            val str: String = binding.txtFineEditSupp.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtFineEditSupp.setText(str2)
                binding.txtFineEditSupp.setSelection(str2.length)
            }

            fineLimitSuppUpdatedValue = df1.format(str2.toDouble())
        }
        binding.txtFineEditSupp.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (this::fineLimitSuppUpdatedValue.isInitialized) {
                when (binding.txtFineEditSupp.text.isNullOrBlank()) {
                    true -> {
                        fineLimitSuppUpdatedValue = "0.000"
                        binding.txtFineEditSupp.setText(fineLimitSuppUpdatedValue)
                        binding.txtFineEditSupp.setSelection(fineLimitSuppUpdatedValue.length)
                    }
                    else -> {
                        binding.txtFineEditSupp.setText(fineLimitSuppUpdatedValue)
                        binding.txtFineEditSupp.setSelection(fineLimitSuppUpdatedValue.length)
                    }
                }
            }
        }
        binding.txtCashEditSupp.doAfterTextChanged {

            val str: String = binding.txtCashEditSupp.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtCashEditSupp.setText(str2)
                binding.txtCashEditSupp.setSelection(str2.length)
            }

            cashLimitSuppUpdatedValue = df.format(str2.toDouble())
        }
        binding.txtCashEditSupp.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (this::cashLimitSuppUpdatedValue.isInitialized) {
                when (binding.txtCashEditSupp.text.isNullOrBlank()) {
                    true -> {
                        cashLimitSuppUpdatedValue = "0.00"
                        binding.txtCashEditSupp.setText(cashLimitSuppUpdatedValue)
                        binding.txtCashEditSupp.setSelection(cashLimitSuppUpdatedValue.length)
                    }
                    else -> {
                        binding.txtCashEditSupp.setText(cashLimitSuppUpdatedValue)
                        binding.txtCashEditSupp.setSelection(cashLimitSuppUpdatedValue.length)
                    }
                }
            }
        }

        if (intent.extras?.containsKey(Constants.SUPPLIER_DETAIL_KEY)!!) {
            var supplier_str: String? = intent.getStringExtra(Constants.SUPPLIER_DETAIL_KEY)
            supplierDetailModel = Gson().fromJson(
                supplier_str,
                SupplierDetailModel::class.java
            )

            vendorID = supplierDetailModel.vendors?.id

        }


    }

    private fun applyingDigitFilter() {
        // applying filters to edit input number decimal fields(which have 2 or 3 decimal after .)
        binding.txtOpenFineEditSupp.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )
        binding.txtOpenCashEditSupp.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )
        binding.txtCashEditSupp.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )
        binding.txtFineEditSupp.setFilters(
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
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "0", "", ""
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


    private fun openFineTermMenu(
        fineDefaultTermNameList: List<String>?
    ) {
        popupMenu = PopupMenu(
            this, binding.txtOpenFineTermEditSupp
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
            binding.txtOpenFineTermEditSupp.setText(item.title)
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
            this, binding.txtOpenSilverFineTermEditSupp
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
            binding.txtOpenSilverFineTermEditSupp.setText(item.title)
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
            this, binding.txtOpenCashTermEditSupp
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
            binding.txtOpenCashTermEditSupp.setText(item.title)
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
        val popupMenu: PopupMenu = PopupMenu(this, binding.txtGSTTretmentEditSupp)
        popupMenu.menu.add("Regular")
        popupMenu.menu.add("Unregistered")
        popupMenu.menu.add("Consumer")
        popupMenu.menu.add("Composition")

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            (binding.txtGSTTretmentEditSupp as TextInputEditText).setText(item.title)
            selectedGSTTreatment = when (item.title) {
                "Regular" -> {
                    binding.tvGSTINEditSupp.visibility = View.VISIBLE
                    "register"
                }
                "Unregistered" -> {
                    binding.tvGSTINEditSupp.visibility = View.GONE
                    "unregister"
                }
                "Consumer" -> {
                    binding.tvGSTINEditSupp.visibility = View.GONE
                    "consumer"
                }
                else -> {
                    binding.tvGSTINEditSupp.visibility = View.VISIBLE
                    "composite"
                }
            }
            true
        })

        popupMenu.show()
    }

    fun setData(supplierDetailModel: SupplierDetailModel, isFromUserRestriction: Boolean) {
        when(isFromUserRestriction){
            false->{
                isLoadedOnce = true
            }else->{

        }
        }
        if (supplierDetailModel.vendors?.customer_type.equals("1", true)) {
            binding.radioBusinessEditSupp.isChecked = true
            selectedSupplierTypeID = "business"
        } else {
            binding.radioIndividualEditSupp.isChecked = true
            selectedSupplierTypeID = "individual"
        }
        binding.txtMrEditSupp.setText(supplierDetailModel.vendors?.title)
        binding.txtFirstNameEditSupp.setText(supplierDetailModel.vendors?.first_name)
        binding.txtLastNameEditSupp.setText(supplierDetailModel.vendors?.last_name)
        binding.txtCompanyNameEditSupp.setText(supplierDetailModel.vendors?.company_name)
        binding.txtCustCodeEditSupp.setText(supplierDetailModel.vendors?.customer_code)
        binding.txtDisNameEditSupp.setText(supplierDetailModel.vendors?.display_name)
        binding.txtMobileEditSupp.setText(supplierDetailModel.vendors?.mobile_number)
        binding.txtAddNumberEditSupp.setText(supplierDetailModel.vendors?.secondary_contact)
        binding.txtEmailEditSupp.setText(supplierDetailModel.vendors?.email)
        binding.txtOpenFineEditSupp.setText(supplierDetailModel.vendors?.opening_fine_balance)
        //txtOpenFineTermEditSupp.setText(supplierDetailModel.vendors?.opening_fine_default_term?.capitalize())

        when(supplierDetailModel.vendors?.opening_fine_term){
            "debit"->{
                when(dashboardModel.data!!.default_term!!.default_term){
                    "debit_credit"->{
                        binding.txtOpenFineTermEditSupp.setText("Debit")
                        openFineNewTerm = "debit"
                    }
                    "udhar_jama"->{
                        binding.txtOpenFineTermEditSupp.setText("Udhar")
                        openFineNewTerm = "debit"
                    }
                    "receivable_payable"->{
                        binding.txtOpenFineTermEditSupp.setText("Receivable")
                        openFineNewTerm = "debit"
                    }
                    "len_den"->{
                        binding.txtOpenFineTermEditSupp.setText("Len")
                        openFineNewTerm = "debit"
                    }
                }
            }
            "credit"->{
                when(dashboardModel.data!!.default_term!!.default_term){
                    "debit_credit"->{
                        binding.txtOpenFineTermEditSupp.setText("Credit")
                        openFineNewTerm = "credit"
                    }
                    "udhar_jama"->{
                        binding.txtOpenFineTermEditSupp.setText("Jama")
                        openFineNewTerm = "credit"
                    }
                    "receivable_payable"->{
                        binding.txtOpenFineTermEditSupp.setText("Payable")
                        openFineNewTerm = "credit"
                    }
                    "len_den"->{
                        binding.txtOpenFineTermEditSupp.setText("Den")
                        openFineNewTerm = "credit"
                    }
                }
            }
        }

        binding.txtOpenSilverFineEditSupp.setText(supplierDetailModel.vendors?.opening_silver_fine_balance)
        //txtOpenFineTermEditSupp.setText(supplierDetailModel.vendors?.opening_fine_default_term?.capitalize())

        when(supplierDetailModel.vendors?.opening_silver_fine_term){
            "debit"->{
                when(dashboardModel.data!!.default_term!!.default_term){
                    "debit_credit"->{
                        binding.txtOpenSilverFineTermEditSupp.setText("Debit")
                        openSilverFineNewTerm = "debit"
                    }
                    "udhar_jama"->{
                        binding.txtOpenSilverFineTermEditSupp.setText("Udhar")
                        openSilverFineNewTerm = "debit"
                    }
                    "receivable_payable"->{
                        binding.txtOpenSilverFineTermEditSupp.setText("Receivable")
                        openSilverFineNewTerm = "debit"
                    }
                    "len_den"->{
                        binding.txtOpenSilverFineTermEditSupp.setText("Len")
                        openSilverFineNewTerm = "debit"
                    }
                }
            }
            "credit"->{
                when(dashboardModel.data!!.default_term!!.default_term){
                    "debit_credit"->{
                        binding.txtOpenSilverFineTermEditSupp.setText("Credit")
                        openSilverFineNewTerm = "credit"
                    }
                    "udhar_jama"->{
                        binding.txtOpenSilverFineTermEditSupp.setText("Jama")
                        openSilverFineNewTerm = "credit"
                    }
                    "receivable_payable"->{
                        binding.txtOpenSilverFineTermEditSupp.setText("Payable")
                        openSilverFineNewTerm = "credit"
                    }
                    "len_den"->{
                        binding.txtOpenSilverFineTermEditSupp.setText("Den")
                        openSilverFineNewTerm = "credit"
                    }
                }
            }
        }


        binding.txtOpenCashEditSupp.setText(supplierDetailModel.vendors?.opening_cash_balance)
        // txtOpenCashTermEditSupp.setText(supplierDetailModel.vendors?.opening_cash_default_term?.capitalize())
        when(supplierDetailModel.vendors?.opening_cash_term){
            "debit"->{
                when(dashboardModel.data!!.default_term!!.default_term){
                    "debit_credit"->{
                        binding.txtOpenCashTermEditSupp.setText("Debit")
                        openCashNewTerm = "debit"
                    }
                    "udhar_jama"->{
                        binding.txtOpenCashTermEditSupp.setText("Udhar")
                        openCashNewTerm = "debit"
                    }
                    "receivable_payable"->{
                        binding.txtOpenCashTermEditSupp.setText("Receivable")
                        openCashNewTerm = "debit"
                    }
                    "len_den"->{
                        binding.txtOpenCashTermEditSupp.setText("Len")
                        openCashNewTerm = "debit"
                    }
                }
            }
            "credit"->{
                when(dashboardModel.data!!.default_term!!.default_term){
                    "debit_credit"->{
                        binding.txtOpenCashTermEditSupp.setText("Credit")
                        openCashNewTerm = "credit"
                    }
                    "udhar_jama"->{
                        binding.txtOpenCashTermEditSupp.setText("Jama")
                        openCashNewTerm = "credit"
                    }
                    "receivable_payable"->{
                        binding.txtOpenCashTermEditSupp.setText("Payable")
                        openCashNewTerm = "credit"
                    }
                    "len_den"->{
                        binding.txtOpenCashTermEditSupp.setText("Den")
                        openCashNewTerm = "credit"
                    }
                }
            }
        }


        binding.txtFineEditSupp.setText(supplierDetailModel.vendors?.fine_limit)
        binding.txtCashEditSupp.setText(supplierDetailModel.vendors?.cash_limit)
        binding.txtCourierEditSupp.setText(supplierDetailModel.vendors?.courier)
        binding.txtNotesEditSupp.setText(supplierDetailModel.vendors?.notes)


        if (supplierDetailModel.vendors?.gst_register == "0") {
            /*radioTaxExeEditSupp.isChecked = true
            selectedTaxableTypeID = "0"
            tvGSTEditSupp.visibility = View.GONE*/

            selectedTaxableTypeID = "0"
            binding.radioTaxEditSupp.isChecked = true
            binding.radioTaxExeEditSupp.isChecked = true


        } else {
            /*radioTaxEditSupp.isChecked = true
            selectedTaxableTypeID = "1"
            tvGSTEditSupp.visibility = View.VISIBLE*/
            selectedTaxableTypeID = "1"
            binding.radioTaxExeEditSupp.isChecked = true
            binding.radioTaxEditSupp.isChecked = true
        }


        /* when (supplierDetailModel.vendors?.is_editable?.equals("1")) {
             true -> {
                 tvOpenFineEditSupp.isEnabled = true
                 tvOpenCashEditSupp.isEnabled = true
                 tvOpenFineTermEditSupp.isEnabled = true
                 tvOpenCashTermEditSupp.isEnabled = true
             }
             else -> {
                 tvOpenFineEditSupp.isEnabled = false
                 tvOpenCashEditSupp.isEnabled = false
                 tvOpenFineTermEditSupp.isEnabled = false
                 tvOpenCashTermEditSupp.isEnabled = false
             }
         }*/


        selectedGSTTreatment = supplierDetailModel.vendors?.gst_treatment?.toLowerCase()

        when (selectedGSTTreatment) {
            "register" -> {
                binding.txtGSTTretmentEditSupp.setText("Regular")
                binding.tvGSTINEditSupp.visibility = View.VISIBLE
            }
            "composite" -> {
                binding.txtGSTTretmentEditSupp.setText("Composition")
                binding.tvGSTINEditSupp.visibility = View.VISIBLE
            }
            "unregister" -> {
                binding.txtGSTTretmentEditSupp.setText("Unregistered")
                binding.tvGSTINEditSupp.visibility = View.GONE
            }
            "consumer" -> {
                binding.txtGSTTretmentEditSupp.setText(supplierDetailModel.vendors?.gst_treatment?.capitalize())
                binding.tvGSTINEditSupp.visibility = View.GONE
            }

        }

        binding.txtGSTINEditSupp.setText(supplierDetailModel.vendors?.gst_tin_number)
        binding.txtPANEditSupp.setText(supplierDetailModel.vendors?.pan_number)


        if (!supplierDetailModel?.billing_address?.country_id.isNullOrBlank()) {
            if (supplierDetailModel.vendors?.is_shipping == "1") {
                supplierDetailModel.billing_address?.is_shipping = "1"
                prefs[Constants.PREF_SHIPPING_ADDRESS_KEY] =
                    Gson().toJson(supplierDetailModel.billing_address) //setter
            } else {
                supplierDetailModel.billing_address?.is_shipping = "0"
                prefs[Constants.PREF_SHIPPING_ADDRESS_KEY] =
                    Gson().toJson(supplierDetailModel.shipping_address) //setter
            }

            prefs[Constants.PREF_BILLING_ADDRESS_KEY] =
                Gson().toJson(supplierDetailModel.billing_address) //setter
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

            tv_billing_address_EditSupp.text =
                CommonUtils.removeUnwantedComma(addressStringBuilder.toString())

            btnCardAddContactOrAddressEditSupp.visibility = View.GONE
            linear_billing_address_EditSupp.visibility = View.VISIBLE
        }
        // else /*saveBillingShippingAddressModel()*/


    }


    fun performValidation(): Boolean {

        if (binding.radiogroupTypeEditSupp.getCheckedRadioButtonId() == -1) { // no radio buttons are checked
            CommonUtils.showDialog(this, "Please Select Business or Individual")
            return false
        } else if (binding.txtFirstNameEditSupp.text.toString().isBlank()) {
            CommonUtils.showDialog(this, "Please Enter First Name")
            binding.txtFirstNameEditSupp.requestFocus()
            return false
        } else if (selectedSupplierTypeID == getString(R.string.business).toLowerCase() && binding.txtCompanyNameEditSupp.text.toString()
                .isBlank()
        ) {
            CommonUtils.showDialog(this, "Please Enter Company Name")
            binding.txtCompanyNameEditSupp.requestFocus()
            return false
        } else if (binding.txtCustCodeEditSupp.text.toString().isBlank()) {
            CommonUtils.showDialog(this, "Please Enter Customer Code")
            binding.txtCustCodeEditSupp.requestFocus()
            return false
        } else if (binding.txtDisNameEditSupp.text.toString().isBlank()) {
            CommonUtils.showDialog(this, "Please Enter Display Name")
            binding.txtDisNameEditSupp.requestFocus()
            return false
        } else if (binding.txtMobileEditSupp.text.toString().isBlank()) {
            CommonUtils.showDialog(this, "Please Enter Mobile Number")
            binding.txtMobileEditSupp.requestFocus()
            return false
        } else if (binding.txtMobileEditSupp.text?.length!! < 10) {
            CommonUtils.showDialog(this, "Please Enter Valid Mobile Number")
            binding.txtMobileEditSupp.requestFocus()
            return false
        }

        when (binding.radiogroupTaxEditSupp.visibility == View.VISIBLE) {
            true -> {
                if (binding.radiogroupTaxEditSupp.getCheckedRadioButtonId() == -1) { // no radio buttons are checked
                    CommonUtils.showDialog(this, "Please Select Tax Exempt or Taxable")
                    return false
                }

                if (selectedTaxableTypeID == "1") {
                    when (selectedGSTTreatment.equals("register", true)) {
                        true -> {
                            if (binding.txtGSTINEditSupp.text.toString()
                                    .isBlank()
                            ) {
                                CommonUtils.showDialog(this, "Please Enter GSTIN")
                                binding.txtGSTINEditSupp.requestFocus()
                                return false
                            } else if (!CommonUtils.isValidGSTNo(
                                    binding.txtGSTINEditSupp.text.toString()
                                )
                            ) {
                                CommonUtils.showDialog(this, "Please Enter Valid GSTIN")
                                binding.txtGSTINEditSupp.requestFocus()
                                return false
                            }


                            when (binding.tvPANEditSupp.visibility == View.VISIBLE) {
                                true -> {
                                    if (binding.txtPANEditSupp.text.toString()
                                            .isBlank() || !CommonUtils.isValidPANDetail(
                                            binding.txtPANEditSupp.text.toString()
                                        )
                                    ) {
                                        CommonUtils.showDialog(this, getString(R.string.enter_correct_pandetails_msg))
                                        binding.txtPANEditSupp.requestFocus()
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
                            if (binding.txtGSTINEditSupp.text.toString()
                                    .isBlank()
                            ) {
                                CommonUtils.showDialog(this, "Please Enter GSTIN")
                                binding.txtGSTINEditSupp.requestFocus()
                                return false
                            } else if (!CommonUtils.isValidGSTNo(
                                    binding.txtGSTINEditSupp.text.toString()
                                )
                            ) {
                                CommonUtils.showDialog(this, "Please Enter Valid GSTIN")
                                binding.txtGSTINEditSupp.requestFocus()
                                return false
                            }


                            when (binding.tvPANEditSupp.visibility == View.VISIBLE) {
                                true -> {
                                    if (binding.txtPANEditSupp.text.toString()
                                            .isBlank() || !CommonUtils.isValidPANDetail(
                                            binding.txtPANEditSupp.text.toString()
                                        )
                                    ) {
                                        CommonUtils.showDialog(this, "Please Enter PAN Details")
                                        binding.txtPANEditSupp.requestFocus()
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

                    when ((binding.tvPANEditSupp.visibility == View.VISIBLE) && (selectedGSTTreatment.equals("register",true) || selectedGSTTreatment.equals("composite",true))) {
                        true -> {
                            if (selectedTaxableTypeID == "1" && (binding.txtPANEditSupp.text.toString()
                                    .isBlank() || !CommonUtils.isValidPANDetail(binding.txtPANEditSupp.text.toString()))
                            ) {
                                CommonUtils.showDialog(
                                    this,
                                    getString(R.string.enter_correct_pandetails_msg)/*"Please Enter Correct PAN Details"*/
                                )
                                binding.txtPANEditSupp.requestFocus()
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
            CommonUtils.showDialog(this, "Please Enter Address Details")
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

    fun openFineEditSuppBalancePopup(view: View?) {
        val popupMenu: PopupMenu = PopupMenu(this, view)
        when(dashboardModel.data!!.default_term!!.default_term){
            "debit_credit"->{
                popupMenu.menu.add(getString(R.string.credit))
                popupMenu.menu.add(getString(R.string.debit))
            }
            "udhar_jama"->{
                popupMenu.menu.add(getString(R.string.udhar))
                popupMenu.menu.add(getString(R.string.jama))
            }
            "receivable_payable"->{
                popupMenu.menu.add(getString(R.string.receivable))
                popupMenu.menu.add(getString(R.string.payable))
            }
            "len_den"->{
                popupMenu.menu.add(getString(R.string.len))
                popupMenu.menu.add(getString(R.string.den))
            }
        }


        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            (binding.txtOpenFineTermEditSupp as TextInputEditText).setText(item.title)
            when (item.title) {
                "Credit" ->{
                    openFineNewTerm = "credit"
                }
                "Debit"->{
                    openFineNewTerm = "debit"
                }
                "Udhar"->{
                    openFineNewTerm = "debit"
                }
                "Jama"->{
                    openFineNewTerm = "credit"
                }
                "Receivable"->{
                    openFineNewTerm = "debit"
                }
                "Payable"->{
                    openFineNewTerm = "credit"
                }
                "Len"->{
                    openFineNewTerm ="debit"
                }
                "Den"->{
                    openFineNewTerm = "credit"
                }

            }
            true
        })

        popupMenu.show()
    }


    fun openCashEditSuppBalancePopup(view: View?) {
        val popupMenu: PopupMenu = PopupMenu(this, view)
        when(dashboardModel.data!!.default_term!!.default_term){
            "debit_credit"->{
                popupMenu.menu.add(getString(R.string.credit))
                popupMenu.menu.add(getString(R.string.debit))
            }
            "udhar_jama"->{
                popupMenu.menu.add(getString(R.string.udhar))
                popupMenu.menu.add(getString(R.string.jama))
            }
            "receivable_payable"->{
                popupMenu.menu.add(getString(R.string.receivable))
                popupMenu.menu.add(getString(R.string.payable))
            }
            "len_den"->{
                popupMenu.menu.add(getString(R.string.len))
                popupMenu.menu.add(getString(R.string.den))
            }
        }


        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            (binding.txtOpenCashTermEditSupp as TextInputEditText).setText(item.title)
            when (item.title) {
                "Credit" ->{
                    openCashNewTerm = "credit"
                }
                "Debit"->{
                    openCashNewTerm = "debit"
                }
                "Udhar"->{
                    openCashNewTerm = "debit"
                }
                "Jama"->{
                    openCashNewTerm = "credit"
                }
                "Receivable"->{
                    openCashNewTerm = "debit"
                }
                "Payable"->{
                    openCashNewTerm = "credit"
                }
                "Len"->{
                    openCashNewTerm ="debit"
                }
                "Den"->{
                    openCashNewTerm = "credit"
                }

            }
            true
        })

        popupMenu.show()
    }


    fun openSuppNameTitlePopup(view: View?) {
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

    fun updateSupplierAPI(
        token: String?,
        vendor_id: String?,
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
        is_tds_applicable: String?,
        tax_deductor_type: String?,
        tax_collector_type: String?,
        selectedNogType: String?,
        selectedNopType: String?,
        selectedNatureofPaymentID: String?,
        selectedNatureofGoodsID: String?
    ) {

        viewModel.updateSupplier(
            token,
            vendor_id,
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
                        Toast.makeText(this, it.data?.errormessage?.message, Toast.LENGTH_LONG)
                            .show()
                    }
                    Status.LOADING -> {
                        CommonUtils.showProgress(this)
                    }
                }
            }
        })
    }

}