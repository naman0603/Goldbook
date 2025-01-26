package com.goldbookapp.ui.activity.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityContactsBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.SettinContactDetailModel
import com.goldbookapp.ui.activity.viewmodel.SettingsContactViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.Status
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_contacts.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class ContactsActivity : AppCompatActivity() {
    private var saveContactBtnShow: Boolean = false
    var disable_credit_limit: Int = 0
    var stop_transaction_if_limit_over: Int = 0
    lateinit var binding: ActivityContactsBinding
    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    private lateinit var viewModel: SettingsContactViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_contacts)
        setupUIandListner()
        setupViewModel()
    }

    private fun setupUIandListner() {
        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.contacts)


        getLoginModelFromPrefs()
        switchContactCreditLimit.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                disable_credit_limit = 1
                lyContactStopTransaction.visibility = GONE

            } else {
                disable_credit_limit = 0
                lyContactStopTransaction.visibility = VISIBLE

            }
        }
        switchContactStopTransaction.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                stop_transaction_if_limit_over = 1
            } else {
                stop_transaction_if_limit_over = 0
            }
        }
        imgLeft?.clickWithDebounce {
            onBackPressed()
        }
        btnSaveSettingContact?.clickWithDebounce {

            savedetailContactApi(
                loginModel.data?.bearer_access_token,
                disable_credit_limit,
                stop_transaction_if_limit_over
            )
        }
    }

    private fun getLoginModelFromPrefs() {
        prefs = PreferenceHelper.defaultPrefs(applicationContext!!)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter
    }

    private fun savedetailContactApi(
        token: String?,
        disable_credit_limit: Int,
        stop_transaction_if_limit_over: Int
    ) {
        if (NetworkUtils.isConnected()) {

            viewModel.saveContactApi(token, disable_credit_limit, stop_transaction_if_limit_over)
                .observe(this, Observer {
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

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                SettingsContactViewModel::class.java
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
            // userwise restric api call (for applying user restriction)
            when(loginModel.data!!.user_info!!.user_type.equals("user",true)){
                // user type user
                true -> {
                    // apply restriciton
                    defaultDisableAllButtonnUI()
                    if (intent.extras != null && intent.extras!!.containsKey(Constants.Change_Status)) {
                        saveContactBtnShow = intent.getBooleanExtra(Constants.Change_Status,false)
                        when(saveContactBtnShow){
                            true -> {
                                binding.btnSaveSettingContact.visibility = View.VISIBLE
                            }
                            false -> {
                                binding.btnSaveSettingContact.visibility = View.GONE
                            }
                        }
                    }
                }
                // user_type -> admin or super_admin or any other
                false -> {
                    defaultEnableAllButtonnUI()
                }
            }
            getdetailContactApi(loginModel.data?.bearer_access_token)
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun defaultEnableAllButtonnUI() {
        binding.btnSaveSettingContact.visibility = View.VISIBLE
    }

    private fun defaultDisableAllButtonnUI() {
        binding.btnSaveSettingContact.visibility = View.GONE
    }

    private fun getdetailContactApi(token: String?) {
        if (NetworkUtils.isConnected()) {

            viewModel.getdetailContactApi(token).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                lyContactSetting.visibility = VISIBLE
                                setdefaultContactData(it.data.data)

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

    private fun setdefaultContactData(data: SettinContactDetailModel.Data?) {
        when (data!!.disable_credit_limit) {
            // 1-> true //0-> false
            "1" -> {
                binding.switchContactCreditLimit.isChecked = true
                lyContactStopTransaction.visibility = GONE
            }
            "0" -> {
                binding.switchContactCreditLimit.isChecked = false
                lyContactStopTransaction.visibility = VISIBLE
            }
        }
        when (data.stop_transaction_if_limit_over) {
            // 1-> true //0-> false
            "1" -> binding.switchContactStopTransaction.isChecked = true
            "0" -> binding.switchContactStopTransaction.isChecked = false
        }

    }

}