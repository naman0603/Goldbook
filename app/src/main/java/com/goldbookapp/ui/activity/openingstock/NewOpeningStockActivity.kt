package com.goldbookapp.ui.activity.openingstock

import OpeningStockDetailModel
import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityNewOpeningStockBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.additem.AddItemActivity
import com.goldbookapp.ui.activity.viewmodel.NewOpeningStockViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.OpeningStockItemAdapter
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
import okhttp3.MediaType
import okhttp3.RequestBody
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NewOpeningStockActivity : AppCompatActivity() {

    lateinit var binding: ActivityNewOpeningStockBinding
    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    private lateinit var viewModel: NewOpeningStockViewModel
    var is_series: String? = ""
    var isNoGenerated: Boolean = false
    private lateinit var adapter: OpeningStockItemAdapter
    var stockSaveAdd: String? = "0"
    var openingStockCalcItemList =
        ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>()

    var openingStockListModel = ArrayList<OpeningStockItemModel.OpeningStockItemModelItem>()
    lateinit var lessWeightModel: OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup
    lateinit var chargeModel: OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup
    lateinit var openingStockDetailsModel: OpeningStockDetailModel.OpeningStock
    var is_From_Edit: Boolean = false
    var transaction_id: String = ""
    lateinit var fiscalYearModel: FiscalYearModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_opening_stock)
        val view = binding.root
        setupViewModel()
        setupUIandListner()

    }

    private fun setupUIandListner() {
        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        )

        fiscalYearModel =
            Gson().fromJson(prefs[Constants.FiscalYear, ""], FiscalYearModel::class.java)

        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.new_opening_stock)

        val sdf = SimpleDateFormat("dd-MMM-yy")
        val currentDate = sdf.format(Date())
        binding.txtDateOPS.setText(currentDate)

        binding.rvNewopeningStockItem.layoutManager = LinearLayoutManager(this)
        adapter = OpeningStockItemAdapter(arrayListOf(), true)
        binding.rvNewopeningStockItem.adapter = adapter

        when (is_From_Edit) {
            false -> {
                clearPref()
            }
            else -> {

            }
        }

        getDataFromIntent()

        binding.cardAddItemOPS.clickWithDebounce {
            when (is_From_Edit) {
                true -> {

                    startActivity(
                        Intent(
                            this,
                            AddItemActivity::class.java
                        ).putExtra(Constants.IS_FROM_NEW_OPENING_STOCK, false)
                            .putExtra(
                                Constants.TRANSACTION_TYPE,
                                "opening_stock"
                            )
                    )
                }
                false -> {

                    startActivity(
                        Intent(
                            this,
                            AddItemActivity::class.java
                        ).putExtra(Constants.IS_FROM_NEW_OPENING_STOCK, true)
                            .putExtra(
                                Constants.TRANSACTION_TYPE,
                                "opening_stock"
                            )
                    )
                }
            }

        }

        binding.txtDateOPS.clickWithDebounce {
            openDatePicker(true)
        }


        binding.btnSaveAddOpeningStock.clickWithDebounce {
            when (is_From_Edit) {
                true -> {
                    setRequestBodyParamCallEditAPI()
                }
                false -> {
                    setRequestBodyParamCallAPI()
                }
            }
        }

    }

    private fun setRequestBodyParamCallEditAPI() {

        if (prefs.contains(Constants.PREF_OPENINGSTOCK_INFO_KEY)) {
            val collectionType =
                object :
                    TypeToken<ArrayList<OpeningStockItemModel.OpeningStockItemModelItem>>() {}.type
            var itemList: ArrayList<OpeningStockItemModel.OpeningStockItemModelItem> =
                Gson().fromJson(
                    prefs[Constants.PREF_OPENINGSTOCK_INFO_KEY, ""],
                    collectionType
                )

            val itemadded: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                Gson().toJson(itemList)
            )

            val transaction_id: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                transaction_id
            )


            val transaction_date: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.txtDateOPS.text.toString()
            )

            val invoice_number: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.txtVoucherNoOPS.text.toString()
            )

            val remarks: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.txtRemarkOPS.text.toString()
            )

            val reference: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.txtReferenceNoOPS.text.toString()
            )

            editOpeningStock(
                loginModel?.data?.bearer_access_token,
                transaction_id,
                transaction_date,
                invoice_number,
                itemadded,
                remarks,
                reference
            )

        }
    }

    private fun getDataFromIntent() {

        if (intent.extras != null) {
            if (intent.extras?.containsKey(Constants.OPENING_STOCK_DETAIL_KEY)!!) {

                var group_str: String? = intent.getStringExtra(Constants.OPENING_STOCK_DETAIL_KEY)
                openingStockDetailsModel =
                    Gson().fromJson(
                        group_str,
                        OpeningStockDetailModel.OpeningStock::class.java
                    )
                tvTitle.setText(R.string.edit_opening_stock)
                // binding.tvAddItemOS.setText("Edit Item")
                is_From_Edit = true

                transaction_id = openingStockDetailsModel.transaction_id!!
                binding.txtVoucherNoOPS.setText(openingStockDetailsModel.invoice_number)
                binding.txtDateOPS.setText(openingStockDetailsModel.transaction_date)
                binding.txtReferenceNoOPS.setText(openingStockDetailsModel.reference)
                binding.txtRemarkOPS.setText(openingStockDetailsModel.remarks)

                openingStockCalcItemList = openingStockDetailsModel.item!!
                /* prefs[Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY] =
                     Gson().toJson(openingStockCalcItemList)*/

                addItemArrayInPref(openingStockDetailsModel)

                adapter.apply {
                    addpurchasebillrow_item(openingStockCalcItemList)
                    notifyDataSetChanged()
                }
                val itemCalcadded: RequestBody = RequestBody.create(
                    MediaType.parse("text/plain"),
                    Gson().toJson(openingStockCalcItemList)
                )
                openingStockCalculateAPI(loginModel?.data?.bearer_access_token, itemCalcadded)
                //  getOpeningStockCalItem()
            }
        }

    }

    private fun addItemArrayInPref(openingStockDetailsModel: OpeningStockDetailModel.OpeningStock) {

        for (i in 0 until openingStockDetailsModel.item!!.size!!) {
            lessWeightModel = OpeningStockItemModel.OpeningStockItemModelItem.ItemWtBreakup(
                "",
                openingStockCalcItemList.get(i).item_wt_breakup.less_wt_array,
                openingStockCalcItemList.get(i).item_wt_breakup.total_less_wt,
                openingStockCalcItemList.get(i).item_wt_breakup.total_less_wt_amount
            )

            chargeModel = OpeningStockItemModel.OpeningStockItemModelItem.ItemChargesBreakup(
                openingStockCalcItemList.get(i).item_charges_breakup.charges_array,
                /*""*/
                openingStockCalcItemList.get(i).item_charges_breakup.making_charge_array,
                openingStockCalcItemList.get(i).item_charges_breakup.total_charges
            )


            var openingStockModel = OpeningStockItemModel.OpeningStockItemModelItem(
                openingStockCalcItemList.get(i).item_id,
                openingStockCalcItemList.get(i).item_stamp_id,
                openingStockCalcItemList.get(i).item_use_gold_color,
                openingStockCalcItemList.get(i).item_gold_color_id ?: "",
                openingStockCalcItemList.get(i).item_quantity,
                openingStockCalcItemList.get(i).item_size,
                openingStockCalcItemList.get(i).item_gross_wt,
                lessWeightModel,
                openingStockCalcItemList.get(i).item_net_wt,
                openingStockCalcItemList.get(i).item_touch,
                openingStockCalcItemList.get(i).item_wastage,
                openingStockCalcItemList.get(i).item_fine_wt,
                openingStockCalcItemList.get(i).item_rate,
                chargeModel,
                openingStockCalcItemList.get(i).item_total,
                openingStockCalcItemList.get(i).item_remarks,
                openingStockCalcItemList.get(i).item_is_studded ?: "",
                openingStockCalcItemList.get(i).item_maintain_stock_in_id,
                openingStockCalcItemList.get(i).item_maintain_stock_in_name,
                openingStockCalcItemList.get(i).item_metal_type_id,
                openingStockCalcItemList.get(i).item_metal_type_name,
                openingStockCalcItemList.get(i).item_unit_id,
                openingStockCalcItemList.get(i).item_unit_name,
                openingStockCalcItemList.get(i).item_use_stamp
            )
            openingStockListModel.add(openingStockModel)


        }
        prefs[Constants.PREF_OPENINGSTOCK_INFO_KEY] = Gson().toJson(openingStockListModel)
        prefs[Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY] = Gson().toJson(openingStockCalcItemList)

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
        if (prefs.contains(Constants.PREF_OPENINGSTOCK_INFO_KEY)) {
            prefs.edit().remove(Constants.PREF_OPENINGSTOCK_INFO_KEY).apply()
        }
        if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {
            prefs.edit().remove(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY).apply()
        }
    }

    private fun setRequestBodyParamCallAPI() {
        if (prefs.contains(Constants.PREF_OPENINGSTOCK_INFO_KEY)) {
            val collectionType =
                object :
                    TypeToken<ArrayList<OpeningStockItemModel.OpeningStockItemModelItem>>() {}.type
            var itemList: ArrayList<OpeningStockItemModel.OpeningStockItemModelItem> =
                Gson().fromJson(
                    prefs[Constants.PREF_OPENINGSTOCK_INFO_KEY, ""],
                    collectionType
                )

            val itemadded: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                Gson().toJson(itemList)
            )

            val transaction_date: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.txtDateOPS.text.toString()
            )

            val invoice_number: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                is_series.toString().trim()
            )

            val remarks: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.txtRemarkOPS.text.toString()
            )

            val reference: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.txtReferenceNoOPS.text.toString()
            )

            addOpeningStock(
                loginModel?.data?.bearer_access_token,
                transaction_date,
                invoice_number,
                itemadded,
                remarks,
                reference
            )


        } else {

        }
    }


    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                NewOpeningStockViewModel::class.java
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
            when (is_From_Edit) {
                false -> {
                    //clearPref()
                    when (isNoGenerated) {
                        false -> getOpeningStockVoucherNoFromApi()
                        else -> {

                        }
                    }
                }
                else -> {

                }
            }

            getOpeningStockCalItem()

        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun getOpeningStockCalItem() {
        if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {

            val collectionType = object :
                TypeToken<ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>>() {}.type
            openingStockCalcItemList =
                Gson().fromJson(
                    prefs[Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY, ""],
                    collectionType
                )

            adapter.apply {
                addpurchasebillrow_item(openingStockCalcItemList)
                notifyDataSetChanged()
            }
        }
        openingStockCalculationAPI()
    }

    private fun openingStockCalculationAPI() {
        if (prefs.contains(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY)) {

            val collectionType =
                object :
                    TypeToken<ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem>>() {}.type
            var itemCalcList: ArrayList<OpeningStockItemCalculationModel.OpeningStockItemCalcModelItem> =
                Gson().fromJson(
                    prefs[Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY, ""],
                    collectionType
                )

            val itemCalcadded: RequestBody = RequestBody.create(
                MediaType.parse("text/plain"),
                Gson().toJson(itemCalcList)
            )

            openingStockCalculateAPI(
                loginModel?.data?.bearer_access_token,
                itemCalcadded
            )

        }
    }


    fun openDatePicker(isFromDate: Boolean) {
        val c = Calendar.getInstance()
        if (isFromDate) {
            val sdf = SimpleDateFormat("dd-MMM-yy")
            val parse = sdf.parse(binding.txtDateOPS.text.toString())
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
                    binding.txtDateOPS.setText(
                        "" + String.format(
                            "%02d",
                            dayOfMonth
                        ) + "-" + SimpleDateFormat("MMM").format(c.time) + "-" + year.toString()
                            .substring(2, 4)
                    )
                    when(is_From_Edit){
                        false->{
                            getOpeningStockVoucherNoFromApi()
                        }else->{

                        }
                    }


                } else {
                }
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


    fun openingStockCalculateAPI(
        token: String?,
        item_json: RequestBody?
    ) {
        if (NetworkUtils.isConnected()) {
            viewModel.openingStockCalculate(
                token,
                item_json
            ).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {

                                fill_item_details_data(it.data.data.item_json)

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
    }

    private fun fill_item_details_data(openingStockModel: OpeningStockCalcModel.Data.ItemJson) {

        binding.tvNewopeningStockGrossWt.setText(openingStockModel.total_gross_wt)
        binding.tvNewopeningStockLessWt.setText(openingStockModel.total_less_wt)
        binding.tvNewopeningStockNetWt.setText(openingStockModel.total_net_wt)
        binding.tvNewopeningStockFineWt.setText(openingStockModel.total_fine_wt)

        binding.llNewopeningStockItemdetailHeading.visibility = View.VISIBLE
        binding.llNewopeningStockMetalweights.visibility = View.VISIBLE


        binding.v1NewopeningStock.visibility = View.VISIBLE
        binding.v2NewopeningStock.visibility = View.VISIBLE


        binding.linearCalculationViewOpeningStock.visibility = View.VISIBLE
    }

    fun getOpeningStockVoucherNoFromApi() {
        viewModel.getOpeningStockVoucherNoFromApi(
            loginModel.data?.bearer_access_token,
            binding.txtDateOPS.text.toString(),
            transaction_id
        ).observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {

                        if (it.data?.status == true) {
                            isNoGenerated = true
                            binding.txtVoucherNoOPS.setText(/*it.data.data?.prefix+ '-'+ */it.data.data?.series /*+ '-' + it.data.data?.suffix*/)

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

                    }
                    Status.LOADING -> {

                    }
                }
            }
        })
    }


    fun addOpeningStock(
        token: String?,
        transaction_date: RequestBody?,
        invoice_number: RequestBody?,
        item_json: RequestBody?,
        remarks: RequestBody?,
        reference: RequestBody?

    ) {

        viewModel.addOpeningStock(
            token, transaction_date, invoice_number, item_json, remarks,
            reference

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


    fun editOpeningStock(
        token: String?,
        transaction_id: RequestBody?,
        transaction_date: RequestBody?,
        invoice_number: RequestBody?,
        item_json: RequestBody?,
        remarks: RequestBody?,
        reference: RequestBody?

    ) {

        viewModel.editOpeningStock(
            token, transaction_id, transaction_date, invoice_number, item_json, remarks,
            reference

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


    fun removeItem(index: Int) {
        if (CommonUtils.isValidClickPressed()) {
            if (openingStockCalcItemList != null && openingStockCalcItemList.size > 0) {
                if (index >= openingStockCalcItemList.size) {
                    //index not exists
                } else {
                    // index exists
                    openingStockCalcItemList.removeAt(index)
                    adapter.apply {
                        addpurchasebillrow_item(openingStockCalcItemList)
                        notifyDataSetChanged()
                    }

                    if (openingStockCalcItemList.size > 0) {
                        prefs[Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY] =
                            Gson().toJson(openingStockCalcItemList)
                        openingStockCalculationAPI()
                    } else {
                        binding.linearCalculationViewOpeningStock.visibility = View.GONE
                        /*prefs.edit().remove(Constants.PREF_OPENINGSTOCK_CALC_INFO_KEY).apply()
                        binding.linearCalculationViewOpeningStock.visibility = View.GONE*/
                    }
                }
            }
        }

    }

}