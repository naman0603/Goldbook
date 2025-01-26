package com.goldbookapp.ui.activity.additem


import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.QRCodeScannerActivity
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.AddItemActivityBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.viewmodel.AddItemViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.ItemDetailsAdapter
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_accounting_info.*
import kotlinx.android.synthetic.main.add_item_activity.*
import kotlinx.android.synthetic.main.new_invoice_activity.*
import kotlinx.android.synthetic.main.sales_bill_detail_activity.*
import kotlinx.android.synthetic.main.tag_dialog.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import android.widget.LinearLayout
import java.math.MathContext


class AddItemActivity : AppCompatActivity() {
    private var isLoadedOnce: Boolean = false
    private var isselectedItemLoadOnce: Boolean = false
    private var countSearchItem: Int = 0

    private var is_From_New_Opening: Boolean = false
    private lateinit var viewModel: AddItemViewModel
    lateinit var binding: AddItemActivityBinding
    lateinit var popupMenu: PopupMenu

    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel

    var qr_code: String? = ""
    var selectedCustStateId: String? = ""
    var transaction_type: String? = ""
    var Transaction_ID: String? = ""
    var itemList: ArrayList<ItemSearchModel.ItemSearch>? = null
    var itemNameList: List<String>? = null
    lateinit var ItemDetailsAdapter: ItemDetailsAdapter
    var selectedItemID: String = ""
    var selectedItemName: String = ""
    var selectedItemMetalType: String = ""
    var selectedItemLedgerName: String = ""
    var selectedItemLedgerId: String = ""
    var selectedItemHsn: String = ""
    var selectedItemGst: String = "0.00"
    var selectedItemGstId: String = ""
    var selectedItemType: String = ""
    var selectedTagNo: String = ""
    var selectedTagRandomId: String = ""

    var wastage: BigDecimal = BigDecimal(0)
    var wastageUpdatedValue: String = "0.00"
    var touchUpdatedValue: String = "0.00"
    var grossUpdatedValue: String = "0.000"
    var lesswtUpdatedValue: String = "0.000"
    var finewtUpdatedValue: String = "0.000"
    var goldRateUpdatedValue: String = "0.00"
    var discountUpdatedValue: String = "0.00"
    var chargeUpdatedValue: String = "0.00"
    var toatlAmtUpdatedValue: String = "0.00"
    var toatltaxAmtUpdatedValue: String = "0.00"
    var sgstUpdatedValue: String = "0.00"
    var cgstUpdatedValue: String = "0.00"
    var igstUpdatedValue: String = "0.00"
    var toatlAmtChargeUpdatedValue: String = "0.00"


    var stampList: List<ItemStampModel.Data.Stamp>? = null
    var stampNameList: List<String>? = null
    var selectedStampId: String = ""
    var selectedStampName: String = ""

    var is_studed: String = ""
    var useColor: String = ""
    var colorList: List<ItemSearchModel.ItemSearch.Color>? = null
    var colorNameList: List<String>? = null
    var selectedColorId: String = ""
    var selectedColorName: String = ""
    var is_tax_preferrence: String = ""
    var item_type: String = ""

    var stockSaveAdd: String? = "0"
    lateinit var selectedItemModel: ItemSearchModel.ItemSearch
    var piecesUpdatedValue: String = "0.00"
    lateinit var lessweightBreakupList: OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup
    lateinit var makingChargeBreakupList: OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup
    val df = DecimalFormat("0.00")
    val df1 = DecimalFormat("0.000")

    var item_maintain_stock_in_id: String = ""
    var item_maintain_stock_in_name: String = ""
    var item_metal_type_id: String = ""
    var item_metal_type_name: String = ""
    var item_unit_id: String = ""
    var item_unit_name: String = ""
    var item_use_stamp: String = ""
    var net_wt: String = ""
    var addopeningStockItemList = ArrayList<OpeningStockItemModel.OpeningStockItemModelItem>()
    var addopeningStockCalcItemList =
        ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>()
    lateinit var addOpeningStockItemModel: OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem
    private var new_openingStock_pos: Int = -1
    private var new_openingStockCalc_pos: Int = -1

    lateinit var unitArrayList: List<ItemSearchModel.ItemSearch.Unit_array>


    lateinit var lessweightCalcBreakupList: OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup

    //lateinit var makingChargeCalcBreakupList: OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem.ItemChargesBreakup
    lateinit var makingChargeCalcBreakupList: OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup


    lateinit var tagDetailsModelItem: TagDetailItemModel
    var is_Edit_OpeningStock_Item: Boolean = false
    var opneningStock_Item_Position: Int = -1
    var is_Less_Weight_click: Boolean? = false
    var is_gst_applicable: String? = "0"

    var convertedGrosswt: String = "0.00"
    var convertedNetwt: String = "0.00"
    var convertedFinewt: String = "0.00"


    var netweightUpdatedValue: String = "0.000"
    var makingChrgsUpdatedValue: String = "0.00"
    var totalmakingChrgsUpdatedValue: String = "0.00"
    var selectedMakingPerID: String = ""
    var is_Igst_enable: Boolean = false

    var totalOtherChrgsUpdatedValue: String = "0.00"
    var otherChrgs1UpdatedValue: String = "0.00"
    var otherChrgs2UpdatedValue: String = "0.00"
    var otherChrgs3UpdatedValue: String = "0.00"
    var otherChrgs4UpdatedValue: String = "0.00"
    var otherChrgs5UpdatedValue: String = "0.00"
    var otherChrgs1CalculatedUpdatedValue: String = "0.00"
    var otherChrgs2CalculatedUpdatedValue: String = "0.00"
    var otherChrgs3CalculatedUpdatedValue: String = "0.00"
    var otherChrgs4CalculatedUpdatedValue: String = "0.00"
    var otherChrgs5CalculatedUpdatedValue: String = "0.00"
    var totalLwChargesUpdatedValue: String = "0.00"
    lateinit var lessweightList: ArrayList<AddLessWeightModel.AddLessWeightModelItem>
    lateinit var lesswtTotal: ArrayList<String> // it is for total calculation of less wt charges
    lateinit var listoflesswtChargesDetail: ArrayList<CalculationPaymentModel.DataPayment.ItemPayment.LessWeights> // this list is for less wt charges adapter
    var totalOfAllChrgsUpdatedValue: String = "0.00"
    var addOtherChargeList =
        ArrayList<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray>()

    var item_name: String = ""
    var selectedRateon: String = "fine"
    var ledgerSalesNameList: List<String>? = null
    var ledgerPurchaseNameList: List<String>? = null
    var ledgerSalesList: List<SearchLedgerModel.LedgerDetails>? = null
    var ledgerPurchaseList: List<SearchLedgerModel.LedgerDetails>? = null

