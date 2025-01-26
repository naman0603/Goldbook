package com.goldbookapp.ui.activity.settings

import android.content.*
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.MyApplication
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityPinCodeBinding
import com.goldbookapp.inapplock.enums.KeyboardButtonEnum
import com.goldbookapp.inapplock.interfaces.KeyboardButtonClickedListener
import com.goldbookapp.inapplock.managers.AppLock
import com.goldbookapp.inapplock.managers.FingerprintUiHelper
import com.goldbookapp.inapplock.managers.FingerprintUiHelper.FingerprintUiHelperBuilder
import com.goldbookapp.inapplock.managers.LockManager
import com.goldbookapp.inapplock.views.KeyboardView
import com.goldbookapp.inapplock.views.PinCodeRoundView
import com.goldbookapp.model.LoginModel
import com.goldbookapp.ui.MainActivity
import com.goldbookapp.ui.activity.auth.LoginActivity
import com.goldbookapp.ui.activity.viewmodel.CustomPinViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.*
import com.goldbookapp.utils.PreferenceHelper.get
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_pin_code.*
import java.util.*

class AppLockActivityNew : AppCompatActivity(), KeyboardButtonClickedListener, View.OnClickListener,
    FingerprintUiHelper.Callback {
    private lateinit var viewModel: CustomPinViewModel
    lateinit var binding: ActivityPinCodeBinding
    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel

    lateinit var filter: IntentFilter
    lateinit var onoffReceiver: BroadcastReceiver
    lateinit var receiverManager: ReceiverManager

    var isPassCorrect: Boolean = false
    var isInternetDialogShown: Boolean = false
    var isFirstTimeArrive : Boolean = false

    companion object {
        var isRunning = false;
        val TAG = AppLockActivityNew::class.java.simpleName

        @kotlin.jvm.JvmField
        var ACTION_CANCEL: String = TAG + ".actionCancelled"
    }

    /* var ACTION_CANCEL = "$TAG.actionCancelled"*/
    private val DEFAULT_PIN_LENGTH = 4

    protected var mStepTextView: TextView? = null
    protected var mForgotTextView: TextView? = null
    protected var mPinCodeRoundView: PinCodeRoundView? = null
    protected var mKeyboardView: KeyboardView? = null
    protected var mFingerprintImageView: ImageView? = null
    protected var mFingerprintTextView: TextView? = null

    // protected var mLockManager: LockManager<*>? = null
    lateinit var mLockManager: LockManager<AppLockActivityNew>


    @RequiresApi(Build.VERSION_CODES.M)
            /* var mFingerprintManager = getSystemService(FINGERPRINT_SERVICE) as FingerprintManager
             var mFingerprintUiHelper = FingerprintUiHelperBuilder(mFingerprintManager).build(
                 mFingerprintImageView,
                 mFingerprintTextView,
                 this
             )*/
    var mFingerprintManager: FingerprintManager? = null
    var mFingerprintUiHelper: FingerprintUiHelper? = null

    protected var mType = AppLock.UNLOCK_PIN
    protected var mAttempts = 1
    var mPinCode: String? = null

    protected var mOldPinCode: String? = null

    private var isCodeSuccessful = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pin_code)
        val view = binding.root
        setupViewModel()
        setupUIandListner()
        initLayout(intent)
        setLayout()
        isFirstTimeArrive = true
        //binding.loginModel = LoginModel()

        /* imgLeft.setImageResource(R.drawable.ic_back)
         //binding.toolbar.imgRight.setImageResource(R.drawable.ic_edit)
         tvTitle.setText(getString(R.string.about))
 */

        /* imgLeft.setOnClickListener(View.OnClickListener {
             onBackPressed()
         })*/


    }

    private fun setLayout() {

        if (resources.displayMetrics.widthPixels * 1080 / 1080 == 1080) {
            val params8 = RelativeLayout.LayoutParams(
                resources.displayMetrics.widthPixels * 1080 / 1080, 1920
            );
            params8.addRule(RelativeLayout.CENTER_IN_PARENT);
            rl.setLayoutParams(params8);
//            params8.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//
//            params8.rightMargin = 40;

        } else if (getResources().getDisplayMetrics().heightPixels > 1280) {
            val params8 = RelativeLayout.LayoutParams(
                resources.displayMetrics.widthPixels * 1080 / 1080,
                (resources.displayMetrics.heightPixels - 60) * 1920 / 1920
            );
            params8.addRule(RelativeLayout.CENTER_IN_PARENT);
            rl.setLayoutParams(params8);
//            params8.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//
//            params8.rightMargin = 40;

        } else {
            val params8 = RelativeLayout.LayoutParams(
                resources.displayMetrics.widthPixels * 1080 / 1080,
                resources.displayMetrics.heightPixels * 1920 / 1920
            );
            params8.addRule(RelativeLayout.CENTER_IN_PARENT);
            rl.setLayoutParams(params8);
//            params8.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//
//            params8.rightMargin = 40;
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }

    override fun onBackPressed() {
        if (getBackableTypes()!!.contains(mType)) {
            if (AppLock.UNLOCK_PIN == getType()) {
                mLockManager!!.appLock.setPinChallengeCancelled(true)
                LocalBroadcastManager
                    .getInstance(this)
                    .sendBroadcast(Intent().setAction(ACTION_CANCEL))
            }
            super.onBackPressed()
        }
    }

    fun getBackableTypes(): List<Int?>? {
        return Arrays.asList(AppLock.CHANGE_PIN, AppLock.ENABLE_PINLOCK, AppLock.DISABLE_PINLOCK)
    }

    private fun initLayout(intent: Intent) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            //Animate if greater than 2.3.3
            overridePendingTransition(R.anim.nothing, R.anim.nothing)
        }
        val extras = intent.extras
        if (extras != null) {
            mType = extras.getInt(AppLock.EXTRA_TYPE, AppLock.UNLOCK_PIN)
            //  Log.e("hasextra m type",mType.toString())
        }
        mLockManager = LockManager.getInstance() as LockManager<AppLockActivityNew>
        mPinCode = ""
        mOldPinCode = ""
        enableAppLockerIfDoesNotExist()
        (mLockManager as LockManager<*>?)!!.getAppLock().setPinChallengeCancelled(false)
        mStepTextView = findViewById<View>(R.id.pin_code_step_textview) as TextView
        mPinCodeRoundView = findViewById<View>(R.id.pin_code_round_view) as PinCodeRoundView
        mPinCodeRoundView!!.setPinLength(this.getPinLength())
        mForgotTextView = findViewById<View>(R.id.pin_code_forgot_textview) as TextView
        mForgotTextView!!.setOnClickListener(this)
        mKeyboardView = findViewById<View>(R.id.pin_code_keyboard_view) as KeyboardView
        mKeyboardView!!.setKeyboardButtonClickedListener(this)
        val logoId = (mLockManager as LockManager<*>?)!!.getAppLock().logoId
        val logoImage = findViewById<View>(R.id.pin_code_logo_imageview) as ImageView
        if (logoId != AppLock.LOGO_ID_NONE) {
            logoImage.visibility = View.VISIBLE
            logoImage.setImageResource(logoId)
        }
        mForgotTextView!!.setText(getForgotText())
        setForgotTextVisibility()
        setStepText()
    }

    private fun enableAppLockerIfDoesNotExist() {
        try {
            if (mLockManager!!.appLock == null) {
                mLockManager!!.enableAppLock(
                    this@AppLockActivityNew,
                    getCustomAppLockActivityClass() as Class<AppLockActivityNew>?
                )
            }
        } catch (e: Exception) {
            //  Log.e(TAG, e.toString())
        }
    }

    private fun setStepText() {
        mStepTextView!!.setText(getStepText(mType))
        val font = ResourcesCompat.getFont(this@AppLockActivityNew, R.font.proxima_nova_bold)
        mStepTextView!!.typeface = font
    }

    fun getStepText(reason: Int): String? {
        var msg: String? = null
        when (reason) {
            AppLock.DISABLE_PINLOCK -> msg = getString(
                R.string.pin_code_step_disable,
                getPinLength()
            )
            AppLock.ENABLE_PINLOCK -> msg = getString(R.string.pin_code_step_create, getPinLength())
            AppLock.CHANGE_PIN -> msg = getString(R.string.pin_code_step_change, getPinLength())
            AppLock.UNLOCK_PIN -> msg = getString(R.string.pin_code_step_unlock, getPinLength())
            AppLock.CONFIRM_PIN -> msg = getString(
                R.string.pin_code_step_enable_confirm,
                getPinLength()
            )
        }
        return msg
    }


    fun getCustomAppLockActivityClass(): Class<out AppLockActivityNew> {
        return this.javaClass
    }

    fun onPinSuccess(attempts: Int) {
        mPinCode = getPin()
        // Toast.makeText(CustomPinActivity.this,mPin,Toast.LENGTH_SHORT).show();
        isRunning = false
        CommonUtils.hideProgress()
        finish()

    }

    fun getPinLength(): Int {
        return DEFAULT_PIN_LENGTH
    }

    fun getPin(): String? {
        return mPinCode
    }

    override fun finish() {
        super.finish()

        //If code successful, reset the timer
        if (isCodeSuccessful) {
            if (mLockManager != null) {
                val appLock = mLockManager.appLock
                appLock?.setLastActiveMillis()
            }
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            //Animate if greater than 2.3.3
            overridePendingTransition(R.anim.nothing, R.anim.slide_down)
        }
    }


    fun getForgotText(): String? {
        return getString(R.string.pin_code_forgot_text)
    }

    private fun setForgotTextVisibility() {
        mForgotTextView!!.visibility =
            if (mLockManager!!.appLock.shouldShowForgot(mType)) View.VISIBLE else View.GONE
    }

    fun getType(): Int {
        return mType
    }

    override fun onPause() {
        super.onPause()
        if (mFingerprintUiHelper != null) {
            mFingerprintUiHelper!!.stopListening()
        }
    }

    override fun onResume() {
        super.onResume()
        //Init layout for Fingerprint
        isRunning = true

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
            if (mPinCode!!.length < 4) {

                try {// Check if we're running on Android 6.0 (M) or higher
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        //Fingerprint API only available on from Android 6.0 (M)
                        val fingerprintManager =
                            this.getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
                        if (!fingerprintManager.isHardwareDetected) {
                            // Device doesn't support fingerprint authentication
                            pin_code_fingerprint_imageview.visibility = View.GONE
                        } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                            // User hasn't enrolled any fingerprints to authenticate with
                            pin_code_fingerprint_imageview.visibility = View.GONE
                        } else {
                            // Everything is ready for fingerprint authentication
                            pin_code_fingerprint_imageview.visibility = View.VISIBLE
                            initLayoutForFingerprint()
                        }
                    }
                } catch (e: Exception) {
                    // exception in fingerprint
                    Log.e("fingerprintwerror", e.toString())
                }
            }
            if (isInternetDialogShown) {
                isInternetDialogShown = false
                onRippleAnimationEnd()
            }
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))
            if (mFingerprintUiHelper != null) {
                mFingerprintUiHelper!!.stopListening()
            }
        }
    }

    private fun initLayoutForFingerprint() {
        mFingerprintImageView = findViewById<View>(R.id.pin_code_fingerprint_imageview) as ImageView
        mFingerprintTextView = findViewById<View>(R.id.pin_code_fingerprint_textview) as TextView
        if (mType == AppLock.UNLOCK_PIN && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when(isFirstTimeArrive){
                true ->  {
                    mFingerprintManager = getSystemService(FINGERPRINT_SERVICE) as FingerprintManager
                    mFingerprintUiHelper = FingerprintUiHelperBuilder(mFingerprintManager).build(
                        mFingerprintImageView,
                        mFingerprintTextView,
                        this@AppLockActivityNew
                    )
                    isFirstTimeArrive = false
                }
                else->{

                }
            }

            try {
                if (mFingerprintManager!!.isHardwareDetected && mFingerprintUiHelper!!.isFingerprintAuthAvailable()
                    && mLockManager.appLock.isFingerprintAuthEnabled
                ) {
                    mFingerprintImageView!!.visibility = View.VISIBLE
                    mFingerprintTextView!!.visibility = View.VISIBLE
                    mFingerprintUiHelper!!.startListening()
                } else {
                    mFingerprintImageView!!.visibility = View.GONE
                    mFingerprintTextView!!.visibility = View.GONE
                }
            } catch (e: SecurityException) {
                // Log.e(TAG, e.toString())
                mFingerprintImageView!!.visibility = View.GONE
                mFingerprintTextView!!.visibility = View.GONE
            }
        } else {
            mFingerprintImageView!!.visibility = View.GONE
            mFingerprintTextView!!.visibility = View.GONE
        }
    }

    private fun setupUIandListner() {
        prefs = PreferenceHelper.defaultPrefs(this)
        try {
            loginModel = Gson().fromJson(
                prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
                LoginModel::class.java
            )
        } catch (e: Exception) {
            //   Log.e("error loginmodel",e.toString())
        }

    }

    private fun setAppLockPin(passcode: String) {
        if (NetworkUtils.isConnected()) {

            viewModel.setAppLockPin(loginModel?.data?.bearer_access_token, passcode).observe(
                this,
                Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {
                                    onPinCodeSuccess()
                                    Toast.makeText(
                                        this,
                                        "In-app PIN lock enabled",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    registerScreenOnoff_Boadcast()

                                    //  Log.v(passcode, "pin set")
                                } else {
                                    //  Log.v("passcode", "fail")
                                    //prefs.edit().remove(Constants.PASSWORD_PREFERENCE_KEY).apply();
                                    /*Toast.makeText(
                                    this,
                                    it.data?.errormessage?.message,
                                    Toast.LENGTH_LONG
                                )
                                    .show()*/
                                }
                                CommonUtils.hideProgress()

                            }
                            Status.ERROR -> {
                                CommonUtils.hideProgress()
                                /* Toast.makeText(this, it.data?.errormessage?.message, Toast.LENGTH_LONG)
                                 .show()*/
                            }
                            Status.LOADING -> {
                                // CommonUtils.showProgress(this)
                            }
                        }
                    }
                })
        } else {
            /*CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))*/
        }
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
            this.finish()

            /*  filter = IntentFilter(Intent.ACTION_SCREEN_ON)
              filter.addAction(Intent.ACTION_SCREEN_OFF)
              registerReceiver(onoffReceiver, filter)*/
        }
    }

    private fun checkPin(passcode: String) {
        if (NetworkUtils.isConnected()) {
           /* if (CommonUtils.isValidClickPressed()) {*/
                viewModel.checkPin(loginModel.data?.bearer_access_token, passcode).observe(
                    this,
                    Observer {
                        it?.let { resource ->
                            when (resource.status) {
                                Status.SUCCESS -> {
                                    if (it.data?.status == true) {
                                        if (it.data.data!!.special == true) {
                                            startActivity(
                                                Intent(this, MainActivity::class.java).setFlags(
                                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                ).putExtra(Constants.isFromDemoCompany, true)
                                            )
                                        } else {
                                            isPassCorrect = true
                                            when (mType) {
                                                AppLock.CHANGE_PIN -> {
                                                    /* if (isPassCorrect*//*mLockManager.appLock.checkPasscode(mPinCode)*//*) {*/
                                                    mType = AppLock.ENABLE_PINLOCK
                                                    setStepText()
                                                    setForgotTextVisibility()
                                                    setPinCode("")
                                                    setPinCode("")
                                                    //onPinCodeSuccess()
                                                    // Log.v("pincode", "status true enable pin")
                                                    /* } else {*/

                                                    //}
                                                }
                                                AppLock.UNLOCK_PIN -> {

                                                    //   Log.v("pincode", "status true unlock pin")
                                                    /*   if (isPassCorrect*//*mLockManager.appLock.checkPasscode(mPinCode)*//*) {*/
                                                    setResult(RESULT_OK)
                                                    onPinCodeSuccess()
                                                    //finish()
                                                    /*  } else {*/

                                                    /*  }*/
                                                }
                                            }
                                        }


                                    } else {
                                        onPinCodeError()
                                        //   Log.v("pincode", "status false")
                                        /*when (mType) {
                                        AppLock.CHANGE_PIN -> {
                                            onPinCodeError()
                                        }
                                        AppLock.UNLOCK_PIN -> {
                                            onPinCodeError()
                                        }
                                    }*/

                                        /*Toast.makeText(
                                    this,
                                    it.data?.errormessage?.message,
                                    Toast.LENGTH_LONG
                                )
                                    .show()*/
                                    }
                                    CommonUtils.hideProgress()

                                }
                                Status.ERROR -> {
                                    CommonUtils.hideProgress()
                                    /* Toast.makeText(this, it.data?.errormessage?.message, Toast.LENGTH_LONG)
                                 .show()*/
                                }
                                Status.LOADING -> {
                                    CommonUtils.showProgress(this)
                                }
                            }
                        }
                    })
           /* }*/
        }
    }

    private fun forgetPin(is_from_disable_pin: Boolean) {
        if (NetworkUtils.isConnected()) {
            if (CommonUtils.isValidClickPressed()) {
                viewModel.forgetPin(loginModel.data?.bearer_access_token).observe(
                    this,
                    Observer {
                        it?.let { resource ->
                            when (resource.status) {
                                Status.SUCCESS -> {
                                    if (it.data?.status == true) {
                                        when (is_from_disable_pin) {
                                            true -> {
                                                Toast.makeText(
                                                    this,
                                                    "InApp PinLock Disabled",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                if (clearPasscodePrefs()) {
                                                    finish()
                                                }

                                            }
                                            false -> {
                                                if (clearPasscodePrefs()) {
                                                    startActivity(
                                                        Intent(
                                                            this@AppLockActivityNew,
                                                            LoginActivity::class.java
                                                        ).putExtra(
                                                            Constants.ErrorCode,
                                                            Constants.ErrorCode
                                                        )
                                                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    )
                                                    finish()
                                                }
                                            }
                                        }


                                    }
                                    CommonUtils.hideProgress()

                                }
                                Status.ERROR -> {
                                    //  CommonUtils.hideProgress()
                                    /* Toast.makeText(this, it.data?.errormessage?.message, Toast.LENGTH_LONG)
                                 .show()*/
                                }
                                Status.LOADING -> {
                                    // CommonUtils.showProgress(this)
                                }
                            }
                        }
                    })
            }
        }
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                CustomPinViewModel::class.java
            )
        binding.setLifecycleOwner(this)
    }

    override fun onKeyboardClick(keyboardButtonEnum: KeyboardButtonEnum) {
        if (mPinCode!!.length < getPinLength()) {
            val value = keyboardButtonEnum.buttonValue
            if (value == KeyboardButtonEnum.BUTTON_CLEAR.buttonValue) {
                if (!mPinCode!!.isEmpty()) {
                    setPinCode(
                        mPinCode!!.substring(
                            0,
                            mPinCode!!.length - 1
                        )
                    )
                } else {
                    setPinCode("")
                }
            } else {
                setPinCode(mPinCode + value)
            }
        }
    }

    fun setPinCode(pinCode: String?) {
        mPinCode = pinCode
        mPinCodeRoundView!!.refresh(mPinCode!!.length)
    }

    override fun onRippleAnimationEnd() {

        if (mPinCode!!.length == getPinLength()) {
           /* Log.v("ripple anim", " end")*/
            CommonUtils.hideInternetDialog()
            onPinCodeInputed()
        }
    }

    protected fun onPinCodeInputed() {
        when (mType) {
            AppLock.DISABLE_PINLOCK -> if (mLockManager.appLock.checkPasscode(mPinCode)) {
                setResult(RESULT_OK)
                forgetPin(true)
            } else {
                onPinCodeError()
            }
            AppLock.ENABLE_PINLOCK -> {
                val passcode: String = mLockManager.appLock.getPasscode(mPinCode)
                if (Constants.passcodeForDemoComp.equals(passcode)) {
                    Toast.makeText(this, "You can not set PIN 0000", Toast.LENGTH_SHORT).show()
                    onPinCodeError()
                } else {
                    mOldPinCode = mPinCode
                    setPinCode("")
                    mType = AppLock.CONFIRM_PIN
                    setStepText()
                    setForgotTextVisibility()
                }

            }
            AppLock.CONFIRM_PIN -> if (mPinCode == mOldPinCode) {
                if (!mPinCode.isNullOrBlank()) {
                    val passcode: String = mLockManager.appLock.getPasscode(mPinCode)
                    //Log.v("passcode",passcode)
                    //  Log.v("passcodeFromConstant",Constants.passcodeForDemoComp)
                    if (Constants.passcodeForDemoComp.equals(passcode)) {
                        Toast.makeText(this, "You can not set PIN 0000", Toast.LENGTH_SHORT).show()
                        onPinCodeError()
                    } else {
                        setResult(RESULT_OK)
                        if (mLockManager.appLock.setPasscode(mPinCode)) {
                            val passcode: String = prefs[Constants.PASSWORD_PREFERENCE_KEY]!!
                            setAppLockPin(passcode)
                            //Log.v("passcode", passcode)
                        }
                    }
                }


                /*if (prefs.contains(Constants.PASSWORD_PREFERENCE_KEY)) {
                    val passcode: String = prefs[Constants.PASSWORD_PREFERENCE_KEY]!!
                    setAppLockPin(passcode)
                }*/

                /*  onPinCodeSuccess()
                finish()*/
            } else {
                mOldPinCode = ""
                setPinCode("")
                mType = AppLock.ENABLE_PINLOCK
                setStepText()
                setForgotTextVisibility()
                onPinCodeError()
            }
            AppLock.CHANGE_PIN -> {
                if (!mPinCode.isNullOrBlank()) {
                    val passcode: String = mLockManager.appLock.getPasscode(mPinCode)
                    if (Constants.passcodeForDemoComp.equals(passcode)) {
                        Toast.makeText(this, "You can not set PIN 0000", Toast.LENGTH_SHORT).show()
                        onPinCodeError()
                    } else {
                        checkPin(passcode)
                    }

                    //    Toast.makeText(this,passcode,Toast.LENGTH_SHORT).show()
                }

                /* if (prefs.contains(Constants.PASSWORD_PREFERENCE_KEY)) {
                    val passcode: String = prefs[Constants.PASSWORD_PREFERENCE_KEY]!!
                    checkPin(passcode)
                }
*/

            }

            AppLock.UNLOCK_PIN -> {
                if (!mPinCode.isNullOrBlank()) {
                    val passcode: String = mLockManager.appLock.getPasscode(mPinCode)
                    checkPin(passcode)
                    // Toast.makeText(this,passcode,Toast.LENGTH_SHORT).show()
                }

                /* if (prefs.contains(Constants.PASSWORD_PREFERENCE_KEY)) {
                    val passcode: String = prefs[Constants.PASSWORD_PREFERENCE_KEY]!!*/

                //}

                /*if (isPassCorrect*//*mLockManager.appLock.checkPasscode(mPinCode)*//*) {
                    setResult(RESULT_OK)
                    onPinCodeSuccess()
                    finish()
                } else {
                    onPinCodeError()
                }*/
            }
            /*else -> {
            }*/
        }
    }

    override fun onAuthenticated() {
        // Log.e(TAG, "Fingerprint READ!!!")
        /*setResult(RESULT_OK)
        onPinCodeSuccess()
        finish()*/
        if (this::prefs.isInitialized) {
            if (prefs.contains(Constants.PASSWORD_PREFERENCE_KEY)) {
                val passcode: String = prefs[Constants.PASSWORD_PREFERENCE_KEY]!!
                /* Toast.makeText(this,passcode,Toast.LENGTH_SHORT).show()*/
                mType = AppLock.UNLOCK_PIN
                checkPin(passcode)
            }

        }
//        val passcode: String = mLockManager.appLock.getPasscode(mPinCode)


    }

    override fun onClick(view: View?) {
        showForgotDialog()
    }

    fun showForgotDialog() {
        forgetPin(false)

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
                // Log.e("passcode pref exception",e.toString())
            }


        }
        return true
    }

    protected fun onPinCodeError() {
        onPinFailure(mAttempts++)
        val thread: Thread = object : Thread() {
            override fun run() {
                mPinCode = ""
                mPinCodeRoundView!!.refresh(mPinCode!!.length)
                val animation = AnimationUtils.loadAnimation(
                    this@AppLockActivityNew, R.anim.shake
                )
                mKeyboardView!!.startAnimation(animation)
            }
        }
        runOnUiThread(thread)
    }

    fun onPinFailure(attempts: Int) {
        isRunning = true;
    }

    protected fun onPinCodeSuccess() {
        isCodeSuccessful = true
        onPinSuccess(mAttempts)
        mAttempts = 1
    }

    override fun onError() {

    }


}