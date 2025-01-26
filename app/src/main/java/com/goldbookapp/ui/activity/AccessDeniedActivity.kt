package com.goldbookapp.ui

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.goldbookapp.R
import com.goldbookapp.databinding.BackToLoginActivityBinding
import com.goldbookapp.ui.activity.settings.PrivacyPolicyActivity
import com.goldbookapp.ui.activity.auth.LoginActivity
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.CommonUtils.Companion.isValidClickPressed
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents

class AccessDeniedActivity : AppCompatActivity() {
    var isFromFourZeroThree: String? = null
    var isFromListRestrict: String? = null

    var errormsg: String? = null
    lateinit var binding: BackToLoginActivityBinding
    var selectedCardNo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.back_to_login_activity)

        setupUIandListner()

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


    private fun setupUIandListner() {
        binding.btlImage.visibility = View.GONE
        binding.bt2Image.visibility = View.VISIBLE
        binding.tvBacktoSignIn.setText(R.string.ok)
        binding.linkSentEmailSms.setText(R.string.access_denied)
        binding.checkLinkEmailSms.setText(R.string.access_denied_errormsg)



        if (intent.extras != null && intent.extras!!.containsKey(Constants.AccessDeniedCode)) {
            isFromFourZeroThree = intent.getStringExtra(Constants.AccessDeniedCode)
            // clearing prefs
            val prefs = PreferenceHelper.defaultPrefs(this)
            CommonUtils.clearAllAppPrefs(prefs)
            binding.btnBackToLogin.clickWithDebounce {

                startActivity(
                    Intent(
                        this,
                        LoginActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
        if (intent.extras != null && intent.extras!!.containsKey(Constants.isFromListRestrict)) {
            isFromListRestrict = intent.getStringExtra(Constants.isFromListRestrict)
            errormsg = intent.getStringExtra((Constants.restrict_msg))
            binding.checkLinkEmailSms.setText(errormsg)
            binding.btnBackToLogin.clickWithDebounce {

                onBackPressed()
            }
        }

    }

    private fun setupclickablespan(accessDeniedActivity: AccessDeniedActivity) {
        val firstPart: String = resources.getString(R.string.signuprestrictmsg)
        val secondPart: String = " "
        val thirdPart: String = "www.goldbook.in"
        val finalString: String = firstPart + secondPart + thirdPart

        var spannableStringBuilder: SpannableStringBuilder = SpannableStringBuilder(finalString)

        val clickablewebsite = object : ClickableSpan() {
            override fun onClick(widget: View) {
                if (isValidClickPressed()) {
                startActivity(
                    Intent(
                        accessDeniedActivity,
                        PrivacyPolicyActivity::class.java
                    ).putExtra("weburi", "https://goldbook.in").putExtra(
                        "title", getString(
                            R.string.golbook
                        )
                    )
                )
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(accessDeniedActivity, R.color.colorPrimary)
                ds.typeface =
                    ResourcesCompat.getFont(accessDeniedActivity, R.font.proxima_nova_regular)
                ds.isUnderlineText = false
            }

        }
        spannableStringBuilder.setSpan(
            clickablewebsite,
            68,
            83,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.checkLinkEmailSms.setText(spannableStringBuilder)
        binding.checkLinkEmailSms.setMovementMethod(LinkMovementMethod.getInstance())
    }

    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }

    override fun onBackPressed() {
        if (shouldAllowBack()) {
            super.onBackPressed();
        } else {

        }
    }

    private fun shouldAllowBack(): Boolean {
        if (isFromListRestrict.equals("1")) {
            return true
        } else
            return false
    }
}