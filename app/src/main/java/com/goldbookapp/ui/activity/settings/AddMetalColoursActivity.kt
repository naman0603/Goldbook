package com.goldbookapp.ui.activity.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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
import com.goldbookapp.databinding.ActivityInventoryMetalBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.ui.activity.viewmodel.AddMetalColourViewModel
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
import kotlinx.android.synthetic.main.activity_inventory_metal.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class AddMetalColoursActivity : AppCompatActivity() {

    lateinit var binding: ActivityInventoryMetalBinding
    private lateinit var viewModel: AddMetalColourViewModel

    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    private lateinit var status: String
    var metalSaveAdd: String? = "0"
    private var changeStatus: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_inventory_metal)
        setupViewModel()
        setupUIandListner()
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                AddMetalColourViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }

    private fun setupUIandListner() {

        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        )

        if (intent.extras != null && intent.extras!!.containsKey(Constants.METAL_SAVE_TYPE)) {
            metalSaveAdd = intent.getStringExtra(Constants.METAL_SAVE_TYPE)
        }
        if (metalSaveAdd == "1") {
            txtMetalCode.clearFocus()
            txtMetalName.clearFocus()
        }



        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.metal_colour)


        status = "1"
        radiogroupSatus.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                radioStatusActive.id -> status = "1"

                radioStatusInactive.id -> status = "0"

            }
        })

        imgLeft?.clickWithDebounce {
            onBackPressed()
        }

        btnSaveAdd_AddMetal?.clickWithDebounce {
            if (performValidation()) {
                if (NetworkUtils.isConnected()) {
                    addMetalColourAPI(
                        loginModel?.data?.bearer_access_token,
                        txtMetalCode.text.toString().trim(),
                        txtMetalName.text.toString().trim(),
                        status, true
                    )

                }
            }
        }
        btnSaveCloseAddMetal?.clickWithDebounce {
            if (performValidation()) {
                if (NetworkUtils.isConnected()) {
                    addMetalColourAPI(
                        loginModel?.data?.bearer_access_token,
                        txtMetalName.text.toString().trim(),
                        txtMetalCode.text.toString().trim(),
                        status,
                        false
                    )


                }
            }
        }

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
            if (intent.extras != null && intent.extras!!.containsKey(Constants.Change_Status)) {
                changeStatus = intent.getBooleanExtra(Constants.Change_Status,false)
              //  Log.v("changestatus",""+changeStatus)
            }
            when(loginModel.data!!.user_info!!.user_type.equals("user",true)){
                // user type user
                true -> {
                    // apply restriciton
                    when(changeStatus){
                        false->{
                            radioStatusInactive.isEnabled = false
                        }else->{

                    }
                    }

                }else->{

            }
                // user_type -> admin or super_admin or any other

            }
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }


    fun performValidation(): Boolean {
        if (txtMetalName.text.toString().isBlank()) {

            CommonUtils.showDialog(this, getString(R.string.enter_metal_name_msg))
            txtMetalName.requestFocus()
            return false
        } else if (txtMetalCode.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.enter_metal_code_msg))
            txtMetalCode.requestFocus()
            return false
        } else if (radiogroupSatus.getCheckedRadioButtonId() == -1) { // no radio buttons are checked
            Toast.makeText(
                this,
                getString(R.string.check_status_active_inactive_msg),
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }


    fun addMetalColourAPI(
        token: String?,
        colour_name: String?,
        colour_code: String?,
        status: String?,
        is_from_saveAdd: Boolean

    ) {
        viewModel.addMetalColour(
            token, colour_name,
            colour_code,
            status

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

                            when (is_from_saveAdd) {
                                true -> {
                                    this.finish()
                                    startActivity(
                                        Intent(
                                            this,
                                            AddMetalColoursActivity::class.java
                                        ).putExtra(Constants.METAL_SAVE_TYPE, "1")
                                    )
                                }
                                false -> onBackPressed()
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