package com.goldbookapp.ui.activity.item

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityInventoryInfoBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.viewmodel.NewItemViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.PreferedColorsAdapter
import com.goldbookapp.ui.adapter.PreferedVendorsAdapter
import com.goldbookapp.ui.adapter.SelectedColorAdapter
import com.goldbookapp.ui.adapter.SelectedVendorsAdapter
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_inventory_info.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.text.DecimalFormat
import java.util.*

class InventoryInfoActivity : AppCompatActivity() {
    private var isLoadedOnce: Boolean = false

    lateinit var binding: ActivityInventoryInfoBinding
    private lateinit var viewModel: NewItemViewModel
    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    lateinit var popupMenu: PopupMenu
    var selectedStamp: String = "0"
    var selectedStockMethod: String = ""
    var isUseColor: String = "0"


    private lateinit var selectedVendorAdapter: SelectedVendorsAdapter
    private lateinit var selectedColorAdapter: SelectedColorAdapter
    var selectedchbVList: ArrayList<Boolean> = arrayListOf()
    var selectedchbCList: ArrayList<Boolean> = arrayListOf()

    var selectedVendorCount: Int = 0
    var selectedVendorsList: ArrayList<String> = arrayListOf()
    var multipleVendorList = ArrayList<MultipleVendorNewItemModel.PrefVendorList>()
    var multipleVendorIdnNameList = ArrayList<ItemDetailModel.Data.Item.ItemPrefVendor>()

    var selectedColorCount: Int = 0
    var selectedColorsList: ArrayList<String> = arrayListOf()
    var multipleColorList = ArrayList<MultipleColorNewItemModel.PrefColorList>()
    var multipleColorIdnNameList = ArrayList<ItemDetailModel.Data.Item.ItemPrefColor>()

    var selectedVendorsID: ArrayList<String> = arrayListOf()
    var selectedColorsID: ArrayList<String> = arrayListOf()

    var vendorList: List<ItemVendorModel.Data427691210>? = null
    var vendorNameList: List<String>? = null

    var colorNameList: List<String>? = null
    private lateinit var metalColourList: List<MetalColourModel.DataMetalColour>

    lateinit var vendorNameAdapter: PreferedVendorsAdapter
    lateinit var colorNameAdapter: PreferedColorsAdapter

    lateinit var addInventoryInfo: AddInventoryInfoModel

    lateinit var minStockGmsUpdatedValue: String
    lateinit var minStockPcsUpdatedValue: String
    lateinit var maxStockGmsUpdatedValue: String
    lateinit var maxStockPcsUpdatedValue: String
    lateinit var productwtUpdatedValue: String
    lateinit var itemrateUpdatedValue: String

    lateinit var item_preferred_vendor: List<ItemDetailModel.Data.Item.ItemPrefVendor>
    lateinit var item_preferred_color: List<ItemDetailModel.Data.Item.ItemPrefColor>

