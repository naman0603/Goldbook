package com.goldbookapp.ui.activity.user

import android.content.Intent
import android.os.Bundle
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
import com.goldbookapp.databinding.UpdatePasswordActivityBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.UpdatePasswordModel
import com.goldbookapp.ui.activity.auth.ForgotPasswordActivity
import com.goldbookapp.ui.activity.auth.LoginActivity
import com.goldbookapp.ui.activity.viewmodel.UpdatePasswordViewModel
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
import kotlinx.android.synthetic.main.update_password_activity.*

class UpdatePasswordActivity : AppCompatActivity() {

    lateinit var binding: UpdatePasswordActivityBinding
    private lateinit var viewModel: UpdatePasswordViewModel
    lateinit var loginModel: LoginModel
    lateinit var updatePasswordModel: UpdatePasswordModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.update_password_activity)

        setupViewModel()

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

    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }

    private fun setupUIandListener() {
        val prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        binding.toolbar.imgLeft.setImageResource(R.drawable.ic_back)
        binding.toolbar.tvTitle.setText("Update Password")


        binding.toolbar.imgLeft.clickWithDebounce {
            onBackPressed()
        }

        binding.uPwdtvCurrentPassword.doAfterTextChanged { txtUPwdCurrentPassword.error = null }
        binding.uPwdtvPassword.doAfterTextChanged { txtUPwdNewPassword.error = null }
        binding.uPwdtvConfirmPassword.doAfterTextChanged { txtUPwdConfirmPassword.error = null }

        binding.tvForgotPassword.clickWithDebounce {

            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.btnUpdatePassword.clickWithDebounce {

            updatePasswordAPI(
                loginModel?.data?.bearer_access_token,
                uPwdtvCurrentPassword.text.toString(),
                uPwdtvPassword.text.toString(),
                uPwdtvConfirmPassword.text.toString()
            )
        }
    }

    private fun updatePasswordAPI(
        token: String?,
        current_password: String?,
        password: String?,
        password_confirmation: String?
    ) {
        if (NetworkUtils.isConnected()) {
            if (performValidation()) {
                viewModel.updatePassword(token, current_password, password, password_confirmation)
                    .observe(this, Observer {
                        it?.let { resource ->
                            when (resource.status) {
                                Status.SUCCESS -> {
                                    CommonUtils.hideProgress()

                                    if (it.data?.status == true) {
                                        updatePasswordModel = it.data
                                        Toast.makeText(
                                            this,
                                            getString(R.string.pwd_updated_successfully_msg),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val prefs = PreferenceHelper.defaultPrefs(this)
                                        prefs.edit().remove(Constants.PREF_LOGIN_DETAIL_KEY).apply()


                                        startActivity(
                                            Intent(
                                                this,
                                                LoginActivity::class.java
                                            ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        )

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

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                UpdatePasswordViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }

    fun performValidation(): Boolean {
        if (uPwdtvCurrentPassword.text.toString().isBlank()) {
            txtUPwdCurrentPassword?.error = "Please Enter Current Password"
            return false
        } else if (uPwdtvPassword.text.toString()
                .isBlank() && !uPwdtvConfirmPassword.text.toString().isBlank()
        ) {
            txtUPwdNewPassword?.error = "Password Can't be Blank"
            return false
        } else if (!uPwdtvPassword.text.toString()
                .isBlank() && uPwdtvConfirmPassword.text.toString().isBlank()
        ) {
            txtUPwdConfirmPassword?.error = "Password Can't be Blank"
            return false
        } else if (uPwdtvPassword.text.toString().isBlank() && uPwdtvConfirmPassword.text.toString()
                .isBlank()
        ) {
            txtUPwdConfirmPassword?.error = "Password Can't be Blank"
            txtUPwdNewPassword?.error = "Password Can't be Blank"
            return false
        } else if (!CommonUtils.isValidPassword(uPwdtvPassword.text.toString())) {
            txtUPwdNewPassword?.error =
                "Use 6 or more characters with a mix of upper, lower, numbers & symbols"
            return false
        } else if (!CommonUtils.isValidPassword(uPwdtvConfirmPassword.text.toString())) {
            txtUPwdConfirmPassword?.error =
                "Use 6 or more characters with a mix of upper, lower, numbers & symbols"
            return false
        } else if (!uPwdtvPassword.text.toString().equals(uPwdtvConfirmPassword.text.toString())) {
            txtUPwdConfirmPassword?.error = "Password Doesn't Match"
            txtUPwdNewPassword?.error = "Password Doesn't Match"
            return false
        } else if (uPwdtvPassword.text.toString().equals(uPwdtvConfirmPassword.text.toString())) {
            return true
        }

        return true
    }
}