package com.goldbookapp.ui.activity.sales

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
import com.goldbookapp.databinding.NewInvoiceActivityBinding
import com.goldbookapp.model.*
import com.goldbookapp.searchablespinner.SearchableSpinner
import com.goldbookapp.ui.activity.additem.AddItemActivity
import com.goldbookapp.ui.activity.settings.TaxAnalysisDetailsActivity
import com.goldbookapp.ui.activity.viewmodel.NewInvoiceViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.IssueReceiveAdapter
import com.goldbookapp.ui.adapter.NewInvoiceItemAdapter
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
import kotlinx.android.synthetic.main.add_cashbank_activity.*
import kotlinx.android.synthetic.main.add_item_activity.*
import kotlinx.android.synthetic.main.bottom_sheet_dialog.view.*
import kotlinx.android.synthetic.main.new_invoice_activity.*
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


class NewInvoiceActivity : AppCompatActivity() {
    lateinit var childItemModel: SalesLineModel.SaleLineModelDetails
    private lateinit var issueReceiveadapter: IssueReceiveAdapter
    var isNoGenerated: Boolean = false
    var isRateCutSaved: Boolean = false
    lateinit var dialog: Dialog
    private lateinit var viewModel: NewInvoiceViewModel
    lateinit var binding: NewInvoiceActivityBinding
    private lateinit var adapter: NewInvoiceItemAdapter
    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    var isPhotoSelected: Boolean = false
    var multipartImageBody: MultipartBody.Part? = null
    var selectedSgstId: String? = ""
    var selectedCgstId: String? = ""
    var selectedIgstId: String? = ""
    var selectedTcsId: String? = ""
    var selectedTdsId: String? = ""
    var selectedRoundoffId: String? = ""

    var isSgstEnable: Boolean = false
    var isCgstEnable: Boolean = false
    var isIgstEnable: Boolean = false
    var isTdsEnable: Boolean = false
    var isTcsEnable: Boolean = false
    var isRoundOffEnable: Boolean = false

    var tds_tcs_enable: String = "0"

    var contactList: List<SearchContactModel.Data.Contact>? = null

    var contactNameList: List<String>? = null

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

    var itemListFormDetail = ArrayList<SaleDetailModel.Item1427117511>()
    var additemList = ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>()
    var addTaxList = ArrayList<TaxAnalysisListModel.TaxAnalysisList>()
    lateinit var taxAnalysisModel: TaxAnalysisListModel.TaxAnalysisList
    var salesLineList = ArrayList<SalesLineModel.SaleLineModelDetails>()

    var selectedContactID: String? = null
    var selectedCustomerCode: String? = null
    var selectedPlaceOfSupply: String? = null
    var selectedPlaceOfSupplyID: String? = null

    var is_gst_applicable: String? = "0"
    var tds_percentage: String? = "0"
    var tcs_percentage: String? = "0"

    var is_prefix: String? = ""
    var is_series: String? = ""
    var is_suffix: String? = ""


    lateinit var goldrateUpdatedValue: String
    lateinit var ratecutWtUpdateValue: String
    val df = DecimalFormat("0.00")
    val df1 = DecimalFormat("0.000")

    lateinit var calculateSalesModelMain: CalculateSalesModel
    lateinit var salesDetailsModel: SaleDetailModel.Data
    var transaction_id: String = ""
    var is_From_Edit: Boolean = false

    var ratecut_type: String = ""
    var selectedRadioTcsTds: String = "tcs"
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
        binding = DataBindingUtil.setContentView(this, R.layout.new_invoice_activity)

        setupViewModel()
        setupUIandListner()

    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                NewInvoiceViewModel::class.java
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

