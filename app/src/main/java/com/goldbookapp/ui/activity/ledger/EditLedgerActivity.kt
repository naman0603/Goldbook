package com.goldbookapp.ui.activity.ledger

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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
import com.goldbookapp.databinding.ActivityEditLedgerBinding
import com.goldbookapp.model.*
import com.goldbookapp.searchablespinner.SearchableSpinner
import com.goldbookapp.ui.activity.group.NewGroupActivity
import com.goldbookapp.ui.activity.viewmodel.EditLedgerViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.Constants.Companion.PREF_CHEQUE_BOOK_EDITKEY
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.goldbookapp.utils.Status
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_edit_ledger.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class EditLedgerActivity : AppCompatActivity() {

    lateinit var binding: ActivityEditLedgerBinding
    private lateinit var viewModel: EditLedgerViewModel
    lateinit var ledgerDetailsModel: LedgerDetailsModel
    lateinit var popupMenu: PopupMenu
    lateinit var loginModel: LoginModel
    lateinit var prefs: SharedPreferences
    var is_bank_account: String? = "0"
    var is_duties_and_taxes: String? = "0"
    var is_sub_account: String? = "0"
    var selectedBill: String? = "0"
    var selectedDutyType: String? = null
    var selectedGSTType: String? = null
    var selectedOpeningBalType: String? = null
    var selectedGSTTreatment: String? = null
    var group_id: String? = ""
    var ledgerId: String? = ""
    var chequeArray: String? = null
    var is_enable_cheque_reg: String? = null
    var type_of_duty: String? = null

    var natureOfGoodsList: List<NatureOfGoodsModel.DataNatureGood>? = null
    var natureOfGoodsNameList: List<String>? = null
    var selectedNatureGoodsID: String? = null

    var natureOfPaymentList: List<NatureOfPaymentModel.DataNaturePayment>? = null
    var natureOfPaymentNameList: List<String>? = null
    var selectedNaturePaymentID: String? = null

    var groupSubGroupList: List<LedgerGroupSubGroupModel.DataLedgerGroupSubGroup>? = null
    var groupSubGroupNameList: List<String>? = null
    var selectedGroupSubGroupID: String? = null
    var groupFromSubGroup: String? = null

    var groupList: List<ParentGroupModel.DataParentGroup>? = null
    var groupNameList: List<String>? = null
    var selectedGroupID: String? = null

    var is_enable_tcs: String? = null
    var is_enable_tds: String? = null
    var is_enable_gst: String? = null
    var is_tcs_applicable: String? = "0"
    var is_tds_applicable: String? = "0"
    var isGroupEnable: Boolean = false
    lateinit var chequeList: ArrayList<AddChequeBookModel.AddChequeBookModelItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_ledger)

        setupViewModel()
        setupUIandListner()
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                EditLedgerViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }

    private fun setupUIandListner() {

        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        if (prefs.contains(Constants.PREF_CHEQUE_BOOK_EDITKEY)) {
            prefs.edit().remove(Constants.PREF_CHEQUE_BOOK_EDITKEY).apply()
        }
        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText("Edit Ledger Details")


        imgGroupEdit.clickWithDebounce {
            when(isGroupEnable){
                true->{
                    startActivity(Intent(this, NewGroupActivity::class.java))
                }
                else->{

                }
            }

        }

        btnSaveAddEditLedger.clickWithDebounce {
            if (performValidation()) {
                if (NetworkUtils.isConnected()) {

                    editLedgerDetails(
                        loginModel?.data?.bearer_access_token,
                        ledgerId,
                        txtNameEditLedger.text.toString().trim(),
                        txtUniqueCodeEditLedger.text.toString().trim(),
                        is_sub_account,
                        selectedGroupID,
                        selectedGroupSubGroupID,
                        is_bank_account,
                        txtBankNameEditLedger.text.toString().trim(),
                        txtAccNoEditLedger.text.toString().trim(),
                        txtIFSCodeEditLedger.text.toString().trim(),
                        txtBranchNameEditLedger.text.toString().trim(),
                        is_duties_and_taxes,
                        selectedDutyType,
                        is_tcs_applicable,
                        is_tds_applicable,
                        selectedNatureGoodsID,
                        selectedNaturePaymentID,
                        selectedGSTType,
                        txtDutyPercantageEditLedger.text.toString().trim(),
                        selectedBill,
                        txtPanCardEditLedger.text.toString().trim(),
                        selectedGSTTreatment,
                        txtGstINEditLedger.text.toString().trim(),
                        txtRemarksEditLedger.text.toString().trim(),
                        selectedOpeningBalType,
                        txtOpenBalEditLedger.text.toString().trim(),
                        chequeArray
                    )
                }
            }
        }
        is_enable_cheque_reg =
            loginModel.data?.company_info?.general_settings?.enable_cheque_reg_for_bank_acc.toString()

        if (intent.extras?.containsKey(Constants.LEDGER_DETAIL_KEY)!!) {
            var group_str: String? = intent.getStringExtra(Constants.LEDGER_DETAIL_KEY)
            var ledgerDetailsModel: LedgerDetailsModel = Gson().fromJson(
                group_str,
                LedgerDetailsModel::class.java
            )

            ledgerId = ledgerDetailsModel.data.ledger.ledgerData.id
            is_bank_account = ledgerDetailsModel.data.ledger.ledgerData.is_bank_account
            is_duties_and_taxes = ledgerDetailsModel.data.ledger.ledgerData.is_duties_and_taxes
            is_enable_tcs = ledgerDetailsModel.data.ledger.enableTcs
            is_enable_tds = ledgerDetailsModel.data.ledger.enableTds
            is_enable_gst = ledgerDetailsModel.data.ledger.enableGst
            is_tcs_applicable = ledgerDetailsModel.data.ledger.ledgerData.is_tcs_applicable
            when (is_tcs_applicable.isNullOrBlank()) {
                true -> {
                    is_tcs_applicable = "0"
                }
                else->{

                }
            }
            is_tds_applicable = ledgerDetailsModel.data.ledger.ledgerData.is_tds_applicable
            when (is_tds_applicable.isNullOrBlank()) {
                true -> is_tds_applicable = "0"
                else->{

                }
            }

            if (is_bank_account == "1") {
                if (is_enable_cheque_reg.equals("1")) {
                    cardBankDetailsEditLedger.visibility = VISIBLE
                    tv_set_alter_cheque_edit.visibility = VISIBLE
                } else {
                    cardBankDetailsEditLedger.visibility = VISIBLE
                    tv_set_alter_cheque_edit.visibility = GONE
                }
            } else {
                cardBankDetailsEditLedger.visibility = GONE
            }
            if (is_duties_and_taxes == "1") {
                cardDutyEditLedger.visibility = VISIBLE
            } else {
                cardDutyEditLedger.visibility = GONE
            }


            type_of_duty = ledgerDetailsModel.data.ledger.ledgerData.type_of_duty
            if (type_of_duty == "gst") {
                tvGSTEditLedger.visibility = VISIBLE
                if (is_enable_tcs.equals("1")) {
                    cardTCSEditLedger.visibility = VISIBLE
                    if (is_tcs_applicable.equals("1")) {
                        radioTCSEditLedgerY.isChecked = true
                        tvTCSNatureEditLedger.visibility = VISIBLE
                    } else {
                        radioTCSEditLedgerN.isChecked = true
                        tvTCSNatureEditLedger.visibility = GONE
                    }
                } else {
                    cardTCSEditLedger.visibility = GONE
                }

                if (is_enable_tds.equals("1")) {
                    cardTDSEditLedger.visibility = VISIBLE
                    if (is_tds_applicable.equals("1")) {
                        radioTDSEditLedgerY.isChecked = true
                        tvTDSNatureEditLedger.visibility = VISIBLE

                    } else {
                        radioTDSEditLedgerN.isChecked = true
                        tvTDSNatureEditLedger.visibility = GONE
                    }
                } else {
                    cardTDSEditLedger.visibility = GONE
                }

            } else if (type_of_duty == "tcs") {
                tvGSTEditLedger.visibility = GONE
                cardTCSEditLedger.visibility = VISIBLE
                cardTDSEditLedger.visibility = GONE
                if (is_tcs_applicable.equals("1")) {
                    radioTCSEditLedgerY.isChecked = true
                    tvTCSNatureEditLedger.visibility = VISIBLE
                } else {
                    radioTCSEditLedgerN.isChecked = true
                    tvTCSNatureEditLedger.visibility = GONE
                }
            } else if (type_of_duty == "tds") {
                tvGSTEditLedger.visibility = GONE
                cardTDSEditLedger.visibility = VISIBLE
                cardTCSEditLedger.visibility = GONE
                if (is_tds_applicable.equals("1")) {
                    radioTDSEditLedgerY.isChecked = true
                    tvTDSNatureEditLedger.visibility = VISIBLE

                } else {
                    radioTDSEditLedgerN.isChecked = true
                    tvTDSNatureEditLedger.visibility = GONE
                }
            } else {
                tvGSTEditLedger.visibility = GONE
                cardTDSEditLedger.visibility = GONE
                cardTCSEditLedger.visibility = GONE
            }

            txtUniqueCodeEditLedger.setText(ledgerDetailsModel.data.ledger.ledgerData.code)
            txtNameEditLedger.setText(ledgerDetailsModel.data.ledger.ledgerData.name)
            txtGroupEditLedger.setText(ledgerDetailsModel.data.ledger.ledgerData.group_name)
            txtSubGroupEditLedger.is_from_edit = true
            txtSubGroupEditLedger.mLabelView?.setText(ledgerDetailsModel.data.ledger.ledgerData.sub_group_name)
            txtBankNameEditLedger.setText(ledgerDetailsModel.data.ledger.ledgerData.bank_name)
            txtAccNoEditLedger.setText(ledgerDetailsModel.data.ledger.ledgerData.account_number)
            txtIFSCodeEditLedger.setText(ledgerDetailsModel.data.ledger.ledgerData.ifsc_code)
            txtBranchNameEditLedger.setText(ledgerDetailsModel.data.ledger.ledgerData.branch_name)
            txtDutyEditLedger.setText(ledgerDetailsModel.data.ledger.ledgerData.type_of_duty.capitalize())
            txtGSTEditLedger.setText(ledgerDetailsModel.data.ledger.ledgerData.type_of_gst.capitalize())
            txtDutyPercantageEditLedger.setText(ledgerDetailsModel.data.ledger.ledgerData.percentage_of_duty)
            txtTCSNatureEditLedger.setText(ledgerDetailsModel.data.ledger.ledgerData.goods_name)
            txtTDSNatureEditLedger.setText(ledgerDetailsModel.data.ledger.ledgerData.payment_name)
            txtPanCardEditLedger.setText(ledgerDetailsModel.data.ledger.ledgerData.pan_card)
            selectedBill = ledgerDetailsModel.data.ledger.ledgerData.bill_by_bill_reference
            if (ledgerDetailsModel.data.ledger.ledgerData.bill_by_bill_reference.equals("1")) {
                radioBillEditLedgerY.isChecked = true
            } else {
                radioBillEditLedgerN.isChecked = true
            }
            selectedGSTTreatment = ledgerDetailsModel.data.ledger.ledgerData.gst_treatment
            if (ledgerDetailsModel.data.ledger.ledgerData.gst_treatment.equals("register")) {
                txtGstTreatmentEditLedger.setText("Regular")
            } else if (ledgerDetailsModel.data.ledger.ledgerData.gst_treatment.equals("composite")) {
                txtGstTreatmentEditLedger.setText("Composition")
            } else if(ledgerDetailsModel.data.ledger.ledgerData.gst_treatment.equals("unregister")) {
                txtGstTreatmentEditLedger.setText("Unregistered")
            }else {
                txtGstTreatmentEditLedger.setText(ledgerDetailsModel.data.ledger.ledgerData.gst_treatment.capitalize())
            }

            txtGstINEditLedger.setText(ledgerDetailsModel.data.ledger.ledgerData.gstin)
            txtOpenBalEditLedger.setText(ledgerDetailsModel.data.ledger.ledgerData.opening_balance)
            txtOpenBalTypeEditLedger.setText(ledgerDetailsModel.data.ledger.ledgerData.opening_balance_type.capitalize())
            selectedOpeningBalType = ledgerDetailsModel.data.ledger.ledgerData.opening_balance_type
            txtRemarksEditLedger.setText(ledgerDetailsModel.data.ledger.ledgerData.notes)

            is_sub_account = ledgerDetailsModel.data.ledger.ledgerData.is_sub_account
            selectedGroupID = ledgerDetailsModel.data.ledger.ledgerData.group_id
            selectedGroupSubGroupID =
                ledgerDetailsModel.data.ledger.ledgerData.sub_group_id
            selectedDutyType = ledgerDetailsModel.data.ledger.ledgerData.type_of_duty
            selectedGSTType = ledgerDetailsModel.data.ledger.ledgerData.type_of_gst
            selectedNatureGoodsID = ledgerDetailsModel.data.ledger.ledgerData.nature_of_good_id
            selectedNaturePaymentID = ledgerDetailsModel.data.ledger.ledgerData.nature_of_payment_id


            if (ledgerDetailsModel.data.ledger.ledgerData.cheque_register.size > 0) {
                prefs[PREF_CHEQUE_BOOK_EDITKEY] =
                    Gson().toJson(ledgerDetailsModel.data.ledger.ledgerData.cheque_register) //setter
            }

        }
        imgLeft?.clickWithDebounce {
            onBackPressed()
        }

        radiogroupBillEditLedger.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                radioBillEditLedgerN.id -> {
                    selectedBill = "0"

                }
                radioBillEditLedgerY.id -> {
                    selectedBill = "1"

                }
            }
        })


        radiogroupTDSEditLedger.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                radioTDSEditLedgerN.id -> {
                    is_tds_applicable = "0"
                    tvTDSNatureEditLedger.visibility = GONE
                }
                radioTDSEditLedgerY.id -> {
                    is_tds_applicable = "1"
                    tvTDSNatureEditLedger.visibility = VISIBLE
                }
            }
        })

        radiogroupTCSEditLedger.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                radioTCSEditLedgerN.id -> {
                    is_tcs_applicable = "0"
                    tvTCSNatureEditLedger.visibility = GONE
                }
                radioTCSEditLedgerY.id -> {
                    is_tcs_applicable = "1"
                    tvTCSNatureEditLedger.visibility = VISIBLE
                }
            }
        })

        tv_set_alter_cheque_edit?.clickWithDebounce {
            startActivity(
                Intent(this, ChequeRegisterActivity::class.java).putExtra(
                    Constants.IS_FROM_NEW_CHEQUE, "0"
                )
            )
        }

        txtTCSNatureEditLedger?.clickWithDebounce {
            openNatureGoodsMenu(natureOfGoodsNameList)
        }

        txtTDSNatureEditLedger?.clickWithDebounce {
            openNaturePaymentsMenu(natureOfPaymentNameList)
        }

        txtGstTreatmentEditLedger?.clickWithDebounce {

            openGSTTreatmentMenu()
        }


        txtGroupEditLedger?.clickWithDebounce {
            openGroupMenu(groupNameList)
        }

        txtDutyEditLedger?.clickWithDebounce {
            openDutyMenu()
        }

        txtGSTEditLedger?.clickWithDebounce {
            openGSTMenu()
        }


        txtOpenBalTypeEditLedger?.clickWithDebounce {
            openOpeningBalTypeMenu()
        }

    }

    override fun onResume() {
        super.onResume()

        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent) {
                handleConnectivityChange()
            }
        })

        if (prefs.contains(Constants.PREF_CHEQUE_BOOK_EDITKEY)) {

            val collectionType =
                object :
                    TypeToken<ArrayList<AddChequeBookModel.AddChequeBookModelItem>>() {}.type
            chequeList =
                Gson().fromJson(prefs[Constants.PREF_CHEQUE_BOOK_EDITKEY, ""], collectionType)


            chequeArray = Gson().toJson(chequeList)

        }
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
            getLedgerGroupSubGroup(loginModel?.data?.bearer_access_token, selectedGroupID)
            getNatureofGoods()
            getNatureofPayment()
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



    fun performValidation(): Boolean {
        if (txtUniqueCodeEditLedger.text.toString().isBlank()) {
            CommonUtils.showDialog(
                this,
                getString(R.string.uniquecode_ledger_msg)/*"Please Enter Code"*/
            )
            txtUniqueCodeEditLedger.requestFocus()
            return false
        } else if (txtNameEditLedger.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.name_ledger_msg))
            txtNameEditLedger.requestFocus()
            return false
        } else if (txtGroupEditLedger.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.group_ledger_msg))
            txtGroupEditLedger.requestFocus()
            return false
        } else if (is_bank_account == "1" && txtBankNameEditLedger.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.bank_ledger_msg))
            txtBankNameEditLedger.requestFocus()
            return false
        } else if (is_bank_account == "1" && txtAccNoEditLedger.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.account_ledger_msg))
            txtAccNoEditLedger.requestFocus()
            return false
        } else if (is_bank_account == "1" && txtIFSCodeEditLedger.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.ifsc_ledger_msg))
            txtIFSCodeEditLedger.requestFocus()
            return false
        } else if (is_bank_account == "1" && txtBranchNameEditLedger.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.branch_ledger_msg))
            txtBranchNameEditLedger.requestFocus()
            return false
        } else if (is_duties_and_taxes == "1" && selectedDutyType.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.duty_ledger_msg))
            txtDutyEditLedger.requestFocus()
            return false
        } else if (selectedDutyType == "gst" && selectedGSTType.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.gst_ledger_msg))
            txtGSTEditLedger.requestFocus()
            return false
        } else if (is_duties_and_taxes == "1" && txtDutyPercantageEditLedger.text.toString()
                .isBlank()
        ) {
            CommonUtils.showDialog(this, getString(R.string.dutyPercentage_ledger_msg))
            txtDutyPercantageEditLedger.requestFocus()
            return false
        } else if ((is_duties_and_taxes == "1" && selectedDutyType == "tds") && selectedNaturePaymentID.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.tds_ledger_msg))
            txtTDSNatureEditLedger.requestFocus()
            return false
        } else if ((is_duties_and_taxes == "1" && (selectedDutyType == "tcs") && selectedNatureGoodsID.isNullOrBlank())) {
            CommonUtils.showDialog(this, getString(R.string.tcs_ledger_msg))
            txtTCSNatureEditLedger.requestFocus()
            return false
        } else if (!txtPanCardEditLedger.text.toString().isBlank() && !CommonUtils.isValidPANDetail(
                txtPanCardEditLedger.text.toString()
            )
        ) {
            CommonUtils.showDialog(this, getString(R.string.enter_correct_pandetails_msg))
            txtPanCardEditLedger.requestFocus()
            return false
        } else if (!txtGstINEditLedger.text.toString().isBlank() && !CommonUtils.isValidGSTNo(
                txtGstINEditLedger.text.toString()
            )
        ) {
            CommonUtils.showDialog(this, getString(R.string.enter_valid_gstin_msg))
            txtGstINEditLedger.requestFocus()
            return false
        }
        return true
    }


    private fun openGSTTreatmentMenu() {
        popupMenu = PopupMenu(this, txtGstTreatmentEditLedger)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Regular")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Unregistered")
        popupMenu.menu.add(Menu.NONE, 3, 3, "Consumer")
        popupMenu.menu.add(Menu.NONE, 4, 4, "Composition")

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtGstTreatmentEditLedger.setText(item.title)
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


    private fun openNatureGoodsMenu(natureOfGoodsNameList: List<String>?) {
        popupMenu = PopupMenu(this, txtTCSNatureEditLedger)
        for (i in 0 until natureOfGoodsNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                natureOfGoodsNameList.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            txtTCSNatureEditLedger.setText(item.title)
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
        popupMenu = PopupMenu(this, txtTDSNatureEditLedger)
        for (i in 0 until natureOfPaymentNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                natureOfPaymentNameList.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            txtTDSNatureEditLedger.setText(item.title)
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
        popupMenu = PopupMenu(this, txtGroupEditLedger)
        for (i in 0 until groupNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                groupNameList.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            txtGroupEditLedger.setText(item.title)
            val selected: String = item.title.toString()
            val pos: Int? = groupNameList.indexOf(selected)

            selectedGroupID =
                pos?.let { it1 -> groupList?.get(it1)?.ledger_group_id }.toString()

            getLedgerGroupSubGroup(loginModel?.data?.bearer_access_token, selectedGroupID)
            // prefs[Constants.PREF_MULTIPLE_OPENINGSTOCK] = Gson().toJson(multipleOpeningStockList) //setter
            true
        })

        popupMenu.show()
    }


    private fun openDutyMenu() {
        popupMenu = PopupMenu(this, txtDutyEditLedger)

        // popupMenu.getMenuInflater().inflate(R.menu.pop_up, popupMenu.getMenu());
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
            binding.txtDutyEditLedger.setText(item.title)
            selectedDutyType = when (item.title) {
                "GST" -> {
                    tvGSTEditLedger.visibility = View.VISIBLE
                    if (is_enable_tcs.equals("1")) {
                        cardTCSEditLedger.visibility = VISIBLE
                        ly_tcsApplicableEdit.visibility = VISIBLE
                        tvTCSNatureEditLedger.visibility = GONE
                        radiogroupTCSEditLedger.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
                            when (checkedId) {
                                radioTCSEditLedgerN.id -> {
                                    is_tcs_applicable = "0"
                                    tvTCSNatureEditLedger.visibility = GONE
                                }
                                radioTCSEditLedgerY.id -> {
                                    is_tcs_applicable = "1"
                                    tvTCSNatureEditLedger.visibility = VISIBLE
                                }
                            }
                        })
                    }
                    if (is_enable_tds.equals("1")) {
                        cardTDSEditLedger.visibility = VISIBLE
                        ly_tdsApplicableEdit.visibility = VISIBLE
                        tvTDSNatureEditLedger.visibility = GONE
                        radiogroupTDSEditLedger.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
                            when (checkedId) {
                                radioTDSEditLedgerN.id -> {
                                    is_tds_applicable = "0"
                                    tvTDSNatureEditLedger.visibility = GONE
                                }
                                radioTDSEditLedgerY.id -> {
                                    is_tds_applicable = "1"
                                    tvTDSNatureEditLedger.visibility = VISIBLE
                                }
                            }
                        })
                    }
                    "gst"
                }
                "TCS" -> {
                    tvGSTEditLedger.visibility = GONE
                    cardTCSEditLedger.visibility = VISIBLE
                    ly_tcsApplicableEdit.visibility = GONE
                    tvTCSNatureEditLedger.visibility = VISIBLE
                    cardTDSEditLedger.visibility = GONE
                    "tcs"
                }
                "TDS" -> {
                    tvGSTEditLedger.visibility = GONE
                    cardTDSEditLedger.visibility = VISIBLE
                    ly_tdsApplicableEdit.visibility = GONE
                    tvTDSNatureEditLedger.visibility = VISIBLE
                    cardTCSEditLedger.visibility = GONE
                    "tds"
                }
                else -> {
                    tvGSTEditLedger.visibility = GONE
                    cardTDSEditLedger.visibility = GONE
                    cardTCSEditLedger.visibility = GONE
                    "other"
                }
            }
            true
        })


        popupMenu.show()

    }


    private fun openGSTMenu() {
        popupMenu = PopupMenu(this, txtGSTEditLedger)

        popupMenu.menu.add(Menu.NONE, 1, 1, "CGST")
        popupMenu.menu.add(Menu.NONE, 2, 2, "SGST")
        popupMenu.menu.add(Menu.NONE, 3, 3, "IGST")
        popupMenu.menu.add(Menu.NONE, 4, 4, "CESS")

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtGSTEditLedger.setText(item.title)
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

    private fun openOpeningBalTypeMenu() {
        popupMenu = PopupMenu(this, txtOpenBalTypeEditLedger)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Credit")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Debit")


        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtOpenBalTypeEditLedger.setText(item.title)
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
                                /* Log.v("..setupObservers..", "..Success...")*/
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

    fun getLedgerGroupSubGroup(token: String?, group_id: String?) {
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
                                    txtSubGroupEditLedger?.setItems(groupSubGroupNameList)
                                    txtSubGroupEditLedger.setOnItemSelectListener(object :
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
                                                cardBankDetailsEditLedger.visibility = VISIBLE
                                            } else {
                                                cardBankDetailsEditLedger.visibility = GONE
                                            }
                                            if (is_duties_and_taxes == "1") {
                                                cardDutyEditLedger.visibility = VISIBLE
                                            } else {
                                                cardDutyEditLedger.visibility = GONE
                                                cardTCSEditLedger.visibility = GONE
                                                cardTDSEditLedger.visibility = GONE

                                            }

                                            txtGroupEditLedger.setText(groupFromSubGroup)
                                            Log.d(
                                                "selected",
                                                "" + is_bank_account + groupFromSubGroup
                                            )
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


    fun editLedgerDetails(
        token: String?,
        ledger_id: String?,
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
        viewModel.editLedgerDetails(
            token,
            ledger_id,
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
