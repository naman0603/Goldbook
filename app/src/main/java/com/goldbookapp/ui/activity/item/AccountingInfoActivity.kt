package com.goldbookapp.ui.activity.item

import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
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
import com.goldbookapp.databinding.ActivityAccountingInfoBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.viewmodel.NewItemViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_accounting_info.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.text.DecimalFormat

class AccountingInfoActivity : AppCompatActivity() {

    lateinit var binding: ActivityAccountingInfoBinding
    private lateinit var viewModel: NewItemViewModel
    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    var selectedTaxPref: String = "0"
    var selectedItemType: String? = "0"
    var selectedSalePurGstId: String = ""
    var salePurGstName: String = ""
    var selectedJobLabGstId: String = ""
    var jobLabGstName: String = ""

    lateinit var popupMenu: PopupMenu
    var selectedSalesLedgerID: String = ""
    var selectedPurchaseLedgerID: String = ""
    var selectedJobworkLedgerID: String = ""
    var selectedLabourLedgerID: String = ""
    var selectedDiscountLedgerID: String = ""

    var salesLedgerName: String = ""
    var purchaseLedgerName: String = ""
    var jobworkLedgerName: String = ""
    var labourLedgerName: String = ""
    var discountLedgerName: String = ""

    var ledgerSalesList: List<SearchLedgerModel.LedgerDetails>? = null
    var ledgerPurchaseList: List<SearchLedgerModel.LedgerDetails>? = null
    var ledgerJobworkList: List<SearchLedgerModel.LedgerDetails>? = null
    var ledgerLabourList: List<SearchLedgerModel.LedgerDetails>? = null
    var ledgerDiscountList: List<SearchLedgerModel.LedgerDetails>? = null

    lateinit var salesMakingChrgsUpdatedValue: String
    lateinit var salesWastageUpdatedValue: String
    lateinit var purchaseMakingChrgsUpdatedValue: String
    lateinit var purchaseWastageUpdatedValue: String
    lateinit var salesRateUpdatedValue: String
    lateinit var purchaseRateUpdatedValue: String
    lateinit var jobworkRateUpdatedValue: String
    lateinit var labourRateUpdatedValue: String

    var ledgerSalesNameList: List<String>? = null
    var ledgerPurchaseNameList: List<String>? = null

    var ledgerJobWorkNameList: List<String>? = null
    var ledgerLabourNameList: List<String>? = null
    var ledgerDiscountNameList: List<String>? = null


    var gstList: List<ItemGSTMenuModel.Data.GSTMenu>? = null
    var gstNameList: List<String>? = null

