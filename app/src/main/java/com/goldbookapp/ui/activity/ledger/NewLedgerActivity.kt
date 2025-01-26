package com.goldbookapp.ui.activity.ledger

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
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
import com.goldbookapp.databinding.ActivityNewLedgerBinding
import com.goldbookapp.model.*
import com.goldbookapp.searchablespinner.SearchableSpinner
import com.goldbookapp.ui.activity.group.NewGroupActivity
import com.goldbookapp.ui.activity.viewmodel.NewLedgerViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.Status
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_new_ledger.*
import kotlinx.android.synthetic.main.toolbar_layout.*


class NewLedgerActivity : AppCompatActivity() {

    lateinit var binding: ActivityNewLedgerBinding
    private lateinit var viewModel: NewLedgerViewModel
    lateinit var chequeList: ArrayList<AddChequeBookModel.AddChequeBookModelItem>
    lateinit var popupMenu: PopupMenu
    lateinit var loginModel: LoginModel
    lateinit var prefs: SharedPreferences
    var is_bank_account: String? = ""
    var is_duties_and_taxes: String? = "0"
    var is_sub_account: String? = "0"
    var selectedBill: String? = "0"
    var selectedDutyType: String? = null
    var selectedGSTType: String? = null
    var selectedGSTTreatment: String? = null
    var selectedOpeningBalType: String? = null
    var group_id: String? = ""
    var groupFromSubGroup: String? = null
    var is_enable_cheque_reg: String? = null
    var is_enable_tcs: String? = null
    var is_enable_tds: String? = null
    var is_enable_gst: String? = null
    var is_tcs_applicable: String? = "0"
    var is_tds_applicable: String? = "0"

    var natureOfGoodsList: List<NatureOfGoodsModel.DataNatureGood>? = null
    var natureOfGoodsNameList: List<String>? = null
    var selectedNatureGoodsID: String? = null

    var natureOfPaymentList: List<NatureOfPaymentModel.DataNaturePayment>? = null
    var natureOfPaymentNameList: List<String>? = null
    var selectedNaturePaymentID: String? = null

    var groupSubGroupList: List<LedgerGroupSubGroupModel.DataLedgerGroupSubGroup>? = null
    var groupSubGroupNameList: List<String>? = null
    var selectedGroupSubGroupID: String? = null

