package com.goldbookapp.ui.activity.settings

import android.content.Intent
import android.content.SharedPreferences
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
import com.goldbookapp.databinding.SettingsActivityBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.UserWiseRestrictionModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.example.goldbookapp.ui.activity.viewmodel.SettingsViewModel
import com.goldbookapp.model.ProfileDetailModel
import com.goldbookapp.ui.activity.organization.OrganizationListActivity
import com.goldbookapp.ui.activity.user.ProfileActivity
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
import kotlinx.android.synthetic.main.nav_header_main.*

class SettingsActivity : AppCompatActivity(){
    private lateinit var viewModel: SettingsViewModel
    lateinit var binding: SettingsActivityBinding
    private var generalEdit:Boolean = false
    private var taxesEdit:Boolean = false
    private var contactEdit:Boolean = false


    lateinit var loginModel: LoginModel
    lateinit var profileModel: ProfileDetailModel
    lateinit var prefs: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.settings_activity)
        val view = binding.root

        setupViewModel()
        setupUIandListner(view)
    }

    override fun onResume() {
        super.onResume()
        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent) {
                handleConnectivityChange()
            }
        })
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                SettingsViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }
    fun setupUIandListner(root: View) {
        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        profileModel = Gson().fromJson(
            prefs[Constants.PREF_PROFILE_DETAIL_KEY, ""],
            ProfileDetailModel::class.java
        ) //getter


        binding.toolbar.imgLeft.setImageResource(R.drawable.ic_back)

        binding.toolbar.tvTitle.setText(getString(R.string.toolbar_title_settings))


        binding.toolbar.imgLeft.clickWithDebounce {
            onBackPressed()
        }

        Glide.with(this).load(profileModel.data?.user?.imageurl).circleCrop()
            .placeholder(R.drawable.ic_user_placeholder).into(imgDrawerProfile)
        binding.tvDrawerUserName.setText(profileModel.data?.user?.name)
        binding.cardProfile.clickWithDebounce {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.linearPrivacySecurity.clickWithDebounce {

            startActivity(Intent(this, PrivacySecurityActivity::class.java))
        }
        binding.llSettingOrgs.clickWithDebounce {

            startActivity(
                Intent(
                    this,
                    OrganizationListActivity::class.java
                ).putExtra(Constants.Change_Status, true)
            )
            // here the Change_Status means viewdetail
        }
        binding.llSettingBranches.clickWithDebounce {

            startActivity(
                Intent(
                    this,
                    BranchListActivity::class.java
                )
            )
        }
        binding.llSettingItemCategory.clickWithDebounce {

            startActivity(
                Intent(
                    this,
                    ItemCategoriesList::class.java
                )
            )
        }
        binding.llSettingPref.clickWithDebounce {

            startActivity(
                Intent(
                    this,
                    PreferencesActivity::class.java
                ).putExtra(Constants.Change_Status,generalEdit)
            )
        }
        binding.llSettingTaxes.clickWithDebounce{

            startActivity(
                Intent(
                    this,
                    TaxesActivity::class.java
                ).putExtra(Constants.Change_Status,taxesEdit)
            )
         //   Log.v("settingsactivity", taxesEdit.toString())
        }

        binding.llSettingContact.clickWithDebounce {

            startActivity(
                Intent(
                    this,
                    ContactsActivity::class.java
                ).putExtra(Constants.Change_Status,contactEdit)
            )
        }

        binding.llSettingItemInventory.clickWithDebounce {

            startActivity(
                Intent(
                    this,
                    ItemInventoryActivity::class.java
                )
            )
        }

        binding.linearAboutus.clickWithDebounce {

            startActivity(
                Intent(
                    this,
                    AboutUsActivity::class.java
                )
            )
        }
        binding.linearSubscription.clickWithDebounce {

            startActivity(
                Intent(
                    this,
                    SubscriptionActivity::class.java
                )
            )
        }
    }

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected) {
            // Network is available
            CommonUtils.hideInternetDialog()
            when(loginModel.data!!.user_info!!.user_type.equals("user",true)){
                // user type user
                true -> {
                    // apply restriciton
                    defaultDisableAllButtonnUI()
                    userWiseRestriction(loginModel.data?.bearer_access_token)
                }
                // user_type -> admin or super_admin or any other
                false -> {
                    defaultEnableAllButtonnUI()
                }
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
            if (data.permission!!.get(i).startsWith(getString(R.string.setting))) {
                // Restriction check for Customerl
                when (data.permission!!.get(i).startsWith(getString(R.string.setting_preference), true)) {
                    true -> {
                        binding.llSettingPref.visibility = View.VISIBLE
                        when (data.permission!!.get(i).endsWith(getString(R.string.edit), true)) {
                            true -> generalEdit = true
                            else->{

                            }
                        }
                        when (data.permission!!.get(i).endsWith(getString(R.string.view), true)) {
                            true -> generalEdit = false
                            else->{

                            }
                        }
                        when (data.permission!!.get(i).endsWith(getString(R.string.view), true) && data.permission!!.get(i).endsWith(getString(R.string.edit), true)) {
                            true -> generalEdit = true
                            else->{

                            }
                        }

                    }
                    else->{

                    }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.setting))) {
                // Restriction check for Customerl
                when (data.permission!!.get(i).startsWith(getString(R.string.setting_taxes), true)) {
                    true -> {
                        binding.llSettingTaxes.visibility = View.VISIBLE
                        when (data.permission!!.get(i).endsWith(getString(R.string.edit), true)) {
                            true -> taxesEdit = true
                            else->{

                            }
                        }
                        when (data.permission!!.get(i).endsWith(getString(R.string.view), true)) {
                            true -> taxesEdit = false
                            else->{

                            }
                        }
                        when (data.permission!!.get(i).endsWith(getString(R.string.view), true) && data.permission!!.get(i).endsWith(getString(R.string.edit), true)) {
                            true -> taxesEdit = true
                            else->{

                            }
                        }
                    }else->{

                }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.setting))) {
                // Restriction check for Customerl
                when (data.permission!!.get(i).startsWith(getString(R.string.setting_contacts), true)) {
                    true -> {
                        binding.llSettingContact.visibility = View.GONE
                        when (data.permission!!.get(i).endsWith(getString(R.string.edit), true)) {
                            true -> contactEdit = true
                            else->{

                            }
                        }
                        when (data.permission!!.get(i).endsWith(getString(R.string.view), true)) {
                            true -> contactEdit = false
                            else->{

                            }
                        }
                        when (data.permission!!.get(i).endsWith(getString(R.string.view), true) && data.permission!!.get(i).endsWith(getString(R.string.edit), true)) {
                            true -> contactEdit = true
                            else->{

                            }
                        }
                    }else->{

                }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.setting))) {
                // Restriction check for Customerl
                when (data.permission!!.get(i).startsWith(getString(R.string.setting_items), true)) {
                    true -> {
                        when (data.permission!!.get(i).endsWith(getString(R.string.view), true)) {
                            true->{
                                binding.llSettingItemInventory.visibility = View.VISIBLE
                            }else->{

                        }
                        }

                    }else->{

                }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.org))) {
                // Restriction check for Supplier
                when (data.permission!!.get(i).startsWith(getString(R.string.org_list), true)) {
                    true -> {
                       binding.llSettingOrgs.visibility = View.VISIBLE
                    }else->{

                }
                }

            }
            if (data.permission!!.get(i).startsWith(getString(R.string.branch))) {
                // Restriction check for Supplier
                when (data.permission!!.get(i).startsWith(getString(R.string.branch_list), true)) {
                    true -> {
                        binding.llSettingBranches.visibility = View.VISIBLE
                    }else->{

                }
                }

            }

        }
    }


    private fun defaultDisableAllButtonnUI() {
        binding.llSettingOrgs.visibility = View.GONE
        binding.llSettingBranches.visibility = View.GONE
        binding.llSettingPref.visibility = View.GONE
        binding.llSettingTaxes.visibility = View.GONE
        binding.llSettingContact.visibility =  View.GONE
        binding.llSettingItemInventory.visibility =  View.GONE

    }
    private fun defaultEnableAllButtonnUI() {
        binding.llSettingOrgs.visibility = View.VISIBLE
        binding.llSettingBranches.visibility = View.VISIBLE
        binding.llSettingPref.visibility = View.VISIBLE
        binding.llSettingTaxes.visibility = View.VISIBLE
        binding.llSettingContact.visibility =  View.GONE
        binding.llSettingItemInventory.visibility =  View.VISIBLE

    }

    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }
}