package com.goldbookapp.ui.activity.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.bumptech.glide.Glide
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ProfileActivityBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.UserWiseRestrictionModel
import com.goldbookapp.ui.activity.viewmodel.ProfileViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.goldbookapp.utils.Status
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.profile_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class ProfileActivity : AppCompatActivity(){

    private lateinit var viewModel: ProfileViewModel

    lateinit var binding: ProfileActivityBinding

    lateinit var loginModel: LoginModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.profile_activity)

        setupViewModel()
        setupUIandListner()

    }


    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                ProfileViewModel::class.java
            )
        binding.setLifecycleOwner(this)
        binding.profileViewModel = viewModel

    }

    private fun setupUIandListner(){

        val prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter


        binding.toolbar.imgLeft.setImageResource(R.drawable.ic_back)
        binding.toolbar.imgRight.setImageResource(R.drawable.ic_edit)
        binding.toolbar.tvTitle.setText("Profile")


        binding.toolbar.imgLeft.clickWithDebounce {
            onBackPressed()
        }

        binding.toolbar.imgRight.clickWithDebounce {

            startActivity(Intent(this, EditProfileActivity::class.java).putExtra(Constants.Change_Status,true))
        }

        binding.tvPasswordLbl.clickWithDebounce {

            startActivity(Intent(this, UpdatePasswordActivity::class.java))
        }

        viewModel.profileDetail.observe(this, Observer { profileModel ->

            Glide.with(this).load(profileModel.data?.user?.imageurl).circleCrop().into(imgProfile)
        })



    }
    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
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
            when(loginModel.data!!.user_info!!.user_type.equals("user",true)){
                // user type user
                true -> {
                    // apply restriciton
                    defaultEnableAllButtonnUI()
                    /*defaultDisableAllButtonnUI()
                    userWiseRestriction(loginModel.data?.bearer_access_token)*/
                }
                // user_type -> admin or super_admin or any other
                false -> {
                    defaultEnableAllButtonnUI()
                }
            }
            if(NetworkUtils.isConnected()) {
                profileDetailAPI(loginModel?.data?.bearer_access_token)
            }
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
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
            if (data.permission!!.get(i).startsWith(getString(R.string.profile))) {
                // Restriction check for Customerl
                when (data.permission!!.get(i).endsWith(getString(R.string.edit_view), true)) {
                    true -> {
                        imgRight.visibility = View.VISIBLE
                    }else->{

                }
                }

            }
            if (data.permission!!.get(i).startsWith(getString(R.string.account_security))) {
                // Restriction check for Customerl
                when (data.permission!!.get(i).endsWith(getString(R.string.edit_view), true)) {
                    true -> {
                        binding.imgPassword.visibility = View.VISIBLE
                        binding.tvPasswordLbl.visibility = View.VISIBLE
                    }else->{

                }
                }

            }
        }
    }

    private fun defaultDisableAllButtonnUI() {
        imgRight.visibility = View.GONE
        binding.imgPassword.visibility = View.GONE
        binding.tvPasswordLbl.visibility = View.GONE
    }
    private fun defaultEnableAllButtonnUI() {
        imgRight.visibility = View.VISIBLE
        binding.imgPassword.visibility = View.VISIBLE
        binding.tvPasswordLbl.visibility = View.VISIBLE
    }

    fun profileDetailAPI(token: String?){
        if(NetworkUtils.isConnected()){
            viewModel.profileDetail(token).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {

                                val prefs = PreferenceHelper.defaultPrefs(this)
                                prefs[Constants.PREF_PROFILE_DETAIL_KEY] = Gson().toJson(it.data) //setter

                                viewModel.profileDetail.postValue(it.data)

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


}