        prefs.edit().remove(Constants.PREF_ADD_ITEM_KEY).apply()
        df.roundingMode = RoundingMode.CEILING
        df1.roundingMode = RoundingMode.CEILING

        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.new_invoice)

        clearPref()
        when (is_From_Edit) {
            false -> {

                autogenerateinvoice(true)
            }
            else -> {

            }
        }

        getSearchContact()
        //getInvoiceNumber()


        // issue receive adapter
        debit_short_term = loginModel?.data!!.company_info!!.general_settings!!.debit_short_term!!
        credit_short_term = loginModel?.data!!.company_info!!.general_settings!!.credit_short_term!!
        binding.rvIssueReceiveList.layoutManager = LinearLayoutManager(this)
        issueReceiveadapter =
            IssueReceiveAdapter(arrayListOf(), "sales", false, debit_short_term, credit_short_term)
        binding.rvIssueReceiveList.adapter = issueReceiveadapter

        // item adapter
        rv_newinvoice_item.layoutManager = LinearLayoutManager(this)
        adapter = NewInvoiceItemAdapter(arrayListOf(), true, selectedPlaceOfSupplyID.toString())
        rv_newinvoice_item.adapter = adapter
        if (loginModel.data?.branch_info?.branch_type.equals("1", true)) { // GST branch
            checkInGstNewnvoice.visibility = View.VISIBLE
            checkInGstNewnvoice.isEnabled = false
            binding.tvAddSaleTaxAnalysis.visibility = View.VISIBLE

            is_gst_applicable = "1"
            tds_tcs_enable = "tcs"
            getLedgerdd("tcs")
            checkInGstNewnvoice.isChecked = true
            tvSupplyNewInvoice.visibility = View.VISIBLE
            tvAddSaleLine.setText("Add Cash / Bank")

        } else { // NON-GST branch
            checkInGstNewnvoice.visibility = View.GONE
            is_gst_applicable = "0"
            tvAddSaleLine.setText("Add Cash / Bank / Rate-Cut / Metal Receipt / Metal Payment")
            binding.tvAddSaleTaxAnalysis.visibility = View.GONE
        }


        getDataFromIntent()
        onTextChanged()
        onFocusChanged()

        imgLeft?.clickWithDebounce {
            onBackPressed()
        }


        tv_newinvoice_uploadphoto?.clickWithDebounce {
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
                )*/.maxResultSize(
                    1080,
                    1080
                )    //Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        iv_newinvoice_attachment?.clickWithDebounce {

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

        cardAddItemNewInvoice?.clickWithDebounce {
            if (selectedContactID != null && !selectedContactID!!.isBlank()) {
                binding.txtCustNameNewInvoice.clearFocus()
                binding.txtRemarkNewInvoice.clearFocus()
                startActivity(
                    Intent(this, AddItemActivity::class.java)
                        .putExtra(Constants.TRANSACTION_TYPE, "sales")
                        .putExtra(Constants.CUST_STATE_ID, selectedPlaceOfSupplyID)
                )
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.select_contact_first_msg),
                    Toast.LENGTH_LONG
                ).show()
                txtCustNameNewInvoice.requestFocus()
            }
        }


        // gst branch
        checkInGstNewnvoice.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                tvSupplyNewInvoice.visibility = View.VISIBLE
                is_gst_applicable = "1";
                invoiceCalculation()
            } else {
                tvSupplyNewInvoice.visibility = View.GONE
                is_gst_applicable = "0";
                invoiceCalculation()
            }
        }

        btnSave_AddSale?.clickWithDebounce {

            if (performValidation()) {
                if (NetworkUtils.isConnected()) {
                    when (is_From_Edit) {
                        true -> {
                            editNewInvoiceRequestBodyParamCallAPI()
                        }
                        false -> {
                            addNewInvoiceRequestBodyParamCallAPI()
                        }
                    }

                } else {
                    CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))
                }
            }
        }


        txtCustNameNewInvoice.doAfterTextChanged { selectedContactID = "" }

        //setupDialogUI()
        cardAddSaleLine.clickWithDebounce {
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
                txtCustNameNewInvoice.requestFocus()
            }

        }

        tvAddSaleTaxAnalysis.clickWithDebounce {
            startActivity(
                Intent(this, TaxAnalysisDetailsActivity::class.java)
                    .putExtra(Constants.TRANSACTION_TYPE, "sales")
            )
        }

        binding.radiogroupTDSTCSNewInvoice.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                binding.radioTCSNewInvoice.id -> {
                    tds_tcs_enable = "tcs"
                    getLedgerdd("tcs")
                    invoiceCalculation()
                    binding.tvNewinvoiceTdstcsCol1.mLabelView!!.hint = "TCS Ledger"

                }
                binding.radioTDSNewInvoice.id -> {
                    binding.tvNewinvoiceTdstcsCol1.mLabelView!!.hint = "TDS Ledger"
                    tds_tcs_enable = "tds"
                    getLedgerdd("tds")
                    invoiceCalculation()
                }
            }
        })

    }

    private fun onFocusChanged() {
        binding.tvNewinvoiceRoundoffCol2.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                when (binding.tvNewinvoiceRoundoffCol2.text.isNullOrBlank()) {
                    true -> {
                        roundOffUpdatedValue = "0.00"
                        binding.tvNewinvoiceRoundoffCol2.setText(roundOffUpdatedValue)
                        binding.tvNewinvoiceRoundoffCol2.setSelection(roundOffUpdatedValue.length)
                    }
                    else -> {
                        when (!roundOffUpdatedValue.toBigDecimal()
                            .equals("0.00")) {
                            true -> {
                                binding.tvNewinvoiceRoundoffCol2.setText(roundOffUpdatedValue)
                                binding.tvNewinvoiceRoundoffCol2.setSelection(roundOffUpdatedValue.length)
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
        binding.tvNewinvoiceRoundoffCol2.doAfterTextChanged {

            val inputValue: Float
            var str: String = "0.00"
            try {
//convert in float for negative value
                inputValue = binding.tvNewinvoiceRoundoffCol2.text.toString().toFloat()
                str = inputValue.toString()

            } catch (nfe: NumberFormatException) {
                //Error handling.
            }

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.tvNewinvoiceRoundoffCol2.setText(str2)
                binding.tvNewinvoiceRoundoffCol2.setSelection(str2.length)
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


    private fun getDataFromIntent() {
        if (intent.extras != null) {
            if (intent.extras?.containsKey(Constants.SALES_DETAIL_KEY)!!) {

                var group_str: String? = intent.getStringExtra(Constants.SALES_DETAIL_KEY)
                salesDetailsModel =
                    Gson().fromJson(
                        group_str,
                        SaleDetailModel.Data::class.java
                    )
                tvTitle.setText(R.string.edit_invoice)
                is_From_Edit = true

                transaction_id = salesDetailsModel.transactionData?.transaction_id!!
                selectedCustomerCode = salesDetailsModel.transactionData?.customer_code
                selectedContactID = salesDetailsModel.transactionData?.contact_id
                selectedPlaceOfSupplyID = salesDetailsModel.transactionData?.place_of_supply_id
                binding.txtCustNameNewInvoice.setText(salesDetailsModel.transactionData?.display_name)
                binding.txtDateNewInvoice.setText(salesDetailsModel.transactionData?.transaction_date)
                binding.txtInvoiceNewInvoice.setText(salesDetailsModel.transactionData?.invoice_number)
                is_series = salesDetailsModel.transactionData?.invoice_number.toString().trim()
                binding.txtSupplyNewInvoice.setText(salesDetailsModel.transactionData?.place_of_supply.toString())

                binding.txtRemarkNewInvoice.setText(salesDetailsModel.transactionData?.remarks)
                binding.txtReffNewInvoice.setText(salesDetailsModel.transactionData?.reference)

                itemListFormDetail = salesDetailsModel.transactionData?.item!!

                /*rv_newinvoice_item.layoutManager = LinearLayoutManager(this)
                adapter = NewInvoiceItemAdapter(arrayListOf(), true, selectedPlaceOfSupplyID.toString())
                rv_newinvoice_item.adapter = adapter*/

                for (i in 0 until itemListFormDetail.size) {
                    var childModel = OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem(
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

                linear_calculation_view.visibility = View.VISIBLE

                ll_closingbal_NewInvoice.visibility = View.VISIBLE
                adapter.apply {
                    addsalebillrow_item(additemList)
                    notifyDataSetChanged()
                }

                addTaxDatainPref()

                addIRTDatainPref()
                getIssueReceiveDataFromPref()

                if (salesDetailsModel.transactionData!!.image != null && salesDetailsModel.transactionData!!.image?.size!! > 0) {
                    binding.tvNewinvoiceUploadphoto.visibility = View.GONE
                    binding.ivNewinvoiceAttachment.visibility = View.VISIBLE
                    imageURL = salesDetailsModel.transactionData!!.image?.get(0)?.image
                    Glide.with(this).load(imageURL).circleCrop()
                        .placeholder(R.drawable.ic_user_placeholder)
                        .into(binding.ivNewinvoiceAttachment)
                } else {
                    binding.tvNewinvoiceUploadphoto.visibility = View.VISIBLE
                }

                tv_newinvoice_itemquantity.setText("Qty: " + salesDetailsModel.transactionData?.total_quantity)
                tv_newinvoice_gross_wt.setText("G: " + salesDetailsModel.transactionData?.total_gross_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                tv_newinvoice_less_wt.setText("L: " + salesDetailsModel.transactionData?.total_less_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                tv_newinvoice_net_wt.setText("N: " + salesDetailsModel.transactionData?.total_net_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                tv_newinvoice_fine_wt.setText("F: " + salesDetailsModel.transactionData?.total_fine_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                //tv_newinvoice_misc_charges.setText(Constants.AMOUNT_RS_APPEND + salesDetailsModel.transactionData?.total_misc_charges)

                binding.tvNewinvoiceSilverItemquantity.setText("Qty: " + salesDetailsModel.transactionData?.silver_total_quantity)
                tv_newinvoice_silver_gross_wt.setText("G: " + salesDetailsModel.transactionData?.silver_total_gross_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                tv_newinvoice_silver_less_wt.setText("L: " + salesDetailsModel.transactionData?.silver_total_less_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                tv_newinvoice_silver_net_wt.setText("N: " + salesDetailsModel.transactionData?.silver_total_net_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                tv_newinvoice_silver_fine_wt.setText("F: " + salesDetailsModel.transactionData?.silver_total_fine_wt /*+ Constants.WEIGHT_GM_APPEND*/)

                binding.tvNewinvoiceOtherItemquantity.setText("Qty: " + salesDetailsModel.transactionData?.other_total_quantity)
                tv_newinvoice_other_gross_wt.setText("G: " + salesDetailsModel.transactionData?.other_total_gross_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                tv_newinvoice_other_less_wt.setText("L: " + "0.000" /*+ Constants.WEIGHT_GM_APPEND*/)
                tv_newinvoice_other_net_wt.setText("N: " + salesDetailsModel.transactionData?.other_total_net_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                tv_newinvoice_other_fine_wt.setText("F: " + "0.000" /*+ Constants.WEIGHT_GM_APPEND*/)

                tv_newinvoice_subtotalCol1.setText(salesDetailsModel.transactionData?.total_fine_wt)
                tv_newinvoice_subtotalCol2.setText(salesDetailsModel.transactionData?.final_total_amount)
                tv_newinvoice_subtotalCol1_Silver.setText(salesDetailsModel.transactionData?.silver_total_fine_wt)


                when (tv_newinvoice_subtotalCol1.text) {
                    "0.000" -> {
                        tv_newinvoice_subtotalCol1.setText(salesDetailsModel.transactionData?.total_fine_wt)
                        tv_newinvoice_subtotalCol1.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.header_black_text
                            )
                        )
                    }
                    else -> {
                        tv_newinvoice_subtotalCol1.setText(salesDetailsModel.transactionData?.total_fine_wt + "" + subTotalTerm)
                        if (subTotalTermValue.equals("debit")) {
                            tv_newinvoice_subtotalCol1.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.debit_color
                                )
                            )
                        }
                    }
                }

                when (tv_newinvoice_subtotalCol2.text) {
                    "0.00" -> {
                        tv_newinvoice_subtotalCol2.setText(salesDetailsModel.transactionData?.final_total_amount)
                        tv_newinvoice_subtotalCol2.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.header_black_text
                            )
                        )
                    }
                    else -> {
                        tv_newinvoice_subtotalCol2.setText(salesDetailsModel.transactionData?.final_total_amount + "" + subTotalTerm)
                        if (subTotalTermValue.equals("debit")) {
                            tv_newinvoice_subtotalCol2.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.debit_color
                                )
                            )
                        }
                    }
                }

                when (tv_newinvoice_subtotalCol1_Silver.text) {
                    "0.000" -> {
                        tv_newinvoice_subtotalCol1_Silver.setText(salesDetailsModel.transactionData?.silver_total_fine_wt)
                        tv_newinvoice_subtotalCol1_Silver.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.header_black_text
                            )
                        )
                    }
                    else -> {
                        tv_newinvoice_subtotalCol1_Silver.setText(salesDetailsModel.transactionData?.silver_total_fine_wt + "" + subTotalTerm)
                        if (subTotalTermValue.equals("debit")) {
                            tv_newinvoice_subtotalCol1_Silver.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.debit_color
                                )
                            )
                        }
                    }
                }

                tv_newinvoice_totalDue_gold.setText(salesDetailsModel.transactionData?.total_fine_wt_with_IRT)
                tv_newinvoice_totalDue_silver.setText(salesDetailsModel.transactionData?.total_silver_fine_wt_with_IRT)
                tv_newinvoice_totalDue_cash.setText(salesDetailsModel.transactionData?.grand_total)


            if (salesDetailsModel.transactionData?.is_tds_applicable.equals("1") && salesDetailsModel.transactionData?.is_tcs_applicable.equals(
                    "1"
                )
            ) {
                isTcsEnable = true
                isTdsEnable = true
                /*  binding.radiogroupTDSTCSNewInvoice.visibility = View.VISIBLE
                  binding.tvNewinvoiceTcstdsCol2.visibility = View.VISIBLE
                  binding.tvNewinvoiceTdstcsCol1.visibility = View.VISIBLE*/
            } else {
                isTcsEnable = false
                isTdsEnable = false

            }


            when (salesDetailsModel.transactionData?.is_tcs_applicable.equals("1")) {
                true -> {
                    isTcsEnable = true
                }
                false -> {
                    isTcsEnable = false
                }
            }

            when (salesDetailsModel.transactionData?.is_tds_applicable.equals("1")) {
                true -> {
                    isTdsEnable = true
                }
                false -> {
                    isTdsEnable = false
                }
            }


            if (salesDetailsModel.transactionData?.tds_tcs_enable.equals("tcs")) {
                tds_tcs_enable = "tcs"
                getLedgerdd("tcs")
                binding.radioTCSNewInvoice.isChecked = true
                binding.tvNewinvoiceTcstdsCol2.setText(salesDetailsModel.transactionData!!.tcs_amount)
                selectedTcsId = salesDetailsModel.transactionData?.tcsData!!.ledger_id
                binding.tvNewinvoiceTdstcsCol1.mLabelView!!.setText(salesDetailsModel.transactionData?.tcsData!!.ledger_name)
            }

            if (salesDetailsModel.transactionData?.tds_tcs_enable.equals("tds")) {
                tds_tcs_enable = "tds"
                getLedgerdd("tds")
                binding.radioTDSNewInvoice.isChecked = true
                binding.tvNewinvoiceTcstdsCol2.setText("-" + salesDetailsModel.transactionData!!.tds_amount)
                selectedTdsId = salesDetailsModel.transactionData?.tdsData!!.ledger_id
                binding.tvNewinvoiceTdstcsCol1.mLabelView!!.setText(salesDetailsModel.transactionData?.tdsData!!.ledger_name)
            }

            if (salesDetailsModel.transactionData?.tds_tcs_enable.equals("")) {
                tds_tcs_enable = ""
            }


            when (salesDetailsModel.transactionData?.is_gst_applicable.toString()
                .contains("1")) {
                true -> {
                    if ((salesDetailsModel.transactionData?.sgst_amount!!.toBigDecimal() > BigDecimal.ZERO && !salesDetailsModel.transactionData?.sgst_amount!!.isBlank()) &&
                        (salesDetailsModel.transactionData?.place_of_supply_id.equals(loginModel.data!!.branch_info!!.state_id.toString()))
                    ) {
                        binding.tvNewinvoiceSgstCol0.visibility = View.VISIBLE
                        binding.tvNewinvoiceSgstCol1.visibility = View.VISIBLE
                        binding.tvNewinvoiceSgstCol2.visibility = View.VISIBLE
                        isSgstEnable = true
                        getLedgerdd("sgst")
                        selectedSgstId = salesDetailsModel.transactionData?.sgstData?.ledger_id
                        binding.tvNewinvoiceSgstCol1.mLabelView!!.setText(salesDetailsModel.transactionData?.sgstData?.ledger_name)

                        binding.tvNewinvoiceSgstCol2.setText(Constants.AMOUNT_RS_APPEND + salesDetailsModel.transactionData?.sgst_amount)
                    } else {
                        binding.tvNewinvoiceSgstCol0.visibility = View.GONE
                        binding.tvNewinvoiceSgstCol1.visibility = View.GONE
                        binding.tvNewinvoiceSgstCol2.visibility = View.GONE
                    }

                    if (
                        (salesDetailsModel.transactionData?.cgst_amount!!.toBigDecimal() > BigDecimal.ZERO && !salesDetailsModel.transactionData?.cgst_amount!!.isBlank()) &&
                        (salesDetailsModel.transactionData?.place_of_supply_id.equals(loginModel.data!!.branch_info!!.state_id.toString()))
                    ) {
                        binding.tvNewinvoiceCgstCol0.visibility = View.VISIBLE
                        binding.tvNewinvoiceCgstCol1.visibility = View.VISIBLE
                        binding.tvNewinvoiceCgstCol2.visibility = View.VISIBLE
                        isCgstEnable = true
                        getLedgerdd("cgst")
                        selectedCgstId = salesDetailsModel.transactionData?.cgstData?.ledger_id
                        binding.tvNewinvoiceCgstCol1.mLabelView!!.setText(salesDetailsModel.transactionData?.cgstData?.ledger_name)

                        binding.tvNewinvoiceCgstCol2.setText(Constants.AMOUNT_RS_APPEND + salesDetailsModel.transactionData?.cgst_amount)

                    } else {
                        binding.tvNewinvoiceCgstCol0.visibility = View.GONE
                        binding.tvNewinvoiceCgstCol1.visibility = View.GONE
                        binding.tvNewinvoiceCgstCol2.visibility = View.GONE
                    }

                    if ((salesDetailsModel.transactionData?.igst_amount!!.toBigDecimal() > BigDecimal.ZERO && !salesDetailsModel.transactionData?.igst_amount!!.isBlank()) &&
                        (!salesDetailsModel.transactionData?.place_of_supply_id.equals(
                            loginModel.data!!.branch_info!!.state_id.toString()
                        ))
                    ) {
                        binding.tvNewinvoiceIgstCol0.visibility = View.VISIBLE
                        binding.tvNewinvoiceIgstCol1.visibility = View.VISIBLE
                        binding.tvNewinvoiceIgstCol2.visibility = View.VISIBLE
                        isIgstEnable = true
                        getLedgerdd("igst")
                        selectedIgstId = salesDetailsModel.transactionData?.igstData?.ledger_id
                        binding.tvNewinvoiceIgstCol1.mLabelView!!.setText(salesDetailsModel.transactionData?.igstData?.ledger_name)
                        binding.tvNewinvoiceIgstCol2.setText(Constants.AMOUNT_RS_APPEND + salesDetailsModel.transactionData?.igst_amount)
                    } else {
                        binding.tvNewinvoiceIgstCol0.visibility = View.GONE
                        binding.tvNewinvoiceIgstCol1.visibility = View.GONE
                        binding.tvNewinvoiceIgstCol2.visibility = View.GONE
                    }

                }
                false -> {
                    //for non-gst invoice
                    //sgst
                    binding.tvNewinvoiceSgstCol0.visibility = View.GONE
                    binding.tvNewinvoiceSgstCol1.visibility = View.GONE
                    binding.tvNewinvoiceSgstCol2.visibility = View.GONE
                    //cgst
                    binding.tvNewinvoiceCgstCol0.visibility = View.GONE
                    binding.tvNewinvoiceCgstCol1.visibility = View.GONE
                    binding.tvNewinvoiceCgstCol2.visibility = View.GONE
                    //Igst
                    binding.tvNewinvoiceIgstCol0.visibility = View.GONE
                    binding.tvNewinvoiceIgstCol1.visibility = View.GONE
                    binding.tvNewinvoiceIgstCol2.visibility = View.GONE

                    binding.radiogroupTDSTCSNewInvoice.visibility = View.GONE
                    binding.tvNewinvoiceTcstdsCol2.visibility = View.GONE
                    binding.tvNewinvoiceTdstcsCol1.visibility = View.GONE
                }
            }


                if (salesDetailsModel.transactionData?.is_show_round_off.equals("1")) {
                    Log.d("is_show_round_off"," "+salesDetailsModel.transactionData?.is_show_round_off)
                    binding.tvNewinvoiceRoundOffCol0.visibility = View.VISIBLE
                    binding.tvNewinvoiceRoundoffCol1.visibility = View.VISIBLE
                    binding.tvNewinvoiceRoundoffCol2.visibility = View.VISIBLE
                    isRoundOffEnable = true
                    getLedgerdd("round_off")
                    binding.tvNewinvoiceRoundoffCol1.mLabelView!!.setText(
                        salesDetailsModel.transactionData?.roundOffLedgerData?.ledger_name
                    )
                    selectedRoundoffId =
                        salesDetailsModel.transactionData?.roundOffLedgerData?.ledger_id
                    roundOffUpdatedValue =
                        salesDetailsModel.transactionData?.round_off_total.toString()
                    binding.tvNewinvoiceRoundoffCol2.setText(Constants.AMOUNT_RS_APPEND + salesDetailsModel.transactionData?.round_off_total)

                } else {
                    binding.tvNewinvoiceRoundOffCol0.visibility = View.GONE
                    binding.tvNewinvoiceRoundoffCol1.visibility = View.GONE
                    binding.tvNewinvoiceRoundoffCol2.visibility = View.GONE
                }

          /*  when (!salesDetailsModel.transactionData?.round_off_total.equals("0.00")) {
                true -> {
                    binding.tvNewinvoiceRoundOffCol0.visibility = View.VISIBLE
                    binding.tvNewinvoiceRoundoffCol1.visibility = View.VISIBLE
                    binding.tvNewinvoiceRoundoffCol2.visibility = View.VISIBLE
                    isRoundOffEnable = true
                    getLedgerdd("round_off")
                    binding.tvNewinvoiceRoundoffCol1.mLabelView!!.setText(
                        salesDetailsModel.transactionData?.roundOffLedgerData?.ledger_name
                    )
                    selectedRoundoffId =
                        salesDetailsModel.transactionData?.roundOffLedgerData?.ledger_id
                    roundOffUpdatedValue =
                        salesDetailsModel.transactionData?.round_off_total.toString()
                    binding.tvNewinvoiceRoundoffCol2.setText(Constants.AMOUNT_RS_APPEND + salesDetailsModel.transactionData?.round_off_total)


                }
                false -> {
                    binding.tvNewinvoiceRoundOffCol0.visibility = View.GONE
                    binding.tvNewinvoiceRoundoffCol1.visibility = View.GONE
                    binding.tvNewinvoiceRoundoffCol2.visibility = View.GONE
                }
            }*/

        }
    }
}

private fun addIRTDatainPref() {

    salesLineList.clear()
    for (i in 0 until salesDetailsModel.IRTData!!.size) {

        if (!salesDetailsModel.IRTData!!.get(i).transaction_type.equals("")) {

            val saleIRTModel = SalesLineModel.SaleLineModelDetails(
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.cash_amount,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.cash_ledger,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.cash_ledger_name,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.cash_description,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.bank_amount,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.bank_ledger,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.bank_ledger_name,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.bank_mode,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.cheque_number,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.cheque_date,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.favouring_name,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.deuct_charges,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.deuct_charges_percentage,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.bank_final_amt,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.recipient_bank,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.account_no,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.ifs_code,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.utr_number,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.bank_description,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.rcm_gold_rate,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.rate_cut_amount,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.rate_cut_fine_term,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.metal_type_id_rate_cut,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.rate_cut_fine,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.item_id,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.item_name,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.metal_type_id_metal,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.maintain_stock_in_name_metal,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.gross_wt,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.less_wt,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.net_wt,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.touch,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.wast,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.fine_wt,
                "", "", "", "", "", "",
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.type,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.transaction_type,
                salesDetailsModel.IRTData!!.get(i).IRTDetails!!.transaction_title

            )

            salesLineList.add(saleIRTModel)
        }
    }
    prefs[Constants.PREF_SALES_LINE_INFO_KEY] = Gson().toJson(salesLineList)

}

private fun addNewInvoiceRequestBodyParamCallAPI() {

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
            salesLineList =
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
            salesLineList.add(childModel)
        }

        val issue_receive_transaction: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            Gson().toJson(salesLineList)
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
            "sales"
        )

        val transaction_date: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.txtDateNewInvoice.text.toString()
        )

        val invoice_number: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            is_series.toString().trim()
        )

        val remarks: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.txtRemarkNewInvoice.text.toString()
        )

        val referrence: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.txtReffNewInvoice.text.toString()
        )

        val customer_code: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            selectedCustomerCode
        )

        val display_name: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.txtCustNameNewInvoice.text.toString()
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

        addInvoice(
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

fun addInvoice(
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

    viewModel.addNewInvoice(
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
                            it.data?.message,
                            Toast.LENGTH_LONG
                        )
                            .show()

                        onBackPressed()

                    } else {
                        when (it.data!!.code.toString() == Constants.ErrorCode) {
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

private fun editNewInvoiceRequestBodyParamCallAPI() {
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
            salesLineList =
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
                "", "", "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", ""
            )
            salesLineList.add(childModel)
        }

        val issue_receive_transaction: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            Gson().toJson(salesLineList)
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
            binding.txtDateNewInvoice.text.toString()
        )

        val invoice_number: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            is_series.toString().trim()
        )

        val remarks: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.txtRemarkNewInvoice.text.toString()
        )

        val referrence: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.txtReffNewInvoice.text.toString()
        )

        val customer_code: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            selectedCustomerCode
        )

        val display_name: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.txtCustNameNewInvoice.text.toString()
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

        editInvoice(
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


    } else {

    }


}

