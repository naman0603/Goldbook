package com.goldbookapp.ui.activity.item

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer

import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.NewItemActivityNewBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.settings.NewItemCategory
import com.goldbookapp.ui.activity.viewmodel.NewItemViewModel
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
import kotlinx.android.synthetic.main.new_item_activity_new.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.ArrayList

class NewItemActivity : AppCompatActivity() {

    lateinit var binding: NewItemActivityNewBinding
    private lateinit var viewModel: NewItemViewModel
    lateinit var popupMenu: PopupMenu
    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    var selectedMetalType: String? = "1"
    var selectedMetalTypeName: String? = ""
    var selectedMaintainStock: String? = "1"
    var selectedStudded: String? = "0"

    var selectedItemType: String? = "Goods"
    var selectedUnitID: String = "0"
    var selectedCategoryID: String? = "0"

    var categoryList: List<ActiveCategoriesModel.Data1033514216>? = null
    var categoryNameList: List<String>? = null
    lateinit var categoryNameAdapter: ArrayAdapter<String>


    var unitList: List<ItemUnitMenuModel.Data.UnitMenu>? = null
    var unitNameList: List<String>? = null

    var metaltypeList: List<MetalTypeModel.Data.MetalType>? = null
    var matalNameList: List<String>? = null

    var maintainStockList: List<MaintainStockModel.Data.MaintainStock>? = null
    var maintainStockNameList: List<String>? = null

    lateinit var addInventoryInfo: AddInventoryInfoModel
    lateinit var addAccountInfoList: AddAccountInfoModel

    var isPhotoSelected: Boolean = false
    var multipartImageBody: MultipartBody.Part? = null

    var multipleVendorIDList = ArrayList<MultipleVendorNewItemModel.PrefVendorList>()
    var multipleColorIDList = ArrayList<MultipleColorNewItemModel.PrefColorList>()
    var ledgerDiscountList: List<SearchLedgerModel.LedgerDetails>? = null
    var ledgerDiscountNameList: List<String>? = null
    var selectedDiscountLedgerID: String = ""
    var discountLedgerName: String = ""

