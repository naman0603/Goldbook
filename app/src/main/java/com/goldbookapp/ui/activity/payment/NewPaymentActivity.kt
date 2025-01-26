package com.goldbookapp.ui.activity.payment

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
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
import com.goldbookapp.databinding.NewPaymentActivityBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.additem.AddItemActivity
import com.goldbookapp.ui.activity.sales.AddCashBankActivity
import com.goldbookapp.ui.activity.sales.AddSalesLineActivity
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.IssueReceiveAdapter
import com.goldbookapp.ui.adapter.NewPaymentItemAdapter
import com.goldbookapp.ui.ui.send.NewPaymentViewModel
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
import kotlinx.android.synthetic.main.new_payment_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NewPaymentActivity : AppCompatActivity() {
    var c = Calendar.getInstance()

    var isPhotoSelected: Boolean = false

    var selectedContactID: String? = null
    var selectedContactType: String? = null
    var selectedTransIdList = ArrayList<MultiplePaymentRefModel.TrasactionIdList>()
    var selectedInvoiceNoList: ArrayList<String> = arrayListOf()
    lateinit var contactNameAdapter: ArrayAdapter<String>

    var contactList: List<SearchContactLedgerModel.Data.Contact>? = null
    var contactNameList: List<String>? = null
    var is_reference: Int = 0
    var multipartImageBody: MultipartBody.Part? = null

    lateinit var binding: NewPaymentActivityBinding
    private lateinit var viewModel: NewPaymentViewModel
    lateinit var paymentDetailModel: ReceiptDetailModel.Data

    var is_series: String? = ""

    private lateinit var adapter: NewPaymentItemAdapter
    private lateinit var issueReceiveadapter: IssueReceiveAdapter
    var additemList = ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>()
    lateinit var calculatePaymentModelMain: CalculationReceiptModel
    var paymentLineList = ArrayList<SalesLineModel.SaleLineModelDetails>()
    var itemListFormDetail = ArrayList<SaleDetailModel.Item1427117511>()
    var transaction_id: String = ""
    var is_gst_applicable: String = "0"
    var is_From_Edit: Boolean = false
    var isNoGenerated: Boolean = false
    var imageURL: String? = ""
    var totalDue: String = "0.00"

    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    var debit_short_term: String = ""
    var credit_short_term: String = ""
    var isUserRestrLoadedOnce: Boolean = false
    private var isDefaultEnableCalledOnce: Boolean = false
    var checkedRowNo: String? = "2"
    lateinit var fiscalYearModel: FiscalYearModel

    var fineDefaultTermList: List<ItemDefaultTermModel.Data.Default_term>? = null
    var subTotalTerm: String = ""
    var subTotalTermValue: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.new_payment_activity)

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
                            //defaultDisableAllButtonnUI()
                            userWiseRestriction(loginModel.data?.bearer_access_token)
                        }
                        else -> {

                        }
                    }
                }
                // user type user
                false -> {
                    if (!isDefaultEnableCalledOnce) {
                        // defaultEnableAllButtonnUI()
                        // checkBranchType(true)
                    }

                }
            }
            autogenerateinvoice(false)
            getSearchContacts()
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
            getIssueReceiveDataFromPref()
            paymentCalculation()

            if (prefs.contains(Constants.PREF_Payment_Ref_Selected_Trans_Ids)) {

                selectedTransIdList.clear()
                selectedInvoiceNoList.clear()
                val multipleTransIDType =
                    object :
                        TypeToken<ArrayList<MultiplePaymentRefModel.TrasactionIdList>>() {}.type
                selectedTransIdList = Gson().fromJson(
                    prefs[Constants.PREF_Payment_Ref_Selected_Trans_Ids, ""],
                    multipleTransIDType
                )
                val multipleInvoiceNoType =
                    object : TypeToken<ArrayList<String>>() {}.type
                selectedInvoiceNoList = Gson().fromJson(
                    prefs[Constants.PREF_Payment_Ref_Selected_Invoice_Nos, ""],
                    multipleInvoiceNoType
                )

                //update UI
                var selectedTransIDsBuilder: StringBuilder = StringBuilder()

                for (i in 0 until selectedInvoiceNoList.size) {
                    selectedTransIDsBuilder.append(selectedInvoiceNoList.get(i)).append(", ")
                }
                tvRefNoNewPayment.visibility = View.VISIBLE
                txtRefNoNewPayment.setText(CommonUtils.removeUnwantedComma(selectedTransIDsBuilder.toString()))

            }
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

    private fun setupUIandListner() {

        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        )

        fiscalYearModel = Gson().fromJson(
            prefs[Constants.FiscalYear, ""],
            FiscalYearModel::class.java
        )

        imgLeft.setImageResource(R.drawable.ic_back)

        tvTitle.setText(R.string.new_payment)

        clearPref()

        when (is_From_Edit) {
            false -> {
                autogenerateinvoice(true)
            }
            else -> {

            }
        }

        if (loginModel.data?.branch_info?.branch_type.equals("1", true)) {
            // GST branch
            is_gst_applicable = "1"
            binding.cardAddItemNewPayment.visibility = View.GONE
            binding.cardAddLineNewPayment.visibility = View.VISIBLE
            binding.cardAddLineNewPaymentNonGST.visibility = View.GONE
            // binding.tvAddpaymentLine.setText("")

        } else { // NON-GST branch
            is_gst_applicable = "0"
            binding.cardAddItemNewPayment.visibility = View.VISIBLE
            binding.cardAddLineNewPayment.visibility = View.GONE
            binding.cardAddLineNewPaymentNonGST.visibility = View.VISIBLE
            // binding.tvAddpaymentLine.setText("Add Cash,Bank,Adjustment and Rate-cut")
        }

        debit_short_term = loginModel?.data!!.company_info!!.general_settings!!.debit_short_term!!
        credit_short_term = loginModel?.data!!.company_info!!.general_settings!!.credit_short_term!!
        binding.rvIssueReceiveList.layoutManager = LinearLayoutManager(this)
        issueReceiveadapter = IssueReceiveAdapter(
            arrayListOf(),
            "payment",
            false,
            debit_short_term,
            credit_short_term
        )
        binding.rvIssueReceiveList.adapter = issueReceiveadapter

        rv_newpayment_item.layoutManager = LinearLayoutManager(this)
        adapter = NewPaymentItemAdapter(arrayListOf())
        rv_newpayment_item.adapter = adapter

        getDataFromIntent()

        binding.cardAddLineNewPaymentNonGST.clickWithDebounce {
            checkedRowNo = "2"
            when (selectedContactID.isNullOrBlank()) {
                true -> {
                    Toast.makeText(
                        this,
                        getString(R.string.select_contact_first_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                    txtSupplierNewPayment.requestFocus()
                }
                else -> {
                    val dialog = BottomSheetDialog(this, R.style.BottomSheetDialog)

                    val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
                    dialog.setContentView(view)
                    view.rbCashPayAddSaleLine.setChecked(true)
                    if (loginModel.data?.branch_info?.branch_type.equals("1", true)) {

                        view.rbCashRecAddSaleLine.visibility = View.GONE
                        view.rbCashPayAddSaleLine.visibility = View.VISIBLE
                        view.rbBankRecAddSaleLine.visibility = View.GONE
                        view.rbBankPayAddSaleLine.visibility = View.VISIBLE
                        view.rbMetalRecAddSaleLine.visibility = View.GONE
                        view.rbMetalPayAddSaleLine.visibility = View.GONE
                        view.rbRateCutAddSaleLine.visibility = View.GONE
                        view.rbAdjustAddSaleLine.visibility = View.VISIBLE
                    }
                    //Non Gst
                    else {
                        view.rbCashRecAddSaleLine.visibility = View.GONE
                        view.rbCashPayAddSaleLine.visibility = View.VISIBLE
                        view.rbBankRecAddSaleLine.visibility = View.GONE
                        view.rbBankPayAddSaleLine.visibility = View.VISIBLE
                        view.rbMetalRecAddSaleLine.visibility = View.GONE
                        view.rbMetalPayAddSaleLine.visibility = View.GONE
                        view.rbAdjustAddSaleLine.visibility = View.VISIBLE
                        view.rbRateCutAddSaleLine.visibility = View.VISIBLE
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

                }

            }
        }

        binding.cardAddLineNewPayment.clickWithDebounce {
            checkedRowNo = "2"
            when (selectedContactID.isNullOrBlank()) {
                true -> {
                    Toast.makeText(
                        this,
                        getString(R.string.select_contact_first_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                    txtSupplierNewPayment.requestFocus()
                }
                else -> {
                    val dialog = BottomSheetDialog(this, R.style.BottomSheetDialog)

                    val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
                    dialog.setContentView(view)
                    view.rbCashPayAddSaleLine.setChecked(true)
                    if (loginModel.data?.branch_info?.branch_type.equals("1", true)) {

                        view.rbCashRecAddSaleLine.visibility = View.GONE
                        view.rbCashPayAddSaleLine.visibility = View.VISIBLE
                        view.rbBankRecAddSaleLine.visibility = View.GONE
                        view.rbBankPayAddSaleLine.visibility = View.VISIBLE
                        view.rbMetalRecAddSaleLine.visibility = View.GONE
                        view.rbMetalPayAddSaleLine.visibility = View.GONE
                        view.rbRateCutAddSaleLine.visibility = View.GONE
                        view.rbAdjustAddSaleLine.visibility = View.VISIBLE
                    }
                    //Non Gst
                    else {
                        view.rbCashRecAddSaleLine.visibility = View.GONE
                        view.rbCashPayAddSaleLine.visibility = View.VISIBLE
                        view.rbBankRecAddSaleLine.visibility = View.GONE
                        view.rbBankPayAddSaleLine.visibility = View.VISIBLE
                        view.rbMetalRecAddSaleLine.visibility = View.GONE
                        view.rbMetalPayAddSaleLine.visibility = View.GONE
                        view.rbAdjustAddSaleLine.visibility = View.VISIBLE
                        view.rbRateCutAddSaleLine.visibility = View.VISIBLE
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

                }

            }
        }

        binding.btnSaveAddPayment?.clickWithDebounce {

            if (performValidation()) {
                if (NetworkUtils.isConnected()) {
                    when (is_From_Edit) {
                        true -> {
                            editNewPaymentRequestBodyParamCallAPI()
                        }
                        false -> {
                            addNewPaymentRequestBodyParamCallAPI()
                        }
                    }
                }
            }
        }

        binding.llNewPayment.setOnFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                CommonUtils.hideKeyboardnew(this);
            }
        })


        imgLeft?.clickWithDebounce {
            onBackPressed()
        }
        txtSupplierNewPayment.doAfterTextChanged { selectedContactID = "" }

        binding.tvnewPaymentUploadphoto.clickWithDebounce {
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

        binding.ivnewPaymentAttachment.clickWithDebounce {
            ImagePicker.with(this)
                .cropSquare()                    //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                // User can only select image from Gallery
                /* .galleryOnly()
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



        binding.cardAddItemNewPayment.clickWithDebounce {

            when (selectedContactID.isNullOrBlank()) {
                true -> {
                    Toast.makeText(
                        this,
                        getString(R.string.select_contact_first_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                    txtSupplierNewPayment.requestFocus()
                }
                else -> startActivity(
                    Intent(this, AddItemActivity::class.java).putExtra(
                        Constants.TRANSACTION_TYPE,
                        "payment"
                    )
                )
            }

        }

        checkAgainstRefNewPayment.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> {
                    is_reference = 1
                    tvRefNoNewPayment.visibility = View.VISIBLE
                }
                false -> {
                    is_reference = 0
                    prefs.edit().remove(Constants.PREF_Payment_Ref_Selected_Trans_Ids).apply()
                    prefs.edit().remove(Constants.PREF_Payment_Ref_Selected_Invoice_Nos).apply()
                    tvRefNoNewPayment.visibility = View.GONE
                }
            }
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
            if (intent.extras?.containsKey(Constants.PAYMENT_DETAIL_KEY)!!) {
                var group_str: String? = intent.getStringExtra(Constants.PAYMENT_DETAIL_KEY)
                paymentDetailModel =
                    Gson().fromJson(
                        group_str,
                        ReceiptDetailModel.Data::class.java
                    )
                tvTitle.setText(R.string.edit_payment)
                is_From_Edit = true

                transaction_id = paymentDetailModel.transactionData?.transaction_id!!
                selectedContactID = paymentDetailModel.transactionData?.contact_id
                selectedContactType = paymentDetailModel.transactionData?.ledger_contact_type
                binding.txtSupplierNewPayment.setText(paymentDetailModel.transactionData?.display_name)
                binding.txtDateNewPayment.setText(paymentDetailModel.transactionData?.transaction_date)
                binding.txtPaymentNewPayment.setText(paymentDetailModel.transactionData?.invoice_number)
                is_series = paymentDetailModel.transactionData?.invoice_number.toString().trim()
                binding.remarkdNewPayment.setText(paymentDetailModel.transactionData?.remarks)
                binding.txtRefNoNewPayment.setText(paymentDetailModel.transactionData?.reference)

                itemListFormDetail = paymentDetailModel.transactionData?.item!!

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

                addIRTDatainPref()
                getIssueReceiveDataFromPref()

                if (paymentDetailModel.transactionData!!.image != null && paymentDetailModel.transactionData!!.image?.size!! > 0) {
                    binding.tvnewPaymentUploadphoto.visibility = View.GONE
                    binding.ivnewPaymentAttachment.visibility = View.VISIBLE
                    imageURL = paymentDetailModel.transactionData!!.image?.get(0)?.image
                    Glide.with(this).load(imageURL).circleCrop()
                        .placeholder(R.drawable.ic_user_placeholder)
                        .into(binding.ivnewPaymentAttachment)

                    /* val imageFile: File = File(imageURL)

                 val fileBody: RequestBody =
                     RequestBody.create(MediaType.parse("multipart/form-data"), imageFile)
                 multipartImageBody =
                     MultipartBody.Part.createFormData("image[]", imageFile.name, fileBody)
                 Log.v("imageFile", "" + imageFile.name)*/

                } else {
                    binding.tvnewPaymentUploadphoto.visibility = View.VISIBLE
                }

                binding.tvNewpaymentItemquantity.setText("Qty: " + paymentDetailModel.transactionData?.total_quantity)
                binding.tvNewpaymentGrossWt.setText("G: " + paymentDetailModel.transactionData?.total_gross_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                binding.tvNewpaymentLessWt.setText("L: " + paymentDetailModel.transactionData?.total_less_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                binding.tvNewpaymentNetWt.setText("N: " + paymentDetailModel.transactionData?.total_net_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                binding.tvNewpaymentFineWt.setText("F: " + paymentDetailModel.transactionData?.total_fine_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                // binding.tvNewpaymentMiscCharges.setText(Constants.AMOUNT_RS_APPEND + paymentDetailModel.transactionData?.total_misc_charges)

                binding.tvNewpaymentSilverItemquantity.setText("Qty: " + paymentDetailModel.transactionData?.silver_total_quantity)
                binding.tvNewpaymentSilverGrossWt.setText("G: " + paymentDetailModel.transactionData?.silver_total_gross_wt)
                binding.tvNewpaymentSilverLessWt.setText("L: " + paymentDetailModel.transactionData?.silver_total_less_wt)
                binding.tvNewpaymentSilverNetWt.setText("N: " + paymentDetailModel.transactionData?.silver_total_net_wt)
                binding.tvNewpaymentSilverFineWt.setText("F: " + paymentDetailModel.transactionData?.silver_total_fine_wt)

                binding.tvNewpaymentOtherItemquantity.setText("Qty: " + paymentDetailModel.transactionData?.other_total_quantity)
                binding.tvNewpaymentOtherGrossWt.setText("G: " + paymentDetailModel.transactionData?.other_total_gross_wt)
                binding.tvNewpaymentOtherLessWt.setText("L: 0.000")
                binding.tvNewpaymentOtherNetWt.setText("N: " + paymentDetailModel.transactionData?.other_total_net_wt)
                binding.tvNewpaymentOtherFineWt.setText("F: 0.000")

                binding.tvNewpaymentSubtotalCol1.setText(paymentDetailModel.transactionData?.total_fine_wt)
                binding.tvNewpaymentSubtotalCol2.setText(paymentDetailModel.transactionData?.final_total_amount)
                binding.tvNewpaymentSubtotalCol1Silver.setText(paymentDetailModel.transactionData?.silver_total_fine_wt)

                when (binding.tvNewpaymentSubtotalCol1.text) {
                    "0.000" -> {
                        binding.tvNewpaymentSubtotalCol1.setText(paymentDetailModel.transactionData?.total_fine_wt)
                        binding.tvNewpaymentSubtotalCol1.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.header_black_text
                            )
                        )
                    }
                    else -> {
                        binding.tvNewpaymentSubtotalCol1.setText(paymentDetailModel.transactionData?.total_fine_wt + " " + subTotalTerm)
                        if (subTotalTermValue.equals("debit")) {
                            binding.tvNewpaymentSubtotalCol1.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.debit_color
                                )
                            )
                        }

                    }
                }

                when (binding.tvNewpaymentSubtotalCol2.text) {
                    "0.00" -> {
                        binding.tvNewpaymentSubtotalCol2.setText(paymentDetailModel.transactionData?.final_total_amount)
                        binding.tvNewpaymentSubtotalCol2.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.header_black_text
                            )
                        )
                    }
                    else -> {
                        binding.tvNewpaymentSubtotalCol2.setText(paymentDetailModel.transactionData?.final_total_amount + " " + subTotalTerm)
                        if (subTotalTermValue.equals("debit")) {
                            binding.tvNewpaymentSubtotalCol2.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.debit_color
                                )
                            )

                        }
                    }
                }

                when (binding.tvNewpaymentSubtotalCol1Silver.text) {
                    "0.000" -> {
                        binding.tvNewpaymentSubtotalCol1Silver.setText(paymentDetailModel.transactionData?.silver_total_fine_wt)
                        binding.tvNewpaymentSubtotalCol1Silver.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.header_black_text
                            )
                        )
                    }
                    else -> {
                        binding.tvNewpaymentSubtotalCol1Silver.setText(paymentDetailModel.transactionData?.silver_total_fine_wt + " " + subTotalTerm)
                        if (subTotalTermValue.equals("debit")) {
                            binding.tvNewpaymentSubtotalCol1Silver.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.debit_color
                                )
                            )
                        }
                    }
                }

                binding.tvNewpaymentTotalDueGold.setText(paymentDetailModel.transactionData?.total_fine_wt_with_IRT)
                binding.tvNewpaymentTotalDueSilver.setText(paymentDetailModel.transactionData?.total_silver_fine_wt_with_IRT)
                binding.tvNewpaymentTotalDueCash.setText(Constants.AMOUNT_RS_APPEND + paymentDetailModel.transactionData?.grand_total)
                totalDue = paymentDetailModel.transactionData?.grand_total!!

            }
        }
    }

    private fun addIRTDatainPref() {
        paymentLineList.clear()
        for (i in 0 until paymentDetailModel.IRTData!!.size) {

            if (!paymentDetailModel.IRTData!!.get(i).transaction_type.equals("")) {

                val saleIRTModel = SalesLineModel.SaleLineModelDetails(
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.cash_amount,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.cash_ledger,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.cash_ledger_name,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.cash_description,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.bank_amount,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.bank_ledger,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.bank_ledger_name,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.bank_mode,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.cheque_number,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.cheque_date,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.favouring_name,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.deuct_charges,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.deuct_charges_percentage,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.bank_final_amt,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.recipient_bank,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.account_no,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.ifs_code,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.utr_number,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.bank_description,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.rcm_gold_rate,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.rate_cut_amount,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.rate_cut_fine_term,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.metal_type_id_rate_cut,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.rate_cut_fine,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.item_id,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.item_name,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.metal_type_id_metal,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.maintain_stock_in_name_metal,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.gross_wt,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.less_wt,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.net_wt,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.touch,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.wast,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.fine_wt,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.adjustment_fine,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.metal_type_id_adjustments,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.adjustment_amount,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.adjustment_ledger,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.adjustment_ledger_name,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.adjustment_description,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.type,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.transaction_type,
                    paymentDetailModel.IRTData!!.get(i).IRTDetails!!.transaction_title

                )

                paymentLineList.add(saleIRTModel)
            }
        }

        prefs[Constants.PREF_SALES_LINE_INFO_KEY] = Gson().toJson(paymentLineList)
    }


    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(
                this,
                ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))
            ).get(
                NewPaymentViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }


    private fun clearPref() {
        if (prefs.contains(Constants.PREF_SALES_LINE_INFO_KEY)) {
            prefs.edit().remove(Constants.PREF_SALES_LINE_INFO_KEY).apply()
        }
        if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {
            prefs.edit().remove(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY).apply()
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


    fun getSearchContacts() {
        contactList = ArrayList<SearchContactLedgerModel.Data.Contact>()
        contactNameList = ArrayList<String>()
        viewModel.getSearchContactsLedger(
            loginModel.data?.bearer_access_token

        ).observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {

                        if (it.data?.status == true) {
                            contactList = it.data.data?.contact

                            contactNameList = contactList?.map { it.contact_name.toString() }

                            contactNameAdapter = ArrayAdapter<String>(
                                this,
                                android.R.layout.simple_dropdown_item_1line,
                                contactNameList!!
                            )
                            binding.txtSupplierNewPayment.setAdapter(contactNameAdapter)
                            binding.txtSupplierNewPayment.threshold = 1

                            binding.txtSupplierNewPayment.setOnItemClickListener { adapterView, _, position, _
                                ->
                                val selected: String =
                                    adapterView.getItemAtPosition(position).toString()
                                val pos: Int? = contactNameList?.indexOf(selected)

                                selectedContactID =
                                    pos?.let { it1 -> contactList?.get(it1)?.contact_id }
                                selectedContactType =
                                    pos?.let { it1 -> contactList?.get(it1)?.contact_type }
                                autogenerateinvoice(false)
                                paymentCalculation()
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

    fun getInvoiceNumber() {
        viewModel.getPaymentInvoiceNumber(
            loginModel?.data?.bearer_access_token,
            txtDateNewPayment.text.toString(),
            transaction_id
        ).observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {

                        if (it.data?.status == true) {
                            isNoGenerated = true
                            txtPaymentNewPayment.setText(/*it.data.data?.prefix +*/ it.data.data?.series /*+ '-' + it.data.data?.suffix*/)

                            /* is_prefix = it.data.data?.prefix*/
                            is_series = it.data.data?.series
                            /*is_suffix = it.data.data?.suffix*/


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

    fun removeItem(index: Int) {
        if (isValidClickPressed()) {
            if (additemList != null && additemList.size > 0) {
                if (index >= additemList.size) {
                    //index not exists
                } else {
                    // index exists
                    additemList.removeAt(index)
                    adapter.notifyDataSetChanged()

                    if (additemList.size > 0) {
                        prefs[Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY] =
                            Gson().toJson(additemList)
                        paymentCalculation()
                    } else {
                        prefs.edit().remove(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY).apply()
                        linear_calculation_view_payment.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun autogenerateinvoice(isFromOnCreate: Boolean) {
        when (isFromOnCreate) {
            true -> {
                txtDateNewPayment.setText(SimpleDateFormat("dd-MMM-yy").format(Date()))
            }
            else -> {

            }
        }

        binding.txtDateNewPayment.clickWithDebounce {
            openDatePicker(true)
        }
    }


    fun openDatePicker(isFromDate: Boolean) {

        //val c = Calendar.getInstance()
        if (isFromDate) {
            val sdf = SimpleDateFormat("dd-MMM-yy")
            val parse = sdf.parse(txtDateNewPayment.text.toString())
            c.setTime(parse)
        } else {

        }


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
                binding.txtDateNewPayment.setText(
                    "" + String.format(
                        "%02d",
                        dayOfMonth
                    ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                        .substring(2, 4)
                )
                when (is_From_Edit) {
                    false -> {
                        getInvoiceNumber()
                    }
                    else -> {

                    }
                }

                paymentCalculation()

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
        // dpd.datePicker.minDate = Date().time
        dpd.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            val fileUri = data?.data
            //imgProfile.setImageURI(fileUri)

            binding.tvnewPaymentUploadphoto.visibility = View.GONE
            binding.ivnewPaymentAttachment.visibility = View.VISIBLE
            isPhotoSelected = true
            Glide.with(this).load(fileUri).circleCrop().into(binding.ivnewPaymentAttachment)

            //You can get File object from intent
            val imageFile: File = ImagePicker.getFile(data)!!

            val fileBody: RequestBody =
                RequestBody.create(MediaType.parse("multipart/form-data"), imageFile)
            multipartImageBody =
                MultipartBody.Part.createFormData("image[]", imageFile.name, fileBody)
            //updateProfileImageAPI(loginModel?.data?.bearer_access_token, multipartBody)

            //You can also get File Path from intent
            val filePath: String = ImagePicker.getFilePath(data)!!


        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {

        }
    }


    private fun paymentCalculation() {
        if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {

            val collectionType = object :
                TypeToken<ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>>() {}.type
            additemList =
                Gson().fromJson(
                    prefs[Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY, ""],
                    collectionType
                )

            getIssueReceiveDataFromPref()

            paymentCalculateAPI(true)

        } else {
            // called when date changed (for opening bal update according to date)
            getIssueReceiveDataFromPref()
            when (!selectedContactID.isNullOrBlank() && !txtDateNewPayment.text.toString()
                .isBlank()) {
                true -> {
                    paymentCalculateAPI(
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
            paymentLineList =
                Gson().fromJson(
                    prefs[Constants.PREF_SALES_LINE_INFO_KEY, ""],
                    collectionType
                )
            setupIssueReceiveAdapter()

        }
    }

    private fun setupIssueReceiveAdapter() {
        when (paymentLineList.size > 0) {
            true -> {
                binding.rvIssueReceiveList.visibility = View.VISIBLE
                issueReceiveadapter.apply {
                    addissueReceiveList(paymentLineList)
                    notifyDataSetChanged()
                }
            }
            else -> {

            }
        }
    }

    fun paymentCalculateAPI(
        showLoading: Boolean
    ) {


        if (NetworkUtils.isConnected()) {

            viewModel.getCalculateItemPayment(
                loginModel.data?.bearer_access_token,
                Gson().toJson(additemList),
                selectedContactID,
                selectedContactType,
                transaction_id,
                binding.txtDateNewPayment.text.toString(),
                Gson().toJson(paymentLineList)
            )
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {
                                    fill_itemPayment_details_data(it.data)

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

    private fun fill_itemPayment_details_data(calculatePaymentModel: CalculationReceiptModel) {

        calculatePaymentModelMain = calculatePaymentModel


        if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {
            if (additemList.size > 0) {
                adapter.apply {
                    addPaymentItems(additemList)
                    notifyDataSetChanged()
                }
            }
        }

        binding.tvNewpaymentItemquantity.setText("Qty: " + calculatePaymentModel.data?.total_quantity)
        binding.tvNewpaymentGrossWt.setText("G: " + calculatePaymentModel.data?.total_gross_wt)
        binding.tvNewpaymentLessWt.setText("L: " + calculatePaymentModel.data?.total_less_wt)
        binding.tvNewpaymentNetWt.setText("N: " + calculatePaymentModel.data?.total_net_wt)
        binding.tvNewpaymentFineWt.setText("F: " + calculatePaymentModel.data?.total_fine_wt)

        binding.tvNewpaymentSilverItemquantity.setText("Qty: " + calculatePaymentModel.data?.silver_total_quantity)
        binding.tvNewpaymentSilverGrossWt.setText("G: " + calculatePaymentModel.data?.silver_total_gross_wt)
        binding.tvNewpaymentSilverLessWt.setText("L: " + calculatePaymentModel.data?.silver_total_less_wt)
        binding.tvNewpaymentSilverNetWt.setText("N: " + calculatePaymentModel.data?.silver_total_net_wt)
        binding.tvNewpaymentSilverFineWt.setText("F: " + calculatePaymentModel.data?.silver_total_fine_wt)

        binding.tvNewpaymentOtherItemquantity.setText("Qty: " + calculatePaymentModel.data?.other_total_quantity)
        binding.tvNewpaymentOtherGrossWt.setText("G: " + calculatePaymentModel.data?.other_total_gross_wt)
        binding.tvNewpaymentOtherLessWt.setText("L: 0.000")
        binding.tvNewpaymentOtherNetWt.setText("N: " + calculatePaymentModel.data?.other_total_net_wt)
        binding.tvNewpaymentOtherFineWt.setText("F: 0.000")

        //  binding.tvNewpaymentMiscCharges.setText(Constants.AMOUNT_RS_APPEND + calculatePaymentModel.data?.total_misc_charges)
        binding.tvNewpaymentSubtotalCol1.setText(calculatePaymentModel.data?.total_fine_wt)
        binding.tvNewpaymentSubtotalCol2.setText(calculatePaymentModel.data?.final_total_amount)
        binding.tvNewpaymentSubtotalCol1Silver.setText(calculatePaymentModel.data?.silver_total_fine_wt)

        when (binding.tvNewpaymentSubtotalCol1.text) {
            "0.000" -> {
                binding.tvNewpaymentSubtotalCol1.setText(calculatePaymentModel.data?.total_fine_wt)
                binding.tvNewpaymentSubtotalCol1.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvNewpaymentSubtotalCol1.setText(calculatePaymentModel.data?.total_fine_wt + " " + subTotalTerm)
                if (subTotalTermValue.equals("debit")) {
                    binding.tvNewpaymentSubtotalCol1.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                }

            }
        }

        when (binding.tvNewpaymentSubtotalCol2.text) {
            "0.00" -> {
                binding.tvNewpaymentSubtotalCol2.setText(calculatePaymentModel.data?.final_total_amount)
                binding.tvNewpaymentSubtotalCol2.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvNewpaymentSubtotalCol2.setText(calculatePaymentModel.data?.final_total_amount + " " + subTotalTerm)
                if (subTotalTermValue.equals("debit")) {
                    binding.tvNewpaymentSubtotalCol2.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )

                }
            }
        }

        when (binding.tvNewpaymentSubtotalCol1Silver.text) {
            "0.000" -> {
                binding.tvNewpaymentSubtotalCol1Silver.setText(calculatePaymentModel.data?.silver_total_fine_wt)
                binding.tvNewpaymentSubtotalCol1Silver.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvNewpaymentSubtotalCol1Silver.setText(calculatePaymentModel.data?.silver_total_fine_wt + " " + subTotalTerm)
                if (subTotalTermValue.equals("debit")) {
                    binding.tvNewpaymentSubtotalCol1Silver.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                }
            }
        }


        binding.tvNewpaymentTotalDueGold.setText(calculatePaymentModel.data?.total_fine_wt_with_IRT)
        binding.tvNewpaymentTotalDueSilver.setText(calculatePaymentModel.data?.total_silver_fine_wt_with_IRT)
        binding.tvNewpaymentTotalDueCash.setText(Constants.AMOUNT_RS_APPEND + calculatePaymentModel.data?.grand_total)
        totalDue = calculatePaymentModel.data?.grand_total!!


        if (!calculatePaymentModel.data?.total_gross_wt.equals("0.000")) {
            binding.llNewpaymentMetalgold.visibility = View.VISIBLE
            //binding.tvNewpaymentSubtotallabel.visibility = View.VISIBLE
            /* binding.tvNewpaymentSubtotalCol1.visibility = View.VISIBLE
             binding.tvNewpaymentSubtotalCol2.visibility = View.VISIBLE*/
        } else {
            binding.llNewpaymentMetalgold.visibility = View.GONE
            /* binding.tvNewpaymentSubtotalCol1.visibility = View.GONE
             binding.tvNewpaymentSubtotalCol2.visibility = View.GONE*/
        }

        if (!calculatePaymentModel.data?.silver_total_gross_wt.equals("0.000")) {
            binding.llNewpaymentMetalsilver.visibility = View.VISIBLE
            /*binding.tvNewpaymentSubtotalCollabelSilver.visibility = View.VISIBLE
            binding.tvNewpaymentSubtotalCol1Silver.visibility = View.VISIBLE
            binding.tvNewpaymentSubtotalCol2Silver.visibility = View.VISIBLE*/
        } else {
            binding.llNewpaymentMetalsilver.visibility = View.GONE
            /* binding.tvNewpaymentSubtotalCollabelSilver.visibility = View.GONE
             binding.tvNewpaymentSubtotalCol1Silver.visibility = View.GONE
             binding.tvNewpaymentSubtotalCol2Silver.visibility = View.GONE*/
        }

        if (!calculatePaymentModel.data?.other_total_gross_wt.equals("0.000")) {
            binding.llNewpaymentMetalother.visibility = View.VISIBLE
            /*binding.tvNewpaymentSubtotalCollabelOther.visibility = View.VISIBLE
            binding.tvNewpaymentSubtotalCol1Other.visibility = View.VISIBLE
            binding.tvNewpaymentSubtotalCol2Other.visibility = View.VISIBLE*/

        } else {
            binding.llNewpaymentMetalother.visibility = View.GONE
            /*binding.tvNewpaymentSubtotalCollabelOther.visibility = View.GONE
            binding.tvNewpaymentSubtotalCol1Other.visibility = View.GONE
            binding.tvNewpaymentSubtotalCol2Other.visibility = View.GONE*/
        }



        updateUIofTotalDue(calculatePaymentModel)

        binding.linearCalculationViewPayment.visibility = View.VISIBLE
        if (loginModel.data?.branch_info?.branch_type.equals("1", true)) {
            //GST branch
            binding.llNewpaymentMetalweights.visibility = View.GONE
            binding.llNewpaymentSubtotalRoot.visibility = View.GONE
        } else {
            binding.llNewpaymentMetalweights.visibility = View.VISIBLE
            binding.llNewpaymentSubtotalRoot.visibility = View.VISIBLE
        }
        // opening balance update method
        updateOpeningFineOpeningCash(calculatePaymentModel)
        updateClosingFineClosingCash(calculatePaymentModel)


    }

    private fun updateUIofTotalDue(calculatePaymentModel: CalculationReceiptModel) {
        if (!calculatePaymentModel.data?.total_fine_wt_with_IRT.equals("0.000") &&
            calculatePaymentModel.data?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewpaymentSilverTotaldue.visibility = View.GONE
            binding.tvNewpaymentTotaldueGoldLabel.visibility = View.VISIBLE
            binding.tvNewpaymentTotalDueGold.visibility = View.VISIBLE
        }

        if (!calculatePaymentModel.data?.total_fine_wt_with_IRT.equals("0.000") &&
            !calculatePaymentModel.data?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewpaymentSilverTotaldue.visibility = View.VISIBLE
            binding.tvNewpaymentTotaldueGoldLabel.visibility = View.VISIBLE
            binding.tvNewpaymentTotalDueGold.visibility = View.VISIBLE
        }



        if (calculatePaymentModel.data?.total_fine_wt_with_IRT.equals("0.000") &&
            !calculatePaymentModel.data?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewpaymentSilverTotaldue.visibility = View.GONE
            binding.tvNewpaymentTotaldueGoldLabel.setText("S: ")
            binding.tvNewpaymentTotalDueGold.setText(calculatePaymentModel.data?.total_silver_fine_wt_with_IRT)
            when (binding.tvNewpaymentTotalDueGold.text) {
                "0.000" -> {
                    binding.tvNewpaymentTotalDueGold.text =
                        calculatePaymentModel.data?.total_silver_fine_wt_with_IRT
                    binding.tvNewpaymentTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.header_black_text
                        )
                    )
                }

                else -> {
                    binding.tvNewpaymentTotalDueGold.text =
                        calculatePaymentModel.data?.total_silver_fine_wt_with_IRT + " " +
                                calculatePaymentModel.data?.total_silver_fine_wt_with_IRT_term
                    if (calculatePaymentModel.data?.total_silver_fine_wt_with_IRT_short_term.equals(
                            "Dr",
                            ignoreCase = true
                        )
                    ) {
                        binding.tvNewpaymentTotalDueGold.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.debit_color
                            )
                        )
                    } else
                        binding.tvNewpaymentTotalDueGold.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.credit_color
                            )
                        )
                }
            }

            when (binding.tvNewpaymentTotalDueCash.text) {
                "0.00" -> {
                    binding.tvNewpaymentTotalDueCash.text =
                        calculatePaymentModel.data?.grand_total
                    binding.tvNewpaymentTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.header_black_text
                        )
                    )
                }
                else -> {
                    binding.tvNewpaymentTotalDueCash.text =
                        calculatePaymentModel.data?.grand_total + " " +
                                calculatePaymentModel.data?.grand_total_term
                    if (calculatePaymentModel.data?.grand_total_short_term.equals(
                            "Dr",
                            ignoreCase = true
                        )
                    ) {
                        binding.tvNewpaymentTotalDueCash.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.debit_color
                            )
                        )
                    } else
                        binding.tvNewpaymentTotalDueCash.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.credit_color
                            )
                        )
                }
            }

        } else {
            binding.tvNewpaymentTotaldueGoldLabel.setText("G: ")
            updateTotalDuewithDrCr(calculatePaymentModel)
        }

        /*if(!calculatePaymentModel.data?.total_silver_fine_wt_with_IRT.equals("0.000")){
            binding.llNewinvoiceSilverTotaldue.visibility = View.VISIBLE
        }else{
            binding.llNewinvoiceSilverTotaldue.visibility = View.GONE
        }*/


        if (calculatePaymentModel.data?.total_fine_wt_with_IRT.equals("0.000") &&
            calculatePaymentModel.data?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewpaymentSilverTotaldue.visibility = View.GONE
            binding.tvNewpaymentTotaldueGoldLabel.visibility = View.GONE
            binding.tvNewpaymentTotalDueGold.visibility = View.GONE
        } else {
            //  binding.llNewinvoiceSilverTotaldue.visibility = View.VISIBLE
            //  tv_newinvoice_totaldue_gold_label.visibility = View.VISIBLE
            // tv_newinvoice_totalDue_gold.visibility = View.VISIBLE
        }
    }

    private fun updateTotalDuewithDrCr(calculatePaymentModel: CalculationReceiptModel) {
        when (binding.tvNewpaymentTotalDueGold.text) {
            "0.000" -> {
                binding.tvNewpaymentTotalDueGold.text =
                    calculatePaymentModel.data?.total_fine_wt_with_IRT
                binding.tvNewpaymentTotalDueGold.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvNewpaymentTotalDueGold.text =
                    calculatePaymentModel.data?.total_fine_wt_with_IRT + " " +
                            calculatePaymentModel.data?.total_fine_wt_with_IRT_term
                if (calculatePaymentModel.data?.total_fine_wt_with_IRT_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewpaymentTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewpaymentTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        when (binding.tvNewpaymentTotalDueSilver.text) {
            "0.000" -> {
                binding.tvNewpaymentTotalDueSilver.text =
                    calculatePaymentModel.data?.total_silver_fine_wt_with_IRT
                binding.tvNewpaymentTotalDueSilver.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvNewpaymentTotalDueSilver.text =
                    calculatePaymentModel.data?.total_silver_fine_wt_with_IRT + " " +
                            calculatePaymentModel.data?.total_silver_fine_wt_with_IRT_term
                if (calculatePaymentModel.data?.total_silver_fine_wt_with_IRT_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewpaymentTotalDueSilver.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewpaymentTotalDueSilver.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        when (binding.tvNewpaymentTotalDueCash.text) {
            "0.00" -> {
                binding.tvNewpaymentTotalDueCash.text =
                    calculatePaymentModel.data?.grand_total
                binding.tvNewpaymentTotalDueCash.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvNewpaymentTotalDueCash.text =
                    calculatePaymentModel.data?.grand_total + " " +
                            calculatePaymentModel.data?.grand_total_term
                if (calculatePaymentModel.data?.grand_total_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewpaymentTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewpaymentTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }
    }

    private fun updateClosingFineClosingCash(calculatePaymentModel: CalculationReceiptModel) {

        if (calculatePaymentModel.data?.closing_fine_balance!!.startsWith("-")) {
            val open_fine_bal: String =
                calculatePaymentModel.data?.closing_fine_balance.toString().trim().substring(1)
            binding.tvCloBalFineWtNewpayment.text = open_fine_bal
        } else {
            binding.tvCloBalFineWtNewpayment.text =
                calculatePaymentModel.data?.closing_fine_balance
        }

        when (binding.tvCloBalFineWtNewpayment.text) {
            "0.000" -> {
                binding.tvCloBalFineWtNewpayment.text =
                    calculatePaymentModel.data?.closing_fine_balance
                binding.tvCloBalFineWtNewpayment.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvCloBalFineWtNewpayment.text =
                    calculatePaymentModel.data?.closing_fine_balance + " " + calculatePaymentModel.data?.closing_fine_balance_term
                if (calculatePaymentModel.data?.closing_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvCloBalFineWtNewpayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvCloBalFineWtNewpayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        if (calculatePaymentModel.data?.closing_silver_fine_balance!!.startsWith("-")) {
            val open_fine_bal: String =
                calculatePaymentModel.data?.closing_silver_fine_balance.toString().trim()
                    .substring(1)
            binding.tvCloBalSilverNewpayment.text = open_fine_bal
        } else {
            binding.tvCloBalSilverNewpayment.text =
                calculatePaymentModel.data?.closing_silver_fine_balance
        }

        when (binding.tvCloBalSilverNewpayment.text) {
            "0.000" -> {
                binding.tvCloBalSilverNewpayment.text =
                    calculatePaymentModel.data?.closing_silver_fine_balance
                binding.tvCloBalSilverNewpayment.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvCloBalSilverNewpayment.text =
                    calculatePaymentModel.data?.closing_silver_fine_balance + " " + calculatePaymentModel.data?.closing_silver_fine_balance_term
                if (calculatePaymentModel.data?.closing_silver_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvCloBalSilverNewpayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvCloBalSilverNewpayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        if (calculatePaymentModel.data?.closing_cash_balance!!.startsWith("-")) {
            val open_cash_bal: String =
                calculatePaymentModel.data?.closing_cash_balance.toString().trim().substring(1)
            binding.tvCloBalCashNewpayment.text = open_cash_bal
        } else {
            binding.tvCloBalCashNewpayment.text =
                calculatePaymentModel.data?.closing_cash_balance
        }

        when (binding.tvCloBalCashNewpayment.text) {
            "0.00" -> {
                binding.tvCloBalCashNewpayment.text =
                    calculatePaymentModel.data?.closing_cash_balance
                binding.tvCloBalCashNewpayment.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvCloBalCashNewpayment.text =
                    calculatePaymentModel.data?.closing_cash_balance + " " + calculatePaymentModel.data?.closing_cash_balance_term
                if (calculatePaymentModel.data?.closing_cash_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvCloBalCashNewpayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvCloBalCashNewpayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }


        }

    }

    private fun updateOpeningFineOpeningCash(calculatePaymentModel: CalculationReceiptModel) {
        if (calculatePaymentModel.data?.opening_fine_balance!!.startsWith("-")) {
            val open_fine_bal: String =
                calculatePaymentModel.data?.opening_fine_balance.toString().trim().substring(1)
            binding.tvOpenBalFineNewPayment.text = open_fine_bal
            binding.tvOpenBalFineLabelPayment.visibility = View.VISIBLE
        } else {
            binding.tvOpenBalFineLabelPayment.visibility = View.VISIBLE
            binding.tvOpenBalFineNewPayment.text =
                calculatePaymentModel.data?.opening_fine_balance
        }


        when (binding.tvOpenBalFineNewPayment.text) {
            "0.000" -> {
                binding.tvOpenBalFineNewPayment.text =
                    calculatePaymentModel.data?.opening_fine_balance
                binding.tvOpenBalFineNewPayment.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvOpenBalFineNewPayment.text =
                    calculatePaymentModel.data?.opening_fine_balance + " " + calculatePaymentModel.data?.opening_fine_balance_term
                if (calculatePaymentModel.data?.opening_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvOpenBalFineNewPayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvOpenBalFineNewPayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }


        if (calculatePaymentModel.data?.opening_silver_fine_balance!!.startsWith("-")) {
            val open_fine_bal: String =
                calculatePaymentModel.data?.opening_silver_fine_balance.toString().trim()
                    .substring(1)
            binding.tvOpenBalSilverFineNewPayment.text = open_fine_bal
            binding.tvOpenBalSilverFineLabelPayment.visibility = View.VISIBLE
        } else {
            binding.tvOpenBalSilverFineLabelPayment.visibility = View.VISIBLE
            binding.tvOpenBalSilverFineNewPayment.text =
                calculatePaymentModel.data?.opening_silver_fine_balance
        }


        when (binding.tvOpenBalSilverFineNewPayment.text) {
            "0.000" -> {
                binding.tvOpenBalSilverFineNewPayment.text =
                    calculatePaymentModel.data?.opening_silver_fine_balance
                binding.tvOpenBalSilverFineNewPayment.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvOpenBalSilverFineNewPayment.text =
                    calculatePaymentModel.data?.opening_silver_fine_balance + " " + calculatePaymentModel.data?.opening_silver_fine_balance_term
                if (calculatePaymentModel.data?.opening_silver_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvOpenBalSilverFineNewPayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvOpenBalSilverFineNewPayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }


        if (calculatePaymentModel.data?.opening_cash_balance!!.startsWith("-")) {
            val open_cash_bal: String =
                calculatePaymentModel.data?.opening_cash_balance.toString().trim().substring(1)
            binding.tvOpenBalCashNewPayment.text = open_cash_bal
            binding.tvOpenBalCashLabelPayment.visibility = View.VISIBLE
        } else {
            binding.tvOpenBalCashLabelPayment.visibility = View.VISIBLE
            binding.tvOpenBalCashNewPayment.text =
                calculatePaymentModel.data?.opening_cash_balance
        }

        when (binding.tvOpenBalCashNewPayment.text) {
            "0.00" -> {
                binding.tvOpenBalCashNewPayment.text =
                    calculatePaymentModel.data?.opening_cash_balance
                binding.tvOpenBalCashNewPayment.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvOpenBalCashNewPayment.text =
                    calculatePaymentModel.data?.opening_cash_balance + " " + calculatePaymentModel.data?.opening_cash_balance_term
                if (calculatePaymentModel.data?.opening_cash_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvOpenBalCashNewPayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvOpenBalCashNewPayment.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }


        }

    }


    fun performValidation(): Boolean {
        if (txtSupplierNewPayment.text.toString().isBlank()) {
            CommonUtils.showDialog(
                this,
                getString(R.string.select_contact_first_msg)/*"Please Select Contact Name"*/
            )
            txtSupplierNewPayment.requestFocus()
            return false
        } else if (txtPaymentNewPayment.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.enter_invoice_msg))
            txtPaymentNewPayment.requestFocus()
            return false
        } /*else if (checkAgainstRefNewPayment.isChecked && selectedInvoiceNoList.size.equals(0)) {
            CommonUtils.showDialog(this, getString(R.string.select_reference_number_msg))
            txtRefNoNewPayment.requestFocus()
            return false
        } */ /*else if (is_gst_applicable == "0" && !prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {
            CommonUtils.showDialog(this, getString(R.string.add_an_item_msg))
            return false
        } */ /*else if (is_gst_applicable == "1" && totalDue == "0.00") {
            CommonUtils.showDialog(this, getString(R.string.total_due_msg))
            return false
        }*/
        return true
    }


    private fun addNewPaymentRequestBodyParamCallAPI() {


        if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {
            val collectionType =
                object :
                    TypeToken<ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>>() {}.type
            additemList =
                Gson().fromJson(
                    prefs[Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY, ""],
                    collectionType
                )
        }
        if (prefs.contains(Constants.PREF_SALES_LINE_INFO_KEY)) {
            val collectionType =
                object :
                    TypeToken<ArrayList<SalesLineModel.SaleLineModelDetails>>() {}.type
            paymentLineList =
                Gson().fromJson(
                    prefs[Constants.PREF_SALES_LINE_INFO_KEY, ""],
                    collectionType
                )

        } else {
            var childModel = SalesLineModel.SaleLineModelDetails(
                "", "", "",
                "", "", "", "", "",
                "", "", "", "", "",
                "", "", "", "", "", "",
                "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", ""
            )
            paymentLineList.add(childModel)
        }

        val transaction_type_id: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            "4"
        )

        val transaction_type_name: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            "Payment"
        )

        val transaction_date: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.txtDateNewPayment.text.toString()
        )

        val contact_id: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            selectedContactID
        )

        val ledger_contact_type: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            selectedContactType
        )
        val invoice_number: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            is_series.toString().trim()
        )

        val is_reference: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            is_reference.toString()
        )
        val remarks: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.remarkdNewPayment.text.toString()
        )

        val issue_receive_transaction: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            Gson().toJson(paymentLineList)
        )

        val itemadded: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            Gson().toJson(additemList)
        )

        val party_po_no: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            "Party PO No"
        )

        val reference: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.txtRefNoNewPayment.text.toString()
        )
        val is_gst_applicabl: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            is_gst_applicable
        )

        addNewPaymentAPI(
            loginModel?.data?.bearer_access_token,
            transaction_type_id,
            transaction_type_name,
            transaction_date,
            contact_id,
            ledger_contact_type,
            invoice_number,
            itemadded,
            issue_receive_transaction,
            is_gst_applicabl,
            party_po_no,
            reference,
            remarks,
            multipartImageBody
        )

    }

    fun addNewPaymentAPI(
        token: String?,
        transaction_type_id: RequestBody?,
        transaction_type_name: RequestBody?,
        transaction_date: RequestBody?,
        contact_id: RequestBody?,
        ledger_contact_type: RequestBody?,
        invoice_number: RequestBody?,
        item_json: RequestBody?,
        issue_receive_transaction: RequestBody?,
        is_gst_applicable: RequestBody?,
        party_po_no: RequestBody?,
        reference: RequestBody?,
        remarks: RequestBody?,
        image: MultipartBody.Part?
    ) {

        viewModel.addNewPayment(
            token,
            transaction_type_id,
            transaction_type_name,
            transaction_date,
            contact_id,
            ledger_contact_type,
            invoice_number,
            item_json,
            issue_receive_transaction,
            is_gst_applicable,
            party_po_no, reference,
            remarks,
            image
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

    private fun editNewPaymentRequestBodyParamCallAPI() {
        if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {
            val collectionType =
                object :
                    TypeToken<ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>>() {}.type
            additemList =
                Gson().fromJson(
                    prefs[Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY, ""],
                    collectionType
                )
        }
        if (prefs.contains(Constants.PREF_SALES_LINE_INFO_KEY)) {
            val collectionType =
                object :
                    TypeToken<ArrayList<SalesLineModel.SaleLineModelDetails>>() {}.type
            paymentLineList =
                Gson().fromJson(
                    prefs[Constants.PREF_SALES_LINE_INFO_KEY, ""],
                    collectionType
                )

        } else {
            var childModel = SalesLineModel.SaleLineModelDetails(
                "", "", "",
                "", "", "", "", "",
                "", "", "", "", "",
                "", "", "", "", "", "",
                "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "",
                "", "", "", "", "",
                "", "", "", ""
            )
            paymentLineList.add(childModel)
        }

        val transaction_type_id: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            "4"
        )

        val transaction_type_name: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            "Payment"
        )

        val transaction_id: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            transaction_id
        )

        val transaction_date: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.txtDateNewPayment.text.toString()
        )

        val contact_id: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            selectedContactID
        )

        val ledger_contact_type: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            selectedContactType
        )
        val invoice_number: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            is_series.toString().trim()
        )

        val is_reference: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            is_reference.toString()
        )
        val remarks: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.remarkdNewPayment.text.toString()
        )

        val issue_receive_transaction: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            Gson().toJson(paymentLineList)
        )

        val itemadded: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            Gson().toJson(additemList)
        )

        val party_po_no: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            "Party PO No"
        )

        val reference: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.txtRefNoNewPayment.text.toString()
        )
        val is_gst_applicabl: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            is_gst_applicable
        )

        editPaymentAPI(
            loginModel?.data?.bearer_access_token,
            transaction_type_id,
            transaction_type_name,
            transaction_id,
            transaction_date,
            contact_id,
            ledger_contact_type,
            invoice_number,
            itemadded,
            issue_receive_transaction,
            is_gst_applicabl,
            party_po_no,
            reference,
            remarks,
            multipartImageBody
        )
    }

    fun editPaymentAPI(
        token: String?,
        transaction_type_id: RequestBody?,
        transaction_type_name: RequestBody?,
        transaction_id: RequestBody?,
        transaction_date: RequestBody?,
        contact_id: RequestBody?,
        ledger_contact_type: RequestBody?,
        invoice_number: RequestBody?,
        item_json: RequestBody?,
        issue_receive_transaction: RequestBody?,
        is_gst_applicable: RequestBody?,
        party_po_no: RequestBody?,
        reference: RequestBody?,
        remarks: RequestBody?,
        image: MultipartBody.Part?
    ) {

        viewModel.editPayment(
            token,
            transaction_type_id,
            transaction_type_name,
            transaction_id,
            transaction_date,
            contact_id,
            ledger_contact_type,
            invoice_number,
            item_json,
            issue_receive_transaction,
            is_gst_applicable,
            party_po_no, reference,
            remarks,
            image
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


    fun editIssueReceiveItem(
        index: Int,
        issueReceive_rowModel: SalesLineModel.SaleLineModelDetails
    ) {
        if (isValidClickPressed()) {
            if (paymentLineList != null && paymentLineList.size > 0) {
                if (index >= paymentLineList.size) {
                    //index not exists
                } else {
                    // index exists
                    startActivity(
                        Intent(this@NewPaymentActivity, AddCashBankActivity::class.java)
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

    fun removeIssueReceiveItem(index: Int) {
        if (isValidClickPressed()) {
            if (paymentLineList != null && paymentLineList.size > 0) {
                if (index >= paymentLineList.size) {
                    //index not exists
                } else {
                    // index exists
                    paymentLineList.removeAt(index)


                    if (paymentLineList.size > 0) {
                        prefs[Constants.PREF_SALES_LINE_INFO_KEY] =
                            Gson().toJson(paymentLineList)
                    } else {
                        prefs.edit().remove(Constants.PREF_SALES_LINE_INFO_KEY).apply()
                        binding.rvIssueReceiveList.visibility = View.GONE
                    }
                    paymentCalculation()
                }
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
        /* for (i in 0 until data.fields!!.size) {
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
