package com.goldbookapp.ui.activity.settings

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.goldbookapp.MyApplication
import com.goldbookapp.R
import com.goldbookapp.databinding.PrivacyAndSecurityActivityBinding
import com.goldbookapp.inapplock.managers.AppLock
import com.goldbookapp.model.WebLinksModel
import com.goldbookapp.ui.activity.user.UpdatePasswordActivity
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.toolbar_layout.*

class PrivacySecurityActivity : AppCompatActivity(){
    protected var mType = AppLock.UNLOCK_PIN
    lateinit var prefs: SharedPreferences
    lateinit var filter: IntentFilter
    lateinit var onoffReceiver: BroadcastReceiver
    lateinit var receiverManager: ReceiverManager

    lateinit var binding: PrivacyAndSecurityActivityBinding
    private val REQUEST_CODE_ENABLE = 11

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.privacy_and_security_activity)


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
            setupUIandListener()
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }


    private fun setupUIandListener() {
        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(resources.getString(R.string.privacy_and_security))
        imgLeft?.clickWithDebounce {
            onBackPressed()
        }
       prefs = PreferenceHelper.defaultPrefs(this)
        val weblinks = Gson().fromJson(
            prefs[Constants.WebLinks, ""],
            WebLinksModel.Data::class.java
        ) //getter
        binding.cardPnSPwd.clickWithDebounce {

            startActivity(
                Intent(
                    this,
                    UpdatePasswordActivity::class.java
                )
            )
            CommonUtils.hideInternetDialog()
        }
        binding.cardPnSTermsofService.clickWithDebounce {

            startActivity(
                Intent(this@PrivacySecurityActivity, PrivacyPolicyActivity::class.java).putExtra(
                    "weburi",
                    weblinks.terms
                ).putExtra(
                    "title", getString(
                        R.string.terms_of_service
                    )
                )
            )
            CommonUtils.hideInternetDialog()
        }
        binding.cardPnSPrivacy.clickWithDebounce {

            startActivity(
                Intent(this@PrivacySecurityActivity, PrivacyPolicyActivity::class.java).putExtra(
                    "weburi",
                    weblinks.privacy
                ).putExtra(
                    "title", getString(
                        R.string.privacy_policy
                    )
                )
            )
            CommonUtils.hideInternetDialog()
        }
        binding.cardPnSAppLock.clickWithDebounce {

            val intent = Intent(this@PrivacySecurityActivity, ManageAppLockActivity::class.java)
            if (prefs.contains(Constants.PASSWORD_PREFERENCE_KEY)) {
                mType = AppLock.CHANGE_PIN
                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.CHANGE_PIN)
            }
            else{
                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK)
            }
            CommonUtils.hideInternetDialog()
            startActivityForResult(intent, REQUEST_CODE_ENABLE)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_ENABLE -> {
                when(mType){
                    AppLock.CHANGE_PIN ->{

                    }
                    AppLock.ENABLE_PINLOCK->{

                        registerScreenOnoff_Boadcast()
                        CommonUtils.hideInternetDialog()
                    }
                }

            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }
    private fun registerScreenOnoff_Boadcast() {
        if(prefs.contains(Constants.PASSWORD_PREFERENCE_KEY)){
            onoffReceiver = ScreenOnOffReceiver.getInstance(this)
            receiverManager = ReceiverManager.init(this)
            if(receiverManager.isReceiverRegistered(onoffReceiver)){

            }
            else{
                filter = IntentFilter(Intent.ACTION_SCREEN_ON)
                filter.addAction(Intent.ACTION_SCREEN_OFF)

                receiverManager.registerReceiver(ScreenOnOffReceiver.getInstance(MyApplication.appContext),filter)
            }

        }
    }
}