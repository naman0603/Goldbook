package com.goldbookapp.ui.activity.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityAddTaxAnalysisBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.viewmodel.TaxAnalysisViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.goldbookapp.utils.Status
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.math.BigDecimal
import java.math.RoundingMode

class AddTaxAnalysisActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddTaxAnalysisBinding
    private lateinit var viewModel: TaxAnalysisViewModel
    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    var selectedItemId: String = ""
    var selectedLedgerId: String = ""
    var selectedLedgerName: String = ""

    var gstList: List<ItemGSTMenuModel.Data.GSTMenu>? = null
    var gstNameList: List<String>? = null
    lateinit var popupMenu: PopupMenu
    var selectedGstId: String = ""
    var totalAmtValue: String = ""
    var selectedGstName: String = "0.00"
    var sgstUpdatedValue: String = "0.00"
    var cgstUpdatedValue: String = "0.00"
    var igstUpdatedValue: String = "0.00"
    var toatltaxAmtUpdatedValue: String = "0.00"
    var is_igst_enable: Boolean = false

    lateinit var taxDetailsModel: TaxAnalysisListModel.TaxAnalysisList
    var additemList = ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>()
    var addTaxList =
        ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem.Tax_analysis_array>()
    var transaction_type: String? = ""
    var ledgerSalesNameList: List<String>? = null
    var ledgerPurchaseNameList: List<String>? = null
    var ledgerSalesList: List<SearchLedgerModel.LedgerDetails>? = null
    var ledgerPurchaseList: List<SearchLedgerModel.LedgerDetails>? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_tax_analysis)
        setupViewModel()
        setupUIandListner()
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                TaxAnalysisViewModel::class.java
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
            getLedgerdd(transaction_type!!)

        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }

    }

    private fun setupUIandListner() {
        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        )
        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText("Tax Analysis")

        getDataFromIntent()
        imgLeft.clickWithDebounce {
            onBackPressed()
        }

        binding.txtLedgerTaxAnalysis.clickWithDebounce {
            when (transaction_type) {
                "sales" -> {
                    openSalesLedgerMenu(ledgerSalesNameList)
                }
                "purchase" -> {
                    openPurchaseLedgerMenu(ledgerPurchaseNameList)
                }
            }
        }

        binding.txtgstRateTaxAnalysis.clickWithDebounce {
            openGstMenu()
        }

        binding.btnSaveCloseAddTaxAnalysis.clickWithDebounce {
            saveTaxAnalysis()
            finish()
        }
    }


    private fun getDataFromIntent() {
        if (intent.extras != null) {
            if (intent.extras?.containsKey(Constants.TAX_ANALYSIS_MODEL)!!) {

                var group_str: String? = intent.getStringExtra(Constants.TAX_ANALYSIS_MODEL)
                taxDetailsModel =
                    Gson().fromJson(
                        group_str,
                        TaxAnalysisListModel.TaxAnalysisList::class.java
                    )

                selectedItemId = taxDetailsModel.item_id.toString()
                selectedLedgerId = taxDetailsModel.ledger_id.toString()
                selectedLedgerName = taxDetailsModel.ledger_name.toString()
                selectedGstId = taxDetailsModel.gst_rate.toString()
                binding.txtItemNameTaxAnalysis.setText(taxDetailsModel.item_name)
                binding.txtLedgerTaxAnalysis.setText(taxDetailsModel.ledger_name)
                totalAmtValue = taxDetailsModel.taxable_amount.toString()
                binding.txttaxableAmtTaxAnalysis.setText(taxDetailsModel.taxable_amount)
                binding.txthsnTaxAnalysis.setText(taxDetailsModel.hsn)
                selectedGstName = taxDetailsModel.gst_rate_percentage.toString()
                binding.txtgstRateTaxAnalysis.setText(taxDetailsModel.gst_rate_percentage)
                sgstUpdatedValue = taxDetailsModel.sgst_amount.toString()
                cgstUpdatedValue = taxDetailsModel.cgst_amount.toString()
                igstUpdatedValue = taxDetailsModel.igst_amount.toString()
                binding.txtigstAmtTaxAnalysis.setText(taxDetailsModel.igst_amount)
                binding.txtcgstAmtTaxAnalysis.setText(taxDetailsModel.cgst_amount)
                binding.txtsgstAmtTaxAnalysis.setText(taxDetailsModel.sgst_amount)

                when (igstUpdatedValue.toBigDecimal().compareTo(BigDecimal.ZERO) > 0) {
                    true -> {
                        is_igst_enable = true
                    }else->{

                }
                }
                // default 3% calculate (gst_rate_percentage)
                updateTaxAmt()
            }
            if (intent.extras!!.containsKey(Constants.TRANSACTION_TYPE)) {
                transaction_type = intent.getStringExtra(Constants.TRANSACTION_TYPE)
            }
        }

    }

    private fun saveTaxAnalysis() {

        val childModel =
            OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem.Tax_analysis_array(
                selectedItemId,
                binding.txtItemNameTaxAnalysis.text.toString(),
                selectedLedgerId,
                binding.txtLedgerTaxAnalysis.text.toString(),
                binding.txttaxableAmtTaxAnalysis.text.toString(),
                binding.txthsnTaxAnalysis.text.toString(),
                selectedGstId,
                selectedGstName,
                igstUpdatedValue,
                cgstUpdatedValue,
                sgstUpdatedValue

            )

        if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {

            val collectionType = object :
                TypeToken<ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>>() {}.type
            additemList =
                Gson().fromJson(
                    prefs[Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY, ""],
                    collectionType
                )
            Log.v("additemlist", additemList.size.toString())

            for (i in 0 until additemList.size) {
                if (childModel.item_id.equals(additemList.get(i).item_id, true)) {
                    additemList.get(i).tax_analysis_array = childModel
                }
            }
            prefs[Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY] =
                Gson().toJson(additemList)

        }
        if (prefs.contains(Constants.PREF_SALES_TAX_ANALYSIS_LIST_KEY)) {

            Log.v("datafound", "")
            val taxAnalysisListcollection =
                object :
                    TypeToken<ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem.Tax_analysis_array>>() {}.type
            addTaxList = Gson().fromJson(
                prefs[Constants.PREF_SALES_TAX_ANALYSIS_LIST_KEY, ""],
                taxAnalysisListcollection
            )
            for (i in 0 until addTaxList.size) {
                if (childModel.item_id.equals(addTaxList.get(i).item_id, true)) {
                    addTaxList.set(i, childModel)

                }
            }
            prefs[Constants.PREF_SALES_TAX_ANALYSIS_LIST_KEY] =
                Gson().toJson(addTaxList)

        }

        val prefs = PreferenceHelper.defaultPrefs(this)
        prefs[Constants.PREF_SALES_TAX_ANALYSIS_INFO_KEY] = Gson().toJson(childModel)

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

    private fun openGstMenu() {

        popupMenu = PopupMenu(this, binding.txtgstRateTaxAnalysis)
        for (i in 0 until this.gstNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                this.gstNameList!!.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtgstRateTaxAnalysis.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = this.gstNameList!!.indexOf(selected)

            selectedGstId =
                pos?.let { it1 -> gstList?.get(it1)?.id }.toString()

            selectedGstName = pos?.let { it1 -> gstList?.get(it1)?.name }.toString()

            updateTaxAmt()
            true
        })

        popupMenu.show()
    }

    private fun openSalesLedgerMenu(ledgerSalesNameList: List<String>?) {
        popupMenu = PopupMenu(this, binding.txtLedgerTaxAnalysis)
        for (i in 0 until ledgerSalesNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                ledgerSalesNameList.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtLedgerTaxAnalysis.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = ledgerSalesNameList.indexOf(selected)

            selectedLedgerId =
                pos?.let { it1 -> ledgerSalesList?.get(it1)?.ledger_id }.toString()


            selectedLedgerName = pos?.let { it1 -> ledgerSalesList?.get(it1)?.name }.toString()

            true
        })

        popupMenu.show()
    }

    private fun openPurchaseLedgerMenu(ledgerPurchaseNameList: List<String>?) {
        popupMenu = PopupMenu(this, binding.txtLedgerTaxAnalysis)
        for (i in 0 until ledgerPurchaseNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                ledgerPurchaseNameList.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtLedgerTaxAnalysis.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = ledgerPurchaseNameList.indexOf(selected)

            selectedLedgerId =
                pos?.let { it1 -> ledgerPurchaseList?.get(it1)?.ledger_id }.toString()

            selectedLedgerName =
                pos?.let { it1 -> ledgerPurchaseList?.get(it1)?.name }.toString()
            true
        })

        popupMenu.show()
    }


    private fun updateTaxAmt() {
        when (is_igst_enable) {
            true -> {
                val totalAmt: BigDecimal = totalAmtValue.toBigDecimal()
                val taxperce: BigDecimal = selectedGstName.toBigDecimal()

                val totaltaxAmt: String =
                    ((totalAmt.setScale(2) * taxperce.setScale(2) / BigDecimal(
                        100
                    ))).setScale(
                        2,
                        RoundingMode.CEILING
                    ).toString()

                igstUpdatedValue = totaltaxAmt
                binding.txtigstAmtTaxAnalysis.setText(igstUpdatedValue)
            }
            false -> {

                val totalAmt: BigDecimal = totalAmtValue.toBigDecimal()
                val taxperce: BigDecimal = selectedGstName.toBigDecimal()

                val totaltaxAmt: String =
                    ((totalAmt.setScale(2) * taxperce.setScale(2) / BigDecimal(
                        100
                    ))).setScale(
                        2,
                        RoundingMode.CEILING
                    ).toString()

                toatltaxAmtUpdatedValue = totaltaxAmt
                Log.v("taxvalue", "" + toatltaxAmtUpdatedValue)

                var totalTaxSgst: BigDecimal = toatltaxAmtUpdatedValue.toBigDecimal()
                val result: String =
                    ((totalTaxSgst.setScale(2)
                        .divide("2".toBigDecimal().setScale(2))
                            )).setScale(2, RoundingMode.CEILING).toString()

                sgstUpdatedValue = result
                cgstUpdatedValue = result
                binding.txtsgstAmtTaxAnalysis.setText(sgstUpdatedValue)
                binding.txtcgstAmtTaxAnalysis.setText(cgstUpdatedValue)
                Log.v("sgdt", "" + sgstUpdatedValue + cgstUpdatedValue)

            }
        }

    }


}