    var groupList: List<ParentGroupModel.DataParentGroup>? = null
    var groupNameList: List<String>? = null
    var selectedGroupID: String? = null
    var chequeArray: String? = ""
    var isGroupEnable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_ledger)

        setupViewModel()
        setupUIandListner()
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                NewLedgerViewModel::class.java
            )
        binding.setLifecycleOwner(this)
    }

    private fun setupUIandListner() {
        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        )

        if (prefs.contains(Constants.PREF_CHEQUE_BOOK_KEY)) {
            prefs.edit().remove(Constants.PREF_CHEQUE_BOOK_KEY).apply()
        }

        is_enable_cheque_reg =
            loginModel.data?.company_info?.general_settings?.enable_cheque_reg_for_bank_acc.toString()

        is_enable_tcs = loginModel.data?.company_info?.tax_settings?.enable_tcs.toString()
        is_enable_tds = loginModel.data?.company_info?.tax_settings?.enable_tds.toString()
        is_enable_gst = loginModel.data?.company_info?.tax_settings?.enable_gst.toString()

        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.new_ledger)
        tvRight.setText(R.string.save)
        tvRight.visibility = GONE


        radiogroupBillNewLedger.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                radioBillNewLedgerN.id -> {
                    selectedBill = "0"
                }
                radioBillNewLedgerY.id -> {
                    selectedBill = "1"
                }
            }
        })

        imgLeft?.clickWithDebounce {
            onBackPressed()
        }

        tv_set_alter_cheque?.clickWithDebounce {
            startActivity(
                Intent(
                    this,
                    ChequeRegisterActivity::class.java
                ).putExtra(Constants.IS_FROM_NEW_CHEQUE, "1")
            )
        }

        txtTCSNatureLedger?.clickWithDebounce {
            openNatureGoodsMenu(natureOfGoodsNameList)
        }

        txtTDSNatureLedger?.clickWithDebounce {
            openNaturePaymentsMenu(natureOfPaymentNameList)
        }

        txtGroupNewLedger?.clickWithDebounce {
            openGroupMenu(groupNameList)
        }


        txtDutyNewLedger?.clickWithDebounce {
            openDutyMenu()
        }

        txtGSTNewLedger?.clickWithDebounce {
            openGSTMenu()
        }

        txtGstTreatmentNewLedger?.clickWithDebounce {
            openGSTTreatmentMenu()
        }

        txtOpenBalTypeNewLedger?.clickWithDebounce {
            openOpeningBalTypeMenu()
        }

        imgGroupAdd?.clickWithDebounce {
            when(isGroupEnable){
                true->{
                    startActivity(Intent(this, NewGroupActivity::class.java))
                }
                else->{

                }
            }

        }



        btnSaveAdd_AddLedger.clickWithDebounce {
            if (performValidation()) {
                if (NetworkUtils.isConnected()) {

                    addLedgerDetails(
                        loginModel?.data?.bearer_access_token,
                        txtNameNewLedger.text.toString().trim(),
                        txtUniqueCodeNewLedger.text.toString().trim(),
                        is_sub_account,
                        selectedGroupID,
                        selectedGroupSubGroupID,
                        is_bank_account,
                        txtBankNameNewLedger.text.toString().trim(),
                        txtAccNoNewLedger.text.toString().trim(),
                        txtIFSCodeNewLedger.text.toString().trim(),
                        txtBranchNameNewLedger.text.toString().trim(),
                        is_duties_and_taxes,
                        selectedDutyType,
                        is_tcs_applicable,
                        is_tds_applicable,
                        selectedNatureGoodsID,
                        selectedNaturePaymentID,
                        selectedGSTType,
                        txtDutyPercantageNewLedger.text.toString().trim(),
                        selectedBill,
                        txtPanCardNewLedger.text.toString().trim(),
                        selectedGSTTreatment,
                        txtGstINNewLedger.text.toString().trim(),
                        txtRemarksNewLedger.text.toString().trim(),
                        selectedOpeningBalType,
                        txtOpenBalNewLedger.text.toString().trim(),
                        chequeArray
                    )
                }
            }
        }

    }


    fun performValidation(): Boolean {
        if (txtUniqueCodeNewLedger.text.toString().isBlank()) {
            CommonUtils.showDialog(
                this,
                getString(R.string.uniquecode_ledger_msg)/*"Please Enter Code"*/
            )
            txtUniqueCodeNewLedger.requestFocus()
            return false
        } else if (txtNameNewLedger.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.name_ledger_msg))
            txtNameNewLedger.requestFocus()
            return false
        } else if (txtGroupNewLedger.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.group_ledger_msg))
            txtGroupNewLedger.requestFocus()
            return false
        } else if (is_bank_account == "1" && txtBankNameNewLedger.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.bank_ledger_msg))
            txtBankNameNewLedger.requestFocus()
            return false
        } else if (is_bank_account == "1" && txtAccNoNewLedger.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.account_ledger_msg))
            txtAccNoNewLedger.requestFocus()
            return false
        } else if (is_bank_account == "1" && txtIFSCodeNewLedger.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.ifsc_ledger_msg))
            txtIFSCodeNewLedger.requestFocus()
            return false
        } else if (is_bank_account == "1" && txtBranchNameNewLedger.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.branch_ledger_msg))
            txtBranchNameNewLedger.requestFocus()
            return false
        } else if (is_duties_and_taxes == "1" && selectedDutyType.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.duty_ledger_msg))
            txtDutyNewLedger.requestFocus()
            return false
        } else if (selectedDutyType == "gst" && selectedGSTType.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.gst_ledger_msg))
            txtGSTNewLedger.requestFocus()
            return false
        } else if (is_duties_and_taxes == "1" && txtDutyPercantageNewLedger.text.toString()
                .isBlank()
        ) {
            CommonUtils.showDialog(this, getString(R.string.dutyPercentage_ledger_msg))
            txtDutyPercantageNewLedger.requestFocus()
            return false
        } else if ((is_duties_and_taxes == "1" && selectedDutyType == "tds") && selectedNaturePaymentID.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.tds_ledger_msg))
            txtTDSNatureLedger.requestFocus()
            return false
        } else if ((is_duties_and_taxes == "1" && (selectedDutyType == "tcs") && selectedNatureGoodsID.isNullOrBlank())) {
            CommonUtils.showDialog(this, getString(R.string.tcs_ledger_msg))
            txtTCSNatureLedger.requestFocus()
            return false
        } else if (!txtPanCardNewLedger.text.toString().isBlank() && !CommonUtils.isValidPANDetail(
                txtPanCardNewLedger.text.toString()
            )
        ) {
            CommonUtils.showDialog(this, getString(R.string.enter_correct_pandetails_msg))
            txtPanCardNewLedger.requestFocus()
            return false
        } else if (!txtGstINNewLedger.text.toString().isBlank() && !CommonUtils.isValidGSTNo(
                txtGstINNewLedger.text.toString()
            )
        ) {
            CommonUtils.showDialog(this, getString(R.string.enter_valid_gstin_msg))
            txtGstINNewLedger.requestFocus()
            return false
        }
        return true
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
            when (loginModel.data!!.user_info!!.user_type.equals("user", true)) {
                // user type user
                true -> {
                    // apply restriciton
                    defaultDisableAllButtonnUI()
                    userWiseRestriction(loginModel.data?.bearer_access_token)
                }
                false->{
                    defaultEnableAllButtonnUI()

                }
            }
            getGroup()
            getLedgerSubGroup(loginModel?.data?.bearer_access_token, selectedGroupID)
            getNatureofGoods()
            getNatureofPayment()
            //get cheque data from Pref
            if (prefs.contains(Constants.PREF_CHEQUE_BOOK_KEY)) {

                val collectionType =
                    object :
                        TypeToken<ArrayList<AddChequeBookModel.AddChequeBookModelItem>>() {}.type
                chequeList = Gson().fromJson(
                    prefs[Constants.PREF_CHEQUE_BOOK_KEY, ""],
                    collectionType
                )
                chequeArray = Gson().toJson(chequeList)
            }
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun defaultEnableAllButtonnUI() {
        isGroupEnable= true
    }

    private fun defaultDisableAllButtonnUI() {
        isGroupEnable= false
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
        for (i in 0 until data.permission!!.size) {
            if (data.permission!!.get(i).startsWith(getString(R.string.ledger_Group))) {
                // Restriction check for Ledger
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                        isGroupEnable = true
                    }
                    else->{

                    }
                }
            }
        }
    }




        private fun openNatureGoodsMenu(natureOfGoodsNameList: List<String>?) {
        selectedNatureGoodsID = null
        popupMenu = PopupMenu(this, txtTCSNatureLedger)
        for (i in 0 until natureOfGoodsNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                natureOfGoodsNameList.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            txtTCSNatureLedger.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = natureOfGoodsNameList.indexOf(selected)

            selectedNatureGoodsID =
                pos?.let { it1 -> natureOfGoodsList?.get(it1)?.nature_of_goods }.toString()

            // prefs[Constants.PREF_MULTIPLE_OPENINGSTOCK] = Gson().toJson(multipleOpeningStockList) //setter
            true
        })

        popupMenu.show()
    }


    private fun openNaturePaymentsMenu(natureOfPaymentNameList: List<String>?) {
        selectedNaturePaymentID = null
        popupMenu = PopupMenu(this, txtTDSNatureLedger)
        for (i in 0 until natureOfPaymentNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                natureOfPaymentNameList.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            txtTDSNatureLedger.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = natureOfPaymentNameList.indexOf(selected)

            selectedNaturePaymentID =
                pos?.let { it1 -> natureOfPaymentList?.get(it1)?.nature_of_payment }.toString()

            // prefs[Constants.PREF_MULTIPLE_OPENINGSTOCK] = Gson().toJson(multipleOpeningStockList) //setter
            true
        })

        popupMenu.show()
    }


    private fun openGroupMenu(groupNameList: List<String>?) {
        popupMenu = PopupMenu(this, txtGroupNewLedger)
        for (i in 0 until groupNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                groupNameList.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            txtGroupNewLedger.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = groupNameList.indexOf(selected)

            selectedGroupID =
                pos?.let { it1 -> groupList?.get(it1)?.ledger_group_id }.toString()

            getLedgerSubGroup(loginModel?.data?.bearer_access_token, selectedGroupID)
            // prefs[Constants.PREF_MULTIPLE_OPENINGSTOCK] = Gson().toJson(multipleOpeningStockList) //setter
            true
        })

        popupMenu.show()
    }


    private fun openDutyMenu() {
        selectedDutyType = null
        popupMenu = PopupMenu(this, txtDutyNewLedger)


        if (loginModel.data?.branch_info?.branch_type.equals("1", true)) { // GST branch
            if (is_enable_gst.equals("1")) {
                popupMenu.menu.add(Menu.NONE, 1, 1, "GST")
            }
        }

        if (is_enable_tcs.equals("1")) {
            popupMenu.menu.add(Menu.NONE, 2, 2, "TCS")
        }

        if (is_enable_tds.equals("1")) {
            popupMenu.menu.add(Menu.NONE, 3, 3, "TDS")
        }

        popupMenu.menu.add(Menu.NONE, 4, 4, "Other")

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtDutyNewLedger.setText(item.title)
            selectedDutyType = when (item.title) {
                "GST" -> {
                    tvGSTNewLedger.visibility = VISIBLE
                    if (is_enable_tcs.equals("1")) {
                        cardTCSNewLedger.visibility = VISIBLE
                        ly_tcsApplicable.visibility = VISIBLE
                        radiogroupTCSNewLedger.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
                            when (checkedId) {
                                radioTCSNewLedgerN.id -> {
                                    is_tcs_applicable = "0"
                                    tvTCSNatureNewLedger.visibility = GONE
                                }
                                radioTCSNewLedgerY.id -> {
                                    is_tcs_applicable = "1"
                                    tvTCSNatureNewLedger.visibility = VISIBLE
                                }
                            }
                        })
                    }
                    if (is_enable_tds.equals("1")) {
                        cardTDSNewLedger.visibility = VISIBLE
                        ly_tdsApplicable.visibility = VISIBLE
                        radiogroupTDSNewLedger.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
                            when (checkedId) {
                                radioTDSNewLedgerN.id -> {
                                    is_tds_applicable = "0"
                                    tvTDSNatureNewLedger.visibility = GONE
                                }
                                radioTDSNewLedgerY.id -> {
                                    is_tds_applicable = "1"
                                    tvTDSNatureNewLedger.visibility = VISIBLE
                                }
                            }
                        })
                    }
                    "gst"
                }
                "TCS" -> {
                    tvGSTNewLedger.visibility = GONE
                    cardTCSNewLedger.visibility = VISIBLE
                    ly_tcsApplicable.visibility = GONE
                    tvTCSNatureNewLedger.visibility = VISIBLE
                    cardTDSNewLedger.visibility = GONE
                    "tcs"
                }
                "TDS" -> {
                    tvGSTNewLedger.visibility = GONE
                    cardTDSNewLedger.visibility = VISIBLE
                    ly_tdsApplicable.visibility = GONE
                    tvTDSNatureNewLedger.visibility = VISIBLE
                    cardTCSNewLedger.visibility = GONE
                    "tds"
                }
                else -> {
                    tvGSTNewLedger.visibility = GONE
                    cardTDSNewLedger.visibility = GONE
                    cardTCSNewLedger.visibility = GONE
                    "other"
                }
            }
            true
        })
        popupMenu.show()
    }


    private fun openGSTMenu() {
        selectedGSTType = null
        popupMenu = PopupMenu(this, txtDutyNewLedger)

        popupMenu.menu.add(Menu.NONE, 1, 1, "CGST")
        popupMenu.menu.add(Menu.NONE, 2, 2, "SGST")
        popupMenu.menu.add(Menu.NONE, 3, 3, "IGST")
        popupMenu.menu.add(Menu.NONE, 4, 4, "CESS")

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtGSTNewLedger.setText(item.title)
            selectedGSTType = when (item.title) {
                "CGST" -> {
                    "cgst"
                }
                "SGST" -> {
                    "sgst"
                }
                "IGST" -> {
                    "igst"
                }
                else -> {
                    "cess"
                }
            }
            true
        })
        popupMenu.show()

    }


    private fun openGSTTreatmentMenu() {
        popupMenu = PopupMenu(this, txtGstTreatmentNewLedger)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Regular")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Unregistered")
        popupMenu.menu.add(Menu.NONE, 3, 3, "Consumer")
        popupMenu.menu.add(Menu.NONE, 4, 4, "Composition")

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtGstTreatmentNewLedger.setText(item.title)
            selectedGSTTreatment = when (item.title) {
                "Regular" -> {
                    "register"
                }
                "Unregistered" -> {
                    "unregister"
                }
                "Consumer" -> {
                    "consumer"
                }
                else -> {
                    "composite"
                }
            }
            true
        })
        popupMenu.show()

    }


    private fun openOpeningBalTypeMenu() {
        popupMenu = PopupMenu(this, txtOpenBalTypeNewLedger)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Credit")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Debit")


        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtOpenBalTypeNewLedger.setText(item.title)
            selectedOpeningBalType = when (item.title) {
                "Credit" -> {
                    "credit"
                }
                else -> {
                    "debit"
                }
            }
            true
        })
        popupMenu.show()

    }

    fun getNatureofGoods() {
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

    fun getNatureofPayment() {
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

    fun getLedgerSubGroup(token: String?, group_id: String?) {
        if (NetworkUtils.isConnected()) {
            viewModel.getLedgerGroupSubGroup(token, group_id)
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {

                                if (it.data?.status == true) {

                                    groupSubGroupList = it.data.data

                                    groupSubGroupNameList =
                                        groupSubGroupList?.map { it.sub_group_name }

                                    txtSubGroupNewLedger?.setItems(groupSubGroupNameList)

                                    txtSubGroupNewLedger.setOnItemSelectListener(object :
                                        SearchableSpinner.SearchableItemListener {
                                        override fun onItemSelected(view: View?, position: Int) {

                                            selectedGroupSubGroupID =
                                                position?.let { it1 -> groupSubGroupList?.get(it1)?.sub_group_id }
                                                    .toString()

                                            groupFromSubGroup =
                                                position?.let { it1 -> groupSubGroupList?.get(it1)?.group_name }

                                            selectedGroupID =
                                                position?.let { it1 -> groupSubGroupList?.get(it1)?.group_id.toString() }

                                            is_bank_account =
                                                position?.let { it1 -> groupSubGroupList?.get(it1)?.is_bank_account.toString() }
                                            is_duties_and_taxes =
                                                position?.let { it1 -> groupSubGroupList?.get(it1)?.is_duties_and_taxes.toString() }
                                            is_sub_account =
                                                position?.let { it1 -> groupSubGroupList?.get(it1)?.is_sub_account.toString() }

                                            if (is_bank_account == "1") {

                                                if (is_enable_cheque_reg == "1") {
                                                    cardBankDetailsNewLedger.visibility = VISIBLE
                                                    tv_set_alter_cheque.visibility = VISIBLE
                                                } else {
                                                    cardBankDetailsNewLedger.visibility = VISIBLE
                                                    tv_set_alter_cheque.visibility = GONE
                                                }
                                            } else {
                                                cardBankDetailsNewLedger.visibility = GONE
                                            }
                                            if (is_duties_and_taxes == "1") {
                                                cardDutyNewLedger.visibility = VISIBLE
                                            } else {
                                                cardDutyNewLedger.visibility = GONE
                                                cardTDSNewLedger.visibility = GONE
                                                cardTCSNewLedger.visibility = GONE
                                            }
                                            txtGroupNewLedger.setText(groupFromSubGroup)
                                        }

                                        override fun onSelectionClear() {

                                        }
                                    })


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

    fun getGroup() {
        if (NetworkUtils.isConnected()) {
            viewModel.getParentGroup(loginModel?.data?.bearer_access_token).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {

                            if (it.data?.status == true) {
                                groupList = it.data.data

                                groupNameList = groupList?.map { it.ledger_group_name }


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


    fun addLedgerDetails(
        token: String?,
        name: String?,
        code: String?,
        is_sub_account: String?,
        group_id: String?,
        sub_group_id: String?,
        is_bank_account: String?,
        bank_name: String?,
        account_number: String?,
        ifsc_code: String?,
        branch_name: String?,
        is_duties_and_taxes: String?,
        type_of_duty: String?,
        is_tcs_applicable: String?,
        is_tds_applicable: String?,
        nature_of_goods: String?,
        nature_of_payment: String?,
        type_of_gst: String?,
        percentage_of_duty: String?,
        bill_by_bill_reference: String?,
        pan_card: String?,
        gst_treatment: String?,
        gstin: String?,
        notes: String?,
        opening_balance_type: String?,
        opening_balance: String?,
        cheque_register_array: String?
    ) {
        viewModel.addLedgerDetails(
            token,
            name,
            code,
            is_sub_account,
            group_id,
            sub_group_id,
            is_bank_account,
            bank_name,
            account_number,
            ifsc_code,
            branch_name,
            is_duties_and_taxes,
            type_of_duty,
            is_tcs_applicable,
            is_tds_applicable,
            nature_of_goods,
            nature_of_payment,
            type_of_gst,
            percentage_of_duty,
            bill_by_bill_reference,
            pan_card,
            gst_treatment,
            gstin,
            notes,
            opening_balance_type,
            opening_balance,
            cheque_register_array
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
                            Toast.makeText(this, it.data?.errormessage?.message, Toast.LENGTH_LONG)
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
}