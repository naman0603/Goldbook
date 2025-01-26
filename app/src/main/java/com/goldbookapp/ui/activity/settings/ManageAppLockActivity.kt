package com.goldbookapp.ui.activity.settings

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.goldbookapp.MyApplication
import com.goldbookapp.R
import com.goldbookapp.databinding.ManageAppLockActivityBinding
import com.goldbookapp.inapplock.managers.AppLock
import com.goldbookapp.inapplock.managers.LockManager
import com.goldbookapp.model.WebLinksModel
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.manage_app_lock_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class ManageAppLockActivity : AppCompatActivity() {
    protected var mType = AppLock.UNLOCK_PIN
    lateinit var prefs: SharedPreferences
    lateinit var filter: IntentFilter
    lateinit var onoffReceiver: BroadcastReceiver
    lateinit var receiverManager: ReceiverManager
    lateinit var mLockManager: LockManager<AppLockActivityNew>

    lateinit var binding: ManageAppLockActivityBinding
    private val REQUEST_CODE_ENABLE = 11

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.manage_app_lock_activity)
        //binding.loginModel = LoginModel()


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

    override fun onResume() {
        super.onResume()

        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent) {
                if (ConnectivityStateHolder.isConnected) {
                    // Network is available
                    CommonUtils.hideInternetDialog()
                    if (prefs.contains(Constants.PASSWORD_PREFERENCE_KEY)) {
                        cardMplRemovepin.visibility = VISIBLE
                    } else {
                        cardMplRemovepin.visibility = GONE
                    }

                    mLockManager = LockManager.getInstance() as LockManager<AppLockActivityNew>
                    mLockManager.getAppLock().logoId = R.mipmap.ic_launcher
                }

                if (!ConnectivityStateHolder.isConnected) {
                    // Network is not available
                    CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

                }
            }
        })
    }

    private fun setupUIandListener() {
        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(resources.getString(R.string.app_lock))
        imgLeft?.clickWithDebounce{

            onBackPressed()
        }
        prefs = PreferenceHelper.defaultPrefs(this)
        val weblinks = Gson().fromJson(
            prefs[Constants.WebLinks, ""],
            WebLinksModel.Data::class.java
        ) //getter
        binding.cardMplAuth.clickWithDebounce {

            startActivity(
                Intent(
                    this,
                    AppLockActivityNew::class.java
                )
            )
            CommonUtils.hideInternetDialog()
        }

        if (prefs.contains(Constants.PASSWORD_PREFERENCE_KEY)) {
            cardMplRemovepin.visibility = VISIBLE
        } else {
            cardMplRemovepin.visibility = GONE
        }

        binding.cardMplAppLock.clickWithDebounce {

            val intent = Intent(this@ManageAppLockActivity, AppLockActivityNew::class.java)
            if (prefs.contains(Constants.PASSWORD_PREFERENCE_KEY)) {
                mType = AppLock.CHANGE_PIN
                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.CHANGE_PIN)
            } else {
                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK)
            }
            CommonUtils.hideInternetDialog()
            startActivityForResult(intent, REQUEST_CODE_ENABLE)
        }

        binding.cardMplRemovepin.clickWithDebounce {

            val intent = Intent(this@ManageAppLockActivity, AppLockActivityNew::class.java)
            if (prefs.contains(Constants.PASSWORD_PREFERENCE_KEY)) {
                mType = AppLock.DISABLE_PINLOCK
                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.DISABLE_PINLOCK)
            }
            startActivityForResult(intent, REQUEST_CODE_ENABLE)
            CommonUtils.hideInternetDialog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_ENABLE -> {
                when (mType) {
                    AppLock.CHANGE_PIN -> {

                    }
                    AppLock.ENABLE_PINLOCK -> {

                    }
                    AppLock.DISABLE_PINLOCK -> {


                    }
                }

            }

        }
    }

    private fun clearPasscodePrefs(): Boolean {

        if (this::prefs.isInitialized) {
            try {
                prefs.edit().remove(Constants.PASSWORD_PREFERENCE_KEY).apply()
                prefs.edit().remove("ALGORITHM").apply()
                prefs.edit().remove("LOGO_ID_PREFERENCE_KEY").apply()
                prefs.edit().remove("LAST_ACTIVE_MILLIS").apply()
                prefs.edit().remove("PIN_CHALLENGE_CANCELLED_PREFERENCE_KEY").apply()
                return true
            } catch (e: Exception) {
                return false
                // Log.e("passcode pref exception", e.toString())
            }


        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }

    private fun registerScreenOnoff_Boadcast() {
        if (prefs.contains(Constants.PASSWORD_PREFERENCE_KEY)) {
            onoffReceiver = ScreenOnOffReceiver.getInstance(this)
            receiverManager = ReceiverManager.init(this)
            if (receiverManager.isReceiverRegistered(onoffReceiver)) {
                //  Log.v(" Privcay Screen on/off","receiver already registered")
            } else {
                filter = IntentFilter(Intent.ACTION_SCREEN_ON)
                filter.addAction(Intent.ACTION_SCREEN_OFF)

                receiverManager.registerReceiver(
                    ScreenOnOffReceiver.getInstance(MyApplication.appContext),
                    filter
                )
            }

        }
    }
}