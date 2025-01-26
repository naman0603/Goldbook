package com.goldbookapp.ui.activity.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivitySubscriptionBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.ui.activity.viewmodel.SubscriptionViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.CommonUtils.Companion.isValidClickPressed
import com.goldbookapp.utils.PreferenceHelper.get
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_subscription.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class SubscriptionActivity : AppCompatActivity() {
    private lateinit var viewModel: SubscriptionViewModel
    lateinit var binding: ActivitySubscriptionBinding
    lateinit var loginModel: LoginModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_subscription)

        setupViewModel()
        setupUIandListener()
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                SubscriptionViewModel::class.java
            )
        binding.setLifecycleOwner(this)
    }

    private fun setupUIandListener() {
        val prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter
        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(resources.getString(R.string.subscription))
        imgLeft?.clickWithDebounce {
            onBackPressed()
        }

        var firstPart: String = "1. Go to ";
        var secondPart: String = "www.goldbook.in";
        var thirdPart: String = "If you need any assistance, contact us at "
        var forthPart: String = "support@goldbook.in"
        var fifthPart: String = "Read more ";
        var sixthPart: String = "about the available plans";
        var finalString: String = firstPart + secondPart
        var finalStringNew: String = thirdPart + forthPart
        var finalStringOld: String = fifthPart + sixthPart

        var spannableStringBuilder: SpannableStringBuilder = SpannableStringBuilder(finalString)
        var spannableStringBuilderNew: SpannableStringBuilder =
            SpannableStringBuilder(finalStringNew)
        var spannableStringBuilderOld: SpannableStringBuilder =
            SpannableStringBuilder(finalStringOld)

        val clickablewebsite = object : ClickableSpan() {
            override fun onClick(widget: View) {
                if (isValidClickPressed()) {

                    startActivity(
                        Intent(
                            this@SubscriptionActivity,
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
                ds.color = ContextCompat.getColor(this@SubscriptionActivity, R.color.colorPrimary)
                ds.typeface =
                    ResourcesCompat.getFont(this@SubscriptionActivity, R.font.proxima_nova_regular)
                ds.isUnderlineText = false
            }

        }
        val clickableEmail = object : ClickableSpan() {
            override fun onClick(widget: View) {
                if (isValidClickPressed()) {
                    composeEmail(arrayOf("support@goldbook.in"), getAppName())
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@SubscriptionActivity, R.color.colorPrimary)
                ds.typeface =
                    ResourcesCompat.getFont(this@SubscriptionActivity, R.font.proxima_nova_regular)
                ds.isUnderlineText = false
            }

        }
        val clickableReadMore = object : ClickableSpan() {
            override fun onClick(widget: View) {
                if (isValidClickPressed()) {

                    startActivity(
                        Intent(
                            this@SubscriptionActivity,
                            PrivacyPolicyActivity::class.java
                        ).putExtra("weburi", "https://goldbook.in/#pricing").putExtra(
                            "title", getString(
                                R.string.golbook
                            )
                        )
                    )
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@SubscriptionActivity, R.color.colorPrimary)
                ds.typeface =
                    ResourcesCompat.getFont(this@SubscriptionActivity, R.font.proxima_nova_regular)
                ds.isUnderlineText = false
            }

        }

        spannableStringBuilder.setSpan(
            clickablewebsite,
            9,
            24,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableStringBuilderNew.setSpan(
            clickableEmail,
            42,
            61,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableStringBuilderOld.setSpan(
            clickableReadMore,
            0,
            10,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        subs_first_line.setText(spannableStringBuilder)
        subs_first_line.setMovementMethod(LinkMovementMethod.getInstance())

        subs_fifth_line.setText(spannableStringBuilderNew)
        subs_fifth_line.setMovementMethod(LinkMovementMethod.getInstance())

        subs_extra_line.setText(spannableStringBuilderOld)
        subs_extra_line.setMovementMethod(LinkMovementMethod.getInstance())
    }

    // to get app name
    fun Context.getAppName(): String = applicationInfo.loadLabel(packageManager).toString()

    // to compose email
    fun composeEmail(
        addresses: Array<String?>?,
        subject: String?
    ) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:") // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
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
            getWebLinksFromApi()
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }


    private fun getWebLinksFromApi() {
        if (NetworkUtils.isConnected()) {

            viewModel.activeplan(loginModel?.data?.bearer_access_token).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {

                                binding.subsCurrentplan.setText(it.data.data!!.plan_name)

                            } else {

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
}