    // edit item variables
    lateinit var itemDetailModel: ItemDetailModel.Data.Item
    var is_from_edit: Boolean = false
    var is_from_edit_inventory: Boolean = false
    var item_id: String = ""
    var isLoaded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.new_item_activity_new)

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

            getActiveCategories()
            getItemUnitMenu()

            getMaintainStock()
            getLedgerdd("discount")

            if (prefs.contains(Constants.PREF_ACCOUNTING_INFO_KEY)) {
                binding.tvAccountInfoNewItem.setText("+ Edit Accounting Information")
                getAccountingInfoDataFromPref()

            }

            if (prefs.contains(Constants.PREF_INVENTORY_INFO_KEY)) {
                binding.tvInventoryInfoNewItem.setText("+ Edit Inventory Information")
                getInventoryInfoDataFromPref()
                is_from_edit_inventory = true
            }
            if (!isLoaded) {
                getMetalType(isLoaded)
            }

        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun getAccountingInfoDataFromPref() {
        if (prefs.contains(Constants.PREF_ACCOUNTING_INFO_KEY)) {
            val accountingInfoDetails =
                object : TypeToken<AddAccountInfoModel>() {}.type
            addAccountInfoList = Gson().fromJson(
                prefs[Constants.PREF_ACCOUNTING_INFO_KEY, ""],
                accountingInfoDetails
            )


        }
    }


    private fun setupUIandListner() {

        imgLeft.setImageResource(R.drawable.ic_back)
        binding.root.tvTitle.setText(R.string.new_item)

        imgLeft?.clickWithDebounce {
            onBackPressed()
        }

        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        )

        clearPrefs()

        getDatafromIntent()
        when (is_from_edit) {
            false -> {
                Log.v("falseEdit", "")
                setDefaultData()
            }
            else -> {

            }

        }


        binding.tvUploadPhotoAllItem?.clickWithDebounce {

            ImagePicker.with(this)
                .cropSquare()                    //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                // User can only select image from Gallery
                .galleryOnly()
                .galleryMimeTypes( // no gif images at all
                    mimeTypes = arrayOf(
                        "image/png",
                        "image/jpg",
                        "image/jpeg"
                    )
                )
                .maxResultSize(
                    1080,
                    1080
                )    //Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        binding.radiogroupItemType.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                //gooods
                binding.radioGoods.id -> {
                    selectedItemType = "Goods"
                    binding.lyCatNewItem.visibility = View.VISIBLE
                    binding.cardMetalUnitNewItem.visibility = View.VISIBLE
                    binding.cardInventoryInfoNewItem.visibility = View.VISIBLE
                }
                //service
                binding.radioService.id -> {
                    selectedItemType = "Service"

                    binding.lyCatNewItem.visibility = View.GONE
                    binding.cardMetalUnitNewItem.visibility = View.GONE
                    binding.cardInventoryInfoNewItem.visibility = View.GONE
                }
            }
        })


        binding.imgCategoryAddItem?.clickWithDebounce {
            startActivity(Intent(this, NewItemCategory::class.java))
        }


        binding.cardAccountInfoNewItem.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AccountingInfoActivity::class.java
                ).putExtra(Constants.NEWITEM_ITEM_TYPE_KEY, selectedItemType)
                    .putExtra(Constants.IS_FOR_EDIT, is_from_edit)
            )
        }

        binding.tvShowInfoNewItem.setOnClickListener {

            binding.lyMoreInfoNewItem.visibility = View.VISIBLE
            binding.tvShowInfoNewItem.visibility = View.GONE
        }

        selectedMetalTypeName = binding.txtMetaltypeNewItem.text.toString()
        binding.cardInventoryInfoNewItem.clickWithDebounce {
            startActivity(
                Intent(
                    this,
                    InventoryInfoActivity::class.java
                )
                    .putExtra(Constants.NEWITEM_METAL_TYPE_KEY, selectedMetalTypeName)
                    .putExtra(Constants.IS_FOR_EDIT, is_from_edit)
            )
        }

        binding.txtDiscountLedgerNewItem.clickWithDebounce {
            openDiscountLedgerMenu(ledgerDiscountNameList)
        }

        binding.txtMetaltypeNewItem.clickWithDebounce {
            openMetalTypeMenu(matalNameList)
        }

        binding.txtStockMaintainNewItem.clickWithDebounce {
            openMintainStockMenu(maintainStockNameList)
        }

        binding.txtUnitNewItem.clickWithDebounce {
            openUnitMenu(unitNameList)
        }


        binding.txtStuddedNewItem.clickWithDebounce {
            openStuddedMenu()
        }

        binding.btnSaveAddItemNew.setOnClickListener {
            if (performValidation()) {
                when (is_from_edit) {
                    true -> {
                        setRequestBodyParamCallEditAPI()
                    }
                    false -> {
                        setRequestBodyParamCallAPI()
                    }
                }
            }
        }

    }

    fun performValidation(): Boolean {
        if (binding.txtCodeNewItem.text.toString().isBlank()) {
            CommonUtils.showDialog(
                this,
                getString(R.string.enter_item_code_msg)
            )
            binding.txtCodeNewItem.requestFocus()
            return false
        } else if (binding.txtItemNameNewItem.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.enter_item_name_msg))
            binding.txtItemNameNewItem.requestFocus()
            return false
        } else if ((selectedItemType.equals("Goods")) && binding.txtCategoryNewItem.text.toString()
                .isBlank()
        ) {
            CommonUtils.showDialog(this, getString(R.string.select_cateogory_msg))
            binding.txtCategoryNewItem.requestFocus()
            return false
        }
        return true
    }

    private fun setDefaultData() {
        binding.txtMetaltypeNewItem.setText("Gold")
        binding.txtStockMaintainNewItem.setText("Grams")
        binding.txtUnitNewItem.setText("Piece")
        binding.txtStuddedNewItem.setText("No")
        selectedStudded = "0"
    }

    private fun setRequestBodyParamCallEditAPI() {

        val item_type: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            selectedItemType
        )

        val item_id: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            item_id
        )


        val item_name: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.txtItemNameNewItem.text.toString().trim()
        )
        val item_code: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.txtCodeNewItem.text.toString().trim()
        )

        val category_id: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            selectedCategoryID
        )
        val unit_name: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            selectedUnitID
        )

        val notes: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.txtNotesNewItem.text.toString().trim()
        )

        val metal_type: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            selectedMetalType
        )
        val maintain_stock_in: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            selectedMaintainStock
        )
        val is_studded: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            selectedStudded
        )
        val discount_ledger_id: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            selectedDiscountLedgerID
        )

        val tax_preference: RequestBody
        val sales_wastage: RequestBody
        val sales_making_charge: RequestBody
        val purchase_wastage: RequestBody
        val purchase_making_charge: RequestBody
        val jobwork_rate: RequestBody
        val labourwork_rate: RequestBody
        val sales_purchase_gst_rate: RequestBody
        val sales_purchase_hsn: RequestBody
        val jobwork_labourwork_gst_rate: RequestBody
        val jobwork_labourwork_sac: RequestBody
        val sales_rate: RequestBody
        val purchase_rate: RequestBody
        val sales_ledger_id: RequestBody
        val purchase_ledger_id: RequestBody
        val jobwork_ledger_id: RequestBody
        val labourwork_ledger_id: RequestBody



        when (prefs.contains(Constants.PREF_ACCOUNTING_INFO_KEY)) {
            true -> {
                tax_preference = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.taxPreference
                )
                sales_wastage = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.wastageSales
                )
                sales_making_charge = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.makingchargeSales
                )
                purchase_wastage = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.wastagePurchase
                )
                purchase_making_charge = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.makingchargepurchase
                )

                jobwork_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.jobworkRate
                )
                labourwork_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.labourRate
                )
                sales_purchase_gst_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.salePurGst
                )
                sales_purchase_hsn = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.salepurHsn
                )
                jobwork_labourwork_gst_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.jobLabourGst
                )

                jobwork_labourwork_sac = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.jobLaburSac
                )
                sales_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.salesRate
                )
                purchase_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.purchaseRate
                )
                sales_ledger_id = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.salesLedger
                )
                purchase_ledger_id = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.purchaseLedger
                )
                jobwork_ledger_id = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.jobworkLedger
                )
                labourwork_ledger_id = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.labourLedger
                )

            }
            false -> {
                tax_preference = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                sales_wastage = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                sales_making_charge = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                purchase_wastage = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                purchase_making_charge = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )

                jobwork_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                labourwork_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                sales_purchase_gst_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                sales_purchase_hsn = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                jobwork_labourwork_gst_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )

                jobwork_labourwork_sac = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                sales_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                purchase_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                sales_ledger_id = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                purchase_ledger_id = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                jobwork_ledger_id = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                labourwork_ledger_id = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )

            }
        }

        val tag_prefix: RequestBody
        val use_stamp: RequestBody
        val use_gold_color: RequestBody
        val min_stock_level_gm: RequestBody
        val min_stock_level_pcs: RequestBody
        val max_stock_level_gm: RequestBody
        val max_stock_level_pcs: RequestBody
        val product_wt: RequestBody
        val item_rate: RequestBody
        val vendor_id: RequestBody
        val gold_colour: RequestBody
        val stock_method: RequestBody

        when (prefs.contains(Constants.PREF_INVENTORY_INFO_KEY)) {
            true -> {
                tag_prefix = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.tagInventoryInfo
                )
                use_stamp = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.stamp
                )
                use_gold_color = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.use_gold_colour
                )


                min_stock_level_gm = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.minStockGms.toString()
                )

                min_stock_level_pcs = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.minStockPcs.toString()
                )

                max_stock_level_gm = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.maxStockGms.toString()
                )

                max_stock_level_pcs = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.maxStockPcs.toString()
                )

                product_wt = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.product_wt.toString()
                )

                item_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.item_rate.toString()
                )

                vendor_id = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.selectedVendors
                )

                gold_colour = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.seletedColors
                )

                stock_method = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.stockMethod
                )

            }
            false -> {
                tag_prefix = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                use_stamp = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                use_gold_color = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )


                min_stock_level_gm = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )

                min_stock_level_pcs = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )

                max_stock_level_gm = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )

                max_stock_level_pcs = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                product_wt = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                item_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )

                vendor_id = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )

                gold_colour = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )

                stock_method = RequestBody.create(
                    MediaType.parse("text/plain"),
                    "Loose"
                )
            }
        }




        editItemAPI(
            loginModel?.data?.bearer_access_token,
            item_id,
            item_type, item_name, item_code, category_id,
            notes, metal_type, maintain_stock_in, unit_name,
            is_studded, stock_method, tax_preference,
            sales_wastage,
            sales_making_charge,
            purchase_wastage,
            purchase_making_charge,
            jobwork_rate,
            labourwork_rate,
            sales_purchase_gst_rate,
            sales_purchase_hsn,
            jobwork_labourwork_gst_rate,
            jobwork_labourwork_sac,
            sales_rate,
            purchase_rate,
            sales_ledger_id,
            purchase_ledger_id,
            jobwork_ledger_id,
            labourwork_ledger_id,
            discount_ledger_id,
            tag_prefix,
            use_stamp,
            use_gold_color,
            min_stock_level_gm,
            min_stock_level_pcs,
            max_stock_level_gm,
            max_stock_level_pcs,
            product_wt,
            item_rate,
            vendor_id,
            gold_colour,
            multipartImageBody
        )

    }

    private fun getDatafromIntent() {

        if (intent.extras != null) {
            if (intent.extras?.containsKey(Constants.ITEM_DETAIL_KEY)!!) {
                val item_str: String? = intent.getStringExtra(Constants.ITEM_DETAIL_KEY)
                itemDetailModel = Gson().fromJson(
                    item_str,
                    ItemDetailModel.Data.Item::class.java
                )
                // set edit item data
                binding.root.tvTitle.setText(R.string.edit_item)
                is_from_edit = true
                item_id = itemDetailModel.item_id!!
                binding.lyMoreInfoNewItem.visibility = View.VISIBLE
                binding.tvShowInfoNewItem.visibility = View.GONE
                selectedMetalType = itemDetailModel.metal_type_id
                setData()


                // fill inventory and accounting info to pref

                when (itemDetailModel.tax_preference.isNullOrBlank()) {
                    // no accounting info saved
                    true -> {

                    }
                    // accounting info saved to pref
                    false -> {
                        saveAccountInfotoPref()
                    }
                }
                when (itemDetailModel.stock_method.isNullOrBlank()) {
                    // no inventory info saved
                    true -> {

                    }
                    // inventory info saved to pref
                    false -> {
                        saveInventoryInfotoPref()
                    }
                }
            }
        }


    }

    private fun saveInventoryInfotoPref() {
        val collectionType = object :
            TypeToken<java.util.ArrayList<MultipleVendorNewItemModel.PrefVendorList>>() {}.type
        val multipleVendorjsonString: String =
            Gson().toJson(itemDetailModel.item_preferred_vendor, collectionType)

        for (vendorid in itemDetailModel.item_preferred_vendor!!) {
            multipleVendorIDList.add(MultipleVendorNewItemModel.PrefVendorList(vendorid.vendor_id))
        }
        for (colorid in itemDetailModel.item_colour!!) {
            multipleColorIDList.add(MultipleColorNewItemModel.PrefColorList(colorid.colour_id))
        }


        val multipleVendorIdType = object :
            TypeToken<java.util.ArrayList<MultipleVendorNewItemModel.PrefVendorList>>() {}.type
        val multipleVendorIdjsonString: String =
            Gson().toJson(multipleVendorIDList, multipleVendorIdType)

        val multipleColorIdType = object :
            TypeToken<java.util.ArrayList<MultipleColorNewItemModel.PrefColorList>>() {}.type
        val multipleColorIdjsonString: String =
            Gson().toJson(multipleColorIDList, multipleColorIdType)


        val collectionColorType = object :
            TypeToken<java.util.ArrayList<MultipleColorNewItemModel.PrefColorList>>() {}.type
        val multipleColorjsonString: String =
            Gson().toJson(itemDetailModel.item_colour, collectionColorType)


        addInventoryInfo = AddInventoryInfoModel(
            itemDetailModel.stock_method ?: "",
            itemDetailModel.tag_prefix ?: "",
            itemDetailModel.use_stamp ?: "",
            itemDetailModel.use_gold_color ?: "",
            multipleColorIdjsonString,
            itemDetailModel.min_stock_level_gm ?: "",
            itemDetailModel.min_stock_level_pcs ?: "",
            itemDetailModel.max_stock_level_gm ?: "",
            itemDetailModel.max_stock_level_pcs ?: "",
            itemDetailModel.product_wt ?: "",
            itemDetailModel.item_rate ?: "",
            multipleVendorIdjsonString,
            multipleColorjsonString,
            multipleVendorjsonString
        )

        prefs[Constants.PREF_INVENTORY_INFO_KEY] = Gson().toJson(addInventoryInfo)

    }

    private fun saveAccountInfotoPref() {

        addAccountInfoList = AddAccountInfoModel(
            itemDetailModel.tax_preference ?: "",
            itemDetailModel.sales_purchase_gst_rate_id ?: "",
            itemDetailModel.sales_purchase_hsn ?: "",
            itemDetailModel.jobwork_labourwork_gst_rate_id ?: "",
            itemDetailModel.jobwork_labourwork_sac ?: "",
            itemDetailModel.sales_wastage ?: "",
            itemDetailModel.sales_making_charges ?: "",
            itemDetailModel.sales_ledger_id ?: "",
            itemDetailModel.purchase_wastage ?: "",
            itemDetailModel.purchase_making_charges ?: "",
            itemDetailModel.purchase_ledger_id ?: "",
            itemDetailModel.jobwork_rate ?: "",
            itemDetailModel.jobwork_ledger_id ?: "",
            itemDetailModel.labourwork_rate ?: "",
            itemDetailModel.labourwork_ledger_id ?: "",
            itemDetailModel.sales_rate ?: "",
            itemDetailModel.purchase_rate ?: "",
            itemDetailModel.sales_ledger_name ?: "",
            itemDetailModel.purchase_ledger_name ?: "",
            itemDetailModel.jobwork_ledger_name ?: "",
            itemDetailModel.labourwork_ledger_name ?: "",
            itemDetailModel.sales_purchase_gst_rate ?: "",
            itemDetailModel.jobwork_labourwork_gst_rate ?: ""

        )
        prefs[Constants.PREF_ACCOUNTING_INFO_KEY] = Gson().toJson(addAccountInfoList)


    }

    private fun clearPrefs() {
        if (prefs.contains(Constants.PREF_ACCOUNTING_INFO_KEY)) {
            prefs.edit().remove(Constants.PREF_ACCOUNTING_INFO_KEY).apply()
        }

        if (prefs.contains(Constants.PREF_INVENTORY_INFO_KEY)) {
            prefs.edit().remove(Constants.PREF_INVENTORY_INFO_KEY).apply()
        }
    }

    private fun setData() {


        if (itemDetailModel.image?.size!! > 0) {
            Glide.with(this).load(itemDetailModel.image?.get(0)?.item_image).circleCrop()
                .into(imgNewItem)
            binding.tvUploadPhotoAllItem.visibility = View.GONE
            binding.imgNewItem.visibility = View.VISIBLE
        } else {
            binding.tvUploadPhotoAllItem.visibility = View.VISIBLE
        }
        selectedItemType = itemDetailModel.item_type
        when (selectedItemType.equals("Goods", true)) {
            true -> {
                binding.radioGoods.isChecked = true
                binding.lyCatNewItem.visibility = View.VISIBLE
                binding.cardMetalUnitNewItem.visibility = View.VISIBLE
                binding.cardInventoryInfoNewItem.visibility = View.VISIBLE
            }
            false -> {
                binding.radioService.isChecked = true
                binding.lyCatNewItem.visibility = View.GONE
                binding.cardMetalUnitNewItem.visibility = View.GONE
                binding.cardInventoryInfoNewItem.visibility = View.GONE
            }

        }

        binding.txtCodeNewItem.setText(itemDetailModel.item_code)
        binding.txtItemNameNewItem.setText(itemDetailModel.item_name)
        binding.txtCategoryNewItem.setText(itemDetailModel.category_name)
        selectedCategoryID = itemDetailModel.category_id ?: "0"
        selectedMetalType = itemDetailModel.metal_type_id ?: "0"
        selectedMetalTypeName = itemDetailModel.metal_type
        binding.txtMetaltypeNewItem.setText(itemDetailModel.metal_type) ?: ""
        selectedMaintainStock = itemDetailModel.maintain_stock_in_id ?: "0"
        binding.txtStockMaintainNewItem.setText(itemDetailModel.maintain_stock_in)
        selectedUnitID = itemDetailModel.unit_id ?: "0"
        binding.txtUnitNewItem.setText(itemDetailModel.unit_name) ?: ""
        selectedStudded = itemDetailModel.is_studded ?: ""
        binding.txtDiscountLedgerNewItem.setText(itemDetailModel.discount_ledger_name)
        selectedDiscountLedgerID = itemDetailModel.discount_ledger_id!!
        discountLedgerName = itemDetailModel.discount_ledger_name!!


        when (selectedStudded.equals("1", false)) {
            true -> {
                Log.v("isstudded", "" + selectedStudded)
                binding.txtStuddedNewItem.setText("Yes")
            }
            false -> {
                binding.txtStuddedNewItem.setText("No")
            }
        }

        binding.txtNotesNewItem.setText(itemDetailModel.notes)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            val fileUri = data?.data
            //imgProfile.setImageURI(fileUri)

            binding.tvUploadPhotoAllItem.visibility = View.GONE
            binding.imgNewItem.visibility = View.VISIBLE
            isPhotoSelected = true
            Glide.with(this).load(fileUri).circleCrop().into(binding.imgNewItem)

            //You can get File object from intent
            val imageFile: File = ImagePicker.getFile(data)!!

            val fileBody: RequestBody =
                RequestBody.create(MediaType.parse("multipart/form-data"), imageFile)
            multipartImageBody =
                MultipartBody.Part.createFormData("image[]", imageFile.name, fileBody)


            //You can also get File Path from intent
            val filePath: String = ImagePicker.getFilePath(data)!!


        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {

        }
    }

    private fun getInventoryInfoDataFromPref() {

        if (prefs.contains(Constants.PREF_INVENTORY_INFO_KEY)) {
            val inventoryInfoDetails =
                object :
                    TypeToken<AddInventoryInfoModel>() {}.type
            addInventoryInfo = Gson().fromJson(
                prefs[Constants.PREF_INVENTORY_INFO_KEY, ""],
                inventoryInfoDetails
            )


        }
    }


    private fun openMetalTypeMenu(matalNameList: List<String>?) {

        popupMenu = PopupMenu(this, binding.txtMetaltypeNewItem)
        for (i in 0 until this.matalNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                this.matalNameList!!.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtMetaltypeNewItem.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = this.matalNameList!!.indexOf(selected)

            selectedMetalType =
                pos?.let { it1 -> metaltypeList?.get(it1)?.id }.toString()

            selectedMetalTypeName = pos?.let { it1 -> metaltypeList?.get(it1)?.name }.toString()

            /*if (selectedMetalType.equals("3", true)) {
                binding.txtStockMaintainNewItem.isEnabled = false
            } else {
                binding.txtStockMaintainNewItem.isEnabled = true

            }*/


            true
        })

        popupMenu.show()
    }

    private fun openMintainStockMenu(maintainStockNameList: List<String>?) {

        popupMenu = PopupMenu(this, binding.txtStockMaintainNewItem)
        for (i in 0 until this.maintainStockNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                this.maintainStockNameList!!.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtStockMaintainNewItem.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = this.maintainStockNameList!!.indexOf(selected)

            selectedMaintainStock =
                pos?.let { it1 -> maintainStockList?.get(it1)?.id }.toString()

            true
        })

        popupMenu.show()


    }

    private fun openUnitMenu(unitNameList: List<String>?) {

        popupMenu = PopupMenu(this, binding.txtUnitNewItem)
        for (i in 0 until unitNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                unitNameList!!.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtUnitNewItem.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = unitNameList!!.indexOf(selected)

            selectedUnitID =
                pos?.let { it1 -> unitList?.get(it1)?.id }.toString()

            true
        })

        popupMenu.show()

    }

    private fun openStuddedMenu() {

        popupMenu = PopupMenu(this, binding.txtStuddedNewItem)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Yes")
        popupMenu.menu.add(Menu.NONE, 2, 2, "No")


        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtStuddedNewItem.setText(item.title)
            selectedStudded = when (item.title) {
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

    fun getActiveCategories() {
        if (NetworkUtils.isConnected()) {
            viewModel.searchItemCategory(loginModel?.data?.bearer_access_token, "")
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {

                                if (it.data?.status == true) {
                                    categoryList = it.data.data

                                    categoryNameList = categoryList?.map { it.category_name }

                                    categoryNameAdapter = ArrayAdapter<String>(
                                        this,
                                        android.R.layout.simple_dropdown_item_1line,
                                        categoryNameList!!
                                    )
                                    binding.txtCategoryNewItem.setAdapter(categoryNameAdapter)
                                    binding.txtCategoryNewItem.threshold = 1

                                    binding.txtCategoryNewItem.setOnItemClickListener { adapterView, view, position, l
                                        ->
                                        val selected: String =
                                            adapterView.getItemAtPosition(position).toString()
                                        val pos: Int? = categoryNameList?.indexOf(selected)

                                        selectedCategoryID =
                                            pos?.let { it1 -> categoryList?.get(it1)?.item_category_id }
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
                                // CommonUtils.hideProgress()
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

                                    "discount" -> {
                                        ledgerDiscountNameList = ArrayList<String>()
                                        ledgerDiscountList = it.data.data
                                        ledgerDiscountNameList =
                                            ledgerDiscountList?.map { it.name.toString() }
                                        if (!is_from_edit) {
                                            binding.txtDiscountLedgerNewItem.setText(
                                                ledgerDiscountList!!.get(0).name
                                            )
                                            selectedDiscountLedgerID =
                                                ledgerDiscountList!!.get(0).ledger_id!!
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

    fun getItemUnitMenu() {
        if (NetworkUtils.isConnected()) {
            viewModel.getItemUnitMenu(
                loginModel?.data?.bearer_access_token
            ).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {

                            if (it.data?.status == true) {
                                unitList = it.data.data?.unit

                                unitNameList = unitList?.map { it.name }
                                selectedUnitID = unitList?.get(0)?.id.toString()

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

    fun getMetalType(is_from_edit_inventory: Boolean) {
        if (NetworkUtils.isConnected()) {
            viewModel.getMetalType(
                loginModel?.data?.bearer_access_token
            ).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {

                            if (it.data?.status == true) {
                                isLoaded = true
                                metaltypeList = it.data.data!!.metal_type

                                matalNameList = metaltypeList?.map { it.name }
                                /* when(is_from_edit_inventory){
                                     false->{
                                         selectedMetalType = metaltypeList?.get(0)?.id.toString()
                                         selectedMetalTypeName = metaltypeList?.get(0)?.name.toString()
                                         Log.v("metal", "" + selectedMetalType)
                                     }
                                     else->{

                                     }
                                 }*/


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

    fun getMaintainStock() {
        if (NetworkUtils.isConnected()) {
            viewModel.getMaintainStock(
                loginModel?.data?.bearer_access_token
            ).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {

                            if (it.data?.status == true) {
                                maintainStockList = it.data.data!!.maintain_stock_in

                                maintainStockNameList = maintainStockList?.map { it.name }
                                selectedMaintainStock = maintainStockList?.get(0)?.id.toString()

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


    fun setRequestBodyParamCallAPI() {


        val item_type: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            selectedItemType
        )

        val item_name: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.txtItemNameNewItem.text.toString().trim()
        )
        val item_code: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.txtCodeNewItem.text.toString().trim()
        )

        val category_id: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            selectedCategoryID
        )
        val unit_name: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            selectedUnitID
        )

        val notes: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.txtNotesNewItem.text.toString().trim()
        )

        val metal_type: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            selectedMetalType
        )
        val maintain_stock_in: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            selectedMaintainStock
        )
        val is_studded: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            selectedStudded
        )

        val discount_ledger_id: RequestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            selectedDiscountLedgerID
        )

        val tax_preference: RequestBody
        val sales_wastage: RequestBody
        val sales_making_charge: RequestBody
        val purchase_wastage: RequestBody
        val purchase_making_charge: RequestBody
        val jobwork_rate: RequestBody
        val labourwork_rate: RequestBody
        val sales_purchase_gst_rate: RequestBody
        val sales_purchase_hsn: RequestBody
        val jobwork_labourwork_gst_rate: RequestBody
        val jobwork_labourwork_sac: RequestBody
        val sales_rate: RequestBody
        val purchase_rate: RequestBody
        val sales_ledger_id: RequestBody
        val purchase_ledger_id: RequestBody
        val jobwork_ledger_id: RequestBody
        val labourwork_ledger_id: RequestBody


        when (prefs.contains(Constants.PREF_ACCOUNTING_INFO_KEY)) {
            true -> {
                tax_preference = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.taxPreference
                )
                sales_wastage = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.wastageSales
                )
                sales_making_charge = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.makingchargeSales
                )
                purchase_wastage = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.wastagePurchase
                )
                purchase_making_charge = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.makingchargepurchase
                )

                jobwork_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.jobworkRate
                )
                labourwork_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.labourRate
                )
                sales_purchase_gst_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.salePurGst
                )
                sales_purchase_hsn = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.salepurHsn
                )
                jobwork_labourwork_gst_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.jobLabourGst
                )

                jobwork_labourwork_sac = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.jobLaburSac
                )
                sales_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.salesRate
                )
                purchase_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.purchaseRate
                )
                sales_ledger_id = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.salesLedger
                )
                purchase_ledger_id = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.purchaseLedger
                )
                jobwork_ledger_id = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.jobworkLedger
                )
                labourwork_ledger_id = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addAccountInfoList.labourLedger
                )


            }
            false -> {
                tax_preference = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                sales_wastage = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                sales_making_charge = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                purchase_wastage = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                purchase_making_charge = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )

                jobwork_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                labourwork_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                sales_purchase_gst_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                sales_purchase_hsn = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                jobwork_labourwork_gst_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )

                jobwork_labourwork_sac = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                sales_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                purchase_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                sales_ledger_id = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                purchase_ledger_id = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                jobwork_ledger_id = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                labourwork_ledger_id = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )

            }
        }

        val tag_prefix: RequestBody
        val use_stamp: RequestBody
        val use_gold_color: RequestBody
        val min_stock_level_gm: RequestBody
        val min_stock_level_pcs: RequestBody
        val max_stock_level_gm: RequestBody
        val max_stock_level_pcs: RequestBody
        val product_wt: RequestBody
        val item_rate: RequestBody
        val vendor_id: RequestBody
        val gold_colour: RequestBody
        val stock_method: RequestBody

        when (prefs.contains(Constants.PREF_INVENTORY_INFO_KEY)) {
            true -> {
                tag_prefix = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.tagInventoryInfo
                )
                use_stamp = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.stamp
                )
                use_gold_color = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.use_gold_colour
                )


                min_stock_level_gm = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.minStockGms.toString()
                )

                min_stock_level_pcs = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.minStockPcs.toString()
                )

                max_stock_level_gm = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.maxStockGms.toString()
                )

                max_stock_level_pcs = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.maxStockPcs.toString()
                )

                product_wt = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.product_wt.toString()
                )

                item_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.item_rate.toString()
                )


                vendor_id = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.selectedVendors
                )

                gold_colour = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.seletedColors
                )

                stock_method = RequestBody.create(
                    MediaType.parse("text/plain"),
                    addInventoryInfo.stockMethod
                )

            }
            false -> {
                tag_prefix = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                use_stamp = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )
                use_gold_color = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )


                min_stock_level_gm = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )

                min_stock_level_pcs = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )

                max_stock_level_gm = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )

                max_stock_level_pcs = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )

                product_wt = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )

                item_rate = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )

                vendor_id = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )

                gold_colour = RequestBody.create(
                    MediaType.parse("text/plain"),
                    ""
                )

                stock_method = RequestBody.create(
                    MediaType.parse("text/plain"),
                    "Loose"
                )
            }
        }



        addNewItemAPI(
            loginModel?.data?.bearer_access_token,
            item_type, item_name, item_code, category_id,
            notes, metal_type, maintain_stock_in, unit_name,
            is_studded, stock_method, tax_preference,
            sales_wastage,
            sales_making_charge,
            purchase_wastage,
            purchase_making_charge,
            jobwork_rate,
            labourwork_rate,
            sales_purchase_gst_rate,
            sales_purchase_hsn,
            jobwork_labourwork_gst_rate,
            jobwork_labourwork_sac,
            sales_rate,
            purchase_rate,
            sales_ledger_id,
            purchase_ledger_id,
            jobwork_ledger_id,
            labourwork_ledger_id,
            discount_ledger_id,
            tag_prefix,
            use_stamp,
            use_gold_color,
            min_stock_level_gm,
            min_stock_level_pcs,
            max_stock_level_gm,
            max_stock_level_pcs,
            product_wt,
            item_rate,
            vendor_id,
            gold_colour,
            multipartImageBody
        )


    }


    fun addNewItemAPI(
        token: String?,
        item_type: RequestBody?,
        item_name: RequestBody?,
        item_code: RequestBody?,
        category_id: RequestBody?,
        notes: RequestBody?,
        metal_type_id: RequestBody?,
        maintain_stock_in_id: RequestBody?,
        unit_id: RequestBody?,
        is_studded: RequestBody?,
        stock_method: RequestBody?,
        tax_preference: RequestBody?,
        sales_wastage: RequestBody?,
        sales_making_charges: RequestBody?,
        purchase_wastage: RequestBody?,
        purchase_making_charges: RequestBody?,
        jobwork_rate: RequestBody?,
        labourwork_rate: RequestBody?,
        sales_purchase_gst_rate_id: RequestBody?,
        sales_purchase_hsn: RequestBody?,
        jobwork_labourwork_gst_rate_id: RequestBody?,
        jobwork_labourwork_sac: RequestBody?,
        sales_rate: RequestBody?,
        purchase_rate: RequestBody?,
        sales_ledger_id: RequestBody?,
        purchase_ledger_id: RequestBody?,
        jobwork_ledger_id: RequestBody?,
        labourwork_ledger_id: RequestBody?,
        discount_ledger_id: RequestBody?,
        tag_prefix: RequestBody?,
        use_stamp: RequestBody?,
        use_gold_color: RequestBody?,
        min_stock_level_gm: RequestBody?,
        min_stock_level_pcs: RequestBody?,
        max_stock_level_gm: RequestBody?,
        max_stock_level_pcs: RequestBody?,
        product_wt: RequestBody?,
        item_rate: RequestBody?,
        vendor_id: RequestBody?,
        gold_colour: RequestBody?,
        item_image: MultipartBody.Part?
    ) {

        viewModel.addNewItem(
            token, item_type, item_name, item_code, category_id,
            notes, metal_type_id, maintain_stock_in_id, unit_id,
            is_studded, stock_method, tax_preference,
            sales_wastage,
            sales_making_charges,
            purchase_wastage,
            purchase_making_charges,
            jobwork_rate,
            labourwork_rate,
            sales_purchase_gst_rate_id,
            sales_purchase_hsn,
            jobwork_labourwork_gst_rate_id,
            jobwork_labourwork_sac,
            sales_rate,
            purchase_rate,
            sales_ledger_id,
            purchase_ledger_id,
            jobwork_ledger_id,
            labourwork_ledger_id,
            discount_ledger_id,
            tag_prefix,
            use_stamp,
            use_gold_color,
            min_stock_level_gm,
            min_stock_level_pcs,
            max_stock_level_gm,
            max_stock_level_pcs,
            product_wt,
            item_rate,
            vendor_id,
            gold_colour,
            item_image
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
                            Toast.makeText(
                                this,
                                it.data?.errormessage?.message,
                                Toast.LENGTH_LONG
                            )
                                .show()
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


    fun editItemAPI(
        token: String?,
        item_id: RequestBody?,
        item_type: RequestBody?,
        item_name: RequestBody?,
        item_code: RequestBody?,
        category_id: RequestBody?,
        notes: RequestBody?,
        metal_type_id: RequestBody?,
        maintain_stock_in_id: RequestBody?,
        unit_id: RequestBody?,
        is_studded: RequestBody?,
        stock_method: RequestBody?,
        tax_preference: RequestBody?,
        sales_wastage: RequestBody?,
        sales_making_charges: RequestBody?,
        purchase_wastage: RequestBody?,
        purchase_making_charges: RequestBody?,
        jobwork_rate: RequestBody?,
        labourwork_rate: RequestBody?,
        sales_purchase_gst_rate_id: RequestBody?,
        sales_purchase_hsn: RequestBody?,
        jobwork_labourwork_gst_rate_id: RequestBody?,
        jobwork_labourwork_sac: RequestBody?,
        sales_rate: RequestBody?,
        purchase_rate: RequestBody?,
        sales_ledger_id: RequestBody?,
        purchase_ledger_id: RequestBody?,
        jobwork_ledger_id: RequestBody?,
        labourwork_ledger_id: RequestBody?,
        discount_ledger_id: RequestBody?,
        tag_prefix: RequestBody?,
        use_stamp: RequestBody?,
        use_gold_color: RequestBody?,
        min_stock_level_gm: RequestBody?,
        min_stock_level_pcs: RequestBody?,
        max_stock_level_gm: RequestBody?,
        max_stock_level_pcs: RequestBody?,
        product_wt: RequestBody?,
        item_rate: RequestBody?,
        vendor_id: RequestBody?,
        gold_colour: RequestBody?,
        item_image: MultipartBody.Part?
    ) {

        viewModel.editItem(
            token, item_id, item_type, item_name, item_code, category_id,
            notes, metal_type_id, maintain_stock_in_id, unit_id,
            is_studded, stock_method, tax_preference,
            sales_wastage,
            sales_making_charges,
            purchase_wastage,
            purchase_making_charges,
            jobwork_rate,
            labourwork_rate,
            sales_purchase_gst_rate_id,
            sales_purchase_hsn,
            jobwork_labourwork_gst_rate_id,
            jobwork_labourwork_sac,
            sales_rate,
            purchase_rate,
            sales_ledger_id,
            purchase_ledger_id,
            jobwork_ledger_id,
            labourwork_ledger_id,
            discount_ledger_id,
            tag_prefix,
            use_stamp,
            use_gold_color,
            min_stock_level_gm,
            min_stock_level_pcs,
            max_stock_level_gm,
            max_stock_level_pcs,
            product_wt,
            item_rate,
            vendor_id,
            gold_colour,
            item_image
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
                            Toast.makeText(
                                this,
                                it.data!!.errormessage?.message,
                                Toast.LENGTH_LONG
                            )
                                .show()
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