    lateinit var taxAnalysisModel: OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem.Tax_analysis_array
    lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.add_item_activity)

        setupViewModel()
        setupUIandListner()
    }

    private fun setupUIandListner() {

        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        )
        binding.root.imgLeft.setImageResource(R.drawable.ic_back)

        binding.root.imgLeft.clickWithDebounce {
            onBackPressed()
        }
        tvTitle.setText(R.string.add_item)

        if (loginModel.data?.branch_info?.branch_type.equals("1", true)) {
            is_gst_applicable = "1"
        } else {
            is_gst_applicable = "0"
        }


        clearPref()
        applyingDigitFilter()
        ontextChangeSetup()
        onFocusChangeSetup()
        getDataFromIntent()

        when (transaction_type) {
            "sales" -> {
                binding.cardBarcodeNewItem.visibility = View.VISIBLE
                binding.cardAddBarcodeManually.visibility = View.VISIBLE

            }
            "payment" -> {
                binding.cardBarcodeNewItem.visibility = View.VISIBLE
                binding.cardAddBarcodeManually.visibility = View.VISIBLE
            }
            else -> {
                binding.cardBarcodeNewItem.visibility = View.GONE
                binding.cardAddBarcodeManually.visibility = View.GONE
                binding.tvTagNo.visibility = View.GONE
            }
        }

        //item_rate and discout UI
        when(transaction_type){
            "opening_stock"->{
                binding.tvRateonAddItem.visibility = View.GONE
                binding.tvDiscountAddItem.visibility = View.GONE
            }
            else->{
                binding.tvRateonAddItem.visibility = View.VISIBLE
                binding.tvDiscountAddItem.visibility = View.VISIBLE
            }
        }

        binding.txtChargesAddItem.clickWithDebounce {
            var salesmakingcharges: String = ""
            var purchasemakingchargeas: String = ""
            if (!selectedItemID.equals("")) {
                when (transaction_type) {
                    "opening_stock" -> {
                        salesmakingcharges = "0.00"
                        purchasemakingchargeas = "0.00"
                    }
                    "receipt" -> {
                        salesmakingcharges = "0.00"
                        purchasemakingchargeas = "0.00"
                    }
                    "payment" -> {
                        salesmakingcharges = "0.00"
                        purchasemakingchargeas = "0.00"
                    }
                    // rest of transactions (sales/purchase/payment/receipt
                    else -> {
                        salesmakingcharges = makingChrgsUpdatedValue
                        purchasemakingchargeas = makingChrgsUpdatedValue
                    }
                }
                when (is_Edit_OpeningStock_Item) {
                    true -> {
                        selectedItemModel = ItemSearchModel.ItemSearch(
                            "",
                            arrayListOf(),
                            "",
                            selectedItemType,
                            is_studed,
                            "",
                            selectedMakingPerID,
                            item_maintain_stock_in_name,
                            "",
                            "",
                            "",
                            item_unit_name,
                            piecesUpdatedValue,
                            net_wt,
                            "",
                            "",
                            unitArrayList,
                            binding.txtWastageAddItem.text.toString().trim(),
                            salesmakingcharges,
                            "",
                            "",
                            "",
                            "",
                            "",
                            binding.txtWastageAddItem.text.toString().trim(),
                            purchasemakingchargeas,
                            "", "", "", "", ""
                        )


                    }
                    false -> {
                        selectedItemModel = ItemSearchModel.ItemSearch(
                            "",
                            arrayListOf(),
                            "",
                            selectedItemType,
                            is_studed,
                            "",
                            selectedMakingPerID,
                            item_maintain_stock_in_name,
                            "",
                            "",
                            "",
                            item_unit_name,
                            piecesUpdatedValue,
                            net_wt,
                            "",
                            "",
                            unitArrayList,
                            binding.txtWastageAddItem.text.toString().trim(),
                            salesmakingcharges, "", "", "",
                            "", "",
                            binding.txtWastageAddItem.text.toString().trim(),
                            purchasemakingchargeas, "", "", "", "", ""
                        )
                        selectedItemModel.unit_value = piecesUpdatedValue
                        selectedItemModel.net_wt = binding.txtNetWtAddItem.text.toString().trim()
                    }
                }

                clearFocus()
                startActivity(
                    Intent(
                        this,
                        AddItemChargesActivity::class.java
                    ).putExtra(Constants.SELECTED_ITEM_DATA_MODEL, Gson().toJson(selectedItemModel))
                        .putExtra("Edit_New",is_Edit_OpeningStock_Item)
                )

            }

        }

        binding.cardBarcodeNewItem.clickWithDebounce {
            startActivity(
                Intent(this, QRCodeScannerActivity::class.java)
                    .putExtra(Constants.TRANSACTION_TYPE, transaction_type)
                    .putExtra(Constants.CUST_STATE_ID, selectedCustStateId)
            )
            finish()
        }

        binding.cardAddBarcodeManually.clickWithDebounce {
            openTagManuallyDialog()
        }

        binding.txtRateonAddItem.clickWithDebounce {
            openRateonPopup()
        }

        binding.txtLessWtAddItem.clickWithDebounce {

            clearFocus()
            if (is_studed.equals("0", true)) {
                startActivity(
                    Intent(this, SimpleLessWeightActivity::class.java)
                        .putExtra(
                            Constants.IS_FROM_NEW_INVOICE_LESS_WEIGHT, true
                        )
                )
            } else {
                startActivity(
                    Intent(this, LessWeightDetailsActivity::class.java)
                )
            }

        }


        binding.txtStampAddItem.clickWithDebounce {
            if (!selectedItemID.equals("")) {
                if (item_use_stamp.equals("1")) {
                    if (stampList!!.size > 0) {
                        openStampMenu(stampNameList)
                    }
                }
            }

        }

        binding.txtColourAddItem.clickWithDebounce {

            if (!selectedItemID.equals("")) {
                if (colorList!!.size > 0) {
                    Log.v("openpopup", "true")
                    openColorMenu(colorNameList)

                }
            }
        }

        binding.txtLdgerAddItem.clickWithDebounce {
            when (transaction_type) {
                "sales" -> {
                    openSalesLedgerMenu(ledgerSalesNameList)
                }
                "purchase" -> {
                    openPurchaseLedgerMenu(ledgerPurchaseNameList)
                }
            }
        }



        binding.btnSaveAddAddItemOpeningStock.clickWithDebounce {
            if (performValidation()) {
                clearFocus()
                when (transaction_type) {
                    "opening_stock" -> {
                        saveOpeningStockItemModel()
                        saveOpeningStockCalcItemModel()
                    }
                    "sales" -> {
                        saveOpeningStockCalcItemModel()
                    }
                    "purchase" -> {
                        saveOpeningStockCalcItemModel()
                    }
                    "receipt" -> {
                        saveOpeningStockCalcItemModel()
                    }
                    "payment" -> {
                        saveOpeningStockCalcItemModel()
                    }
                }
                startActivity(
                    Intent(
                        this,
                        AddItemActivity::class.java
                    ).putExtra(Constants.TRANSACTION_TYPE, transaction_type)
                )
                finish()
            }
        }



        binding.btnSaveCloseAddItemOpeningStock.clickWithDebounce {
            if (performValidation()) {
                clearFocus()
                when (transaction_type) {
                    "opening_stock" -> {
                        saveOpeningStockItemModel()
                        saveOpeningStockCalcItemModel()
                    }
                    "sales" -> {
                        saveOpeningStockCalcItemModel()
                    }
                    "purchase" -> {
                        saveOpeningStockCalcItemModel()
                    }
                    "receipt" -> {
                        saveOpeningStockCalcItemModel()
                    }
                    "payment" -> {
                        saveOpeningStockCalcItemModel()
                    }
                }

                finish()
            }

        }


    }


    fun openRateonPopup() {
        popupMenu = PopupMenu(this, binding.txtRateonAddItem)
        if (selectedItemMetalType.equals("Other")) {
            popupMenu.menu.add("Gross Weight")
            popupMenu.menu.add("Fix")
        } else {
            popupMenu.menu.add("Fine Weight")
            popupMenu.menu.add("Net Weight")
            popupMenu.menu.add("Gross Weight")
            popupMenu.menu.add("Fix")
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            (binding.txtRateonAddItem as TextInputEditText).setText(item.title)
            selectedRateon = when (item.title) {
                "Fine Weight" -> {
                    updateTotalamtFromFine()
                    "fine"
                }
                "Net Weight" -> {
                    updateTotalamtFromNet()
                    "net"

                }
                "Gross Weight" -> {
                    updateTotalAmtFromGross()
                    "gross"
                }
                else -> {
                    updateTotalAmtFromFix()
                    "fix"
                }
            }
            true
        })

        popupMenu.show()
    }

    private fun updateTotalAmtFromFix() {
        if(!goldRateUpdatedValue.equals("0.00")){
            val goldRate: BigDecimal = goldRateUpdatedValue.toBigDecimal()

            val totalAmt :String = goldRate.toString()

            toatlAmtUpdatedValue = totalAmt
            when (discountUpdatedValue.toBigDecimal().compareTo(BigDecimal.ZERO) == 0) {
                true -> {
                    toatlAmtUpdatedValue = totalAmt
                    binding.txtTotalAmountAddItem.setText(toatlAmtUpdatedValue)
                }
                false -> {
                    val totalAmtwithCharge: BigDecimal = toatlAmtUpdatedValue.toBigDecimal()
                    val discount: BigDecimal = discountUpdatedValue.toBigDecimal()
                    val totalAmtWithdiscount : String = (totalAmtwithCharge - discount).toString()
                    toatlAmtUpdatedValue =  totalAmtWithdiscount
                    binding.txtTotalAmountAddItem.setText(toatlAmtUpdatedValue)
                }
            }

            when (chargeUpdatedValue.toBigDecimal().compareTo(BigDecimal.ZERO) > 0) {
                true -> {
                    val chargeRate: BigDecimal = chargeUpdatedValue.toBigDecimal()
                    val totalGoldCharge: BigDecimal = toatlAmtUpdatedValue.toBigDecimal()
                    val totalAmtWithCharge: String =
                        ((totalGoldCharge.setScale(2)
                            .plus(chargeRate.setScale(2))
                                )).setScale(2, RoundingMode.HALF_UP).toString()

                    toatlAmtChargeUpdatedValue = totalAmtWithCharge
                    binding.txtTotalAmountAddItem.setText(toatlAmtChargeUpdatedValue)

                }
                false -> {
                    toatlAmtChargeUpdatedValue = toatlAmtUpdatedValue
                    binding.txtTotalAmountAddItem.setText(toatlAmtChargeUpdatedValue)
                }
            }


            if (selectedCustStateId.equals(loginModel.data!!.branch_info!!.state_id.toString())) {
                Log.v("testiuoi", "")
                is_Igst_enable = false
                updateTaxAmout(is_Igst_enable)
            } else {
                is_Igst_enable = true
                updateTaxAmout(is_Igst_enable)
            }

        }
    }

    private fun updateTotalAmtFromGross() {
        if(!goldRateUpdatedValue.equals("0.00")){
            val goldRate: BigDecimal = goldRateUpdatedValue.toBigDecimal()
            val grossWeight: BigDecimal = grossUpdatedValue.toBigDecimal()
            val totalAmt :String =
                ((goldRate.setScale(3)
                    .multiply(grossWeight.setScale(3))
                        )).setScale(2, RoundingMode.HALF_UP).toString()

            toatlAmtUpdatedValue = totalAmt
            when (discountUpdatedValue.toBigDecimal().compareTo(BigDecimal.ZERO) == 0) {
                true -> {
                    toatlAmtUpdatedValue = totalAmt
                    binding.txtTotalAmountAddItem.setText(toatlAmtUpdatedValue)
                }
                false -> {
                    val totalAmtwithCharge: BigDecimal = toatlAmtUpdatedValue.toBigDecimal()
                    val discount: BigDecimal = discountUpdatedValue.toBigDecimal()
                    val totalAmtWithdiscount : String = (totalAmtwithCharge - discount).toString()
                    toatlAmtUpdatedValue =  totalAmtWithdiscount
                    binding.txtTotalAmountAddItem.setText(toatlAmtUpdatedValue)
                }
            }

            when (chargeUpdatedValue.toBigDecimal().compareTo(BigDecimal.ZERO) > 0) {
                true -> {
                    val chargeRate: BigDecimal = chargeUpdatedValue.toBigDecimal()
                    val totalGoldCharge: BigDecimal = toatlAmtUpdatedValue.toBigDecimal()
                    val totalAmtWithCharge: String =
                        ((totalGoldCharge.setScale(2)
                            .plus(chargeRate.setScale(2))
                                )).setScale(2, RoundingMode.HALF_UP).toString()

                    toatlAmtChargeUpdatedValue = totalAmtWithCharge
                    binding.txtTotalAmountAddItem.setText(toatlAmtChargeUpdatedValue)

                }
                false -> {
                    toatlAmtChargeUpdatedValue = toatlAmtUpdatedValue
                    binding.txtTotalAmountAddItem.setText(toatlAmtChargeUpdatedValue)
                }
            }


            if (selectedCustStateId.equals(loginModel.data!!.branch_info!!.state_id.toString())) {
                Log.v("testiuoi", "")
                is_Igst_enable = false
                updateTaxAmout(is_Igst_enable)
            } else {
                is_Igst_enable = true
                updateTaxAmout(is_Igst_enable)
            }

        }
    }

    private fun updateTotalamtFromNet() {
        if(!goldRateUpdatedValue.equals("0.00")){
            val goldRate: BigDecimal = goldRateUpdatedValue.toBigDecimal()
            val netWeight: BigDecimal = netweightUpdatedValue.toBigDecimal()
            val totalAmt :String =
                ((goldRate.setScale(3)
                    .multiply(netWeight.setScale(3))
                        )).setScale(2, RoundingMode.HALF_UP).toString()

            toatlAmtUpdatedValue = totalAmt
            when (discountUpdatedValue.toBigDecimal().compareTo(BigDecimal.ZERO) == 0) {
                true -> {
                    toatlAmtUpdatedValue = totalAmt
                    binding.txtTotalAmountAddItem.setText(toatlAmtUpdatedValue)
                }
                false -> {
                    val totalAmtwithCharge: BigDecimal = toatlAmtUpdatedValue.toBigDecimal()
                    val discount: BigDecimal = discountUpdatedValue.toBigDecimal()
                    val totalAmtWithdiscount : String = (totalAmtwithCharge - discount).toString()
                    toatlAmtUpdatedValue =  totalAmtWithdiscount
                    binding.txtTotalAmountAddItem.setText(toatlAmtUpdatedValue)
                }
            }

            when (chargeUpdatedValue.toBigDecimal().compareTo(BigDecimal.ZERO) > 0) {
                true -> {
                    val chargeRate: BigDecimal = chargeUpdatedValue.toBigDecimal()
                    val totalGoldCharge: BigDecimal = toatlAmtUpdatedValue.toBigDecimal()
                    val totalAmtWithCharge: String =
                        ((totalGoldCharge.setScale(2)
                            .plus(chargeRate.setScale(2))
                                )).setScale(2, RoundingMode.HALF_UP).toString()

                    toatlAmtChargeUpdatedValue = totalAmtWithCharge
                    binding.txtTotalAmountAddItem.setText(toatlAmtChargeUpdatedValue)

                }
                false -> {
                    toatlAmtChargeUpdatedValue = toatlAmtUpdatedValue
                    binding.txtTotalAmountAddItem.setText(toatlAmtChargeUpdatedValue)
                }
            }


            if (selectedCustStateId.equals(loginModel.data!!.branch_info!!.state_id.toString())) {
                Log.v("testiuoi", "")
                is_Igst_enable = false
                updateTaxAmout(is_Igst_enable)
            } else {
                is_Igst_enable = true
                updateTaxAmout(is_Igst_enable)
            }

        }
    }

    private fun updateTotalamtFromFine() {
        if(!goldRateUpdatedValue.equals("0.00")){
            val goldRate: BigDecimal = goldRateUpdatedValue.toBigDecimal()
            val fineWeight: BigDecimal = finewtUpdatedValue.toBigDecimal()
            val totalAmt :String =
                ((goldRate.setScale(3)
                    .multiply(fineWeight.setScale(3))
                        )).setScale(2, RoundingMode.HALF_UP).toString()

            toatlAmtUpdatedValue = totalAmt




            when (discountUpdatedValue.toBigDecimal().compareTo(BigDecimal.ZERO) == 0) {
                true -> {
                    toatlAmtUpdatedValue = totalAmt
                    binding.txtTotalAmountAddItem.setText(toatlAmtUpdatedValue)
                }
                false -> {

                    val totalAmtwithCharge: BigDecimal = toatlAmtUpdatedValue.toBigDecimal()
                    val discount: BigDecimal = discountUpdatedValue.toBigDecimal()
                    val totalAmtWithdiscount : String = (totalAmtwithCharge - discount).toString()
                    toatlAmtUpdatedValue =  totalAmtWithdiscount
                    binding.txtTotalAmountAddItem.setText(toatlAmtUpdatedValue)
                }
            }
            when (chargeUpdatedValue.toBigDecimal().compareTo(BigDecimal.ZERO) > 0) {
                true -> {
                    val chargeRate: BigDecimal = chargeUpdatedValue.toBigDecimal()
                    val totalGoldCharge: BigDecimal = toatlAmtUpdatedValue.toBigDecimal()
                    val totalAmtWithCharge: String =
                        ((totalGoldCharge.setScale(2)
                            .plus(chargeRate.setScale(2))
                                )).setScale(2, RoundingMode.HALF_UP).toString()

                    toatlAmtChargeUpdatedValue = totalAmtWithCharge
                    binding.txtTotalAmountAddItem.setText(toatlAmtChargeUpdatedValue)

                }
                false -> {
                    toatlAmtChargeUpdatedValue = toatlAmtUpdatedValue
                    binding.txtTotalAmountAddItem.setText(toatlAmtChargeUpdatedValue)
                }
            }


            if (selectedCustStateId.equals(loginModel.data!!.branch_info!!.state_id.toString())) {
                Log.v("testiuoi", "")
                is_Igst_enable = false
                updateTaxAmout(is_Igst_enable)
            } else {
                is_Igst_enable = true
                updateTaxAmout(is_Igst_enable)
            }

        }
    }


    private fun openTagManuallyDialog() {

        dialog = Dialog(this, R.style.Full_Dialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        );

        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.tag_dialog)

        dialog.tvTagSave.clickWithDebounce {
            qr_code = dialog.txtAddTag.text.toString()
            getTagDetailsApi(loginModel.data?.bearer_access_token, qr_code)
            disableFieldForQRCode()
            binding.tvTagNo.visibility = View.VISIBLE
            dialog.dismiss()
        }
        dialog.tvTagCancel.clickWithDebounce {
            dialog.dismiss()
        }
        dialog.show()

    }

    private fun openSalesLedgerMenu(ledgerSalesNameList: List<String>?) {
        popupMenu = PopupMenu(this, binding.txtLdgerAddItem)
        for (i in 0 until ledgerSalesNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                ledgerSalesNameList.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtLdgerAddItem.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = ledgerSalesNameList.indexOf(selected)

            selectedItemLedgerId =
                pos?.let { it1 -> ledgerSalesList?.get(it1)?.ledger_id }.toString()


            selectedItemLedgerName = pos?.let { it1 -> ledgerSalesList?.get(it1)?.name }.toString()

            true
        })

        popupMenu.show()
    }

    private fun openPurchaseLedgerMenu(ledgerPurchaseNameList: List<String>?) {
        popupMenu = PopupMenu(this, binding.txtLdgerAddItem)
        for (i in 0 until ledgerPurchaseNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                ledgerPurchaseNameList.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtLdgerAddItem.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = ledgerPurchaseNameList.indexOf(selected)

            selectedItemLedgerId =
                pos?.let { it1 -> ledgerPurchaseList?.get(it1)?.ledger_id }.toString()

            selectedItemLedgerName =
                pos?.let { it1 -> ledgerPurchaseList?.get(it1)?.name }.toString()
            true
        })

        popupMenu.show()
    }

    private fun clearFocus() {
        binding.txtUnitAddItem.clearFocus()
        binding.txtGrossWtAddItem.clearFocus()
        binding.txtNetWtAddItem.clearFocus()
        binding.txtLessWtAddItem.clearFocus()
        binding.txtTouchAddItem.clearFocus()
        binding.txtWastageAddItem.clearFocus()
        binding.txtGoldRateAddItem.clearFocus()
    }

    fun performValidation(): Boolean {
        if (binding.txtItemNameAddItem.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.select_item_name_msg))
            binding.txtItemNameAddItem.requestFocus()
            return false
        } else if (selectedItemName.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.select_item_name_msg))
            binding.txtItemNameAddItem.requestFocus()
            return false
        } else if (!selectedItemName!!.length.equals(binding.txtItemNameAddItem.text.toString().length)) {
            CommonUtils.showDialog(this, getString(R.string.select_item_name_msg))
            binding.txtItemNameAddItem.requestFocus()
            return false
        } else if (binding.txtUnitAddItem.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.enter_units_msg))
            binding.txtUnitAddItem.requestFocus()
            return false
        } else if (selectedItemType == "Goods" && binding.txtGrossWtAddItem.text.toString()
                .isBlank()
        ) {
            CommonUtils.showDialog(this, getString(R.string.enter_grosswt_msg))
            binding.txtGrossWtAddItem.requestFocus()
            return false
        } else if (selectedItemType == "Goods" && binding.txtLessWtAddItem.text.toString()
                .isBlank()
        ) {
            CommonUtils.showDialog(this, getString(R.string.enter_lesswt_msg))
            binding.txtLessWtAddItem.requestFocus()
            return false
        } else if (selectedItemType == "Goods" && binding.txtNetWtAddItem.text.toString()
                .isBlank()
        ) {
            CommonUtils.showDialog(this, getString(R.string.enter_netwt_msg))
            binding.txtNetWtAddItem.requestFocus()
            return false
        } else if (selectedItemType == "Goods" && binding.txtNetWtAddItem.text.toString()
                .toDouble() <= 0.00
        ) {
            CommonUtils.showDialog(this, getString(R.string.error_netwt_msg))
            binding.txtGrossWtAddItem.requestFocus()
            return false
        } else if (selectedItemType == "Goods" && binding.txtTouchAddItem.text.toString()
                .isBlank()
        ) {
            CommonUtils.showDialog(this, getString(R.string.enter_touch_msg))
            binding.txtTouchAddItem.requestFocus()
            return false
        } else if (selectedItemMetalType != "Other" && selectedItemType == "Goods" && binding.txtTouchAddItem.text.toString()
                .toDouble() <= 0.00
        ) {
            CommonUtils.showDialog(this, getString(R.string.enter_valid_touch_msg))
            binding.txtTouchAddItem.requestFocus()
            return false
        } else if (selectedItemMetalType != "Other" && selectedItemType == "Goods" && binding.txtWastageAddItem.text.toString()
                .isBlank()
        ) {
            CommonUtils.showDialog(this, getString(R.string.enter_wastage_msg))
            binding.txtWastageAddItem.requestFocus()
            return false
        } else if (selectedItemType == "Goods" && binding.txtFineWtAddItem.text.toString()
                .isBlank()
        ) {
            CommonUtils.showDialog(this, getString(R.string.enter_finewt))
            binding.txtFineWtAddItem.requestFocus()
            return false
        } else if (is_gst_applicable == "1" && binding.txtGoldRateAddItem.text.toString()
                .toDouble() <= 0.00
        ) {
            CommonUtils.showDialog(this, getString(R.string.enter_valid_goldrate_msg))
            binding.txtGoldRateAddItem.requestFocus()
            return false
        } else if (transaction_type != "opening_stock" && is_gst_applicable == "1" && is_tax_preferrence == "1" && selectedItemType == "Goods" && selectedItemLedgerId.equals(
                ""
            )
        ) {
            CommonUtils.showDialog(this, "Please enter Ledger")
            binding.txtLdgerAddItem.requestFocus()
            return false
        }
        when (checkForValidPositiveNegativeDecimal()) {
            true -> return true
            false -> return false
        }
        return true
    }

    private fun checkForValidPositiveNegativeDecimal(): Boolean {
        if (!binding.txtWastageAddItem.text.toString()
                .isBlank() && binding.txtWastageAddItem.text.toString().length > 1
        ) {
            try {
                val a: Double = binding.txtWastageAddItem.text.toString().toDouble()
                if (binding.txtWastageAddItem.text.toString().toDouble() >= 0.00) {
                    wastage = binding.txtWastageAddItem.text.toString().toBigDecimal()
                    // System.out.println(txtWastageAddItem.text.toString() + " is positive number");
                } else if (binding.txtWastageAddItem.text.toString().toDouble() < 0.00) {
                    wastage = binding.txtWastageAddItem.text.toString().toBigDecimal().negate()
                    //  System.out.println(txtWastageAddItem.text.toString() + " is negative number");
                } else {

                    // System.out.println(txtWastageAddItem.text.toString() + " is neither positive nor negative");
                }
                if (!binding.txtNetWtAddItem.text.toString()
                        .isBlank() && !binding.txtTouchAddItem.text.toString()
                        .isBlank()
                ) {
                    val net: Float = binding.txtNetWtAddItem.text.toString().toFloat()
                    val touch: Float = txtTouchAddItem.text.toString().toFloat()

                    val result: Float = (net * (touch.plus(wastage.toFloat())).div(100F))
                    /*val result: String =
                        ((net.setScale(3) * (touch.setScale(3) + wastage.setScale(3)) / BigDecimal(
                            100
                        ))).setScale(3, RoundingMode.CEILING).toString()*/
                    val result1: BigDecimal =
                        result.toString().toBigDecimal().setScale(3, RoundingMode.HALF_UP)

                    finewtUpdatedValue = result1.toString()
                    binding.txtFineWtAddItem.setText(finewtUpdatedValue)
                }
            } catch (ex: NumberFormatException) {
                Toast.makeText(this, getString(R.string.valid_wastage), Toast.LENGTH_SHORT).show()
                // System.err.println("Please Enter Valid Wastage")
                return false
                //request for well-formatted string
            }


        }
        return true
    }


    private fun getDataFromIntent() {
        if (intent.extras != null && intent.extras!!.containsKey(Constants.QR_DETAILS)) {
            qr_code = intent.getStringExtra(Constants.QR_DETAILS)
            transaction_type = intent.getStringExtra(Constants.TRANSACTION_TYPE)
            selectedCustStateId = intent.getStringExtra(Constants.CUST_STATE_ID)
            Log.v("qrcode", "" + qr_code)
            disableFieldForQRCode()
            getTagDetailsApi(loginModel.data?.bearer_access_token, qr_code)
            binding.tvTagNo.visibility = View.VISIBLE
        } else {
            binding.tvTagNo.visibility = View.GONE
            selectedTagRandomId = "Notag"
            selectedTagNo = ""
        }

        if (intent.extras != null && intent.extras!!.containsKey(Constants.TRANSACTION_TYPE)) {
            transaction_type = intent.getStringExtra(Constants.TRANSACTION_TYPE)
            selectedCustStateId = intent.getStringExtra(Constants.CUST_STATE_ID)
            Log.v("cutsid", "" + selectedCustStateId)
        }
        if (intent.extras != null && intent.extras!!.containsKey(Constants.OPENING_STOCK_POSITION_KEY)) {

            is_From_New_Opening = intent.getBooleanExtra(Constants.IS_FROM_NEW_OPENING_STOCK, false)
            tvTitle.setText(intent.getStringExtra(Constants.EDIT_ITEM))
            val position: Int = intent.getIntExtra(Constants.OPENING_STOCK_POSITION_KEY, -1)
            val group_str: String? = intent.getStringExtra(Constants.OPENING_STOCK_DETAIL_KEY)

            //set data
            // if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {

            is_Edit_OpeningStock_Item = true
            opneningStock_Item_Position = position
            Log.d("New_Check","New Opening :-$is_From_New_Opening \n Edit Item :- $is_Edit_OpeningStock_Item")

            addOpeningStockItemModel = Gson().fromJson(
                group_str,
                OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem::class.java

            )

            /*lessWeightList!!.addAll(itemList.get(position).less_wt!!)
            lessWeightListForCancelbtn!!.addAll(itemList.get(position).less_wt!!)*/
            fill_itemfields(addOpeningStockItemModel)

            //}
        }
    }

    fun getTagDetailsApi(
        token: String?,
        tag_number: String?
    ) {

        if (NetworkUtils.isConnected()) {

            viewModel.getTagDetails(token, tag_number).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {

                        Status.SUCCESS -> {

                            if (it.data?.status == true) {
                                Log.v("sucessdetail1", "")

                                //  tagDetailsModelItem = !!

                                fill_itemfileds_tag(it.data)


                            } else {
                                Log.v("sucessfail", "")
                                when (it.data!!.code == Constants.ErrorCode) {
                                    true -> {

                                        val builder = AlertDialog.Builder(this)

                                        val dialogdismiss = { dialog: DialogInterface, which: Int ->
                                            dialog.dismiss()
                                        }
                                        with(builder)
                                        {
                                            setTitle("Attention!")
                                            setMessage(it.data.errormessage?.message)
                                            setPositiveButton(
                                                context.getString(R.string.ok),
                                                dialogdismiss
                                            )
                                            //   setNeutralButton(context.getString(R.string.Delete), DialogInterface.OnClickListener(function = DeleteClick))
                                            show()
                                        }

                                        /*Toast.makeText(
                                            this,
                                            it.data.errormessage?.message,
                                            Toast.LENGTH_LONG
                                        )
                                            .show()*/
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

    private fun fill_itemfileds_tag(tagDetailItemModel: TagDetailItemModel) {

        selectedItemID = tagDetailItemModel.data!!.item_id
        selectedItemName = tagDetailItemModel.data!!.item_name
        selectedItemType = tagDetailItemModel.data.item_type
        when (selectedItemType.equals("Goods")) {
            true -> {
                binding.tvUnitAddItem.hint =
                    "Units" + " (" + tagDetailItemModel.data.item_unit_name + ")"

                binding.tvGrossWtAddItem.hint =
                    "Gross Weight" + " (" + tagDetailItemModel.data.item_maintain_stock_in_name + ")"
            }
            else -> {

            }
        }
        selectedItemMetalType = tagDetailItemModel.data.item_metal_type_name
        binding.tvTagNo.setText("Tag# " + tagDetailItemModel.data!!.tag_no)
        binding.txtItemNameAddItem.setText(selectedItemName)
        is_studed = tagDetailItemModel.data.item_is_studded
        item_unit_id = tagDetailItemModel.data.item_unit_id
        item_unit_name = tagDetailItemModel.data.item_unit_name
        binding.txtStampAddItem.setText(tagDetailItemModel.data.item_stamp_name)
        binding.txtSizeAddItem.setText(tagDetailItemModel.data.item_size)
        selectedStampId = tagDetailItemModel.data.item_stamp_id
        selectedStampName = tagDetailItemModel.data.item_stamp_name
        useColor = tagDetailItemModel.data.item_use_gold_color
        binding.txtColourAddItem.setText(tagDetailItemModel.data.item_gold_color_name)
        selectedColorId = tagDetailItemModel.data.item_gold_color_id
        selectedColorName = tagDetailItemModel.data.item_gold_color_name
        item_metal_type_id = tagDetailItemModel.data.item_metal_type_id
        item_metal_type_name = tagDetailItemModel.data.item_metal_type_name
        item_maintain_stock_in_id = tagDetailItemModel.data.item_maintain_stock_in_id
        item_maintain_stock_in_name = tagDetailItemModel.data.item_maintain_stock_in_name
        binding.txtUnitAddItem.setText(tagDetailItemModel.data.item_quantity)
        piecesUpdatedValue = tagDetailItemModel.data.item_quantity
        binding.txtGrossWtAddItem.setText(tagDetailItemModel.data.item_gross_wt)
        grossUpdatedValue = tagDetailItemModel.data.item_gross_wt
        binding.txtLessWtAddItem.setText(tagDetailItemModel.data.item_less_wt)
        lesswtUpdatedValue = tagDetailItemModel.data.item_less_wt
        binding.txtNetWtAddItem.setText(tagDetailItemModel.data.item_net_wt)
        netweightUpdatedValue = tagDetailItemModel.data.item_net_wt
        binding.txtTouchAddItem.setText(tagDetailItemModel.data.item_touch)
        touchUpdatedValue = tagDetailItemModel.data.item_touch
        binding.txtWastageAddItem.setText(tagDetailItemModel.data.item_wastage)
        wastageUpdatedValue = tagDetailItemModel.data.item_wastage
        binding.txtFineWtAddItem.setText(tagDetailItemModel.data.item_fine_wt)
        finewtUpdatedValue = tagDetailItemModel.data.item_fine_wt
        binding.txtGoldRateAddItem.setText(tagDetailItemModel.data.item_rate)
        goldRateUpdatedValue = tagDetailItemModel.data.item_rate
        binding.txtChargesAddItem.setText(tagDetailItemModel.data.item_charges)
        chargeUpdatedValue = tagDetailItemModel.data.item_charges
        binding.txtTotalAmountAddItem.setText(tagDetailItemModel.data.item_total)
        toatlAmtUpdatedValue = tagDetailItemModel.data.item_total
        binding.txtDiscountAddItem.setText(tagDetailItemModel.data.item_discount)
        discountUpdatedValue = tagDetailItemModel.data.item_discount
        // toatlAmtChargeUpdatedValue = tagDetailItemModel.data.item_total
        binding.txtRemarkAddItem.setText(tagDetailItemModel.data.item_remarks)
        selectedRateon = tagDetailItemModel.data.item_rate_on

        when(selectedRateon){
            "fine"->{
                binding.txtRateonAddItem.setText("Fine Weight")
            }
            "net"->{
                binding.txtRateonAddItem.setText("Net Weight")
            }
            "gross"->{
                binding.txtRateonAddItem.setText("Groos Weight")
            }
            "fix"->{
                binding.txtRateonAddItem.setText("Fix")
            }
        }
        selectedTagNo = tagDetailItemModel.data.tag_no
        selectedTagRandomId = tagDetailItemModel.data.random_tag_id
        unitArrayList = tagDetailItemModel.data.item_unit_array
        if (is_gst_applicable.equals("1")) {

            if (!transaction_type.equals("opening_stock")) {
                if (tagDetailItemModel.data!!.item_tax_preference.equals("")) {
                    is_tax_preferrence = "0"
                } else {
                    is_tax_preferrence = "1"
                }

                selectedItemLedgerId = tagDetailItemModel.data!!.item_sales_ledger_id
                selectedItemLedgerName = tagDetailItemModel.data!!.item_sales_ledger_name
                selectedItemHsn = tagDetailItemModel.data!!.item_sales_purchase_hsn
                selectedItemGstId = tagDetailItemModel.data!!.item_sales_purchase_gst_rate
                // selectedItemGst = tagDetailItemModel.data!!.item_gst_rate_percentage
                if (tagDetailItemModel.data!!.item_gst_rate_percentage.equals("")) {
                    selectedItemGst = "0.00"
                } else {
                    selectedItemGst = tagDetailItemModel.data!!.item_gst_rate_percentage
                    Log.v("itemGst", "" + selectedItemGst)
                }

                if (is_tax_preferrence.equals("1")) {
                    if (selectedItemLedgerId.equals("")) {
                        binding.tvLedgerAddItem.visibility =
                            View.VISIBLE
                        when (transaction_type) {
                            "sales" -> {
                                getLedgerdd("sales")
                            }
                        }

                    } else {
                        binding.tvLedgerAddItem.visibility = View.GONE
                    }
                }


            }
        }
        if (selectedCustStateId.equals(loginModel.data!!.branch_info!!.state_id.toString())) {
            Log.v("testiuoi", "")
            is_Igst_enable = false
            updateTaxAmout(is_Igst_enable)
        } else {
            is_Igst_enable = true
            updateTaxAmout(is_Igst_enable)
        }


        val lessWeightList: List<AddLessWeightModel.AddLessWeightModelItem>
        lessWeightList = tagDetailItemModel.data!!.item_wt_breakup.less_wt_array

        if (lessWeightList.size > 0) {
            // if (tagDetailItemModel.data!!.item_wt_breakup.total_less_wt.toBigDecimal() != BigDecimal.ZERO) {
            prefs[Constants.PREF_LESS_WEIGHT_INFO_KEY] = Gson().toJson(lessWeightList)
            val lessWeightBreakupModel: OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup
            lessWeightBreakupModel = tagDetailItemModel.data!!.item_wt_breakup
            prefs[Constants.PREF_LESS_WEIGHT_BREAKUP_INFO_KEY] =
                Gson().toJson(lessWeightBreakupModel)
            //}
        }

        prefs[Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY] =
            Gson().toJson(tagDetailItemModel.data!!.item_charges_breakup)

        prefs[Constants.PREF_MAKING_CHARGES_KEY] =
            Gson().toJson(tagDetailItemModel.data!!.item_charges_breakup.making_charge_array)

        val otherChargesList: List<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray>
        otherChargesList = tagDetailItemModel.data!!.item_charges_breakup.charges_array
        prefs[Constants.PREF_OTHER_CHARGES_KEY] =
            Gson().toJson(otherChargesList)

    }


    private fun disableFieldForQRCode() {
        binding.txtItemNameAddItem.isEnabled = false
        binding.txtStampAddItem.isEnabled = false
        binding.txtColourAddItem.isEnabled = false
        binding.txtUnitAddItem.isEnabled = false
        binding.txtGrossWtAddItem.isEnabled = false
        binding.txtLessWtAddItem.isEnabled = false
        binding.txtNetWtAddItem.isEnabled = false
        binding.txtTouchAddItem.isEnabled = false
        //enable wastage edit
        binding.txtWastageAddItem.isEnabled = true
        binding.txtFineWtAddItem.isEnabled = false
    }


    private fun fill_itemfields(
        openingStockModel: OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem
    ) {

        selectedItemID = openingStockModel.item_id
        selectedItemName = openingStockModel.item_name
        binding.txtItemNameAddItem.setText(openingStockModel.item_name)

        getItemStamp(selectedItemID.trim())

        selectedTagRandomId = openingStockModel.random_tag_id
        selectedTagNo = openingStockModel.tag_no

        when (transaction_type) {
            "opening_stock" -> {
                getItemSearch("opening_stock", selectedItemName, true)
            }
        }

        if (!transaction_type.equals("opening_stock")) {
            //"No Tag" means item is not Tag Item
            if (!selectedTagRandomId.equals("Notag")) {
                disableFieldForQRCode()
            } else {
                when (transaction_type) {
                    "sales" -> {
                        getItemSearch("sales", selectedItemName, true)
                        getLedgerdd("sales")
                    }
                    "purchase" -> {
                        getItemSearch("purchase", selectedItemName, true)
                        getLedgerdd("purchase")
                    }
                    "receipt" -> {
                        getItemSearch("receipt", selectedItemName, true)
                    }
                    "payment" -> {
                        getItemSearch("payment", selectedItemName, true)
                    }
                }
            }
        }


        selectedStampId = openingStockModel.item_stamp_id
        selectedStampName = openingStockModel.item_stamp_name
        binding.txtStampAddItem.setText(openingStockModel.item_stamp_name)
        item_use_stamp = openingStockModel.item_use_stamp
        useColor = openingStockModel.item_use_gold_color
        selectedColorName = openingStockModel.item_gold_color_name
        selectedColorId = openingStockModel.item_gold_color_id
        is_studed = openingStockModel.item_is_studded
        selectedItemType = openingStockModel.item_type


        if (selectedTagRandomId.equals("Notag")) {
            when (selectedItemType.equals("Goods")) {
                true -> {
                    when (item_use_stamp.equals("1", true)) {
                        true -> {
                            getItemStamp(selectedItemID.trim())
                            binding.tvStampAddItem.visibility = View.VISIBLE
                            if (useColor.equals("0")) {
                                val params = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                binding.tvStampAddItem.setLayoutParams(params)
                            }
                            /*binding.txtStampAddItem.isEnabled = true*/
                            setDrawableTint(
                                binding.tvStampAddItem,
                                resources.getColor(R.color.drop_down_arrow_color)
                            )
                        }
                        false -> {
                            // binding.txtStampAddItem.isEnabled = false
                            binding.txtStampAddItem.setText("")
                            binding.tvStampAddItem.visibility = View.GONE
                            if (useColor.equals("1")) {
                                val params = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                binding.tvColourAddItem.setLayoutParams(params)
                            }


                        }
                    }
                    if (useColor.equals("0")) {
                        binding.tvColourAddItem.visibility = View.GONE
                    } else {
                        binding.tvColourAddItem.visibility = View.VISIBLE
                        setDrawableTint(
                            binding.tvColourAddItem,
                            resources.getColor(R.color.drop_down_arrow_color)
                        )
                    }
                }
                false -> {
                    binding.tvColourAddItem.visibility = View.VISIBLE
                    binding.tvStampAddItem.visibility = View.VISIBLE
                    setDrawableTint(
                        binding.tvColourAddItem,
                        resources.getColor(R.color.end_icon_gray)
                    )
                    setDrawableTint(
                        binding.tvStampAddItem,
                        resources.getColor(R.color.end_icon_gray)
                    )

                }
            }
        }



        if (openingStockModel.tag_no.equals("")) {
            binding.tvTagNo.visibility = View.GONE
        } else {
            binding.tvTagNo.visibility = View.VISIBLE
            binding.tvTagNo.text = "Tag# " + openingStockModel.tag_no
        }


        if (!transaction_type.equals("opening_stock")) {
            if (selectedTagRandomId.equals("Notag")) {
                when (selectedItemType.equals("Goods")) {
                    true -> {
                        binding.txtStampAddItem.isEnabled = true
                        binding.txtColourAddItem.isEnabled = true
                        binding.txtGrossWtAddItem.isEnabled = true
                        binding.txtLessWtAddItem.isEnabled = true
                        binding.txtNetWtAddItem.isEnabled = true
                        binding.txtTouchAddItem.isEnabled = true
                        binding.txtWastageAddItem.isEnabled = true
                        binding.txtFineWtAddItem.isEnabled = true
                        binding.txtRateonAddItem.isEnabled = true
                    }
                    false -> {
                        binding.txtStampAddItem.isEnabled = false
                        binding.txtColourAddItem.isEnabled = false
                        binding.txtGrossWtAddItem.isEnabled = false
                        binding.txtLessWtAddItem.isEnabled = false
                        binding.txtNetWtAddItem.isEnabled = false
                        binding.txtTouchAddItem.isEnabled = false
                        binding.txtWastageAddItem.isEnabled = false
                        binding.txtFineWtAddItem.isEnabled = false
                        binding.txtRateonAddItem.isEnabled = false
                    }
                }
            }
        }


        binding.txtColourAddItem.setText(openingStockModel.item_gold_color_name)
        binding.txtUnitAddItem.setText(openingStockModel.item_quantity)
        binding.txtSizeAddItem.setText(openingStockModel.item_size)
        grossUpdatedValue = openingStockModel.item_gross_wt
        binding.txtGrossWtAddItem.setText(openingStockModel.item_gross_wt)
        //binding.txtLessWtAddItem.setText(openingStockModel.item_less_wt)
        binding.txtNetWtAddItem.setText(openingStockModel.item_net_wt)
        netweightUpdatedValue = openingStockModel.item_net_wt
        touchUpdatedValue = openingStockModel.item_touch
        binding.txtTouchAddItem.setText(openingStockModel.item_touch)
        wastageUpdatedValue = openingStockModel.item_wastage
        binding.txtWastageAddItem.setText(openingStockModel.item_wastage)
        binding.txtDiscountAddItem.setText(openingStockModel.item_discount)
        discountUpdatedValue = openingStockModel.item_discount

        if(!transaction_type.equals("opening_stock")){
            selectedRateon = openingStockModel.item_rate_on
            when(selectedRateon){
                "fine"->{
                    binding.txtRateonAddItem.setText("Fine Weight")
                }
                "net"->{
                    binding.txtRateonAddItem.setText("Net Weight")
                }
                "gross"->{
                    binding.txtRateonAddItem.setText("Gross Weight")
                }
                "fix"->{
                    binding.txtRateonAddItem.setText("Fix")
                }
            }
        }else{
            selectedRateon = "fine"
        }

//        finewtUpdatedValue = openingStockModel.item_fine_wt
//        binding.txtFineWtAddItem.setText(openingStockModel.item_fine_wt)

        when (openingStockModel.item_rate.toBigDecimal().compareTo(BigDecimal.ZERO) > 0) {
            true -> {
                Log.v("ratefinewt", "true")
                finewtUpdatedValue = openingStockModel.item_fine_wt
                binding.txtFineWtAddItem.setText("0.000")
            }
            false -> {
                finewtUpdatedValue = openingStockModel.item_fine_wt
                binding.txtFineWtAddItem.setText(openingStockModel.item_fine_wt)
            }
        }

        goldRateUpdatedValue = openingStockModel.item_rate
        binding.txtGoldRateAddItem.setText(openingStockModel.item_rate)
        chargeUpdatedValue = openingStockModel.item_charges
        binding.txtChargesAddItem.setText(openingStockModel.item_charges)
        toatlAmtUpdatedValue = openingStockModel.item_total
        binding.txtTotalAmountAddItem.setText(openingStockModel.item_total)

        binding.txtRemarkAddItem.setText(openingStockModel.item_remarks)

        net_wt = openingStockModel.item_net_wt
        item_maintain_stock_in_id = openingStockModel.item_maintain_stock_in_id
        item_maintain_stock_in_name = openingStockModel.item_maintain_stock_in_name
        item_metal_type_id = openingStockModel.item_metal_type_id
        item_metal_type_name = openingStockModel.item_metal_type_name
        item_unit_id = openingStockModel.item_unit_id
        item_unit_name = openingStockModel.item_unit_name

        unitArrayList = openingStockModel.item_unit_array
        item_maintain_stock_in_name = openingStockModel.item_maintain_stock_in_name

        if (is_gst_applicable.equals("1")) {

            if (!transaction_type.equals("opening_stock")) {
                if (openingStockModel.tax_analysis_array!!.ledger_name.isBlank()) {
                    is_tax_preferrence = "0"
                } else {
                    is_tax_preferrence = "1"
                }

                selectedItemLedgerId = openingStockModel.tax_analysis_array!!.ledger_id
                selectedItemLedgerName =
                    openingStockModel.tax_analysis_array!!.ledger_name
                selectedItemHsn = openingStockModel.tax_analysis_array!!.hsn
                selectedItemGstId = openingStockModel.tax_analysis_array!!.gst_rate
                if (openingStockModel.tax_analysis_array!!.gst_rate_percentage.equals("")) {
                    selectedItemGst = "0.00"
                } else
                    selectedItemGst =
                        openingStockModel.tax_analysis_array!!.gst_rate_percentage

                igstUpdatedValue = openingStockModel.tax_analysis_array!!.igst_amount
                sgstUpdatedValue = openingStockModel.tax_analysis_array!!.sgst_amount
                cgstUpdatedValue = openingStockModel.tax_analysis_array!!.cgst_amount


                if (is_tax_preferrence.equals("1")) {
                    binding.tvLedgerAddItem.visibility =
                        View.VISIBLE
                    binding.txtLdgerAddItem.setText(selectedItemLedgerName)
                }

            }
        }

        when (openingStockModel.item_less_wt.toBigDecimal()
            .compareTo(BigDecimal.ZERO) > 0) {
            true -> {
                when (item_maintain_stock_in_name) {
                    "Kilograms" -> {
                        // txtLessWtAddItem.setText(CommonUtils.gmsTokg(openingStockModel.item_less_wt))
                        txtLessWtAddItem.setText(openingStockModel.item_less_wt)
                    }
                    "Carat" -> {
                        //txtLessWtAddItem.setText(CommonUtils.gmsTocarrot(openingStockModel.item_less_wt))
                        txtLessWtAddItem.setText(openingStockModel.item_less_wt)
                    }
                    "Grams" -> {
                        txtLessWtAddItem.setText(openingStockModel.item_less_wt)

                    }
                    "" -> {
                        txtLessWtAddItem.setText(openingStockModel.item_less_wt)
                    }

                }
            }
            false -> {
                txtLessWtAddItem.setText(openingStockModel.item_less_wt)
            }
        }


        val lessWeightList
                : List<AddLessWeightModel.AddLessWeightModelItem>
        lessWeightList = openingStockModel.item_wt_breakup.less_wt_array

        if (lessWeightList.size > 0) {
            if (openingStockModel.item_wt_breakup.total_less_wt.toBigDecimal() != BigDecimal.ZERO) {
                prefs[Constants.PREF_LESS_WEIGHT_INFO_KEY] = Gson().toJson(lessWeightList)
                val lessWeightBreakupModel: OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup
                lessWeightBreakupModel = openingStockModel.item_wt_breakup
                prefs[Constants.PREF_LESS_WEIGHT_BREAKUP_INFO_KEY] =
                    Gson().toJson(lessWeightBreakupModel)
            }
        }

        prefs[Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY] =
            Gson().toJson(openingStockModel.item_charges_breakup)

        prefs[Constants.PREF_MAKING_CHARGES_KEY] =
            Gson().toJson(openingStockModel.item_charges_breakup.making_charge_array)

        val otherChargesList
                : List<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray>
        otherChargesList = openingStockModel.item_charges_breakup.charges_array
        prefs[Constants.PREF_OTHER_CHARGES_KEY] =
            Gson().toJson(otherChargesList)


    }


    //Opening Stock Item Calculation
    private fun saveOpeningStockCalcItemModel() {

        if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {
            val collectionType = object :
                TypeToken<java.util.ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>>() {}.type
            var openingStockItemCalcList: ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem> =
                Gson().fromJson(
                    prefs[Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY, ""],
                    collectionType
                )
            addopeningStockCalcItemList.addAll(openingStockItemCalcList)
        } else {
            addopeningStockCalcItemList = ArrayList()
        }


        if (prefs.contains(Constants.PREF_LESS_WEIGHT_BREAKUP_INFO_KEY)) {

            val lessWeightBreakup =
                object :
                    TypeToken<OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup>() {}.type
            lessweightCalcBreakupList = Gson().fromJson(
                prefs[Constants.PREF_LESS_WEIGHT_BREAKUP_INFO_KEY, ""],
                lessWeightBreakup
            )

        } else {
            lessweightCalcBreakupList =
                OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup(
                    "", arrayListOf(), "", ""
                )
        }

        if (prefs.contains(Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY)) {
            val makingChargeBreakup =
                object :
                    TypeToken<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup>() {}.type
            makingChargeCalcBreakupList = Gson().fromJson(
                prefs[Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY, ""],
                makingChargeBreakup
            )
        } else {

            when (chargeUpdatedValue.toBigDecimal().compareTo(BigDecimal.ZERO) > 0) {
                true -> {
                    var making_charge_array =
                        OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.MakingChargeArray(
                            makingChrgsUpdatedValue,
                            unitArrayList.get(1).id,
                            unitArrayList.get(1).name
                        )
                    makingChargeCalcBreakupList =
                        OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup(
                            arrayListOf(),
                            making_charge_array,
                            binding.txtChargesAddItem.text.toString()
                        )

                }
                false -> {
                    var making_charge_array =
                        OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.MakingChargeArray(
                            "", "", ""
                        )
                    makingChargeCalcBreakupList =
                        OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup(
                            arrayListOf(),
                            making_charge_array,
                            binding.txtChargesAddItem.text.toString()
                        )
                }
            }
        }

        when (transaction_type) {
            "opening_stock" -> {
                item_type = "opening_stock_item"
                taxAnalysisModel =
                    OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem.Tax_analysis_array(
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "0.00",
                        "",
                        "",
                        ""
                    )
            }
            "sales" -> {
                item_type = "sales_item"
                //gst enabled
                if (is_gst_applicable.equals("1")) {
                    //is_tax_preferrence =  1 -> gst tax percentage is their
                    if (is_tax_preferrence.equals("1")) {
                        taxAnalysisModel =
                            OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem.Tax_analysis_array(
                                selectedItemID,
                                selectedItemName,
                                selectedItemLedgerId,
                                selectedItemLedgerName,
                                binding.txtTotalAmountAddItem.text.toString(),
                                selectedItemHsn,
                                selectedItemGstId,
                                selectedItemGst,
                                igstUpdatedValue,
                                cgstUpdatedValue,
                                sgstUpdatedValue
                            )
                    } else {
                        //gst tax percentage = 0 -> if is_tax_preferrence = 0
                        taxAnalysisModel =
                            OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem.Tax_analysis_array(
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "0.00",
                                "",
                                "",
                                ""
                            )
                    }
                }
                //Tax is not calculated for Non GST
                else {
                    taxAnalysisModel =
                        OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem.Tax_analysis_array(
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "0.00",
                            "",
                            "",
                            ""
                        )

                }

            }
            "purchase" -> {
                item_type = "purchase_item"
                if (is_gst_applicable.equals("1")) {
                    if (is_tax_preferrence.equals("1")) {
                        taxAnalysisModel =
                            OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem.Tax_analysis_array(
                                selectedItemID,
                                selectedItemName,
                                selectedItemLedgerId,
                                selectedItemLedgerName,
                                binding.txtTotalAmountAddItem.text.toString(),
                                selectedItemHsn,
                                selectedItemGstId,
                                selectedItemGst,
                                igstUpdatedValue,
                                cgstUpdatedValue,
                                sgstUpdatedValue
                            )
                    } else {
                        taxAnalysisModel =
                            OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem.Tax_analysis_array(
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "0.00",
                                "",
                                "",
                                ""
                            )
                    }
                } else {
                    taxAnalysisModel =
                        OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem.Tax_analysis_array(
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "0.00",
                            "",
                            "",
                            ""
                        )

                }

            }
            "receipt" -> {
                item_type = "receipt_item"
                taxAnalysisModel =
                    OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem.Tax_analysis_array(
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "0.00",
                        "",
                        "",
                        ""
                    )
            }
            "payment" -> {
                item_type = "payment_item"
                taxAnalysisModel =
                    OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem.Tax_analysis_array(
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "0.00",
                        "",
                        "",
                        ""
                    )
            }
        }


        val childModel = OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem(
            selectedItemID,
            selectedItemName,
            binding.txtUnitAddItem.text.toString(),
            binding.txtSizeAddItem.text.toString(),
            binding.txtGrossWtAddItem.text.toString(),
            binding.txtLessWtAddItem.text.toString(),
            binding.txtNetWtAddItem.text.toString(),
            binding.txtTouchAddItem.text.toString(),
            binding.txtWastageAddItem.text.toString(),
            finewtUpdatedValue,
            binding.txtTotalAmountAddItem.text.toString(),
            binding.txtRemarkAddItem.text.toString(),
            item_unit_id,
            item_unit_name,
            item_use_stamp,
            selectedStampId,
            selectedStampName,
            useColor,
            selectedColorId,
            selectedColorName,
            item_metal_type_id,
            item_metal_type_name,
            item_maintain_stock_in_id,
            item_maintain_stock_in_name,
            binding.txtGoldRateAddItem.text.toString(),
            selectedRateon,
            binding.txtTotalAmountAddItem.text.toString(),
            binding.txtChargesAddItem.text.toString(),
            binding.txtDiscountAddItem.text.toString(),
            selectedItemType,
            selectedTagNo,
            selectedTagRandomId,
            is_studed,
            lessweightCalcBreakupList,
            makingChargeCalcBreakupList,
            unitArrayList,
            taxAnalysisModel

        )

        if (opneningStock_Item_Position >= 0 && opneningStock_Item_Position != -1) {
            // Update selected item
            addopeningStockCalcItemList.set(opneningStock_Item_Position, childModel)
        } else {
            // Add new item
            addopeningStockCalcItemList.add(childModel)
        }

        prefs[Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY] =
            Gson().toJson(addopeningStockCalcItemList)
    }


    private fun calculationTotalOfOtherCharges() {
        val othercharge1: BigDecimal = otherChrgs1CalculatedUpdatedValue.toBigDecimal()
        val othercharge2: BigDecimal = otherChrgs2CalculatedUpdatedValue.toBigDecimal()
        val othercharge3: BigDecimal = otherChrgs3CalculatedUpdatedValue.toBigDecimal()
        val othercharge4: BigDecimal = otherChrgs4CalculatedUpdatedValue.toBigDecimal()
        val othercharge5: BigDecimal = otherChrgs5CalculatedUpdatedValue.toBigDecimal()
        Log.v("othercharge1", otherChrgs1CalculatedUpdatedValue)
        Log.v("othercharge2", otherChrgs2CalculatedUpdatedValue)
        Log.v("othercharge3", otherChrgs3CalculatedUpdatedValue)
        Log.v("othercharge4", otherChrgs4CalculatedUpdatedValue)
        Log.v("othercharge5", otherChrgs5CalculatedUpdatedValue)
        //val netwt: BigDecimal = netweightUpdatedValue.toBigDecimal()
        totalOtherChrgsUpdatedValue =
            (othercharge1.setScale(2))
                .plus(
                    (othercharge2.setScale(2))
                        .plus((othercharge3.setScale(2)))
                        .plus((othercharge4.setScale(2)))
                        .plus((othercharge5.setScale(2)))
                ).setScale(2, RoundingMode.HALF_UP).toString()
        Log.v("totalother", totalOtherChrgsUpdatedValue)
    }


    private fun onFocusChangeSetup() {
        binding.txtGrossWtAddItem.setOnFocusChangeListener { v, hasFocus -> //if(!hasFocus)
            if (!hasFocus) {
                when (binding.txtGrossWtAddItem.text.isNullOrBlank()) {
                    true -> {
                        grossUpdatedValue = "0.000"
                        binding.txtGrossWtAddItem.setText(grossUpdatedValue)
                        binding.txtGrossWtAddItem.setSelection(grossUpdatedValue.length)

                    }
                    else -> {
                        when (grossUpdatedValue.toBigDecimal()
                            .compareTo(BigDecimal.ZERO) == 0) {
                            true -> {
                                //  Toast.makeText(this, getString(R.string.lesswt_st_weight_error_msg), Toast.LENGTH_SHORT).show()
                            }
                            false -> {
                                binding.txtGrossWtAddItem.setText(grossUpdatedValue)
                                if (!binding.txtGrossWtAddItem.text.toString().isBlank() &&
                                    !binding.txtLessWtAddItem.text.toString().isBlank()
                                ) {
                                    val gross: BigDecimal =
                                        binding.txtGrossWtAddItem.text.toString().toBigDecimal()
                                    val less: BigDecimal =
                                        binding.txtLessWtAddItem.text.toString().toBigDecimal()
                                    val result: String = (gross - less).toString()

                                    binding.txtNetWtAddItem.setText(result)
                                    netweightUpdatedValue = result
                                    net_wt = netweightUpdatedValue
                                    when (selectedItemMetalType.equals("Other")) {
                                        true -> {
                                            finewtUpdatedValue = net_wt
                                            when (goldRateUpdatedValue.toBigDecimal()
                                                .compareTo(BigDecimal.ZERO) > 0) {
                                                true -> {
                                                    binding.txtFineWtAddItem.setText("0.000")
                                                    updateTotalAmount(true)
                                                }
                                                false -> {
                                                    binding.txtFineWtAddItem.setText(
                                                        finewtUpdatedValue
                                                    )
                                                }
                                            }

                                        }
                                        else -> {
                                            var wastage: BigDecimal = BigDecimal(0)
                                            if (!binding.txtWastageAddItem.text.toString()
                                                    .isBlank()
                                            ) {
                                                wastage =
                                                    binding.txtWastageAddItem.text.toString()
                                                        .toBigDecimal()
                                            }
                                            if (!binding.txtNetWtAddItem.text.toString()
                                                    .isBlank() && !binding.txtTouchAddItem.text.toString()
                                                    .isBlank()
                                            ) {
                                                val net: Float = binding.txtNetWtAddItem.text.toString().toFloat()
                                                val touch: Float = txtTouchAddItem.text.toString().toFloat()


                                                val result: Float = (net * (touch.plus(wastage.toFloat())).div(100F))
                                                /*val result: String =
                                                    ((net.setScale(3) * (touch.setScale(3) + wastage.setScale(3)) / BigDecimal(
                                                        100
                                                    ))).setScale(3, RoundingMode.CEILING).toString()*/
                                                val result1: BigDecimal =
                                                    result.toString().toBigDecimal().setScale(3, RoundingMode.HALF_UP)

                                                finewtUpdatedValue = result1.toString()
                                                Log.d("finrwtGrosswt",""+finewtUpdatedValue)
                                                when (goldRateUpdatedValue.toBigDecimal()
                                                    .compareTo(BigDecimal.ZERO) > 0) {
                                                    true -> {
                                                        binding.txtFineWtAddItem.setText("0.000")
                                                        updateTotalAmount(true)
                                                    }
                                                    false -> {
                                                        binding.txtFineWtAddItem.setText(
                                                            finewtUpdatedValue
                                                        )
                                                    }
                                                }

                                            }
                                        }
                                    }
                                    when (transaction_type) {
                                        //all transaction without opening
                                        "sales" -> {
                                            settingupchargesTotal()
                                            updateTotalAmount(true)
                                            if (is_Edit_OpeningStock_Item) {
                                                //making charge update on gross weight changed
                                                if (prefs.contains(Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY)) {
                                                    var making_charge_array =
                                                        OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.MakingChargeArray(
                                                            makingChrgsUpdatedValue,
                                                            unitArrayList.get(1).id,
                                                            unitArrayList.get(1).name
                                                        )
                                                    makingChargeCalcBreakupList =
                                                        OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup(
                                                            arrayListOf(),
                                                            making_charge_array,
                                                            binding.txtChargesAddItem.text.toString()
                                                        )
                                                    prefs[Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY] =
                                                        Gson().toJson(
                                                            makingChargeCalcBreakupList
                                                        )

                                                }
                                            }
                                        }
                                        "purchase" -> {
                                            settingupchargesTotal()
                                            updateTotalAmount(true)
                                            if (is_Edit_OpeningStock_Item) {
                                                //making charge update on gross weight changed
                                                if (prefs.contains(Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY)) {
                                                    var making_charge_array =
                                                        OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.MakingChargeArray(
                                                            makingChrgsUpdatedValue,
                                                            unitArrayList.get(1).id,
                                                            unitArrayList.get(1).name
                                                        )
                                                    makingChargeCalcBreakupList =
                                                        OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup(
                                                            arrayListOf(),
                                                            making_charge_array,
                                                            binding.txtChargesAddItem.text.toString()
                                                        )
                                                    prefs[Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY] =
                                                        Gson().toJson(
                                                            makingChargeCalcBreakupList
                                                        )

                                                }
                                            }
                                        }
                                    }

                                }
                            }

                        }
                    }

                }
            }
        }


        binding.txtTouchAddItem.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                when (txtTouchAddItem.text.isNullOrBlank()) {
                    true -> {
                        touchUpdatedValue = "0.000"
                        binding.txtTouchAddItem.setText(touchUpdatedValue)
                        binding.txtTouchAddItem.setSelection(touchUpdatedValue.length)

                    }
                    else -> {
                        when (touchUpdatedValue.toBigDecimal()
                            .compareTo(BigDecimal.ZERO) == 0) {
                            true -> {

                            }
                            else -> {
                                binding.txtTouchAddItem.setText(touchUpdatedValue)
                                var wastage: BigDecimal = BigDecimal(0)
                                if (!txtWastageAddItem.text.toString().isBlank()) {
                                    wastage = txtWastageAddItem.text.toString().toBigDecimal()
                                }
                                if (!binding.txtNetWtAddItem.text.toString()
                                        .isBlank() && !binding.txtTouchAddItem.text.toString()
                                        .isBlank()
                                ) {
                                    val net: Float = binding.txtNetWtAddItem.text.toString().toFloat()
                                    val touch: Float = txtTouchAddItem.text.toString().toFloat()


                                    val result: Float = (net * (touch.plus(wastage.toFloat())).div(100F))
                                    /*val result: String =
                                        ((net.setScale(3) * (touch.setScale(3) + wastage.setScale(3)) / BigDecimal(
                                            100
                                        ))).setScale(3, RoundingMode.CEILING).toString()*/
                                    val result1: BigDecimal =
                                        result.toString().toBigDecimal().setScale(3, RoundingMode.HALF_UP)

                                    finewtUpdatedValue = result1.toString()

                                    /*val net: BigDecimal =
                                        binding.txtNetWtAddItem.text.toString().toBigDecimal()
                                    val touch: BigDecimal =
                                        binding.txtTouchAddItem.text.toString().toBigDecimal()
                                    val result: String =
                                        ((net.setScale(3) * (touch.setScale(3) + wastage.setScale(
                                            3
                                        )) / BigDecimal(
                                            100
                                        ))).setScale(
                                            3,
                                            RoundingMode.HALF_EVEN
                                        ).toString()
                                    finewtUpdatedValue = result*/
                                    Log.d("finewttouch",""+finewtUpdatedValue)
                                    when (goldRateUpdatedValue.toBigDecimal()
                                        .compareTo(BigDecimal.ZERO) > 0) {
                                        true -> {
                                            binding.txtFineWtAddItem.setText("0.000")
                                            updateTotalAmount(true)
                                        }
                                        false -> {
                                            binding.txtFineWtAddItem.setText(finewtUpdatedValue)
                                        }
                                    }
                                    binding.txtFineWtAddItem.setText(result1.toString())
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.txtLessWtAddItem.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                when (txtLessWtAddItem.text.isNullOrBlank()) {
                    true -> {
                        lesswtUpdatedValue = "0.000"
                        binding.txtLessWtAddItem.setText(lesswtUpdatedValue)
                        binding.txtLessWtAddItem.setSelection(lesswtUpdatedValue.length)

                    }
                    else -> {
                        when (lesswtUpdatedValue.toBigDecimal()
                            .compareTo(BigDecimal.ZERO) == 0) {
                            true -> {

                            }
                            else -> {
                                if (!binding.txtGrossWtAddItem.text.toString()
                                        .isBlank() && !binding.txtLessWtAddItem.text.toString()
                                        .isBlank()
                                ) {
                                    val gross: BigDecimal =
                                        binding.txtGrossWtAddItem.text.toString().toBigDecimal()
                                    val less: BigDecimal =
                                        binding.txtLessWtAddItem.text.toString().toBigDecimal()
                                    val result: String = (gross - less).toString()
                                    binding.txtNetWtAddItem.setText(result)
                                    netweightUpdatedValue = result
                                    net_wt = netweightUpdatedValue
                                }
                                when (transaction_type) {
                                    //all transaction without opening
                                    "sales" -> {
                                        settingupchargesTotal()
                                        updateTotalAmount(true)
                                    }
                                    "purchase" -> {
                                        settingupchargesTotal()
                                        updateTotalAmount(true)
                                    }
                                }
                                var wastage: BigDecimal = BigDecimal(0)
                                if (!binding.txtWastageAddItem.text.toString().isBlank()) {
                                    wastage =
                                        binding.txtWastageAddItem.text.toString().toBigDecimal()
                                }
                                if (!binding.txtNetWtAddItem.text.toString()
                                        .isBlank() && !binding.txtTouchAddItem.text.toString()
                                        .isBlank()
                                ) {
                                    val net: Float = binding.txtNetWtAddItem.text.toString().toFloat()
                                    val touch: Float = txtTouchAddItem.text.toString().toFloat()


                                    val result: Float = (net * (touch.plus(wastage.toFloat())).div(100F))
                                    /*val result: String =
                                        ((net.setScale(3) * (touch.setScale(3) + wastage.setScale(3)) / BigDecimal(
                                            100
                                        ))).setScale(3, RoundingMode.CEILING).toString()*/
                                    val result1: BigDecimal =
                                        result.toString().toBigDecimal().setScale(3, RoundingMode.HALF_UP)

                                    finewtUpdatedValue = result1.toString()
                                    binding.txtFineWtAddItem.setText(result1.toString())
                                }

                            }
                        }

                    }
                }
            }
        }


        binding.txtWastageAddItem.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                when (txtWastageAddItem.text.isNullOrBlank()) {
                    true -> {
                        wastageUpdatedValue = "0.00"
                        binding.txtWastageAddItem.setText(wastageUpdatedValue)
                        binding.txtWastageAddItem.setSelection(wastageUpdatedValue.length)
                    }
                    else -> {
                        when (wastageUpdatedValue.toBigDecimal()
                            .compareTo(BigDecimal.ZERO) > 0) {
                            true -> {
                                binding.txtWastageAddItem.setText(wastageUpdatedValue)
                                binding.txtWastageAddItem.setSelection(wastageUpdatedValue.length)
                            }
                            else -> {

                            }
                        }
                    }
                }
            }
        }


        binding.txtGoldRateAddItem.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                when (txtGoldRateAddItem.text.isNullOrBlank()) {
                    true -> {
                        //goldRateUpdatedValue = "1.00"
                        goldRateUpdatedValue = "0.00"
                        binding.txtGoldRateAddItem.setText(goldRateUpdatedValue)
                        binding.txtFineWtAddItem.setText(finewtUpdatedValue)
                        //binding.txtGoldRateAddItem.setText(goldRateUpdatedValue)
                        // binding.txtFineWtAddItem.setSelection(finewtUpdatedValue.length)
                    }
                    else -> {
                        when (goldRateUpdatedValue.toBigDecimal()
                            .compareTo(BigDecimal.ZERO) == 0) {
                            true -> {

                                binding.txtGoldRateAddItem.setText(goldRateUpdatedValue)
                                binding.txtGoldRateAddItem.setSelection(goldRateUpdatedValue.length)
                            }
                            else -> {
                                binding.txtGoldRateAddItem.setText(goldRateUpdatedValue)
                                binding.txtGoldRateAddItem.setSelection(goldRateUpdatedValue.length)
                                binding.txtFineWtAddItem.setText("0.000")
                            }
                        }

                    }
                }
                // to update item amount labour amount and total amount when pieces changesd
                updateTotalAmount(true)
            }

        }

        binding.txtChargesAddItem.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                when (txtChargesAddItem.text.isNullOrBlank()) {
                    true -> {
                        chargeUpdatedValue = "0.00"
                        binding.txtChargesAddItem.setText(chargeUpdatedValue)
                        binding.txtChargesAddItem.setSelection(chargeUpdatedValue.length)
                    }
                    else -> {
                        when (chargeUpdatedValue.toBigDecimal()
                            .compareTo(BigDecimal.ZERO) == 0) {
                            true -> {
                                binding.txtChargesAddItem.setText(chargeUpdatedValue)
                                binding.txtChargesAddItem.setSelection(chargeUpdatedValue.length)
                            }
                            else -> {
                                binding.txtChargesAddItem.setText(chargeUpdatedValue)
                                binding.txtChargesAddItem.setSelection(chargeUpdatedValue.length)
                            }
                        }

                    }
                }

                updateTotalAmount(true)
            }
        }

        binding.txtDiscountAddItem.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                when (txtDiscountAddItem.text.isNullOrBlank()) {
                    true -> {
                        discountUpdatedValue = "0.00"
                        binding.txtDiscountAddItem.setText(discountUpdatedValue)
                        binding.txtDiscountAddItem.setSelection(discountUpdatedValue.length)
                    }
                    else -> {
                        when (discountUpdatedValue.toBigDecimal()
                            .compareTo(BigDecimal.ZERO) == 0) {
                            true -> {
                                binding.txtDiscountAddItem.setText(discountUpdatedValue)
                                binding.txtDiscountAddItem.setSelection(discountUpdatedValue.length)
                            }
                            else -> {
                                binding.txtDiscountAddItem.setText(discountUpdatedValue)
                                binding.txtDiscountAddItem.setSelection(discountUpdatedValue.length)
                            }
                        }

                    }
                }

                updateTotalAmount(true)
            }
        }



        binding.txtUnitAddItem.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                when (txtUnitAddItem.text.isNullOrBlank()) {
                    true -> {
                        piecesUpdatedValue = "0.00"
                        binding.txtUnitAddItem.setText(piecesUpdatedValue)
                        binding.txtUnitAddItem.setSelection(piecesUpdatedValue.length)
                    }
                    else -> {
                        when (piecesUpdatedValue.toBigDecimal()
                            .compareTo(BigDecimal.ZERO) == 0) {
                            true -> {
                                /*binding.txtUnitAddItem.setText(piecesUpdatedValue)
                                binding.txtUnitAddItem.setSelection(piecesUpdatedValue.length)*/
                            }
                            else -> {
                                binding.txtUnitAddItem.setText(piecesUpdatedValue)
                                binding.txtUnitAddItem.setSelection(piecesUpdatedValue.length)
                            }
                        }

                    }
                }
                when (transaction_type) {
                    //all transaction without opening
                    "sales" -> {
                        settingupchargesTotal()
                    }
                    "purchase" -> {
                        settingupchargesTotal()
                    }
                }
                updateTotalAmount(true)
            }
        }
    }


    private fun ontextChangeSetup() {


        binding.txtTouchAddItem.doAfterTextChanged {

            val str: String = txtTouchAddItem.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 3, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtTouchAddItem.setText(str2)
                binding.txtTouchAddItem.setSelection(str2.length)
            }

            touchUpdatedValue = df.format(str2.toDouble())

        }

        binding.txtWastageAddItem.doAfterTextChanged {

            when (binding.txtWastageAddItem.text.toString().startsWith("-")) {
                true -> if (!binding.txtWastageAddItem.text.toString()
                        .isBlank() && binding.txtWastageAddItem.text.toString().length > 1
                ) {
                    when (binding.txtWastageAddItem.text.toString().startsWith("-.")) {
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

        binding.txtGrossWtAddItem.doAfterTextChanged {

            val str: String = binding.txtGrossWtAddItem.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtGrossWtAddItem.setText(str2)
                binding.txtGrossWtAddItem.setSelection(str2.length)
            }

            grossUpdatedValue = df1.format(str2.toDouble())
            // binding.txtGrossWtAddItem.setText(grossUpdatedValue)

        }


        binding.txtLessWtAddItem.doAfterTextChanged {

            val str: String = binding.txtLessWtAddItem.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtLessWtAddItem.setText(str2)
                binding.txtLessWtAddItem.setSelection(str2.length)
            }

            lesswtUpdatedValue = df1.format(str2.toDouble())

        }

        binding.txtUnitAddItem.doAfterTextChanged {

            val str: String = binding.txtUnitAddItem.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtUnitAddItem.setText(str2)
                binding.txtUnitAddItem.setSelection(str2.length)
            }

            piecesUpdatedValue = df.format(str2.toDouble())
            Log.v("piecesupdatedValue", piecesUpdatedValue)
        }


        binding.txtGoldRateAddItem.doAfterTextChanged {
            val str: String = binding.txtGoldRateAddItem.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtGoldRateAddItem.setText(str2)
                binding.txtGoldRateAddItem.setSelection(str2.length)
            }

            goldRateUpdatedValue = df.format(str2.toDouble())

        }

        binding.txtDiscountAddItem.doAfterTextChanged {
            val str: String = binding.txtDiscountAddItem.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtDiscountAddItem.setText(str2)
                binding.txtDiscountAddItem.setSelection(str2.length)
            }

            discountUpdatedValue = df.format(str2.toDouble())

        }


        binding.txtChargesAddItem.doAfterTextChanged {
            val str: String = binding.txtChargesAddItem.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtChargesAddItem.setText(str2)
                binding.txtChargesAddItem.setSelection(str2.length)
            }

            chargeUpdatedValue = df.format(str2.toDouble())

        }

    }

    private fun updateTotalAmount(is_charge: Boolean) {
        //true-> with Charge
        if (is_charge) {
            when (selectedItemType.equals("Goods")) {
                true -> {
                    var totalAmt: String = "0.00"
                    when(selectedRateon){
                        "fine"->{
                            val goldRate: BigDecimal = goldRateUpdatedValue.toBigDecimal()
                            val fineWeight: BigDecimal = finewtUpdatedValue.toBigDecimal()
                            totalAmt =
                                ((goldRate.setScale(3)
                                    .multiply(fineWeight.setScale(3))
                                        )).setScale(2, RoundingMode.HALF_UP).toString()
                        }
                        "net"->{
                            val goldRate: BigDecimal = goldRateUpdatedValue.toBigDecimal()
                            val netWeight: BigDecimal = netweightUpdatedValue.toBigDecimal()
                            totalAmt =
                                ((goldRate.setScale(3)
                                    .multiply(netWeight.setScale(3))
                                        )).setScale(2, RoundingMode.HALF_UP).toString()
                        }
                        "gross"->{
                            val goldRate: BigDecimal = goldRateUpdatedValue.toBigDecimal()
                            val grossWeight: BigDecimal = grossUpdatedValue.toBigDecimal()
                            totalAmt =
                                ((goldRate.setScale(3)
                                    .multiply(grossWeight.setScale(3))
                                        )).setScale(2, RoundingMode.HALF_UP).toString()
                        }
                        "fix"->{
                            val goldRate: BigDecimal = goldRateUpdatedValue.toBigDecimal()
                            totalAmt = goldRate.toString()
                        }
                    }

                    when (discountUpdatedValue.toBigDecimal().compareTo(BigDecimal.ZERO) == 0) {
                        true -> {
                            toatlAmtUpdatedValue = totalAmt
                            binding.txtTotalAmountAddItem.setText(toatlAmtUpdatedValue)
                        }
                        false -> {
                            toatlAmtUpdatedValue = totalAmt
                            val totalAmtwithCharge: BigDecimal = toatlAmtUpdatedValue.toBigDecimal()
                            val discount: BigDecimal = discountUpdatedValue.toBigDecimal()
                            val totalAmtWithdiscount : String = (totalAmtwithCharge - discount).toString()
                            toatlAmtUpdatedValue =  totalAmtWithdiscount
                            binding.txtTotalAmountAddItem.setText(toatlAmtUpdatedValue)
                        }
                    }
                }
                false -> {
                    val goldRate: BigDecimal = goldRateUpdatedValue.toBigDecimal()
                    val unit: BigDecimal = piecesUpdatedValue.toBigDecimal()
                    val totalAmt: String =
                        ((goldRate.setScale(3)
                            .multiply(unit.setScale(3))
                                )).setScale(2, RoundingMode.HALF_UP).toString()

                    when (discountUpdatedValue.toBigDecimal().compareTo(BigDecimal.ZERO) == 0) {
                        true -> {
                            toatlAmtUpdatedValue = totalAmt
                            binding.txtTotalAmountAddItem.setText(toatlAmtUpdatedValue)
                        }
                        false -> {
                            val totalAmtwithCharge: BigDecimal = toatlAmtUpdatedValue.toBigDecimal()
                            val discount: BigDecimal = discountUpdatedValue.toBigDecimal()
                            val totalAmtWithdiscount : String = (totalAmtwithCharge - discount).toString()
                            toatlAmtUpdatedValue =  totalAmtWithdiscount
                            binding.txtTotalAmountAddItem.setText(toatlAmtUpdatedValue)
                        }
                    }

                }
            }


            when (chargeUpdatedValue.toBigDecimal().compareTo(BigDecimal.ZERO) > 0) {
                true -> {
                    val chargeRate: BigDecimal = chargeUpdatedValue.toBigDecimal()
                    val totalGoldCharge: BigDecimal = toatlAmtUpdatedValue.toBigDecimal()
                    val totalAmtWithCharge: String =
                        ((totalGoldCharge.setScale(2)
                            .plus(chargeRate.setScale(2))
                                )).setScale(2, RoundingMode.HALF_UP).toString()

                    toatlAmtChargeUpdatedValue = totalAmtWithCharge
                    binding.txtTotalAmountAddItem.setText(toatlAmtChargeUpdatedValue)

                }
                false -> {
                    toatlAmtChargeUpdatedValue = toatlAmtUpdatedValue
                    binding.txtTotalAmountAddItem.setText(toatlAmtChargeUpdatedValue)
                }
            }

            if (selectedCustStateId.equals(loginModel.data!!.branch_info!!.state_id.toString())) {
                Log.v("testiuoi", "")
                is_Igst_enable = false
                updateTaxAmout(is_Igst_enable)
            } else {
                is_Igst_enable = true
                updateTaxAmout(is_Igst_enable)
            }
        }
        //false- > without charge
        else {
            if (is_charge) {

                val goldRate: BigDecimal = goldRateUpdatedValue.toBigDecimal()
                val fineWeight: BigDecimal = finewtUpdatedValue.toBigDecimal()
                val totalAmt: String =
                    ((goldRate.setScale(3)
                        .multiply(fineWeight.setScale(3))
                            )).setScale(2, RoundingMode.HALF_UP).toString()
                toatlAmtUpdatedValue = totalAmt
                binding.txtTotalAmountAddItem.setText(toatlAmtUpdatedValue)
                Log.v("itemrate", toatlAmtUpdatedValue)
                Log.v("pcs", piecesUpdatedValue)

            }

        }

    }

    private fun updateTaxAmout(is_Igst_enable: Boolean) {
        when (is_Igst_enable) {
            true -> {
                when (selectedItemGst.toBigDecimal().compareTo(BigDecimal.ZERO) > 0) {
                    true -> {
                        Log.v("taxtrue", "")
                        val totalAmt: BigDecimal = toatlAmtChargeUpdatedValue.toBigDecimal()
                        val taxperce: BigDecimal = selectedItemGst.toBigDecimal()

                        val totaltaxAmt: String =
                            ((totalAmt.setScale(3) * taxperce.setScale(3) / BigDecimal(
                                100
                            ))).setScale(
                                2,
                                RoundingMode.HALF_UP
                            ).toString()

                        igstUpdatedValue = totaltaxAmt
                    }
                    false -> {
                        igstUpdatedValue = "0.00"
                    }
                }

            }
            false -> {
                when (selectedItemGst.toBigDecimal().compareTo(BigDecimal.ZERO) > 0) {
                    true -> {
                        val totalAmt: BigDecimal = toatlAmtChargeUpdatedValue.toBigDecimal()
                        val taxperce: BigDecimal = selectedItemGst.toBigDecimal()

                        val totaltaxAmt: String =
                            ((totalAmt.setScale(3) * taxperce.setScale(3) / BigDecimal(
                                100
                            ))).setScale(
                                2,
                                RoundingMode.DOWN
                            ).toString()

                        toatltaxAmtUpdatedValue = totaltaxAmt
                        Log.v("taxvalue", "" + toatltaxAmtUpdatedValue)

                        var totalTaxSgst: BigDecimal = toatltaxAmtUpdatedValue.toBigDecimal()
                        val result: String =
                            ((totalTaxSgst.setScale(3)
                                .divide("2".toBigDecimal().setScale(3))
                                    )).setScale(2, RoundingMode.HALF_UP).toString()

                        sgstUpdatedValue = result
                        cgstUpdatedValue = result
                        Log.v("sgdt", "" + sgstUpdatedValue + cgstUpdatedValue)
                    }
                    false -> {
                        sgstUpdatedValue = "0.00"
                        cgstUpdatedValue = "0.00"
                    }
                }

            }
        }
    }


    private fun clearPref() {
        if (prefs.contains(Constants.PREF_LESS_WEIGHT_BREAKUP_INFO_KEY)) {
            prefs.edit().remove(Constants.PREF_LESS_WEIGHT_BREAKUP_INFO_KEY).apply()
        }
        if (prefs.contains(Constants.PREF_LESS_WEIGHT_INFO_KEY)) {
            prefs.edit().remove(Constants.PREF_LESS_WEIGHT_INFO_KEY).apply()
        }
        if (prefs.contains(Constants.PREF_MAKING_CHARGES_KEY)) {
            prefs.edit().remove(Constants.PREF_MAKING_CHARGES_KEY).apply()
        }
        if (prefs.contains(Constants.PREF_OTHER_CHARGES_KEY)) {
            prefs.edit().remove(Constants.PREF_OTHER_CHARGES_KEY).apply()
        }
        if (prefs.contains(Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY)) {
            prefs.edit().remove(Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY).apply()
        }
        /* if (prefs.contains(Constants.PREF_OPENINGSTOCK_INFO_KEY)) {
             prefs.edit().remove(Constants.PREF_OPENINGSTOCK_INFO_KEY).apply()
         }*/
        /* if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {
             prefs.edit().remove(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY).apply()
         }*/
    }


    private fun setWastagenFinewt(df: DecimalFormat) {
        val str: String = txtWastageAddItem.text.toString()

        if (str.isEmpty()) return
        val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
        if (!str2.equals(str)) {
            //val str3:String = df.format(str2.toDouble())
            txtWastageAddItem.setText(str2)
            txtWastageAddItem.setSelection(str2.length)
        }
        /* Log.v("wastage", (df.format(str2.toDouble())))*/
        wastageUpdatedValue = df.format(str2.toDouble())
        //binding.txtWastageAddItem.setText(wastageUpdatedValue)
        wastage = txtWastageAddItem.text.toString().toBigDecimal()

        if (!binding.txtNetWtAddItem.text.toString()
                .isBlank() && !txtTouchAddItem.text.toString()
                .isBlank()
        ) {
            val net: Float = binding.txtNetWtAddItem.text.toString().toFloat()
            val touch: Float = txtTouchAddItem.text.toString().toFloat()
            Log.v("touchw", "" + touch + wastage)

            val result: Float = (net * (touch.plus(wastage.toFloat())).div(100F))
            /*val result: String =
                ((net.setScale(3) * (touch.setScale(3) + wastage.setScale(3)) / BigDecimal(
                    100
                ))).setScale(3, RoundingMode.CEILING).toString()*/
            val result1: BigDecimal =
                result.toString().toBigDecimal().setScale(3, RoundingMode.HALF_UP)

            finewtUpdatedValue = result1.toString()
            Log.v("finewtUpdatedValue", "" + finewtUpdatedValue)
            when (goldRateUpdatedValue.toBigDecimal().compareTo(BigDecimal.ZERO) > 0) {
                true -> {
                    binding.txtFineWtAddItem.setText("0.000")
                    updateTotalAmount(true)
                }
                false -> {
                    binding.txtFineWtAddItem.setText(finewtUpdatedValue)
                }
            }
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
    }

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected) {
            // Network is available
            CommonUtils.hideInternetDialog()
            when (isLoadedOnce) {
                false -> {
                    when (transaction_type) {
                        "opening_stock" -> {
                            getItemSearch("opening_stock", "", false)
                        }
                        "sales" -> {
                            getItemSearch("sales", "", false)
                        }
                        "purchase" -> {
                            getItemSearch("purchase", "", false)
                        }
                        "receipt" -> {
                            getItemSearch("receipt", "", false)
                        }
                        "payment" -> {
                            getItemSearch("payment", "", false)
                        }
                    }

                }
                else -> {

                }
            }


            //New Item

            getLessWeightMakingChargeFromPref()


        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun getLessWeightMakingChargeFromPref() {
        if (prefs.contains(Constants.PREF_LESS_WEIGHT_BREAKUP_INFO_KEY)) {

            val lessWeightBreakup =
                object :
                    TypeToken<OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup>() {}.type
            lessweightBreakupList = Gson().fromJson(
                prefs[Constants.PREF_LESS_WEIGHT_BREAKUP_INFO_KEY, ""],
                lessWeightBreakup
            )


            convertvalueInGrams(item_maintain_stock_in_name, binding.txtLessWtAddItem)
            updateNetwt()


            /*binding.txtLessWtAddItem.setText(lessweightBreakupList.total_less_wt)
            binding.txtLessWtAddItem.setSelection(lessweightBreakupList.total_less_wt.length)
            binding.txtChargesAddItem.setText(lessweightBreakupList.total_less_wt_amount)*/
            // lesswtUpdatedValue = lessweightBreakupList.get(0).total_less_wt
        } else {
            txtLessWtAddItem.setText("0.000")
        }

        if (prefs.contains(Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY)) {
            val makingChargeBreakup =
                object :
                    TypeToken<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup>() {}.type
            makingChargeBreakupList = Gson().fromJson(
                prefs[Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY, ""],
                makingChargeBreakup
            )

            binding.txtChargesAddItem.setText(makingChargeBreakupList.total_charges)
            binding.txtChargesAddItem.setSelection(makingChargeBreakupList.total_charges.length)
            updateTotalAmount(true)
        }


        /*if (prefs.contains(Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY)) {
            val makingChargeBreakup =
                object :
                    TypeToken<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup>() {}.type
            makingChargeBreakupList = Gson().fromJson(
                prefs[Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY, ""],
                makingChargeBreakup
            )

            selectedMakingPerID = makingChargeBreakupList.making_charge_array.unit_id
            Log.v("selectedMakingPerID", selectedMakingPerID)
            binding.txtChargesAddItem.setText(makingChargeBreakupList.total_charges)
            binding.txtChargesAddItem.setSelection(makingChargeBreakupList.total_charges.length)

            // chargeUpdatedValue = makingChargeBreakupList.get(0).total_charges
        }*/
        //updateTotalAmount(true)
        /*settingupchargesTotal()
        updateTotalAmount(true)*/
    }

    private fun updateNetwt() {

        when (txtLessWtAddItem.text.isNullOrBlank()) {
            true -> {
                lesswtUpdatedValue = "0.000"
                binding.txtLessWtAddItem.setText(lesswtUpdatedValue)
                binding.txtLessWtAddItem.setSelection(lesswtUpdatedValue.length)
            }
            else -> {
                when (lesswtUpdatedValue.toBigDecimal().compareTo(BigDecimal.ZERO) == 0) {
                    true -> {

                    }
                    else -> {
                        if (!binding.txtGrossWtAddItem.text.toString()
                                .isBlank() && !binding.txtLessWtAddItem.text.toString()
                                .isBlank()
                        ) {
                            val gross: BigDecimal =
                                binding.txtGrossWtAddItem.text.toString().toBigDecimal()
                            val less: BigDecimal =
                                binding.txtLessWtAddItem.text.toString().toBigDecimal()
                            val result: String = (gross - less).toString()
                            binding.txtNetWtAddItem.setText(result)
                            netweightUpdatedValue = result
                            net_wt = netweightUpdatedValue
                        }
                        when (transaction_type) {
                            //all transaction without opening
                            "sales" -> {
                                settingupchargesTotal()
                                updateTotalAmount(true)
                            }
                            "purchase" -> {
                                settingupchargesTotal()
                                updateTotalAmount(true)
                            }
                            "receipt"->{
                                settingupchargesTotal()
                                updateTotalAmount(true)
                            }
                            "payment"->{
                                settingupchargesTotal()
                                updateTotalAmount(true)
                            }
                        }
                        var wastage: BigDecimal = BigDecimal(0)
                        if (!binding.txtWastageAddItem.text.toString().isBlank()) {
                            wastage =
                                binding.txtWastageAddItem.text.toString().toBigDecimal()
                        }
                        if (!binding.txtNetWtAddItem.text.toString()
                                .isBlank() && !binding.txtTouchAddItem.text.toString()
                                .isBlank()
                        ) {
                            val net: Float = binding.txtNetWtAddItem.text.toString().toFloat()
                            val touch: Float = txtTouchAddItem.text.toString().toFloat()

                            val result: Float = (net * (touch.plus(wastage.toFloat())).div(100F))
                            /*val result: String =
                                ((net.setScale(3) * (touch.setScale(3) + wastage.setScale(3)) / BigDecimal(
                                    100
                                ))).setScale(3, RoundingMode.CEILING).toString()*/
                            val result1: BigDecimal =
                                result.toString().toBigDecimal().setScale(3, RoundingMode.HALF_UP)

                            finewtUpdatedValue = result1.toString()
                            //  finewtUpdatedValue = result
                            Log.d("finewtless",""+finewtUpdatedValue)
                            when (goldRateUpdatedValue.toBigDecimal()
                                .compareTo(BigDecimal.ZERO) > 0) {
                                true -> {
                                    binding.txtFineWtAddItem.setText("0.000")
                                    updateTotalAmount(true)
                                }
                                false -> {
                                    binding.txtFineWtAddItem.setText(finewtUpdatedValue)
                                }
                            }
                            // binding.txtFineWtAddItem.setText(result)
                        }
                    }
                }
            }
        }


    }

    //convert any values to grams
    private fun convertvalueInGrams(
        itemMaintainStockInName: String,
        txtLessWtAddItem: TextInputEditText
    ) {
        // less wt value update set according to maintain stock in item selected in add item

        when (lessweightBreakupList.total_less_wt.toBigDecimal()
            .compareTo(BigDecimal.ZERO) > 0) {
            true -> {
                when (itemMaintainStockInName) {
                    "Kilograms" -> {
                        txtLessWtAddItem.setText(CommonUtils.gmsTokg(lessweightBreakupList.total_less_wt))
                    }
                    "Carat" -> {
                        txtLessWtAddItem.setText(CommonUtils.gmsTocarrot(lessweightBreakupList.total_less_wt))
                    }
                    "Grams" -> {
                        txtLessWtAddItem.setText(lessweightBreakupList.total_less_wt)

                    }
                }
            }
            false -> {
                txtLessWtAddItem.setText(lessweightBreakupList.total_less_wt)
            }
        }
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(
                this,
                ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))
            ).get(
                AddItemViewModel::class.java
            )
        binding.setLifecycleOwner(this)
    }

    private fun applyingDigitFilter() {
        // applying filters to edit input number decimal fields(which have 2 or 3 decimal after .)

        binding.txtGrossWtAddItem.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )
        binding.txtLessWtAddItem.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )
        binding.txtTouchAddItem.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    3,
                    2,
                    100.00
                )
            )
        )
        binding.txtWastageAddItem.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )
        binding.txtGoldRateAddItem.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )
        binding.txtChargesAddItem.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    10,
                    2,
                    9999999999.99
                )
            )
        )

    }

    private fun openColorMenu(colorNameList: List<String>?) {

        popupMenu = PopupMenu(this, txtColourAddItem)
        for (i in 0 until this.colorNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                this.colorNameList!!.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtColourAddItem.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = this.colorNameList!!.indexOf(selected)

            selectedColorId =
                pos?.let { it1 -> colorList?.get(it1)?.id }.toString()

            selectedColorName = pos?.let { it1 -> colorList?.get(it1)?.colour_name }.toString()

            true
        })

        popupMenu.show()
    }


    private fun openStampMenu(stampNameList: List<String>?) {

        popupMenu = PopupMenu(this, txtStampAddItem)
        for (i in 0 until this.stampNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                this.stampNameList!!.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtStampAddItem.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = this.stampNameList!!.indexOf(selected)

            selectedStampId =
                pos?.let { it1 -> stampList?.get(it1)?.id }.toString()

            selectedStampName =
                pos?.let { it1 -> stampList?.get(it1)?.stamp_name }.toString()


            true
        })

        popupMenu.show()
    }


    fun getItemSearch(module: String?, itemName: String?, isFromEdit: Boolean) {
        if (NetworkUtils.isConnected()) {
            viewModel.getItemSearch(
                loginModel?.data?.bearer_access_token,
                itemName,
                module,
                ""
            ).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            /*Log.v("..setupObservers..", "..Success...")*/
                            if (it.data?.status == true) {
                                isLoadedOnce = false
                                /*when(isFromEdit){
                                    false->{
                                       // isLoadedOnce = true
                                    }
                                    true ->{
                                        //isselectedItemLoadOnce = false
                                        colorList = itemList!!.get(0).color
                                    }
                                }*/
                                // binding.txtRateonAddItem.text!!.clear()
                                itemList = it.data.data

                                itemNameList = itemList?.map { it.item_name.toString() }

                                ItemDetailsAdapter = ItemDetailsAdapter(
                                    this, true, R.layout.search_item_popup,
                                    itemList!!
                                )
                                /*  ItemDetailsAdapter.apply { addItems(itemList)
                                      notifyDataSetChanged()}*/
                                binding.txtItemNameAddItem.setAdapter(ItemDetailsAdapter)
                                binding.txtItemNameAddItem.threshold = 1


                                if (isFromEdit) {
                                    selectedItemMetalType = itemList!!.get(0).metal_type_name
                                    when (selectedItemMetalType.equals("Other")) {
                                        true -> {
                                            binding.llTouchWastage.visibility = View.GONE
                                            binding.tvLessWtAddItem.visibility = View.GONE
                                        }
                                        false -> {
                                            binding.llTouchWastage.visibility = View.VISIBLE
                                            binding.tvLessWtAddItem.visibility = View.VISIBLE
                                        }
                                    }
                                    colorList = itemList!!.get(0).color
                                    Log.v("color", "" + colorList)
                                    //   selectedItemID = itemList!!.get(0).id
                                    when (colorList!!.size > 0) {
                                        true -> {

                                            colorNameList = colorList?.map { it.colour_name }
                                            setDrawableTint(
                                                binding.tvColourAddItem,
                                                resources.getColor(R.color.drop_down_arrow_color)
                                            )
                                            Log.v(
                                                "colorlist",
                                                "true" + colorNameList.toString()
                                            )

                                            //binding.tvColourAddItem.visibility = View.VISIBLE
                                            for (i in 0 until colorList!!.size) {
                                                if (colorList!!.get(i).id.equals(
                                                        selectedColorId
                                                    )
                                                ) {
                                                    binding.txtColourAddItem.setText(
                                                        colorList!!.get(i).colour_name
                                                    )
                                                }
                                            }

                                        }
                                        false -> {
                                            // binding.tvColourAddItem.visibility = View.GONE
                                            //binding.txtColourAddItem.setText("")
                                            /* setDrawableTint(
                                                binding.tvColourAddItem,
                                                resources.getColor(R.color.end_icon_gray)
                                            )*/
                                        }
                                    }

                                }

                                binding.txtItemNameAddItem.setOnItemClickListener { adapterView, view, position, l
                                    ->
                                    val selected: String =
                                        adapterView.getItemAtPosition(position).toString()

                                    val pos: Int? =
                                        itemList?.get(0)?.item_name?.indexOf(selected)

                                    /* selectedItemID = pos?.let { it1 -> itemList?.get(it1)?.id
                                     }*/

                                    binding.txtStampAddItem.text.clear()
                                    binding.txtColourAddItem.text.clear()

                                    val selectedPoi =
                                        adapterView.adapter.getItem(position) as ItemSearchModel.ItemSearch?

                                    selectedItemModel = selectedPoi!!

                                    selectedItemID = selectedPoi?.id.toString()
                                    binding.txtItemNameAddItem.setText(selectedPoi?.item_name)
                                    selectedItemName = selectedPoi.item_name!!
                                    binding.txtItemNameAddItem.setSelection(selectedPoi?.item_name?.length!!)
                                    selectedItemMetalType = selectedPoi.metal_type_name

                                    if (selectedPoi.metal_type_name.equals("Other")) {
                                        binding.llTouchWastage.visibility = View.GONE
                                        binding.tvLessWtAddItem.visibility = View.GONE
                                        binding.txtRateonAddItem.setText("Gross Weight")
                                        selectedRateon = "gross"
                                    } else {
                                        binding.llTouchWastage.visibility = View.VISIBLE
                                        binding.tvLessWtAddItem.visibility = View.VISIBLE
                                        binding.txtRateonAddItem.setText("Fine Weight")
                                        selectedRateon = "fine"
                                    }
                                    Log.v(
                                        "details",
                                        "" + selectedPoi?.unit_name + selectedPoi?.id
                                    )

                                    is_studed = selectedPoi.is_studded
                                    useColor = selectedPoi.use_gold_color


                                    selectedItemType = selectedPoi.item_type!!
                                    when (selectedItemType.equals("Goods")) {
                                        true -> {
                                            binding.tvUnitAddItem.hint =
                                                "Units" + " (" + selectedPoi.unit_name + ")"

                                            binding.tvGrossWtAddItem.hint =
                                                "Gross Weight" + " (" + selectedPoi.maintain_stock_in_name + ")"
                                        }
                                        else -> {

                                        }
                                    }

                                    if (!transaction_type.equals("opening_stock")) {

                                        when (selectedItemType.equals("Goods")) {
                                            true -> {

                                                binding.txtStampAddItem.isEnabled = true
                                                binding.txtColourAddItem.isEnabled = true
                                                binding.txtGrossWtAddItem.isEnabled = true
                                                binding.txtLessWtAddItem.isEnabled = true
                                                binding.txtNetWtAddItem.isEnabled = true
                                                binding.txtTouchAddItem.isEnabled = true
                                                binding.txtWastageAddItem.isEnabled = true
                                                binding.txtFineWtAddItem.isEnabled = true
                                                binding.txtRateonAddItem.isEnabled = true
                                            }
                                            false -> {
                                                /* binding.tvUnitAddItem.hint =
                                                    "Units" + " (" + selectedPoi.unit_name + ")"*/
                                                binding.txtStampAddItem.isEnabled = false
                                                binding.txtColourAddItem.isEnabled = false
                                                binding.txtGrossWtAddItem.isEnabled = false
                                                binding.txtLessWtAddItem.isEnabled = false
                                                binding.txtNetWtAddItem.isEnabled = false
                                                binding.txtTouchAddItem.isEnabled = false
                                                binding.txtWastageAddItem.isEnabled = false
                                                binding.txtFineWtAddItem.isEnabled = false
                                                binding.txtRateonAddItem.isEnabled = false
                                            }
                                        }

                                    }

                                    when (transaction_type) {
                                        "sales" -> {
                                            is_tax_preferrence = selectedPoi.tax_preference
                                            selectedItemLedgerId = selectedPoi.sales_ledger_id
                                            selectedItemLedgerName =
                                                selectedPoi.sales_ledger_name
                                            selectedItemHsn = selectedPoi.sales_purchase_hsn
                                            selectedItemGst =
                                                selectedPoi.sales_purchase_gst_rate
                                            selectedItemGstId =
                                                selectedPoi.sales_purchase_gst_rate_id
                                            if (is_gst_applicable.equals("1")) {
                                                if (selectedItemLedgerId.equals("")) {
                                                    binding.tvLedgerAddItem.visibility =
                                                        View.VISIBLE
                                                    getLedgerdd("sales")
                                                } else {
                                                    binding.tvLedgerAddItem.visibility =
                                                        View.GONE
                                                }
                                            }

                                        }
                                        "purchase" -> {
                                            is_tax_preferrence = selectedPoi.tax_preference
                                            selectedItemLedgerId =
                                                selectedPoi.purchase_ledger_id
                                            selectedItemLedgerName =
                                                selectedPoi.purchase_ledger_name
                                            selectedItemHsn = selectedPoi.sales_purchase_hsn
                                            selectedItemGst =
                                                selectedPoi.sales_purchase_gst_rate
                                            selectedItemGstId =
                                                selectedPoi.sales_purchase_gst_rate_id
                                            if (is_gst_applicable.equals("1")) {
                                                if (selectedItemLedgerId.equals("")) {
                                                    binding.tvLedgerAddItem.visibility =
                                                        View.VISIBLE
                                                    getLedgerdd("purchase")
                                                } else {
                                                    binding.tvLedgerAddItem.visibility =
                                                        View.GONE
                                                }
                                            }

                                        }
                                    }

                                    colorList = selectedPoi?.color
                                    when (colorList!!.size > 0) {
                                        true -> {
                                            colorNameList = colorList?.map { it.colour_name }
                                            setDrawableTint(
                                                binding.tvColourAddItem,
                                                resources.getColor(R.color.drop_down_arrow_color)
                                            )
                                        }
                                        false -> {
                                            binding.txtColourAddItem.setText("")
                                            setDrawableTint(
                                                binding.tvColourAddItem,
                                                resources.getColor(R.color.end_icon_gray)
                                            )

                                        }
                                    }


                                    item_maintain_stock_in_id =
                                        selectedPoi.maintain_stock_in_id
                                    item_maintain_stock_in_name =
                                        selectedPoi.maintain_stock_in_name
                                    item_metal_type_id = selectedPoi.metal_type_id
                                    item_metal_type_name = selectedPoi.metal_type_name
                                    item_unit_id = selectedPoi.unit_id
                                    item_unit_name = selectedPoi.unit_name
                                    item_use_stamp = selectedPoi.use_stamp

                                    Log.v("use stamp", "" + item_use_stamp + useColor)


                                    unitArrayList = selectedPoi.unit_array
                                    if (selectedItemType.equals("Goods")) {
                                        when (item_use_stamp.equals("1")) {
                                            true -> {
                                                getItemStamp(selectedItemID.trim())
                                                setDrawableTint(
                                                    binding.tvStampAddItem,
                                                    resources.getColor(R.color.drop_down_arrow_color)
                                                )
                                                if (useColor.equals("0")) {
                                                    val params = LinearLayout.LayoutParams(
                                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                                                    )
                                                    binding.tvStampAddItem.setLayoutParams(params)
                                                    binding.tvColourAddItem.visibility = View.GONE
                                                    binding.tvStampAddItem.visibility = View.VISIBLE
                                                } else {
                                                    binding.tvStampAddItem.visibility = View.VISIBLE
                                                    binding.tvColourAddItem.visibility =
                                                        View.VISIBLE
                                                    val params = LinearLayout.LayoutParams(
                                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                                        LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f
                                                    )
                                                    params.setMargins(10, 0, 0, 0)
                                                    binding.tvColourAddItem.setLayoutParams(params)

                                                    val stampParams = LinearLayout.LayoutParams(
                                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                                        LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f
                                                    )
                                                    stampParams.setMargins(0, 0, 10, 0)
                                                    binding.tvStampAddItem.setLayoutParams(
                                                        stampParams
                                                    )
                                                }
                                                /*binding.txtStampAddItem.isEnabled = true
                                                setDrawableTint(
                                                    binding.tvStampAddItem,
                                                    resources.getColor(R.color.drop_down_arrow_color)
                                                )*/
                                            }
                                            false -> {
                                                // binding.txtStampAddItem.isEnabled = false
                                                binding.txtStampAddItem.setText("")

                                                if (useColor.equals("1")) {
                                                    val params = LinearLayout.LayoutParams(
                                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                                                    )
                                                    binding.tvColourAddItem.setLayoutParams(params)
                                                    binding.tvStampAddItem.visibility = View.GONE
                                                    binding.tvColourAddItem.visibility =
                                                        View.VISIBLE

                                                } else {
                                                    binding.tvStampAddItem.visibility = View.GONE
                                                    binding.tvColourAddItem.visibility = View.GONE
                                                    binding.tvColourAddItem.setLayoutParams(
                                                        LinearLayout.LayoutParams(
                                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                                            LinearLayout.LayoutParams.WRAP_CONTENT,
                                                            0.5f
                                                        )
                                                    )
                                                    binding.tvStampAddItem.setLayoutParams(
                                                        LinearLayout.LayoutParams(
                                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                                            LinearLayout.LayoutParams.WRAP_CONTENT,
                                                            0.5f
                                                        )
                                                    )
                                                }

                                                /*setDrawableTint(
                                                    binding.tvStampAddItem,
                                                    resources.getColor(R.color.end_icon_gray)
                                                )*/

                                            }
                                        }





                                        if (useColor.equals("0", true)) {
                                            /*binding.txtColourAddItem.isEnabled = false
                                            binding.tvColourAddItem.isEnabled = false*/
                                            binding.tvColourAddItem.visibility = View.GONE


                                        } else {
                                            binding.tvColourAddItem.visibility = View.VISIBLE
                                            setDrawableTint(
                                                binding.tvColourAddItem,
                                                resources.getColor(R.color.drop_down_arrow_color)
                                            )
                                        }
                                    } else {
                                        setDrawableTint(
                                            binding.tvColourAddItem,
                                            resources.getColor(R.color.end_icon_gray)
                                        )
                                        setDrawableTint(
                                            binding.tvStampAddItem,
                                            resources.getColor(R.color.end_icon_gray)
                                        )
                                    }

                                    when (transaction_type) {
                                        "opening_stock" -> {

                                        }
                                        "sales" -> {
                                            binding.txtWastageAddItem.setText(selectedItemModel.sales_wastage)
                                            selectedMakingPerID = "maintain_stock_in"
                                            makingChrgsUpdatedValue =
                                                selectedItemModel.sales_making_charges
                                            Log.v("sales_mcuv", makingChrgsUpdatedValue)
                                        }
                                        // other transacitons (sales/purchase/payment/receipt
                                        "purchase" -> {
                                            binding.txtWastageAddItem.setText(selectedItemModel.purchase_wastage)
                                            selectedMakingPerID = "maintain_stock_in"
                                            makingChrgsUpdatedValue =
                                                selectedItemModel.purchase_making_charges
                                            Log.v("purchase_mcuv", makingChrgsUpdatedValue)
                                        }
                                    }

                                    when (transaction_type) {
                                        "opening_stock" -> {

                                        }
                                        "sales" -> {

                                        }
                                        // other transacitons (sales/purchase/payment/receipt
                                        "purchase" -> {

                                        }
                                    }


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
                                    /* "jobwork" -> {
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
                                     }*/

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

    private fun setDrawableTint(
        textInput: TextInputLayout,
        color: Int
    ) {
        val myDrawable =
            ContextCompat.getDrawable(this, R.drawable.ic_keyboard_arrow_down_white)
        myDrawable!!.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        textInput.endIconDrawable = myDrawable
    }

    private fun settingupchargesTotal() {
        getUpdatedMakingvalueFromPref()
        calculationTotalOfMakingCharges()
        calculateTotalofOtherCharges()
        calulateTotalofLessWts()
        calulateTotalOfAllcharges()
        binding.txtChargesAddItem.setText(totalOfAllChrgsUpdatedValue)
        binding.txtChargesAddItem.setSelection(totalOfAllChrgsUpdatedValue.length)


    }

    private fun calculateTotalofOtherCharges() {
        if (prefs.contains(Constants.PREF_OTHER_CHARGES_KEY)) {
            val collectionType = object :
                TypeToken<java.util.ArrayList<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.ChargesArray>>() {}.type
            addOtherChargeList =
                Gson().fromJson(prefs[Constants.PREF_OTHER_CHARGES_KEY, ""], collectionType)
            for (i in 0 until addOtherChargeList.size) {
                updateAllotherchargesValueforTotalofOthers(i, addOtherChargeList.get(i).unit_id)
            }
            calculationTotalOfOtherCharges()
        }
    }

    private fun calulateTotalofLessWts() {
        if (prefs.contains(Constants.PREF_LESS_WEIGHT_INFO_KEY)) {
            val collectionType =
                object :
                    TypeToken<ArrayList<AddLessWeightModel.AddLessWeightModelItem>>() {}.type
            lessweightList =
                Gson().fromJson(prefs[Constants.PREF_LESS_WEIGHT_INFO_KEY, ""], collectionType)
            lesswtTotal = arrayListOf()
            listoflesswtChargesDetail = arrayListOf()
            for (data in lessweightList) {
                lesswtTotal.add(data.less_wt_total_amount)
                listoflesswtChargesDetail.add(
                    CalculationPaymentModel.DataPayment.ItemPayment.LessWeights(
                        data.less_wt_item_name,
                        data.less_wt_total_amount
                    )
                )
                if (lesswtTotal.size == 1) {
                    totalLwChargesUpdatedValue = data.less_wt_total_amount
                } else {
                    // 1 onwards
                    val tempLessWt1: BigDecimal = totalLwChargesUpdatedValue.toBigDecimal()
                    val tempLessWt2: BigDecimal = data.less_wt_total_amount.toBigDecimal()
                    totalLwChargesUpdatedValue =
                        ((tempLessWt1.setScale(2)
                            .plus(tempLessWt2.setScale(2))
                                )).setScale(2, RoundingMode.HALF_UP).toString()
                }
            }
            Log.v("totalLw", totalLwChargesUpdatedValue)
        }
    }

    private fun calulateTotalOfAllcharges() {
        val totalLwCharges: BigDecimal = totalLwChargesUpdatedValue.toBigDecimal()
        val totalMakingCharges: BigDecimal = totalmakingChrgsUpdatedValue.toBigDecimal()
        val totalOtherCharges: BigDecimal = totalOtherChrgsUpdatedValue.toBigDecimal()
        totalOfAllChrgsUpdatedValue =
            ((totalLwCharges.setScale(2)
                .plus(totalMakingCharges.setScale(2).plus(totalOtherCharges.setScale(2)))
                    )).setScale(2, RoundingMode.HALF_UP).toString()
    }

    private fun getUpdatedMakingvalueFromPref() {
        if (prefs.contains(Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY)) {
            val makingChargeBreakup =
                object :
                    TypeToken<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup>() {}.type
            makingChargeBreakupList = Gson().fromJson(
                prefs[Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY, ""],
                makingChargeBreakup
            )
            makingChrgsUpdatedValue = makingChargeBreakupList.making_charge_array.amount
            selectedMakingPerID = makingChargeBreakupList.making_charge_array.unit_id
            binding.txtChargesAddItem.setText(makingChrgsUpdatedValue)
            binding.txtChargesAddItem.setSelection(makingChrgsUpdatedValue.length)
        }
    }

    private fun calculationTotalOfMakingCharges() {
        when (selectedMakingPerID) {
            "unit" -> {
                Log.v("unit", selectedMakingPerID)
                val pcs: BigDecimal = piecesUpdatedValue.toBigDecimal()
                val makingcharge: BigDecimal =
                    makingChrgsUpdatedValue.toBigDecimal()
                totalmakingChrgsUpdatedValue =
                    ((pcs.setScale(2)
                        .multiply(makingcharge.setScale(2))
                            )).setScale(2, RoundingMode.HALF_UP).toString()
                Log.v("totalmaking", totalmakingChrgsUpdatedValue)

            }
            "maintain_stock_in" -> {
                Log.v("msi", selectedMakingPerID)
                val netwt: BigDecimal = netweightUpdatedValue.toBigDecimal()
                val makingcharge: BigDecimal =
                    makingChrgsUpdatedValue.toBigDecimal()
                totalmakingChrgsUpdatedValue =
                    ((netwt.setScale(3)
                        .multiply(makingcharge.setScale(2))
                            )).setScale(2, RoundingMode.HALF_UP).toString()
                Log.v("makingchrgupdvalue", makingcharge.toString())
                Log.v("netwtupdaval", netwt.toString())
                Log.v("totalmaking", totalmakingChrgsUpdatedValue)
            }
            "fix" -> {
                totalmakingChrgsUpdatedValue = makingChrgsUpdatedValue
            }
        }


        /*  binding.txtChargesAddItem.setText(totalmakingChrgsUpdatedValue)
          binding.txtChargesAddItem.setSelection(totalmakingChrgsUpdatedValue.length)*/
    }

    private fun updateAllotherchargesValueforTotalofOthers(
        listPos: Int,
        selectedOtherID: String
    ) {
        when (selectedOtherID) {
            "unit" -> {
                val pcs: BigDecimal = piecesUpdatedValue.toBigDecimal()
                when (listPos) {
                    // other1
                    0 -> {
                        Log.v("unit", listPos.toString())
                        otherChrgs1UpdatedValue = addOtherChargeList.get(listPos).amount
                        val othercharge1: BigDecimal =
                            otherChrgs1UpdatedValue.toBigDecimal()
                        otherChrgs1CalculatedUpdatedValue =
                            (othercharge1.setScale(2).multiply(pcs.setScale(2))
                                .setScale(2, RoundingMode.HALF_UP).toString())
                        Log.v("otcharge1", otherChrgs1CalculatedUpdatedValue)
                    }
                    1 -> {
                        Log.v("unit", listPos.toString())
                        otherChrgs2UpdatedValue = addOtherChargeList.get(listPos).amount
                        val othercharge2: BigDecimal =
                            otherChrgs2UpdatedValue.toBigDecimal()
                        otherChrgs2CalculatedUpdatedValue =
                            (othercharge2.setScale(2).multiply(pcs.setScale(2))
                                .setScale(2, RoundingMode.HALF_UP).toString())
                        Log.v("otcharge2", otherChrgs2CalculatedUpdatedValue)
                    }
                    2 -> {
                        Log.v("unit", listPos.toString())
                        otherChrgs3UpdatedValue = addOtherChargeList.get(listPos).amount
                        val othercharge3: BigDecimal =
                            otherChrgs3UpdatedValue.toBigDecimal()
                        otherChrgs3CalculatedUpdatedValue =
                            (othercharge3.setScale(2).multiply(pcs.setScale(2))
                                .setScale(2, RoundingMode.HALF_UP).toString())
                    }
                    3 -> {
                        Log.v("unit", listPos.toString())
                        otherChrgs4UpdatedValue = addOtherChargeList.get(listPos).amount
                        val othercharge4: BigDecimal =
                            otherChrgs4UpdatedValue.toBigDecimal()
                        otherChrgs4CalculatedUpdatedValue =
                            (othercharge4.setScale(2).multiply(pcs.setScale(2))
                                .setScale(2, RoundingMode.HALF_UP).toString())
                    }
                    4 -> {
                        Log.v("unit", listPos.toString())
                        otherChrgs5UpdatedValue = addOtherChargeList.get(listPos).amount
                        val othercharge5: BigDecimal =
                            otherChrgs5UpdatedValue.toBigDecimal()
                        otherChrgs5CalculatedUpdatedValue =
                            (othercharge5.setScale(2).multiply(pcs.setScale(2))
                                .setScale(2, RoundingMode.HALF_UP).toString())
                    }
                }
            }
            "maintain_stock_in" -> {
                val netwt: BigDecimal = netweightUpdatedValue.toBigDecimal()
                when (listPos) {
                    // other1
                    0 -> {
                        otherChrgs1UpdatedValue = addOtherChargeList.get(listPos).amount
                        val othercharge1: BigDecimal =
                            otherChrgs1UpdatedValue.toBigDecimal()
                        otherChrgs1CalculatedUpdatedValue =
                            (othercharge1.setScale(2).multiply(netwt.setScale(3))
                                .setScale(2, RoundingMode.HALF_UP).toString())
                        Log.v("msiotcharge1", otherChrgs1CalculatedUpdatedValue)
                    }
                    1 -> {
                        otherChrgs2UpdatedValue = addOtherChargeList.get(listPos).amount
                        val othercharge2: BigDecimal =
                            otherChrgs2UpdatedValue.toBigDecimal()
                        otherChrgs2CalculatedUpdatedValue =
                            (othercharge2.setScale(2).multiply(netwt.setScale(3))
                                .setScale(2, RoundingMode.HALF_UP).toString())
                        Log.v("msiotcharge2", otherChrgs2CalculatedUpdatedValue)
                    }
                    2 -> {
                        otherChrgs3UpdatedValue = addOtherChargeList.get(listPos).amount
                        val othercharge3: BigDecimal =
                            otherChrgs3UpdatedValue.toBigDecimal()
                        otherChrgs3CalculatedUpdatedValue =
                            (othercharge3.setScale(2).multiply(netwt.setScale(3))
                                .setScale(2, RoundingMode.HALF_UP).toString())
                    }
                    3 -> {
                        otherChrgs4UpdatedValue = addOtherChargeList.get(listPos).amount
                        val othercharge4: BigDecimal =
                            otherChrgs4UpdatedValue.toBigDecimal()
                        otherChrgs4CalculatedUpdatedValue =
                            (othercharge4.setScale(2).multiply(netwt.setScale(3))
                                .setScale(2, RoundingMode.HALF_UP).toString())
                    }
                    4 -> {
                        otherChrgs5UpdatedValue = addOtherChargeList.get(listPos).amount
                        val othercharge5: BigDecimal =
                            otherChrgs5UpdatedValue.toBigDecimal()
                        otherChrgs5CalculatedUpdatedValue =
                            (othercharge5.setScale(2).multiply(netwt.setScale(3))
                                .setScale(2, RoundingMode.HALF_UP).toString())
                    }
                }
                // Log.v("totalother",totalOtherChrgsUpdatedValue)
            }
            "fix" -> {
                Log.v("fix", selectedOtherID)
                val netwt: BigDecimal = netweightUpdatedValue.toBigDecimal()
                when (listPos) {
                    // other1
                    0 -> {
                        otherChrgs1UpdatedValue = addOtherChargeList.get(listPos).amount
                        otherChrgs1CalculatedUpdatedValue = otherChrgs1UpdatedValue
                    }
                    1 -> {
                        otherChrgs2UpdatedValue = addOtherChargeList.get(listPos).amount
                        otherChrgs2CalculatedUpdatedValue = otherChrgs2UpdatedValue
                    }
                    2 -> {
                        otherChrgs3UpdatedValue = addOtherChargeList.get(listPos).amount
                        otherChrgs3CalculatedUpdatedValue = otherChrgs3UpdatedValue
                    }
                    3 -> {
                        otherChrgs4UpdatedValue = addOtherChargeList.get(listPos).amount
                        otherChrgs4CalculatedUpdatedValue = otherChrgs4UpdatedValue
                    }
                    4 -> {
                        otherChrgs5UpdatedValue = addOtherChargeList.get(listPos).amount
                        otherChrgs5CalculatedUpdatedValue = otherChrgs5UpdatedValue
                    }
                }
            }
        }

    }

    fun getItemStamp(selectedItemID: String) {
        if (NetworkUtils.isConnected()) {
            viewModel.getItemStamp(
                loginModel?.data?.bearer_access_token,
                selectedItemID
            ).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            /*Log.v("..setupObservers..", "..Success...")*/
                            if (it.data?.status == true) {

                                stampList = it.data.data!!.stamp
                                stampNameList = stampList?.map { it.stamp_name }
                                //  CommonUtils.hideKeyboard(this,txtItemNameAddItem)

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

                        }
                        Status.ERROR -> {
                            /*Toast.makeText(this, it.data?.errormessage?.message, Toast.LENGTH_LONG)
                                .show()*/
                            /* Log.v("..setupObservers..", "..ERROR...")*/
                        }
                        Status.LOADING -> {
                            /*Log.v("..setupObservers..", "..LOADING...")*/
                        }
                    }
                }
            })
        }

    }

    private fun convertValues() {
        when (item_maintain_stock_in_name) {
            "Kilograms" -> {
                /*    convertedGrosswt =
                        CommonUtils.kgTogms(binding.txtGrossWtAddItem.text.toString())
                    convertedNetwt = CommonUtils.kgTogms(binding.txtNetWtAddItem.text.toString())
                    convertedFinewt = CommonUtils.kgTogms(binding.txtFineWtAddItem.text.toString())
                    Log.v("kg", "kgtogm")*/
                convertedGrosswt = binding.txtGrossWtAddItem.text.toString()
                convertedNetwt = binding.txtNetWtAddItem.text.toString()
                convertedFinewt = binding.txtFineWtAddItem.text.toString()
                Log.v("gms", "gm")
            }
            "Carat" -> {
                /*  convertedGrosswt =
                      CommonUtils.carrotTogm(binding.txtGrossWtAddItem.text.toString())
                  convertedNetwt = CommonUtils.carrotTogm(binding.txtNetWtAddItem.text.toString())
                  convertedFinewt =
                      CommonUtils.carrotTogm(binding.txtFineWtAddItem.text.toString())
                  Log.v("carrot", "carrotTogm")*/

                convertedGrosswt = binding.txtGrossWtAddItem.text.toString()
                convertedNetwt = binding.txtNetWtAddItem.text.toString()
                convertedFinewt = binding.txtFineWtAddItem.text.toString()
                Log.v("gms", "gm")
            }
            "Grams" -> {
                convertedGrosswt = binding.txtGrossWtAddItem.text.toString()
                convertedNetwt = binding.txtNetWtAddItem.text.toString()
                convertedFinewt = binding.txtFineWtAddItem.text.toString()
                Log.v("gms", "gm")
            }
        }
    }


    fun saveOpeningStockItemModel() {

        if (prefs.contains(Constants.PREF_OPENINGSTOCK_INFO_KEY)) {
            val collectionType = object :
                TypeToken<java.util.ArrayList<OpeningStockItemModel.OpeningStockItemModelItem>>() {}.type
            var openingStockItemList: ArrayList<OpeningStockItemModel.OpeningStockItemModelItem> =
                Gson().fromJson(prefs[Constants.PREF_OPENINGSTOCK_INFO_KEY, ""], collectionType)
            addopeningStockItemList.addAll(openingStockItemList)
        } else {
            addopeningStockItemList = ArrayList()
        }


        if (prefs.contains(Constants.PREF_LESS_WEIGHT_BREAKUP_INFO_KEY)) {

            val lessWeightBreakup =
                object :
                    TypeToken<OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup>() {}.type
            lessweightBreakupList = Gson().fromJson(
                prefs[Constants.PREF_LESS_WEIGHT_BREAKUP_INFO_KEY, ""],
                lessWeightBreakup
            )
        } else {
            lessweightBreakupList =
                OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup(
                    "", arrayListOf(), "", ""
                )
        }

        if (prefs.contains(Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY)) {
            val makingChargeBreakup =
                object :
                    TypeToken<OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup>() {}.type
            makingChargeBreakupList = Gson().fromJson(
                prefs[Constants.PREF_MAKING_CHARGES_BREAKUP_INFO_KEY, ""],
                makingChargeBreakup
            )
        } else {
            var making_charge_array =
                OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup.MakingChargeArray(
                    "", "", ""
                )
            makingChargeBreakupList =
                OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup(
                    arrayListOf(), making_charge_array, ""
                )
        }

        convertValues()
        val childModel = OpeningStockItemModel.OpeningStockItemModelItem(
            selectedItemID,
            selectedStampId,
            useColor,
            selectedColorId,
            binding.txtUnitAddItem.text.toString(),
            binding.txtSizeAddItem.text.toString(),
            convertedGrosswt,
            lessweightBreakupList,
            convertedNetwt,
            binding.txtTouchAddItem.text.toString(),
            binding.txtWastageAddItem.text.toString(),
            convertedFinewt,
            binding.txtGoldRateAddItem.text.toString(),
            makingChargeBreakupList,
            binding.txtTotalAmountAddItem.text.toString(),
            binding.txtRemarkAddItem.text.toString(),
            is_studed,
            item_maintain_stock_in_id,
            item_maintain_stock_in_name,
            item_metal_type_id,
            item_metal_type_name,
            item_unit_id,
            item_unit_name,
            item_use_stamp
        )

        when (is_From_New_Opening) {
            true -> {
                if (new_openingStock_pos >= 0 && new_openingStock_pos != -1) {
                    // Update selected item
                    addopeningStockItemList.set(new_openingStock_pos, childModel)
                } else {
                    // Add new item
                    addopeningStockItemList.add(childModel)
                }
            }
            false -> {
                if (opneningStock_Item_Position >= 0 && opneningStock_Item_Position != -1) {
                    // Update selected item
                    addopeningStockItemList.set(opneningStock_Item_Position, childModel)
                } else {
                    // Add new item
                    addopeningStockItemList.add(childModel)
                }
            }
        }


        val prefs = PreferenceHelper.defaultPrefs(this)
        prefs[Constants.PREF_OPENINGSTOCK_INFO_KEY] = Gson().toJson(addopeningStockItemList)

    }


}