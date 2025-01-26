package com.goldbookapp.ui.activity.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.goldbookapp.R
import com.goldbookapp.databinding.ForgotPasswordActivityBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.forgot_password_activity.*
import java.util.regex.Pattern


class ForgotPasswordActivity : AppCompatActivity() {

    lateinit var binding: ForgotPasswordActivityBinding
    lateinit var loginModel: LoginModel
    var selectedCardNo: String = "1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.forgot_password_activity)

        setupUIandListener()

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


    private fun setupUIandListener() {
        val prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter
        val mobileNo: String =
            loginModel?.data?.user_info?.mobile_no.toString().replaceRange(1, 7, "******")
        edtForgotPwdMobileNo.setText(mobileNo)


        val email = loginModel?.data?.user_info?.email.toString()
        val pattern = "([^@]+)@(.*)\\.(.*)"

        //email masking
        val r = Pattern.compile(pattern)
        val m = r.matcher(email)
        if (m.find()) {
            val sb = StringBuilder("")
            sb.append(m.group(1)[0])
            sb.append(m.group(1).substring(1).replace(".".toRegex(), "*"))
            sb.append("@")
            sb.append(m.group(2).replace(".".toRegex(), "*"))
            sb.append(".").append(m.group(3))
            println(sb)
            edtForgotPwdEmail.setText(sb)
        }
        llmobile.setBackgroundResource(R.drawable.card_view_border)


        binding.btnCardNext.clickWithDebounce {

            startActivity(
                Intent(
                    this,
                    RecoverAccountActivity::class.java
                ).putExtra("selected_card_no", selectedCardNo)
            )
        }
        cardMobile.clickWithDebounce {
            llmobile.setBackgroundResource(R.drawable.card_view_border)
            llemail.setBackgroundColor(resources.getColor(R.color.gray_bg))

            selectedCardNo = "1"
        }
        cardEmail.clickWithDebounce {

            llemail.setBackgroundResource(R.drawable.card_view_border)
            llmobile.setBackgroundColor(resources.getColor(R.color.gray_bg))
            selectedCardNo = "2"
        }
    }


}