package com.goldbookapp.ui.activity.sales

import android.app.DatePickerDialog
import android.content.Intent
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
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.AddCashbankActivityBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.viewmodel.CashPayRecViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.ItemDetailsAdapter
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.add_cashbank_activity.*
import kotlinx.android.synthetic.main.new_invoice_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class AddCashBankActivity : AppCompatActivity() {

    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    lateinit var dashboardModel: DashboardDetailsModel

    lateinit var binding: AddCashbankActivityBinding
    private lateinit var viewModel: CashPayRecViewModel
    lateinit var popupMenu: PopupMenu


    var itemList: ArrayList<ItemSearchModel.ItemSearch>? = null
    var itemNameList: List<String>? = null
    lateinit var ItemDetailsAdapter: ItemDetailsAdapter
    var selectedItemID: String? = null
    var selectedItemName: String? = null
    var selectedMetalItemId: String? = null
    var selectedItemMaintainStockName: String? = null

    lateinit var receivedIssueReceive: SalesLineModel.SaleLineModelDetails
    var receivedIssuePosition: Int = -1

    var selectedFineDefaultTermName: String = ""
    var selectedFineDefaultTermValue: String = ""
    var selectedAmtDefaultTermName: String = ""
    var selectedAmtDefaultTermValue: String = ""
    var selectedCashLedgerID: String = ""
    var selectedCashLedgerName: String = ""
    var ledgerSalesNameList: List<String>? = arrayListOf()
    var ledgerSalesList: List<SearchLedgerModel.LedgerDetails>? = null

    var fineDefaultTermNameList: List<String>? = arrayListOf()
    var fineDefaultTermList: List<ItemDefaultTermModel.Data.Default_term>? = null

    var checkedRowNo: String? = "0"
    var checkedBankMode: String? = "0"
    var selectedRateCut: String? = ""
    var selectedMetalRateCut: String? = "1"
    var selectedMetalAdjustment: String? = ""
    var wastage: BigDecimal = BigDecimal(0)
    var wastageUpdatedValue: String = "0.00"
    var touchUpdatedValue: String = "0.00"
    var grossUpdatedValue: String = "0.000"
    var lesswtUpdatedValue: String = "0.000"
    var goldRateUpdatedValue: String = "0.00"
    var updatedgoldRateValue: String = "0.00"
    var fineRateCutUpdatedValue: String = "0.000"
    var amtRateCutUpdatedValue: String = "0.00"
    var amtCashUpdatedValue: String = "0.00"
    var amtBankUpdatedValue: String = "0.00"
    var deductChargesUpdatedValue: String = "0.00"
    var chargesPerUpdatedValue: String = "0.00"
    var finalAmtUpdatedValue: String = "0.00"
    var fineAdjustUpdatedValue: String = "0.000"
    var amtAdjustUpdatedValue: String = "0.00"

    val df = DecimalFormat("0.00")
    val df1 = DecimalFormat("0.000")
    var addsaleLineList = ArrayList<SalesLineModel.SaleLineModelDetails>()
    lateinit var childItemModel: SalesLineModel.SaleLineModelDetails
    var is_gst_applicable: Boolean = false
    var is_From_FirstTime_Fine: Boolean = false
    var is_From_FirstTime_Amt: Boolean = false
    var is_From_FirstTime_Gold: Boolean = false
    var is_From_edit: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.add_cashbank_activity)

        setupViewModel()
        setupUIandListner()

        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent) {
                handleConnectivityChange()
            }
        })

    }


    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                CashPayRecViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected) {
            // Network is available
            CommonUtils.hideInternetDialog()

        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }

    override fun onResume() {
        super.onResume()
        // getLedgerdd("cash")
        getSearchItem()

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

        //gst branch
        if (loginModel.data?.branch_info?.branch_type.equals("1", true)) {
            is_gst_applicable = true
        } else {
            is_gst_applicable = false
        }

        imgLeft.setImageResource(R.drawable.ic_back)

        if (intent.extras != null && intent.extras!!.containsKey(Constants.SaleLine_Row_No)) {
            checkedRowNo = intent.getStringExtra(Constants.SaleLine_Row_No)
            visibleUIAccordingToPaymentRowNo(checkedRowNo, true)
        }
        if (intent.extras != null && intent.extras!!.containsKey(Constants.ISSUE_RECEIVE_MODEL)) {
            val issue_str: String? = intent.getStringExtra(Constants.ISSUE_RECEIVE_MODEL)
            receivedIssueReceive =
                Gson().fromJson(
                    issue_str,
                    SalesLineModel.SaleLineModelDetails::class.java
                )
            receivedIssuePosition = intent.getIntExtra(Constants.ISSUE_RECEIVE_POS, -1)
            fillAllIssueReceiveTransactions()
            visibleUIAccordingToPaymentRowNo(checkedRowNo, false)
            is_From_edit = true
        }
        val sdf = SimpleDateFormat("dd-MMM-yy")
        val currentDate = sdf.format(Date())
        binding.txtDateAddSaleLine.setText(currentDate)

        imgLeft?.clickWithDebounce {
            onBackPressed()
        }

        applyingDigitFilter()
        onTextChangedSetup()
        onFocusChangeSetup()

        txtFineRateCutTerm.clickWithDebounce {
            openFineTermMenu(fineDefaultTermNameList, binding.txtFineRateCutTerm)
        }
        txtAmtRateCutTerm.clickWithDebounce {
            openAmtTermMenu(fineDefaultTermNameList, binding.txtAmtRateCutTerm)
        }

        txtLedgerAddSaleLine.clickWithDebounce {
            openCashLedgerMenu(ledgerSalesNameList, binding.txtLedgerAddSaleLine)
        }

        txtLedgerBankAddSaleLine.clickWithDebounce {
            openCashLedgerMenu(ledgerSalesNameList, binding.txtLedgerBankAddSaleLine)
        }

        txtLedgerAdjustAddSaleLine.clickWithDebounce {
            openCashLedgerMenu(ledgerSalesNameList, binding.txtLedgerAdjustAddSaleLine)
        }

        binding.btnCardSavenAddNewAddFinemetal.clickWithDebounce {
            if (performValidation()) {
                clearFocus()
                saveSalesLineModel()
                startActivity(
                    Intent(
                        this,
                        AddCashBankActivity::class.java
                    ).putExtra(Constants.SaleLine_Row_No, checkedRowNo)
                )
                finish()
            }

        }



        rgAddSaleMode.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                rbRtgsNeftAddSaleLine.id -> {
                    checkedBankMode = "e-transfer"
                    lyRTGSBankMode.visibility = View.VISIBLE
                    lyChequeBankMode.visibility = View.GONE
                    tvDescriptionAddSaleLine.visibility = View.GONE
                }

                rbChequeAddSaleLine.id -> {
                    checkedBankMode = "cheque"
                    lyRTGSBankMode.visibility = View.GONE
                    lyChequeBankMode.visibility = View.VISIBLE
                    tvDescriptionAddSaleLine.visibility = View.GONE
                }
                rbOthersAddSaleLine.id -> {
                    checkedBankMode = "other"
                    lyRTGSBankMode.visibility = View.GONE
                    lyChequeBankMode.visibility = View.GONE
                    tvDescriptionAddSaleLine.visibility = View.VISIBLE
                }

            }
        })


        radiogroupRatCut.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                radioGoldRateCut.id -> {
                    selectedMetalRateCut = "1"
                }
                radioSilverRateCut.id -> {
                    selectedMetalRateCut = "2"
                }
            }
        })

        radiogroupAdjustment.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                radioGoldAdjustment.id -> {
                    selectedMetalAdjustment = "1"
                }
                radioSilverAdjustment.id -> {
                    selectedMetalAdjustment = "2"
                }
            }
        })

        /*  radiogroupAddRateCutFineMet.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
              when (checkedId) {
                  radioRateCutFine.id -> {
                      selectedRateCut = "fine"
                      binding.txtAmountRatecutFine.isEnabled = false
                      binding.txtFineWtRateCutFine.isEnabled = true
                      binding.txtFineWtRateCutFine.isFocusable = true
                      binding.txtFineWtRateCutFine.text!!.clear()
                      binding.txtAmountRatecutFine.text!!.clear()

                  }
                  radioRateCutMet.id -> {
                      selectedRateCut = "amount"
                      binding.txtFineWtRateCutFine.text!!.clear()
                      binding.txtAmountRatecutFine.text!!.clear()
                      binding.txtAmountRatecutFine.isEnabled = true
                      binding.txtAmountRatecutFine.isFocusable = true
                      binding.txtFineWtRateCutFine.isEnabled = false
                  }
              }
          })
  */
        binding.txtDateAddSaleLine.clickWithDebounce {
            openDatePicker(true)
        }

        binding.btnCardSavenCloseAddFinemetal.clickWithDebounce {
            if (performValidation()) {
                clearFocus()
                saveSalesLineModel()
                finish()
            }

        }

    }

    fun performValidation(): Boolean {
        if ((checkedRowNo == "1" || checkedRowNo == "2") && txtAmountAddSaleLine.text.toString()
                .isBlank()
        ) {
            CommonUtils.showDialog(this, "Please Enter Amount")
            txtAmountAddSaleLine.requestFocus()
            return false
        } else if ((checkedRowNo == "1" || checkedRowNo == "2") && selectedCashLedgerID.isNullOrBlank()) {
            CommonUtils.showDialog(this, "Please Select Ledger")
            txtLedgerAddSaleLine.requestFocus()
            return false
        } else if ((checkedRowNo == "3" || checkedRowNo == "4") && txtAmountBankAddSaleLine.text.toString()
                .isBlank()
        ) {
            CommonUtils.showDialog(this, "Please Enter Amount")
            txtAmountBankAddSaleLine.requestFocus()
            return false
        } else if ((checkedRowNo == "3" || checkedRowNo == "4") && selectedCashLedgerID.isNullOrBlank()) {
            CommonUtils.showDialog(this, "Please Select Ledger")
            txtLedgerBankAddSaleLine.requestFocus()
            return false
        } else if ((checkedRowNo == "5" || checkedRowNo == "6") && selectedItemID.isNullOrBlank()) {
            CommonUtils.showDialog(this, "Please Select Item")
            txtItemNameAddItemFineMetal.requestFocus()
            return false
        } else if ((checkedRowNo == "5" || checkedRowNo == "6") && txtGrossWtAddFineMe.text.toString()
                .isBlank()
        ) {
            CommonUtils.showDialog(this, "Please Enter Gross Weight")
            txtGrossWtAddFineMe.requestFocus()
            return false
        } else if ((checkedRowNo == "5" || checkedRowNo == "6") && txtTouchAddFineMe.text.toString()
                .isBlank()
        ) {
            CommonUtils.showDialog(this, "Please Enter Touch")
            txtTouchAddFineMe.requestFocus()
            return false
        } else if (checkedRowNo == "7" && txtGoldrateRateCutFine.text.toString()
                .isBlank()
        ) {
            CommonUtils.showDialog(this, "Please Enter Gold Rate")
            txtGoldrateRateCutFine.requestFocus()
            return false
        } else if (checkedRowNo == "7" && txtFineWtRateCutFine.text.toString()
                .isBlank()
        ) {
            CommonUtils.showDialog(this, "Please Enter Fine Weight")
            txtFineWtRateCutFine.requestFocus()
            return false
        } else if (checkedRowNo == "7" && txtGoldrateRateCutFine.text.toString()
                .isBlank()
        ) {
            CommonUtils.showDialog(this, "Please Enter Gold Rate")
            txtGoldrateRateCutFine.requestFocus()
            return false
        } else if (checkedRowNo == "7" && txtAmountRatecutFine.text.toString()
                .isBlank()
        ) {
            CommonUtils.showDialog(this, "Please Enter Amount")
            txtAmountRatecutFine.requestFocus()
            return false
        } else if (checkedRowNo == "8" && is_gst_applicable == false && binding.txtFineAdjustAddSaleLine.text.toString()
                .isBlank() && binding.txtAmountAdjustAddSaleLine.text.toString().isBlank()
        ) {
            CommonUtils.showDialog(this, "Please Enter Fine Weight or Amount")
            binding.txtFineAdjustAddSaleLine.requestFocus()
            return false
        } else if (checkedRowNo == "8" && is_gst_applicable == true && binding.txtAmountAdjustAddSaleLine.text.toString()
                .isBlank()
        ) {
            CommonUtils.showDialog(this, "Please Enter Amount")
            binding.txtAmountAdjustAddSaleLine.requestFocus()
            return false
        }
        return true
    }

    private fun clearFocus() {
        binding.txtAmountAddSaleLine.clearFocus()
        binding.txtDeductChargAddSaleLine.clearFocus()
        binding.txtAmountBankAddSaleLine.clearFocus()
        binding.txtChargePerAddSaleLine.clearFocus()
        binding.txtAmountRatecutFine.clearFocus()
        binding.txtFineWtRateCutFine.clearFocus()
        binding.txtFineAdjustAddSaleLine.clearFocus()
        binding.txtAmountAdjustAddSaleLine.clearFocus()
    }

    private fun fillAllIssueReceiveTransactions() {
        when (receivedIssueReceive.type) {
            "cash_receipt" -> {
                checkedRowNo = "1"
                binding.txtAmountAddSaleLine.setText(receivedIssueReceive.cash_amount)
                selectedCashLedgerID = receivedIssueReceive.cash_ledger!!
                selectedCashLedgerName = receivedIssueReceive.cash_ledger_name!!
                binding.txtLedgerAddSaleLine.setText(receivedIssueReceive.cash_ledger_name)
                binding.txtRemarkAddCash.setText(receivedIssueReceive.cash_description)
            }
            "cash_payment" -> {
                checkedRowNo = "2"
                binding.txtAmountAddSaleLine.setText(receivedIssueReceive.cash_amount)
                selectedCashLedgerID = receivedIssueReceive.cash_ledger!!
                selectedCashLedgerName = receivedIssueReceive.cash_ledger_name!!
                binding.txtLedgerAddSaleLine.setText(selectedCashLedgerName)
                binding.txtRemarkAddCash.setText(receivedIssueReceive.cash_description)
            }
            "bank_receipt" -> {
                checkedRowNo = "3"
                binding.txtAmountBankAddSaleLine.setText(receivedIssueReceive.bank_amount)
                amtBankUpdatedValue = receivedIssueReceive.bank_amount!!
                selectedCashLedgerID = receivedIssueReceive.bank_ledger!!
                selectedCashLedgerName = receivedIssueReceive.bank_ledger_name!!
                binding.txtLedgerBankAddSaleLine.setText(selectedCashLedgerName)
                checkedBankMode = receivedIssueReceive.bank_mode
                when (checkedBankMode) {
                    "e-transfer" -> {
                        lyRTGSBankMode.visibility = View.VISIBLE
                        lyChequeBankMode.visibility = View.GONE
                        tvDescriptionAddSaleLine.visibility = View.GONE
                        rbRtgsNeftAddSaleLine.isChecked = true
                    }

                    "cheque" -> {
                        lyRTGSBankMode.visibility = View.GONE
                        lyChequeBankMode.visibility = View.VISIBLE
                        tvDescriptionAddSaleLine.visibility = View.GONE
                        rbChequeAddSaleLine.isChecked = true
                    }
                    "other" -> {
                        checkedBankMode = "other"
                        lyRTGSBankMode.visibility = View.GONE
                        lyChequeBankMode.visibility = View.GONE
                        tvDescriptionAddSaleLine.visibility = View.VISIBLE
                        rbOthersAddSaleLine.isChecked = true
                    }

                }
                binding.txtChequeAddSaleLine.setText(receivedIssueReceive.cheque_number)
                binding.txtDateAddSaleLine.setText(receivedIssueReceive.cheque_date)
                binding.txtFavNameAddSaleLine.setText(receivedIssueReceive.favouring_name)
                binding.txtDeductChargAddSaleLine.setText(receivedIssueReceive.deuct_charges)
                binding.txtChargePerAddSaleLine.setText(receivedIssueReceive.deuct_charges_percentage)
                binding.txtFinalAmtAddSaleLine.setText(receivedIssueReceive.bank_final_amt)
                binding.txtBankNameAddSaleLine.setText(receivedIssueReceive.recipient_bank)
                binding.txtBankAccNoAddSaleLine.setText(receivedIssueReceive.account_no)
                binding.txtIfsCodeAddSaleLine.setText(receivedIssueReceive.ifs_code)
                binding.txtUTRNoAddSaleLine.setText(receivedIssueReceive.utr_number)
                binding.txtDescriptionAddSaleLine.setText(receivedIssueReceive.bank_description)

            }
            "bank_payment" -> {
                checkedRowNo = "4"
                binding.txtAmountBankAddSaleLine.setText(receivedIssueReceive.bank_amount)
                amtBankUpdatedValue = receivedIssueReceive.bank_amount!!
                selectedCashLedgerID = receivedIssueReceive.bank_ledger!!
                selectedCashLedgerName = receivedIssueReceive.bank_ledger_name!!
                binding.txtLedgerBankAddSaleLine.setText(selectedCashLedgerName)
                checkedBankMode = receivedIssueReceive.bank_mode
                when (checkedBankMode) {
                    "e-transfer" -> {
                        lyRTGSBankMode.visibility = View.VISIBLE
                        lyChequeBankMode.visibility = View.GONE
                        tvDescriptionAddSaleLine.visibility = View.GONE
                        rbRtgsNeftAddSaleLine.isChecked = true
                    }

                    "cheque" -> {
                        lyRTGSBankMode.visibility = View.GONE
                        lyChequeBankMode.visibility = View.VISIBLE
                        tvDescriptionAddSaleLine.visibility = View.GONE
                        rbChequeAddSaleLine.isChecked = true
                    }
                    "other" -> {
                        checkedBankMode = "other"
                        lyRTGSBankMode.visibility = View.GONE
                        lyChequeBankMode.visibility = View.GONE
                        tvDescriptionAddSaleLine.visibility = View.VISIBLE
                        rbOthersAddSaleLine.isChecked = true
                    }

                }
                binding.txtChequeAddSaleLine.setText(receivedIssueReceive.cheque_number)
                binding.txtDateAddSaleLine.setText(receivedIssueReceive.cheque_date)
                binding.txtFavNameAddSaleLine.setText(receivedIssueReceive.favouring_name)
                deductChargesUpdatedValue = receivedIssueReceive.deuct_charges.toString()
                binding.txtDeductChargAddSaleLine.setText(receivedIssueReceive.deuct_charges)
                chargesPerUpdatedValue = receivedIssueReceive.deuct_charges_percentage.toString()
                binding.txtChargePerAddSaleLine.setText(receivedIssueReceive.deuct_charges_percentage)
                binding.txtFinalAmtAddSaleLine.setText(receivedIssueReceive.bank_final_amt)
                binding.txtBankNameAddSaleLine.setText(receivedIssueReceive.recipient_bank)
                binding.txtBankAccNoAddSaleLine.setText(receivedIssueReceive.account_no)
                binding.txtIfsCodeAddSaleLine.setText(receivedIssueReceive.ifs_code)
                binding.txtUTRNoAddSaleLine.setText(receivedIssueReceive.utr_number)
                binding.txtDescriptionAddSaleLine.setText(receivedIssueReceive.bank_description)
            }
            "metal_receipt" -> {
                checkedRowNo = "5"
                selectedItemID = receivedIssueReceive.item_id
                selectedItemName = receivedIssueReceive.item_name
                selectedItemMaintainStockName = receivedIssueReceive.maintain_stock_in_name_metal
                selectedMetalItemId = receivedIssueReceive.metal_type_id_metal
                binding.txtItemNameAddItemFineMetal.setText(receivedIssueReceive.item_name)
                grossUpdatedValue = receivedIssueReceive.gross_wt.toString()
                binding.tvGrosWtAddFineMe.hint = "Gross Weight (" + selectedItemMaintainStockName + ")"
                binding.txtGrossWtAddFineMe.setText(receivedIssueReceive.gross_wt)
                lesswtUpdatedValue = receivedIssueReceive.less_wt.toString()
                binding.txtLessWtAddFineMe.setText(receivedIssueReceive.less_wt)
                binding.txtNetWtAddFineMe.setText(receivedIssueReceive.net_wt)
                touchUpdatedValue = receivedIssueReceive.touch.toString()
                binding.txtTouchAddFineMe.setText(receivedIssueReceive.touch)
                wastageUpdatedValue = receivedIssueReceive.wast.toString()
                binding.txtWastageAddFineMe.setText(receivedIssueReceive.wast)
                binding.txtFineWtAddFineMe.setText(receivedIssueReceive.fine_wt)
            }
            "metal_payment" -> {
                checkedRowNo = "6"
                selectedItemID = receivedIssueReceive.item_id
                selectedItemName = receivedIssueReceive.item_name
                selectedItemMaintainStockName = receivedIssueReceive.maintain_stock_in_name_metal
                selectedMetalItemId = receivedIssueReceive.metal_type_id_metal
                binding.txtItemNameAddItemFineMetal.setText(receivedIssueReceive.item_name)
                grossUpdatedValue = receivedIssueReceive.gross_wt.toString()
                binding.tvGrosWtAddFineMe.hint = "Gross Weight (" + selectedItemMaintainStockName + ")"
                binding.txtGrossWtAddFineMe.setText(receivedIssueReceive.gross_wt)
                lesswtUpdatedValue = receivedIssueReceive.less_wt.toString()
                binding.txtLessWtAddFineMe.setText(receivedIssueReceive.less_wt)
                binding.txtNetWtAddFineMe.setText(receivedIssueReceive.net_wt)
                touchUpdatedValue = receivedIssueReceive.touch.toString()
                binding.txtTouchAddFineMe.setText(receivedIssueReceive.touch)
                wastageUpdatedValue = receivedIssueReceive.wast.toString()
                binding.txtWastageAddFineMe.setText(receivedIssueReceive.wast)
                binding.txtFineWtAddFineMe.setText(receivedIssueReceive.fine_wt)
            }
            "rate_cut" -> {
                checkedRowNo = "7"
                selectedMetalRateCut = receivedIssueReceive.metal_type_id_rate_cut
                when (selectedMetalRateCut) {
                    "1" -> {
                        radioGoldRateCut.isChecked = true
                    }
                    "2" -> {
                        radioSilverRateCut.isChecked = true
                    }
                }
                selectedRateCut = receivedIssueReceive.rate_cut_fine_term
                goldRateUpdatedValue = receivedIssueReceive.rcm_gold_rate.toString()
                binding.txtGoldrateRateCutFine.setText(receivedIssueReceive.rcm_gold_rate)
                fineRateCutUpdatedValue = receivedIssueReceive.rate_cut_fine.toString()
                binding.txtFineWtRateCutFine.setText(receivedIssueReceive.rate_cut_fine)
                amtRateCutUpdatedValue = receivedIssueReceive.rate_cut_amount.toString()
                binding.txtAmountRatecutFine.setText(receivedIssueReceive.rate_cut_amount)


                is_From_FirstTime_Fine = true
                is_From_FirstTime_Amt = true
                is_From_FirstTime_Gold = true
                when (receivedIssueReceive.rate_cut_fine_term) {
                    "debit" -> {
                        when (dashboardModel.data!!.default_term!!.default_term) {
                            "debit_credit" -> {
                                binding.txtFineRateCutTerm.setText("Debit")
                                binding.txtAmtRateCutTerm.setText("Credit")
                                selectedRateCut = "debit"
                            }
                            "udhar_jama" -> {
                                binding.txtFineRateCutTerm.setText("Udhar")
                                binding.txtAmtRateCutTerm.setText("Jama")
                                selectedRateCut = "debit"
                            }
                            "receivable_payable" -> {
                                binding.txtFineRateCutTerm.setText("Receivable")
                                binding.txtAmtRateCutTerm.setText("Payable")
                                selectedRateCut = "debit"
                            }
                            "len_den" -> {
                                binding.txtFineRateCutTerm.setText("Len")
                                binding.txtAmtRateCutTerm.setText("Den")
                                selectedRateCut = "debit"
                            }
                        }
                    }
                    "credit" -> {
                        when (dashboardModel.data!!.default_term!!.default_term) {
                            "debit_credit" -> {
                                binding.txtFineRateCutTerm.setText("Credit")
                                binding.txtAmtRateCutTerm.setText("Debit")
                            }
                            "udhar_jama" -> {
                                binding.txtFineRateCutTerm.setText("Jama")
                                binding.txtAmtRateCutTerm.setText("Udhar")
                            }
                            "receivable_payable" -> {
                                binding.txtFineRateCutTerm.setText("Payable")
                                binding.txtAmtRateCutTerm.setText("Receivable")
                            }
                            "len_den" -> {
                                binding.txtFineRateCutTerm.setText("Den")
                                binding.txtAmtRateCutTerm.setText("Len")
                            }
                        }


                    }
                }
                /*when (selectedRateCut) {
                    "fine" -> {
                        binding.radioRateCutFine.isChecked = true
                        goldRateUpdatedValue = receivedIssueReceive.rcm_gold_rate.toString()
                        binding.txtGoldrateRateCutFine.setText(receivedIssueReceive.rcm_gold_rate)
                        fineRateCutUpdatedValue = receivedIssueReceive.rate_cut_fine.toString()
                        binding.txtFineWtRateCutFine.setText(receivedIssueReceive.rate_cut_fine)
                        amtRateCutUpdatedValue = receivedIssueReceive.rate_cut_amount.toString()
                        binding.txtAmountRatecutFine.setText(receivedIssueReceive.rate_cut_amount)
                    }
                    "amount" -> {
                        binding.radioRateCutMet.isChecked = true
                        goldRateUpdatedValue = receivedIssueReceive.rcm_gold_rate.toString()
                        binding.txtGoldrateRateCutFine.setText(receivedIssueReceive.rcm_gold_rate)
                        fineRateCutUpdatedValue = receivedIssueReceive.rate_cut_fine.toString()
                        binding.txtFineWtRateCutFine.setText(receivedIssueReceive.rate_cut_fine)
                        amtRateCutUpdatedValue = receivedIssueReceive.rate_cut_amount.toString()
                        binding.txtAmountRatecutFine.setText(receivedIssueReceive.rate_cut_amount)
                    }
                }*/
            }
            "adjustment" -> {
                checkedRowNo = "8"
                selectedMetalAdjustment = receivedIssueReceive.metal_type_id_adjustments
                when (selectedMetalAdjustment) {
                    "1" -> {
                        radioGoldAdjustment.isChecked = true
                    }
                    "2" -> {
                        radioSilverAdjustment.isChecked = true
                    }
                }
                binding.txtFineAdjustAddSaleLine.setText(receivedIssueReceive.adjustment_fine)
                binding.txtAmountAdjustAddSaleLine.setText(receivedIssueReceive.adjustment_amount)
                selectedCashLedgerID = receivedIssueReceive.adjustment_ledger!!
                selectedCashLedgerName = receivedIssueReceive.adjustment_ledger_name!!
                binding.txtLedgerAdjustAddSaleLine.setText(receivedIssueReceive.adjustment_ledger_name)
                binding.txtRemarkAdjustAddSaleLine.setText(receivedIssueReceive.adjustment_description)
            }
        }

    }

    fun openDatePicker(isFromDate: Boolean) {
        val c = Calendar.getInstance()
        if (isFromDate) {
            val sdf = SimpleDateFormat("dd-MMM-yy")
            val parse = sdf.parse(binding.txtDateAddSaleLine.text.toString())
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
                    binding.txtDateAddSaleLine.setText(
                        "" + String.format(
                            "%02d",
                            dayOfMonth
                        ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                            .substring(2, 4)
                    )

                } else {
                }
            },
            year,
            month,
            day
        )

        // dpd.datePicker.maxDate = Date().time
        dpd.show()
    }

    private fun onFocusChangeSetup() {

        binding.txtAmountAddSaleLine.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                when (txtAmountAddSaleLine.text.isNullOrBlank()) {
                    true -> {


                    }
                    else -> {
                        txtAmountAddSaleLine.setText(amtCashUpdatedValue)
                        txtAmountAddSaleLine.setSelection(amtCashUpdatedValue.length)
                    }
                }
            }
        }



        binding.txtGrossWtAddFineMe.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                when (txtGrossWtAddFineMe.text.isNullOrBlank()) {
                    true -> {
                        grossUpdatedValue = "0.00"
                        txtGrossWtAddFineMe.setText(grossUpdatedValue)
                        //txtGrossWtAddFineMe.setSelection(grossUpdatedValue.length)
                    }
                    else -> {
                        txtGrossWtAddFineMe.setText(grossUpdatedValue)
                        txtGrossWtAddFineMe.setSelection(grossUpdatedValue.length)

                    }
                }
            }
        }

        binding.txtAmountBankAddSaleLine.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                when (txtAmountBankAddSaleLine.text.isNullOrBlank()) {
                    true -> {

                    }
                    false -> {
                        binding.txtAmountBankAddSaleLine.setText(amtBankUpdatedValue)
                        finalAmtUpdatedValue = amtBankUpdatedValue
                        binding.txtFinalAmtAddSaleLine.setText(finalAmtUpdatedValue)
                    }
                }
            }
        }


        binding.txtDeductChargAddSaleLine.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                when (txtDeductChargAddSaleLine.text.isNullOrBlank()) {
                    true -> {


                    }
                    else -> {
                        binding.txtChargePerAddSaleLine.setText("0")
                        txtDeductChargAddSaleLine.setText(deductChargesUpdatedValue)
                        txtDeductChargAddSaleLine.setSelection(deductChargesUpdatedValue.length)
                        updateFinalAmt()
                    }
                }
            }
        }

        binding.txtChargePerAddSaleLine.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                Log.d("chargesper",""+txtChargePerAddSaleLine.text +chargesPerUpdatedValue)
                when (txtChargePerAddSaleLine.text!!.isNullOrBlank()) {
                    true -> {

                    }
                    else -> {
                        if(chargesPerUpdatedValue.equals("0.00")){
                            txtDeductChargAddSaleLine.setText(deductChargesUpdatedValue)
                            updateFinalAmt()
                        } else{
                            binding.txtDeductChargAddSaleLine.setText("0.00")
                            deductChargesUpdatedValue = binding.txtDeductChargAddSaleLine.text.toString()
                            chargesPerUpdatedValue = binding.txtChargePerAddSaleLine.text.toString()
                            txtChargePerAddSaleLine.setText(chargesPerUpdatedValue)
                            // txtChargePerAddSaleLine.setSelection(chargesPerUpdatedValue.length)
                            updateFinalAmtCharge()
                        }

                    }
                }
            } else {

            }
        }


        binding.txtLessWtAddFineMe.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                when (txtLessWtAddFineMe.text.isNullOrBlank()) {
                    true -> {
                        lesswtUpdatedValue = "0.00"
                        txtLessWtAddFineMe.setText(lesswtUpdatedValue)
                        txtLessWtAddFineMe.setSelection(lesswtUpdatedValue.length)
                    }
                    else -> {
                        txtLessWtAddFineMe.setText(lesswtUpdatedValue)
                        txtLessWtAddFineMe.setSelection(lesswtUpdatedValue.length)
                    }
                }
            }
        }

        binding.txtWastageAddFineMe.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                when (txtWastageAddFineMe.text.isNullOrBlank()) {
                    true -> {
                        wastageUpdatedValue = "0.00"
                        txtWastageAddFineMe.setText(wastageUpdatedValue)
                        txtWastageAddFineMe.setSelection(wastageUpdatedValue.length)
                    }
                    else -> {
                        txtWastageAddFineMe.setText(wastageUpdatedValue)
                        txtWastageAddFineMe.setSelection(wastageUpdatedValue.length)
                    }
                }
            }
        }
        binding.txtGoldrateRateCutFine.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {

                when (txtGoldrateRateCutFine.text.isNullOrBlank()) {
                    true -> {
                        if (txtGoldrateRateCutFine.text.isNullOrBlank() && txtFineWtRateCutFine.text.isNullOrBlank() &&
                            txtAmountRatecutFine.text.isNullOrBlank()){
                            is_From_FirstTime_Fine = false
                            is_From_FirstTime_Amt = false
                            is_From_FirstTime_Gold = false
                            txtGoldrateRateCutFine.setText("")
                        }else{
                            goldRateUpdatedValue = "0.00"
                            txtGoldrateRateCutFine.setText(goldRateUpdatedValue)
                            txtGoldrateRateCutFine.setSelection(goldRateUpdatedValue.length)
                        }

                    }
                    else -> {
                        if (txtFineWtRateCutFine.text.isNullOrBlank() &&
                            txtAmountRatecutFine.text.isNullOrBlank()){
                            is_From_FirstTime_Fine = false
                            is_From_FirstTime_Amt = false
                            is_From_FirstTime_Gold = false
                            if (is_From_edit){
                                goldRateUpdatedValue = txtGoldrateRateCutFine.text.toString()
                            }
                        }
                        when (is_From_FirstTime_Gold) {
                            true -> {

                                txtGoldrateRateCutFine.setText(updatedgoldRateValue)
                                txtGoldrateRateCutFine.setSelection(updatedgoldRateValue.length)
                                goldRateUpdatedValue = updatedgoldRateValue
                            }
                            false -> {
                                if (goldRateUpdatedValue.toBigDecimal()
                                        .compareTo(BigDecimal.ZERO) > 0
                                ) {
                                    Log.v("goldchange", "" + goldRateUpdatedValue)
                                    txtGoldrateRateCutFine.setText(goldRateUpdatedValue)
                                    txtGoldrateRateCutFine.setSelection(goldRateUpdatedValue.length)
                                    updatedgoldRateValue = goldRateUpdatedValue
                                    is_From_FirstTime_Gold = true
                                } else {
                                    Log.v("goldchange", "" + goldRateUpdatedValue)
                                    goldRateUpdatedValue = "1.00"
                                    txtGoldrateRateCutFine.setText(goldRateUpdatedValue)
                                    txtGoldrateRateCutFine.setSelection(goldRateUpdatedValue.length)

                                }

                            }
                        }


                    }

                }
            }
        }
        binding.txtFineWtRateCutFine.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (!hasFocus) {
                when (txtFineWtRateCutFine.text.isNullOrBlank()) {
                    true -> {
                        fineRateCutUpdatedValue = "0.000"
                        binding.txtFineWtRateCutFine.setText("")

                    }
                    else -> {
                        when (is_From_FirstTime_Fine) {
                            false -> {
                                when (txtGoldrateRateCutFine.text.isNullOrBlank()) {
                                    false -> {
                                        binding.txtFineWtRateCutFine.setText(fineRateCutUpdatedValue)
                                        binding.txtFineWtRateCutFine.setSelection(
                                            fineRateCutUpdatedValue.length
                                        )

                                        val goldRate: BigDecimal =
                                            goldRateUpdatedValue.toBigDecimal()
                                        /*val goldRatePerGram: BigDecimal =
                                            ((goldRate.setScale(2) / BigDecimal(10))
                                                    ).setScale(2, RoundingMode.CEILING)

                                        Log.v("goldRte", "" + goldRatePerGram)*/
                                        val fineWeight: BigDecimal =
                                            fineRateCutUpdatedValue.toBigDecimal()
                                        val result: String =
                                            ((goldRate.setScale(2)
                                                .multiply(fineWeight.setScale(3))
                                                    )).setScale(2, RoundingMode.CEILING).toString()

                                        amtRateCutUpdatedValue = result
                                        binding.txtAmountRatecutFine.setText(amtRateCutUpdatedValue)
                                        is_From_FirstTime_Fine = true
                                        is_From_FirstTime_Amt = true
                                    }
                                    true -> {
                                        binding.txtFineWtRateCutFine.setText(fineRateCutUpdatedValue)
                                        binding.txtFineWtRateCutFine.setSelection(
                                            fineRateCutUpdatedValue.length
                                        )


                                    }
                                }

                            }
                            true -> {

                                if (!txtGoldrateRateCutFine.text.isNullOrBlank() && !txtFineWtRateCutFine.text.isNullOrBlank() &&
                                    !txtAmountRatecutFine.text.isNullOrBlank()
                                ) {
                                    binding.txtFineWtRateCutFine.setText(fineRateCutUpdatedValue)
                                    binding.txtFineWtRateCutFine.setSelection(
                                        fineRateCutUpdatedValue.length
                                    )

                                    val fine: BigDecimal = fineRateCutUpdatedValue.toBigDecimal()
                                    Log.v("fine1", "" + fineRateCutUpdatedValue)
                                    val amt: BigDecimal = amtRateCutUpdatedValue.toBigDecimal()

                                    try {
                                        val result: BigDecimal =
                                            ((amt.setScale(2) / fine.setScale(3))
                                                    ).setScale(2, RoundingMode.CEILING)

                                        val finalResult: BigDecimal =
                                            (result.setScale(2))
                                                .setScale(2, RoundingMode.CEILING)

                                        goldRateUpdatedValue = finalResult.toString()
                                        Log.v("goldrate", "" + goldRateUpdatedValue)
                                        binding.txtGoldrateRateCutFine.setText(goldRateUpdatedValue)
                                        updatedgoldRateValue = goldRateUpdatedValue
                                        is_From_FirstTime_Amt = true
                                        is_From_FirstTime_Gold = true

                                    }catch (e: Exception) {
                                        e.printStackTrace()
                                        goldRateUpdatedValue = "0.00"
                                        binding.txtGoldrateRateCutFine.setText(goldRateUpdatedValue)
                                        is_From_FirstTime_Amt = true
                                        is_From_FirstTime_Gold = true
                                    }

                                    /*val finalResult: BigDecimal =
                                        (result.setScale(2) * BigDecimal(10))
                                            .setScale(2, RoundingMode.CEILING)*/


                                }

                            }
                        }

                    }
                }
            }
        }
        binding.txtAmountRatecutFine.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (!hasFocus) {
                when (txtAmountRatecutFine.text.isNullOrBlank()) {
                    true -> {
                        if (!txtGoldrateRateCutFine.text.isNullOrBlank() && !txtFineWtRateCutFine.text.isNullOrBlank()
                        ) {
                            when (txtGoldrateRateCutFine.text.isNullOrBlank()) {
                                false -> {
                                    binding.txtFineWtRateCutFine.setText(fineRateCutUpdatedValue)
                                    binding.txtFineWtRateCutFine.setSelection(
                                        fineRateCutUpdatedValue.length
                                    )

                                    val goldRate: BigDecimal =
                                        goldRateUpdatedValue.toBigDecimal()
                                    /*val goldRatePerGram: BigDecimal =
                                        ((goldRate.setScale(2) / BigDecimal(10))
                                                ).setScale(2, RoundingMode.CEILING)

                                    Log.v("goldRte", "" + goldRatePerGram)*/
                                    val fineWeight: BigDecimal =
                                        fineRateCutUpdatedValue.toBigDecimal()
                                    val result: String =
                                        ((goldRate.setScale(2)
                                            .multiply(fineWeight.setScale(3))
                                                )).setScale(2, RoundingMode.CEILING).toString()

                                    amtRateCutUpdatedValue = result
                                    binding.txtAmountRatecutFine.setText(amtRateCutUpdatedValue)
                                    is_From_FirstTime_Fine = true
                                    is_From_FirstTime_Amt = true
                                }
                                true -> {
                                    binding.txtFineWtRateCutFine.setText(fineRateCutUpdatedValue)
                                    binding.txtFineWtRateCutFine.setSelection(
                                        fineRateCutUpdatedValue.length
                                    )


                                }
                            }
                        } else{
                            amtRateCutUpdatedValue = "0.00"
                            binding.txtAmountRatecutFine.setText("")
                        }
                    }
                    else -> {
                        when (is_From_FirstTime_Amt) {
                            false -> {
                                when (txtGoldrateRateCutFine.text.isNullOrBlank()) {
                                    true -> {

                                        binding.txtAmountRatecutFine.setText(amtRateCutUpdatedValue)
                                        binding.txtAmountRatecutFine.setSelection(
                                            amtRateCutUpdatedValue.length
                                        )

                                        val fine: BigDecimal =
                                            fineRateCutUpdatedValue.toBigDecimal()
                                        Log.v("fine2", "" + fineRateCutUpdatedValue)
                                        val amt: BigDecimal = amtRateCutUpdatedValue.toBigDecimal()

                                        val result: BigDecimal =
                                            ((amt.setScale(2) / fine.setScale(3))
                                                    ).setScale(2, RoundingMode.CEILING)
                                        goldRateUpdatedValue = result.toString()
                                        binding.txtGoldrateRateCutFine.setText(goldRateUpdatedValue)
                                        updatedgoldRateValue = goldRateUpdatedValue
                                        is_From_FirstTime_Gold = true
                                    }
                                    false -> {
                                        binding.txtAmountRatecutFine.setText(amtRateCutUpdatedValue)
                                        binding.txtAmountRatecutFine.setSelection(
                                            amtRateCutUpdatedValue.length
                                        )

                                        val goldRate: BigDecimal =
                                            goldRateUpdatedValue.toBigDecimal()

                                        /* val goldRatePerGram: BigDecimal =
                                             ((goldRate.setScale(2) / BigDecimal(10))
                                                     ).setScale(2, RoundingMode.CEILING)

                                         Log.v("goldRte", "" + goldRatePerGram)*/

                                        val amt: BigDecimal = amtRateCutUpdatedValue.toBigDecimal()
                                        val result: String =
                                            ((amt.setScale(2) / goldRate.setScale(2)
                                                    )).setScale(3, RoundingMode.CEILING).toString()

                                        fineRateCutUpdatedValue = result
                                        binding.txtFineWtRateCutFine.setText(fineRateCutUpdatedValue)
                                        is_From_FirstTime_Amt = true
                                        is_From_FirstTime_Fine = true

                                    }
                                }

                            }
                            true -> {
                                if (!txtGoldrateRateCutFine.text.isNullOrBlank() && !txtFineWtRateCutFine.text.isNullOrBlank() &&
                                    !txtAmountRatecutFine.text.isNullOrBlank()
                                ) {
                                    binding.txtAmountRatecutFine.setText(amtRateCutUpdatedValue)
                                    binding.txtAmountRatecutFine.setSelection(amtRateCutUpdatedValue.length)

                                    val fine: BigDecimal = fineRateCutUpdatedValue.toBigDecimal()
                                    Log.v("fine3", "" + fineRateCutUpdatedValue)
                                    val amt: BigDecimal = amtRateCutUpdatedValue.toBigDecimal()

                                    try {
                                        val result: BigDecimal =
                                            ((amt.setScale(2) / fine.setScale(3))
                                                    ).setScale(2, RoundingMode.CEILING)
                                        goldRateUpdatedValue = result.toString()
                                        binding.txtGoldrateRateCutFine.setText(goldRateUpdatedValue)
                                        updatedgoldRateValue = goldRateUpdatedValue
                                        is_From_FirstTime_Gold = true
                                        is_From_FirstTime_Fine = true
                                    }catch (e: Exception) {
                                        e.printStackTrace()

                                    }

                                }

                            }
                        }

                    }
                }
            }
        }

        binding.txtFineAdjustAddSaleLine.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                when (binding.txtFineAdjustAddSaleLine.text.isNullOrBlank()) {
                    true -> {
                    }
                    else -> {
                        binding.txtFineAdjustAddSaleLine.setText(fineAdjustUpdatedValue)
                        binding.txtFineAdjustAddSaleLine.setSelection(fineAdjustUpdatedValue.length)

                    }
                }
            }
        }

        binding.txtAmountAdjustAddSaleLine.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                when (binding.txtAmountAdjustAddSaleLine.text.isNullOrBlank()) {
                    true -> {
                    }
                    else -> {
                        binding.txtAmountAdjustAddSaleLine.setText(amtAdjustUpdatedValue)
                        binding.txtAmountAdjustAddSaleLine.setSelection(amtAdjustUpdatedValue.length)

                    }
                }
            }
        }
    }

    private fun updateFinalAmtCharge() {
        if (!binding.txtChargePerAddSaleLine.text!!.isNullOrBlank()) {

            val Chaargeper: BigDecimal = chargesPerUpdatedValue.toBigDecimal()
            val amt: BigDecimal = amtBankUpdatedValue.toBigDecimal()
            val result: String =
                ((amt * Chaargeper) / BigDecimal(100)).setScale(
                    2,
                    RoundingMode.CEILING
                )
                    .toString()
            val resultPer = result.toBigDecimal()
            val resultFinal: String = (amt - resultPer).toString()
            finalAmtUpdatedValue = resultFinal
            binding.txtFinalAmtAddSaleLine.setText(finalAmtUpdatedValue)
        } else {

        }
    }

    private fun updateFinalAmt() {
        if (!binding.txtDeductChargAddSaleLine.text!!.isNullOrBlank()) {

            Log.v("deductcharge", "" + deductChargesUpdatedValue)
            val deductChaarge: BigDecimal = deductChargesUpdatedValue.toBigDecimal()
            val amt: BigDecimal = amtBankUpdatedValue.toBigDecimal()
            val result: String = (amt - deductChaarge).toString()
            finalAmtUpdatedValue = result
            binding.txtFinalAmtAddSaleLine.setText(finalAmtUpdatedValue)


        } else {

        }

    }


    private fun onTextChangedSetup() {

        binding.txtAmountAddSaleLine.doAfterTextChanged {

            val str: String = binding.txtAmountAddSaleLine.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtAmountAddSaleLine.setText(str2)
                binding.txtAmountAddSaleLine.setSelection(str2.length)
            }

            amtCashUpdatedValue = df.format(str2.toDouble())
        }



        binding.txtAmountBankAddSaleLine.doAfterTextChanged {

            val str: String = binding.txtAmountBankAddSaleLine.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtAmountBankAddSaleLine.setText(str2)
                binding.txtAmountBankAddSaleLine.setSelection(str2.length)
            }

            amtBankUpdatedValue = df.format(str2.toDouble())
        }


        binding.txtDeductChargAddSaleLine.doAfterTextChanged {
            val str: String = binding.txtDeductChargAddSaleLine.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtDeductChargAddSaleLine.setText(str2)
                binding.txtDeductChargAddSaleLine.setSelection(str2.length)
            }

            deductChargesUpdatedValue = df.format(str2.toDouble())


        }

        binding.txtChargePerAddSaleLine.doAfterTextChanged {
            val str: String = binding.txtChargePerAddSaleLine.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtChargePerAddSaleLine.setText(str2)
                binding.txtChargePerAddSaleLine.setSelection(str2.length)
            }

            chargesPerUpdatedValue = df.format(str2.toDouble())


        }

        binding.txtFineWtRateCutFine.doAfterTextChanged {
            val str: String = txtFineWtRateCutFine.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            var str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                txtFineWtRateCutFine.setText(str2)
                if (txtFineWtRateCutFine.text.toString().substringBefore(".").length == 0) {
                    str2 = "1.000"
                    txtFineWtRateCutFine.setText(str2)
                    txtFineWtRateCutFine.setSelection(str2.length)
                } else if (txtFineWtRateCutFine.text.toString().toBigDecimal()
                        .compareTo(BigDecimal.ZERO) == 0
                ) {
                    str2 = "1.000"
                    txtFineWtRateCutFine.setText(str2)
                    txtFineWtRateCutFine.setSelection(str2.length)
                } else {

                    txtFineWtRateCutFine.setSelection(str2.length)
                }

            }
            //  Log.v("dashboardGoldrate", (df.format(str2.toDouble())))
            fineRateCutUpdatedValue = df1.format(str2.toDouble())

        }
        binding.txtAmountRatecutFine.doAfterTextChanged {
            val str: String = binding.txtAmountRatecutFine.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtAmountRatecutFine.setText(str2)
                binding.txtAmountRatecutFine.setSelection(str2.length)
            }

            amtRateCutUpdatedValue = df.format(str2.toDouble())
            Log.v("amt", "" + amtRateCutUpdatedValue)

        }

        when (is_From_FirstTime_Gold) {
            false -> {
                Log.v("changefalse", "true")
                binding.txtGoldrateRateCutFine.doAfterTextChanged {
                    val str: String = txtGoldrateRateCutFine.text.toString()

                    if (str.isEmpty()) return@doAfterTextChanged
                    var str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
                    if (!str2.equals(str)) {
                        //val str3:String = df.format(str2.toDouble())
                        txtGoldrateRateCutFine.setText(str2)
                        if (txtGoldrateRateCutFine.text.toString()
                                .substringBefore(".").length == 0
                        ) {
                            str2 = "1.00"
                            txtGoldrateRateCutFine.setText(str2)
                            txtGoldrateRateCutFine.setSelection(str2.length)
                        } else if (txtGoldrateRateCutFine.text.toString().toBigDecimal()
                                .compareTo(BigDecimal.ZERO) == 0
                        ) {
                            str2 = "1.00"
                            txtGoldrateRateCutFine.setText(str2)
                            txtGoldrateRateCutFine.setSelection(str2.length)
                        } else {
                            txtGoldrateRateCutFine.setSelection(str2.length)
                        }

                    }
                    //  Log.v("dashboardGoldrate", (df.format(str2.toDouble())))
                    goldRateUpdatedValue = df.format(str2.toDouble())

                }

            }
            else -> {

            }
        }




        binding.txtFineWtRateCutFine.doAfterTextChanged {
            val str: String = txtFineWtRateCutFine.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            var str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                txtFineWtRateCutFine.setText(str2)
                if (txtFineWtRateCutFine.text.toString().substringBefore(".").length == 0) {
                    str2 = "1.000"
                    txtFineWtRateCutFine.setText(str2)
                    txtFineWtRateCutFine.setSelection(str2.length)
                } else if (txtFineWtRateCutFine.text.toString().toBigDecimal()
                        .compareTo(BigDecimal.ZERO) == 0
                ) {
                    str2 = "1.000"
                    txtFineWtRateCutFine.setText(str2)
                    txtFineWtRateCutFine.setSelection(str2.length)
                } else {
                    txtFineWtRateCutFine.setSelection(str2.length)
                }

            }
            //  Log.v("dashboardGoldrate", (df.format(str2.toDouble())))
            fineRateCutUpdatedValue = df1.format(str2.toDouble())
        }


        binding.txtAmountRatecutFine.doAfterTextChanged {
            val str: String = binding.txtAmountRatecutFine.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtAmountRatecutFine.setText(str2)
                binding.txtAmountRatecutFine.setSelection(str2.length)
            }

            amtRateCutUpdatedValue = df.format(str2.toDouble())
            Log.v("amt", "" + amtRateCutUpdatedValue)

        }


        binding.txtTouchAddFineMe.doAfterTextChanged {

            val str: String = binding.txtTouchAddFineMe.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 3, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                txtTouchAddFineMe.setText(str2)
                txtTouchAddFineMe.setSelection(str2.length)
            }

            touchUpdatedValue = df.format(str2.toDouble())


            var wastage: BigDecimal = BigDecimal(0)
            if (!binding.txtWastageAddFineMe.text.toString().isBlank()) {
                wastage = binding.txtWastageAddFineMe.text.toString().toBigDecimal()
            }
            if (!binding.txtNetWtAddFineMe.text.toString()
                    .isBlank() && !binding.txtTouchAddFineMe.text.toString()
                    .isBlank()
            ) {
                val net: BigDecimal = binding.txtNetWtAddFineMe.text.toString().toBigDecimal()
                val touch: BigDecimal = binding.txtTouchAddFineMe.text.toString().toBigDecimal()
                val result: String =
                    ((net.setScale(3) * (touch.setScale(3) + wastage.setScale(3)) / BigDecimal(100))).setScale(
                        3,
                        RoundingMode.HALF_UP
                    ).toString()
                binding.txtFineWtAddFineMe.setText(result)
            }
        }

        binding.txtWastageAddFineMe.doAfterTextChanged {

            when (txtWastageAddFineMe.text.toString().startsWith("-")) {
                true -> if (!txtWastageAddFineMe.text.toString()
                        .isBlank() && txtWastageAddFineMe.text.toString().length > 1
                ) {
                    when (txtWastageAddFineMe.text.toString().startsWith("-.")) {
                        true -> Toast.makeText(
                            this,
                            getString(R.string.valid_wastage),
                            Toast.LENGTH_SHORT
                        ).show()
                        false -> setWastagenFinewt(df)
                    }

                }
                false -> {
                    setWastagenFinewt(df)
                }
            }


        }

        binding.txtGrossWtAddFineMe.doAfterTextChanged {

            val str: String = binding.txtGrossWtAddFineMe.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtGrossWtAddFineMe.setText(str2)
                binding.txtGrossWtAddFineMe.setSelection(str2.length)
            }

            grossUpdatedValue = df1.format(str2.toDouble())


            if (!binding.txtGrossWtAddFineMe.text.toString()
                    .isBlank() && !binding.txtLessWtAddFineMe.text.toString()
                    .isBlank()
            ) {
                val gross: BigDecimal = binding.txtGrossWtAddFineMe.text.toString().toBigDecimal()
                val less: BigDecimal = binding.txtLessWtAddFineMe.text.toString().toBigDecimal()
                val result: String = (gross - less).toString()
                binding.txtNetWtAddFineMe.setText(result)
            }

            var wastage: BigDecimal = BigDecimal(0)
            if (!binding.txtWastageAddFineMe.text.toString().isBlank()) {
                wastage = binding.txtWastageAddFineMe.text.toString().toBigDecimal()
            }
            if (!binding.txtNetWtAddFineMe.text.toString()
                    .isBlank() && !binding.txtTouchAddFineMe.text.toString()
                    .isBlank()
            ) {
                val net: BigDecimal = binding.txtNetWtAddFineMe.text.toString().toBigDecimal()
                val touch: BigDecimal = binding.txtTouchAddFineMe.text.toString().toBigDecimal()
                val result: String =
                    ((net.setScale(3) * (touch.setScale(3) + wastage.setScale(3)) / BigDecimal(100))).setScale(
                        3,
                        RoundingMode.HALF_UP
                    ).toString()
                binding.txtFineWtAddFineMe.setText(result)
            }
        }

        binding.txtLessWtAddFineMe.doAfterTextChanged {

            val str: String = binding.txtLessWtAddFineMe.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtLessWtAddFineMe.setText(str2)
                binding.txtLessWtAddFineMe.setSelection(str2.length)
            }

            lesswtUpdatedValue = df1.format(str2.toDouble())


            if (!binding.txtGrossWtAddFineMe.text.toString()
                    .isBlank() && !binding.txtLessWtAddFineMe.text.toString()
                    .isBlank()
            ) {
                val gross: BigDecimal = binding.txtGrossWtAddFineMe.text.toString().toBigDecimal()
                val less: BigDecimal = binding.txtLessWtAddFineMe.text.toString().toBigDecimal()
                val result: String = (gross - less).toString()
                binding.txtNetWtAddFineMe.setText(result)
            }

            var wastage: BigDecimal = BigDecimal(0)
            if (!binding.txtWastageAddFineMe.text.toString().isBlank()) {
                wastage = binding.txtWastageAddFineMe.text.toString().toBigDecimal()
            }
            if (!binding.txtNetWtAddFineMe.text.toString()
                    .isBlank() && !binding.txtTouchAddFineMe.text.toString()
                    .isBlank()
            ) {
                val net: BigDecimal = binding.txtNetWtAddFineMe.text.toString().toBigDecimal()
                val touch: BigDecimal = binding.txtTouchAddFineMe.text.toString().toBigDecimal()
                val result: String =
                    ((net.setScale(3) * (touch.setScale(3) + wastage.setScale(3)) / BigDecimal(100))).setScale(
                        3,
                        RoundingMode.HALF_UP
                    ).toString()
                binding.txtFineWtAddFineMe.setText(result)
            }
        }

        binding.txtFineAdjustAddSaleLine.doAfterTextChanged {
            val inputValue: Float
            var str: String = "0.000"
            try {
//convert in float for negative value
                inputValue = binding.txtFineAdjustAddSaleLine.text.toString().toFloat()
                str = inputValue.toString()

            } catch (nfe: NumberFormatException) {
                //Error handling.
            }

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //str3 = df1.format(str2.toDouble())
                binding.txtFineAdjustAddSaleLine.setText(str2)
                binding.txtFineAdjustAddSaleLine.setSelection(str2.length)

            }
            fineAdjustUpdatedValue = df1.format(str2.toDouble())
            Log.d("fineAdjust", "" + fineAdjustUpdatedValue)

        }

        binding.txtAmountAdjustAddSaleLine.doAfterTextChanged {

            val inputValue: Float
            var str: String = "0.00"
            try {
                //convert in float for negative value
                inputValue = binding.txtAmountAdjustAddSaleLine.text.toString().toFloat()
                str = inputValue.toString()

            } catch (nfe: NumberFormatException) {
                //Error handling.
            }

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtAmountAdjustAddSaleLine.setText(str2)
                binding.txtAmountAdjustAddSaleLine.setSelection(str2.length)
            }

            amtAdjustUpdatedValue = df.format(str2.toDouble())
        }
    }


    private fun setWastagenFinewt(df: DecimalFormat) {
        val str: String = txtWastageAddFineMe.text.toString()

        if (str.isEmpty()) return
        val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
        if (!str2.equals(str)) {
            //val str3:String = df.format(str2.toDouble())
            txtWastageAddFineMe.setText(str2)
            txtWastageAddFineMe.setSelection(str2.length)
        }
        /* Log.v("wastage", (df.format(str2.toDouble())))*/
        wastageUpdatedValue = df.format(str2.toDouble())
        wastage = txtWastageAddFineMe.text.toString().toBigDecimal()

        if (!binding.txtNetWtAddFineMe.text.toString()
                .isBlank() && !binding.txtTouchAddFineMe.text.toString()
                .isBlank()
        ) {
            val net: BigDecimal = binding.txtNetWtAddFineMe.text.toString().toBigDecimal()
            val touch: BigDecimal = binding.txtTouchAddFineMe.text.toString().toBigDecimal()
            val result: String =
                ((net.setScale(3) * (touch.setScale(3) + wastage.setScale(3)) / BigDecimal(
                    100
                ))).setScale(3, RoundingMode.HALF_UP).toString()
            binding.txtFineWtAddFineMe.setText(result)
        }
    }


    private fun applyingDigitFilter() {
        // applying filters to edit input number decimal fields(which have 2 or 3 decimal after .)

        txtGrossWtAddFineMe.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )
        txtLessWtAddFineMe.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )
        txtTouchAddFineMe.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    3,
                    2,
                    100.00
                )
            )
        )
        txtWastageAddFineMe.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )


    }


    private fun visibleUIAccordingToPaymentRowNo(checkedRowNo: String?, isFromNew: Boolean) {
        when (checkedRowNo) {
            // add cash receipt
            "1" -> {
                when (isFromNew) {
                    true -> binding.root.tvTitle.setText(R.string.add_cash_receipt)
                    false -> binding.root.tvTitle.setText(
                        getString(R.string.add_cash_receipt).replace(
                            "Add",
                            "Edit"
                        )
                    )
                }
                llAddSaleCash.visibility = View.VISIBLE
                getLedgerdd("cash")

            }
            // add cash payment
            "2" -> {
                when (isFromNew) {
                    true -> binding.root.tvTitle.setText(R.string.add_cash_payment)
                    false -> binding.root.tvTitle.setText(
                        getString(R.string.add_cash_payment).replace(
                            "Add",
                            "Edit"
                        )
                    )
                }
                llAddSaleCash.visibility = View.VISIBLE
                getLedgerdd("cash")
            }
            // add bank receipt
            "3" -> {
                when (isFromNew) {
                    true -> binding.root.tvTitle.setText(R.string.add_bank_receipt)
                    false -> binding.root.tvTitle.setText(
                        getString(R.string.add_bank_receipt).replace(
                            "Add",
                            "Edit"
                        )
                    )
                }
                llSaleAddbank.visibility = View.VISIBLE
                getLedgerdd("bank")
            }
            // add bank payment
            "4" -> {
                when (isFromNew) {
                    true -> binding.root.tvTitle.setText(R.string.add_bank_payment)
                    false -> binding.root.tvTitle.setText(
                        getString(R.string.add_bank_payment).replace(
                            "Add",
                            "Edit"
                        )
                    )
                }
                llSaleAddbank.visibility = View.VISIBLE
                getLedgerdd("bank")
            }
            // add metal receipt
            "5" -> {
                when (isFromNew) {
                    true -> binding.root.tvTitle.setText(R.string.add_metal_receipt)
                    false -> binding.root.tvTitle.setText(
                        getString(R.string.add_metal_receipt).replace(
                            "Add",
                            "Edit"
                        )
                    )
                }
                llSaleAddMetal.visibility = View.VISIBLE

            }
            //add metal payment
            "6" -> {
                when (isFromNew) {
                    true -> binding.root.tvTitle.setText(R.string.add_metal_payment)
                    false -> binding.root.tvTitle.setText(
                        getString(R.string.add_metal_payment).replace(
                            "Add",
                            "Edit"
                        )
                    )
                }
                llSaleAddMetal.visibility = View.VISIBLE
            }
            //add rate-cut
            "7" -> {
                when (isFromNew) {
                    true -> {
                        binding.root.tvTitle.setText(R.string.add_ratecut)
                        // selectedRateCut = "fine"
                        getDefaultTerm(isFromNew)
                    }
                    false -> {
                        binding.root.tvTitle.setText(
                            getString(R.string.add_ratecut).replace(
                                "Add",
                                "Edit"
                            )
                        )
                        getDefaultTerm(isFromNew)
                    }
                }
                llSaleAddRatecut.visibility = View.VISIBLE

            }
            //add adjustment
            "8" -> {
                when (isFromNew) {
                    true -> {
                        binding.root.tvTitle.setText(R.string.add_adjustment)

                    }
                    false -> binding.root.tvTitle.setText(
                        getString(R.string.add_adjustment).replace(
                            "Add",
                            "Edit"
                        )
                    )
                }
                getLedgerdd("round_off")
                llSaleAddAdjustment.visibility = View.VISIBLE
                if (is_gst_applicable) {
                    binding.tvFineAdjustAddSaleLine.visibility = View.GONE
                    binding.radiogroupAdjustment.visibility = View.GONE
                    selectedMetalAdjustment = ""

                } else {
                    binding.tvFineAdjustAddSaleLine.visibility = View.VISIBLE
                    binding.radiogroupAdjustment.visibility = View.VISIBLE
                    selectedMetalAdjustment = "1"
                }

            }
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
                                    "cash" -> {
                                        ledgerSalesNameList = ArrayList<String>()
                                        ledgerSalesList = it.data.data
                                        ledgerSalesNameList =
                                            ledgerSalesList?.map { it.name.toString() }
                                        txtLedgerAddSaleLine.setText(ledgerSalesList!!.get(0).name)
                                        selectedCashLedgerID = ledgerSalesList!!.get(0).ledger_id!!
                                        selectedCashLedgerName = ledgerSalesList!!.get(0).name!!
                                    }
                                    "bank" -> {
                                        ledgerSalesNameList = ArrayList<String>()
                                        ledgerSalesList = it.data.data
                                        ledgerSalesNameList =
                                            ledgerSalesList?.map { it.name.toString() }
                                        txtLedgerBankAddSaleLine.setText(ledgerSalesList!!.get(0).name)
                                        selectedCashLedgerID = ledgerSalesList!!.get(0).ledger_id!!
                                        selectedCashLedgerName = ledgerSalesList!!.get(0).name!!
                                    }
                                    "round_off" -> {
                                        ledgerSalesNameList = ArrayList<String>()
                                        ledgerSalesList = it.data.data
                                        ledgerSalesNameList =
                                            ledgerSalesList?.map { it.name.toString() }
                                        txtLedgerAdjustAddSaleLine.setText(ledgerSalesList!!.get(0).name)
                                        selectedCashLedgerID = ledgerSalesList!!.get(0).ledger_id!!
                                        selectedCashLedgerName = ledgerSalesList!!.get(0).name!!
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

    fun getDefaultTerm(isFromNew: Boolean) {
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
                                when (isFromNew) {
                                    true -> {
                                        txtFineRateCutTerm.setText(fineDefaultTermList!!.get(0).default_term)
                                        selectedFineDefaultTermName =
                                            fineDefaultTermList!!.get(0).default_term!!
                                        selectedFineDefaultTermValue =
                                            fineDefaultTermList!!.get(0).default_term_value!!
                                        selectedRateCut =
                                            fineDefaultTermList!!.get(0).default_term_value!!

                                        txtAmtRateCutTerm.setText(fineDefaultTermList!!.get(1).default_term)
                                        selectedAmtDefaultTermName =
                                            fineDefaultTermList!!.get(1).default_term!!
                                        selectedAmtDefaultTermValue =
                                            fineDefaultTermList!!.get(1).default_term_value!!
                                    }
                                    else -> {

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


    private fun openFineTermMenu(
        fineDefaultTermNameList: List<String>?,
        txtSelectedTextView: AppCompatAutoCompleteTextView
    ) {
        popupMenu = PopupMenu(
            this,
            txtSelectedTextView
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
            txtSelectedTextView.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = fineDefaultTermNameList!!.indexOf(selected)

            selectedFineDefaultTermName =
                pos?.let { it1 -> fineDefaultTermList?.get(it1)?.default_term }.toString()

            selectedFineDefaultTermValue =
                pos?.let { it1 -> fineDefaultTermList?.get(it1)?.default_term_value }.toString()
            selectedRateCut =
                pos?.let { it1 -> fineDefaultTermList?.get(it1)?.default_term_value }.toString()

            when (selectedFineDefaultTermName.equals(fineDefaultTermList!!.get(0).default_term)) {
                true -> {
                    txtAmtRateCutTerm.setText(fineDefaultTermList?.get(1)?.default_term)
                    selectedAmtDefaultTermName = fineDefaultTermList!!.get(1)!!.default_term!!
                    selectedAmtDefaultTermValue =
                        fineDefaultTermList!!.get(1)!!.default_term_value!!
                }
                false -> {
                    txtAmtRateCutTerm.setText(fineDefaultTermList?.get(0)?.default_term)
                    selectedAmtDefaultTermName = fineDefaultTermList!!.get(0)!!.default_term!!
                    selectedAmtDefaultTermValue =
                        fineDefaultTermList!!.get(0)!!.default_term_value!!
                }
            }
            true
        })

        popupMenu.show()
    }

    private fun openAmtTermMenu(
        fineDefaultTermNameList: List<String>?,
        txtSelectedTextView: AppCompatAutoCompleteTextView
    ) {
        popupMenu = PopupMenu(
            this,
            txtSelectedTextView
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
            txtSelectedTextView.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = fineDefaultTermNameList!!.indexOf(selected)

            selectedAmtDefaultTermName =
                pos?.let { it1 -> fineDefaultTermList?.get(it1)?.default_term }.toString()

            selectedAmtDefaultTermValue =
                pos?.let { it1 -> fineDefaultTermList?.get(it1)?.default_term_value }.toString()


            when (selectedAmtDefaultTermName.equals(fineDefaultTermList!!.get(0).default_term)) {
                true -> {
                    txtFineRateCutTerm.setText(fineDefaultTermList?.get(1)?.default_term)
                    selectedFineDefaultTermName = fineDefaultTermList!!.get(1)!!.default_term!!
                    selectedFineDefaultTermValue =
                        fineDefaultTermList!!.get(1)!!.default_term_value!!
                    selectedRateCut = fineDefaultTermList!!.get(1)!!.default_term_value!!
                }
                false -> {
                    txtFineRateCutTerm.setText(fineDefaultTermList?.get(0)?.default_term)
                    selectedFineDefaultTermName = fineDefaultTermList!!.get(0)!!.default_term!!
                    selectedFineDefaultTermValue =
                        fineDefaultTermList!!.get(0)!!.default_term_value!!
                    selectedRateCut = fineDefaultTermList!!.get(0)!!.default_term_value!!
                }
            }
            true
        })

        popupMenu.show()
    }


    private fun openCashLedgerMenu(
        ledgerSalesNameList: List<String>?,
        txtSelectedTextView: AppCompatAutoCompleteTextView
    ) {
        popupMenu = PopupMenu(
            this,
            txtSelectedTextView
        )
        for (i in 0 until ledgerSalesNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                ledgerSalesNameList.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            txtSelectedTextView.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = ledgerSalesNameList.indexOf(selected)

            selectedCashLedgerID =
                pos?.let { it1 -> ledgerSalesList?.get(it1)?.ledger_id }.toString()

            selectedCashLedgerName =
                pos?.let { it1 -> ledgerSalesList?.get(it1)?.name }.toString()

            true
        })

        popupMenu.show()
    }


    fun getSearchItem() {
        if (NetworkUtils.isConnected()) {
            viewModel.getItemSearch(
                loginModel?.data?.bearer_access_token,
                binding.txtItemNameAddItemFineMetal.text.toString(),
                "metal_payment_receipt",
                ""
            ).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {

                            if (it.data?.status == true) {
                                itemList = it.data.data

                                itemNameList = itemList?.map { it.item_name.toString() }


                                ItemDetailsAdapter = ItemDetailsAdapter(
                                    this, true, R.layout.search_item_popup,
                                    itemList!!
                                )

                                binding.txtItemNameAddItemFineMetal.setAdapter(ItemDetailsAdapter)
                                binding.txtItemNameAddItemFineMetal.threshold = 1

                                binding.txtItemNameAddItemFineMetal.setOnItemClickListener { adapterView, view, position, l
                                    ->
                                    val selected: String =
                                        adapterView.getItemAtPosition(position).toString()

                                    val pos: Int? = itemList?.get(0)?.item_name?.indexOf(selected)


                                    val selectedPoi =
                                        adapterView.adapter.getItem(position) as ItemSearchModel.ItemSearch?


                                    selectedItemID = selectedPoi?.id.toString()
                                    binding.txtItemNameAddItemFineMetal.setText(selectedPoi?.item_name)
                                    selectedItemName = selectedPoi?.item_name
                                    binding.txtItemNameAddItemFineMetal.setSelection(selectedPoi?.item_name?.length!!)
                                    selectedMetalItemId = selectedPoi?.metal_type_id
                                    selectedItemMaintainStockName =
                                        selectedPoi?.maintain_stock_in_name
                                    binding.tvGrosWtAddFineMe.hint =
                                        "Gross Weight (" + selectedItemMaintainStockName + ")"
                                    //  CommonUtils.hideKeyboard(this,txtItemNameAddItem)

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
                            Toast.makeText(this, it.data?.errormessage?.message, Toast.LENGTH_LONG)
                                .show()
                            Log.v("..setupObservers..", "..ERROR...")
                        }
                        Status.LOADING -> {
                            Log.v("..setupObservers..", "..LOADING...")
                        }
                    }
                }
            })
        }

    }

    private fun saveSalesLineModel() {

        if (prefs.contains(Constants.PREF_SALES_LINE_INFO_KEY)) {
            val collectionType = object :
                TypeToken<ArrayList<SalesLineModel.SaleLineModelDetails>>() {}.type
            var itemList: ArrayList<SalesLineModel.SaleLineModelDetails> =
                Gson().fromJson(prefs[Constants.PREF_SALES_LINE_INFO_KEY, ""], collectionType)
            addsaleLineList.addAll(itemList)
        } else {
            addsaleLineList = ArrayList()
        }

        when (checkedRowNo) {
            "1" -> {
                childItemModel = SalesLineModel.SaleLineModelDetails(
                    binding.txtAmountAddSaleLine.text.toString(),
                    selectedCashLedgerID,
                    selectedCashLedgerName,
                    binding.txtRemarkAddCash.text.toString(),
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
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "cash_receipt",
                    "cash_receipt",
                    "Cash Receipt"

                )
            }
            "2" -> {
                childItemModel = SalesLineModel.SaleLineModelDetails(
                    binding.txtAmountAddSaleLine.text.toString(),
                    selectedCashLedgerID,
                    selectedCashLedgerName,
                    binding.txtRemarkAddCash.text.toString(),
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
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "cash_payment",
                    "cash_payment",
                    "Cash Payment"
                )
            }
            "3" -> {
                childItemModel = SalesLineModel.SaleLineModelDetails(
                    "",
                    "",
                    "",
                    "",
                    binding.txtAmountBankAddSaleLine.text.toString(),
                    selectedCashLedgerID,
                    selectedCashLedgerName,
                    checkedBankMode,
                    binding.txtChequeAddSaleLine.text.toString(),
                    binding.txtDateAddSaleLine.text.toString(),
                    binding.txtFavNameAddSaleLine.text.toString(),
                    binding.txtDeductChargAddSaleLine.text.toString(),
                    binding.txtChargePerAddSaleLine.text.toString(),
                    binding.txtFinalAmtAddSaleLine.text.toString(),
                    binding.txtBankNameAddSaleLine.text.toString(),
                    binding.txtBankAccNoAddSaleLine.text.toString(),
                    binding.txtIfsCodeAddSaleLine.text.toString(),
                    binding.txtUTRNoAddSaleLine.text.toString(),
                    binding.txtDescriptionAddSaleLine.text.toString(),
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
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "bank_receipt",
                    "bank_receipt",
                    "Bank Receipt"
                )
            }
            "4" -> {
                childItemModel = SalesLineModel.SaleLineModelDetails(
                    "",
                    "",
                    "",
                    "",
                    binding.txtAmountBankAddSaleLine.text.toString(),
                    selectedCashLedgerID,
                    selectedCashLedgerName,
                    checkedBankMode,
                    binding.txtChequeAddSaleLine.text.toString(),
                    binding.txtDateAddSaleLine.text.toString(),
                    binding.txtFavNameAddSaleLine.text.toString(),
                    binding.txtDeductChargAddSaleLine.text.toString(),
                    binding.txtChargePerAddSaleLine.text.toString(),
                    binding.txtFinalAmtAddSaleLine.text.toString(),
                    binding.txtBankNameAddSaleLine.text.toString(),
                    binding.txtBankAccNoAddSaleLine.text.toString(),
                    binding.txtIfsCodeAddSaleLine.text.toString(),
                    binding.txtUTRNoAddSaleLine.text.toString(),
                    binding.txtDescriptionAddSaleLine.text.toString(),
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
                    "", "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "bank_payment", "bank_payment", "Bank Payment"

                )
            }
            "5" -> {
                childItemModel = SalesLineModel.SaleLineModelDetails(
                    binding.txtAmountAddSaleLine.text.toString(),
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
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    selectedItemID,
                    selectedItemName,
                    selectedMetalItemId,
                    selectedItemMaintainStockName,
                    binding.txtGrossWtAddFineMe.text.toString(),
                    binding.txtLessWtAddFineMe.text.toString(),
                    binding.txtNetWtAddFineMe.text.toString(),
                    binding.txtTouchAddFineMe.text.toString(),
                    binding.txtWastageAddFineMe.text.toString(),
                    binding.txtFineWtAddFineMe.text.toString(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "metal_receipt",
                    "metal_receipt",
                    "Metal Receipt"
                )
            }
            "6" -> {
                childItemModel = SalesLineModel.SaleLineModelDetails(
                    binding.txtAmountAddSaleLine.text.toString(),
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
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    selectedItemID,
                    selectedItemName,
                    selectedMetalItemId,
                    selectedItemMaintainStockName,
                    binding.txtGrossWtAddFineMe.text.toString(),
                    binding.txtLessWtAddFineMe.text.toString(),
                    binding.txtNetWtAddFineMe.text.toString(),
                    binding.txtTouchAddFineMe.text.toString(),
                    binding.txtWastageAddFineMe.text.toString(),
                    binding.txtFineWtAddFineMe.text.toString(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "metal_payment",
                    "metal_payment",
                    "Metal Payment"
                )

            }
            "7" -> {
                childItemModel = SalesLineModel.SaleLineModelDetails(
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
                    "",
                    "",
                    "",
                    "",
                    binding.txtGoldrateRateCutFine.text.toString(),
                    binding.txtAmountRatecutFine.text.toString(),
                    selectedRateCut,
                    selectedMetalRateCut,
                    binding.txtFineWtRateCutFine.text.toString(),
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
                    "",
                    "rate_cut",
                    "rate_cut",
                    "Rate Cut"

                )

            }
            "8" -> {
                childItemModel = SalesLineModel.SaleLineModelDetails(
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
                    "",
                    "",
                    "",
                    "",
                    binding.txtFineAdjustAddSaleLine.text.toString(),
                    selectedMetalAdjustment,
                    binding.txtAmountAdjustAddSaleLine.text.toString(),
                    selectedCashLedgerID,
                    selectedCashLedgerName,
                    binding.txtRemarkAdjustAddSaleLine.text.toString(),
                    "adjustment",
                    "adjustment",
                    "Adjustment"
                )

            }
        }
        if (receivedIssuePosition >= 0 && receivedIssuePosition != -1) {
            // Update selected issueReceive
            addsaleLineList.set(receivedIssuePosition, childItemModel)
        } else {
            // Add new issue
            addsaleLineList.add(childItemModel)
        }

        prefs[Constants.PREF_SALES_LINE_INFO_KEY] = Gson().toJson(addsaleLineList)

    }


}