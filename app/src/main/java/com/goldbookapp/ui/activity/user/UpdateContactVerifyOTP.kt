package com.goldbookapp.ui.activity.user

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.VerifyPhoneActivityBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.ProfileDetailModel
import com.goldbookapp.ui.activity.viewmodel.UpdateContactVerifyOTPViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.CommonUtils.Companion.isValidClickPressed
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.goldbookapp.utils.Status
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import com.mukesh.OnOtpCompletionListener


class UpdateContactVerifyOTP : AppCompatActivity() {

    private lateinit var viewModel: UpdateContactVerifyOTPViewModel
    lateinit var profileDetailModel: ProfileDetailModel
    lateinit var binding: VerifyPhoneActivityBinding
    var newMobNo: String? = null
    var newEmail: String? = null

    lateinit var countDownTimer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.verify_phone_activity)
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
                UpdateContactVerifyOTPViewModel::class.java
            )

    }

    private fun setupUIandListner() {

        val prefs = PreferenceHelper.defaultPrefs(this)
        val loginModel: LoginModel? = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        profileDetailModel = Gson().fromJson(
            prefs[Constants.PREF_PROFILE_DETAIL_KEY, ""],
            ProfileDetailModel::class.java
        ) //getter

        if(intent.extras != null && intent.extras!!.containsKey(Constants.MobileNo)){
            newMobNo = intent.getStringExtra(Constants.MobileNo)
            newEmail = intent.getStringExtra(Constants.Email)
        }

        var firstPart: String = "Waiting to automatically detect an SMS\nsent to ";
        //var secondPart: String = "+91-9876543210. ";

                //profile update contact info
        var secondPart: String = "+91-" + newMobNo+ ". "
        var thirdPart: String = "Wrong number?";
        var finalString: String = firstPart + secondPart + thirdPart;
        var spannableStringBuilder: SpannableStringBuilder = SpannableStringBuilder(finalString)


        val clickableTermsSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                if (isValidClickPressed()) {
                    onBackPressed()
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@UpdateContactVerifyOTP, R.color.colorPrimary)
                ds.typeface = ResourcesCompat.getFont(this@UpdateContactVerifyOTP,R.font.proxima_nova_bold)
                ds.isUnderlineText = false
            }

        }


        spannableStringBuilder.setSpan(
            clickableTermsSpan,
            (finalString.length - thirdPart.length),
            finalString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvMobileNumber.setText(spannableStringBuilder)
        binding.tvMobileNumber.setMovementMethod(LinkMovementMethod.getInstance())

        val futureMillis: Long = 60 * 1000 + 1000

        countDownTimer = object : CountDownTimer(futureMillis, 1000) {

            override fun onFinish() {
               /* Log.v("..tick..", "...Done...")*/
                binding.tvTimer.visibility = View.INVISIBLE
                binding.tvDidNotReceiveCode.visibility = View.VISIBLE
                binding.tvResendOTP.visibility = View.VISIBLE
            }

            override fun onTick(millisUntilFinished: Long) {

                binding.tvTimer.visibility = View.VISIBLE
                binding.tvDidNotReceiveCode.visibility = View.GONE
                binding.tvResendOTP.visibility = View.GONE

                var seconds: Int = ((millisUntilFinished / 1000).toInt());
                var minutes = seconds / 60;
                seconds = seconds % 60;
                binding.tvTimer.setText(
                    String.format("%02d", minutes) + ":" + String.format(
                        "%02d",
                        seconds
                    )
                )


            }

        }.start()

        binding.otpView.setOtpCompletionListener(OnOtpCompletionListener {

            updateContactAPI(loginModel?.data?.bearer_access_token, newMobNo, newEmail,it)
        })


        binding.tvResendOTP.clickWithDebounce {

            updateContactAPI(loginModel?.data?.bearer_access_token, newMobNo, newEmail,"")
        }


    }


    fun updateContactAPI(token: String?,
                         mobile_no: String?,
                         email: String?,
                          otp: String?  ){
        if(NetworkUtils.isConnected()){
            viewModel.updateContact(token, mobile_no, email, otp).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                when(otp.isNullOrBlank()){
                                    true->{
                                        Toast.makeText(
                                            this,
                                            it.data?.message,
                                            Toast.LENGTH_LONG
                                        )
                                            .show()
                                    }
                                    false->{
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
                                binding.otpView.setText("")
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



    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
        CommonUtils.hideProgress()
    }

}