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
import com.goldbookapp.databinding.VerifyPasswordActivityBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.ui.activity.viewmodel.VerifyPasswordViewModel
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
import kotlinx.android.synthetic.main.verify_password_activity.*

class VerifyPasswordActivity : AppCompatActivity(){

    private lateinit var viewModel: VerifyPasswordViewModel

    lateinit var binding: VerifyPasswordActivityBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.verify_password_activity)


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
            setupUIandListner()
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
    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                VerifyPasswordViewModel::class.java
            )
        //binding.alm = viewModel
    }

    private fun setupUIandListner(){

        val prefs = PreferenceHelper.defaultPrefs(this)
        val loginModel: LoginModel? = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        binding.toolbar.imgLeft.setImageResource(R.drawable.ic_back)
        binding.toolbar.tvTitle.setText("Verification")

        binding.txtPasswordVerify.doAfterTextChanged { tvPasswordInputLayout.error = null }

        binding.toolbar.imgLeft.clickWithDebounce {

            onBackPressed()
        }

        binding.btnNext.clickWithDebounce {

            if(performValidation()){
                if(NetworkUtils.isConnected()){
                    verifyPasswordAPI(loginModel?.data?.bearer_access_token, txtPasswordVerify.text.toString().trim())
                }
            }
        }

    }

    fun performValidation(): Boolean {

        if(binding.txtPasswordVerify.text.toString().isBlank() || !CommonUtils.isValidPassword(txtPasswordVerify.text.toString())){
            binding.tvPasswordInputLayout?.error = getString(R.string.password_condition_text_msg)/*"Use 6 or more characters with a mix of upper, lower, numbers & symbols"*/
            return false
        }

        return true
    }

    fun verifyPasswordAPI(token: String?,
                          password: String?){

        viewModel.verifyPassword(token, password).observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {

                        if (it.data?.status == true) {

                            startActivity(Intent(this, UpdateContactActivity::class.java))

                            finish()
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
                        Toast.makeText(this, it.data?.errormessage?.message, Toast.LENGTH_LONG)
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