    var is_from_edit: Boolean = false
    var selectedMetalType: String? = "0"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_inventory_info)
        setupViewModel()
        setupUIandListner()

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
                    getItemVendors()
                    getMetalColour()
                }
                else->{

                }
            }
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }


    private fun setupUIandListner() {

        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.inventory_info)

        imgLeft?.clickWithDebounce {
            onBackPressed()
        }

        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        )

        if (intent.extras != null && intent.extras!!.containsKey(Constants.IS_FOR_EDIT)) {
            selectedMetalType = intent.getStringExtra(Constants.NEWITEM_METAL_TYPE_KEY)
            is_from_edit = intent.getBooleanExtra(Constants.IS_FOR_EDIT, false)
            // setEditData()
            Log.v("metal type",""+selectedMetalType)
        }


        when(selectedMetalType.equals("Other")){
            true->{
                binding.tvProductWtNewItem.visibility = View.VISIBLE
                binding.tvItemRateNewItem.visibility = View.VISIBLE
            }
            false->{
                binding.tvProductWtNewItem.visibility = View.GONE
                binding.tvItemRateNewItem.visibility = View.GONE
            }
        }
        when(selectedMetalType.equals("Silver")){
            true->{
                binding.tvUseColorNewItem.visibility= View.GONE
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                )
                binding.tvStampNewItem.setLayoutParams(params)
            }
            false->{
                binding.tvUseColorNewItem.visibility= View.VISIBLE
            }
        }

        binding.rvVendorsList.layoutManager = LinearLayoutManager(this)
        selectedVendorAdapter = SelectedVendorsAdapter(this, selectedVendorsList)
        binding.rvVendorsList.adapter = selectedVendorAdapter


        binding.rvGoldColorList.layoutManager = LinearLayoutManager(this)
        selectedColorAdapter = SelectedColorAdapter(this, selectedColorsList)
        binding.rvGoldColorList.adapter = selectedColorAdapter

        applyingDigitFilter()
        //add Item
        if (!is_from_edit) {
            setDefaultData()
        }

        binding.txtUseColorNewItem.clickWithDebounce {
            openUseGoldColorMenu()
        }



        binding.txtStampNewItem.clickWithDebounce {
            openStampMenu()
        }

        binding.txtStockMethodNewItem.clickWithDebounce {
            openStockMethodMenu()
        }

        val df1 = DecimalFormat("0.000")
        val df = DecimalFormat("0.00")

        binding.txtStockMinGmsNewItem.doAfterTextChanged {
            val str: String = binding.txtStockMinGmsNewItem.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtStockMinGmsNewItem.setText(str2)
                binding.txtStockMinGmsNewItem.setSelection(str2.length)
            }

            minStockGmsUpdatedValue = df1.format(str2.toDouble())
        }


        binding.txtStockMinGmsNewItem.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (this::minStockGmsUpdatedValue.isInitialized) {
                    when (binding.txtStockMinGmsNewItem.text.isNullOrBlank()) {
                        true -> {
                            minStockGmsUpdatedValue = "0.000"
                            binding.txtStockMinGmsNewItem.setText(minStockGmsUpdatedValue)
                            binding.txtStockMinGmsNewItem.setSelection(minStockGmsUpdatedValue.length)

                        }
                        else -> {
                            binding.txtStockMinGmsNewItem.setText(minStockGmsUpdatedValue)
                            binding.txtStockMinGmsNewItem.setSelection(minStockGmsUpdatedValue.length)
                        }
                    }
                }
            }
        }


        binding.txtStockMinPcsNewItem.doAfterTextChanged {
            val str: String = binding.txtStockMinPcsNewItem.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtStockMinPcsNewItem.setText(str2)
                binding.txtStockMinPcsNewItem.setSelection(str2.length)
            }

            minStockPcsUpdatedValue = df1.format(str2.toDouble())
        }


        binding.txtStockMinPcsNewItem.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (this::minStockPcsUpdatedValue.isInitialized) {
                    when (binding.txtStockMinPcsNewItem.text.isNullOrBlank()) {
                        true -> {
                            minStockPcsUpdatedValue = "0.000"
                            binding.txtStockMinPcsNewItem.setText(minStockPcsUpdatedValue)
                            binding.txtStockMinPcsNewItem.setSelection(minStockPcsUpdatedValue.length)

                        }
                        else -> {
                            binding.txtStockMinPcsNewItem.setText(minStockPcsUpdatedValue)
                            binding.txtStockMinPcsNewItem.setSelection(minStockPcsUpdatedValue.length)
                        }
                    }
                }
            }
        }


        binding.txtStockMaxGmsNewItem.doAfterTextChanged {
            val str: String = binding.txtStockMaxGmsNewItem.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtStockMaxGmsNewItem.setText(str2)
                binding.txtStockMaxGmsNewItem.setSelection(str2.length)
            }

            maxStockGmsUpdatedValue = df1.format(str2.toDouble())
        }


        binding.txtStockMaxGmsNewItem.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (this::maxStockGmsUpdatedValue.isInitialized) {
                    when (binding.txtStockMaxGmsNewItem.text.isNullOrBlank()) {
                        true -> {
                            maxStockGmsUpdatedValue = "0.000"
                            binding.txtStockMaxGmsNewItem.setText(maxStockGmsUpdatedValue)
                            binding.txtStockMaxGmsNewItem.setSelection(maxStockGmsUpdatedValue.length)

                        }
                        else -> {
                            binding.txtStockMaxGmsNewItem.setText(maxStockGmsUpdatedValue)
                            binding.txtStockMaxGmsNewItem.setSelection(maxStockGmsUpdatedValue.length)
                        }
                    }
                }
            }
        }

        binding.txtStockMaxPcsNewItem.doAfterTextChanged {
            val str: String = binding.txtStockMaxPcsNewItem.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtStockMaxPcsNewItem.setText(str2)
                binding.txtStockMaxPcsNewItem.setSelection(str2.length)
            }

            maxStockPcsUpdatedValue = df1.format(str2.toDouble())
        }


        binding.txtStockMaxPcsNewItem.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (this::maxStockPcsUpdatedValue.isInitialized) {
                    when (binding.txtStockMaxPcsNewItem.text.isNullOrBlank()) {
                        true -> {
                            maxStockPcsUpdatedValue = "0.000"
                            binding.txtStockMaxPcsNewItem.setText(maxStockPcsUpdatedValue)
                            binding.txtStockMaxPcsNewItem.setSelection(maxStockPcsUpdatedValue.length)

                        }
                        else -> {
                            binding.txtStockMaxPcsNewItem.setText(maxStockPcsUpdatedValue)
                            binding.txtStockMaxPcsNewItem.setSelection(maxStockPcsUpdatedValue.length)
                        }
                    }
                }
            }
        }

        binding.txtProductWtNewItem.doAfterTextChanged {
            val str: String = binding.txtProductWtNewItem.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 9, 3).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtProductWtNewItem.setText(str2)
                binding.txtProductWtNewItem.setSelection(str2.length)
            }

            productwtUpdatedValue = df1.format(str2.toDouble())
        }


        binding.txtProductWtNewItem.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (this::productwtUpdatedValue.isInitialized) {
                    when (binding.txtProductWtNewItem.text.isNullOrBlank()) {
                        true -> {
                            productwtUpdatedValue = "0.000"
                            binding.txtProductWtNewItem.setText(productwtUpdatedValue)
                            binding.txtProductWtNewItem.setSelection(productwtUpdatedValue.length)

                        }
                        else -> {
                            binding.txtProductWtNewItem.setText(productwtUpdatedValue)
                            binding.txtProductWtNewItem.setSelection(productwtUpdatedValue.length)
                        }
                    }
                }
            }
        }

        binding.txtItemRateNewItem.doAfterTextChanged {
            val str: String = binding.txtItemRateNewItem.text.toString()

            if (str.isEmpty()) return@doAfterTextChanged
            val str2: String = CommonUtils.perfectDecimal(str, 10, 2).toString()
            if (!str2.equals(str)) {
                //val str3:String = df.format(str2.toDouble())
                binding.txtItemRateNewItem.setText(str2)
                binding.txtItemRateNewItem.setSelection(str2.length)
            }

            itemrateUpdatedValue = df.format(str2.toDouble())
        }


        binding.txtItemRateNewItem.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (this::itemrateUpdatedValue.isInitialized) {
                    when (binding.txtItemRateNewItem.text.isNullOrBlank()) {
                        true -> {
                            itemrateUpdatedValue = "0.00"
                            binding.txtItemRateNewItem.setText(itemrateUpdatedValue)
                            binding.txtItemRateNewItem.setSelection(itemrateUpdatedValue.length)

                        }
                        else -> {
                            binding.txtItemRateNewItem.setText(itemrateUpdatedValue)
                            binding.txtItemRateNewItem.setSelection(itemrateUpdatedValue.length)
                        }
                    }
                }
            }
        }


        btnSaveInventoryInfoNewItem.clickWithDebounce {
            if (performValidation()) {
                if (NetworkUtils.isConnected()) {
                    saveInventoryInfoModel()
                    finish()
                }
            }
        }
    }

    fun performValidation(): Boolean {
        if ((selectedStockMethod.equals("Tag")) && txtTagPrefixNewItem.text.toString().isBlank()) {
            CommonUtils.showDialog(
                this,
                getString(R.string.tag_prefix_msg)
            )
            txtTagPrefixNewItem.requestFocus()
            return false

        } else if ((selectedStockMethod.equals("Both")) && txtTagPrefixNewItem.text.toString().isBlank()) {
            CommonUtils.showDialog(
                this,
                getString(R.string.tag_prefix_msg)
            )
            txtTagPrefixNewItem.requestFocus()
            return false
        }
        return true
    }

    private fun setDefaultData() {
        binding.txtUseColorNewItem.setText("No")
        isUseColor = "0"
        binding.txtStampNewItem.setText("Yes")
        selectedStamp = "1"
        binding.txtStockMethodNewItem.setText("Loose")
        selectedStockMethod = "Loose"
    }

    /* private fun setEditData() {
         when (is_from_edit) {
             true -> {
                 getDataFromPrefInventory()
             }
         }
     }*/

    private fun applyingDigitFilter() {
        // applying filters to edit input number decimal fields(which have 2 or 3 decimal after .)
        binding.txtStockMinGmsNewItem.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )

        binding.txtStockMinPcsNewItem.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )

        binding.txtStockMaxGmsNewItem.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )

        binding.txtStockMaxPcsNewItem.setFilters(
            arrayOf<InputFilter>(
                DigitsInputFilter(
                    9,
                    3,
                    999999999.999
                )
            )
        )
    }


    private fun getDataFromPrefInventory() {
        when (prefs.contains(Constants.PREF_INVENTORY_INFO_KEY)) {
            true -> {

                val inventoryInfoDetails =
                    object :
                        TypeToken<AddInventoryInfoModel>() {}.type
                addInventoryInfo = Gson().fromJson(
                    prefs[Constants.PREF_INVENTORY_INFO_KEY, ""],
                    inventoryInfoDetails
                )
                binding.txtStockMethodNewItem.setText(addInventoryInfo.stockMethod)
                binding.txtTagPrefixNewItem.setText(addInventoryInfo.tagInventoryInfo)
                selectedStamp = addInventoryInfo.stamp
                when (selectedStamp.equals("0", true)) {
                    true -> {
                        binding.txtStampNewItem.setText("No")
                    }
                    false -> {
                        binding.txtStampNewItem.setText("Yes")
                    }
                }
                isUseColor = addInventoryInfo.use_gold_colour
                when (isUseColor.equals("0", true)) {
                    true -> {
                        binding.txtUseColorNewItem.setText("No")
                        binding.tvGoldColorNewItem.visibility = View.GONE
                        binding.llGoldColor.visibility = View.GONE
                    }
                    false -> {
                        binding.txtUseColorNewItem.setText("Yes")
                        binding.tvGoldColorNewItem.visibility = View.VISIBLE

                    }
                }


                val collectionType = object :
                    TypeToken<ArrayList<ItemDetailModel.Data.Item.ItemPrefVendor>>() {}.type

                item_preferred_vendor =
                    Gson().fromJson(addInventoryInfo.vendorNamesNIds, collectionType)



                for (vendors in item_preferred_vendor!!) {
                    //   Log.v("itemvendorName", "" + vendors.vendor.toString())

                    vendorselected(true, vendors.vendor.toString())

                }


                val collectionType1 = object :
                    TypeToken<ArrayList<ItemDetailModel.Data.Item.ItemPrefColor>>() {}.type

                item_preferred_color =
                    Gson().fromJson(addInventoryInfo.colorNamesNIds, collectionType1)



                for (color in item_preferred_color!!) {
                    // Log.v("itemvendorName", "" + vendors.vendor.toString())

                    colorselected(true, color.colour_name.toString())

                }
                //Vendor Selection
                vendorNameAdapter.apply {
                    addChbList(
                        vendorNameList,
                        selectedchbVList,
                        selectedVendorsList
                    )

                }
                // color selection
                colorNameAdapter.apply {
                    addChbList(
                        colorNameList,
                        selectedchbCList,
                        selectedColorsList
                    )

                }

                /* if (!addInventoryInfo.seletedColors.isNullOrBlank()) {
                     binding.txtGoldColorNewItem.setText(addInventoryInfo.seletedColors)
                 }*/

                binding.txtStockMinGmsNewItem.setText(addInventoryInfo.minStockGms)
                binding.txtStockMinPcsNewItem.setText(addInventoryInfo.minStockPcs)
                binding.txtStockMaxGmsNewItem.setText(addInventoryInfo.maxStockGms)
                binding.txtStockMaxPcsNewItem.setText(addInventoryInfo.maxStockPcs)
                binding.txtProductWtNewItem.setText(addInventoryInfo.product_wt)
                binding.txtItemRateNewItem.setText(addInventoryInfo.item_rate)
                /*if (!addInventoryInfo.selectedVendors.isNullOrBlank()) {
                    binding.txtVendorNewItem.setText(addInventoryInfo.selectedVendors)
                }*/


            }
            else->{

            }
        }
    }

    private fun saveInventoryInfoModel() {

        val collectionType = object :
            TypeToken<ArrayList<MultipleVendorNewItemModel.PrefVendorList>>() {}.type
        val multipleVendorjsonString: String = Gson().toJson(multipleVendorList, collectionType)

        val multiplevendorNamesnIDs = object :
            TypeToken<ArrayList<ItemDetailModel.Data.Item.ItemPrefVendor>>() {}.type
        val multipleVendorNamesnIdsjsonString: String =
            Gson().toJson(multipleVendorIdnNameList, multiplevendorNamesnIDs)

        val collectionColorType = object :
            TypeToken<ArrayList<MultipleColorNewItemModel.PrefColorList>>() {}.type
        val multipleColorjsonString: String =
            Gson().toJson(multipleColorList, collectionColorType)

        val multipleColorNamesnIDs = object :
            TypeToken<ArrayList<ItemDetailModel.Data.Item.ItemPrefColor>>() {}.type
        val multipleColorNamesnIdsjsonString: String =
            Gson().toJson(multipleColorIdnNameList, multiplevendorNamesnIDs)


        val addInventoryInfo = AddInventoryInfoModel(
            binding.txtStockMethodNewItem.text.toString(),
            binding.txtTagPrefixNewItem.text.toString(),
            selectedStamp,
            isUseColor,
            multipleColorjsonString.toString().trim(),
            binding.txtStockMinGmsNewItem.text.toString(),
            binding.txtStockMinPcsNewItem.text.toString(),
            binding.txtStockMaxGmsNewItem.text.toString(),
            binding.txtStockMaxPcsNewItem.text.toString(),
            binding.txtProductWtNewItem.text.toString(),
            binding.txtItemRateNewItem.text.toString(),
            multipleVendorjsonString.toString().trim(),
            multipleColorNamesnIdsjsonString,
            multipleVendorNamesnIdsjsonString
        )


        prefs[Constants.PREF_INVENTORY_INFO_KEY] =
            Gson().toJson(addInventoryInfo) //setter

    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                NewItemViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }


    private fun setupVendorAdapter() {

        vendorNameAdapter = PreferedVendorsAdapter(
            this,
            R.layout.row_vendors_list_additem,
            arrayListOf(), true
        )
        binding.txtVendorNewItem.setAdapter(vendorNameAdapter)
        binding.txtVendorNewItem.threshold = 1
    }

    private fun setupColorAdapter() {

        colorNameAdapter = PreferedColorsAdapter(
            this,
            R.layout.row_vendors_list_additem,
            arrayListOf(), true
        )
        binding.txtGoldColorNewItem.setAdapter(colorNameAdapter)
        binding.txtGoldColorNewItem.threshold = 1
    }


    fun getItemVendors() {
        if (NetworkUtils.isConnected()) {
            viewModel.getItemVendors(
                loginModel?.data?.bearer_access_token,
                loginModel?.data?.company_info?.id
            ).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {

                            if (it.data?.status == true) {
                                vendorList = it.data.data

                                vendorNameList = vendorList?.map { it.display_name }
                                //updateVendorNameList = vendorNameList as ArrayList<String>
                                setupVendorAdapter()


                                /*//Vendor Selection
                                vendorNameAdapter.apply {
                                    addChbList(
                                        vendorNameList,
                                        selectedchbVList,
                                        selectedVendorsList
                                    )

                                }*/



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

    fun getMetalColour() {

        if (NetworkUtils.isConnected()) {

            viewModel.getMetalColours(loginModel?.data?.bearer_access_token, "")
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {
                                    CommonUtils.hideProgress()
                                    metalColourList = it.data?.data!!

                                    colorNameList = metalColourList?.map { it.colour_name }
                                    //updateColorNameList = colorNameList as ArrayList<String>
                                    setupColorAdapter()

                                    /*colorNameAdapter.apply {
                                        addChbList(
                                            colorNameList,
                                            selectedchbCList,
                                            selectedColorsList
                                        )

                                    }*/
                                    //setEditData()
                                    isLoadedOnce = true

                                    when (is_from_edit) {
                                        true -> {
                                            getDataFromPrefInventory()
                                        }
                                        false -> {
                                            vendorNameAdapter.apply {
                                                addChbList(
                                                    vendorNameList,
                                                    selectedchbVList,
                                                    selectedVendorsList
                                                )

                                            }
                                            colorNameAdapter.apply {
                                                addChbList(
                                                    colorNameList,
                                                    selectedchbCList,
                                                    selectedColorsList
                                                )

                                            }
                                            getDataFromPrefInventory()

                                        }
                                    }


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

    fun vendorselected(chbChecked: Boolean, vendorName: String) {
        selectedVendorCount++

        val pos: Int? = vendorNameList?.indexOf(vendorName)
        //    Log.v("pos", pos.toString())
        binding.llVendorName.visibility = View.VISIBLE

        selectedVendorsList.add(vendorName)
        selectedVendorAdapter.notifyDataSetChanged()
        //showing total on right side corner
        selVendorsCount.text = selectedVendorCount.toString()

        val selectedVendorID = pos?.let { vendorList?.get(it)?.vendor_id }
        val selectedVendorName = pos?.let { vendorList?.get(it)?.display_name }

        selectedchbVList.add(chbChecked)
        selectedVendorsID.add(selectedVendorID.toString())

        multipleVendorList.add(MultipleVendorNewItemModel.PrefVendorList(selectedVendorID))
        multipleVendorIdnNameList.add(
            ItemDetailModel.Data.Item.ItemPrefVendor(
                selectedVendorName,
                selectedVendorID
            )
        )

        binding.txtVendorNewItem.setText("")

        //    Log.v("selectedchbVList", selectedchbVList.size.toString())
    }

    fun vendordeselected(chbChecked: Boolean, vendorName: String) {
        selectedVendorCount--
        val pos: Int? = vendorNameList?.indexOf(vendorName)
        //  Log.v("pos", pos.toString())
        if (selectedVendorCount > 0) {
            binding.llVendorName.visibility = View.VISIBLE
        } else binding.llVendorName.visibility = View.GONE

        val selectedVendorPos: Int? = selectedVendorsList.indexOf(vendorName)
        //   Log.v("selectedVendorPos", selectedVendorPos.toString())
        selectedVendorsList.remove(vendorName)


        selectedVendorAdapter.notifyDataSetChanged()
        //showing total on right side corner
        selVendorsCount.text = selectedVendorCount.toString()
        val deselectedVendorID = pos?.let { vendorList?.get(it)?.vendor_id }
        val deselectedVendorName = pos?.let { vendorList?.get(it)?.display_name }

        //selectedchbVList.removeAt(pos!!)

        selectedchbVList.removeAt(selectedVendorPos!!)

        //  Log.v("selectedchbVList", selectedchbVList.size.toString())
        //selectedchbVList.set(pos!!,chbChecked)
        selectedVendorsID.remove(deselectedVendorID.toString())
        multipleVendorList.remove(MultipleVendorNewItemModel.PrefVendorList(deselectedVendorID))
        multipleVendorIdnNameList.remove(
            ItemDetailModel.Data.Item.ItemPrefVendor(
                deselectedVendorName,
                deselectedVendorID
            )
        )

        txtVendorNewItem.setText("")
        when (chbChecked) {
            true -> {
                vendorNameAdapter.apply {
                    addChbList(
                        vendorNameList,
                        selectedchbVList,
                        selectedVendorsList
                    )

                }
            }
            else->{

            }
        }
    }


    fun colorselected(chbChecked: Boolean, colorName: String) {
        selectedColorCount++

        val pos: Int? = colorNameList?.indexOf(colorName)
        //  Log.v("colorpos", pos.toString())
        binding.llGoldColor.visibility = View.VISIBLE

        selectedColorsList.add(colorName)
        selectedColorAdapter.notifyDataSetChanged()
        //showing total on right side corner
        selGoldColorCount.text = selectedColorCount.toString()

        val selectedColorID = pos?.let { metalColourList?.get(it)?.metal_colour_id }
        val selectedColorName = pos?.let { metalColourList?.get(it)?.colour_name }

        selectedchbCList.add(chbChecked)
        //   Log.v("selectedchbCList", selectedchbCList.size.toString())
        selectedColorsID.add(selectedColorID.toString())
        multipleColorList.add(MultipleColorNewItemModel.PrefColorList(selectedColorID))
        multipleColorIdnNameList.add(
            ItemDetailModel.Data.Item.ItemPrefColor(
                selectedColorName,
                selectedColorID
            )
        )
        txtGoldColorNewItem.setText("")


    }

    fun colordeselected(chbChecked: Boolean, colorName: String) {
        selectedColorCount--

        val pos: Int? = colorNameList?.indexOf(colorName)
        //  Log.v("colorpos", pos.toString())
        if (selectedColorCount > 0) {
            binding.llGoldColor.visibility = View.VISIBLE
        } else binding.llGoldColor.visibility = View.GONE

        val selectedColorPos: Int? = selectedColorsList.indexOf(colorName)
        //  Log.v("selectedColorPos", selectedColorPos.toString())
        selectedColorsList.remove(colorName)
        selectedColorAdapter.notifyDataSetChanged()
        //showing total on right side corner
        selGoldColorCount.text = selectedColorCount.toString()

        selectedchbCList.removeAt(selectedColorPos!!)


        //  Log.v("selectedchbCList", selectedchbCList.size.toString())

        //selectedchbCList.set(pos!!,chbChecked)
        val deselectedColorID = pos?.let { metalColourList.get(it).metal_colour_id }
        val deselectedColorName = pos?.let { metalColourList.get(it).colour_name }

        selectedColorsID.remove(deselectedColorID.toString())
        multipleColorList.remove(MultipleColorNewItemModel.PrefColorList(deselectedColorID))
        multipleColorIdnNameList.remove(
            ItemDetailModel.Data.Item.ItemPrefColor(
                deselectedColorName,
                deselectedColorID
            )
        )

        txtGoldColorNewItem.setText("")
        when (chbChecked) {
            true -> {
                colorNameAdapter.apply {
                    addChbList(
                        colorNameList,
                        selectedchbCList,
                        selectedColorsList
                    )

                }
            }
            else->{

            }
        }
    }

    private fun openStockMethodMenu() {

        popupMenu = PopupMenu(this, txtStockMethodNewItem)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Loose")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Tag")
        popupMenu.menu.add(Menu.NONE, 3, 3, "Both")


        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtStockMethodNewItem.setText(item.title)
            selectedStockMethod = item.title.toString()

            true
        })
        popupMenu.show()

    }


    private fun openStampMenu() {

        popupMenu = PopupMenu(this, txtStampNewItem)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Yes")
        popupMenu.menu.add(Menu.NONE, 2, 2, "No")


        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtStampNewItem.setText(item.title)
            selectedStamp = when (item.title) {
                "Yes" -> {
                    "1"
                }
                "No" -> {
                    "0"
                }
                else -> {
                    ""
                }
            }
            true
        })
        popupMenu.show()

    }

    private fun openUseGoldColorMenu() {

        popupMenu = PopupMenu(this, txtUseColorNewItem)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Yes")
        popupMenu.menu.add(Menu.NONE, 2, 2, "No")


        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtUseColorNewItem.setText(item.title)
            isUseColor = when (item.title) {
                "Yes" -> {
                    binding.tvGoldColorNewItem.visibility = View.VISIBLE
                    "1"
                }
                "No" -> {
                    binding.tvGoldColorNewItem.visibility = View.GONE
                    "0"
                }
                else -> {
                    ""
                }
            }
            true
        })
        popupMenu.show()

    }

    fun deleteSelectedVendor(vendor: String) {

    }


}