fun editInvoice(
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
    image: MultipartBody.Part?,
    transaction_type: RequestBody?

) {

    viewModel.editInvoice(
        token, transaction_id,
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
                            it.data?.message,
                            Toast.LENGTH_LONG
                        )
                            .show()

                        finish()

                    } else {
                        when (it.data!!.code.toString() == Constants.ErrorCode) {
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
                        //  defaultDisableAllButtonnUI()
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
        // item adapter

        rv_newinvoice_item.layoutManager = LinearLayoutManager(this)
        adapter = NewInvoiceItemAdapter(arrayListOf(), true, selectedPlaceOfSupplyID.toString())
        rv_newinvoice_item.adapter = adapter

        when (is_From_Edit) {
            false -> {
                when (isNoGenerated) {
                    false -> getInvoiceNumber()
                    else -> {

                    }
                }
            }
            else -> {

            }
        }
        //setupUIandListner()
        // getLedgerdd("round_off")
        getIssueReceiveDataFromPref()
        invoiceCalculation()

    }

    if (!ConnectivityStateHolder.isConnected) {
        // Network is not available
        CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

    }
}

private fun defaultDisableAllButtonnUI() {

}


fun invoiceCalculation() {
    if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {

        val collectionType = object :
            TypeToken<ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>>() {}.type
        additemList =
            Gson().fromJson(
                prefs[Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY, ""],
                collectionType
            )
        Log.v("additemlist", additemList.size.toString())

        getIssueReceiveDataFromPref()

        invoiceCalculateAPI(
            true
        )

    } else {
        // called when date changed (for opening bal update according to date)
        when (!selectedContactID.isNullOrBlank() && !txtDateNewInvoice.text.toString()
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
                    rv_newinvoice_item.visibility = View.GONE
                    invoiceCalculateAPI(true)
                }
            }
        }
    }
}

