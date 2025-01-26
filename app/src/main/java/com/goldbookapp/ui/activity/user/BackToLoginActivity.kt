package com.goldbookapp.ui.activity.user

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.goldbookapp.R
import com.goldbookapp.databinding.BackToLoginActivityBinding
import com.goldbookapp.ui.MainActivity
import com.goldbookapp.ui.activity.auth.LoginActivity
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.back_to_login_activity.*

class BackToLoginActivity : AppCompatActivity(){
    var isFromRecover:String? = null
    lateinit var binding: BackToLoginActivityBinding
    var selectedCardNo:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.back_to_login_activity)
        //binding.loginModel = LoginModel()


        setupUIandListner()
        /*binding.btnSignup.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this,VerifyPhoneActivity::class.java))
        })*/


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

    private fun setupUIandListner(){

        if(intent.extras != null && intent.extras!!.containsKey("isFromRecover")){
            isFromRecover = intent.getStringExtra("isFromRecover")
        }
        if(intent.extras != null && intent.extras!!.containsKey("selected_card_no")){
            selectedCardNo = intent.getStringExtra("selected_card_no")
        }
        when(selectedCardNo) {
            "2" -> {
                linkSentEmailSms.setText(R.string.linksentemail)
                checkLinkEmailSms.setText(R.string.checklinkemail)
            }
            "1" -> {
                linkSentEmailSms.setText(R.string.linksentsms)
                checkLinkEmailSms.setText(R.string.checklinksms)
            }
        }
        when(isFromRecover){
            "1" -> tvBacktoSignIn.setText("Back to Sign In")
             else -> tvBacktoSignIn.setText("OK")
        }
       /* tvBacktoSignIn.setOnClickListener {
            when(isFromRecover){
                "1" ->  startActivity(Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                else ->  startActivity(Intent(this,LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            }
        }*/
        binding.btnBackToLogin.clickWithDebounce {

            when(isFromRecover){
                "1" -> startActivity(Intent(this, LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK))
                else -> startActivity(Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            }
            /*startActivity(Intent(this,LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))*/
        }
    }
}