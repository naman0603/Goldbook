package com.goldbookapp.ui.activity.auth

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
import com.goldbookapp.databinding.RecoverAccountActivityBinding
import com.goldbookapp.ui.activity.user.BackToLoginActivity
import com.goldbookapp.ui.activity.viewmodel.RecoverAccountViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.Status
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.recover_account_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class RecoverAccountActivity : AppCompatActivity(){

    private lateinit var viewModel: RecoverAccountViewModel
     var selectedCardNo:String? = null
    lateinit var binding: RecoverAccountActivityBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.recover_account_activity)

        setupViewModel()
        setupUIandListner()

    }
    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }
    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                RecoverAccountViewModel::class.java
            )

    }

    private fun setupUIandListner(){

        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.recover_account)

        binding.txtEmailMobile.doAfterTextChanged { tvEmailMobileInputLayout.error = null
            if(!binding.txtEmailMobile.text.toString().isBlank() && CommonUtils.isValidMobile(binding.txtEmailMobile.text.toString())){
                selectedCardNo = "1"
            }
            else selectedCardNo = "2"
        }

        btnReset?.clickWithDebounce {

            if(performValidation()) {
                //startActivity(Intent(this, BackToLoginActivity::class.java))
                if(NetworkUtils.isConnected()){
                    forgotPasswordAPI(binding.txtEmailMobile.text.toString())
                }else{
                    CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))
                }
            }
        }
        if(intent.extras != null && intent.extras!!.containsKey("selected_card_no")){
            selectedCardNo = intent.getStringExtra("selected_card_no")
        }
        when(selectedCardNo){
            "1" -> {
                binding.tvEmailMobileInputLayout.hint = "Enter Mobile No."
                recoverAccountTitle.setText(R.string.recover_account_text_1)
            }
            "2" -> {
                binding.tvEmailMobileInputLayout.hint = "Enter Email"
                recoverAccountTitle.setText(R.string.recover_account_text_2)
            }
        }

        imgLeft?.clickWithDebounce {

            onBackPressed()
        }

    }

    fun performValidation(): Boolean {
        when(selectedCardNo){
            "1" -> {
                if (binding.txtEmailMobile.text.toString().isBlank()) {
                    binding.tvEmailMobileInputLayout?.error = "Please Enter Mobile number"
                    return false
                }else if(!binding.txtEmailMobile.text.toString().isBlank() && !CommonUtils.isValidMobile(binding.txtEmailMobile.text.toString())){
                    binding.tvEmailMobileInputLayout?.error = "Please Enter Mobile number"
                    return false
                }
            }
            "2" -> {
                if (binding.txtEmailMobile.text.toString().isBlank()) {
                    binding.tvEmailMobileInputLayout?.error = "Please Enter Email"
                    return false
                }
                else if(!binding.txtEmailMobile.text.toString().isBlank() && !CommonUtils.isValidEmail(binding.txtEmailMobile.text.toString())){
                    binding.tvEmailMobileInputLayout?.error = "Please Enter Valid Email"
                    return false
                }
            }
        }
        return true
    }

    fun forgotPasswordAPI(email_mobile: String?){
        if(NetworkUtils.isConnected()){
            viewModel.forgotPassword(email_mobile).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                /*onBackPressed()*/
                                startActivity(Intent(this, BackToLoginActivity::class.java).putExtra("isFromRecover","1").putExtra("selected_card_no",selectedCardNo))
                                finish()

                                Toast.makeText(
                                    this,
                                    it.data?.message,
                                    Toast.LENGTH_LONG
                                )
                                    .show()

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

        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))
        }
    }
}