fun removeIssueReceiveItem(index: Int) {
    if (isValidClickPressed()) {
        if (salesLineList != null && salesLineList.size > 0) {
            if (index >= salesLineList.size) {
                //index not exists
            } else {
                // index exists
                salesLineList.removeAt(index)


                if (salesLineList.size > 0) {
                    prefs[Constants.PREF_SALES_LINE_INFO_KEY] = Gson().toJson(salesLineList)
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
        if (salesLineList != null && salesLineList.size > 0) {
            if (index >= salesLineList.size) {
                //index not exists
            } else {
                // index exists
                startActivity(
                    Intent(this@NewInvoiceActivity, AddCashBankActivity::class.java)
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
        loginModel?.data?.bearer_access_token,
        loginModel?.data?.company_info?.id,
        "",
        "", ""
    ).observe(this, Observer {
        it?.let { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    /*Log.v("..setupObservers..", "..Success...")*/
                    if (it.data?.status == true) {
                        contactList = it.data.data?.contact

                        contactNameList = contactList?.map { it.full_name.toString() }

                        contactNameAdapter = ArrayAdapter<String>(
                            this,
                            android.R.layout.simple_dropdown_item_1line,
                            contactNameList!!
                        )
                        binding.txtCustNameNewInvoice.setAdapter(contactNameAdapter)
                        binding.txtCustNameNewInvoice.threshold = 1

                        binding.txtCustNameNewInvoice.setOnItemClickListener { adapterView, view, position, l
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

                            txtSupplyNewInvoice.setText(selectedPlaceOfSupply)
                            when (pos?.let { it1 -> contactList?.get(it1)?.is_tcs_applicable }!!
                                .equals("1", true)) {
                                true -> {
                                    isTcsEnable = true
                                    //   tds_tcs_enable = "tcs"
                                }
                                else -> {

                                }
                            }
                            when (pos?.let { it1 -> contactList?.get(it1)?.is_tds_applicable }!!
                                .equals("1", true)) {
                                true -> {
                                    isTdsEnable = true
                                    // tds_tcs_enable = "tds"
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
                            subTotalTerm = fineDefaultTermList!!.get(0).default_short_term!!
                            subTotalTermValue =
                                fineDefaultTermList!!.get(0).default_term_value!!

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

private fun autogenerateinvoice(isFromOnCreate: Boolean) {
    when (isFromOnCreate) {
        true -> {
            txtDateNewInvoice.setText(SimpleDateFormat("dd-MMM-yy").format(Date()))
        }
        else -> {

        }
    }

    txtDateNewInvoice.clickWithDebounce {
        openDatePicker()
    }
}

fun openDatePicker() {

    val c = Calendar.getInstance()

    val sdf = SimpleDateFormat("dd-MMM-yy")
    val parse = sdf.parse(txtDateNewInvoice.text.toString())
    c.setTime(parse)


    val year = c.get(Calendar.YEAR)
    val month = c.get(Calendar.MONTH)
    val day = c.get(Calendar.DAY_OF_MONTH)


    val dpd = DatePickerDialog(
        this,
        DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

            c.set(Calendar.YEAR, year)
            c.set(Calendar.MONTH, monthOfYear)
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth)


            // Display Selected date in textbox
            txtDateNewInvoice.setText(
                "" + String.format("%02d", dayOfMonth) + "-" + SimpleDateFormat(
                    "MMM"
                ).format(c.time) + "-" + year.toString().substring(2, 4)
            )
            // txtDateNewInvoice.setText("" + String.format("%02d", dayOfMonth)   + "-" + String.format("%02d", (monthOfYear + 1)) + "-" + year.toString().substring(2,4))
            when (is_From_Edit) {
                false -> {
                    getInvoiceNumber()
                }
                else -> {

                }
            }

            invoiceCalculation()
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
    //dpd.datePicker.minDate = Date().time
    dpd.show()
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

fun performValidation(): Boolean {
    if (txtCustNameNewInvoice.text.toString().isBlank()) {
        CommonUtils.showDialog(this, "Please Select Contact Name")
        txtCustNameNewInvoice.requestFocus()
        return false
    } else if (txtInvoiceNewInvoice.text.toString().isBlank()) {
        CommonUtils.showDialog(this, "Please Enter Invoice")
        txtInvoiceNewInvoice.requestFocus()
        return false
    } else if (txtDateNewInvoice.text.toString().isBlank()) {
        CommonUtils.showDialog(this, "Please Select Date")
        txtDateNewInvoice.requestFocus()
        return false
    } else if (!prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {
        CommonUtils.showDialog(this, "Please Add an Item")
        return false
    } else if (is_gst_applicable == "1" && txtSupplyNewInvoice.text.toString().isBlank()) {
        CommonUtils.showDialog(this, "Please Enter Place of Supply")
        txtSupplyNewInvoice.requestFocus()
        return false
    } else if (is_gst_applicable == "1" && isSgstEnable == true && selectedSgstId.isNullOrBlank()) {
        CommonUtils.showDialog(this, "Please Select SGST Ledger From Drop Down")
        return false
    } else if (is_gst_applicable == "1" && isCgstEnable == true && selectedCgstId.isNullOrBlank()) {
        CommonUtils.showDialog(this, "Please Select CGST Ledger From Drop Down")
        return false
    } else if (is_gst_applicable == "1" && isIgstEnable == true && selectedIgstId.isNullOrBlank()) {
        CommonUtils.showDialog(this, "Please Select IGST Ledger From Drop Down")
        return false
    } else if (is_gst_applicable == "1" && isTcsEnable == true && tds_tcs_enable == "tcs" && selectedTcsId.isNullOrBlank()) {
        CommonUtils.showDialog(this, "Please Select TCS Ledger From Drop Down")
        return false
    } else if (is_gst_applicable == "1" && isTdsEnable == true && tds_tcs_enable == "tds" && selectedTdsId.isNullOrBlank()) {
        CommonUtils.showDialog(this, "Please Select TDS Ledger From Drop Down")
        return false
    } else if (isRoundOffEnable == true && selectedRoundoffId.isNullOrBlank()) {
        CommonUtils.showDialog(this, "Provide Round-Off Ledger")
        return false
    }

    return true
}

fun invoiceCalculateAPI(
    showLoading: Boolean
) {


    if (NetworkUtils.isConnected()) {
        //  Log.v("selectedgstid", selectedIgstId.toString())
        viewModel.getCalculateItem(
            loginModel.data?.bearer_access_token,
            "1",
            selectedContactID,
            transaction_id,
            Gson().toJson(additemList),
            Gson().toJson(salesLineList),
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
            binding.txtDateNewInvoice.text.toString().trim()
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

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == Activity.RESULT_OK) {
        //Image Uri will not be null for RESULT_OK
        val fileUri = data?.data
        //imgProfile.setImageURI(fileUri)

        tv_newinvoice_uploadphoto.visibility = View.GONE
        iv_newinvoice_attachment.visibility = View.VISIBLE
        isPhotoSelected = true
        Glide.with(this).load(fileUri).circleCrop().into(iv_newinvoice_attachment)

        //You can get File object from intent
        val imageFile: File = ImagePicker.getFile(data)!!

        val fileBody: RequestBody = RequestBody.create(
            MediaType.parse("multipart/form-data"),
            imageFile
        )
        multipartImageBody = MultipartBody.Part.createFormData(
            "image[]",
            imageFile.name,
            fileBody
        )
        //updateProfileImageAPI(loginModel?.data?.bearer_access_token, multipartBody)

        //You can also get File Path from intent
        val filePath: String = ImagePicker.getFilePath(data)!!


    } else if (resultCode == ImagePicker.RESULT_ERROR) {
        Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
    } else {

    }
}

private fun fill_item_details_data(calculateSalesModel: CalculateSalesModel) {
    calculateSalesModelMain = calculateSalesModel

    if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {
        if (additemList.size > 0) {
            adapter.apply {
                addsalebillrow_item(additemList)
                notifyDataSetChanged()
            }
            addTaxDatainPref()

        }

        /* val swipeHandler = object : SwipeToDeleteCallback(this) {
             override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                 //val position = ad
                 removeItem(viewHolder.adapterPosition)
             }
         }
         val itemTouchHelper = ItemTouchHelper(swipeHandler)
         itemTouchHelper.attachToRecyclerView(rv_newinvoice_item)*/
    }


    binding.tvNewinvoiceItemquantity.setText("Qty: " + calculateSalesModel.data?.total_quantity)
    tv_newinvoice_gross_wt.setText("G: " + calculateSalesModel.data?.total_gross_wt /*+ Constants.WEIGHT_GM_APPEND*/)
    tv_newinvoice_less_wt.setText("L: " + calculateSalesModel.data?.total_less_wt /*+ Constants.WEIGHT_GM_APPEND*/)
    tv_newinvoice_net_wt.setText("N: " + calculateSalesModel.data?.total_net_wt /*+ Constants.WEIGHT_GM_APPEND*/)
    tv_newinvoice_fine_wt.setText("F: " + calculateSalesModel.data?.total_fine_wt /*+ Constants.WEIGHT_GM_APPEND*/)
    //tv_newinvoice_misc_charges.setText(Constants.AMOUNT_RS_APPEND + calculateSalesModel.data?.total_misc_charges)
    tv_newinvoice_subtotalCol1.setText(calculateSalesModel.data?.total_fine_wt)
    tv_newinvoice_subtotalCol2.setText(calculateSalesModel.data?.final_total_amount)
    tv_newinvoice_subtotalCol1_Silver.setText(calculateSalesModel.data?.silver_total_fine_wt)



    when (tv_newinvoice_subtotalCol1.text) {
        "0.000" -> {
            tv_newinvoice_subtotalCol1.setText(calculateSalesModel.data?.total_fine_wt)
            tv_newinvoice_subtotalCol1.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.header_black_text
                )
            )
        }
        else -> {
            if (subTotalTermValue.equals("debit")) {
                tv_newinvoice_subtotalCol1.setText(calculateSalesModel.data?.total_fine_wt + " " + subTotalTerm)
                tv_newinvoice_subtotalCol1.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.debit_color
                    )
                )
            }
        }
    }

    when (tv_newinvoice_subtotalCol2.text) {
        "0.00" -> {
            tv_newinvoice_subtotalCol2.setText(calculateSalesModel.data?.final_total_amount)
            tv_newinvoice_subtotalCol2.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.header_black_text
                )
            )
        }
        else -> {
            tv_newinvoice_subtotalCol2.setText(calculateSalesModel.data?.final_total_amount + " " + subTotalTerm)
            if (subTotalTermValue.equals("debit")) {
                tv_newinvoice_subtotalCol2.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.debit_color
                    )
                )
            }
        }
    }

    when (tv_newinvoice_subtotalCol1_Silver.text) {
        "0.000" -> {
            tv_newinvoice_subtotalCol1_Silver.setText(calculateSalesModel.data?.silver_total_fine_wt)
            tv_newinvoice_subtotalCol1_Silver.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.header_black_text
                )
            )
        }
        else -> {
            tv_newinvoice_subtotalCol1_Silver.setText(calculateSalesModel.data?.silver_total_fine_wt + " " + subTotalTerm)
            if (subTotalTermValue.equals("debit")) {
                tv_newinvoice_subtotalCol1_Silver.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.debit_color
                    )
                )
            }
        }
    }




    tv_newinvoice_totalDue_gold.setText(calculateSalesModel.data?.total_fine_wt_with_IRT)
    tv_newinvoice_totalDue_silver.setText(calculateSalesModel.data?.total_silver_fine_wt_with_IRT)
    tv_newinvoice_totalDue_cash.setText(calculateSalesModel.data?.grand_total)

    //updateTotalDuewithDrCr(calculateSalesModel)

    updateUIofTotalDue(calculateSalesModel)

    if (!calculateSalesModel.data?.total_gross_wt.equals("0.000")) {
        ll_newinvoice_metalgold.visibility = View.VISIBLE
    } else {
        ll_newinvoice_metalgold.visibility = View.GONE
    }

    if (!calculateSalesModel.data?.silver_total_gross_wt.equals("0.000")) {
        ll_newinvoice_metalsilver.visibility = View.VISIBLE

    } else {
        ll_newinvoice_metalsilver.visibility = View.GONE

    }

    if (!calculateSalesModel.data?.other_total_gross_wt.equals("0.000")) {
        ll_newinvoice_metalother.visibility = View.VISIBLE


    } else {
        ll_newinvoice_metalother.visibility = View.GONE

    }

    binding.tvNewinvoiceSilverItemquantity.setText("Qty: " + calculateSalesModel.data?.silver_total_quantity)
    tv_newinvoice_silver_gross_wt.setText("G: " + calculateSalesModel.data?.silver_total_gross_wt /*+ Constants.WEIGHT_GM_APPEND*/)
    tv_newinvoice_silver_less_wt.setText("L: " + calculateSalesModel.data?.silver_total_less_wt /*+ Constants.WEIGHT_GM_APPEND*/)
    tv_newinvoice_silver_net_wt.setText("N: " + calculateSalesModel.data?.silver_total_net_wt /*+ Constants.WEIGHT_GM_APPEND*/)
    tv_newinvoice_silver_fine_wt.setText("F: " + calculateSalesModel.data?.silver_total_fine_wt /*+ Constants.WEIGHT_GM_APPEND*/)

    binding.tvNewinvoiceOtherItemquantity.setText("Qty: " + calculateSalesModel.data?.other_total_quantity)
    tv_newinvoice_other_gross_wt.setText("G: " + calculateSalesModel.data?.other_total_gross_wt /*+ Constants.WEIGHT_GM_APPEND*/)
    tv_newinvoice_other_less_wt.setText("L: " + "0.000" /*+ Constants.WEIGHT_GM_APPEND*/)
    tv_newinvoice_other_net_wt.setText("N: " + calculateSalesModel.data?.other_total_net_wt /*+ Constants.WEIGHT_GM_APPEND*/)
    tv_newinvoice_other_fine_wt.setText("F: " + "0.000" /*+ Constants.WEIGHT_GM_APPEND*/)


    when (is_gst_applicable) {
        "1" -> {
            // sgst
            if (
                (calculateSalesModel.data?.sgst_amount!!.toBigDecimal() > BigDecimal.ZERO
                        && !calculateSalesModel.data?.sgst_amount.isBlank()) &&
                (selectedPlaceOfSupplyID!!.equals(loginModel.data!!.branch_info!!.state_id.toString()))
            ) {
                binding.tvNewinvoiceSgstCol0.visibility = View.VISIBLE
                binding.tvNewinvoiceSgstCol1.visibility = View.VISIBLE
                binding.tvNewinvoiceSgstCol2.visibility = View.VISIBLE
                isSgstEnable = true
                getLedgerdd("sgst")
                binding.tvNewinvoiceSgstCol2.setText(Constants.AMOUNT_RS_APPEND + calculateSalesModel.data?.sgst_amount)

            } else {
                binding.tvNewinvoiceSgstCol0.visibility = View.GONE
                binding.tvNewinvoiceSgstCol1.visibility = View.GONE
                binding.tvNewinvoiceSgstCol2.visibility = View.GONE
            }
            // cgst
            if (
                (calculateSalesModel.data?.cgst_amount!!.toBigDecimal() > BigDecimal.ZERO
                        && !calculateSalesModel.data?.cgst_amount.isBlank())
                &&
                (selectedPlaceOfSupplyID!!.equals(loginModel.data!!.branch_info!!.state_id.toString()))
            ) {
                binding.tvNewinvoiceCgstCol0.visibility = View.VISIBLE
                binding.tvNewinvoiceCgstCol1.visibility = View.VISIBLE
                binding.tvNewinvoiceCgstCol2.visibility = View.VISIBLE

                isCgstEnable = true
                getLedgerdd("cgst")
                binding.tvNewinvoiceCgstCol2.setText(Constants.AMOUNT_RS_APPEND + calculateSalesModel.data?.cgst_amount)
            } else {
                binding.tvNewinvoiceCgstCol0.visibility = View.GONE
                binding.tvNewinvoiceCgstCol1.visibility = View.GONE
                binding.tvNewinvoiceCgstCol2.visibility = View.GONE
            }

            // igst
            if ((calculateSalesModel.data?.igst_amount!!.toBigDecimal() > BigDecimal.ZERO
                        && !calculateSalesModel.data?.igst_amount.isBlank())
                &&
                (!selectedPlaceOfSupplyID!!.equals(loginModel.data!!.branch_info!!.state_id.toString()))
            ) {
                binding.tvNewinvoiceIgstCol0.visibility = View.VISIBLE
                binding.tvNewinvoiceIgstCol1.visibility = View.VISIBLE
                binding.tvNewinvoiceIgstCol2.visibility = View.VISIBLE
                isIgstEnable = true
                getLedgerdd("igst")
                binding.tvNewinvoiceIgstCol2.setText(Constants.AMOUNT_RS_APPEND + calculateSalesModel.data?.igst_amount)
            } else {
                binding.tvNewinvoiceIgstCol0.visibility = View.GONE
                binding.tvNewinvoiceIgstCol1.visibility = View.GONE
                binding.tvNewinvoiceIgstCol2.visibility = View.GONE
            }

            if (!isTdsEnable && !isTcsEnable) {
                binding.radiogroupTDSTCSNewInvoice.visibility = View.GONE
                binding.tvNewinvoiceTcstdsCol2.visibility = View.GONE
                binding.tvNewinvoiceTdstcsCol1.visibility = View.GONE

            } else {
                binding.radiogroupTDSTCSNewInvoice.visibility = View.VISIBLE
                binding.tvNewinvoiceTcstdsCol2.visibility = View.VISIBLE
                binding.tvNewinvoiceTdstcsCol1.visibility = View.VISIBLE

            }
            // tcs / tds radio check
            when (isTdsEnable) {
                true -> {
                    if (isTcsEnable == false) {
                        binding.radioTDSNewInvoice.isChecked = true
                    }
                    binding.radioTDSNewInvoice.visibility = View.VISIBLE
                    binding.tvNewinvoiceTcstdsCol2.setText("-" + calculateSalesModel.data!!.tds_amount)
                    binding.radioTDSNewInvoice.isEnabled = true

                }
                false -> {
                    binding.radioTDSNewInvoice.visibility = View.GONE
                    binding.radioTDSNewInvoice.isEnabled = false
                }
            }
            when (isTcsEnable) {
                true -> {
                    // default tcs radio selected
                    if (isTdsEnable == false) {
                        binding.radioTCSNewInvoice.isChecked = true
                    }
                    binding.radioTCSNewInvoice.visibility = View.VISIBLE
                    binding.tvNewinvoiceTcstdsCol2.setText(calculateSalesModel.data!!.tcs_amount)
                    binding.radioTCSNewInvoice.isEnabled = true


                }
                false -> {
                    binding.radioTCSNewInvoice.visibility = View.GONE
                    binding.radioTCSNewInvoice.isEnabled = false
                }
            }



            when (tds_tcs_enable) {
                "tcs" -> {
                    binding.tvNewinvoiceTcstdsCol2.setText(calculateSalesModel.data!!.tcs_amount)
                }
                "tds" -> {
                    binding.tvNewinvoiceTcstdsCol2.setText("-" + calculateSalesModel.data!!.tds_amount)
                }
            }

        }
        "0" -> {
            binding.tvNewinvoiceSgstCol0.visibility = View.GONE
            binding.tvNewinvoiceSgstCol1.visibility = View.GONE
            binding.tvNewinvoiceSgstCol2.visibility = View.GONE
            isSgstEnable = false
            binding.tvNewinvoiceCgstCol0.visibility = View.GONE
            binding.tvNewinvoiceCgstCol1.visibility = View.GONE
            binding.tvNewinvoiceCgstCol2.visibility = View.GONE
            isCgstEnable = false
            binding.tvNewinvoiceIgstCol0.visibility = View.GONE
            binding.tvNewinvoiceIgstCol1.visibility = View.GONE
            binding.tvNewinvoiceIgstCol2.visibility = View.GONE
            isIgstEnable = false
            binding.radiogroupTDSTCSNewInvoice.visibility = View.GONE
            binding.tvNewinvoiceTcstdsCol2.visibility = View.GONE
            binding.tvNewinvoiceTdstcsCol1.visibility = View.GONE

        }
    }


    if (calculateSalesModel.data?.is_show_round_off.equals("1")) {
        Log.d("is_show_round_off"," "+calculateSalesModel.data?.is_show_round_off)
        llRoundOff.visibility = View.VISIBLE
        isRoundOffEnable = true
        binding.tvNewinvoiceRoundOffCol0.visibility = View.VISIBLE
        binding.tvNewinvoiceRoundoffCol1.visibility = View.VISIBLE
        binding.tvNewinvoiceRoundoffCol2.visibility = View.VISIBLE
        getLedgerdd("round_off")
        roundOffUpdatedValue = calculateSalesModel.data!!.round_off_total.toString()
        binding.tvNewinvoiceRoundoffCol2.setText(calculateSalesModel.data!!.round_off_total)
    } else {
        Log.d("is_show_round_off"," "+calculateSalesModel.data?.is_show_round_off)
        llRoundOff.visibility = View.GONE
        binding.tvNewinvoiceRoundOffCol0.visibility = View.GONE
        binding.tvNewinvoiceRoundoffCol1.visibility = View.GONE
        binding.tvNewinvoiceRoundoffCol2.visibility = View.GONE
    }
/*
    when (!calculateSalesModel.data?.round_off_total.equals("0.00")) {
        true -> {
            isRoundOffEnable = true
            binding.tvNewinvoiceRoundOffCol0.visibility = View.VISIBLE
            binding.tvNewinvoiceRoundoffCol1.visibility = View.VISIBLE
            binding.tvNewinvoiceRoundoffCol2.visibility = View.VISIBLE
            getLedgerdd("round_off")
            roundOffUpdatedValue = calculateSalesModel.data!!.round_off_total.toString()
            binding.tvNewinvoiceRoundoffCol2.setText(calculateSalesModel.data!!.round_off_total)
        }
        false -> {
            binding.tvNewinvoiceRoundOffCol0.visibility = View.GONE
            binding.tvNewinvoiceRoundoffCol1.visibility = View.GONE
            binding.tvNewinvoiceRoundoffCol2.visibility = View.GONE
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

    linear_calculation_view.visibility = View.VISIBLE

    ll_closingbal_NewInvoice.visibility = View.VISIBLE
    updateClosingFineClosingCash(calculateSalesModel)
    updateOpeningFineOpeningCash(calculateSalesModel)


}

private fun updateUIofTotalDue(calculateSalesModel: CalculateSalesModel) {
    if (!calculateSalesModel.data?.total_fine_wt_with_IRT.equals("0.000") &&
        calculateSalesModel.data?.total_silver_fine_wt_with_IRT.equals("0.000")
    ) {
        binding.llNewinvoiceSilverTotaldue.visibility = View.GONE
        tv_newinvoice_totaldue_gold_label.visibility = View.VISIBLE
        tv_newinvoice_totalDue_gold.visibility = View.VISIBLE
    }

    if (!calculateSalesModel.data?.total_fine_wt_with_IRT.equals("0.000") &&
        !calculateSalesModel.data?.total_silver_fine_wt_with_IRT.equals("0.000")
    ) {
        binding.llNewinvoiceSilverTotaldue.visibility = View.VISIBLE
        tv_newinvoice_totaldue_gold_label.visibility = View.VISIBLE
        tv_newinvoice_totalDue_gold.visibility = View.VISIBLE
    }



    if (calculateSalesModel.data?.total_fine_wt_with_IRT.equals("0.000") &&
        !calculateSalesModel.data?.total_silver_fine_wt_with_IRT.equals("0.000")
    ) {
        binding.llNewinvoiceSilverTotaldue.visibility = View.GONE
        tv_newinvoice_totaldue_gold_label.setText("S: ")
        tv_newinvoice_totalDue_gold.setText(calculateSalesModel.data?.total_silver_fine_wt_with_IRT)
        when (binding.tvNewinvoiceTotalDueGold.text) {
            "0.000" -> {
                binding.tvNewinvoiceTotalDueGold.text =
                    calculateSalesModel.data?.total_silver_fine_wt_with_IRT
                binding.tvNewinvoiceTotalDueGold.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvNewinvoiceTotalDueGold.text =
                    calculateSalesModel.data?.total_silver_fine_wt_with_IRT + " " +
                            calculateSalesModel.data?.total_silver_fine_wt_with_IRT_term
                if (calculateSalesModel.data?.total_silver_fine_wt_with_IRT_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewinvoiceTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewinvoiceTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        when (binding.tvNewinvoiceTotalDueCash.text) {
            "0.00" -> {
                binding.tvNewinvoiceTotalDueCash.text =
                    calculateSalesModel.data?.grand_total
                binding.tvNewinvoiceTotalDueCash.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvNewinvoiceTotalDueCash.text =
                    calculateSalesModel.data?.grand_total + " " +
                            calculateSalesModel.data?.grand_total_term
                if (calculateSalesModel.data?.grand_total_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewinvoiceTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewinvoiceTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

    } else {
        tv_newinvoice_totaldue_gold_label.setText("G: ")
        updateTotalDuewithDrCr(calculateSalesModel)
    }

    /*if(!calculateSalesModel.data?.total_silver_fine_wt_with_IRT.equals("0.000")){
        binding.llNewinvoiceSilverTotaldue.visibility = View.VISIBLE
    }else{
        binding.llNewinvoiceSilverTotaldue.visibility = View.GONE
    }*/


    if (calculateSalesModel.data?.total_fine_wt_with_IRT.equals("0.000") &&
        calculateSalesModel.data?.total_silver_fine_wt_with_IRT.equals("0.000")
    ) {
        binding.llNewinvoiceSilverTotaldue.visibility = View.GONE
        tv_newinvoice_totaldue_gold_label.visibility = View.GONE
        tv_newinvoice_totalDue_gold.visibility = View.GONE
    } else {
        //  binding.llNewinvoiceSilverTotaldue.visibility = View.VISIBLE
        //  tv_newinvoice_totaldue_gold_label.visibility = View.VISIBLE
        // tv_newinvoice_totalDue_gold.visibility = View.VISIBLE
    }

}

private fun updateTotalDuewithDrCr(calculateSalesModel: CalculateSalesModel) {
    when (binding.tvNewinvoiceTotalDueGold.text) {
        "0.000" -> {
            binding.tvNewinvoiceTotalDueGold.text =
                calculateSalesModel.data?.total_fine_wt_with_IRT
            binding.tvNewinvoiceTotalDueGold.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.header_black_text
                )
            )
        }

        else -> {
            binding.tvNewinvoiceTotalDueGold.text =
                calculateSalesModel.data?.total_fine_wt_with_IRT + " " +
                        calculateSalesModel.data?.total_fine_wt_with_IRT_term
            if (calculateSalesModel.data?.total_fine_wt_with_IRT_short_term.equals(
                    "Dr",
                    ignoreCase = true
                )
            ) {
                binding.tvNewinvoiceTotalDueGold.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.debit_color
                    )
                )
            } else
                binding.tvNewinvoiceTotalDueGold.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.credit_color
                    )
                )
        }
    }

    when (binding.tvNewinvoiceTotalDueSilver.text) {
        "0.000" -> {
            binding.tvNewinvoiceTotalDueSilver.text =
                calculateSalesModel.data?.total_silver_fine_wt_with_IRT
            binding.tvNewinvoiceTotalDueSilver.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.header_black_text
                )
            )
        }

        else -> {
            binding.tvNewinvoiceTotalDueSilver.text =
                calculateSalesModel.data?.total_silver_fine_wt_with_IRT + " " +
                        calculateSalesModel.data?.total_silver_fine_wt_with_IRT_term
            if (calculateSalesModel.data?.total_silver_fine_wt_with_IRT_short_term.equals(
                    "Dr",
                    ignoreCase = true
                )
            ) {
                binding.tvNewinvoiceTotalDueSilver.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.debit_color
                    )
                )
            } else
                binding.tvNewinvoiceTotalDueSilver.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.credit_color
                    )
                )
        }
    }

    when (binding.tvNewinvoiceTotalDueCash.text) {
        "0.00" -> {
            binding.tvNewinvoiceTotalDueCash.text =
                calculateSalesModel.data?.grand_total
            binding.tvNewinvoiceTotalDueCash.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.header_black_text
                )
            )
        }
        else -> {
            binding.tvNewinvoiceTotalDueCash.text =
                calculateSalesModel.data?.grand_total + " " +
                        calculateSalesModel.data?.grand_total_term
            if (calculateSalesModel.data?.grand_total_short_term.equals(
                    "Dr",
                    ignoreCase = true
                )
            ) {
                binding.tvNewinvoiceTotalDueCash.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.debit_color
                    )
                )
            } else
                binding.tvNewinvoiceTotalDueCash.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.credit_color
                    )
                )
        }
    }
}


