package com.goldbookapp.ui.activity.receipt

import ReceiptDetailModel
import android.app.Activity
import android.app.DatePickerDialog
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
import com.goldbookapp.databinding.NewReceiptActivityBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.additem.AddItemActivity
import com.goldbookapp.ui.activity.sales.AddCashBankActivity
import com.goldbookapp.ui.activity.sales.AddSalesLineActivity
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.IssueReceiveAdapter
import com.goldbookapp.ui.adapter.NewReceiptItemAdapter
import com.goldbookapp.ui.ui.send.NewReceiptViewModel
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
import kotlinx.android.synthetic.main.new_receipt_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NewReceiptActivity : AppCompatActivity() {
    var c = Calendar.getInstance()
    var isPhotoSelected: Boolean = false

    var selectedContactID: String? = null
    var selectedContactType: String? = null

    var selectedTransIdList = ArrayList<MultipleReceiptRefModel.TrasactionIdList>()
    var selectedInvoiceNoList: ArrayList<String> = arrayListOf()
    lateinit var contactNameAdapter: ArrayAdapter<String>
    lateinit var receiptDetailModel: ReceiptDetailModel.Data
    var contactList: List<SearchContactLedgerModel.Data.Contact>? = null
    var contactNameList: List<String>? = null
    var is_reference: Int = 0
    var multipartImageBody: MultipartBody.Part? = null
    var receiptLineList = ArrayList<SalesLineModel.SaleLineModelDetails>()
    lateinit var binding: NewReceiptActivityBinding
    private lateinit var viewModel: NewReceiptViewModel
    var itemListFormDetail = ArrayList<SaleDetailModel.Item1427117511>()

    var is_series: String? = ""
    var is_From_Edit: Boolean = false
    var isNoGenerated: Boolean = false

    private lateinit var adapter: NewReceiptItemAdapter
    private lateinit var issueReceiveadapter: IssueReceiveAdapter
    var additemList = ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>()
    lateinit var calculateReceiptModelMain: CalculationReceiptModel

    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    var transaction_id: String = ""
    var is_gst_applicable: String = "0"
    var totalDue: String = "0.00"
    var imageURL: String? = ""
    var debit_short_term: String = ""
    var credit_short_term: String = ""
    var isUserRestrLoadedOnce: Boolean = false
    private var isDefaultEnableCalledOnce: Boolean = false
    var checkedRowNo: String? = "1"
    lateinit var fiscalYearModel: FiscalYearModel

    var fineDefaultTermList: List<ItemDefaultTermModel.Data.Default_term>? = null
    var subTotalTerm: String = ""
    var subTotalTermValue: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.new_receipt_activity)

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
                        // defaultEnableAllButtonnUI()
                        // checkBranchType(true)
                    }

                }
            }
            getSearchContactsLedger()
            autogenerateinvoice(false)
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
            receiptcalculation()


            //For Reference
            if (prefs.contains(Constants.Receipt_Ref_Selected_Trans_Ids)) {

                selectedTransIdList.clear()
                selectedInvoiceNoList.clear()
                val multipleTransIDType =
                    object :
                        TypeToken<ArrayList<MultipleReceiptRefModel.TrasactionIdList>>() {}.type
                selectedTransIdList = Gson().fromJson(
                    prefs[Constants.Receipt_Ref_Selected_Trans_Ids, ""],
                    multipleTransIDType
                )
                val multipleInvoiceNoType =
                    object : TypeToken<ArrayList<String>>() {}.type
                selectedInvoiceNoList = Gson().fromJson(
                    prefs[Constants.Receipt_Ref_Selected_Invoice_Nos, ""],
                    multipleInvoiceNoType
                )

                //update UI
                var selectedTransIDsBuilder: StringBuilder = StringBuilder()

                for (i in 0 until selectedInvoiceNoList.size) {
                    selectedTransIDsBuilder.append(selectedInvoiceNoList.get(i)).append(", ")
                }
                tvRefNoNewReceipt.visibility = View.VISIBLE
                txtRefNoNewReceipt.setText(CommonUtils.removeUnwantedComma(selectedTransIDsBuilder.toString()))


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

    private fun receiptcalculation() {
        if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {

            val collectionType =
                object :
                    TypeToken<ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>>() {}.type
            additemList = Gson().fromJson(
                prefs[Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY, ""],
                collectionType
            )

            getIssueReceiveDataFromPref()

            receiptCalculateAPI(
                true

            )
        } else {
            // called when date changed (for opening bal update according to date)
            getIssueReceiveDataFromPref()
            when (!selectedContactID.isNullOrBlank() && !binding.txtDateNewReceipt.text.toString()
                .isBlank()) {
                true -> {
                    receiptCalculateAPI(
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
            receiptLineList =
                Gson().fromJson(
                    prefs[Constants.PREF_SALES_LINE_INFO_KEY, ""],
                    collectionType
                )
            setupIssueReceiveAdapter()

        }
    }

    private fun setupIssueReceiveAdapter() {
        when (receiptLineList.size > 0) {
            true -> {
                binding.rvIssueReceiveList.visibility = View.VISIBLE
                issueReceiveadapter.apply {
                    addissueReceiveList(receiptLineList)
                    notifyDataSetChanged()
                }
            }
            else -> {

            }
        }

    }


    fun receiptCalculateAPI(
        showLoading: Boolean
    ) {
        if (NetworkUtils.isConnected()) {

            viewModel.getCalculateItemReceipt(
                loginModel.data?.bearer_access_token,
                Gson().toJson(additemList),
                selectedContactID,
                selectedContactType,
                transaction_id,
                binding.txtDateNewReceipt.text.toString(),
                Gson().toJson(receiptLineList)
            )
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {
                                    fill_itemReceipt_details_data(it.data)

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
                                if (!it.data?.errormessage?.message.isNullOrBlank()) {
                                    Toast.makeText(
                                        this,
                                        it.data?.errormessage?.message,
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                }


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

    private fun fill_itemReceipt_details_data(calculateReceiptModel: CalculationReceiptModel) {

        calculateReceiptModelMain = calculateReceiptModel


        if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {
            if (additemList.size > 0) {
                adapter.apply {
                    addReceiptItems(additemList)
                    notifyDataSetChanged()
                }
            }
        }

        binding.tvNewreceiptItemquantity.setText("Qty: " + calculateReceiptModel.data?.total_quantity)
        binding.tvNewreceiptGrossWt.setText("G: " + calculateReceiptModel.data?.total_gross_wt)
        binding.tvNewreceiptLessWt.setText("L: " + calculateReceiptModel.data?.total_less_wt)
        binding.tvNewreceiptNetWt.setText("N: " + calculateReceiptModel.data?.total_net_wt)
        binding.tvNewreceiptFineWt.setText("F: " + calculateReceiptModel.data?.total_fine_wt)

        binding.tvNewreceiptSilverItemquantity.setText("Qty: " + calculateReceiptModel.data?.silver_total_quantity)
        binding.tvNewreceiptSilverGrossWt.setText("G: " + calculateReceiptModel.data?.silver_total_gross_wt)
        binding.tvNewreceiptSilverLessWt.setText("L: " + calculateReceiptModel.data?.silver_total_less_wt)
        binding.tvNewreceiptSilverNetWt.setText("N: " + calculateReceiptModel.data?.silver_total_less_wt)
        binding.tvNewreceiptSilverFineWt.setText("F: " + calculateReceiptModel.data?.silver_total_fine_wt)

        binding.tvNewreceiptOtherItemquantity.setText("Qty: " + calculateReceiptModel.data?.other_total_quantity)
        binding.tvNewreceiptOtherGrossWt.setText("G: " + calculateReceiptModel.data?.other_total_gross_wt)
        binding.tvNewreceiptOtherLessWt.setText("L: 0.000")
        binding.tvNewreceiptOtherNetWt.setText("N: " + calculateReceiptModel.data?.other_total_net_wt)
        binding.tvNewreceiptOtherFineWt.setText("F: 0.000")

        //binding.tvNewreceiptMiscCharges.setText(Constants.AMOUNT_RS_APPEND + calculateReceiptModel.data?.total_misc_charges)
        binding.tvNewreceiptSubtotalCol1.setText(calculateReceiptModel.data?.total_fine_wt)
        binding.tvNewreceiptSubtotalCol2.setText(calculateReceiptModel.data?.final_total_amount)
        binding.tvNewreceiptSubtotalCol1Silver.setText(calculateReceiptModel.data?.silver_total_fine_wt)

        when (binding.tvNewreceiptSubtotalCol1.text) {
            "0.000"-> {
                binding.tvNewreceiptSubtotalCol1.setText(calculateReceiptModel.data?.total_fine_wt)
                binding.tvNewreceiptSubtotalCol1.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvNewreceiptSubtotalCol1.setText(calculateReceiptModel.data?.total_fine_wt + " " + subTotalTerm)
                if (subTotalTermValue.equals("credit")) {
                    binding.tvNewreceiptSubtotalCol1.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
                }
            }
        }

        when (binding.tvNewreceiptSubtotalCol2.text) {
            "0.00" -> {
                binding.tvNewreceiptSubtotalCol2.setText(calculateReceiptModel.data?.final_total_amount)
                binding.tvNewreceiptSubtotalCol1.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else->{
                binding.tvNewreceiptSubtotalCol2.setText(calculateReceiptModel.data?.final_total_amount + " " + subTotalTerm)
                if (subTotalTermValue.equals("credit")) {
                    binding.tvNewreceiptSubtotalCol2.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
                }
            }
        }

        when(binding.tvNewreceiptSubtotalCol1Silver.text){
            "0.000"->{
                binding.tvNewreceiptSubtotalCol1Silver.setText(calculateReceiptModel.data?.silver_total_fine_wt)
                binding.tvNewreceiptSubtotalCol1.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else->{
                binding.tvNewreceiptSubtotalCol1Silver.setText(calculateReceiptModel.data?.silver_total_fine_wt + " " + subTotalTerm)
                if (subTotalTermValue.equals("credit")) {
                    binding.tvNewreceiptSubtotalCol1Silver.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
                }
            }
        }



        binding.tvNewreceiptTotalDueGold.setText(calculateReceiptModel.data?.total_fine_wt_with_IRT)
        binding.tvNewreceiptTotalDueSilver.setText(calculateReceiptModel.data?.total_silver_fine_wt_with_IRT)
        binding.tvNewreceiptTotalDueCash.setText(Constants.AMOUNT_RS_APPEND + calculateReceiptModel.data?.grand_total)
        totalDue = calculateReceiptModel.data?.grand_total!!


        if (!calculateReceiptModel.data?.total_gross_wt.equals("0.000")) {
            binding.llNewreceiptMetalgold.visibility = View.VISIBLE
            //binding.tvNewreceiptSubtotallabel.visibility = View.VISIBLE
            /* binding.tvNewreceiptSubtotalCol1.visibility = View.VISIBLE
             binding.tvNewreceiptSubtotalCol2.visibility = View.VISIBLE*/
        } else {
            binding.llNewreceiptMetalgold.visibility = View.GONE
            /*binding.tvNewreceiptSubtotalCol1.visibility = View.GONE
            binding.tvNewreceiptSubtotalCol2.visibility = View.GONE*/
        }

        if (!calculateReceiptModel.data?.silver_total_gross_wt.equals("0.000")) {
            binding.llNewreceiptMetalsilver.visibility = View.VISIBLE
            // binding.llNewreceiptSubtotalSilver.visibility = View.VISIBLE
            /*binding.tvNewreceiptSubtotallabelSilver.visibility = View.VISIBLE
            binding.tvNewreceiptSubtotalCol1Silver.visibility = View.VISIBLE
            binding.tvNewreceiptSubtotalCol2Silver.visibility = View.VISIBLE*/
        } else {
            binding.llNewreceiptMetalsilver.visibility = View.GONE
            //binding.llNewreceiptSubtotalSilver.visibility = View.GONE
            /*binding.tvNewreceiptSubtotallabelSilver.visibility = View.GONE
            binding.tvNewreceiptSubtotalCol1Silver.visibility = View.GONE
            binding.tvNewreceiptSubtotalCol2Silver.visibility = View.GONE*/
        }

        if (!calculateReceiptModel.data?.other_total_gross_wt.equals("0.000")) {
            binding.llNewreceiptMetalother.visibility = View.VISIBLE
            // binding.llNewreceiptSubtotalOther.visibility = View.VISIBLE
            /*binding.tvNewreceiptSubtotallabelOther.visibility = View.VISIBLE
            binding.tvNewreceiptSubtotalCol1Other.visibility = View.VISIBLE
            binding.tvNewreceiptSubtotalCol2Other.visibility = View.VISIBLE*/

        } else {
            binding.llNewreceiptMetalother.visibility = View.GONE
            // binding.llNewreceiptSubtotalOther.visibility = View.GONE
            /*binding.tvNewreceiptSubtotallabelOther.visibility = View.GONE
            binding.tvNewreceiptSubtotalCol1Other.visibility = View.GONE
            binding.tvNewreceiptSubtotalCol2Other.visibility = View.GONE*/
        }

        updateUIofTotalDue(calculateReceiptModel)

        binding.linearCalculationViewReceipt.visibility = View.VISIBLE
        if (loginModel.data?.branch_info?.branch_type.equals("1", true)) {
            //GST branch
            binding.llNewreceiptMetalweights.visibility = View.GONE
            binding.llNewReceiptSubtotalRoot.visibility = View.GONE
        } else {
            binding.llNewreceiptMetalweights.visibility = View.VISIBLE
            binding.llNewReceiptSubtotalRoot.visibility = View.VISIBLE
        }
        // opening balance update method
        updateOpeningFineOpeningCash(calculateReceiptModel)
        updateClosingFineClosingCash(calculateReceiptModel)

    }


    private fun updateUIofTotalDue(calculateReceiptModel: CalculationReceiptModel) {

        if (!calculateReceiptModel.data?.total_fine_wt_with_IRT.equals("0.000") &&
            calculateReceiptModel.data?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewreceiptSilverTotaldue.visibility = View.GONE
            binding.tvNewreceiptTotaldueGoldLabel.visibility = View.VISIBLE
            binding.tvNewreceiptTotalDueGold.visibility = View.VISIBLE
        }

        if (!calculateReceiptModel.data?.total_fine_wt_with_IRT.equals("0.000") &&
            !calculateReceiptModel.data?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewreceiptSilverTotaldue.visibility = View.VISIBLE
            binding.tvNewreceiptTotaldueGoldLabel.visibility = View.VISIBLE
            binding.tvNewreceiptTotalDueGold.visibility = View.VISIBLE
        }



        if (calculateReceiptModel.data?.total_fine_wt_with_IRT.equals("0.000") &&
            !calculateReceiptModel.data?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewreceiptSilverTotaldue.visibility = View.GONE
            binding.tvNewreceiptTotaldueGoldLabel.setText("S: ")
            binding.tvNewreceiptTotalDueGold.setText(calculateReceiptModel.data?.total_silver_fine_wt_with_IRT)
            when (binding.tvNewreceiptTotalDueGold.text) {
                "0.000" -> {
                    binding.tvNewreceiptTotalDueGold.text =
                        calculateReceiptModel.data?.total_silver_fine_wt_with_IRT
                    binding.tvNewreceiptTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.header_black_text
                        )
                    )
                }

                else -> {
                    binding.tvNewreceiptTotalDueGold.text =
                        calculateReceiptModel.data?.total_silver_fine_wt_with_IRT + " " +
                                calculateReceiptModel.data?.total_silver_fine_wt_with_IRT_term
                    if (calculateReceiptModel.data?.total_silver_fine_wt_with_IRT_short_term.equals(
                            "Dr",
                            ignoreCase = true
                        )
                    ) {
                        binding.tvNewreceiptTotalDueGold.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.debit_color
                            )
                        )
                    } else
                        binding.tvNewreceiptTotalDueGold.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.credit_color
                            )
                        )
                }
            }

            when (binding.tvNewreceiptTotalDueCash.text) {
                "0.00" -> {
                    binding.tvNewreceiptTotalDueCash.text =
                        calculateReceiptModel.data?.grand_total
                    binding.tvNewreceiptTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.header_black_text
                        )
                    )
                }
                else -> {
                    binding.tvNewreceiptTotalDueCash.text =
                        calculateReceiptModel.data?.grand_total + " " +
                                calculateReceiptModel.data?.grand_total_term
                    if (calculateReceiptModel.data?.grand_total_short_term.equals(
                            "Dr",
                            ignoreCase = true
                        )
                    ) {
                        binding.tvNewreceiptTotalDueCash.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.debit_color
                            )
                        )
                    } else
                        binding.tvNewreceiptTotalDueCash.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.credit_color
                            )
                        )
                }
            }

        } else {
            binding.tvNewreceiptTotaldueGoldLabel.setText("G: ")
            updateTotalDuewithDrCr(calculateReceiptModel)
        }

        /*if(!calculateReceiptModel.data?.total_silver_fine_wt_with_IRT.equals("0.000")){
            binding.llNewinvoiceSilverTotaldue.visibility = View.VISIBLE
        }else{
            binding.llNewinvoiceSilverTotaldue.visibility = View.GONE
        }*/


        if (calculateReceiptModel.data?.total_fine_wt_with_IRT.equals("0.000") &&
            calculateReceiptModel.data?.total_silver_fine_wt_with_IRT.equals("0.000")
        ) {
            binding.llNewreceiptSilverTotaldue.visibility = View.GONE
            binding.tvNewreceiptTotaldueGoldLabel.visibility = View.GONE
            binding.tvNewreceiptTotalDueGold.visibility = View.GONE
        } else {
            //  binding.llNewinvoiceSilverTotaldue.visibility = View.VISIBLE
            //  tv_newinvoice_totaldue_gold_label.visibility = View.VISIBLE
            // tv_newinvoice_totalDue_gold.visibility = View.VISIBLE
        }
    }

    private fun updateTotalDuewithDrCr(calculateReceiptModel: CalculationReceiptModel) {
        when (binding.tvNewreceiptTotalDueGold.text) {
            "0.000" -> {
                binding.tvNewreceiptTotalDueGold.text =
                    calculateReceiptModel.data?.total_fine_wt_with_IRT
                binding.tvNewreceiptTotalDueGold.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvNewreceiptTotalDueGold.text =
                    calculateReceiptModel.data?.total_fine_wt_with_IRT + " " +
                            calculateReceiptModel.data?.total_fine_wt_with_IRT_term
                if (calculateReceiptModel.data?.total_fine_wt_with_IRT_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewreceiptTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewreceiptTotalDueGold.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        when (binding.tvNewreceiptTotalDueSilver.text) {
            "0.000" -> {
                binding.tvNewreceiptTotalDueSilver.text =
                    calculateReceiptModel.data?.total_silver_fine_wt_with_IRT
                binding.tvNewreceiptTotalDueSilver.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvNewreceiptTotalDueSilver.text =
                    calculateReceiptModel.data?.total_silver_fine_wt_with_IRT + " " +
                            calculateReceiptModel.data?.total_silver_fine_wt_with_IRT_term
                if (calculateReceiptModel.data?.total_silver_fine_wt_with_IRT_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewreceiptTotalDueSilver.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewreceiptTotalDueSilver.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        when (binding.tvNewreceiptTotalDueCash.text) {
            "0.00" -> {
                binding.tvNewreceiptTotalDueCash.text =
                    calculateReceiptModel.data?.grand_total
                binding.tvNewreceiptTotalDueCash.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvNewreceiptTotalDueCash.text =
                    calculateReceiptModel.data?.grand_total + " " +
                            calculateReceiptModel.data?.grand_total_term
                if (calculateReceiptModel.data?.grand_total_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvNewreceiptTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvNewreceiptTotalDueCash.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }
    }


    private fun updateClosingFineClosingCash(calculateReceiptModel: CalculationReceiptModel) {
        if (calculateReceiptModel.data?.closing_fine_balance!!.startsWith("-")) {
            val open_fine_bal: String =
                calculateReceiptModel.data?.closing_fine_balance.toString().trim().substring(1)
            binding.tvCloBalFineWtNewreceipt.text = open_fine_bal
        } else {
            binding.tvCloBalFineWtNewreceipt.text =
                calculateReceiptModel.data?.closing_fine_balance
        }

        when (binding.tvCloBalFineWtNewreceipt.text) {
            "0.000" -> {
                binding.tvCloBalFineWtNewreceipt.text =
                    calculateReceiptModel.data?.closing_fine_balance
                binding.tvCloBalFineWtNewreceipt.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvCloBalFineWtNewreceipt.text =
                    calculateReceiptModel.data?.closing_fine_balance + " " + calculateReceiptModel.data?.closing_fine_balance_term
                if (calculateReceiptModel.data?.closing_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvCloBalFineWtNewreceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvCloBalFineWtNewreceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        if (calculateReceiptModel.data?.closing_silver_fine_balance!!.startsWith("-")) {
            val open_fine_bal: String =
                calculateReceiptModel.data?.closing_silver_fine_balance.toString().trim()
                    .substring(1)
            binding.tvCloBalSilverNewreceipt.text = open_fine_bal
        } else {
            binding.tvCloBalSilverNewreceipt.text =
                calculateReceiptModel.data?.closing_silver_fine_balance
        }

        when (binding.tvCloBalSilverNewreceipt.text) {
            "0.000" -> {
                binding.tvCloBalSilverNewreceipt.text =
                    calculateReceiptModel.data?.closing_silver_fine_balance
                binding.tvCloBalSilverNewreceipt.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvCloBalSilverNewreceipt.text =
                    calculateReceiptModel.data?.closing_silver_fine_balance + " " + calculateReceiptModel.data?.closing_silver_fine_balance_term
                if (calculateReceiptModel.data?.closing_silver_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvCloBalSilverNewreceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvCloBalSilverNewreceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }


        if (calculateReceiptModel.data?.closing_cash_balance!!.startsWith("-")) {
            val open_cash_bal: String =
                calculateReceiptModel.data?.closing_cash_balance.toString().trim().substring(1)
            binding.tvCloBalCashNewreceipt.text = open_cash_bal
        } else {
            binding.tvCloBalCashNewreceipt.text =
                calculateReceiptModel.data?.closing_cash_balance
        }

        when (binding.tvCloBalCashNewreceipt.text) {
            "0.00" -> {
                binding.tvCloBalCashNewreceipt.text =
                    calculateReceiptModel.data?.closing_cash_balance
                binding.tvCloBalCashNewreceipt.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvCloBalCashNewreceipt.text =
                    calculateReceiptModel.data?.closing_cash_balance + " " + calculateReceiptModel.data?.closing_cash_balance_term
                if (calculateReceiptModel.data?.closing_cash_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvCloBalCashNewreceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvCloBalCashNewreceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }


        }

    }

    private fun updateOpeningFineOpeningCash(calculateReceiptModel: CalculationReceiptModel) {
        if (calculateReceiptModel.data?.opening_fine_balance!!.startsWith("-")) {
            val open_fine_bal: String =
                calculateReceiptModel.data?.opening_fine_balance.toString().trim().substring(1)
            binding.tvOpenBalFineNewReceipt.text = open_fine_bal
            binding.tvopenBalFineLabelReceipt.visibility = View.VISIBLE
        } else {
            binding.tvopenBalFineLabelReceipt.visibility = View.VISIBLE
            binding.tvOpenBalFineNewReceipt.text =
                calculateReceiptModel.data?.opening_fine_balance
        }


        when (binding.tvOpenBalFineNewReceipt.text) {
            "0.000" -> {
                binding.tvOpenBalFineNewReceipt.text =
                    calculateReceiptModel.data?.opening_fine_balance
                binding.tvOpenBalFineNewReceipt.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvOpenBalFineNewReceipt.text =
                    calculateReceiptModel.data?.opening_fine_balance + " " + calculateReceiptModel.data?.opening_fine_balance_term
                if (calculateReceiptModel.data?.opening_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvOpenBalFineNewReceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvOpenBalFineNewReceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }


        if (calculateReceiptModel.data?.opening_silver_fine_balance!!.startsWith("-")) {
            val open_fine_bal: String =
                calculateReceiptModel.data?.opening_silver_fine_balance.toString().trim()
                    .substring(1)
            binding.tvOpenBalSilverFineNewReceipt.text = open_fine_bal
            binding.tvopenBalSilverFineLabelReceipt.visibility = View.VISIBLE
        } else {
            binding.tvopenBalSilverFineLabelReceipt.visibility = View.VISIBLE
            binding.tvOpenBalSilverFineNewReceipt.text =
                calculateReceiptModel.data?.opening_silver_fine_balance
        }


        when (binding.tvOpenBalSilverFineNewReceipt.text) {
            "0.000" -> {
                binding.tvOpenBalSilverFineNewReceipt.text =
                    calculateReceiptModel.data?.opening_silver_fine_balance
                binding.tvOpenBalSilverFineNewReceipt.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvOpenBalSilverFineNewReceipt.text =
                    calculateReceiptModel.data?.opening_silver_fine_balance + " " + calculateReceiptModel.data?.opening_silver_fine_balance_term
                if (calculateReceiptModel.data?.opening_silver_fine_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvOpenBalSilverFineNewReceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvOpenBalSilverFineNewReceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }



        if (calculateReceiptModel.data?.opening_cash_balance!!.startsWith("-")) {
            val open_cash_bal: String =
                calculateReceiptModel.data?.opening_cash_balance.toString().trim().substring(1)
            binding.tvOpenBalCashNewReceipt.text = open_cash_bal
            binding.tvopenBalCashLabelReceipt.visibility = View.VISIBLE
        } else {
            binding.tvOpenBalCashNewReceipt.text =
                calculateReceiptModel.data?.opening_cash_balance
            binding.tvopenBalCashLabelReceipt.visibility = View.VISIBLE
        }

        when (binding.tvOpenBalCashNewReceipt.text) {
            "0.00" -> {
                binding.tvOpenBalCashNewReceipt.text =
                    calculateReceiptModel.data?.opening_cash_balance
                binding.tvOpenBalCashNewReceipt.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvOpenBalCashNewReceipt.text =
                    calculateReceiptModel.data?.opening_cash_balance + " " + calculateReceiptModel.data?.opening_cash_balance_term
                if (calculateReceiptModel.data?.opening_cash_balance_short_term.equals(
                        "Dr",
                        ignoreCase = true
                    )
                ) {
                    binding.tvOpenBalCashNewReceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvOpenBalCashNewReceipt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }


        }

    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                NewReceiptViewModel::class.java
            )
        binding.setLifecycleOwner(this)

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
        // tvRight.setText(R.string.save)
        tvTitle.setText(R.string.new_receipt)

        clearPref()

        if (loginModel.data?.branch_info?.branch_type.equals("1", true)) {
            // GST branch
            is_gst_applicable = "1"
            binding.cardAddLineNewReceipt.visibility = View.VISIBLE
            binding.cardAddItemeNewReceipt.visibility = View.GONE
            binding.cardAddLineNewReceiptNonGST.visibility = View.GONE


        } else { // NON-GST branch
            is_gst_applicable = "0"
            binding.cardAddItemeNewReceipt.visibility = View.VISIBLE
            binding.cardAddLineNewReceipt.visibility = View.GONE
            binding.cardAddLineNewReceiptNonGST.visibility = View.VISIBLE

        }

        debit_short_term = loginModel?.data!!.company_info!!.general_settings!!.debit_short_term!!
        credit_short_term = loginModel?.data!!.company_info!!.general_settings!!.credit_short_term!!
        binding.rvIssueReceiveList.layoutManager = LinearLayoutManager(this)
        issueReceiveadapter = IssueReceiveAdapter(
            arrayListOf(),
            "receipt",
            false,
            debit_short_term,
            credit_short_term
        )
        binding.rvIssueReceiveList.adapter = issueReceiveadapter

        binding.rvNewreceiptItem.layoutManager = LinearLayoutManager(this)
        adapter = NewReceiptItemAdapter(arrayListOf())
        binding.rvNewreceiptItem.adapter = adapter

        getDataFromIntent()

        binding.cardAddLineNewReceiptNonGST.clickWithDebounce {
            checkedRowNo = "1"
            when (selectedContactID.isNullOrBlank()) {
                true -> {
                    Toast.makeText(
                        this,
                        getString(R.string.select_contact_first_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                    txtContactNewReceipt.requestFocus()
                }
                else -> {

                    val dialog = BottomSheetDialog(this, R.style.BottomSheetDialog)

                    val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
                    dialog.setContentView(view)

                    if (loginModel.data?.branch_info?.branch_type.equals("1", true)) {

                        view.rbCashRecAddSaleLine.visibility = View.VISIBLE
                        view.rbCashPayAddSaleLine.visibility = View.GONE
                        view.rbBankRecAddSaleLine.visibility = View.VISIBLE
                        view.rbBankPayAddSaleLine.visibility = View.GONE
                        view.rbMetalRecAddSaleLine.visibility = View.GONE
                        view.rbMetalPayAddSaleLine.visibility = View.GONE
                        view.rbRateCutAddSaleLine.visibility = View.GONE
                        view.rbAdjustAddSaleLine.visibility = View.VISIBLE
                    }
                    //Non Gst
                    else {
                        view.rbCashRecAddSaleLine.visibility = View.VISIBLE
                        view.rbCashPayAddSaleLine.visibility = View.GONE
                        view.rbBankRecAddSaleLine.visibility = View.VISIBLE
                        view.rbBankPayAddSaleLine.visibility = View.GONE
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

        binding.cardAddLineNewReceipt.clickWithDebounce {
            checkedRowNo = "1"
            when (selectedContactID.isNullOrBlank()) {
                true -> {
                    Toast.makeText(
                        this,
                        getString(R.string.select_contact_first_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                    txtContactNewReceipt.requestFocus()
                }
                else -> {
                    val dialog = BottomSheetDialog(this, R.style.BottomSheetDialog)

                    val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
                    dialog.setContentView(view)

                    if (loginModel.data?.branch_info?.branch_type.equals("1", true)) {

                        view.rbCashRecAddSaleLine.visibility = View.VISIBLE
                        view.rbCashPayAddSaleLine.visibility = View.GONE
                        view.rbBankRecAddSaleLine.visibility = View.VISIBLE
                        view.rbBankPayAddSaleLine.visibility = View.GONE
                        view.rbMetalRecAddSaleLine.visibility = View.GONE
                        view.rbMetalPayAddSaleLine.visibility = View.GONE
                        view.rbRateCutAddSaleLine.visibility = View.GONE
                        view.rbAdjustAddSaleLine.visibility = View.VISIBLE
                    }
                    //Non Gst
                    else {
                        view.rbCashRecAddSaleLine.visibility = View.VISIBLE
                        view.rbCashPayAddSaleLine.visibility = View.GONE
                        view.rbBankRecAddSaleLine.visibility = View.VISIBLE
                        view.rbBankPayAddSaleLine.visibility = View.GONE
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

        binding.btnSaveAddReceipt?.clickWithDebounce() {
            if (performValidation()) {
                if (NetworkUtils.isConnected()) {
                    when (is_From_Edit) {
                        true -> {
                            editReceiptRequestBodyParamCallAPI()
                        }
                        false -> {
                            addNewReceiptRequestBodyParamCallAPI()
                        }
                    }

                }
            }
        }

        binding.llNewReceipt.setOnFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                CommonUtils.hideKeyboardnew(this);
            }
        })
        //set todays date by default

        when (is_From_Edit) {
            false -> {
                autogenerateinvoice(true)
            }
            else -> {

            }
        }


        imgLeft?.clickWithDebounce {
            onBackPressed()
        }
        txtContactNewReceipt.doAfterTextChanged { /*selectedContactID = ""*/ }
        binding.tvnewReceiptUploadphoto?.clickWithDebounce {

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

        binding.ivnewReceiptAttachment?.clickWithDebounce {
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



        binding.cardAddItemeNewReceipt.clickWithDebounce {
            when (selectedContactID.isNullOrBlank()) {
                true -> {
                    Toast.makeText(
                        this,
                        getString(R.string.select_contact_first_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                    txtContactNewReceipt.requestFocus()
                }
                else -> {
                    startActivity(
                        Intent(this, AddItemActivity::class.java).putExtra(
                            Constants.TRANSACTION_TYPE,
                            "receipt"
                        )
                    )
                }
            }

        }

        checkAgainstRefNewReceipt.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> {
                    is_reference = 1
                    tvRefNoNewReceipt.visibility = View.VISIBLE
                }
                false -> {
                    is_reference = 0
                    prefs.edit().remove(Constants.Receipt_Ref_Selected_Trans_Ids).apply()
                    prefs.edit().remove(Constants.Receipt_Ref_Selected_Invoice_Nos).apply()
                    tvRefNoNewReceipt.visibility = View.GONE
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
            if (intent.extras?.containsKey(Constants.RECEIPT_DETAIL_KEY)!!) {
                var group_str: String? = intent.getStringExtra(Constants.RECEIPT_DETAIL_KEY)
                receiptDetailModel =
                    Gson().fromJson(
                        group_str,
                        ReceiptDetailModel.Data::class.java
                    )
                tvTitle.setText(R.string.edit_receipt)
                is_From_Edit = true

                transaction_id = receiptDetailModel.transactionData?.transaction_id!!
                selectedContactID = receiptDetailModel.transactionData?.contact_id
                selectedContactType = receiptDetailModel.transactionData?.ledger_contact_type
                binding.txtContactNewReceipt.setText(receiptDetailModel.transactionData?.display_name)
                binding.txtDateNewReceipt.setText(receiptDetailModel.transactionData?.transaction_date)
                binding.txtReceiptNewReceipt.setText(receiptDetailModel.transactionData?.invoice_number)
                is_series = receiptDetailModel.transactionData?.invoice_number.toString().trim()
                binding.remarkdNewReceipt.setText(receiptDetailModel.transactionData?.remarks)
                binding.txtRefNoNewReceipt.setText(receiptDetailModel.transactionData?.reference)

                itemListFormDetail = receiptDetailModel.transactionData?.item!!

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

                if (receiptDetailModel.transactionData!!.image != null && receiptDetailModel.transactionData!!.image?.size!! > 0) {
                    binding.tvnewReceiptUploadphoto.visibility = View.GONE
                    binding.ivnewReceiptAttachment.visibility = View.VISIBLE
                    imageURL = receiptDetailModel.transactionData!!.image?.get(0)?.image
                    Glide.with(this).load(imageURL).circleCrop()
                        .placeholder(R.drawable.ic_user_placeholder)
                        .into(binding.ivnewReceiptAttachment)

                    /* val imageFile: File = File(imageURL)

                     val fileBody: RequestBody =
                         RequestBody.create(MediaType.parse("multipart/form-data"), imageFile)
                     multipartImageBody =
                         MultipartBody.Part.createFormData("image[]", imageFile.name, fileBody)
                     Log.v("imageFile", "" + imageFile.name)*/

                } else {
                    binding.tvnewReceiptUploadphoto.visibility = View.VISIBLE
                }

                binding.tvNewreceiptItemquantity.setText("Qty: " + receiptDetailModel.transactionData?.total_quantity)
                binding.tvNewreceiptGrossWt.setText("G: " + receiptDetailModel.transactionData?.total_gross_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                binding.tvNewreceiptLessWt.setText("L: " + receiptDetailModel.transactionData?.total_less_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                binding.tvNewreceiptNetWt.setText("N: " + receiptDetailModel.transactionData?.total_net_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                binding.tvNewreceiptFineWt.setText("F: " + receiptDetailModel.transactionData?.total_fine_wt /*+ Constants.WEIGHT_GM_APPEND*/)
                //binding.tvNewreceiptMiscCharges.setText(Constants.AMOUNT_RS_APPEND + receiptDetailModel.transactionData?.total_misc_charges)

                binding.tvNewreceiptSilverItemquantity.setText("Qty: " + receiptDetailModel.transactionData?.silver_total_quantity)
                binding.tvNewreceiptSilverGrossWt.setText("G: " + receiptDetailModel.transactionData?.silver_total_gross_wt)
                binding.tvNewreceiptSilverLessWt.setText("L: " + receiptDetailModel.transactionData?.silver_total_less_wt)
                binding.tvNewreceiptSilverNetWt.setText("N: " + receiptDetailModel.transactionData?.silver_total_less_wt)
                binding.tvNewreceiptSilverFineWt.setText("F: " + receiptDetailModel.transactionData?.silver_total_fine_wt)

                binding.tvNewreceiptOtherItemquantity.setText("Qty: " + receiptDetailModel.transactionData?.other_total_quantity)
                binding.tvNewreceiptOtherGrossWt.setText("G: " + receiptDetailModel.transactionData?.other_total_gross_wt)
                binding.tvNewreceiptOtherLessWt.setText("L: 0.000")
                binding.tvNewreceiptOtherNetWt.setText("N: " + receiptDetailModel.transactionData?.other_total_net_wt)
                binding.tvNewreceiptOtherFineWt.setText("F: 0.000")

                binding.tvNewreceiptSubtotalCol1.setText(receiptDetailModel.transactionData?.total_fine_wt)
                binding.tvNewreceiptSubtotalCol2.setText(receiptDetailModel.transactionData?.final_total_amount)
                binding.tvNewreceiptSubtotalCol1Silver.setText(receiptDetailModel.transactionData?.silver_total_fine_wt)

                when (binding.tvNewreceiptSubtotalCol1.text) {
                    "0.000"-> {
                        binding.tvNewreceiptSubtotalCol1.setText(receiptDetailModel.transactionData?.total_fine_wt)
                        binding.tvNewreceiptSubtotalCol1.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.header_black_text
                            )
                        )
                    }
                    else -> {
                        binding.tvNewreceiptSubtotalCol1.setText(receiptDetailModel.transactionData?.total_fine_wt + " " + subTotalTerm)
                        if (subTotalTermValue.equals("credit")) {
                            binding.tvNewreceiptSubtotalCol1.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.credit_color
                                )
                            )
                        }
                    }
                }

                when (binding.tvNewreceiptSubtotalCol2.text) {
                    "0.00" -> {
                        binding.tvNewreceiptSubtotalCol2.setText(receiptDetailModel.transactionData?.final_total_amount)
                        binding.tvNewreceiptSubtotalCol2.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.header_black_text
                            )
                        )
                    }
                        else->{
                            binding.tvNewreceiptSubtotalCol2.setText(receiptDetailModel.transactionData?.final_total_amount + " " + subTotalTerm)
                            if (subTotalTermValue.equals("credit")) {
                                binding.tvNewreceiptSubtotalCol2.setTextColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.credit_color
                                    )
                                )
                            }
                        }
                    }

                when(binding.tvNewreceiptSubtotalCol1Silver.text){
                    "0.000"->{
                        binding.tvNewreceiptSubtotalCol1Silver.setText(receiptDetailModel.transactionData?.silver_total_fine_wt)
                        binding.tvNewreceiptSubtotalCol1Silver.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.header_black_text
                            )
                        )
                    }
                    else->{
                        binding.tvNewreceiptSubtotalCol1Silver.setText(receiptDetailModel.transactionData?.silver_total_fine_wt + " " + subTotalTerm)
                        if (subTotalTermValue.equals("credit")) {
                            binding.tvNewreceiptSubtotalCol1Silver.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.credit_color
                                )
                            )
                        }
                    }
                }

                    binding.tvNewreceiptTotalDueGold.setText(receiptDetailModel.transactionData?.total_fine_wt_with_IRT)
                            binding . tvNewreceiptTotalDueSilver . setText (receiptDetailModel.transactionData?.total_silver_fine_wt_with_IRT)
                            binding . tvNewreceiptTotalDueCash . setText (Constants.AMOUNT_RS_APPEND + receiptDetailModel.transactionData?.grand_total)
                            totalDue = receiptDetailModel . transactionData ?. grand_total !!

                }
            }
        }

        private fun addIRTDatainPref() {
            receiptLineList.clear()
            for (i in 0 until receiptDetailModel.IRTData!!.size) {

                if (!receiptDetailModel.IRTData!!.get(i).transaction_type.equals("")) {

                    val saleIRTModel = SalesLineModel.SaleLineModelDetails(
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.cash_amount,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.cash_ledger,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.cash_ledger_name,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.cash_description,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.bank_amount,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.bank_ledger,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.bank_ledger_name,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.bank_mode,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.cheque_number,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.cheque_date,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.favouring_name,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.deuct_charges,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.deuct_charges_percentage,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.bank_final_amt,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.recipient_bank,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.account_no,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.ifs_code,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.utr_number,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.bank_description,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.rcm_gold_rate,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.rate_cut_amount,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.rate_cut_fine_term,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.metal_type_id_rate_cut,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.rate_cut_fine,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.item_id,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.item_name,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.metal_type_id_metal,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.maintain_stock_in_name_metal,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.gross_wt,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.less_wt,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.net_wt,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.touch,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.wast,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.fine_wt,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.adjustment_fine,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.metal_type_id_adjustments,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.adjustment_amount,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.adjustment_ledger,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.adjustment_ledger_name,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.adjustment_description,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.type,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.transaction_type,
                        receiptDetailModel.IRTData!!.get(i).IRTDetails!!.transaction_title

                    )

                    receiptLineList.add(saleIRTModel)
                }
            }

            prefs[Constants.PREF_SALES_LINE_INFO_KEY] = Gson().toJson(receiptLineList)
        }

        private fun clearPref() {
            if (prefs.contains(Constants.PREF_SALES_LINE_INFO_KEY)) {
                prefs.edit().remove(Constants.PREF_SALES_LINE_INFO_KEY).apply()
            }
            if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {
                prefs.edit().remove(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY).apply()
            }
        }

        fun performValidation(): Boolean {
            if (txtContactNewReceipt.text.toString().isBlank()) {
                CommonUtils.showDialog(
                    this,
                    getString(R.string.select_contact_first_msg)/*"Please Select Contact Name"*/
                )
                txtContactNewReceipt.requestFocus()
                return false
            } else if (txtReceiptNewReceipt.text.toString().isBlank()) {
                CommonUtils.showDialog(
                    this,
                    getString(R.string.enter_invoice_msg)/*"Please Enter Invoice"*/
                )
                txtReceiptNewReceipt.requestFocus()
                return false
            } /*else if (checkAgainstRefNewReceipt.isChecked && selectedInvoiceNoList.size.equals(0)) {
            CommonUtils.showDialog(
                this,
                getString(R.string.select_reference_number_msg)*//*"Please Select Reference No."*//*
            )
            txtRefNoNewReceipt.requestFocus()
            return false
        }*/ /*else if (is_gst_applicable == "0" && !prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {
            CommonUtils.showDialog(this, getString(R.string.add_an_item_msg))
            return false
        } */ /*else if (*//*is_gst_applicable == "1" &&*//* totalDue == "0.00") {
                CommonUtils.showDialog(this, getString(R.string.total_due_msg))
                return false
            }*/
            return true
        }

        fun getSearchContactsLedger() {
            contactList = ArrayList<SearchContactLedgerModel.Data.Contact>()
            contactNameList = ArrayList<String>()
            viewModel.getSearchContactsLedger(
                loginModel.data?.bearer_access_token

            ).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            Log.v("..setupObservers..", "..Success...")
                            if (it.data?.status == true) {
                                contactList = it.data.data?.contact

                                contactNameList = contactList?.map { it.contact_name.toString() }

                                contactNameAdapter = ArrayAdapter<String>(
                                    this,
                                    android.R.layout.simple_dropdown_item_1line,
                                    contactNameList!!
                                )
                                binding.txtContactNewReceipt.setAdapter(contactNameAdapter)
                                binding.txtContactNewReceipt.threshold = 1

                                binding.txtContactNewReceipt.setOnItemClickListener { adapterView, _, position, _
                                    ->
                                    val selected: String =
                                        adapterView.getItemAtPosition(position).toString()
                                    val pos: Int? = contactNameList?.indexOf(selected)

                                    selectedContactID =
                                        pos?.let { it1 -> contactList?.get(it1)?.contact_id }
                                    selectedContactType =
                                        pos?.let { it1 -> contactList?.get(it1)?.contact_type }
//                                selectedPlaceOfSupply = pos?.let { it1 -> contactList?.get(it1)?.place_of_supply }
//                                selectedPlaceOfSupplyID = pos?.let { it1 -> contactList?.get(it1)?.state_id }
                                    autogenerateinvoice(false)
                                    receiptcalculation()
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


        fun getInvoiceNumber() {
            viewModel.getReceiptInvoiceNumber(
                loginModel?.data?.bearer_access_token,
                txtDateNewReceipt.text.toString(),
                transaction_id
            ).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            /*   Log.v("..setupObservers..", "..Success...")*/
                            if (it.data?.status == true) {
                                isNoGenerated = true
                                txtReceiptNewReceipt.setText(/*it.data.data?.prefix + */it.data.data?.series /*+ '-' + it.data.data?.suffix*/)

                                /* is_prefix = it.data.data?.prefix*/
                                is_series = it.data.data?.series
                                /* is_suffix = it.data.data?.suffix*/


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
                            Toast.makeText(this, it.data?.errormessage?.message, Toast.LENGTH_LONG)
                                .show()
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


                        if (additemList.size > 0) {
                            prefs[Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY] =
                                Gson().toJson(additemList)
                            receiptcalculation()
                        } else {
                            prefs.edit().remove(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY).apply()
                            binding.rvNewreceiptItem.visibility = View.GONE
                            receiptCalculateAPI(true)
                        }
                    }
                }
            }
        }


        private fun autogenerateinvoice(isFromOnCreate: Boolean) {
            when (isFromOnCreate) {
                true -> {
                    txtDateNewReceipt.setText(SimpleDateFormat("dd-MMM-yy").format(Date()))
                }
                else -> {

                }
            }

            binding.txtDateNewReceipt.clickWithDebounce {
                openDatePicker(true)
            }
        }


        fun openDatePicker(isFromDate: Boolean) {

            //val c = Calendar.getInstance()
            // val c = Calendar.getInstance()
            if (isFromDate) {
                val sdf = SimpleDateFormat("dd-MMM-yy")
                val parse = sdf.parse(txtDateNewReceipt.text.toString())
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
                    binding.txtDateNewReceipt.setText(
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

                    receiptcalculation()
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

                binding.tvnewReceiptUploadphoto.visibility = View.GONE
                binding.ivnewReceiptAttachment.visibility = View.VISIBLE
                isPhotoSelected = true
                Glide.with(this).load(fileUri).circleCrop().into(binding.ivnewReceiptAttachment)

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

        private fun addNewReceiptRequestBodyParamCallAPI() {

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
                receiptLineList =
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
                    "", "", "", "",
                    "", "", ""
                )
                receiptLineList.add(childModel)
            }

            val transaction_type_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                "3"
            )

            val transaction_type_name: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                "Receipt"
            )

            val transaction_date: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.txtDateNewReceipt.text.toString()
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
                binding.remarkdNewReceipt.text.toString()
            )

            val issue_receive_transaction: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                Gson().toJson(receiptLineList)
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
                binding.txtRefNoNewReceipt.text.toString()
            )
            val is_gst_applicabl: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                is_gst_applicable
            )

            addNewReceiptAPI(
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

        fun addNewReceiptAPI(
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

            viewModel.addNewReceipt(
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

        private fun editReceiptRequestBodyParamCallAPI() {
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
                receiptLineList =
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
                    "", "", "", "",
                    "", "", ""
                )
                receiptLineList.add(childModel)
            }

            val transaction_type_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                "3"
            )

            val transaction_type_name: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                "Receipt"
            )

            val transaction_date: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.txtDateNewReceipt.text.toString()
            )

            val transaction_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                transaction_id
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
                binding.remarkdNewReceipt.text.toString()
            )

            val issue_receive_transaction: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                Gson().toJson(receiptLineList)
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
                binding.txtRefNoNewReceipt.text.toString()
            )

            val is_gst_applicabl: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                is_gst_applicable
            )
            editReceiptAPI(
                loginModel?.data?.bearer_access_token,
                transaction_id,
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

        fun editReceiptAPI(
            token: String?,
            transaction_id: RequestBody?,
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

            viewModel.editReceipt(
                token,
                transaction_id,
                transaction_type_id,
                transaction_type_name,
                transaction_date,
                contact_id,
                ledger_contact_type,
                invoice_number,
                item_json,
                issue_receive_transaction,
                is_gst_applicable,
                party_po_no,
                reference,
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
                if (receiptLineList != null && receiptLineList.size > 0) {
                    if (index >= receiptLineList.size) {
                        //index not exists
                    } else {
                        // index exists
                        startActivity(
                            Intent(this@NewReceiptActivity, AddCashBankActivity::class.java)
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
                if (receiptLineList != null && receiptLineList.size > 0) {
                    if (index >= receiptLineList.size) {
                        //index not exists
                    } else {
                        // index exists
                        receiptLineList.removeAt(index)


                        if (receiptLineList.size > 0) {
                            prefs[Constants.PREF_SALES_LINE_INFO_KEY] =
                                Gson().toJson(receiptLineList)
                        } else {
                            prefs.edit().remove(Constants.PREF_SALES_LINE_INFO_KEY).apply()
                            binding.rvIssueReceiveList.visibility = View.GONE
                        }
                        receiptcalculation()
                    }
                }
            }
        }

        fun enableBtnsHideProgress() {
            CommonUtils.hideProgress()
            binding.btnSaveAddReceipt.isEnabled = true
            binding.cardAddItemeNewReceipt.isEnabled = true

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