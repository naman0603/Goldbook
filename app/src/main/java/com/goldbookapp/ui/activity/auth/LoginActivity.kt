package com.goldbookapp.ui.activity.auth

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.LoginActivityBinding
import com.goldbookapp.inapplock.managers.LockManager
import com.goldbookapp.model.FiscalYearModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.ReviewAppModel
import com.goldbookapp.model.SignupModel
import com.goldbookapp.ui.MainActivity
import com.goldbookapp.ui.activity.settings.AppLockActivityNew
import com.goldbookapp.ui.activity.viewmodel.LoginViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.*
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.login_activity.*
import java.text.SimpleDateFormat
import java.util.*

class LoginActivity : AppCompatActivity() {

    var isFromErrorInternetceptor: Boolean = false;
    lateinit var onoffReceiver: BroadcastReceiver
    lateinit var receiverManager: ReceiverManager
    lateinit var prefs: SharedPreferences
    private lateinit var viewModel: LoginViewModel

    var signupModel = SignupModel()

    lateinit var binding: LoginActivityBinding
    lateinit var loginModel: LoginModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.login_activity)

        setupViewModel()

    }

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected) {
            // Network is available
            CommonUtils.hideInternetDialog()
            setupUIandListner()
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun setupUIandListner() {
        prefs = PreferenceHelper.defaultPrefs(this)
        binding.rlLogin.setOnFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                CommonUtils.hideKeyboardnew(this);
            }
        })
        txtEmail.doAfterTextChanged { txtEmailTextInputLayout.error = null }
        txtPassword.doAfterTextChanged { txtPasswordTextInputLayout.error = null }

        if (intent.extras != null && intent.extras!!.containsKey(Constants.Error)) {
            val prefs = PreferenceHelper.defaultPrefs(this)
            isFromErrorInternetceptor = true;
            if (prefs.contains(Constants.PREF_LOGIN_DETAIL_KEY)) {
                loginModel = Gson().fromJson(
                    prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
                    LoginModel::class.java
                ) //getter
                logout()
            }



            onoffReceiver = ScreenOnOffReceiver.getInstance(this)
            receiverManager = ReceiverManager.init(this)
            try {
                this.unregisterReceiver(onoffReceiver)
            } catch (e: Exception) {
            }

        }


    }

    override fun onBackPressed() {
        //super.onBackPressed()
        AppLockActivityNew.isRunning = false
        if (isFromErrorInternetceptor) {
            finish()
        } else {
            super.onBackPressed()
        }

    }

    private fun logout() {
        if (NetworkUtils.isConnected()) {
            viewModel.logout(loginModel?.data?.bearer_access_token)
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
                                    //openUpdateDialog()
                                    val prefs = PreferenceHelper.defaultPrefs(this)
                                    CommonUtils.clearAllAppPrefs(prefs)
                                    prefs.edit().remove(Constants.PASSWORD_PREFERENCE_KEY).apply()
                                    val lockManager: LockManager<AppLockActivityNew> =
                                        LockManager.getInstance() as LockManager<AppLockActivityNew>
                                    lockManager.getAppLock().logoId = R.mipmap.ic_launcher

                                } else {

                                }
                                CommonUtils.hideProgress()

                            }
                            Status.ERROR -> {

                                val prefs = PreferenceHelper.defaultPrefs(this)
                                CommonUtils.clearAllAppPrefs(prefs)
                                prefs.edit().remove(Constants.PASSWORD_PREFERENCE_KEY).apply()
                                val lockManager: LockManager<AppLockActivityNew> =
                                    LockManager.getInstance() as LockManager<AppLockActivityNew>
                                lockManager.getAppLock().logoId = R.mipmap.ic_launcher
                                CommonUtils.hideProgress()
                                Toast.makeText(
                                    this,
                                    "Logout successfully",
                                    Toast.LENGTH_LONG
                                )
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

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                LoginViewModel::class.java
            )
        binding.loginViewmodel = viewModel
    }

    //setupObservers()
    fun loginClicked(view: View) {
        if (NetworkUtils.isConnected()) {
            Constants.apicallcount = 0
            if (performValidation()) {
                viewModel.getLoginData(txtEmail.text.toString(), txtPassword.text.toString().trim())
                    .observe(this, Observer {
                        it?.let { resource ->
                            when (resource.status) {
                                Status.SUCCESS -> {
                                    CommonUtils.hideProgress()

                                    if (it.data?.status == true) {
                                        it.data.data?.bearer_access_token =
                                            "Bearer " + it.data.data?.access_token

                                        if (it.data.data?.step == "1") {


                                            if (prefs.contains(Constants.PREF_COMPANY_REGISTER_KEY)) {
                                                // already pref have signup data
                                            } else {
                                                // filled signup data from login model to signup model
                                                prefs[Constants.PREF_COMPANY_REGISTER_KEY] =
                                                    Gson().toJson(signupModel) //setter
                                            }
                                            signupModel = Gson().fromJson(
                                                prefs[Constants.PREF_COMPANY_REGISTER_KEY, ""],
                                                SignupModel::class.java
                                            ) //getter
                                            signupModel?.data?.user?.userInfo?.username =
                                                txtEmail.text.toString().trim()
                                            signupModel?.data?.user?.userInfo?.password =
                                                txtPassword.text.toString().trim()
                                            signupModel?.data?.user?.userInfo?.company_id =
                                                it.data.data?.company_info?.id
                                            signupModel.data?.user?.userInfo?.bearer_access_token =
                                                "Bearer " + it.data.data?.access_token
                                            //signupModel?.data?.company?.companyInfo?.mobile_no = txtMobileSignup.text.toString().trim()


                                            prefs[Constants.PREF_COMPANY_REGISTER_KEY] =
                                                Gson().toJson(signupModel) //setter
                                            startActivity(
                                                Intent(
                                                    this,
                                                    AlmostThereActivity::class.java
                                                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            )
                                            Toast.makeText(
                                                this,
                                                getString(R.string.complete_registration_msg),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {

                                            //val prefs = PreferenceHelper.defaultPrefs(this)
                                            prefs[Constants.PREF_LOGIN_DETAIL_KEY] =
                                                Gson().toJson(it.data) //setter
                                            startActivity(
                                                Intent(
                                                    this,
                                                    MainActivity::class.java
                                                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            )
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

    private fun getDateforReviewApp() {
        val sdf = SimpleDateFormat(
            "yyyy-MM-dd"
        )
        val date = Date()
        val start_date = "2023-01-15"

        val c = Calendar.getInstance()
        c.time = sdf.parse(start_date)
        c.add(Calendar.DATE, 1)
        val end_date = sdf.format(c.time)

        /*val childModel = ReviewAppModel(start_date, end_date, "true")
        prefs[Constants.ReviewApp] = Gson().toJson(childModel)*/

    }

    fun openForgotPassword(view: View) {
        startActivity(Intent(this, RecoverAccountActivity::class.java))
    }

    fun openSignup(view: View) {

        startActivity(Intent(this, SignupActivity::class.java))
    }

    fun performValidation(): Boolean {

        if (txtEmail.text.toString().isBlank()) {
            txtEmailTextInputLayout?.error = "Invalid Username"
            return false
        } else if (txtPassword.text.toString()
                .isBlank() /*|| !CommonUtils.isValidPassword(txtPassword.text.toString())*/
        ) {
            txtPasswordTextInputLayout?.error = "Incorrect Password"
            return false
        }

        return true
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

}