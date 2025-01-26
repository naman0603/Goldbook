package com.goldbookapp.ui.activity.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.View
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
import com.goldbookapp.databinding.ActivityPreferencesBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.PreferenceDetailModel
import com.goldbookapp.ui.activity.viewmodel.PreferencesActivityViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import hk.ids.gws.android.sclick.SClick
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_preferences.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class PreferencesActivity : AppCompatActivity() {

    private var savePreferenceBtnShow: Boolean = false
    lateinit var popupMenu: PopupMenu
    lateinit var popupMenuPrintCopy: PopupMenu
    var enable_cheque_reg_for_bank_acc: Int = 0
    var round_off_for_sales: Int = 0
    lateinit var selectedTermBalanceID: String
    lateinit var selectedPrintCopyID: String
    var resID: Int = 0
    var printCopyResID = 0
    private lateinit var viewModel: PreferencesActivityViewModel
    lateinit var binding: ActivityPreferencesBinding

    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_preferences)
        setupUIandListner()
        setupViewModel()
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
            // userwise restric api call (for applying user restriction)
            when(loginModel.data!!.user_info!!.user_type.equals("user",true)){
                // user type user
                true -> {
                    // apply restriciton
                    defaultDisableAllButtonnUI()
                    if (intent.extras != null && intent.extras!!.containsKey(Constants.Change_Status)) {
                        savePreferenceBtnShow = intent.getBooleanExtra(Constants.Change_Status,false)
                        when(savePreferenceBtnShow){
                            true -> {
                                binding.btnSaveSettingPreferences.visibility = View.VISIBLE
                            }
                            false -> {
                                binding.btnSaveSettingPreferences.visibility = View.GONE
                            }
                        }
                    }
                }
                // user_type -> admin or super_admin or any other
                false -> {
                    defaultEnableAllButtonnUI()
                }
            }
            getdetailPreferenceApi(loginModel.data?.bearer_access_token)
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun defaultEnableAllButtonnUI() {
        binding.btnSaveSettingPreferences.visibility = View.VISIBLE
    }

    private fun defaultDisableAllButtonnUI() {
        binding.btnSaveSettingPreferences.visibility = View.GONE
    }

    private fun getdetailPreferenceApi(token: String?) {
        if(NetworkUtils.isConnected()) {

            viewModel.getdetailPreferenceApi(token).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                setdefaultPrefData(it.data.data)

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

    private fun setdefaultPrefData(data: PreferenceDetailModel.Data?) {
        when(data!!.enable_cheque_reg_for_bank_acc){
            // 1-> true //0-> false
            "1" -> binding.switchDisableCreditLimit.isChecked = true
            "0" -> binding.switchDisableCreditLimit.isChecked = false
        }
        when(data.round_off_for_sales){
            // 1-> true //0-> false
            "1" -> binding.SwitchStopTransaction.isChecked = true
            "0" -> binding.SwitchStopTransaction.isChecked = false
        }
        resID = resources.getIdentifier(
            data.default_term,
            "string", packageName
        )
        printCopyResID =  resources.getIdentifier(
            data.print_copies,
            "string", packageName
        )

        when(data.default_term){
            // 1-> true //0-> false
            "setting_debit_credit" -> {
                selectedTermBalanceID = "setting_debit_credit"
                binding.txtTermBalance.id = resID
                binding.txtTermBalance.setText(resources.getString(resID))
            }
            "setting_udhar_jama" -> {
                selectedTermBalanceID = "setting_udhar_jama"
                binding.txtTermBalance.id = resID
                binding.txtTermBalance.setText(resources.getString(resID))
            }
            "setting_receivable_payable" -> {
                selectedTermBalanceID = "setting_receivable_payable"
                binding.txtTermBalance.id = resID
                binding.txtTermBalance.setText(resources.getString(resID))
            }
            "setting_len_den" -> {
                selectedTermBalanceID = "setting_len_den"
                binding.txtTermBalance.id = resID
                binding.txtTermBalance.setText(resources.getString(resID))
            }
            else ->{
                selectedTermBalanceID = "setting_debit_credit"
               // binding.txtTermBalance.id = resID
               // binding.txtTermBalance.setText(resources.getString(resID))
            }
        }
        when(data.print_copies){
            // 1-> true //0-> false
            "setting_custom" -> {
                selectedPrintCopyID = "setting_custom"
                binding.txtPrintCopies.id = printCopyResID
                binding.txtPrintCopies.setText(resources.getString(printCopyResID))
            }
            "setting_one" -> {
                selectedPrintCopyID = "setting_one"
                binding.txtPrintCopies.id = printCopyResID
                binding.txtPrintCopies.setText(resources.getString(printCopyResID))
            }
            "setting_two" -> {
                selectedPrintCopyID = "setting_two"
                binding.txtPrintCopies.id = printCopyResID
                binding.txtPrintCopies.setText(resources.getString(printCopyResID))
            }
            "setting_three" -> {
                selectedPrintCopyID = "setting_three"
                binding.txtPrintCopies.id = printCopyResID
                binding.txtPrintCopies.setText(resources.getString(printCopyResID))
            }
            "setting_four" -> {
                selectedPrintCopyID = "setting_four"
                binding.txtPrintCopies.id = printCopyResID
                binding.txtPrintCopies.setText(resources.getString(printCopyResID))
            }
            else -> {
                selectedPrintCopyID = "setting_custom"

            }
        }

    }

    private fun setupUIandListner() {
        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.preferences)
       // tvRight.setText(R.string.save)
        getLoginModelFromPrefs()
        switchDisableCreditLimit.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                enable_cheque_reg_for_bank_acc = 1
            } else {
                enable_cheque_reg_for_bank_acc = 0
            }
        }
        SwitchStopTransaction.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                round_off_for_sales = 1
            } else {
                round_off_for_sales = 0
            }
        }
        imgLeft?.clickWithDebounce {

            onBackPressed()
        }
        selectedPrintCopyID = "setting_custom"

        btnSaveSettingPreferences?.clickWithDebounce {

            savedetailPreferenceApi(loginModel.data?.bearer_access_token,
             enable_cheque_reg_for_bank_acc,
             round_off_for_sales,
             selectedTermBalanceID,
             selectedPrintCopyID
            )
        }

        //fillDefaultBalance()
        fillDefaultPrintCopies()
        binding.txtTermBalance.clickWithDebounce {

            openDefaultTermPopup(binding.txtTermBalance)
        }
        binding.txtPrintCopies.clickWithDebounce {

            openDefaultPrintCopies(binding.txtPrintCopies)
        }


    }
    private fun savedetailPreferenceApi(
        token: String?,
        enable_cheque_reg_for_bank_acc: Int,
        round_off_for_sales: Int,
        selectedTermBalanceID: String,
        selectedPrintCopyID: String) {
        if(NetworkUtils.isConnected()) {

            viewModel.savePreferenceApi(token, enable_cheque_reg_for_bank_acc, round_off_for_sales, selectedTermBalanceID, selectedPrintCopyID).observe(this, Observer {
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

                                when(it.data!!.code == Constants.ErrorCode){
                                    true-> {
                                        Toast.makeText(
                                            this,
                                            it.data.errormessage?.message,
                                            Toast.LENGTH_LONG
                                        )
                                            .show()
                                    }
                                    false->{
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
    private fun getLoginModelFromPrefs() {
        prefs = PreferenceHelper.defaultPrefs(applicationContext!!)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                PreferencesActivityViewModel::class.java
            )

        binding.setLifecycleOwner(this)
    }

    private fun fillDefaultPrintCopies() {
        popupMenuPrintCopy = PopupMenu(this, binding.txtPrintCopies)
        popupMenuPrintCopy.menu.add(Menu.NONE, 1, 1, getString(R.string.setting_custom))
        popupMenuPrintCopy.menu.add(Menu.NONE, 2, 2, getString(R.string.setting_one))
        popupMenuPrintCopy.menu.add(Menu.NONE, 3, 3, getString(R.string.setting_two))
        popupMenuPrintCopy.menu.add(Menu.NONE, 4, 4, getString(R.string.setting_three))
        popupMenuPrintCopy.menu.add(Menu.NONE, 5, 5, getString(R.string.setting_four))
        binding.txtPrintCopies.setText(getString(R.string.setting_custom))
    }

    fun openDefaultTermPopup(view: View){
        val popupMenu: PopupMenu = PopupMenu(this, view)
        popupMenu.menu.add(
            Menu.NONE,
            1,
            1,
            getString(R.string.debit_credit)
        ) //add(groupId, itemId, order, title);
        popupMenu.menu.add(Menu.NONE, 2, 2,getString(R.string.udhar_jama))
        popupMenu.menu.add(Menu.NONE, 3, 3,R.string.rec_pay)
        popupMenu.menu.add(Menu.NONE, 4, 4,R.string.len_den)


        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when(item.itemId.toString()){
                // 1-> true //0-> false
                "1" -> {
                    selectedTermBalanceID = "setting_debit_credit"
                }
                "2" -> {
                    selectedTermBalanceID = "setting_udhar_jama"
                }
                "3" -> {
                    selectedTermBalanceID = "setting_receivable_payable"

                }
                "4" -> {
                    selectedTermBalanceID = "setting_len_den"

                }
            }

            binding.txtTermBalance.setText(item.title)
            true

        })

        popupMenu.show()
    }
    fun openDefaultPrintCopies(view: View){

        //popupMenu.menu.add(Menu.NONE, 2, 2,getString(R.string.udhar_jama))
        //popupMenu.menu.add(Menu.NONE, 3, 3,"Receivables - Payables")
        //popupMenu.menu.add(Menu.NONE, 4, 4,"Len - Den")

        popupMenuPrintCopy.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when(item.itemId.toString()){
                // 1-> true //0-> false
                "1" -> {
                    selectedPrintCopyID = "setting_custom"
                }
                "2" -> {
                    selectedPrintCopyID = "setting_one"
                }
                "3" -> {
                    selectedPrintCopyID = "setting_two"

                }
                "4" -> {
                    selectedPrintCopyID = "setting_three"

                }
                "5" -> {
                    selectedPrintCopyID = "setting_four"

                }
            }
            binding.txtPrintCopies.setText(item.title)
            true
        })

        popupMenuPrintCopy.show()
    }
    private fun fillDefaultBalance() {
        popupMenu = PopupMenu(this, binding.txtTermBalance)
        popupMenu.menu.add(Menu.NONE, 1, 1, getString(R.string.setting_debit_credit))
        popupMenu.menu.add(Menu.NONE, 2, 2, getString(R.string.setting_udhar_jama))
        popupMenu.menu.add(Menu.NONE, 3, 3, getString(R.string.setting_receivable_payable))
        popupMenu.menu.add(Menu.NONE, 4, 4, getString(R.string.setting_len_den))


        binding.txtTermBalance.setText("")
        //selectedTermBalanceID = popupMenu.menu.getItem(0).toString()
    }
}