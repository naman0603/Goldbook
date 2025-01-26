package com.goldbookapp.ui.activity.user

import android.content.Intent
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
import com.goldbookapp.model.SignupModel
import com.goldbookapp.ui.activity.auth.AlmostThereActivity
import com.goldbookapp.ui.activity.viewmodel.VerifyPhoneOTPViewModel
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


class VerifyPhoneActivity : AppCompatActivity() {

    private lateinit var viewModel: VerifyPhoneOTPViewModel

    lateinit var binding: VerifyPhoneActivityBinding
    lateinit var countDownTimer: CountDownTimer
    var otp_mobile :String = ""
    var otp_email :String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.verify_phone_activity)


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
                VerifyPhoneOTPViewModel::class.java
            )

    }

    private fun setupUIandListner() {

        val prefs = PreferenceHelper.defaultPrefs(this)
        val signupModel: SignupModel? = Gson().fromJson(
            prefs[Constants.PREF_COMPANY_REGISTER_KEY, ""],
            SignupModel::class.java
        ) //getter


        var firstPart: String = "Enter the OTP sent to ";
        //var secondPart: String = "+91-9876543210. ";
        var secondPart: String = "+91-" + signupModel?.data?.user?.userInfo?.mobile_no + " and " + signupModel?.data?.user?.userInfo?.email+". "
        var thirdPart: String = "Wrong Number/Email?";
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
                ds.color = ContextCompat.getColor(this@VerifyPhoneActivity, R.color.colorPrimary)
                ds.typeface = ResourcesCompat.getFont(this@VerifyPhoneActivity,R.font.proxima_nova_bold)
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

        val futureMillis: Long = 180 * 1000 + 1000

        countDownTimer = object : CountDownTimer(futureMillis, 1000) {

            override fun onFinish() {

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
        //    Log.v("..OTP..", "......" + it)
             otp_mobile = it

        })

        binding.otpViewEmail.setOtpCompletionListener(OnOtpCompletionListener {
         //   Log.v("..OTP..Email", "......" + it)
            otp_email = it

        })


        binding.imgVerifyOtp.clickWithDebounce{

            verifyOTPAPI(otp_mobile, signupModel,otp_email)
        }

        binding.tvResendOTP.clickWithDebounce {
            resendOTPAPI(signupModel)
        }


    }

    fun verifyOTPAPI(otp: String, signupModel: SignupModel?,otp_email:String) {
        if(NetworkUtils.isConnected()) {
            viewModel.verifyOTP(
                otp,
                signupModel?.data?.user?.userInfo?.mobile_no,
                signupModel?.data?.user?.userInfo?.name,
                signupModel?.data?.user?.userInfo?.email,
                signupModel?.data?.user?.userInfo?.password,
                signupModel?.data?.user?.userInfo?.username,
                otp_email
            ).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {

                            if (it.data?.status == true) {


                                signupModel?.data?.user?.userInfo?.company_id = it.data?.data?.company?.company_id
                                signupModel?.data?.user?.userInfo?.bearer_access_token = "Bearer " + it.data?.data?.user_info?.token?.access_token

                                val prefs = PreferenceHelper.defaultPrefs(this)
                                prefs[Constants.PREF_COMPANY_REGISTER_KEY] = Gson().toJson(signupModel) //setter


                                startActivity(Intent(this, AlmostThereActivity::class.java))

                            } else {

                                when(it.data!!.code.toString().equals(Constants.ErrorCode.toString())){
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

    fun resendOTPAPI(signupModel: SignupModel?) {
        if(NetworkUtils.isConnected()){
                viewModel.companyRegister(
                    signupModel?.data?.user?.userInfo?.name,
                    signupModel?.data?.user?.userInfo?.password,
                    signupModel?.data?.user?.userInfo?.email,
                    signupModel?.data?.user?.userInfo?.mobile_no,
                    signupModel?.data?.user?.userInfo?.username,
                    true
                ).observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {

                                if (it.data?.status == true) {

                                    binding.tvTimer.setText("");
                                    countDownTimer.start()


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

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
        CommonUtils.hideProgress()
    }

}