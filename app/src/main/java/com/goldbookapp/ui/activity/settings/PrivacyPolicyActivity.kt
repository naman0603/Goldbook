package com.goldbookapp.ui.activity.settings

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.goldbookapp.R
import com.goldbookapp.databinding.PrivacyPolicyActivityBinding
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.MyWebViewClient
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import hk.ids.gws.android.sclick.SClick
import kotlinx.android.synthetic.main.privacy_policy_activity.view.*
import kotlinx.android.synthetic.main.toolbar_layout.*


class PrivacyPolicyActivity : AppCompatActivity(){

    private  var weburl: String? = null
    private var title: String? = null
    lateinit var binding: PrivacyPolicyActivityBinding
    val MAX_PROGRESS = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.privacy_policy_activity)
        //binding.loginModel = LoginModel()
        val view = binding.root

        imgLeft.setImageResource(R.drawable.ic_back)

        imgLeft.clickWithDebounce {
            onBackPressed() }


        if(intent.extras?.containsKey("weburi")!!){
            weburl = intent.getStringExtra("weburi")
            title = intent.getStringExtra("title")
            tvTitle.setText(title)

        }

        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent) {
                if (ConnectivityStateHolder.isConnected) {
                    // Network is available
                    CommonUtils.hideInternetDialog()
                    setupUIandListner(view)
                }

                if (!ConnectivityStateHolder.isConnected) {
                    // Network is not available
                    CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

                }
            }
        })

    }


    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }
    private fun setupUIandListner(view: View) {
        initWebview(view)
        setWebClient(view)
        binding.webviewPrivacy.webViewClient = MyWebViewClient(this@PrivacyPolicyActivity)
        loadUrl(weburl)
    }

    private fun loadUrl(pageUrl: String?) {
        binding.webviewPrivacy.loadUrl(pageUrl!!)
    }

    private fun setWebClient(view: View) {
        view.webviewPrivacy.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(
                    view: WebView,
                    newProgress: Int
                ) {
                    super.onProgressChanged(view, newProgress)
                    if (newProgress < MAX_PROGRESS) {
                        CommonUtils.showProgress(this@PrivacyPolicyActivity)
                    }
                    if (newProgress == MAX_PROGRESS) {
                        CommonUtils.hideProgress()
                    }
                }
            }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebview(view: View) {

            view.webviewPrivacy.settings.javaScriptEnabled = true
            view.webviewPrivacy.settings.loadWithOverviewMode = true
            view.webviewPrivacy.settings.useWideViewPort = true
            view.webviewPrivacy.settings.domStorageEnabled = true
            view.webviewPrivacy.webViewClient = object : WebViewClient() {
                override
                fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                    /*handler?.proceed()*/
                    val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this@PrivacyPolicyActivity)
                    var message = "SSL Certificate error."
                    when (error!!.primaryError) {
                        SslError.SSL_UNTRUSTED -> message =
                            "The certificate authority is not trusted."
                        SslError.SSL_EXPIRED -> message = "The certificate has expired."
                        SslError.SSL_IDMISMATCH -> message = "The certificate Hostname mismatch."
                        SslError.SSL_NOTYETVALID -> message = "The certificate is not yet valid."
                    }
                    message += " Do you want to continue anyway?"

                    builder.setTitle("SSL Certificate Error")
                    builder.setMessage(message)
                    builder.setPositiveButton("continue",
                        DialogInterface.OnClickListener { dialog, which -> handler!!.proceed() })
                    builder.setNegativeButton("cancel",
                        DialogInterface.OnClickListener { dialog, which -> handler!!.cancel() })
                    val dialog: android.app.AlertDialog? = builder.create()
                    dialog!!.show()
                }
            }


    }
}