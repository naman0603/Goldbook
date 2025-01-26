package com.goldbookapp.ui.activity.purchase

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.NetworkUtils
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.NewPurchaseActivityBinding
import com.goldbookapp.model.*
import com.goldbookapp.searchablespinner.SearchableSpinner
import com.goldbookapp.ui.activity.additem.AddItemActivity
import com.goldbookapp.ui.activity.sales.AddCashBankActivity
import com.goldbookapp.ui.activity.sales.AddSalesLineActivity
import com.goldbookapp.ui.activity.settings.TaxAnalysisDetailsActivity
import com.goldbookapp.ui.activity.viewmodel.NewPurchaseViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.IssueReceiveAdapter
import com.goldbookapp.ui.adapter.NewPurchaseItemAdapter
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.CommonUtils.Companion.isValidClickPressed
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.goldbookapp.utils.Status
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.bottom_sheet_dialog.view.*
import kotlinx.android.synthetic.main.new_customer_activity.*
import kotlinx.android.synthetic.main.new_invoice_activity.*
import kotlinx.android.synthetic.main.new_invoice_activity.v1
import kotlinx.android.synthetic.main.new_invoice_activity.v2
import kotlinx.android.synthetic.main.new_purchase_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NewPurchaseActivity : AppCompatActivity() {

    var isNoGenerated: Boolean = false
    var isRateCutSaved: Boolean = false
    private lateinit var viewModel: NewPurchaseViewModel
    lateinit var purchaseDetailModel: SaleDetailModel.Data
    lateinit var binding: NewPurchaseActivityBinding
    private lateinit var adapter: NewPurchaseItemAdapter
    private lateinit var issueReceiveadapter: IssueReceiveAdapter
    var additemList = ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>()
    var addTaxList = ArrayList<TaxAnalysisListModel.TaxAnalysisList>()
    lateinit var taxAnalysisModel: TaxAnalysisListModel.TaxAnalysisList
    var selectedContactID: String? = null
    var selectedCustomerCode: String? = null
    var selectedPlaceOfSupply: String? = null
    var selectedPlaceOfSupplyID: String? = null
    var itemListFormDetail = ArrayList<SaleDetailModel.Item1427117511>()
    var transaction_id: String = ""
    var is_From_Edit: Boolean = false

    var ledgerDetailsList: ArrayList<SearchLedgerModel.LedgerDetails>? = null
    var selectedLedgerID: String = ""
    var selectedLedgerName: String = ""

    var isPhotoSelected: Boolean = false
    var multipartImageBody: MultipartBody.Part? = null

    var contactList: List<SearchContactModel.Data.Contact>? = null
    var contactNameList: List<String>? = null

    var is_gst_applicable: String? = "0"
    var tds_percentage: String? = "0"
    var tcs_percentage: String? = "0"
    var tds_tcs_enable: String = "0"
    var is_rate_cut: String? = "0"
    var is_tcs_applicable: String? = "0"

    var is_sgst_applicable: String? = "1"
    var is_cgst_applicable: String? = "1"
    var is_igst_applicable: String? = "1"

    var is_prefix: String? = ""
    var is_series: String? = ""
    var is_suffix: String? = ""
    var goldRate: String? = ""

    lateinit var calculatePurchaseModelMain: CalculateSalesModel
    lateinit var goldrateUpdatedValue: String
    lateinit var ratecutWtUpdateValue: String
    lateinit var tcsAmtUpdatedvalue: String
    val df = DecimalFormat("0.00")
    val df1 = DecimalFormat("0.000")

    lateinit var dialog: Dialog
    var selectedSgstId: String? = ""
    var selectedCgstId: String? = ""
    var selectedIgstId: String? = ""
    var selectedTcsId: String? = ""
    var selectedTdsId: String? = ""
    var selectedRoundoffId: String? = ""

    var isSgstEnable: Boolean = false
    var isCgstEnable: Boolean = false
    var isIgstEnable: Boolean = false
    var isTcsEnable: Boolean = false
    var isTdsEnable: Boolean = false
    var isRoundOffEnable: Boolean = false

    var ledgerSgsttList: List<SearchLedgerModel.LedgerDetails>? = null
    var ledgerCgstList: List<SearchLedgerModel.LedgerDetails>? = null
    var ledgerIgstList: List<SearchLedgerModel.LedgerDetails>? = null
    var ledgerTcsList: List<SearchLedgerModel.LedgerDetails>? = null
    var ledgerTdsList: List<SearchLedgerModel.LedgerDetails>? = null
    var ledgerRoundOffList: List<SearchLedgerModel.LedgerDetails>? = null

    var ledgerSgstNameList: List<String>? = null
    var ledgerCgstNameList: List<String>? = null
    var ledgerIgstNameList: List<String>? = null
    var ledgerTcsNameList: List<String>? = null
    var ledgerTdsNameList: List<String>? = null
    var ledgerRoundoffNameList: List<String>? = null

    lateinit var contactNameAdapter: ArrayAdapter<String>
    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel

    var tcs_amount: String = "0"
    var purchaseLineList = ArrayList<SalesLineModel.SaleLineModelDetails>()
    var imageURL: String? = ""
    var debit_short_term: String = ""
    var credit_short_term: String = ""
    var isUserRestrLoadedOnce: Boolean = false
    private var isDefaultEnableCalledOnce: Boolean = false
    var checkedRowNo: String? = "1"
    lateinit var fiscalYearModel: FiscalYearModel
    var roundOffUpdatedValue: String = "0.00"

    var fineDefaultTermList: List<ItemDefaultTermModel.Data.Default_term>? = null
    var subTotalTerm: String = ""
    var subTotalTermValue: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.new_purchase_activity)
        //binding.loginModel = LoginModel()
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
                            // defaultDisableAllButtonnUI()
                            userWiseRestriction(loginModel.data?.bearer_access_token)
                        }
                        else -> {

                        }
                    }
                }
                // user type user
                false -> {
                    if (!isDefaultEnableCalledOnce) {
                        //defaultEnableAllButtonnUI()
                        // checkBranchType(true)
                    }

                }
            }
            autogenerateinvoice(false)
            getSearchContact()
            when (is_From_Edit) {
                false -> {
                    when (isNoGenerated) {
                        false -> getPurchaseInvoiceNoFromApi()
                        else -> {

                        }
                    }
                }
                else -> {

                }
            }
            binding.rvNewpurchaseItem.layoutManager = LinearLayoutManager(this)
            adapter =
                NewPurchaseItemAdapter(arrayListOf(), true, selectedPlaceOfSupplyID.toString())
            binding.rvNewpurchaseItem.adapter = adapter

            // getLedgerDetails()
            getLedgerdd("round_off")
            getIssueReceiveDataFromPref()
            invoiceCalculation()
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun defaultEnableAllButtonnUI() {

    }

    private fun defaultDisableAllButtonnUI() {

    }

    fun invoiceCalculation() {

        if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {

            val collectionType =
                object :
                    TypeToken<ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>>() {}.type
            additemList = Gson().fromJson(
                prefs[Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY, ""],
                collectionType
            )

            getIssueReceiveDataFromPref()

            invoiceCalculateAPI(
                true

            )

        } else {
            // called when date changed (for opening bal update according to date)
            when (!selectedContactID.isNullOrBlank() && !binding.txtDatePBM.text.toString()
                .isBlank()) {
                true -> {
                    invoiceCalculateAPI(
                        true
                    )
                }
                else -> {

                }
            }

        }

    }

    private fun getIssueReceiveDataFromPref() {
        if (prefs.contains(Constants.PREF_SALES_LINE_INFO_KEY)) {
            val collectionType =
                object :
                    TypeToken<ArrayList<SalesLineModel.SaleLineModelDetails>>() {}.type
            purchaseLineList =
                Gson().fromJson(
                    prefs[Constants.PREF_SALES_LINE_INFO_KEY, ""],
                    collectionType
                )
            setupIssueReceiveAdapter()

        }
    }

    private fun setupIssueReceiveAdapter() {
        when (purchaseLineList.size > 0) {
            true -> {
                binding.rvIssueReceiveList.visibility = View.VISIBLE
                issueReceiveadapter.apply {
                    addissueReceiveList(purchaseLineList)
                    notifyDataSetChanged()
                }
            }
            else -> {

            }
        }

    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(
                this,
                ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))
            ).get(
                NewPurchaseViewModel::class.java
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

        fiscalYearModel = Gson().fromJson(
            prefs[Constants.FiscalYear, ""],
            FiscalYearModel::class.java
        )

        clearPref()

        df.roundingMode = RoundingMode.CEILING
        df1.roundingMode = RoundingMode.CEILING

        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.purchase_bill)


        imgLeft?.clickWithDebounce {
            onBackPressed()
        }

        getSearchContact()


        //recyclerview setup
        binding.rvNewpurchaseItem.layoutManager = LinearLayoutManager(this)
        adapter = NewPurchaseItemAdapter(arrayListOf(), true, selectedPlaceOfSupplyID.toString())
        binding.rvNewpurchaseItem.adapter = adapter

        // issue receive adapter
        debit_short_term = loginModel?.data!!.company_info!!.general_settings!!.debit_short_term!!
        credit_short_term = loginModel?.data!!.company_info!!.general_settings!!.credit_short_term!!
        binding.rvIssueReceiveList.layoutManager = LinearLayoutManager(this)
        issueReceiveadapter = IssueReceiveAdapter(
            arrayListOf(),
            "purchase",
            false,
            debit_short_term,
            credit_short_term
        )
        binding.rvIssueReceiveList.adapter = issueReceiveadapter


        // gst branch
        if (loginModel.data?.branch_info?.branch_type.equals("1", true)) { // GST branch
            binding.checkInGstPBM.visibility = View.VISIBLE
            binding.checkInGstPBM.isEnabled = false


            is_gst_applicable = "1"
            tds_tcs_enable = "tcs"
            getLedgerdd("tcs")
            binding.checkInGstPBM.isChecked = true
            binding.tvSupplierNamePBM.visibility = View.VISIBLE
            binding.tvAddPurchaseTaxAnalysis.visibility = View.VISIBLE
            binding.tvAddPurchaseLine.setText("Add Cash / Bank")

        } else { // NON-GST branch
            binding.checkInGstPBM.visibility = View.GONE
            is_gst_applicable = "0"
            binding.tvAddPurchaseLine.setText("Add Cash / Bank / Rate-Cut / Metal Receipt / Metal Payment")
            binding.tvAddPurchaseTaxAnalysis.visibility = View.GONE
        }


        getDataFromIntent()
        onTextChanged()
        onFocusChanged()
        binding.btnSaveAddPurchase?.clickWithDebounce {

            if (performValidation()) {
                if (NetworkUtils.isConnected()) {
                    when (is_From_Edit) {
                        true -> {
                            editPBMRequestBodyParamCallAPI()
                        }
                        false -> {
                            addPBMRequestBodyParamCallAPI()
                        }
                    }

                }
            }
        }


        binding.tvPbmUploadphoto?.clickWithDebounce {

            ImagePicker.with(this)
                .cropSquare()                    //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                // User can only select image from Gallery
                /*.galleryOnly()
                .galleryMimeTypes( // no gif images at all
                    mimeTypes = arrayOf(
                        "image/png",
                        "image/jpg",
                        "image/jpeg"
                    )
                )*/
                .maxResultSize(
                    1080,
                    1080
                )    //Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }
        binding.ivPbmAttachment?.clickWithDebounce {

            ImagePicker.with(this)
                .cropSquare()                    //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                // User can only select image from Gallery
                /*.galleryOnly()
                .galleryMimeTypes( // no gif images at all
                    mimeTypes = arrayOf(
                        "image/png",
                        "image/jpg",
                        "image/jpeg"
                    )
                )*/
                .maxResultSize(
                    1080,
                    1080
                )    //Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        binding.cardAddItemPBM?.clickWithDebounce {

            if (selectedContactID != null && !selectedContactID!!.isBlank()) {
                binding.txtSupplierNamePBM.clearFocus()
                binding.txtRemarkPBM.clearFocus()
                startActivity(
                    Intent(
                        this,
                        AddItemActivity::class.java
                    ).putExtra(Constants.TRANSACTION_TYPE, "purchase")
                        .putExtra(Constants.CUST_STATE_ID, selectedPlaceOfSupplyID)
                )
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.select_contact_first_msg),
                    Toast.LENGTH_LONG
                ).show()
                binding.txtSupplierNamePBM.requestFocus()
            }
        }


        binding.txtSupplierNamePBM.doAfterTextChanged { selectedContactID = "" }


        applyingDigitFilter()

        //hides keyboard on focus change from linearlayout (root) to tap anywhere in the screen (below toolbar).
        binding.llPbmRoot.setOnFocusChangeListener { view, b -> CommonUtils.hideKeyboardnew(this) }

        when (is_From_Edit) {
            false -> {
                autogenerateinvoice(true)
            }
            else -> {

            }
        }


        binding.cardAddPurchaseLine.clickWithDebounce {
            checkedRowNo = "1"
            if (selectedContactID != null && !selectedContactID!!.isBlank()) {

                val dialog = BottomSheetDialog(this, R.style.BottomSheetDialog)

                val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
                dialog.setContentView(view)

//GST Branch
                if (loginModel.data?.branch_info?.branch_type.equals("1", true)) {

                    view.rbCashRecAddSaleLine.visibility = View.VISIBLE
                    view.rbCashPayAddSaleLine.visibility = View.VISIBLE
                    view.rbBankRecAddSaleLine.visibility = View.VISIBLE
                    view.rbBankPayAddSaleLine.visibility = View.VISIBLE
                    view.rbMetalRecAddSaleLine.visibility = View.GONE
                    view.rbMetalPayAddSaleLine.visibility = View.GONE
                    view.rbRateCutAddSaleLine.visibility = View.GONE
                    view.rbAdjustAddSaleLine.visibility = View.GONE
                }
                //Non Gst
                else {
                    view.rbCashRecAddSaleLine.visibility = View.VISIBLE
                    view.rbCashPayAddSaleLine.visibility = View.VISIBLE
                    view.rbBankRecAddSaleLine.visibility = View.VISIBLE
                    view.rbBankPayAddSaleLine.visibility = View.VISIBLE
                    view.rbMetalRecAddSaleLine.visibility = View.VISIBLE
                    view.rbMetalPayAddSaleLine.visibility = View.VISIBLE
                    view.rbRateCutAddSaleLine.visibility = View.VISIBLE
                    view.rbAdjustAddSaleLine.visibility = View.GONE
                }

                view.rgAddSaleLine.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
                    when (checkedId) {
                        view.rbCashRecAddSaleLine.id -> checkedRowNo = "1"
                        view.rbCashPayAddSaleLine.id -> checkedRowNo = "2"
                        view.rbBankRecAddSaleLine.id -> checkedRowNo = "3"
                        view.rbBankPayAddSaleLine.id -> checkedRowNo = "4"
                        view.rbMetalRecAddSaleLine.id -> checkedRowNo = "5"
                        view.rbMetalPayAddSaleLine.id -> checkedRowNo = "6"
                        view.rbRateCutAddSaleLine.id -> checkedRowNo = "7"
                        view.rbAdjustAddSaleLine.id -> checkedRowNo = "8"
                    }
                })


                view.btnNextAddSaleLine.clickWithDebounce {
                    nextAddSaleLine()
                    dialog.dismiss()
                }

                dialog.show()

            } else {
                Toast.makeText(
                    this,
                    getString(R.string.select_contact_first_msg),
                    Toast.LENGTH_LONG
                ).show()
                binding.txtSupplierNamePBM.requestFocus()
            }

        }

        binding.tvAddPurchaseTaxAnalysis.clickWithDebounce {
            startActivity(
                Intent(this, TaxAnalysisDetailsActivity::class.java)
                    .putExtra(Constants.TRANSACTION_TYPE, "sales")
            )
        }

        binding.radiogroupTDSTCSNewpurchase.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                binding.radioTCSNewpurchase.id -> {
                    tds_tcs_enable = "tcs"
                    binding.tvNewpurchaseTdstcsCol1.mLabelView!!.hint = "TCS Ledger"
                    getLedgerdd("tcs")
                    invoiceCalculation()

                }
                binding.radioTDSNewpurchase.id -> {
                    tds_tcs_enable = "tds"
                    binding.tvNewpurchaseTdstcsCol1.mLabelView!!.hint = "TDS Ledger"
                    getLedgerdd("tds")
                    invoiceCalculation()


                }
            }
        })

    }

    private fun onFocusChanged() {
        binding.tvNewpurchaseRoundoffCol2.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                when (binding.tvNewpurchaseRoundoffCol2.text.isNullOrBlank()) {
                    true -> {
                        roundOffUpdatedValue = "0.00"
                        binding.tvNewpurchaseRoundoffCol2.setText(roundOffUpdatedValue)
                        binding.tvNewpurchaseRoundoffCol2.setSelection(roundOffUpdatedValue.length)
                    }
                    else -> {
                        when (!roundOffUpdatedValue.toBigDecimal()
                            .equals("0.00")) {
                            true -> {
                                binding.tvNewpurchaseRoundoffCol2.setText(roundOffUpdatedValue)
                                binding.tvNewpurchaseRoundoffCol2.setSelection(roundOffUpdatedValue.length)
                                invoiceCalculation()
                            }
                            else -> {

                            }
                        }
                    }
                }
            }
        }
    }

    private fun onTextChanged() {
        binding.tvNewpurchaseRoundoffCol2.doAfterTextChanged {

            val inputValue: Float
            var str: String = "0.00"
            try {
//convert in float for negative value
                inputValue = binding.tvNewpurchaseRoundoffCol2.text.toString().toFloat()
                str = inputValue.toString()

            } catch (nfe: NumberFormatException) {
                //Error handling.
            }

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.tvNewpurchaseRoundoffCol2.setText(str2)
                binding.tvNewpurchaseRoundoffCol2.setSelection(str2.length)
            }

            roundOffUpdatedValue = df.format(str2.toDouble())

        }
    }

    private fun nextAddSaleLine() {
        if (isValidClickPressed()) {
            when (checkedRowNo) {
                // Cash Receipt
                "1" -> startActivity(
                    Intent(
                        this,
                        AddCashBankActivity::class.java
                    ).putExtra(Constants.SaleLine_Row_No, checkedRowNo)
                )
                //Cash Payment
                "2" -> startActivity(
                    Intent(
                        this,
                        AddCashBankActivity::class.java
                    ).putExtra(Constants.SaleLine_Row_No, checkedRowNo)
                )
                //Bank Receipt
                "3" -> startActivity(
                    Intent(
                        this,
                        AddCashBankActivity::class.java
                    ).putExtra(Constants.SaleLine_Row_No, checkedRowNo)
                )
                //Bank Payment
                "4" -> startActivity(
                    Intent(
                        this,
                        AddCashBankActivity::class.java
                    ).putExtra(Constants.SaleLine_Row_No, checkedRowNo)
                )
                //Metal Receipt
                "5" -> startActivity(
                    Intent(
                        this,
                        AddCashBankActivity::class.java
                    ).putExtra(Constants.SaleLine_Row_No, checkedRowNo)

                )
                //Metal Payment
                "6" -> startActivity(
                    Intent(
                        this,
                        AddCashBankActivity::class.java
                    ).putExtra(Constants.SaleLine_Row_No, checkedRowNo)

                )
                //Rate-cut
                "7" -> startActivity(
                    Intent(
                        this,
                        AddCashBankActivity::class.java
                    ).putExtra(Constants.SaleLine_Row_No, checkedRowNo)
                )
                //adjustment
                "8" -> startActivity(
                    Intent(
                        this,
                        AddCashBankActivity::class.java
                    ).putExtra(Constants.SaleLine_Row_No, checkedRowNo)
                )
            }

        }
    }


    private fun editPBMRequestBodyParamCallAPI() {
        if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {
            val collectionType =
                object :
                    TypeToken<ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>>() {}.type
            var itemList: ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem> =
                Gson().fromJson(
                    prefs[Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY, ""],
                    collectionType
                )

            if (prefs.contains(Constants.PREF_SALES_LINE_INFO_KEY)) {
                val collectionType =
                    object :
                        TypeToken<ArrayList<SalesLineModel.SaleLineModelDetails>>() {}.type
                purchaseLineList =
                    Gson().fromJson(
                        prefs[Constants.PREF_SALES_LINE_INFO_KEY, ""],
                        collectionType
                    )

            } else {
                var childModel = SalesLineModel.SaleLineModelDetails(
                    "", "", "",
                    "", "", "", "", "",
                    "", "", "", "", "",
                    "", "", "", "", "", "", "",
                    "", "", "", "", "", "",
                    "", "", "", "", "", "", "", "", "", "",
                    "", "", "", "", "", "", ""
                )
                purchaseLineList.add(childModel)
            }

            val issue_receive_transaction: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                Gson().toJson(purchaseLineList)
            )

            val itemadded: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                Gson().toJson(itemList)
            )

            val transaction_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                transaction_id
            )

            val transaction_type_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                "1"
            )

            val transaction_type_name: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                "sales"
            )

            val transaction_date: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.txtDatePBM.text.toString()
            )

            val invoice_number: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                is_series.toString().trim()
            )

            val referrence: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.txtReffPBM.text.toString()
            )

            val remarks: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.txtRemarkPBM.text.toString()
            )

            val customer_code: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                selectedCustomerCode
            )

            val display_name: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.txtSupplierNamePBM.text.toString()
            )
            val contact_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                selectedContactID
            )
            val party_po_no: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                " Party PO No"
            )
            val place_of_supply: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                selectedPlaceOfSupplyID
            )
            val sgst_ledger_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                selectedSgstId
            )
            val cgst_ledger_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                selectedCgstId
            )
            val igst_ledger_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                selectedIgstId
            )
            val tds_ledger_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                selectedTdsId
            )
            val tds_percentage: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                "0"
            )
            val tcs_ledger_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                selectedTcsId
            )
            val tcs_percentage: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                "0"
            )
            val tds_tcs_enable: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                tds_tcs_enable
            )
            val round_off_ledger_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                selectedRoundoffId
            )
            val round_off_total: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                roundOffUpdatedValue
            )
            val branch_type: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                loginModel?.data?.branch_info?.branch_type
            )
            val ledger_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                ""
            )
            val transaction_type: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                ""
            )

            editPBMAPI(
                loginModel?.data?.bearer_access_token,
                transaction_id,
                transaction_type_id,
                transaction_type_name,
                transaction_date,
                customer_code,
                display_name,
                contact_id,
                party_po_no,
                referrence,
                remarks,
                invoice_number,
                itemadded,
                issue_receive_transaction,
                place_of_supply,
                sgst_ledger_id,
                cgst_ledger_id,
                igst_ledger_id,
                tds_ledger_id,
                tds_percentage,
                tcs_ledger_id,
                tcs_percentage,
                tds_tcs_enable,
                round_off_ledger_id,
                round_off_total,
                branch_type,
                ledger_id,
                multipartImageBody,
                transaction_type
            )


        }

    }

    private fun getDataFromIntent() {
        if (intent.extras != null) {
            if (intent.extras?.containsKey(Constants.PURCHASE_DETAIL_KEY)!!) {

                var group_str: String? = intent.getStringExtra(Constants.PURCHASE_DETAIL_KEY)
                purchaseDetailModel =
                    Gson().fromJson(
                        group_str,
                        SaleDetailModel.Data::class.java
                    )
                tvTitle.setText(R.string.edit_purchase)
                is_From_Edit = true

                transaction_id = purchaseDetailModel.transactionData?.transaction_id!!
                selectedCustomerCode = purchaseDetailModel.transactionData?.customer_code
                selectedContactID = purchaseDetailModel.transactionData?.contact_id
                selectedPlaceOfSupplyID = purchaseDetailModel.transactionData?.place_of_supply_id
                binding.txtSupplierNamePBM.setText(purchaseDetailModel.transactionData?.display_name)
                binding.txtDatePBM.setText(purchaseDetailModel.transactionData?.transaction_date)
                binding.txtpurchaseNoPBM.setText(purchaseDetailModel.transactionData?.invoice_number)
                is_series = purchaseDetailModel.transactionData?.invoice_number.toString().trim()
                binding.txtSupplyPBM.setText(purchaseDetailModel.transactionData?.place_of_supply.toString())

                binding.txtRemarkPBM.setText(purchaseDetailModel.transactionData?.remarks)
                binding.txtReffPBM.setText(purchaseDetailModel.transactionData?.reference)

                itemListFormDetail = purchaseDetailModel.transactionData?.item!!


                // multipartImageBody = purchaseDetailModel.transactionData.image!!.get(0).image.

                for (i in 0 until itemListFormDetail.size) {
                    var childModel =
                        OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem(
                            itemListFormDetail.get(i).item_id,
                            itemListFormDetail.get(i).item_name,
                            itemListFormDetail.get(i).item_quantity,
                            itemListFormDetail.get(i).item_size,
                            itemListFormDetail.get(i).item_gross_wt,
                            itemListFormDetail.get(i).item_less_wt,
                            itemListFormDetail.get(i).item_net_wt,
                            itemListFormDetail.get(i).item_touch,
                            itemListFormDetail.get(i).item_wastage,
                            itemListFormDetail.get(i).item_fine_wt,
                            itemListFormDetail.get(i).item_total,
                            itemListFormDetail.get(i).item_remarks,
                            itemListFormDetail.get(i).item_unit_id,
                            itemListFormDetail.get(i).item_unit_name,
                            itemListFormDetail.get(i).item_use_stamp,
                            itemListFormDetail.get(i).item_stamp_id,
                            itemListFormDetail.get(i).item_stamp_name,
                            itemListFormDetail.get(i).item_use_gold_color,
                            itemListFormDetail.get(i).item_gold_color_id,
                            itemListFormDetail.get(i).item_gold_color_name,
                            itemListFormDetail.get(i).item_metal_type_id,
                            itemListFormDetail.get(i).item_metal_type_name,
                            itemListFormDetail.get(i).item_maintain_stock_in_id,
                            itemListFormDetail.get(i).item_maintain_stock_in_name,
                            itemListFormDetail.get(i).item_rate,
                            itemListFormDetail.get(i).item_rate_on,
                            itemListFormDetail.get(i).item_total,
                            itemListFormDetail.get(i).item_charges,
                            itemListFormDetail.get(i).item_discount,
                            itemListFormDetail.get(i).item_type,
                            itemListFormDetail.get(i).tag_no,
                            itemListFormDetail.get(i).random_tag_id,
                            itemListFormDetail.get(i).item_is_studded,
                            itemListFormDetail.get(i).item_wt_breakup,
                            itemListFormDetail.get(i).item_charges_breakup,
                            itemListFormDetail.get(i).item_unit_array,
                            itemListFormDetail.get(i).tax_analysis_array

                        )

                    additemList.add(childModel)

                }
                prefs[Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY] = Gson().toJson(additemList)

                binding.linearCalculationView.visibility = View.VISIBLE

                binding.llClosingbalNewpurchase.visibility = View.VISIBLE
                adapter.apply {
                    addsalebillrow_item(additemList)
                    notifyDataSetChanged()
                }

                addTaxDatainPref()

                addIRTDatainPref()
                getIssueReceiveDataFromPref()

                if (purchaseDetailModel.transactionData!!.image != null && purchaseDetailModel.transactionData!!.image?.size!! > 0) {
                    binding.tvPbmUploadphoto.visibility = View.GONE
                    binding.ivPbmAttachment.visibility = View.VISIBLE
                    imageURL = purchaseDetailModel.transactionData!!.image?.get(0)?.image
                    Glide.with(this).load(imageURL).circleCrop()
                        .placeholder(R.drawable.ic_user_placeholder)
                        .into(binding.ivPbmAttachment)

                    /* val imageFile: File = File(imageURL)

                     val fileBody: RequestBody =
                         RequestBody.create(MediaType.parse("multipart/form-data"), imageFile)
                     multipartImageBody =
                         MultipartBody.Part.createFormData("image[]", imageFile.name, fileBody)
                     Log.v("imageFile", "" + imageFile.name)*/

                } else {
                    binding.tvPbmUploadphoto.visibility = View.VISIBLE
                }


                binding.tvNewpurchaseItemquantity.setText("Qty: " + purchaseDetailModel.transactionData?.total_quantity)
                binding.tvNewpurchaseGrossWt.setText("G: " + purchaseDetailModel.transactionData?.total_gross_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                binding.tvNewpurchaseLessWt.setText("L: " + purchaseDetailModel.transactionData?.total_less_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                binding.tvNewpurchaseNetWt.setText("N: " + purchaseDetailModel.transactionData?.total_net_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                binding.tvNewpurchaseFineWt.setText("F: " + purchaseDetailModel.transactionData?.total_fine_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                // binding.tvNewpurchaseMiscCharges.setText(Constants.AMOUNT_RS_APPEND + purchaseDetailModel.transactionData?.total_misc_charges)

                binding.tvNewpurchaseSilverItemquantity.setText("Qty: " + purchaseDetailModel.transactionData?.silver_total_quantity)
                binding.tvNewpurchaseSilverGrossWt.setText("G: " + purchaseDetailModel.transactionData?.silver_total_gross_wt)
                binding.tvNewpurchaseSilverLessWt.setText("L: " + purchaseDetailModel.transactionData?.silver_total_less_wt)
                binding.tvNewpurchaseSilverNetWt.setText("N: " + purchaseDetailModel.transactionData?.silver_total_net_wt)
                binding.tvNewpurchaseSilverFineWt.setText("F: " + purchaseDetailModel.transactionData?.silver_total_fine_wt)

                binding.tvNewpurchaseOtherItemquantity.setText("Qty: " + purchaseDetailModel.transactionData?.other_total_quantity)
                binding.tvNewpurchaseOtherGrossWt.setText("G: " + purchaseDetailModel.transactionData?.other_total_gross_wt)
                binding.tvNewpurchaseOtherLessWt.setText("L: 0.000")
                binding.tvNewpurchaseOtherNetWt.setText("N: " + purchaseDetailModel.transactionData?.other_total_net_wt)
                binding.tvNewpurchaseOtherFineWt.setText("F: 0.000")


                binding.tvNewpurchaseSubtotalCol1.setText(purchaseDetailModel.transactionData?.total_fine_wt)
                binding.tvNewpurchaseSubtotalCol2.setText(purchaseDetailModel.transactionData?.final_total_amount)
                binding.tvNewpurchaseSubtotalCol1Silver.setText(purchaseDetailModel.transactionData?.silver_total_fine_wt)

                when (binding.tvNewpurchaseSubtotalCol1.text) {
                    "0.000" -> {
                        binding.tvNewpurchaseSubtotalCol1.setText(purchaseDetailModel.transactionData?.total_fine_wt)
                        binding.tvNewpurchaseSubtotalCol1.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.header_black_text
                            )
                        )
                    }
                    else -> {
                        binding.tvNewpurchaseSubtotalCol1.setText(purchaseDetailModel.transactionData?.total_fine_wt + " " + subTotalTerm)
                        if (subTotalTermValue.equals("credit")) {
                            binding.tvNewpurchaseSubtotalCol1.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.credit_color
                                )
                            )
                        }
                    }
                }

                when (binding.tvNewpurchaseSubtotalCol2.text) {
                    "0.00" -> {
                        binding.tvNewpurchaseSubtotalCol2.setText(purchaseDetailModel.transactionData?.final_total_amount)
                        binding.tvNewpurchaseSubtotalCol2.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.header_black_text
                            )
                        )
                    }
                    else -> {
                        binding.tvNewpurchaseSubtotalCol2.setText(purchaseDetailModel.transactionData?.final_total_amount + " " + subTotalTerm)
                        if (subTotalTermValue.equals("credit")) {
                            binding.tvNewpurchaseSubtotalCol2.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.credit_color
                                )
                            )
                        }
                    }
                }

                when (binding.tvNewpurchaseSubtotalCol1Silver.text) {
                    "0.000" -> {
                        binding.tvNewpurchaseSubtotalCol1Silver.setText(purchaseDetailModel.transactionData?.silver_total_fine_wt)
                        binding.tvNewpurchaseSubtotalCol1Silver.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.header_black_text
                            )
                        )
                    }
                    else -> {
                        binding.tvNewpurchaseSubtotalCol1Silver.setText(purchaseDetailModel.transactionData?.silver_total_fine_wt + " " + subTotalTerm)
                        if (subTotalTermValue.equals("credit")) {
                            binding.tvNewpurchaseSubtotalCol1Silver.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.credit_color
                                )
                            )

                        }
                    }
                }


                binding.tvNewpurchaseTotalDueGold.setText(purchaseDetailModel.transactionData?.total_fine_wt_with_IRT)
                binding.tvNewpurchaseTotalDueSilver.setText(purchaseDetailModel.transactionData?.total_silver_fine_wt_with_IRT)
                binding.tvNewpurchaseTotalDueCash.setText(Constants.AMOUNT_RS_APPEND + purchaseDetailModel.transactionData?.grand_total)


                if (purchaseDetailModel.transactionData?.is_tds_applicable.equals("1") && purchaseDetailModel.transactionData?.is_tcs_applicable.equals(
                        "1"
                    )
                ) {
                    isTcsEnable = true
                    isTdsEnable = true
                } else {
                    isTcsEnable = false
                    isTdsEnable = false

                }


                when (purchaseDetailModel.transactionData?.is_tcs_applicable.equals("1")) {
                    true -> {
                        isTcsEnable = true
                    }
                    false -> {
                        isTcsEnable = false
                    }
                }

                when (purchaseDetailModel.transactionData?.is_tds_applicable.equals("1")) {
                    true -> {
                        isTdsEnable = true
                    }
                    false -> {
                        isTdsEnable = false
                    }
                }


                if (purchaseDetailModel.transactionData?.tds_tcs_enable.equals("tcs")) {
                    tds_tcs_enable = "tcs"
                    getLedgerdd("tcs")
                    binding.radioTCSNewpurchase.isChecked = true
                    binding.tvNewpurchaseTcstdsCol2.setText(purchaseDetailModel.transactionData!!.tcs_amount)
                    selectedTcsId = purchaseDetailModel.transactionData?.tcsData!!.ledger_id
                    binding.tvNewpurchaseTdstcsCol1.mLabelView!!.setText(purchaseDetailModel.transactionData?.tcsData!!.ledger_name)
                }

                if (purchaseDetailModel.transactionData?.tds_tcs_enable.equals("tds")) {
                    tds_tcs_enable = "tds"
                    selectedTdsId = purchaseDetailModel.transactionData?.tdsData!!.ledger_id
                    getLedgerdd("tds")
                    binding.radioTDSNewpurchase.isChecked = true
                    binding.tvNewpurchaseTcstdsCol2.setText("-" + purchaseDetailModel.transactionData!!.tds_amount)
                    binding.tvNewpurchaseTdstcsCol1.mLabelView!!.setText(purchaseDetailModel.transactionData?.tdsData!!.ledger_name)
                }

                /*if (purchaseDetailModel.transactionData?.tds_tcs_enable.equals("")) {
                    tds_tcs_enable = ""
                }*/
                when (purchaseDetailModel.transactionData?.is_gst_applicable.toString()
                    .contains("1")) {
                    true -> {
                        if ((purchaseDetailModel.transactionData?.sgst_amount!!.toBigDecimal() > BigDecimal.ZERO && !purchaseDetailModel.transactionData?.sgst_amount!!.isBlank()) &&
                            (purchaseDetailModel.transactionData?.place_of_supply_id.equals(
                                loginModel.data!!.branch_info!!.state_id.toString()
                            ))
                        ) {
                            is_gst_applicable == "1"
                            binding.tvNewpurchaseSgstCol0.visibility = View.VISIBLE
                            binding.tvNewpurchaseSgstCol1.visibility = View.VISIBLE
                            binding.tvNewpurchaseSgstCol2.visibility = View.VISIBLE
                            isSgstEnable = true
                            getLedgerdd("sgst")
                            selectedSgstId =
                                purchaseDetailModel.transactionData?.sgstData?.ledger_id
                            binding.tvNewpurchaseSgstCol1.mLabelView!!.setText(
                                purchaseDetailModel.transactionData?.sgstData?.ledger_name
                            )

                            binding.tvNewpurchaseSgstCol2.setText(Constants.AMOUNT_RS_APPEND + purchaseDetailModel.transactionData?.sgst_amount)
                        } else {
                            binding.tvNewpurchaseSgstCol0.visibility = View.GONE
                            binding.tvNewpurchaseSgstCol1.visibility = View.GONE
                            binding.tvNewpurchaseSgstCol2.visibility = View.GONE
                        }

                        if (
                            (purchaseDetailModel.transactionData?.cgst_amount!!.toBigDecimal() > BigDecimal.ZERO && !purchaseDetailModel.transactionData?.cgst_amount!!.isBlank()) &&
                            (purchaseDetailModel.transactionData?.place_of_supply_id.equals(
                                loginModel.data!!.branch_info!!.state_id.toString()
                            ))
                        ) {
                            binding.tvNewpurchaseCgstCol0.visibility = View.VISIBLE
                            binding.tvNewpurchaseCgstCol1.visibility = View.VISIBLE
                            binding.tvNewpurchaseCgstCol2.visibility = View.VISIBLE
                            isCgstEnable = true
                            getLedgerdd("cgst")
                            selectedCgstId =
                                purchaseDetailModel.transactionData?.cgstData?.ledger_id
                            binding.tvNewpurchaseCgstCol1.mLabelView!!.setText(
                                purchaseDetailModel.transactionData?.cgstData?.ledger_name
                            )

                            binding.tvNewpurchaseCgstCol2.setText(Constants.AMOUNT_RS_APPEND + purchaseDetailModel.transactionData?.cgst_amount)

                        } else {
                            binding.tvNewpurchaseCgstCol0.visibility = View.GONE
                            binding.tvNewpurchaseCgstCol1.visibility = View.GONE
                            binding.tvNewpurchaseCgstCol2.visibility = View.GONE
                        }

                        if ((purchaseDetailModel.transactionData?.igst_amount!!.toBigDecimal() > BigDecimal.ZERO && !purchaseDetailModel.transactionData?.igst_amount!!.isBlank()) &&
                            (!purchaseDetailModel.transactionData?.place_of_supply_id.equals(
                                loginModel.data!!.branch_info!!.state_id.toString()
                            ))
                        ) {
                            binding.tvNewpurchaseIgstCol0.visibility = View.VISIBLE
                            binding.tvNewpurchaseIgstCol1.visibility = View.VISIBLE
                            binding.tvNewpurchaseIgstCol2.visibility = View.VISIBLE
                            isIgstEnable = true
                            getLedgerdd("igst")
                            selectedIgstId =
                                purchaseDetailModel.transactionData?.igstData?.ledger_id
                            binding.tvNewpurchaseIgstCol1.mLabelView!!.setText(
                                purchaseDetailModel.transactionData?.igstData?.ledger_name
                            )
                            binding.tvNewpurchaseIgstCol2.setText(Constants.AMOUNT_RS_APPEND + purchaseDetailModel.transactionData?.igst_amount)
                        } else {
                            binding.tvNewpurchaseIgstCol0.visibility = View.GONE
                            binding.tvNewpurchaseIgstCol1.visibility = View.GONE
                            binding.tvNewpurchaseIgstCol2.visibility = View.GONE
                        }
                    }
                    false -> {
                        is_gst_applicable == "0"
                        //for non-gst purchase
                        //sgst
                        binding.tvNewpurchaseSgstCol0.visibility = View.GONE
                        binding.tvNewpurchaseSgstCol1.visibility = View.GONE
                        binding.tvNewpurchaseSgstCol2.visibility = View.GONE
                        //cgst
                        binding.tvNewpurchaseCgstCol0.visibility = View.GONE
                        binding.tvNewpurchaseCgstCol1.visibility = View.GONE
                        binding.tvNewpurchaseCgstCol2.visibility = View.GONE
                        //Igst
                        binding.tvNewpurchaseIgstCol0.visibility = View.GONE
                        binding.tvNewpurchaseIgstCol1.visibility = View.GONE
                        binding.tvNewpurchaseIgstCol2.visibility = View.GONE

                        binding.radiogroupTDSTCSNewpurchase.visibility = View.GONE
                        binding.tvNewpurchaseTdstcsCol1.visibility = View.GONE
                        binding.tvNewpurchaseTcstdsCol2.visibility = View.GONE
                    }
                }

                if (purchaseDetailModel.transactionData?.is_show_round_off.equals("1")) {
                    binding.tvNewpurchaseRoundOffCol0.visibility = View.VISIBLE
                    binding.tvNewpurchaseRoundoffCol1.visibility = View.VISIBLE
                    binding.tvNewpurchaseRoundoffCol2.visibility = View.VISIBLE
                    isRoundOffEnable = true
                    getLedgerdd("round_off")
                    binding.tvNewpurchaseRoundoffCol1.mLabelView!!.setText(
                        purchaseDetailModel.transactionData?.roundOffLedgerData?.ledger_name
                    )
                    selectedRoundoffId =
                        purchaseDetailModel.transactionData?.roundOffLedgerData?.ledger_id
                    roundOffUpdatedValue =
                        purchaseDetailModel.transactionData?.round_off_total.toString()
                    binding.tvNewpurchaseRoundoffCol2.setText(Constants.AMOUNT_RS_APPEND + purchaseDetailModel.transactionData?.round_off_total)
                }else{
                    binding.tvNewpurchaseRoundOffCol0.visibility = View.GONE
                    binding.tvNewpurchaseRoundoffCol1.visibility = View.GONE
                    binding.tvNewpurchaseRoundoffCol2.visibility = View.GONE
                }