private fun getIssueReceiveDataFromPref() {
    if (prefs.contains(Constants.PREF_SALES_LINE_INFO_KEY)) {
        val collectionType =
            object :
                TypeToken<ArrayList<SalesLineModel.SaleLineModelDetails>>() {}.type
        salesLineList =
            Gson().fromJson(
                prefs[Constants.PREF_SALES_LINE_INFO_KEY, ""],
                collectionType
            )
        setupIssueReceiveAdapter()

    }
}

private fun setupIssueReceiveAdapter() {
    when (salesLineList.size > 0) {
        true -> {
            binding.rvIssueReceiveList.visibility = View.VISIBLE
            issueReceiveadapter.apply {
                addissueReceiveList(salesLineList)
                notifyDataSetChanged()
            }
        }
        else -> {

        }
    }
}

private fun updateClosingFineClosingCash(calculateSalesModel: CalculateSalesModel) {
    if (calculateSalesModelMain.data?.closing_fine_balance!!.startsWith("-")) {
        val open_fine_bal: String =
            calculateSalesModelMain.data?.closing_fine_balance.toString().trim().substring(1)
        tvCloBalFineWtNewInvoice.text = open_fine_bal
    } else {
        tvCloBalFineWtNewInvoice.text = calculateSalesModelMain.data?.closing_fine_balance
    }

    when (tvCloBalFineWtNewInvoice.text) {
        "0.000" -> {
            tvCloBalFineWtNewInvoice.text = calculateSalesModelMain.data?.closing_fine_balance
            tvCloBalFineWtNewInvoice.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.header_black_text
                )
            )
        }

        else -> {
            tvCloBalFineWtNewInvoice.text =
                calculateSalesModelMain.data?.closing_fine_balance + " " + calculateSalesModelMain.data?.closing_fine_balance_term
            if (calculateSalesModelMain.data?.closing_fine_balance_short_term.equals(
                    "Dr",
                    ignoreCase = true
                )
            ) {
                tvCloBalFineWtNewInvoice.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.debit_color
                    )
                )
            } else
                tvCloBalFineWtNewInvoice.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.credit_color
                    )
                )
        }
    }

    if (calculateSalesModelMain.data?.closing_silver_fine_balance!!.startsWith("-")) {
        val open_fine_bal: String =
            calculateSalesModelMain.data?.closing_silver_fine_balance.toString().trim()
                .substring(1)
        tvCloBalSilverNewInvoice.text = open_fine_bal
    } else {
        tvCloBalSilverNewInvoice.text =
            calculateSalesModelMain.data?.closing_silver_fine_balance
    }

    when (tvCloBalSilverNewInvoice.text) {
        "0.000" -> {
            tvCloBalSilverNewInvoice.text =
                calculateSalesModelMain.data?.closing_silver_fine_balance
            tvCloBalSilverNewInvoice.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.header_black_text
                )
            )
        }

        else -> {
            tvCloBalSilverNewInvoice.text =
                calculateSalesModelMain.data?.closing_silver_fine_balance + " " + calculateSalesModelMain.data?.closing_silver_fine_balance_term
            if (calculateSalesModelMain.data?.closing_silver_fine_balance_short_term.equals(
                    "Dr",
                    ignoreCase = true
                )
            ) {
                tvCloBalSilverNewInvoice.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.debit_color
                    )
                )
            } else
                tvCloBalSilverNewInvoice.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.credit_color
                    )
                )
        }
    }

    if (calculateSalesModelMain.data?.closing_cash_balance!!.startsWith("-")) {
        val open_cash_bal: String =
            calculateSalesModelMain.data?.closing_cash_balance.toString().trim().substring(1)
        tvCloBalCashNewInvoice.text = open_cash_bal
    } else {
        tvCloBalCashNewInvoice.text = calculateSalesModelMain.data?.closing_cash_balance
    }

    when (tvCloBalCashNewInvoice.text) {
        "0.00" -> {
            tvCloBalCashNewInvoice.text = calculateSalesModelMain.data?.closing_cash_balance
            tvCloBalCashNewInvoice.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.header_black_text
                )
            )
        }
        else -> {
            tvCloBalCashNewInvoice.text =
                calculateSalesModelMain.data?.closing_cash_balance + " " + calculateSalesModelMain.data?.closing_cash_balance_term
            if (calculateSalesModelMain.data?.closing_cash_balance_short_term.equals(
                    "Dr",
                    ignoreCase = true
                )
            ) {
                tvCloBalCashNewInvoice.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.debit_color
                    )
                )
            } else
                tvCloBalCashNewInvoice.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.credit_color
                    )
                )
        }


    }

}

