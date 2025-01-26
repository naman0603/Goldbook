package com.goldbookapp.ui.activity.settings

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.goldbookapp.R
import com.goldbookapp.databinding.ActivityTaxesBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_taxes.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class TaxesActivity : AppCompatActivity() {
    private var saveTaxbtnShow: Boolean = false
    lateinit var binding: ActivityTaxesBinding
    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_taxes)
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
            when(loginModel.data!!.user_info!!.user_type.equals("user",true)){
                // user type user
                true -> {
                    // apply restriciton
                    if (intent.extras != null && intent.extras!!.containsKey(Constants.Change_Status)) {
                        saveTaxbtnShow = intent.getBooleanExtra(Constants.Change_Status,false)
                    }
                   // Log.v("taxesactivtiy", saveTaxbtnShow.toString())
                }
                // user_type -> admin or super_admin or any other
                false -> {
                   // nothing to pass in intent for admin/superadmin user
                    saveTaxbtnShow = true
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
        tvTitle.setText(R.string.taxes)
        //tvRight.setText(R.string.save)

        getLoginModelFromPrefs()
        // gst branch
        if(loginModel.data?.branch_info?.branch_type.equals("1",true)) {
            cardTaxesGst.visibility = View.VISIBLE

        }
        // non gst branch
        else  {
            cardTaxesGst.visibility = View.GONE

        }

        imgLeft?.clickWithDebounce {
            onBackPressed()
        }
        cardTaxesGst?.clickWithDebounce {
            startActivity(
                Intent(
                    this,
                    GSTActivity::class.java
                ).putExtra(Constants.Change_Status,saveTaxbtnShow)
            )
        }

        cardTaxesTds?.clickWithDebounce {
            startActivity(
                Intent(
                    this,
                    TCSActivity::class.java
                ).putExtra(Constants.Change_Status,saveTaxbtnShow).putExtra(Constants.isFromTcsNogAdd, false)
            )
        }

        cardTaxesTcs?.clickWithDebounce {

            startActivity(
                Intent(
                    this,
                    TCSActivity::class.java
                ).putExtra(Constants.Change_Status,saveTaxbtnShow).putExtra(Constants.isFromTcsNogAdd, true)
            )
        }
    }
    private fun getLoginModelFromPrefs() {
        prefs = PreferenceHelper.defaultPrefs(applicationContext!!)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter
        if (prefs.contains(Constants.PREF_ADD_NOG_KEY)) {
            prefs.edit().remove(Constants.PREF_ADD_NOG_KEY).apply()
        }
        if (prefs.contains(Constants.PREF_ADD_NOP_KEY)) {
            prefs.edit().remove(Constants.PREF_ADD_NOP_KEY).apply()
        }
    }
}