/*
                when (!purchaseDetailModel.transactionData?.round_off_total.equals("0.00")) {
                    true -> {
                        binding.tvNewpurchaseRoundOffCol0.visibility = View.VISIBLE
                        binding.tvNewpurchaseRoundoffCol1.visibility = View.VISIBLE
                        binding.tvNewpurchaseRoundoffCol2.visibility = View.VISIBLE
                        isRoundOffEnable = true
                        getLedgerdd("round_off")
                        binding.tvNewpurchaseRoundoffCol1.mLabelView!!.setText(
                            purchaseDetailModel.transactionData?.roundOffLedgerData?.ledger_name
                        )
                        selectedRoundoffId =
                            purchaseDetailModel.transactionData?.roundOffLedgerData?.ledger_id
                        roundOffUpdatedValue =
                            purchaseDetailModel.transactionData?.round_off_total.toString()
                        binding.tvNewpurchaseRoundoffCol2.setText(Constants.AMOUNT_RS_APPEND + purchaseDetailModel.transactionData?.round_off_total)

                    }
                    false -> {
                        binding.tvNewpurchaseRoundOffCol0.visibility = View.GONE
                        binding.tvNewpurchaseRoundoffCol1.visibility = View.GONE
                        binding.tvNewpurchaseRoundoffCol2.visibility = View.GONE
                    }
                }
*/

            }
        }
    }

    private fun addIRTDatainPref() {
        purchaseLineList.clear()
        for (i in 0 until purchaseDetailModel.IRTData!!.size) {

            if (!purchaseDetailModel.IRTData!!.get(i).transaction_type.equals("")) {

                val saleIRTModel = SalesLineModel.SaleLineModelDetails(
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.cash_amount,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.cash_ledger,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.cash_ledger_name,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.cash_description,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.bank_amount,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.bank_ledger,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.bank_ledger_name,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.bank_mode,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.cheque_number,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.cheque_date,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.favouring_name,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.deuct_charges,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.deuct_charges_percentage,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.bank_final_amt,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.recipient_bank,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.account_no,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.ifs_code,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.utr_number,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.bank_description,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.rcm_gold_rate,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.rate_cut_amount,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.rate_cut_fine_term,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.metal_type_id_rate_cut,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.rate_cut_fine,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.item_id,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.item_name,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.metal_type_id_metal,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.maintain_stock_in_name_metal,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.gross_wt,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.less_wt,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.net_wt,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.touch,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.wast,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.fine_wt,
                    "", "", "", "",
                    "", "",
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.type,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.transaction_type,
                    purchaseDetailModel.IRTData!!.get(i).IRTDetails!!.transaction_title

                )

                purchaseLineList.add(saleIRTModel)
            }
        }

        prefs[Constants.PREF_SALES_LINE_INFO_KEY] = Gson().toJson(purchaseLineList)
    }


    private fun clearPref() {
        if (prefs.contains(Constants.PREF_SALES_LINE_INFO_KEY)) {
            prefs.edit().remove(Constants.PREF_SALES_LINE_INFO_KEY).apply()
        }
        if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {
            prefs.edit().remove(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY).apply()
        }
        if (prefs.contains(Constants.PREF_SALES_TAX_ANALYSIS_LIST_KEY)) {
            prefs.edit().remove(Constants.PREF_SALES_TAX_ANALYSIS_LIST_KEY).apply()
        }
    }


    private fun applyingDigitFilter() {
        // applying filters to edit input number decimal fields(which have 2 or 3 decimal after .)


    }

    private fun autogenerateinvoice(isFromOnCreate: Boolean) {
        when (isFromOnCreate) {
            true -> {
                txtDatePBM.setText(SimpleDateFormat("dd-MMM-yy").format(Date()))
            }
            else -> {

            }
        }

        binding.txtDatePBM.clickWithDebounce {
            openDatePicker(true)
        }
    }

    fun openDatePicker(isFromDate: Boolean) {
        val c = Calendar.getInstance()
        if (isFromDate) {
            val sdf = SimpleDateFormat("dd-MMM-yy")
            val parse = sdf.parse(binding.txtDatePBM.text.toString())
            c.setTime(parse)
        } else {


        }

        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)


        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->

                c.set(Calendar.YEAR, year)
                c.set(Calendar.MONTH, monthOfYear)
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Display Selected date in Date
                if (isFromDate) {
                    binding.txtDatePBM.setText(
                        "" + String.format(
                            "%02d",
                            dayOfMonth
                        ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                            .substring(2, 4)
                    )
                    when (is_From_Edit) {
                        false -> {
                            getPurchaseInvoiceNoFromApi()
                        }
                        else -> {

                        }
                    }

                    invoiceCalculation()
                }
                // txtDateNewInvoice.setText("" + String.format("%02d", dayOfMonth)   + "-" + String.format("%02d", (monthOfYear + 1)) + "-" + year.toString().substring(2,4))

            },
            year,
            month,
            day
        )

        val minDate = fiscalYearModel.start_date
        val sdf1 = SimpleDateFormat("dd-MMM-yyyy")
        val date = sdf1.parse(minDate)

        val startDate = date.time

        val maxDate = fiscalYearModel.end_date
        val date1 = sdf1.parse(maxDate)
        val endDate = date1.time

        dpd.datePicker.minDate = startDate
        dpd.datePicker.maxDate = endDate
        // dpd.datePicker.maxDate = Date().time
        dpd.show()
    }


    fun getPurchaseInvoiceNoFromApi() {
        viewModel.getPurchaseInvoiceNoFromApi(
            loginModel.data?.bearer_access_token,
            binding.txtDatePBM.text.toString(),
            transaction_id
        ).observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {

                        if (it.data?.status == true) {
                            isNoGenerated = true
                            binding.txtpurchaseNoPBM.setText(it.data.data?.series)

                            //is_prefix = it.data.data?.prefix
                            is_series = it.data.data?.series
                            // is_suffix = it.data.data?.suffix

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
                                    "sgst" -> {
                                        ledgerSgstNameList = ArrayList<String>()
                                        ledgerSgsttList = it.data.data
                                        ledgerSgstNameList =
                                            ledgerSgsttList?.map { it.name.toString() }

                                        binding.tvNewpurchaseSgstCol1.setItems(
                                            ledgerSgstNameList
                                        )

                                        for (i in 0 until ledgerSgsttList!!.size) {
                                            if (ledgerSgsttList!!.get(i).ledger_id.equals(
                                                    selectedSgstId
                                                )
                                            ) {
                                                binding.tvNewpurchaseSgstCol1.is_from_edit =
                                                    true
                                                binding.tvNewpurchaseSgstCol1.mLabelView!!.setText(
                                                    ledgerSgsttList!!.get(i).name
                                                )
                                            }
                                        }
                                        binding.tvNewpurchaseSgstCol1.setOnItemSelectListener(
                                            object :
                                                SearchableSpinner.SearchableItemListener {
                                                override fun onItemSelected(
                                                    view: View?,
                                                    position: Int
                                                ) {
                                                    selectedSgstId =
                                                        position.let { it1 ->
                                                            ledgerSgsttList?.get(
                                                                it1
                                                            )?.ledger_id
                                                        }

                                                }

                                                override fun onSelectionClear() {

                                                }
                                            })
                                    }
                                    "cgst" -> {
                                        ledgerCgstNameList = ArrayList<String>()
                                        ledgerCgstList = it.data.data
                                        ledgerCgstNameList =
                                            ledgerCgstList?.map { it.name.toString() }

                                        binding.tvNewpurchaseCgstCol1.setItems(
                                            ledgerCgstNameList
                                        )

                                        for (i in 0 until ledgerCgstList!!.size) {
                                            if (ledgerCgstList!!.get(i).ledger_id.equals(
                                                    selectedCgstId
                                                )
                                            ) {
                                                binding.tvNewpurchaseCgstCol1.is_from_edit =
                                                    true
                                                binding.tvNewpurchaseCgstCol1.mLabelView!!.setText(
                                                    ledgerCgstList!!.get(i).name
                                                )
                                            }
                                        }
                                        binding.tvNewpurchaseCgstCol1.setOnItemSelectListener(
                                            object :
                                                SearchableSpinner.SearchableItemListener {
                                                override fun onItemSelected(
                                                    view: View?,
                                                    position: Int
                                                ) {
                                                    selectedCgstId =
                                                        position.let { it1 ->
                                                            ledgerCgstList?.get(
                                                                it1
                                                            )?.ledger_id
                                                        }

                                                }

                                                override fun onSelectionClear() {

                                                }
                                            })
                                    }
                                    "igst" -> {
                                        ledgerIgstNameList = ArrayList<String>()
                                        ledgerIgstList = it.data.data
                                        ledgerIgstNameList =
                                            ledgerIgstList?.map { it.name.toString() }

                                        binding.tvNewpurchaseIgstCol1.setItems(
                                            ledgerIgstNameList
                                        )
                                        for (i in 0 until ledgerIgstList!!.size) {
                                            if (ledgerIgstList!!.get(i).ledger_id.equals(
                                                    selectedIgstId
                                                )
                                            ) {
                                                binding.tvNewpurchaseIgstCol1.is_from_edit =
                                                    true
                                                binding.tvNewpurchaseIgstCol1.mLabelView!!.setText(
                                                    ledgerIgstList!!.get(i).name
                                                )
                                            }
                                        }
                                        binding.tvNewpurchaseIgstCol1.setOnItemSelectListener(
                                            object :
                                                SearchableSpinner.SearchableItemListener {
                                                override fun onItemSelected(
                                                    view: View?,
                                                    position: Int
                                                ) {
                                                    selectedIgstId =
                                                        position.let { it1 ->
                                                            ledgerIgstList?.get(
                                                                it1
                                                            )?.ledger_id
                                                        }

                                                }

                                                override fun onSelectionClear() {

                                                }
                                            })

                                    }
                                    "tcs" -> {
                                        ledgerTcsNameList = ArrayList<String>()
                                        ledgerTcsList = it.data.data
                                        ledgerTcsNameList =
                                            ledgerTcsList?.map { it.name.toString() }

                                        binding.tvNewpurchaseTdstcsCol1.setItems(
                                            ledgerTcsNameList
                                        )
                                        for (i in 0 until ledgerTcsList!!.size) {
                                            if (ledgerTcsList!!.get(i).ledger_id.equals(
                                                    selectedTcsId
                                                )
                                            ) {
                                                binding.tvNewpurchaseTdstcsCol1.is_from_edit =
                                                    true
                                                binding.tvNewpurchaseTdstcsCol1.mLabelView!!.setText(
                                                    ledgerTcsList!!.get(i).name!!.replaceRange(
                                                        10,
                                                        ledgerTcsList!!.get(i).name!!.length,
                                                        ".."
                                                    )
                                                )
                                            } else {
                                                binding.tvNewpurchaseTdstcsCol1.mLabelView!!.setText(
                                                    ""
                                                )
                                            }
                                        }

                                        binding.tvNewpurchaseTdstcsCol1.setOnItemSelectListener(
                                            object :
                                                SearchableSpinner.SearchableItemListener {
                                                override fun onItemSelected(
                                                    view: View?,
                                                    position: Int
                                                ) {

                                                    selectedTcsId =
                                                        position.let { it1 ->
                                                            ledgerTcsList?.get(
                                                                it1
                                                            )?.ledger_id
                                                        }
                                                            .toString()

                                                }

                                                override fun onSelectionClear() {

                                                }
                                            })
                                    }
                                    "tds" -> {
                                        ledgerTdsNameList = ArrayList<String>()
                                        ledgerTdsList = it.data.data
                                        ledgerTdsNameList =
                                            ledgerTdsList?.map { it.name.toString() }

                                        binding.tvNewpurchaseTdstcsCol1.setItems(
                                            ledgerTdsNameList
                                        )
                                        for (i in 0 until ledgerTdsList!!.size) {
                                            if (ledgerTdsList!!.get(i).ledger_id.equals(
                                                    selectedTdsId
                                                )
                                            ) {
                                                binding.tvNewpurchaseTdstcsCol1.is_from_edit =
                                                    true
                                                binding.tvNewpurchaseTdstcsCol1.mLabelView!!.setText(
                                                    ledgerTdsList!!.get(i).name
                                                )
                                            } else {
                                                binding.tvNewpurchaseTdstcsCol1.mLabelView!!.setText(
                                                    ""
                                                )
                                            }
                                        }

                                        binding.tvNewpurchaseTdstcsCol1.setOnItemSelectListener(
                                            object :
                                                SearchableSpinner.SearchableItemListener {
                                                override fun onItemSelected(
                                                    view: View?,
                                                    position: Int
                                                ) {
                                                    selectedTdsId =
                                                        position.let { it1 ->
                                                            ledgerTdsList?.get(
                                                                it1
                                                            )?.ledger_id
                                                        }
                                                            .toString()

                                                }

                                                override fun onSelectionClear() {

                                                }
                                            })

                                    }
                                    "round_off" -> {
                                        ledgerRoundoffNameList = ArrayList<String>()
                                        ledgerRoundOffList = it.data.data
                                        ledgerRoundoffNameList =
                                            ledgerRoundOffList?.map { it.name.toString() }


                                        binding.tvNewpurchaseRoundoffCol1.setItems(
                                            ledgerRoundoffNameList
                                        )
                                        for (i in 0 until ledgerRoundOffList!!.size) {
                                            if (ledgerRoundOffList!!.get(i).ledger_id.equals(
                                                    selectedRoundoffId
                                                )
                                            ) {
                                                binding.tvNewpurchaseRoundoffCol1.is_from_edit =
                                                    true
                                                binding.tvNewpurchaseRoundoffCol1.mLabelView!!.setText(
                                                    ledgerRoundOffList!!.get(i).name
                                                )
                                            }
                                        }
                                        binding.tvNewpurchaseRoundoffCol1.setOnItemSelectListener(
                                            object :
                                                SearchableSpinner.SearchableItemListener {
                                                override fun onItemSelected(
                                                    view: View?,
                                                    position: Int
                                                ) {
                                                    selectedRoundoffId =
                                                        position.let { it1 ->
                                                            ledgerRoundOffList?.get(
                                                                it1
                                                            )?.ledger_id
                                                        }.toString()

                                                }

                                                override fun onSelectionClear() {

                                                }
                                            })

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


    private fun addPBMRequestBodyParamCallAPI() {
        if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {
            val collectionType =
                object :
                    TypeToken<ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>>() {}.type
            var itemList: ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem> =
                Gson().fromJson(
                    prefs[Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY, ""],
                    collectionType
                )

            if (prefs.contains(Constants.PREF_SALES_LINE_INFO_KEY)) {
                val collectionType =
                    object :
                        TypeToken<ArrayList<SalesLineModel.SaleLineModelDetails>>() {}.type
                purchaseLineList =
                    Gson().fromJson(
                        prefs[Constants.PREF_SALES_LINE_INFO_KEY, ""],
                        collectionType
                    )

            } else {
                var childModel = SalesLineModel.SaleLineModelDetails(
                    "", "", "",
                    "", "", "", "", "",
                    "", "", "", "", "",
                    "", "", "", "", "", "", "",
                    "", "", "", "", "", "",
                    "", "", "", "", "", "", "", "", "", "", "",
                    "", "", "", "", "", ""
                )
                purchaseLineList.add(childModel)
            }

            val issue_receive_transaction: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                Gson().toJson(purchaseLineList)
            )

            val itemadded: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                Gson().toJson(itemList)
            )

            val transaction_type_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                "1"
            )

            val transaction_type_name: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                "purchase"
            )

            val transaction_date: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.txtDatePBM.text.toString()
            )

            val invoice_number: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                is_series.toString().trim()
            )

            val referrence: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.txtReffPBM.text.toString()
            )

            val remarks: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.txtRemarkPBM.text.toString()
            )

            val customer_code: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                selectedCustomerCode
            )

            val display_name: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.txtSupplierNamePBM.text.toString()
            )
            val contact_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                selectedContactID
            )
            val party_po_no: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                " Party PO No"
            )
            val place_of_supply: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                selectedPlaceOfSupplyID
            )
            val sgst_ledger_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                selectedSgstId
            )
            val cgst_ledger_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                selectedCgstId
            )
            val igst_ledger_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                selectedIgstId
            )
            val tds_ledger_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                selectedTdsId
            )
            val tds_percentage: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                "0"
            )
            val tcs_ledger_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                selectedTcsId
            )
            val tcs_percentage: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                "0"
            )
            val tds_tcs_enable: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                tds_tcs_enable
            )
            val round_off_ledger_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                selectedRoundoffId
            )
            val round_off_total: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                roundOffUpdatedValue
            )
            val branch_type: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                loginModel?.data?.branch_info?.branch_type
            )
            val ledger_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                ""
            )
            val transaction_type: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                ""
            )

            addPBMAPI(
                loginModel?.data?.bearer_access_token,
                transaction_type_id,
                transaction_type_name,
                transaction_date,
                customer_code,
                display_name,
                contact_id,
                party_po_no,
                referrence,
                remarks,
                invoice_number,
                itemadded,
                issue_receive_transaction,
                place_of_supply,
                sgst_ledger_id,
                cgst_ledger_id,
                igst_ledger_id,
                tds_ledger_id,
                tds_percentage,
                tcs_ledger_id,
                tcs_percentage,
                tds_tcs_enable,
                round_off_ledger_id,
                round_off_total,
                branch_type,
                ledger_id,
                multipartImageBody,
                transaction_type
            )


        } else {

        }


    }

    fun addPBMAPI(
        token: String?,
        transaction_type_id: RequestBody?,
        transaction_type_name: RequestBody?,
        transaction_date: RequestBody?,
        customer_code: RequestBody?,
        display_name: RequestBody?,
        contact_id: RequestBody?,
        party_po_no: RequestBody?,
        referrence: RequestBody?,
        remarks: RequestBody?,
        invoice_number: RequestBody?,
        item_json: RequestBody?,
        issue_receive_transaction: RequestBody?,
        place_of_supply: RequestBody?,
        sgst_ledger_id: RequestBody?,
        cgst_ledger_id: RequestBody?,
        igst_ledger_id: RequestBody?,
        tds_ledger_id: RequestBody?,
        tds_percentage: RequestBody?,
        tcs_ledger_id: RequestBody?,
        tcs_percentage: RequestBody?,
        tds_tcs_enable: RequestBody?,
        round_off_ledger_id: RequestBody?,
        round_off_total: RequestBody?,
        branch_type: RequestBody?,
        ledger_id: RequestBody?,
        image: MultipartBody.Part??,
        transaction_type: RequestBody?
    ) {

        viewModel.addPBM(
            token, transaction_type_id,
            transaction_type_name,
            transaction_date,
            customer_code,
            display_name,
            contact_id,
            party_po_no,
            referrence,
            remarks,
            invoice_number,
            item_json,
            issue_receive_transaction,
            place_of_supply,
            sgst_ledger_id,
            cgst_ledger_id,
            igst_ledger_id,
            tds_ledger_id,
            tds_percentage,
            tcs_ledger_id,
            tcs_percentage,
            tds_tcs_enable,
            round_off_ledger_id,
            round_off_total,
            branch_type,
            ledger_id,
            image,
            transaction_type
        ).observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        if (it.data?.status == true) {

                            Toast.makeText(
                                this,
                                it.data.message,
                                Toast.LENGTH_LONG
                            )
                                .show()

                            this.finish()

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

    fun editPBMAPI(
        token: String?,
        transaction_id: RequestBody?,
        transaction_type_id: RequestBody?,
        transaction_type_name: RequestBody?,
        transaction_date: RequestBody?,
        customer_code: RequestBody?,
        display_name: RequestBody?,
        contact_id: RequestBody?,
        party_po_no: RequestBody?,
        referrence: RequestBody?,
        remarks: RequestBody?,
        invoice_number: RequestBody?,
        item_json: RequestBody?,
        issue_receive_transaction: RequestBody?,
        place_of_supply: RequestBody?,
        sgst_ledger_id: RequestBody?,
        cgst_ledger_id: RequestBody?,
        igst_ledger_id: RequestBody?,
        tds_ledger_id: RequestBody?,
        tds_percentage: RequestBody?,
        tcs_ledger_id: RequestBody?,
        tcs_percentage: RequestBody?,
        tds_tcs_enable: RequestBody?,
        round_off_ledger_id: RequestBody?,
        round_off_total: RequestBody?,
        branch_type: RequestBody?,
        ledger_id: RequestBody?,
        image: MultipartBody.Part??,
        transaction_type: RequestBody?
    ) {

        viewModel.editPBM(
            token, transaction_id, transaction_type_id,
            transaction_type_name,
            transaction_date,
            customer_code,
            display_name,
            contact_id,
            party_po_no,
            referrence,
            remarks,
            invoice_number,
            item_json,
            issue_receive_transaction,
            place_of_supply,
            sgst_ledger_id,
            cgst_ledger_id,
            igst_ledger_id,
            tds_ledger_id,
            tds_percentage,
            tcs_ledger_id,
            tcs_percentage,
            tds_tcs_enable,
            round_off_ledger_id,
            round_off_total,
            branch_type,
            ledger_id,
            image,
            transaction_type
        ).observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        if (it.data?.status == true) {

                            Toast.makeText(
                                this,
                                it.data.message,
                                Toast.LENGTH_LONG
                            )
                                .show()

                            this.finish()

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


    fun performValidation(): Boolean {
        if (binding.txtSupplierNamePBM.text.toString().isBlank()) {
            CommonUtils.showDialog(
                this,
                getString(R.string.select_contact_first_msg)
            )
            binding.txtSupplierNamePBM.requestFocus()
            return false
        } else if (binding.txtDatePBM.text.toString().isBlank()) {
            CommonUtils.showDialog(
                this,
                getString(R.string.select_date_msg)
            )
            binding.txtDatePBM.requestFocus()
            return false
        } else if (!prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {
            CommonUtils.showDialog(
                this,
                getString(R.string.add_an_item_msg)
            )
            return false
        } else if (is_gst_applicable == "1" && binding.txtSupplyPBM.text.toString().isBlank()) {
            CommonUtils.showDialog(
                this,
                getString(R.string.enter_place_of_supply_msg)
            )
            binding.txtSupplyPBM.requestFocus()
            return false
        } else if (is_gst_applicable == "1" && isSgstEnable == true && selectedSgstId.isNullOrBlank()) {
            if (is_sgst_applicable == "1"){
                CommonUtils.showDialog(this, "Please Select SGST Ledger From Drop Down")
                return false
            }
        } else if (is_gst_applicable == "1" && isCgstEnable == true && selectedCgstId.isNullOrBlank()) {
            if (is_cgst_applicable == "1"){
                CommonUtils.showDialog(this, "Please Select CGST Ledger From Drop Down")
                return false
            }
        } else if (is_gst_applicable == "1" && isIgstEnable == true && selectedIgstId.isNullOrBlank()) {
            if (is_igst_applicable == "1"){
                CommonUtils.showDialog(this, "Please Select IGST Ledger From Drop Down")
                return false
            }
        } else if (is_gst_applicable == "1" && isTcsEnable == true && tds_tcs_enable == "tcs" && selectedTcsId.isNullOrBlank()) {
            CommonUtils.showDialog(this, "Please Select TCS Ledger From Drop Down")
            return false
        } else if (is_gst_applicable == "1" && isTdsEnable == true && tds_tcs_enable == "tds" && selectedTdsId.isNullOrBlank()) {
            CommonUtils.showDialog(this, "Please Select TDS Ledger From Drop Down")
            return false
        } else if (isRoundOffEnable && selectedRoundoffId.isNullOrBlank()) {
            CommonUtils.showDialog(this, "Provide Round-Off Ledger")
            return false
        }

        return true
    }

    fun invoiceCalculateAPI(
        showLoading: Boolean
    ) {


        if (NetworkUtils.isConnected()) {

            viewModel.getCalculateItemPurchase(
                loginModel.data?.bearer_access_token,
                "1",
                selectedContactID,
                transaction_id,
                Gson().toJson(additemList),
                Gson().toJson(purchaseLineList),
                is_gst_applicable,
                tds_percentage,
                tcs_percentage,
                selectedPlaceOfSupplyID,
                tds_tcs_enable,
                selectedSgstId,
                selectedCgstId,
                selectedIgstId,
                selectedTcsId,
                selectedRoundoffId,
                roundOffUpdatedValue,
                binding.txtDatePBM.text.toString().trim()
            )
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {
                                    if (it.data.message?.length!! > 5)
                                        Toast.makeText(
                                            this,
                                            it.data.message,
                                            Toast.LENGTH_LONG
                                        )
                                            .show()
                                    fill_item_details_data(it.data)

                                } else {

                                    when (it.data!!.code == Constants.ErrorCode) {
                                        true -> {
                                            Toast.makeText(
                                                this,
                                                it.data.errormessage?.message,
                                                Toast.LENGTH_LONG
                                            )
                                                .show()
                                            if (is_rate_cut.equals("1", true)) {
                                                isRateCutSaved = false
                                            }
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
                                if (showLoading)
                                    CommonUtils.showProgress(this)
                            }
                        }
                    }
                })
        }
    }

    fun removeItem(index: Int) {
        if (isValidClickPressed()) {
            if (additemList != null && additemList.size > 0) {
                if (index >= additemList.size) {
                    //index not exists
                } else {
                    // index exists
                    additemList.removeAt(index)
                    adapter.apply {
                        addsalebillrow_item(additemList)
                        notifyDataSetChanged()
                    }

                    if (additemList.size > 0) {
                        prefs[Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY] =
                            Gson().toJson(additemList)
                        invoiceCalculation()
                    } else {
                        prefs.edit().remove(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY).apply()
                        binding.rvNewpurchaseItem.visibility = View.GONE
                        invoiceCalculateAPI(true)
                    }
                }
            }
        }

    }

    fun removeIssueReceiveItem(index: Int) {
        if (isValidClickPressed()) {
            if (purchaseLineList != null && purchaseLineList.size > 0) {
                if (index >= purchaseLineList.size) {
                    //index not exists
                } else {
                    // index exists
                    purchaseLineList.removeAt(index)


                    if (purchaseLineList.size > 0) {
                        prefs[Constants.PREF_SALES_LINE_INFO_KEY] = Gson().toJson(purchaseLineList)
                    } else {
                        prefs.edit().remove(Constants.PREF_SALES_LINE_INFO_KEY).apply()
                        binding.rvIssueReceiveList.visibility = View.GONE
                    }
                    invoiceCalculation()
                }
            }
        }
    }

    fun editIssueReceiveItem(
        index: Int,
        issueReceive_rowModel: SalesLineModel.SaleLineModelDetails
    ) {
        if (isValidClickPressed()) {
            if (purchaseLineList != null && purchaseLineList.size > 0) {
                if (index >= purchaseLineList.size) {
                    //index not exists
                } else {
                    // index exists
                    startActivity(
                        Intent(this@NewPurchaseActivity, AddCashBankActivity::class.java)
                            .putExtra(Constants.ISSUE_RECEIVE_POS, index)
                            .putExtra(
                                Constants.ISSUE_RECEIVE_MODEL,
                                Gson().toJson(issueReceive_rowModel)
                            )

                    )
                }
            }
        }
    }


    fun getSearchContact() {
        contactList = ArrayList<SearchContactModel.Data.Contact>()
        contactNameList = ArrayList<String>()
        viewModel.getSearchContacts(
            loginModel.data?.bearer_access_token,
            loginModel?.data?.company_info?.id,
            "",
            "", ""
        ).observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        Log.v("..setupObservers..", "..Success...")
                        if (it.data?.status == true) {
                            contactList = it.data.data?.contact

                            contactNameList = contactList?.map { it.full_name.toString() }

                            contactNameAdapter = ArrayAdapter<String>(
                                this,
                                android.R.layout.simple_dropdown_item_1line,
                                contactNameList!!
                            )
                            binding.txtSupplierNamePBM.setAdapter(contactNameAdapter)
                            binding.txtSupplierNamePBM.threshold = 1

                            binding.txtSupplierNamePBM.setOnItemClickListener { adapterView, _, position, _
                                ->
                                val selected: String =
                                    adapterView.getItemAtPosition(position).toString()
                                val pos: Int? = contactNameList?.indexOf(selected)

                                selectedContactID =
                                    pos?.let { it1 -> contactList?.get(it1)?.contact_id }
                                selectedCustomerCode =
                                    pos?.let { it1 -> contactList?.get(it1)?.customer_code }
                                selectedPlaceOfSupply =
                                    pos?.let { it1 -> contactList?.get(it1)?.place_of_supply }
                                selectedPlaceOfSupplyID =
                                    pos?.let { it1 -> contactList?.get(it1)?.state_id }

                                txtSupplyPBM.setText(selectedPlaceOfSupply)

                                when (pos?.let { it1 -> contactList?.get(it1)?.is_tcs_applicable }!!
                                    .equals("1", true)) {
                                    true -> {
                                        isTcsEnable = true

                                    }
                                    else -> {

                                    }
                                }
                                when (pos?.let { it1 -> contactList?.get(it1)?.is_tds_applicable }!!
                                    .equals("1", true)) {
                                    true -> {
                                        isTdsEnable = true

                                    }
                                    else -> {

                                    }
                                }

                                //autogenerateinvoice
                                autogenerateinvoice(false)
                                invoiceCalculation()

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

    fun getDefaultTerm() {
        if (NetworkUtils.isConnected()) {
            viewModel.getDefaultTerm(
                loginModel?.data?.bearer_access_token
            ).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {

                            if (it.data?.status == true) {
                                fineDefaultTermList = it.data.data!!.default_term
                                subTotalTerm = fineDefaultTermList!!.get(1).default_short_term!!
                                subTotalTermValue =
                                    fineDefaultTermList!!.get(1).default_term_value!!

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            val fileUri = data?.data
            //imgProfile.setImageURI(fileUri)

            tv_pbm_uploadphoto.visibility = View.GONE
            iv_pbm_attachment.visibility = View.VISIBLE
            isPhotoSelected = true
            Glide.with(this).load(fileUri).circleCrop().into(iv_pbm_attachment)

            //You can get File object from intent
            val imageFile: File = ImagePicker.getFile(data)!!

            val fileBody: RequestBody =
                RequestBody.create(MediaType.parse("multipart/form-data"), imageFile)
            multipartImageBody =
                MultipartBody.Part.createFormData("image[]", imageFile.name, fileBody)
            //updateProfileImageAPI(loginModel?.data?.bearer_access_token, multipartBody)

            //You can also get File Path from intent
            // val filePath:String = ImagePicker.getFilePath(data)!!


        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
//
        }
    }

    private fun fill_item_details_data(calculatePurchaseModel: CalculateSalesModel) {

        calculatePurchaseModelMain = calculatePurchaseModel

        if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {
            if (additemList.size > 0) {
                adapter.apply {
                    addsalebillrow_item(additemList)
                    notifyDataSetChanged()
                }
                addTaxDatainPref()

            }
        }


        binding.tvNewpurchaseItemquantity.setText("Qty: " + calculatePurchaseModel.data?.total_quantity)
        binding.tvNewpurchaseGrossWt.setText("G: " + calculatePurchaseModel.data?.total_gross_wt)
        binding.tvNewpurchaseLessWt.setText("L: " + calculatePurchaseModel.data?.total_less_wt)
        binding.tvNewpurchaseNetWt.setText("N: " + calculatePurchaseModel.data?.total_net_wt)
        binding.tvNewpurchaseFineWt.setText("F: " + calculatePurchaseModel.data?.total_fine_wt)
        //   binding.tvNewpurchaseMiscCharges.setText(Constants.AMOUNT_RS_APPEND + calculatePurchaseModel.data?.total_misc_charges)

        binding.tvNewpurchaseSilverItemquantity.setText("Qty: " + calculatePurchaseModel.data?.silver_total_quantity)
        binding.tvNewpurchaseSilverGrossWt.setText("G: " + calculatePurchaseModel.data?.silver_total_gross_wt)
        binding.tvNewpurchaseSilverLessWt.setText("L: " + calculatePurchaseModel.data?.silver_total_less_wt)
        binding.tvNewpurchaseSilverNetWt.setText("N: " + calculatePurchaseModel.data?.silver_total_net_wt)
        binding.tvNewpurchaseSilverFineWt.setText("F: " + calculatePurchaseModel.data?.silver_total_fine_wt)

        binding.tvNewpurchaseOtherItemquantity.setText("Qty: " + calculatePurchaseModel.data?.other_total_quantity)
        binding.tvNewpurchaseOtherGrossWt.setText("G: " + calculatePurchaseModel.data?.other_total_gross_wt)
        binding.tvNewpurchaseOtherLessWt.setText("L: 0.000")
        binding.tvNewpurchaseOtherNetWt.setText("N: " + calculatePurchaseModel.data?.other_total_net_wt)
        binding.tvNewpurchaseOtherFineWt.setText("F: 0.000")


        binding.tvNewpurchaseSubtotalCol1.setText(calculatePurchaseModel.data?.total_fine_wt)
        binding.tvNewpurchaseSubtotalCol2.setText(calculatePurchaseModel.data?.final_total_amount)
        binding.tvNewpurchaseSubtotalCol1Silver.setText(calculatePurchaseModel.data?.silver_total_fine_wt)

        when(binding.tvNewpurchaseSubtotalCol1.text){
            "0.000"->{
                binding.tvNewpurchaseSubtotalCol1.setText(calculatePurchaseModel.data?.total_fine_wt)
                binding.tvNewpurchaseSubtotalCol1.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else->{
                binding.tvNewpurchaseSubtotalCol1.setText(calculatePurchaseModel.data?.total_fine_wt + " " + subTotalTerm)
                if (subTotalTermValue.equals("credit")) {
                    binding.tvNewpurchaseSubtotalCol1.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
                }
            }
        }

        when(binding.tvNewpurchaseSubtotalCol2.text){
            "0.00"->{
                binding.tvNewpurchaseSubtotalCol2.setText(calculatePurchaseModel.data?.final_total_amount)
                binding.tvNewpurchaseSubtotalCol2.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else->{
                binding.tvNewpurchaseSubtotalCol2.setText(calculatePurchaseModel.data?.final_total_amount + " " + subTotalTerm)
                if (subTotalTermValue.equals("credit")) {
                    binding.tvNewpurchaseSubtotalCol2.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
                }
            }
        }

        when(binding.tvNewpurchaseSubtotalCol1Silver.text){
           "0.000"->{
               binding.tvNewpurchaseSubtotalCol1Silver.setText(calculatePurchaseModel.data?.silver_total_fine_wt)
               binding.tvNewpurchaseSubtotalCol1Silver.setTextColor(
                   ContextCompat.getColor(
                       this,
                       R.color.header_black_text
                   )
               )
           }
            else->{
                binding.tvNewpurchaseSubtotalCol1Silver.setText(calculatePurchaseModel.data?.silver_total_fine_wt + " " + subTotalTerm)
                if (subTotalTermValue.equals("credit")) {
                    binding.tvNewpurchaseSubtotalCol1Silver.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
                }
                }
            }



        binding.tvNewpurchaseTotalDueGold.setText(calculatePurchaseModel.data?.total_fine_wt_with_IRT)
        binding.tvNewpurchaseTotalDueSilver.setText(calculatePurchaseModel.data?.total_silver_fine_wt_with_IRT)
        binding.tvNewpurchaseTotalDueCash.setText(Constants.AMOUNT_RS_APPEND + calculatePurchaseModel.data?.grand_total)



        if (!calculatePurchaseModel.data?.total_gross_wt.equals("0.000")) {
            binding.llNewpurchaseMetalgold.visibility = View.VISIBLE

        } else {
            binding.llNewpurchaseMetalgold.visibility = View.GONE

        }

        if (!calculatePurchaseModel.data?.silver_total_gross_wt.equals("0.000")) {
            binding.llNewpurchaseMetalsilver.visibility = View.VISIBLE

        } else {
            binding.llNewpurchaseMetalsilver.visibility = View.GONE

        }

        if (!calculatePurchaseModel.data?.other_total_gross_wt.equals("0.000")) {
            binding.llNewpurchaseMetalother.visibility = View.VISIBLE

        } else {
            binding.llNewpurchaseMetalother.visibility = View.GONE

        }


        updateUIofTotalDue(calculatePurchaseModel)

        when (is_gst_applicable) {
            "1" -> {
                // sgst
                if (
                    (calculatePurchaseModel.data?.sgst_amount!!.toBigDecimal() > BigDecimal.ZERO
                            && !calculatePurchaseModel.data?.sgst_amount.isBlank()) &&
                    (selectedPlaceOfSupplyID!!.equals(loginModel.data!!.branch_info!!.state_id.toString()))
                ) {
                    is_sgst_applicable == "1"
                    binding.tvNewpurchaseSgstCol0.visibility = View.VISIBLE
                    binding.tvNewpurchaseSgstCol1.visibility = View.VISIBLE
                    binding.tvNewpurchaseSgstCol2.visibility = View.VISIBLE
                    isSgstEnable = true
                    getLedgerdd("sgst")
                    binding.tvNewpurchaseSgstCol2.setText(Constants.AMOUNT_RS_APPEND + calculatePurchaseModel.data?.sgst_amount)

                } else {
                    is_sgst_applicable == "0"
                    binding.tvNewpurchaseSgstCol0.visibility = View.GONE
                    binding.tvNewpurchaseSgstCol1.visibility = View.GONE
                    binding.tvNewpurchaseSgstCol2.visibility = View.GONE
                }
                // cgst
                if (
                    (calculatePurchaseModel.data?.cgst_amount!!.toBigDecimal() > BigDecimal.ZERO
                            && !calculatePurchaseModel.data?.cgst_amount.isBlank())
                    &&
                    (selectedPlaceOfSupplyID!!.equals(loginModel.data!!.branch_info!!.state_id.toString()))
                ) {
                    is_cgst_applicable == "1"
                    binding.tvNewpurchaseCgstCol0.visibility = View.VISIBLE
                    binding.tvNewpurchaseCgstCol1.visibility = View.VISIBLE
                    binding.tvNewpurchaseCgstCol2.visibility = View.VISIBLE

                    isCgstEnable = true
                    getLedgerdd("cgst")
                    binding.tvNewpurchaseCgstCol2.setText(Constants.AMOUNT_RS_APPEND + calculatePurchaseModel.data?.cgst_amount)
                } else {
                    is_cgst_applicable == "0"
                    binding.tvNewpurchaseCgstCol0.visibility = View.GONE
                    binding.tvNewpurchaseCgstCol1.visibility = View.GONE
                    binding.tvNewpurchaseCgstCol2.visibility = View.GONE
                }

                // igst
                if ((calculatePurchaseModel.data?.igst_amount!!.toBigDecimal() > BigDecimal.ZERO
                            && !calculatePurchaseModel.data?.igst_amount.isBlank())
                    &&
                    (!selectedPlaceOfSupplyID!!.equals(loginModel.data!!.branch_info!!.state_id.toString()))
                ) {
                    is_igst_applicable == "1"
                    binding.tvNewpurchaseIgstCol0.visibility = View.VISIBLE
                    binding.tvNewpurchaseIgstCol1.visibility = View.VISIBLE
                    binding.tvNewpurchaseIgstCol2.visibility = View.VISIBLE
                    isIgstEnable = true
                    getLedgerdd("igst")
                    binding.tvNewpurchaseIgstCol2.setText(Constants.AMOUNT_RS_APPEND + calculatePurchaseModel.data?.igst_amount)
                } else {
                    is_igst_applicable == "0"
                    binding.tvNewpurchaseIgstCol0.visibility = View.GONE
                    binding.tvNewpurchaseIgstCol1.visibility = View.GONE
                    binding.tvNewpurchaseIgstCol2.visibility = View.GONE
                }

                if (!isTdsEnable && !isTcsEnable) {
                    // getLedgerdd("tcs")
                    binding.radiogroupTDSTCSNewpurchase.visibility = View.GONE
                    binding.tvNewpurchaseTcstdsCol2.visibility = View.GONE
                    binding.tvNewpurchaseTdstcsCol1.visibility = View.GONE

                } else {
                    binding.radiogroupTDSTCSNewpurchase.visibility = View.VISIBLE
                    binding.tvNewpurchaseTcstdsCol2.visibility = View.VISIBLE
                    binding.tvNewpurchaseTdstcsCol1.visibility = View.VISIBLE
                }
                when (isTcsEnable) {
                    true -> {
                        if (isTdsEnable == false) {
                            binding.radioTCSNewpurchase.isChecked = true
                        }
                        binding.radioTCSNewpurchase.visibility = View.VISIBLE
                        binding.tvNewpurchaseTcstdsCol2.setText(calculatePurchaseModel.data!!.tcs_amount)
                        binding.radioTCSNewpurchase.isEnabled = true
                    }
                    false -> {
                        binding.radioTCSNewpurchase.visibility = View.GONE
                        binding.radioTCSNewpurchase.isEnabled = false
                    }
                }
                when (isTdsEnable) {
                    true -> {
                        if (isTcsEnable == false) {
                            binding.radioTDSNewpurchase.isChecked = true
                        }
                        binding.radioTDSNewpurchase.visibility = View.VISIBLE
                        binding.tvNewpurchaseTcstdsCol2.setText("-" + calculatePurchaseModel.data!!.tds_amount)
                        binding.radioTDSNewpurchase.isEnabled = true
                    }
                    false -> {
                        binding.radioTCSNewpurchase.visibility = View.GONE
                        binding.radioTDSNewpurchase.isEnabled = false
                    }
                }

                when (tds_tcs_enable) {
                    "tcs" -> {
                        binding.tvNewpurchaseTcstdsCol2.setText(calculatePurchaseModel.data!!.tcs_amount)
                    }
                    "tds" -> {
                        binding.tvNewpurchaseTcstdsCol2.setText("-" + calculatePurchaseModel.data!!.tds_amount)
                    }
                }

            }
            "0" -> {
                binding.tvNewpurchaseSgstCol0.visibility = View.GONE
                binding.tvNewpurchaseSgstCol1.visibility = View.GONE
                binding.tvNewpurchaseSgstCol2.visibility = View.GONE
                isSgstEnable = false
                binding.tvNewpurchaseCgstCol0.visibility = View.GONE
                binding.tvNewpurchaseCgstCol1.visibility = View.GONE
                binding.tvNewpurchaseCgstCol2.visibility = View.GONE
                isCgstEnable = false
                binding.tvNewpurchaseIgstCol0.visibility = View.GONE
                binding.tvNewpurchaseIgstCol1.visibility = View.GONE
                binding.tvNewpurchaseIgstCol2.visibility = View.GONE
                isIgstEnable = false
                binding.radiogroupTDSTCSNewpurchase.visibility = View.GONE
                binding.tvNewpurchaseTcstdsCol2.visibility = View.GONE
                binding.tvNewpurchaseTdstcsCol1.visibility = View.GONE

            }
        }

        if (calculatePurchaseModel.data?.is_show_round_off.equals("1")) {
            isRoundOffEnable = true
            binding.tvNewpurchaseRoundOffCol0.visibility = View.VISIBLE
            binding.tvNewpurchaseRoundoffCol1.visibility = View.VISIBLE
            binding.tvNewpurchaseRoundoffCol2.visibility = View.VISIBLE
            getLedgerdd("round_off")
            roundOffUpdatedValue = calculatePurchaseModel.data!!.round_off_total.toString()
            binding.tvNewpurchaseRoundoffCol2.setText(calculatePurchaseModel.data!!.round_off_total)
        }else{
            binding.tvNewpurchaseRoundOffCol0.visibility = View.GONE
            binding.tvNewpurchaseRoundoffCol1.visibility = View.GONE
            binding.tvNewpurchaseRoundoffCol2.visibility = View.GONE
        }
     /*   when (!calculatePurchaseModel.data?.round_off_total.equals("0.00")) {
            true -> {
                isRoundOffEnable = true
                binding.tvNewpurchaseRoundOffCol0.visibility = View.VISIBLE
                binding.tvNewpurchaseRoundoffCol1.visibility = View.VISIBLE
                binding.tvNewpurchaseRoundoffCol2.visibility = View.VISIBLE
                getLedgerdd("round_off")
                roundOffUpdatedValue = calculatePurchaseModel.data!!.round_off_total.toString()
                binding.tvNewpurchaseRoundoffCol2.setText(calculatePurchaseModel.data!!.round_off_total)
            }
            false -> {
                binding.tvNewpurchaseRoundOffCol0.visibility = View.GONE
                binding.tvNewpurchaseRoundoffCol1.visibility = View.GONE
                binding.tvNewpurchaseRoundoffCol2.visibility = View.GONE
            }
        }*/

        v1.visibility = View.VISIBLE
        v2.visibility = View.VISIBLE
        /*v3.visibility = View.VISIBLE
        v4.visibility = View.VISIBLE
        v33.visibility = View.VISIBLE
        v44.visibility = View.VISIBLE
        v5.visibility = View.VISIBLE
        v6.visibility = View.VISIBLE*/



        binding.linearCalculationView.visibility = View.VISIBLE

        binding.llClosingbalNewpurchase.visibility = View.VISIBLE
        updateClosingFineClosingCash(calculatePurchaseModel)
        updateOpeningFineOpeningCash(calculatePurchaseModel)


    }

    private fun updateUIofTotalDue(calculatePurchaseModel: CalculateSalesModel) {

        if (!calculatePurchaseModel.data?.total_fine_wt_with_IRT.equals("0.000") &&
            calculatePurchaseModel.data?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewpurchaseSilverTotaldue.visibility = View.GONE
            binding.tvNewpurchaseTotaldueGoldLabel.visibility = View.VISIBLE
            binding.tvNewpurchaseTotalDueGold.visibility = View.VISIBLE
        }

        if (!calculatePurchaseModel.data?.total_fine_wt_with_IRT.equals("0.000") &&
            !calculatePurchaseModel.data?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewpurchaseSilverTotaldue.visibility = View.VISIBLE
            binding.tvNewpurchaseTotaldueGoldLabel.visibility = View.VISIBLE
            binding.tvNewpurchaseTotalDueGold.visibility = View.VISIBLE
        }



        if (calculatePurchaseModel.data?.total_fine_wt_with_IRT.equals("0.000") &&
            !calculatePurchaseModel.data?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewpurchaseSilverTotaldue.visibility = View.GONE
            binding.tvNewpurchaseTotaldueGoldLabel.setText("S: ")
            binding.tvNewpurchaseTotalDueGold.setText(calculatePurchaseModel.data?.total_silver_fine_wt_with_IRT)
            when (binding.tvNewpurchaseTotalDueGold.text) {
                "0.000" -> {
                    binding.tvNewpurchaseTotalDueGold.text =
                        calculatePurchaseModel.data?.total_silver_fine_wt_with_IRT
                    binding.tvNewpurchaseTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.header_black_text
                        )
                    )
                }

                else -> {
                    binding.tvNewpurchaseTotalDueGold.text =
                        calculatePurchaseModel.data?.total_silver_fine_wt_with_IRT + " " +
                                calculatePurchaseModel.data?.total_silver_fine_wt_with_IRT_term
                    if (calculatePurchaseModel.data?.total_silver_fine_wt_with_IRT_short_term.equals(
                            "Dr",
                            ignoreCase = true
                        )
                    ) {
                        binding.tvNewpurchaseTotalDueGold.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.debit_color
                            )
                        )
                    } else
                        binding.tvNewpurchaseTotalDueGold.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.credit_color
                            )
                        )
                }
            }

            when (binding.tvNewpurchaseTotalDueCash.text) {
                "0.00" -> {
                    binding.tvNewpurchaseTotalDueCash.text =
                        calculatePurchaseModel.data?.grand_total
                    binding.tvNewpurchaseTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.header_black_text
                        )
                    )
                }
                else -> {
                    binding.tvNewpurchaseTotalDueCash.text =
                        calculatePurchaseModel.data?.grand_total + " " +
                                calculatePurchaseModel.data?.grand_total_term
                    if (calculatePurchaseModel.data?.grand_total_short_term.equals(
                            "Dr",
                            ignoreCase = true
                        )
                    ) {
                        binding.tvNewpurchaseTotalDueCash.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.debit_color
                            )
                        )
                    } else
                        binding.tvNewpurchaseTotalDueCash.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.credit_color
                            )
                        )
                }
            }

        } else {
            binding.tvNewpurchaseTotaldueGoldLabel.setText("G: ")
            updateTotalDuewithDrCr(calculatePurchaseModel)
        }

        /*if(!calculatePurchaseModel.data?.total_silver_fine_wt_with_IRT.equals("0.000")){
            binding.llNewinvoiceSilverTotaldue.visibility = View.VISIBLE
        }else{
            binding.llNewinvoiceSilverTotaldue.visibility = View.GONE
        }*/


        if (calculatePurchaseModel.data?.total_fine_wt_with_IRT.equals("0.000") &&
            calculatePurchaseModel.data?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewpurchaseSilverTotaldue.visibility = View.GONE
            binding.tvNewpurchaseTotaldueGoldLabel.visibility = View.GONE
            binding.tvNewpurchaseTotalDueGold.visibility = View.GONE
        } else {
            //  binding.llNewinvoiceSilverTotaldue.visibility = View.VISIBLE
            //  tv_newinvoice_totaldue_gold_label.visibility = View.VISIBLE
            // tv_newinvoice_totalDue_gold.visibility = View.VISIBLE
        }
    }

    private fun updateTotalDuewithDrCr(calculatePurchaseModel: CalculateSalesModel) {
        when (binding.tvNewpurchaseTotalDueGold.text) {
            "0.000" -> {
                binding.tvNewpurchaseTotalDueGold.text =
                    calculatePurchaseModel.data?.total_fine_wt_with_IRT
                binding.tvNewpurchaseTotalDueGold.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvNewpurchaseTotalDueGold.text =
                    calculatePurchaseModel.data?.total_fine_wt_with_IRT + " " +
                            calculatePurchaseModel.data?.total_fine_wt_with_IRT_term
                if (calculatePurchaseModel.data?.total_fine_wt_with_IRT_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewpurchaseTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewpurchaseTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        when (binding.tvNewpurchaseTotalDueSilver.text) {
            "0.000" -> {
                binding.tvNewpurchaseTotalDueSilver.text =
                    calculatePurchaseModel.data?.total_silver_fine_wt_with_IRT
                binding.tvNewpurchaseTotalDueSilver.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvNewpurchaseTotalDueSilver.text =
                    calculatePurchaseModel.data?.total_silver_fine_wt_with_IRT + " " +
                            calculatePurchaseModel.data?.total_silver_fine_wt_with_IRT_term
                if (calculatePurchaseModel.data?.total_silver_fine_wt_with_IRT_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewpurchaseTotalDueSilver.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewpurchaseTotalDueSilver.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        when (binding.tvNewpurchaseTotalDueCash.text) {
            "0.00" -> {
                binding.tvNewpurchaseTotalDueCash.text =
                    calculatePurchaseModel.data?.grand_total
                binding.tvNewpurchaseTotalDueCash.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvNewpurchaseTotalDueCash.text =
                    calculatePurchaseModel.data?.grand_total + " " +
                            calculatePurchaseModel.data?.grand_total_term
                if (calculatePurchaseModel.data?.grand_total_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewpurchaseTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewpurchaseTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }
    }

    private fun addTaxDatainPref() {
        addTaxList.clear()
        for (i in 0 until additemList.size) {
            if (!additemList.get(i).tax_analysis_array!!.item_name.equals("")) {

                taxAnalysisModel = TaxAnalysisListModel.TaxAnalysisList(
                    additemList.get(i).tax_analysis_array!!.item_id,
                    additemList.get(i).tax_analysis_array!!.item_name,
                    additemList.get(i).tax_analysis_array!!.ledger_id,
                    additemList.get(i).tax_analysis_array!!.ledger_name,
                    additemList.get(i).tax_analysis_array!!.taxable_amount,
                    additemList.get(i).tax_analysis_array!!.hsn,
                    additemList.get(i).tax_analysis_array!!.gst_rate,
                    additemList.get(i).tax_analysis_array!!.gst_rate_percentage,
                    additemList.get(i).tax_analysis_array!!.igst_amount,
                    additemList.get(i).tax_analysis_array!!.cgst_amount,
                    additemList.get(i).tax_analysis_array!!.sgst_amount

                )

                addTaxList.add(taxAnalysisModel)
            }
        }
        prefs[Constants.PREF_SALES_TAX_ANALYSIS_LIST_KEY] = Gson().toJson(addTaxList)
    }

    private fun updateClosingFineClosingCash(calculatePurchaseModel: CalculateSalesModel) {
        if (calculatePurchaseModel.data?.closing_fine_balance!!.startsWith("-")) {
            val open_fine_bal: String =
                calculatePurchaseModel.data?.closing_fine_balance.toString().trim().substring(1)
            binding.tvCloBalFineWtNewPurchase.text = open_fine_bal
        } else {
            binding.tvCloBalFineWtNewPurchase.text =
                calculatePurchaseModel.data?.closing_fine_balance
        }

        when (binding.tvCloBalFineWtNewPurchase.text) {
            "0.000" -> {
                binding.tvCloBalFineWtNewPurchase.text =
                    calculatePurchaseModel.data?.closing_fine_balance
                binding.tvCloBalFineWtNewPurchase.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvCloBalFineWtNewPurchase.text =
                    calculatePurchaseModel.data?.closing_fine_balance + " " + calculatePurchaseModel.data?.closing_fine_balance_term
                if (calculatePurchaseModel.data?.closing_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvCloBalFineWtNewPurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvCloBalFineWtNewPurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        if (calculatePurchaseModel.data?.closing_silver_fine_balance!!.startsWith("-")) {
            val open_fine_bal: String =
                calculatePurchaseModel.data?.closing_silver_fine_balance.toString().trim()
                    .substring(1)
            binding.tvCloBalSilverNewPurchase.text = open_fine_bal
        } else {
            binding.tvCloBalSilverNewPurchase.text =
                calculatePurchaseModel.data?.closing_silver_fine_balance
        }

        when (binding.tvCloBalSilverNewPurchase.text) {
            "0.000" -> {
                binding.tvCloBalSilverNewPurchase.text =
                    calculatePurchaseModel.data?.closing_silver_fine_balance
                binding.tvCloBalSilverNewPurchase.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvCloBalSilverNewPurchase.text =
                    calculatePurchaseModel.data?.closing_silver_fine_balance + " " + calculatePurchaseModel.data?.closing_silver_fine_balance_term
                if (calculatePurchaseModel.data?.closing_silver_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvCloBalSilverNewPurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvCloBalSilverNewPurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        if (calculatePurchaseModel.data?.closing_cash_balance!!.startsWith("-")) {
            val open_cash_bal: String =
                calculatePurchaseModel.data?.closing_cash_balance.toString().trim().substring(1)
            binding.tvCloBalCashNewPurchase.text = open_cash_bal
        } else {
            binding.tvCloBalCashNewPurchase.text =
                calculatePurchaseModel.data?.closing_cash_balance
        }

        when (binding.tvCloBalCashNewPurchase.text) {
            "0.00" -> {
                binding.tvCloBalCashNewPurchase.text =
                    calculatePurchaseModel.data?.closing_cash_balance
                binding.tvCloBalCashNewPurchase.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvCloBalCashNewPurchase.text =
                    calculatePurchaseModel.data?.closing_cash_balance + " " + calculatePurchaseModel.data?.closing_cash_balance_term
                if (calculatePurchaseModel.data?.closing_cash_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvCloBalCashNewPurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvCloBalCashNewPurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }


        }

    }

    private fun updateOpeningFineOpeningCash(calculatePurchaseModel: CalculateSalesModel) {
        if (calculatePurchaseModel.data?.opening_fine_balance!!.startsWith("-")) {
            val open_fine_bal: String =
                calculatePurchaseModel.data?.opening_fine_balance.toString().trim().substring(1)
            lblopenFineGoldNewPurchase.visibility = View.VISIBLE
            binding.tvOpenBalFineNewPurchase.text = open_fine_bal
        } else {
            lblopenFineGoldNewPurchase.visibility = View.VISIBLE
            binding.tvOpenBalFineNewPurchase.text =
                calculatePurchaseModel.data?.opening_fine_balance
        }


        when (binding.tvOpenBalFineNewPurchase.text) {
            "0.000" -> {
                binding.tvOpenBalFineNewPurchase.text =
                    calculatePurchaseModel.data?.opening_fine_balance
                binding.tvOpenBalFineNewPurchase.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvOpenBalFineNewPurchase.text =
                    calculatePurchaseModel.data?.opening_fine_balance + " " + calculatePurchaseModel.data?.opening_fine_balance_term

                if (calculatePurchaseModel.data?.opening_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvOpenBalFineNewPurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvOpenBalFineNewPurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        if (calculatePurchaseModel.data?.opening_silver_fine_balance!!.startsWith("-")) {
            val open_fine_bal: String =
                calculatePurchaseModel.data?.opening_silver_fine_balance.toString().trim()
                    .substring(1)
            lblopenFineSilverNewPurchase.visibility = View.VISIBLE
            binding.tvOpenBalFineSilverNewPurchase.text = open_fine_bal
        } else {
            lblopenFineSilverNewPurchase.visibility = View.VISIBLE
            binding.tvOpenBalFineSilverNewPurchase.text =
                calculatePurchaseModel.data?.opening_silver_fine_balance
        }


        when (binding.tvOpenBalFineSilverNewPurchase.text) {
            "0.000" -> {
                binding.tvOpenBalFineSilverNewPurchase.text =
                    calculatePurchaseModel.data?.opening_silver_fine_balance
                binding.tvOpenBalFineSilverNewPurchase.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvOpenBalFineSilverNewPurchase.text =
                    calculatePurchaseModel.data?.opening_silver_fine_balance + " " +
                            calculatePurchaseModel.data?.opening_silver_fine_balance_term

                if (calculatePurchaseModel.data?.opening_silver_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvOpenBalFineSilverNewPurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvOpenBalFineSilverNewPurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        if (calculatePurchaseModel.data?.opening_cash_balance!!.startsWith("-")) {
            val open_cash_bal: String =
                calculatePurchaseModel.data?.opening_cash_balance.toString().trim().substring(1)
            lblopenCashNewPurchase.visibility = View.VISIBLE
            binding.tvOpenBalCashNewPurchase.text = open_cash_bal
        } else {
            lblopenCashNewPurchase.visibility = View.VISIBLE
            binding.tvOpenBalCashNewPurchase.text =
                calculatePurchaseModel.data?.opening_cash_balance
        }

        when (binding.tvOpenBalCashNewPurchase.text) {
            "0.00" -> {
                binding.tvOpenBalCashNewPurchase.text =
                    calculatePurchaseModel.data?.opening_cash_balance
                binding.tvOpenBalCashNewPurchase.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvOpenBalCashNewPurchase.text =
                    calculatePurchaseModel.data?.opening_cash_balance + " " + calculatePurchaseModel.data?.opening_cash_balance_term
                if (calculatePurchaseModel.data?.opening_cash_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvOpenBalCashNewPurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvOpenBalCashNewPurchase.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }


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
                                    // checkBranchType(false)
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
        /*for (i in 0 until data.fields!!.size) {
            if (data.fields!!.get(i).startsWith(getString(R.string.customers))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_fine_bal), true)) {
                    true -> {

                    }
                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.customers))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_opening_bal), true)) {
                    true -> {

                    }

                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.customers))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_fine_limit), true)) {
                    true -> {

                    }

                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.customers))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_cash_limit), true)) {
                    true -> {

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


                    }
                    false->{
                        //radioTaxNewCust.isChecked = false
                        radioTaxExeNewCust.isChecked = true

                    }

                }
            }
            if (data.fields!!.get(i).startsWith(getString(R.string.customers))) {
                // Restriction check for Ledger
                when (data.fields!!.get(i)
                    .endsWith(getString(R.string.permission_pan_card), true)) {
                    true -> {

                    }

                }
            }

        }*/
    }

}