    var is_from_edit: Boolean = false
    var is_from_first_time: Boolean = true
    lateinit var addAccountInfoList: AddAccountInfoModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_accounting_info)


        setupViewModel()
        setupUIandListner()
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                NewItemViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }

    override fun onResume() {
        super.onResume()

        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent) {
                handleConnectivityChange()
            }
        })


    }

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected) {
            // Network is available
            CommonUtils.hideInternetDialog()

            getItemGSTMenu()
            getLedgerdd("sales")
            getLedgerdd("purchase")
            getLedgerdd("jobwork")
            getLedgerdd("labourwork")
            getLedgerdd("discount")
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun setupUIandListner() {

        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.account_info)

        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        )

        imgLeft?.clickWithDebounce {
            onBackPressed()
        }


        if (intent.extras != null && intent.extras!!.containsKey(Constants.IS_FOR_EDIT)) {
            selectedItemType = intent.getStringExtra(Constants.NEWITEM_ITEM_TYPE_KEY)
            is_from_edit = intent.getBooleanExtra(Constants.IS_FOR_EDIT, false)
            setEditData()
        }

        applyingDigitFilter()
        getDataFromPrefAccount()

        if (is_from_first_time) {
            setDefaultData()
        }


        when (selectedItemType.equals("Goods")) {
            //0->goods
            true -> {
                binding.radiogroupTaxPref.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
                    when (checkedId) {
                        //Tax Exampt
                        binding.radioTaxExempt.id -> {
                            selectedTaxPref = "0"
                            binding.lysaleGSTHsn.visibility = View.GONE
                            binding.JobworkGstHsn.visibility = View.GONE
                            binding.lySalesTaxExampt.visibility = View.VISIBLE
                            binding.tvSaleRateNewItem.visibility = View.GONE
                            binding.tvSalesLedgerNewItem.visibility = View.GONE
                            binding.lyPurchaseTaxExampt.visibility = View.VISIBLE
                            binding.tvPurchaseRateNewItem.visibility = View.GONE
                            binding.tvPurchaseLedgerNewItem.visibility = View.GONE
                            binding.tvJobworkLedgerNewItem.visibility = View.GONE
                            binding.tvLabourLedgerNewItem.visibility = View.GONE


                        }
                        //Taxable
                        binding.radioTaxable.id -> {
                            selectedTaxPref = "1"
                            binding.lysaleGSTHsn.visibility = View.VISIBLE
                            binding.JobworkGstHsn.visibility = View.VISIBLE
                            binding.lySalesTaxExampt.visibility = View.VISIBLE
                            binding.tvSaleRateNewItem.visibility = View.GONE
                            binding.tvSalesLedgerNewItem.visibility = View.VISIBLE
                            binding.lyPurchaseTaxExampt.visibility = View.VISIBLE
                            binding.tvPurchaseRateNewItem.visibility = View.GONE
                            binding.tvPurchaseLedgerNewItem.visibility = View.VISIBLE
                            binding.tvJobworkLedgerNewItem.visibility = View.VISIBLE
                            binding.tvLabourLedgerNewItem.visibility = View.VISIBLE
                        }
                    }
                })
            }
            //1->service
            false -> {

                binding.lySalesTaxExampt.visibility = View.GONE
                binding.tvSaleRateNewItem.visibility = View.VISIBLE
                binding.tvSalesLedgerNewItem.visibility = View.GONE
                binding.lyPurchaseTaxExampt.visibility = View.GONE
                binding.tvPurchaseRateNewItem.visibility = View.VISIBLE
                binding.tvPurchaseLedgerNewItem.visibility = View.GONE



                binding.radiogroupTaxPref.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
                    when (checkedId) {
                        //Tax Exampt
                        binding.radioTaxExempt.id -> {
                            selectedTaxPref = "0"
                            binding.lysaleGSTHsn.visibility = View.GONE
                            binding.JobworkGstHsn.visibility = View.GONE
                            binding.lySalesTaxExampt.visibility = View.GONE
                            binding.tvSaleRateNewItem.visibility = View.VISIBLE
                            binding.tvSalesLedgerNewItem.visibility = View.GONE
                            binding.lyPurchaseTaxExampt.visibility = View.GONE
                            binding.tvPurchaseRateNewItem.visibility = View.VISIBLE
                            binding.tvPurchaseLedgerNewItem.visibility = View.GONE
                            binding.tvJobworkLedgerNewItem.visibility = View.GONE
                            binding.tvLabourLedgerNewItem.visibility = View.GONE


                        }
                        //Taxable
                        binding.radioTaxable.id -> {
                            selectedTaxPref = "1"
                            binding.lysaleGSTHsn.visibility = View.VISIBLE
                            binding.JobworkGstHsn.visibility = View.VISIBLE
                            binding.lySalesTaxExampt.visibility = View.GONE
                            binding.tvSaleRateNewItem.visibility = View.VISIBLE
                            binding.tvSalesLedgerNewItem.visibility = View.VISIBLE
                            binding.lyPurchaseTaxExampt.visibility = View.GONE
                            binding.tvPurchaseRateNewItem.visibility = View.VISIBLE
                            binding.tvPurchaseLedgerNewItem.visibility = View.VISIBLE
                            binding.tvJobworkLedgerNewItem.visibility = View.VISIBLE
                            binding.tvLabourLedgerNewItem.visibility = View.VISIBLE

                        }
                    }
                })
            }
        }

        binding.txtSalesLedgerNewItem.clickWithDebounce {
            openSalesLedgerMenu(ledgerSalesNameList)
        }

        binding.txtPurchaseLedgerNewItem.clickWithDebounce {
            openPurchaseLedgerMenu(ledgerPurchaseNameList)
        }

        binding.txtJobworkLedgerNewItem.clickWithDebounce {
            openJobworkLedgerMenu(ledgerJobWorkNameList)
        }

        binding.txtLabourLedgerNewItem.clickWithDebounce {
            openLabourLedgerMenu(ledgerLabourNameList)
        }

        binding.txtDiscountLedgerNewItem.clickWithDebounce {
            openDiscountLedgerMenu(ledgerDiscountNameList)
        }

        binding.txtSalePurAccountNewItem.clickWithDebounce {
            openSalePurchaseGstMenu()
        }

        binding.txtJobLabourGstNewItem.clickWithDebounce {
            openJobLabourGstMenu()
        }

        btnSaveAccountInfoNewItem.clickWithDebounce {
            /*if (performValidation()) {
                saveAccountInfoModel()
                finish()
            }*/

            saveAccountInfoModel()
            finish()

        }

        val df = DecimalFormat("0.00")
        val df1 = DecimalFormat("0.000")

        binding.txtSalesWastageNewItem.doAfterTextChanged {
            val str: String = binding.txtSalesWastageNewItem.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtSalesWastageNewItem.setText(str2)
                binding.txtSalesWastageNewItem.setSelection(str2.length)
            }

            salesWastageUpdatedValue = df1.format(str2.toDouble())
        }


        binding.txtSalesWastageNewItem.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (this::salesWastageUpdatedValue.isInitialized) {
                    when (binding.txtSalesWastageNewItem.text.isNullOrBlank()) {
                        true -> {
                            salesWastageUpdatedValue = "0.000"
                            binding.txtSalesWastageNewItem.setText(salesWastageUpdatedValue)
                            binding.txtSalesWastageNewItem.setSelection(salesWastageUpdatedValue.length)

                        }
                        else -> {
                            binding.txtSalesWastageNewItem.setText(salesWastageUpdatedValue)
                            binding.txtSalesWastageNewItem.setSelection(salesWastageUpdatedValue.length)
                        }
                    }
                }
            }
        }

        binding.txtPurchaseWastageNewItem.doAfterTextChanged {
            val str: String = binding.txtPurchaseWastageNewItem.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtPurchaseWastageNewItem.setText(str2)
                binding.txtPurchaseWastageNewItem.setSelection(str2.length)
            }

            purchaseWastageUpdatedValue = df1.format(str2.toDouble())
        }


        binding.txtPurchaseWastageNewItem.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (this::purchaseWastageUpdatedValue.isInitialized) {
                    when (binding.txtPurchaseWastageNewItem.text.isNullOrBlank()) {
                        true -> {
                            purchaseWastageUpdatedValue = "0.000"
                            binding.txtPurchaseWastageNewItem.setText(purchaseWastageUpdatedValue)
                            binding.txtPurchaseWastageNewItem.setSelection(
                                purchaseWastageUpdatedValue.length
                            )

                        }
                        else -> {
                            binding.txtPurchaseWastageNewItem.setText(purchaseWastageUpdatedValue)
                            binding.txtPurchaseWastageNewItem.setSelection(
                                purchaseWastageUpdatedValue.length
                            )
                        }
                    }
                }
            }
        }

        binding.txtPurchaseChargeNewItem.doAfterTextChanged {
            val str: String = binding.txtPurchaseChargeNewItem.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtPurchaseChargeNewItem.setText(str2)
                binding.txtPurchaseChargeNewItem.setSelection(str2.length)
            }

            purchaseMakingChrgsUpdatedValue = df.format(str2.toDouble())
        }


        binding.txtPurchaseChargeNewItem.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (this::purchaseMakingChrgsUpdatedValue.isInitialized) {
                    when (binding.txtPurchaseChargeNewItem.text.isNullOrBlank()) {
                        true -> {
                            purchaseMakingChrgsUpdatedValue = "0.00"
                            binding.txtPurchaseChargeNewItem.setText(purchaseMakingChrgsUpdatedValue)
                            binding.txtPurchaseChargeNewItem.setSelection(
                                purchaseMakingChrgsUpdatedValue.length
                            )

                        }
                        else -> {
                            binding.txtPurchaseChargeNewItem.setText(purchaseMakingChrgsUpdatedValue)
                            binding.txtPurchaseChargeNewItem.setSelection(
                                purchaseMakingChrgsUpdatedValue.length
                            )
                        }
                    }
                }
            }
        }

        binding.txtSalesChargeNewItem.doAfterTextChanged {
            val str: String = binding.txtSalesChargeNewItem.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtSalesChargeNewItem.setText(str2)
                binding.txtSalesChargeNewItem.setSelection(str2.length)
            }

            salesMakingChrgsUpdatedValue = df.format(str2.toDouble())
        }


        binding.txtSalesChargeNewItem.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (this::salesMakingChrgsUpdatedValue.isInitialized) {
                    when (binding.txtSalesChargeNewItem.text.isNullOrBlank()) {
                        true -> {
                            salesMakingChrgsUpdatedValue = "0.00"
                            binding.txtSalesChargeNewItem.setText(salesMakingChrgsUpdatedValue)
                            binding.txtSalesChargeNewItem.setSelection(salesMakingChrgsUpdatedValue.length)

                        }
                        else -> {
                            binding.txtSalesChargeNewItem.setText(salesMakingChrgsUpdatedValue)
                            binding.txtSalesChargeNewItem.setSelection(salesMakingChrgsUpdatedValue.length)
                        }
                    }
                }
            }
        }

        binding.txtSaleRateNewItem.doAfterTextChanged {
            val str: String = binding.txtSaleRateNewItem.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtSaleRateNewItem.setText(str2)
                binding.txtSaleRateNewItem.setSelection(str2.length)
            }

            salesRateUpdatedValue = df.format(str2.toDouble())
        }


        binding.txtSaleRateNewItem.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (this::salesRateUpdatedValue.isInitialized) {
                    when (binding.txtSaleRateNewItem.text.isNullOrBlank()) {
                        true -> {
                            salesRateUpdatedValue = "0.00"
                            binding.txtSaleRateNewItem.setText(salesRateUpdatedValue)
                            binding.txtSaleRateNewItem.setSelection(salesRateUpdatedValue.length)

                        }
                        else -> {
                            binding.txtSaleRateNewItem.setText(salesRateUpdatedValue)
                            binding.txtSaleRateNewItem.setSelection(salesRateUpdatedValue.length)
                        }
                    }
                }
            }
        }

        binding.txtPurchaseRateNewItem.doAfterTextChanged {
            val str: String = binding.txtPurchaseRateNewItem.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtPurchaseRateNewItem.setText(str2)
                binding.txtPurchaseRateNewItem.setSelection(str2.length)
            }

            purchaseRateUpdatedValue = df.format(str2.toDouble())
        }


        binding.txtPurchaseRateNewItem.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (this::purchaseRateUpdatedValue.isInitialized) {
                    when (binding.txtPurchaseRateNewItem.text.isNullOrBlank()) {
                        true -> {
                            purchaseRateUpdatedValue = "0.00"
                            binding.txtPurchaseRateNewItem.setText(purchaseRateUpdatedValue)
                            binding.txtPurchaseRateNewItem.setSelection(purchaseRateUpdatedValue.length)

                        }
                        else -> {
                            binding.txtPurchaseRateNewItem.setText(purchaseRateUpdatedValue)
                            binding.txtPurchaseRateNewItem.setSelection(purchaseRateUpdatedValue.length)
                        }
                    }
                }
            }
        }

        binding.txtJobworkRateNewItem.doAfterTextChanged {
            val str: String = binding.txtJobworkRateNewItem.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtJobworkRateNewItem.setText(str2)
                binding.txtJobworkRateNewItem.setSelection(str2.length)
            }

            jobworkRateUpdatedValue = df.format(str2.toDouble())
        }


        binding.txtJobworkRateNewItem.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (this::jobworkRateUpdatedValue.isInitialized) {
                    when (binding.txtJobworkRateNewItem.text.isNullOrBlank()) {
                        true -> {
                            jobworkRateUpdatedValue = "0.00"
                            binding.txtJobworkRateNewItem.setText(jobworkRateUpdatedValue)
                            binding.txtJobworkRateNewItem.setSelection(jobworkRateUpdatedValue.length)

                        }
                        else -> {
                            binding.txtJobworkRateNewItem.setText(jobworkRateUpdatedValue)
                            binding.txtJobworkRateNewItem.setSelection(jobworkRateUpdatedValue.length)
                        }
                    }
                }
            }
        }

        binding.txtLabourRateNewItem.doAfterTextChanged {
            val str: String = binding.txtLabourRateNewItem.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtLabourRateNewItem.setText(str2)
                binding.txtLabourRateNewItem.setSelection(str2.length)
            }

            labourRateUpdatedValue = df.format(str2.toDouble())
        }


        binding.txtLabourRateNewItem.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (this::labourRateUpdatedValue.isInitialized) {
                    when (binding.txtLabourRateNewItem.text.isNullOrBlank()) {
                        true -> {
                            labourRateUpdatedValue = "0.00"
                            binding.txtLabourRateNewItem.setText(labourRateUpdatedValue)
                            binding.txtLabourRateNewItem.setSelection(labourRateUpdatedValue.length)

                        }
                        else -> {
                            binding.txtLabourRateNewItem.setText(labourRateUpdatedValue)
                            binding.txtLabourRateNewItem.setSelection(labourRateUpdatedValue.length)
                        }
                    }
                }
            }
        }


    }

    private fun performValidation(): Boolean {
        if (selectedDiscountLedgerID.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.select_discount_ledger))
            binding.txtDiscountLedgerNewItem.requestFocus()
            return false
        }
        return true
    }

    private fun setDefaultData() {
        binding.radioTaxExempt.isChecked = true
        binding.txtSalePurAccountNewItem.setText("0.00")
        selectedSalePurGstId = "1"
        binding.txtJobLabourGstNewItem.setText("0.00")
        selectedJobLabGstId = "1"
    }

    private fun setEditData() {
        when (is_from_edit) {
            true -> {
                getDataFromPrefAccount()
            }
            else -> {

            }
        }
    }


    private fun applyingDigitFilter() {
        // applying filters to edit input number decimal fields(which have 2 or 3 decimal after .)
        binding.txtSalesWastageNewItem.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )
        binding.txtSalesChargeNewItem.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )

        binding.txtPurchaseChargeNewItem.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )
        binding.txtPurchaseWastageNewItem.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )
        binding.txtJobworkRateNewItem.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )

        binding.txtLabourRateNewItem.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )

        binding.txtSaleRateNewItem.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )
        binding.txtPurchaseRateNewItem.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )

    }

    private fun getDataFromPrefAccount() {
        when (prefs.contains(Constants.PREF_ACCOUNTING_INFO_KEY)) {
            true -> {
                is_from_first_time = false
                val accountingInfoDetails =
                    object : TypeToken<AddAccountInfoModel>() {}.type
                addAccountInfoList = Gson().fromJson(
                    prefs[Constants.PREF_ACCOUNTING_INFO_KEY, ""],
                    accountingInfoDetails
                )

                selectedTaxPref = addAccountInfoList.taxPreference
                when (selectedTaxPref.equals("0")) {
                    true -> {
                        binding.radioTaxExempt.isChecked = true
                    }
                    false -> {
                        binding.radioTaxable.isChecked = true
                    }
                }
                setUIFromTaxPref(selectedTaxPref, selectedItemType)

                selectedSalePurGstId = addAccountInfoList.salePurGst
                // if(selectedSalePurGstId.isNotBlank()){
                Log.v("salepurGst", "" + addAccountInfoList.salePurGstName)
                binding.txtSalePurAccountNewItem.setText(addAccountInfoList.salePurGstName)
                //  }
                binding.txtSalePurHSNNewItem.setText(addAccountInfoList.salepurHsn)
                selectedJobLabGstId = addAccountInfoList.jobLabourGst
                if (selectedJobLabGstId.isNotBlank()) {
                    binding.txtJobLabourGstNewItem.setText(addAccountInfoList.jobLabourGstName)
                }
                binding.txtJobLabourSACNewItem.setText(addAccountInfoList.jobLaburSac)
                binding.txtSalesWastageNewItem.setText(addAccountInfoList.wastageSales)
                binding.txtSalesChargeNewItem.setText(addAccountInfoList.makingchargeSales)
                selectedSalesLedgerID = addAccountInfoList.salesLedger
                salesLedgerName = addAccountInfoList.salesLedgerName
                if (selectedSalesLedgerID.isNotBlank()) {
                    binding.txtSalesLedgerNewItem.setText(addAccountInfoList.salesLedgerName)
                }
                binding.txtPurchaseWastageNewItem.setText(addAccountInfoList.wastagePurchase)
                binding.txtPurchaseChargeNewItem.setText(addAccountInfoList.makingchargepurchase)
                selectedPurchaseLedgerID = addAccountInfoList.purchaseLedger
                purchaseLedgerName = addAccountInfoList.purchaseLedgerName
                if (selectedPurchaseLedgerID.isNotBlank()) {
                    binding.txtPurchaseLedgerNewItem.setText(addAccountInfoList.purchaseLedgerName)
                }
                binding.txtJobworkRateNewItem.setText(addAccountInfoList.jobworkRate)
                selectedJobworkLedgerID = addAccountInfoList.jobworkLedger
                jobworkLedgerName = addAccountInfoList.jobwrkLedgerName
                if (selectedJobworkLedgerID.isNotBlank()) {
                    binding.txtJobworkLedgerNewItem.setText(addAccountInfoList.jobwrkLedgerName)
                }
                binding.txtLabourRateNewItem.setText(addAccountInfoList.labourRate)
                selectedLabourLedgerID = addAccountInfoList.labourLedger
                labourLedgerName = addAccountInfoList.labourLedger
                if (selectedLabourLedgerID.isNotBlank()) {
                    binding.txtLabourLedgerNewItem.setText(addAccountInfoList.labourLedgerName)
                }
                binding.txtSaleRateNewItem.setText(addAccountInfoList.salesRate)
                binding.txtPurchaseRateNewItem.setText(addAccountInfoList.purchaseRate)

            }
            else -> {

            }
        }
    }

    private fun setUIFromTaxPref(selectedTaxPref: String, selectedItemType: String?) {
        when (selectedItemType.equals("Goods")) {
            //0->goods
            true -> {
                if (selectedTaxPref.equals("0")) {

                    binding.lysaleGSTHsn.visibility = View.GONE
                    binding.JobworkGstHsn.visibility = View.GONE
                    binding.lySalesTaxExampt.visibility = View.VISIBLE
                    binding.tvSaleRateNewItem.visibility = View.GONE
                    binding.tvSalesLedgerNewItem.visibility = View.GONE
                    binding.lyPurchaseTaxExampt.visibility = View.VISIBLE
                    binding.tvPurchaseRateNewItem.visibility = View.GONE
                    binding.tvPurchaseLedgerNewItem.visibility = View.GONE
                    binding.tvJobworkLedgerNewItem.visibility = View.GONE
                    binding.tvLabourLedgerNewItem.visibility = View.GONE

                }
                //Taxable
                else {

                    binding.lysaleGSTHsn.visibility = View.VISIBLE
                    binding.JobworkGstHsn.visibility = View.VISIBLE
                    binding.lySalesTaxExampt.visibility = View.VISIBLE
                    binding.tvSaleRateNewItem.visibility = View.GONE
                    binding.tvSalesLedgerNewItem.visibility = View.VISIBLE
                    binding.lyPurchaseTaxExampt.visibility = View.VISIBLE
                    binding.tvPurchaseRateNewItem.visibility = View.GONE
                    binding.tvPurchaseLedgerNewItem.visibility = View.VISIBLE
                }
            }
            //1->service
            false -> {

                binding.lySalesTaxExampt.visibility = View.GONE
                binding.tvSaleRateNewItem.visibility = View.VISIBLE
                binding.tvSalesLedgerNewItem.visibility = View.GONE
                binding.lyPurchaseTaxExampt.visibility = View.GONE
                binding.tvPurchaseRateNewItem.visibility = View.VISIBLE
                binding.tvPurchaseLedgerNewItem.visibility = View.GONE

                //Tax Exampt
                if (selectedTaxPref.equals("0")) {

                    binding.lysaleGSTHsn.visibility = View.GONE
                    binding.JobworkGstHsn.visibility = View.GONE
                    binding.lySalesTaxExampt.visibility = View.GONE
                    binding.tvSaleRateNewItem.visibility = View.VISIBLE
                    binding.tvSalesLedgerNewItem.visibility = View.GONE
                    binding.lyPurchaseTaxExampt.visibility = View.GONE
                    binding.tvPurchaseRateNewItem.visibility = View.VISIBLE
                    binding.tvPurchaseLedgerNewItem.visibility = View.GONE
                    binding.tvJobworkLedgerNewItem.visibility = View.GONE
                    binding.tvLabourLedgerNewItem.visibility = View.GONE


                }
                //Taxable
                else {

                    binding.lysaleGSTHsn.visibility = View.VISIBLE
                    binding.JobworkGstHsn.visibility = View.VISIBLE
                    binding.lySalesTaxExampt.visibility = View.GONE
                    binding.tvSaleRateNewItem.visibility = View.VISIBLE
                    binding.tvSalesLedgerNewItem.visibility = View.VISIBLE
                    binding.lyPurchaseTaxExampt.visibility = View.GONE
                    binding.tvPurchaseRateNewItem.visibility = View.VISIBLE
                    binding.tvPurchaseLedgerNewItem.visibility = View.VISIBLE
                    binding.tvJobworkLedgerNewItem.visibility = View.VISIBLE
                    binding.tvLabourLedgerNewItem.visibility = View.VISIBLE

                }

            }
        }
    }


    fun saveAccountInfoModel() {


        val addAccountInfoList = AddAccountInfoModel(
            selectedTaxPref,
            selectedSalePurGstId,
            binding.txtSalePurHSNNewItem.text.toString(),
            selectedJobLabGstId,
            binding.txtJobLabourSACNewItem.text.toString(),
            binding.txtSalesWastageNewItem.text.toString(),
            binding.txtSalesChargeNewItem.text.toString(),
            selectedSalesLedgerID,
            binding.txtPurchaseWastageNewItem.text.toString(),
            binding.txtPurchaseChargeNewItem.text.toString(),
            selectedPurchaseLedgerID,
            binding.txtJobworkRateNewItem.text.toString(),
            selectedJobworkLedgerID,
            binding.txtLabourRateNewItem.text.toString(),
            selectedLabourLedgerID,
            binding.txtSaleRateNewItem.text.toString(),
            binding.txtPurchaseRateNewItem.text.toString(),
            salesLedgerName,
            purchaseLedgerName,
            jobworkLedgerName,
            labourLedgerName,
            salePurGstName,
            jobLabGstName
        )

        //addAccountInfoList.add(childModel)

        val prefs = PreferenceHelper.defaultPrefs(this)
        prefs[Constants.PREF_ACCOUNTING_INFO_KEY] = Gson().toJson(addAccountInfoList)

    }


    private fun openSalePurchaseGstMenu() {

        popupMenu = PopupMenu(this, binding.txtSalePurAccountNewItem)
        for (i in 0 until this.gstNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                this.gstNameList!!.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtSalePurAccountNewItem.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = this.gstNameList!!.indexOf(selected)

            selectedSalePurGstId =
                pos?.let { it1 -> gstList?.get(it1)?.id }.toString()

            salePurGstName = pos?.let { it1 -> gstList?.get(it1)?.name }.toString()
            true
        })

        popupMenu.show()
    }


    private fun openJobLabourGstMenu() {

        popupMenu = PopupMenu(this, binding.txtJobLabourGstNewItem)
        for (i in 0 until this.gstNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                this.gstNameList!!.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtJobLabourGstNewItem.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = this.gstNameList!!.indexOf(selected)

            selectedJobLabGstId =
                pos?.let { it1 -> gstList?.get(it1)?.id }.toString()

            jobLabGstName = pos?.let { it1 -> gstList?.get(it1)?.name }.toString()
            true
        })

        popupMenu.show()
    }


    private fun openSalesLedgerMenu(ledgerSalesNameList: List<String>?) {
        popupMenu = PopupMenu(this, binding.txtSalesLedgerNewItem)
        for (i in 0 until ledgerSalesNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                ledgerSalesNameList.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtSalesLedgerNewItem.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = ledgerSalesNameList.indexOf(selected)

            selectedSalesLedgerID =
                pos?.let { it1 -> ledgerSalesList?.get(it1)?.ledger_id }.toString()


            salesLedgerName = pos?.let { it1 -> ledgerSalesList?.get(it1)?.name }.toString()

            true
        })

        popupMenu.show()
    }


    private fun openPurchaseLedgerMenu(ledgerPurchaseNameList: List<String>?) {
        popupMenu = PopupMenu(this, binding.txtPurchaseLedgerNewItem)
        for (i in 0 until ledgerPurchaseNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                ledgerPurchaseNameList.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtPurchaseLedgerNewItem.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = ledgerPurchaseNameList.indexOf(selected)

            selectedPurchaseLedgerID =
                pos?.let { it1 -> ledgerPurchaseList?.get(it1)?.ledger_id }.toString()

            purchaseLedgerName = pos?.let { it1 -> ledgerPurchaseList?.get(it1)?.name }.toString()
            true
        })

        popupMenu.show()
    }


    private fun openJobworkLedgerMenu(ledgerJobworkNameList: List<String>?) {
        popupMenu = PopupMenu(this, binding.txtJobworkLedgerNewItem)
        for (i in 0 until ledgerJobWorkNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                ledgerJobWorkNameList!!.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtJobworkLedgerNewItem.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = ledgerJobWorkNameList!!.indexOf(selected)

            selectedJobworkLedgerID =
                pos?.let { it1 -> ledgerJobworkList?.get(it1)?.ledger_id }.toString()

            jobworkLedgerName = pos?.let { it1 -> ledgerJobworkList?.get(it1)?.name }.toString()
            true
        })

        popupMenu.show()
    }

    private fun openLabourLedgerMenu(ledgerLabourNameList: List<String>?) {
        popupMenu = PopupMenu(this, binding.txtLabourLedgerNewItem)
        for (i in 0 until ledgerLabourNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                ledgerLabourNameList!!.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtLabourLedgerNewItem.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = ledgerLabourNameList!!.indexOf(selected)

            selectedLabourLedgerID =
                pos?.let { it1 -> ledgerLabourList?.get(it1)?.ledger_id }.toString()

            labourLedgerName = pos?.let { it1 -> ledgerLabourList?.get(it1)?.name }.toString()
            true
        })

        popupMenu.show()
    }

    private fun openDiscountLedgerMenu(ledgerDiscountNameList: List<String>?) {
        popupMenu = PopupMenu(this, binding.txtDiscountLedgerNewItem)
        for (i in 0 until ledgerDiscountNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                ledgerDiscountNameList!!.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtDiscountLedgerNewItem.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = ledgerDiscountNameList!!.indexOf(selected)

            selectedDiscountLedgerID =
                pos?.let { it1 -> ledgerDiscountList?.get(it1)?.ledger_id }.toString()

            discountLedgerName = pos?.let { it1 -> ledgerDiscountList?.get(it1)?.name }.toString()
            true
        })

        popupMenu.show()
    }

    fun getItemGSTMenu() {
        if (NetworkUtils.isConnected()) {
            viewModel.getItemGSTMenu(
                loginModel?.data?.bearer_access_token
            ).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {

                            if (it.data?.status == true) {
                                gstList = it.data.data?.gst

                                gstNameList = gstList?.map { it.name }


                                CommonUtils.hideProgress()

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


    fun getLedgerdd(type: String) {
        if (NetworkUtils.isConnected()) {
            viewModel.getLedgerdd(
                loginModel?.data?.bearer_access_token,
                type
            ).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {

                            if (it.data?.status == true) {
                                when (type) {
                                    "sales" -> {
                                        ledgerSalesNameList = ArrayList<String>()
                                        ledgerSalesList = it.data.data
                                        ledgerSalesNameList =
                                            ledgerSalesList?.map { it.name.toString() }
                                    }
                                    "purchase" -> {
                                        ledgerPurchaseNameList = ArrayList<String>()
                                        ledgerPurchaseList = it.data.data
                                        ledgerPurchaseNameList =
                                            ledgerPurchaseList?.map { it.name.toString() }
                                    }
                                    "jobwork" -> {
                                        ledgerJobWorkNameList = ArrayList<String>()
                                        ledgerJobworkList = it.data.data
                                        ledgerJobWorkNameList =
                                            ledgerJobworkList?.map { it.name.toString() }
                                    }
                                    "labourwork" -> {
                                        ledgerLabourNameList = ArrayList<String>()
                                        ledgerLabourList = it.data.data
                                        ledgerLabourNameList =
                                            ledgerLabourList?.map { it.name.toString() }
                                    }
                                    "discount" -> {
                                        ledgerDiscountNameList = ArrayList<String>()
                                        ledgerDiscountList = it.data.data
                                        ledgerDiscountNameList =
                                            ledgerDiscountList?.map { it.name.toString() }
                                        if(!is_from_edit){
                                            binding.txtDiscountLedgerNewItem.setText(ledgerDiscountList!!.get(0).name)
                                            selectedDiscountLedgerID = ledgerDiscountList!!.get(0).ledger_id!!
                                            discountLedgerName = ledgerDiscountList!!.get(0).name!!
                                        }

                                    }

                                }

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


}