package com.goldbookapp.ui.activity.customer

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityTdsDetailsBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.viewmodel.CustSuppTcsTdsViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.SupplierDetailModel
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
import hk.ids.gws.android.sclick.SClick
import kotlinx.android.synthetic.main.activity_tds_details.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class CustSuppTcsTdsMgmtActivity : AppCompatActivity() {
    private lateinit var tcsTdsShareDataModel: TcsTdsShareDataModel

    private lateinit var viewModel: CustSuppTcsTdsViewModel
    lateinit var binding: ActivityTdsDetailsBinding
    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    lateinit var popupMenuCollector: PopupMenu
    lateinit var popupMenuDeductor: PopupMenu

    lateinit var popupMenuNog: PopupMenu
    lateinit var popupMenuNop: PopupMenu

    lateinit var custDetailModel: CustomerDetailModel
    lateinit var suppDetailModel: SupplierDetailModel

    var customerID: String? = ""
    var supplierID: String? = ""

    // tds fields
    var selectedDeductorType: String? = ""
    var selectedDeductorTypeValue: String? = ""
    var is_tds_applicable: String? = ""

    // tcs fields
    var selectedCollectorType: String? = ""
    var selectedCollectorTypeValue: String? = ""
    var is_tcs_applicable: String? = ""
    var isFromCustEdit: Boolean = false // true -> edit cust false -> edit supp


    var natureOfGoodsList: List<NatureOfGoodsModel.DataNatureGood>? = null
    var natureOfGoodsNameList: List<String>? = null
    var selectedNatureGoodsID: String? = null
    var selectedNOGType: String? = ""

    var natureOfPaymentList: List<NatureOfPaymentModel.DataNaturePayment>? = null
    var natureOfPaymentNameList: List<String>? = null
    var selectedNaturePaymentID: String? = null
    var selectedNOPType: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_tds_details)
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
            if (intent.extras != null && intent.extras?.containsKey(Constants.isFromEditCustAddress)!!) {
                isFromCustEdit = intent.getBooleanExtra(Constants.isFromEditCustAddress, true)
                when (isFromCustEdit) {
                    // cust edit
                    true -> {
                        if (intent.extras != null && intent.extras?.containsKey(Constants.CUST_TCS_TDS_EDIT)!!) {
                            var customer_str: String? =
                                intent.getStringExtra(Constants.CUST_TCS_TDS_EDIT)
                            custDetailModel = Gson().fromJson(
                                customer_str,
                                CustomerDetailModel::class.java
                            )

                            customerID = custDetailModel.customers?.id
                            setCustData(custDetailModel)

                        }

                    }
                    // supp edit
                    false -> {
                        if (intent.extras != null && intent.extras?.containsKey(Constants.SUPP_TCS_TDS_EDIT)!!) {
                            var customer_str: String? =
                                intent.getStringExtra(Constants.SUPP_TCS_TDS_EDIT)
                            suppDetailModel = Gson().fromJson(
                                customer_str,
                                SupplierDetailModel::class.java
                            )

                            supplierID = suppDetailModel.vendors?.id
                            setSupplierData(suppDetailModel)
                        }

                    }
                }

            }
            getTcsTdsShareDataFromPref()
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun getTcsTdsShareDataFromPref() {
        if (prefs.contains(Constants.PREF_TCS_TDS_SHARE_DATA)) {
            val selectedTcsTdsDetails =
                object : TypeToken<TcsTdsShareDataModel>() {}.type
            tcsTdsShareDataModel = Gson().fromJson(
                prefs[Constants.PREF_TCS_TDS_SHARE_DATA, ""],
                selectedTcsTdsDetails
            )
            is_tcs_applicable = tcsTdsShareDataModel.is_tcs_applicable
            is_tds_applicable = tcsTdsShareDataModel.is_tds_applicable
            selectedDeductorType = tcsTdsShareDataModel.selectedDeductorType
            selectedNOGType = tcsTdsShareDataModel.selectedNogType
            selectedCollectorType = tcsTdsShareDataModel.selectedCollectorType
            selectedNOPType = tcsTdsShareDataModel.selectedNopType
            selectedNaturePaymentID = tcsTdsShareDataModel.selectedNatureofPaymentID
            selectedNatureGoodsID = tcsTdsShareDataModel.selectedNatureofGoodsID

            selectedDeductorTypeValue = getDeductorNameFromkey(selectedDeductorType!!)
            selectedCollectorTypeValue = getCollectorNameFromkey(selectedCollectorType!!)

            txtTCSCollectorType.setText(selectedCollectorTypeValue)

            txtTCSNatureGoods.setText(selectedNOGType)

            txtTDSDeductType.setText(selectedDeductorTypeValue)

            txtTDSPayment.setText(selectedNOPType)
        }
    }


    private fun setSupplierData(suppDetailModel: SupplierDetailModel?) {
        // tds checkbox enable/disable condition
        is_tds_applicable = suppDetailModel!!.vendors!!.is_tds_applicable
        when (suppDetailModel!!.vendors!!.is_tds_applicable.equals("1")) {
            true -> {
                radioTDSNewCustY.isChecked = true
                radioTDSNewCustN.isChecked = false
            }
            false -> {
                radioTDSNewCustN.isChecked = true
                radioTDSNewCustY.isChecked = false
            }
        }
        selectedDeductorTypeValue =
            getDeductorNameFromkey(suppDetailModel.vendors!!.tax_deductor_type!!)
        txtTDSDeductType.setText(selectedDeductorTypeValue)
        selectedDeductorType = suppDetailModel.vendors.tax_deductor_type
        txtTDSPayment.setText(suppDetailModel.vendors.nature_of_payment_name)
        selectedNOPType = suppDetailModel.vendors.nature_of_payment_name
        selectedNaturePaymentID = suppDetailModel.vendors.nature_of_payment_id


        // tcs checkbox enable/disable condition
        is_tcs_applicable = suppDetailModel.vendors.is_tcs_applicable
        when (suppDetailModel.vendors.is_tcs_applicable.equals("1")) {
            true -> {
                radioTCSNewCustY.isChecked = true
                radioTCSNewCustN.isChecked = false
            }
            false -> {
                radioTCSNewCustN.isChecked = true
                radioTCSNewCustY.isChecked = false
            }
        }
        selectedCollectorTypeValue =
            getCollectorNameFromkey(suppDetailModel.vendors.tax_collector_type!!)
        txtTCSCollectorType.setText(selectedCollectorTypeValue)
        selectedCollectorType = suppDetailModel.vendors.tax_collector_type
        txtTCSNatureGoods.setText(suppDetailModel.vendors.nature_of_good_name)
        selectedNOGType = suppDetailModel.vendors.nature_of_good_name
        selectedNatureGoodsID = suppDetailModel.vendors.nature_of_good_id

        // if data changeg then get updated data from pref.
        getTcsTdsShareDataFromPref()
    }

    private fun setCustData(custDetailModel: CustomerDetailModel?) {
        // tds checkbox enable/disable condition
        is_tds_applicable = custDetailModel!!.customers!!.is_tds_applicable
        when (custDetailModel!!.customers!!.is_tds_applicable.equals("1")) {
            true -> {
                radioTDSNewCustY.isChecked = true

            }
            false -> {
                radioTDSNewCustN.isChecked = true
            }
        }
        selectedDeductorTypeValue =
            getDeductorNameFromkey(custDetailModel.customers!!.tax_deductor_type!!)
        txtTDSDeductType.setText(selectedDeductorTypeValue)
        selectedDeductorType = custDetailModel.customers!!.tax_deductor_type
        txtTDSPayment.setText(custDetailModel.customers.nature_of_payment_name)
        selectedNOPType = custDetailModel.customers.nature_of_payment_name
        selectedNaturePaymentID = custDetailModel.customers.nature_of_payment_id

        // tcs checkbox enable/disable condition
        is_tcs_applicable = custDetailModel.customers.is_tcs_applicable
        when (custDetailModel.customers.is_tcs_applicable.equals("1")) {
            true -> {
                radioTCSNewCustY.isChecked = true
            }
            false -> {
                radioTCSNewCustN.isChecked = true
            }
        }
        selectedCollectorTypeValue =
            getCollectorNameFromkey(custDetailModel.customers.tax_collector_type!!)
        txtTCSCollectorType.setText(selectedCollectorTypeValue)
        selectedCollectorType = custDetailModel.customers.tax_collector_type
        txtTCSNatureGoods.setText(custDetailModel.customers.nature_of_good_name)
        selectedNOGType = custDetailModel.customers.nature_of_good_name
        selectedNatureGoodsID = custDetailModel.customers.nature_of_good_id
        // if data changeg then get updated data from pref.
        getTcsTdsShareDataFromPref()
    }

    private fun gettcsCollectorTypeApi(token: String?) {
        if (NetworkUtils.isConnected()) {

            viewModel.gettcsCollectorTypeApi(token).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                setCollectorDropDownData(it.data.data)

                            } else {

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

    private fun gettdsDeductorTypeApi(token: String?) {
        if (NetworkUtils.isConnected()) {

            viewModel.gettdsDeductorTypeApi(token).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                setDeductorDropDownData(it.data.data)

                            } else {

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

    private fun setCollectorDropDownData(data: List<GetTcsCollectorTypeModel.Data>?) {
        fillDropDownCollectorType(data)

    }

    private fun setDeductorDropDownData(data: List<GetTcsCollectorTypeModel.Data>?) {
        fillDropDownDeductorType(data)

    }

    private fun fillDropDownDeductorType(data: List<GetTcsCollectorTypeModel.Data>?) {
        popupMenuDeductor = PopupMenu(this, binding.txtTDSDeductType)

        popupMenuDeductor.menu.add(Menu.NONE, 1, 1, data!!.get(0).artificial_juridical_person)
        popupMenuDeductor.menu.add(Menu.NONE, 2, 2, data.get(1).association_of_persons)
        popupMenuDeductor.menu.add(Menu.NONE, 3, 3, data.get(2).body_of_individuals)
        popupMenuDeductor.menu.add(Menu.NONE, 4, 4, data.get(3).company_non_resident)
        popupMenuDeductor.menu.add(Menu.NONE, 5, 5, data.get(4).company_resident)
        popupMenuDeductor.menu.add(Menu.NONE, 6, 6, data.get(5).cooperative_society)
        popupMenuDeductor.menu.add(Menu.NONE, 7, 7, data.get(6).government)
        popupMenuDeductor.menu.add(Menu.NONE, 8, 8, data.get(7).individual_huf_non_resident)
        popupMenuDeductor.menu.add(Menu.NONE, 9, 9, data.get(8).individual_huf_resident)
        popupMenuDeductor.menu.add(Menu.NONE, 10, 10, data.get(9).local_authority)
        popupMenuDeductor.menu.add(Menu.NONE, 11, 11, data.get(10).partnership_firm)

        binding.txtTDSDeductType.clickWithDebounce {

            openDeductorType(binding.txtTCSCollectorType)
        }

    }

    private fun fillDropDownCollectorType(data: List<GetTcsCollectorTypeModel.Data>?) {
        popupMenuCollector = PopupMenu(this, binding.txtTCSCollectorType)

        popupMenuCollector.menu.add(Menu.NONE, 1, 1, data!!.get(0).artificial_juridical_person)
        popupMenuCollector.menu.add(Menu.NONE, 2, 2, data.get(1).association_of_persons)
        popupMenuCollector.menu.add(Menu.NONE, 3, 3, data.get(2).body_of_individuals)
        popupMenuCollector.menu.add(Menu.NONE, 4, 4, data.get(3).company_non_resident)
        popupMenuCollector.menu.add(Menu.NONE, 5, 5, data.get(4).company_resident)
        popupMenuCollector.menu.add(Menu.NONE, 6, 6, data.get(5).cooperative_society)
        popupMenuCollector.menu.add(Menu.NONE, 7, 7, data.get(6).government)
        popupMenuCollector.menu.add(Menu.NONE, 8, 8, data.get(7).individual_huf_non_resident)
        popupMenuCollector.menu.add(Menu.NONE, 9, 9, data.get(8).individual_huf_resident)
        popupMenuCollector.menu.add(Menu.NONE, 10, 10, data.get(9).local_authority)
        popupMenuCollector.menu.add(Menu.NONE, 11, 11, data.get(10).partnership_firm)

        binding.txtTCSCollectorType.clickWithDebounce{

            openCollectorType(binding.txtTCSCollectorType)
        }


    }

    private fun getDeductorNameFromkey(selectedDedType: String): String? {
        when (selectedDedType) {
            "artificial_juridical_person" -> {
                selectedDeductorTypeValue = "Artificial Juridical Person"
            }
            "association_of_persons" -> {
                selectedDeductorTypeValue = "Association Of Persons"
            }
            "body_of_individuals" -> {
                selectedDeductorTypeValue = "Body Of Individuals"

            }
            "company_non_resident" -> {
                selectedDeductorTypeValue = "Company Non Resident"
            }
            "company_resident" -> {
                selectedDeductorTypeValue = "Company Resident"
            }
            "cooperative_society" -> {
                selectedDeductorTypeValue = "Cooperative Society"
            }
            "government" -> {
                selectedDeductorTypeValue = "Government"

            }
            "individual_huf_non_resident" -> {
                selectedDeductorTypeValue = "Individual Huf Non Resident"
            }
            "individual_huf_resident" -> {
                selectedDeductorTypeValue = "Individual Huf Resident"
            }
            "local_authority" -> {
                selectedDeductorTypeValue = "Local Authority"
            }
            "partnership_firm" -> {
                selectedDeductorTypeValue = "Partnership Firm"

            }

        }
        return selectedDeductorTypeValue
    }

    private fun getCollectorNameFromkey(selectedCollType: String): String? {
        when (selectedCollType) {
            "artificial_juridical_person" -> {
                selectedCollectorTypeValue = "Artificial Juridical Person"
            }
            "association_of_persons" -> {
                selectedCollectorTypeValue = "Association Of Persons"
            }
            "body_of_individuals" -> {
                selectedCollectorTypeValue = "Body Of Individuals"

            }
            "company_non_resident" -> {
                selectedCollectorTypeValue = "Company Non Resident"
            }
            "company_resident" -> {
                selectedCollectorTypeValue = "Company Resident"
            }
            "cooperative_society" -> {
                selectedCollectorTypeValue = "Cooperative Society"
            }
            "government" -> {
                selectedCollectorTypeValue = "Government"

            }
            "individual_huf_non_resident" -> {
                selectedCollectorTypeValue = "Individual Huf Non Resident"
            }
            "individual_huf_resident" -> {
                selectedCollectorTypeValue = "Individual Huf Resident"
            }
            "local_authority" -> {
                selectedCollectorTypeValue = "Local Authority"
            }
            "partnership_firm" -> {
                selectedCollectorTypeValue = "Partnership Firm"

            }

        }
        return selectedCollectorTypeValue

    }

    private fun openDeductorType(view: View) {
        popupMenuDeductor.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when (item.itemId.toString()) {
                "1" -> {
                    selectedDeductorType = "artificial_juridical_person"
                }
                "2" -> {
                    selectedDeductorType = "association_of_persons"
                }
                "3" -> {
                    selectedDeductorType = "body_of_individuals"

                }
                "4" -> {
                    selectedDeductorType = "company_non_resident"
                }
                "5" -> {
                    selectedDeductorType = "company_resident"
                }
                "6" -> {
                    selectedDeductorType = "cooperative_society"
                }
                "7" -> {
                    selectedDeductorType = "government"

                }
                "8" -> {
                    selectedDeductorType = "individual_huf_non_resident"
                }
                "9" -> {
                    selectedDeductorType = "individual_huf_resident"
                }
                "10" -> {
                    selectedDeductorType = "local_authority"
                }
                "11" -> {
                    selectedDeductorType = "partnership_firm"

                }

            }

            binding.txtTDSDeductType.setText(item.title)
            true

        })

        popupMenuDeductor.show()


    }

    private fun openCollectorType(view: View) {
        popupMenuCollector.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when (item.itemId.toString()) {
                "1" -> {
                    selectedCollectorType = "artificial_juridical_person"
                }
                "2" -> {
                    selectedCollectorType = "association_of_persons"
                }
                "3" -> {
                    selectedCollectorType = "body_of_individuals"

                }
                "4" -> {
                    selectedCollectorType = "company_non_resident"
                }
                "5" -> {
                    selectedCollectorType = "company_resident"
                }
                "6" -> {
                    selectedCollectorType = "cooperative_society"
                }
                "7" -> {
                    selectedCollectorType = "government"

                }
                "8" -> {
                    selectedCollectorType = "individual_huf_non_resident"
                }
                "9" -> {
                    selectedCollectorType = "individual_huf_resident"
                }
                "10" -> {
                    selectedCollectorType = "local_authority"
                }
                "11" -> {
                    selectedCollectorType = "partnership_firm"

                }

            }

            binding.txtTCSCollectorType.setText(item.title)
            true

        })

        popupMenuCollector.show()


    }

    private fun getNatureofGoods() {
        if (NetworkUtils.isConnected()) {
            viewModel.getNatureofGoods(loginModel?.data?.bearer_access_token)
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {

                                if (it.data?.status == true) {
                                    natureOfGoodsList = it.data.data

                                    natureOfGoodsNameList = natureOfGoodsList?.map { it.name }

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

                            }
                            Status.LOADING -> {

                            }
                        }
                    }
                })
        }

    }

    private fun getNatureofPayment() {
        if (NetworkUtils.isConnected()) {
            viewModel.getNatureOfPayment(loginModel?.data?.bearer_access_token)
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {

                                if (it.data?.status == true) {
                                    natureOfPaymentList = it.data.data

                                    natureOfPaymentNameList = natureOfPaymentList?.map { it.name }

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

                            }
                            Status.LOADING -> {

                            }
                        }
                    }
                })
        }

    }

    private fun openNatureGoodsMenu(natureOfGoodsNameList: List<String>?) {
        popupMenuNog = PopupMenu(this, txtTCSNatureGoods)
        for (i in 0 until natureOfGoodsNameList!!.size) {
            popupMenuNog.menu.add(
                Menu.NONE,
                i,
                i,
                natureOfGoodsNameList.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenuNog.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            txtTCSNatureGoods.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = natureOfGoodsNameList.indexOf(selected)

            selectedNatureGoodsID =
                pos?.let { it1 -> natureOfGoodsList?.get(it1)?.nature_of_goods }

            selectedNOGType = pos?.let { it1 -> natureOfGoodsList?.get(it1)?.name }

            // prefs[Constants.PREF_MULTIPLE_OPENINGSTOCK] = Gson().toJson(multipleOpeningStockList) //setter
            true
        })

        popupMenuNog.show()
    }

    private fun openNaturePaymentsMenu(natureOfPaymentNameList: List<String>?) {
        popupMenuNop = PopupMenu(this, txtTDSPayment)
        for (i in 0 until natureOfPaymentNameList!!.size) {
            popupMenuNop.menu.add(
                Menu.NONE,
                i,
                i,
                natureOfPaymentNameList.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenuNop.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            txtTDSPayment.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = natureOfPaymentNameList.indexOf(selected)

            selectedNaturePaymentID =
                pos?.let { it1 -> natureOfPaymentList?.get(it1)?.nature_of_payment }

            selectedNOPType = pos?.let { it1 -> natureOfPaymentList?.get(it1)?.name }

            // prefs[Constants.PREF_MULTIPLE_OPENINGSTOCK] = Gson().toJson(multipleOpeningStockList) //setter
            true
        })

        popupMenuNop.show()
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                CustSuppTcsTdsViewModel::class.java
            )

        binding.setLifecycleOwner(this)
    }

    private fun setupUIandListner() {
        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.tds_tcs_detail)


        getLoginModelFromPrefs()
        // enable tcs = 1 then call related apis same for enable tds
        if (loginModel.data!!.company_info!!.tax_settings!!.enable_tcs.equals("1")
        ) {
            cardTcs.visibility = View.VISIBLE
            gettcsCollectorTypeApi(loginModel.data?.bearer_access_token)
            getNatureofGoods()
            is_tcs_applicable = "1"
        } else {
            cardTcs.visibility = View.GONE
            is_tcs_applicable = "0"
        }
        // enable_tds
        if (loginModel.data!!.company_info!!.tax_settings!!.enable_tds.equals("1")) {
            cardTDS.visibility = View.VISIBLE
            gettdsDeductorTypeApi(loginModel.data?.bearer_access_token)
            getNatureofPayment()
            is_tds_applicable = "1"
        } else {
            cardTDS.visibility = View.GONE
            is_tds_applicable = "0"
        }

        btnSaveTDSTCSMGMT?.clickWithDebounce {

            if (performValidation()) {
                val tcsTdsShareDataModel = TcsTdsShareDataModel(
                    is_tcs_applicable,
                    is_tds_applicable,
                    selectedDeductorType,
                    selectedCollectorType,
                    selectedNOGType,
                    selectedNOPType,
                    selectedNaturePaymentID,
                    selectedNatureGoodsID
                )

                val prefs = PreferenceHelper.defaultPrefs(this)
                prefs[Constants.PREF_TCS_TDS_SHARE_DATA] =
                    Gson().toJson(tcsTdsShareDataModel) //setter
                finish()
            }

        }

        imgLeft?.clickWithDebounce {
            onBackPressed()
        }
        txtTDSPayment?.clickWithDebounce {
            openNaturePaymentsMenu(natureOfPaymentNameList)
        }
        txtTCSNatureGoods?.clickWithDebounce {
            openNatureGoodsMenu(natureOfGoodsNameList)
        }

        radiogroupTDSNewCust.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                radioTDSNewCustY.id -> {
                    is_tds_applicable = "1"
                    tvTDSDeductType.visibility = View.VISIBLE
                    tvTDSPayment.visibility = View.VISIBLE
                    when (isFromCustEdit) {
                        true -> {
                            // edit cust
                            if (this::custDetailModel.isInitialized) {
                                txtTDSDeductType.setText(custDetailModel.customers!!.tax_deductor_type)
                                txtTDSPayment.setText(custDetailModel.customers!!.nature_of_payment_name)
                                selectedNaturePaymentID =
                                    custDetailModel.customers!!.nature_of_payment_id
                            }
                        }
                        // edit supp
                        false -> {
                            if (this::suppDetailModel.isInitialized) {
                                txtTDSDeductType.setText(suppDetailModel.vendors!!.tax_deductor_type)
                                txtTDSPayment.setText(suppDetailModel.vendors!!.nature_of_payment_name)
                                selectedNaturePaymentID =
                                    suppDetailModel.vendors!!.nature_of_payment_id
                            }

                        }
                    }


                }
                radioTDSNewCustN.id -> {
                    is_tds_applicable = "0"
                    selectedNOPType = ""
                    selectedNaturePaymentID = ""
                    tvTDSDeductType.visibility = View.GONE
                    tvTDSPayment.visibility = View.GONE
                }
            }
        })
        radiogroupTCSNewCust.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                radioTCSNewCustY.id -> {
                    is_tcs_applicable = "1"
                    tvTCSDeductType.visibility = View.VISIBLE
                    tvTCSNatureGoods.visibility = View.VISIBLE
                    when (isFromCustEdit) {
                        true -> {
                            // edit cust
                            if (this::custDetailModel.isInitialized) {
                                txtTCSCollectorType.setText(custDetailModel.customers!!.tax_collector_type)
                                txtTCSNatureGoods.setText(custDetailModel.customers!!.nature_of_good_name)
                                selectedNatureGoodsID =
                                    custDetailModel.customers!!.nature_of_good_id
                            }
                        }
                        // edit supp
                        false -> {
                            if (this::suppDetailModel.isInitialized) {
                                txtTCSCollectorType.setText(suppDetailModel.vendors!!.tax_collector_type)
                                txtTCSNatureGoods.setText(suppDetailModel.vendors!!.nature_of_good_name)
                                selectedNatureGoodsID = suppDetailModel.vendors!!.nature_of_good_id
                            }

                        }
                    }
                }
                radioTCSNewCustN.id -> {
                    is_tcs_applicable = "0"
                    selectedNOGType = ""
                    selectedNatureGoodsID = ""
                    tvTCSDeductType.visibility = View.GONE
                    tvTCSNatureGoods.visibility = View.GONE
                }
            }
        })

    }

    private fun getLoginModelFromPrefs() {
        prefs = PreferenceHelper.defaultPrefs(applicationContext!!)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter
    }

    fun performValidation(): Boolean {
        if (is_tds_applicable.equals("1") && selectedDeductorType.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.tds_deductor_error_msg))
            return false
        } else if (is_tds_applicable.equals("1") && selectedNOPType.isNullOrBlank()) {

            CommonUtils.showDialog(this, getString(R.string.tds_nop_error_msg))
            return false
        }
        if (is_tcs_applicable.equals("1") && selectedCollectorType.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.tcs_collector_error_msg))
            return false
        } else if (is_tcs_applicable.equals("1") && selectedNOGType.isNullOrBlank()) {

            CommonUtils.showDialog(this, getString(R.string.tcs_nog_error_msg))
            return false
        } else if (is_tds_applicable.equals("0") && is_tcs_applicable.equals("0")) { // no radio buttons are checked
            return true
        }
        return true
    }
}