private fun updateOpeningFineOpeningCash(calculateSalesModel: CalculateSalesModel) {
    if (calculateSalesModelMain.data?.opening_fine_balance!!.startsWith("-")) {
        val open_fine_bal: String =
            calculateSalesModelMain.data?.opening_fine_balance.toString().trim().substring(1)
        lblopenFineGoldNewInvoice.visibility = View.VISIBLE
        tvOpenBalFineNewInvoice.text = open_fine_bal
    } else {
        lblopenFineGoldNewInvoice.visibility = View.VISIBLE
        tvOpenBalFineNewInvoice.text = calculateSalesModelMain.data?.opening_fine_balance
    }


    when (tvOpenBalFineNewInvoice.text) {
        "0.000" -> {
            lblopenFineGoldNewInvoice.visibility = View.VISIBLE
            tvOpenBalFineNewInvoice.text = calculateSalesModelMain.data?.opening_fine_balance
            tvOpenBalFineNewInvoice.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.header_black_text
                )
            )
        }

        else -> {
            lblopenFineGoldNewInvoice.visibility = View.VISIBLE
            tvOpenBalFineNewInvoice.text =
                calculateSalesModelMain.data?.opening_fine_balance + " " + calculateSalesModelMain.data?.opening_fine_balance_term
            if (calculateSalesModelMain.data?.opening_fine_balance_short_term.equals(
                    "Dr",
                    ignoreCase = true
                )
            ) {
                tvOpenBalFineNewInvoice.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.debit_color
                    )
                )
            } else
                tvOpenBalFineNewInvoice.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.credit_color
                    )
                )
        }
    }


    if (calculateSalesModelMain.data?.opening_silver_fine_balance!!.startsWith("-")) {
        val open_silver_fine_bal: String =
            calculateSalesModelMain.data?.opening_silver_fine_balance.toString().trim()
                .substring(1)
        lblopenFineSilverNewInvoice.visibility = View.VISIBLE
        tvOpenBalFineSilverNewInvoice.text = open_silver_fine_bal
    } else {
        lblopenFineSilverNewInvoice.visibility = View.VISIBLE
        tvOpenBalFineSilverNewInvoice.text =
            calculateSalesModelMain.data?.opening_silver_fine_balance
    }

    when (tvOpenBalFineSilverNewInvoice.text) {
        "0.000" -> {
            lblopenFineSilverNewInvoice.visibility = View.VISIBLE
            tvOpenBalFineSilverNewInvoice.text =
                calculateSalesModelMain.data?.opening_silver_fine_balance
            tvOpenBalFineSilverNewInvoice.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.header_black_text
                )
            )
        }
        else -> {
            lblopenFineSilverNewInvoice.visibility = View.VISIBLE
            tvOpenBalFineSilverNewInvoice.text =
                calculateSalesModelMain.data?.opening_silver_fine_balance + " " + calculateSalesModelMain.data?.opening_silver_fine_balance_term
            if (calculateSalesModelMain.data?.opening_silver_fine_balance_short_term.equals(
                    "Dr",
                    ignoreCase = true
                )
            ) {
                tvOpenBalFineSilverNewInvoice.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.debit_color
                    )
                )
            } else
                tvOpenBalFineSilverNewInvoice.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.credit_color
                    )
                )
        }


    }



    if (calculateSalesModelMain.data?.opening_cash_balance!!.startsWith("-")) {
        val open_cash_bal: String =
            calculateSalesModelMain.data?.opening_cash_balance.toString().trim().substring(1)
        lblopenCashNewInvoice.visibility = View.VISIBLE
        tvOpenBalCashNewInvoice.text = open_cash_bal
    } else {
        lblopenCashNewInvoice.visibility = View.VISIBLE
        tvOpenBalCashNewInvoice.text = calculateSalesModelMain.data?.opening_cash_balance
    }

    when (tvOpenBalCashNewInvoice.text) {
        "0.00" -> {
            lblopenCashNewInvoice.visibility = View.VISIBLE
            tvOpenBalCashNewInvoice.text = calculateSalesModelMain.data?.opening_cash_balance
            tvOpenBalCashNewInvoice.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.header_black_text
                )
            )
        }
        else -> {
            lblopenCashNewInvoice.visibility = View.VISIBLE
            tvOpenBalCashNewInvoice.text =
                calculateSalesModelMain.data?.opening_cash_balance + " " + calculateSalesModelMain.data?.opening_cash_balance_term
            if (calculateSalesModelMain.data?.opening_cash_balance_short_term.equals(
                    "Dr",
                    ignoreCase = true
                )
            ) {
                tvOpenBalCashNewInvoice.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.debit_color
                    )
                )
            } else
                tvOpenBalCashNewInvoice.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.credit_color
                    )
                )
        }


    }

}


