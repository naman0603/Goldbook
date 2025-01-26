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
import com.goldbookapp.databinding.UpdateContactActivityBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.ProfileDetailModel
import com.goldbookapp.ui.activity.viewmodel.UpdateContactViewModel
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
import kotlinx.android.synthetic.main.update_contact_activity.*

class UpdateContactActivity : AppCompatActivity(){

    private lateinit var viewModel: UpdateContactViewModel

    private var isOnlyEmail: Boolean = false
     var loginModel: LoginModel? = null
    lateinit var binding: UpdateContactActivityBinding

    lateinit var profileDetailModel: ProfileDetailModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.update_contact_activity)
        //binding.loginModel = LoginModel()

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

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                UpdateContactViewModel::class.java
            )
        //binding.alm = viewModel
    }
    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }
    override fun onResume() {
        super.onResume()

    }

    private fun setupUIandListner(){

        val prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        profileDetailModel = Gson().fromJson(
            prefs[Constants.PREF_PROFILE_DETAIL_KEY, ""],
            ProfileDetailModel::class.java
        ) //getter



        binding.toolbar.imgLeft.setImageResource(R.drawable.ic_back)
        binding.toolbar.tvTitle.setText("Update Contact Information")

        binding.txtMobileUpdate.doAfterTextChanged { tvMobileUpdateInputLayout.error = null }
        binding.txtEmailUpdate.doAfterTextChanged { tvEmailUpdateInputLayout.error = null }

        var details: String = ""
        if(!profileDetailModel?.data?.user?.mobile_no?.isBlank()!! && !profileDetailModel?.data?.user?.email?.isBlank()!!){
            details = "Your current mobile number is " + profileDetailModel?.data?.user?.mobile_no + " and email is " + profileDetailModel?.data?.user?.email
        }else if(!profileDetailModel?.data?.user?.mobile_no?.isBlank()!!){
            details = "Your current mobile number is " + profileDetailModel?.data?.user?.mobile_no
        }else if(!profileDetailModel?.data?.user?.email?.isBlank()!!){
            details = "Your current email is " + profileDetailModel?.data?.user?.email
        }

        binding.tvCurrentMobileEmailContact.setText(details)

        binding.toolbar.imgLeft.clickWithDebounce {
            onBackPressed()
        }



        binding.btnSave.clickWithDebounce {

            if(performValidation()){
                if(NetworkUtils.isConnected()){
                    if(!binding.txtMobileUpdate.text.toString().isBlank() && binding.txtEmailUpdate.text.toString().isBlank()){
                        when(binding.txtMobileUpdate.text.toString().equals(profileDetailModel?.data?.user?.mobile_no)){
                            true->{
                                // same mobile number entered
                                Toast.makeText(this,getString(R.string.enter_new_mobile_to_update_msg),Toast.LENGTH_SHORT).show()
                            }
                            false->{
                                // different new mobile number entered
                                isOnlyEmail = false
                                updateContactAPI(loginModel?.data?.bearer_access_token, binding.txtMobileUpdate.text.toString(), binding.txtEmailUpdate.text.toString(),"")
                                /*startActivity(Intent(this,UpdateContactVerifyOTP::class.java).putExtra(Constants.MobileNo,txtMobileUpdate.text.toString()).putExtra(Constants.Email,txtEmailUpdate.text.toString()))
                                finish()*/
                            }
                        }
                    }

                    else if(binding.txtMobileUpdate.text.toString().isBlank() && !binding.txtEmailUpdate.text.toString().isBlank()){
                        when(binding.txtEmailUpdate.text.toString().equals(profileDetailModel?.data?.user?.email)){
                            true->{
                                // same email entered
                                Toast.makeText(this,getString(R.string.enter_new_email_to_update),Toast.LENGTH_SHORT).show()
                            }
                            false->{
                                // different new email entered
                                isOnlyEmail = true
                                updateContactAPI(loginModel?.data?.bearer_access_token, binding.txtMobileUpdate.text.toString(), txtEmailUpdate.text.toString(),"")
                            }
                        }

                    }else if(!binding.txtMobileUpdate.text.toString().isBlank() && !binding.txtEmailUpdate.text.toString().isBlank()){
                        when(binding.txtEmailUpdate.text.toString().equals(profileDetailModel?.data?.user?.email) &&  binding.txtMobileUpdate.text.toString().equals(profileDetailModel?.data?.user?.mobile_no)){
                            true->{
                                // same mob/email entered
                                Toast.makeText(this,getString(R.string.enter_new_details_to_update_msg),Toast.LENGTH_SHORT).show()
                            }
                            false->{
                                // different new mob/email entered
                                isOnlyEmail = false
                                updateContactAPI(loginModel?.data?.bearer_access_token, binding.txtMobileUpdate.text.toString(), binding.txtEmailUpdate.text.toString(),"")
                            }
                        }

                    }




                }
            }
        }

    }

    fun performValidation(): Boolean {
        if(binding.txtMobileUpdate.text.toString().isBlank() && binding.txtEmailUpdate.text.toString().isBlank()){
            //tvMobileUpdateInputLayout?.error = getString(R.string.mobile_validation_msg)
            Toast.makeText(this,getString(R.string.enter_either_mobno_or_email_msg),Toast.LENGTH_SHORT).show()
            return false
        }

       else if(!binding.txtMobileUpdate.text.toString().isBlank() && binding.txtMobileUpdate.text?.length!! < 10){
            binding.tvMobileUpdateInputLayout?.error = getString(R.string.mobile_digit_count)
            return false
        }else if(!binding.txtEmailUpdate.text.toString().isBlank() && !CommonUtils.isValidEmail(binding.txtEmailUpdate.text.toString())){
            binding.tvEmailUpdateInputLayout?.error = getString(R.string.email_validation_msg)
            return false
        }

        return true
    }

    fun updateContactAPI(token: String?,
                         mobile_no: String?,
                         email: String?,
                            otp:String?){

        viewModel.updateContact(token, mobile_no, email,otp).observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        if (it.data?.status == true) {
                            when(isOnlyEmail){
                                false->{
                                    Toast.makeText(
                                        this,
                                        it.data?.message,
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                    startActivity(Intent(this, UpdateContactVerifyOTP::class.java).putExtra(Constants.MobileNo,txtMobileUpdate.text.toString()).putExtra(Constants.Email,txtEmailUpdate.text.toString()))
                                    finish()
                                }
                                true->{
                                    profileDetailModel.data?.user?.mobile_no = it.data.data!!.user!!.mobile_no
                                    profileDetailModel.data?.user?.email = it.data.data!!.user!!.email

                                    val prefs = PreferenceHelper.defaultPrefs(this)
                                    prefs[Constants.PREF_PROFILE_DETAIL_KEY] = Gson().toJson(profileDetailModel) //setter


                                    Toast.makeText(
                                        this,
                                        it.data?.message,
                                        Toast.LENGTH_LONG
                                    )
                                        .show()

                                    onBackPressed()
                                }
                            }

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