fun getInvoiceNumber() {
    if (NetworkUtils.isConnected()) {
        viewModel.getInvoiceNumber(
            loginModel?.data?.bearer_access_token,
            txtDateNewInvoice.text.toString(),
            transaction_id
        ).observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {

                        if (it.data?.status == true) {
                            isNoGenerated = true
                            txtInvoiceNewInvoice.setText(/*it.data.data?.prefix + '-' +*/ it.data.data?.series /*+ '-' + it.data.data?.suffix*/)

                            is_series = it.data.data?.series

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

                                    tv_newinvoice_sgst_col1.setItems(ledgerSgstNameList)

                                    for (i in 0 until ledgerSgsttList!!.size) {
                                        if (ledgerSgsttList!!.get(i).ledger_id.equals(
                                                selectedSgstId
                                            )
                                        ) {
                                            tv_newinvoice_sgst_col1.is_from_edit = true
                                            tv_newinvoice_sgst_col1.mLabelView!!.setText(
                                                ledgerSgsttList!!.get(i).name
                                            )
                                        }
                                    }
                                    tv_newinvoice_sgst_col1.setOnItemSelectListener(object :
                                        SearchableSpinner.SearchableItemListener {
                                        override fun onItemSelected(
                                            view: View?,
                                            position: Int
                                        ) {
                                            selectedSgstId =
                                                position.let { it1 -> ledgerSgsttList?.get(it1)?.ledger_id }

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

                                    tv_newinvoice_cgst_col1.setItems(ledgerCgstNameList)

                                    for (i in 0 until ledgerCgstList!!.size) {
                                        if (ledgerCgstList!!.get(i).ledger_id.equals(
                                                selectedCgstId
                                            )
                                        ) {
                                            tv_newinvoice_cgst_col1.is_from_edit = true
                                            tv_newinvoice_cgst_col1.mLabelView!!.setText(
                                                ledgerCgstList!!.get(i).name
                                            )
                                        }
                                    }
                                    tv_newinvoice_cgst_col1.setOnItemSelectListener(object :
                                        SearchableSpinner.SearchableItemListener {
                                        override fun onItemSelected(
                                            view: View?,
                                            position: Int
                                        ) {
                                            selectedCgstId =
                                                position.let { it1 -> ledgerCgstList?.get(it1)?.ledger_id }

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
                                    tv_newinvoice_igst_col1.setItems(ledgerIgstNameList)
                                    for (i in 0 until ledgerIgstList!!.size) {
                                        if (ledgerIgstList!!.get(i).ledger_id.equals(
                                                selectedIgstId
                                            )
                                        ) {
                                            tv_newinvoice_igst_col1.is_from_edit = true
                                            tv_newinvoice_igst_col1.mLabelView!!.setText(
                                                ledgerIgstList!!.get(i).name
                                            )
                                        }
                                    }
                                    tv_newinvoice_igst_col1.setOnItemSelectListener(object :
                                        SearchableSpinner.SearchableItemListener {
                                        override fun onItemSelected(
                                            view: View?,
                                            position: Int
                                        ) {
                                            selectedIgstId =
                                                position.let { it1 -> ledgerIgstList?.get(it1)?.ledger_id }

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

                                    binding.tvNewinvoiceTdstcsCol1.setItems(ledgerTcsNameList)

                                    for (i in 0 until ledgerTcsList!!.size) {
                                        if (ledgerTcsList!!.get(i).ledger_id.equals(
                                                selectedTcsId
                                            )
                                        ) {
                                            binding.tvNewinvoiceTdstcsCol1.is_from_edit = true
                                            binding.tvNewinvoiceTdstcsCol1.mLabelView!!.setText(
                                                ledgerTcsList!!.get(i).name
                                            )
                                        } else {
                                            binding.tvNewinvoiceTdstcsCol1.mLabelView!!.setText(
                                                ""
                                            )
                                        }
                                    }

                                    binding.tvNewinvoiceTdstcsCol1.setOnItemSelectListener(
                                        object :
                                            SearchableSpinner.SearchableItemListener {
                                            override fun onItemSelected(
                                                view: View?,
                                                position: Int
                                            ) {
                                                selectedTcsId =
                                                    position.let { it1 -> ledgerTcsList?.get(it1)?.ledger_id }
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

                                    binding.tvNewinvoiceTdstcsCol1.setItems(ledgerTdsNameList)
                                    for (i in 0 until ledgerTdsList!!.size) {
                                        if (ledgerTdsList!!.get(i).ledger_id.equals(
                                                selectedTdsId
                                            )
                                        ) {
                                            binding.tvNewinvoiceTdstcsCol1.is_from_edit = true
                                            binding.tvNewinvoiceTdstcsCol1.mLabelView!!.setText(
                                                ledgerTdsList!!.get(i).name
                                            )
                                        } else {
                                            binding.tvNewinvoiceTdstcsCol1.mLabelView!!.setText(
                                                ""
                                            )
                                        }
                                    }

                                    binding.tvNewinvoiceTdstcsCol1.setOnItemSelectListener(
                                        object :
                                            SearchableSpinner.SearchableItemListener {
                                            override fun onItemSelected(
                                                view: View?,
                                                position: Int
                                            ) {
                                                selectedTdsId =
                                                    position.let { it1 -> ledgerTdsList?.get(it1)?.ledger_id }
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

                                    binding.tvNewinvoiceRoundoffCol1.setItems(
                                        ledgerRoundoffNameList
                                    )
                                    for (i in 0 until ledgerRoundOffList!!.size) {
                                        if (ledgerRoundOffList!!.get(i).ledger_id.equals(
                                                selectedRoundoffId
                                            )
                                        ) {
                                            binding.tvNewinvoiceRoundoffCol1.is_from_edit = true
                                            binding.tvNewinvoiceRoundoffCol1.mLabelView!!.setText(
                                                ledgerRoundOffList!!.get(i).name
                                            )
                                        }
                                    }
                                    binding.tvNewinvoiceRoundoffCol1.setOnItemSelectListener(
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


}